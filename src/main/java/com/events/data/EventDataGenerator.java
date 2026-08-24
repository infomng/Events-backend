package com.events.data;

import com.events.modules.category.entity.Category;
import com.events.modules.category.repository.ICategoryRepository;
import com.events.modules.country.entity.Country;
import com.events.modules.country.repository.ICountryRepository;
import com.events.modules.event.entity.Event;
import com.events.modules.event.enumeration.EventStatusEnum;
import com.events.modules.event.repository.IEventRepository;
import com.events.modules.user.entity.User;
import com.events.modules.user.repository.IUserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import net.datafaker.Faker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class EventDataGenerator implements IDataGenerator {

    private static final Logger log = LoggerFactory.getLogger(EventDataGenerator.class);
    private static final int BATCH_SIZE = 500; // Optimal batch size for JPA
    private final IEventRepository eventRepository;
    private final IUserRepository userRepository;
    private final ICategoryRepository categoryRepository;
    private final ICountryRepository countryRepository;
    private final Faker faker = new Faker(new Locale("en-GB"));
    private final Random random = new Random();

    @Override
    @Transactional
    public void generate(int numberOfRecords) {
        log.info("Starting generation of {} dummy events...", numberOfRecords);

        List<User> organizers = ensureMinimumOrganizers(10);
        List<Category> categories = ensureMinimumCategories(5);
        List<Country> countries = ensureMinimumCountries(5);

        if (organizers.isEmpty() || categories.isEmpty() || countries.isEmpty()) {
            log.error("Cannot generate events: Missing organizers, categories, or countries.");
            return;
        }

        List<Event> eventsToSave = new ArrayList<>(BATCH_SIZE);
        for (int i = 0; i < numberOfRecords; i++) {
            Event event = createRandomEvent(organizers, categories, countries);
            eventsToSave.add(event);

            if (eventsToSave.size() == BATCH_SIZE) {
                eventRepository.saveAll(eventsToSave);
                eventsToSave.clear();
                log.info("Generated and saved {} events. Total progress: {}/{}", BATCH_SIZE, i + 1, numberOfRecords);
            }
        }
        // Save any remaining events
        if (!eventsToSave.isEmpty()) {
            eventRepository.saveAll(eventsToSave);
            log.info("Generated and saved {} remaining events. Total progress: {}/{}", eventsToSave.size(), numberOfRecords, numberOfRecords);
        }

        log.info("Finished generation of {} dummy events.", numberOfRecords);
    }

    private Event createRandomEvent(List<User> organizers, List<Category> categories, List<Country> countries) {
        LocalDateTime startDate = faker.date().future(365, TimeUnit.DAYS).toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        LocalDateTime endDate = startDate.plusHours(faker.number().numberBetween(1, 12));
        LocalDateTime ticketSalesStartDate = startDate.minusDays(faker.number().numberBetween(1, 30));
        LocalDateTime ticketSalesEndDate = endDate.minusDays(faker.number().numberBetween(1, 5));

        // compute methods are called via @PrePersist/@PreUpdate, so no need to call them explicitly here
        return Event.builder()
                .name(faker.book().title())
                .description(faker.lorem().paragraph(3))
                .isPublic(faker.bool().bool())
                .isFeatured(faker.bool().bool())
                .hasInvitationCode(faker.bool().bool())
                .latitude(Double.valueOf(faker.address().latitude()))
                .longitude(Double.valueOf(faker.address().longitude()))
                .ticketSalesStartDate(ticketSalesStartDate)
                .ticketSalesEndDate(ticketSalesEndDate)
                .hasSeats(faker.bool().bool())
                .location(faker.address().fullAddress())
                .startDate(startDate)
                .endDate(endDate)
                .status(EventStatusEnum.values()[random.nextInt(EventStatusEnum.values().length)])
                .organizer(organizers.get(random.nextInt(organizers.size())))
                .category(categories.get(random.nextInt(categories.size())))
                .country(countries.get(random.nextInt(countries.size())))
                .build();
    }

    @Transactional
    private List<User> ensureMinimumOrganizers(int minCount) {
        List<User> existingOrganizers = userRepository.findAll();
        if (existingOrganizers.size() < minCount) {
            log.info("Creating {} dummy organizers...", minCount - existingOrganizers.size());
            for (int i = existingOrganizers.size(); i < minCount; i++) {
                User user = User.builder()
                        .fullName(faker.name().firstName())
                        .email(faker.internet().emailAddress())
                        .password(faker.internet().password()) // In a real app, this should be encoded
                        .build();
                existingOrganizers.add(user);
            }
            userRepository.saveAll(existingOrganizers);
            log.info("Finished creating dummy organizers.");
        }
        return existingOrganizers;
    }

    @Transactional
    private List<Category> ensureMinimumCategories(int minCount) {
        List<Category> existingCategories = categoryRepository.findAll();
        if (existingCategories.size() < minCount) {
            log.info("Creating {} dummy categories...", minCount - existingCategories.size());
            for (int i = existingCategories.size(); i < minCount; i++) {
                Category category = Category.builder()
                        .name(faker.book().genre())
                        .description(faker.lorem().sentence())
                        .build();
                existingCategories.add(category);
            }
            categoryRepository.saveAll(existingCategories);
            log.info("Finished creating dummy categories.");
        }
        return existingCategories;
    }

    @Transactional
    private List<Country> ensureMinimumCountries(int minCount) {
        List<Country> existingCountries = countryRepository.findAll();
        if (existingCountries.size() < minCount) {
            log.info("Creating {} dummy countries...", minCount - existingCountries.size());
            for (int i = existingCountries.size(); i < minCount; i++) {
                Country country = Country.builder()
                        .name(faker.address().country())
                        .build();
                existingCountries.add(country);
            }
            countryRepository.saveAll(existingCountries);
            log.info("Finished creating dummy countries.");
        }
        return existingCountries;
    }
}

package com.events.modules.datageneration.service;

import com.events.modules.event.dto.CreateEventCommandDto;
import com.events.modules.event.dto.PriceCategoryDto;
import com.events.modules.event.entity.Event;
import com.events.modules.event.service.impl.EventService;
import lombok.RequiredArgsConstructor;
import net.datafaker.Faker;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Transactional
@Service
public class FakeDataGenerationService implements IFakeDataGenerationService{

    private final EventService eventService;

    @Override
    public void generateEvents(int numberOfEvents) {
        Faker faker = new Faker();

        List<CreateEventCommandDto> events = new ArrayList<>();

        for(int i = 0; i < numberOfEvents; i++){
            CreateEventCommandDto event = CreateEventCommandDto.builder()
                    .name(faker.book().title())
                    .description(faker.lorem().paragraph())
                    .isPublic(faker.bool().bool())
                    .isFreeEntry(faker.bool().bool())
                    .hasInvitationCode(faker.bool().bool())
                    .isInvitationCodeUnique(faker.bool().bool() ? faker.code().isbn10() : null)
                    .location(faker.address().fullAddress())
                    .startDate(faker.date().future(365, java.util.concurrent.TimeUnit.DAYS).toLocalDateTime())
                    .endDate(faker.date().future(366, java.util.concurrent.TimeUnit.DAYS).toLocalDateTime())
                    .totalTickets(faker.number().numberBetween(50, 500))
                    .ticketSalesStartDate(faker.date().future(30, java.util.concurrent.TimeUnit.DAYS).toLocalDateTime())
                    .ticketSalesEndDate(faker.date().future(60, java.util.concurrent.TimeUnit.DAYS).toLocalDateTime())
                    .ticketPrice(faker.number().randomDouble(2, 10, 100))
                    .hasSeats(faker.bool().bool())
                    .priceCategories(List.of(
                            PriceCategoryDto.builder()
                                    .name("Standard")
                                    .price(BigDecimal.valueOf(faker.number().randomDouble(2, 10, 100)))
                                    .build(),
                            PriceCategoryDto.builder()
                                    .name("VIP")
                                    .price(BigDecimal.valueOf(faker.number().randomDouble(2, 30, 100)))
                                    .build()
                    ))
                    .categoryId(faker.options().option(
                            UUID.fromString("11eb512b-efed-4c51-bf07-9baedcb0913e"),
                            UUID.fromString("f606cbb8-b971-496f-ad11-bcc61a724d4b"),
                            UUID.fromString("98ad4b0a-4dc1-4e2e-9123-6493002de979"),
                            UUID.fromString("452efc48-1d3e-4bac-98f1-fc057e643233"),
                            UUID.fromString("3b550460-4893-4aea-af98-3c8ca2677ae8"),
                            UUID.fromString("3da11e81-62cc-46b5-8c7e-2febd1e1858f"),
                            UUID.fromString("86b3f068-fb50-442b-8113-d36311ca6bc0"),
                            UUID.fromString( "65ff279a-5f48-4e30-bff9-ccb143efe565"),
                            UUID.fromString( "c55b77cb-2ab5-4b0c-a5f9-00bc85f98bb4"),
                            UUID.fromString( "a1571a95-6779-46b4-bd2f-ed5528a85156"))) // Remplacez par des UUID de catégories réels
                    .countryId(faker.options().option(
                            UUID.fromString("fc6bf07a-577d-4935-a99f-dae679b774c0"),
                            UUID.fromString("f71c7986-eca2-4dfb-bd18-84ce45e64ce5"),
                            UUID.fromString("ee87bb5e-0e8b-4cf3-9cc6-b1333ffc354b"),
                            UUID.fromString( "d0003a7e-a51f-430f-8a5e-68d1ffaab4c7"),
                            UUID.fromString( "11bfeacb-a28b-4d5a-8607-df2e991d57ee"))) // Remplacez par des UUID de pays réels))
                    .build();
            events.add(event);

            if(events.size() >= 1000){
                eventService.saveAll(events);
                events.clear();
            }
        }



    }
}

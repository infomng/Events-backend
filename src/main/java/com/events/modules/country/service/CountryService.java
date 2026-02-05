package com.events.modules.country.service;

import com.events.modules.country.dto.CountryCreateCommandDto;
import com.events.modules.country.dto.CountryDto;
import com.events.modules.country.dto.CountryUpdateCommandDto;
import com.events.modules.country.dto.mapper.ICountryMapper;
import com.events.modules.country.entity.Country;
import com.events.modules.country.exception.CountryAlreadyExistsException;
import com.events.modules.country.exception.CountryInUseException;
import com.events.modules.country.exception.CountryNotFoundException;
import com.events.modules.country.repository.ICountryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CountryService implements ICountryService {

    private final ICountryRepository countryRepository;
    private final ICountryMapper countryMapper;

    @Override
    public CountryDto createCountry(CountryCreateCommandDto command) {
        log.debug("Creating country from PaysEnum: {}", command.paysEnum());

        // Validate that the PaysEnum is not null (already validated by @NotNull)
        if (command.paysEnum() == null) {
            throw new IllegalArgumentException("PaysEnum cannot be null");
        }

        // Check if country with the same PaysEnum already exists
        if (countryRepository.existsByPaysEnum(command.paysEnum())) {
            throw new CountryAlreadyExistsException(command.paysEnum().getName());
        }

        Country country = countryMapper.toEntity(command);
        Country savedCountry = countryRepository.save(country);

        log.info("Country created successfully with id: {} and name: {}", savedCountry.getId(), savedCountry.getName());
        return countryMapper.toDto(savedCountry);
    }

    @Override
    public CountryDto updateCountry(UUID id, CountryUpdateCommandDto command) {
        log.debug("Updating country with id: {}", id);

        Country country = countryRepository.findById(id)
                .orElseThrow(() -> new CountryNotFoundException(id));

        // Check if new PaysEnum conflicts with another country
        countryRepository.findByPaysEnum(command.paysEnum())
                .ifPresent(existingCountry -> {
                    if (!existingCountry.getId().equals(id)) {
                        throw new CountryAlreadyExistsException(command.paysEnum().getName());
                    }
                });

        countryMapper.updateEntityFromDto(command, country);
        Country updatedCountry = countryRepository.save(country);

        log.info("Country updated successfully with id: {}", id);
        return countryMapper.toDto(updatedCountry);
    }

    @Override
    public void deleteCountry(UUID id) {
        log.debug("Deleting country with id: {}", id);

        Country country = countryRepository.findById(id)
                .orElseThrow(() -> new CountryNotFoundException(id));

        // Check if country is referenced by any users
        if (countryRepository.isCountryUsedByUsers(id)) {
            throw new CountryInUseException("Country is currently used by users and cannot be deleted");
        }

        // Check if country is referenced by any events
        if (countryRepository.isCountryUsedByEvents(id)) {
            throw new CountryInUseException("Country is currently used by events and cannot be deleted");
        }

        countryRepository.delete(country);
        log.info("Country deleted successfully with id: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public CountryDto getCountryById(UUID id) {
        log.debug("Fetching country with id: {}", id);

        Country country = countryRepository.findById(id)
                .orElseThrow(() -> new CountryNotFoundException(id));

        return countryMapper.toDto(country);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CountryDto> getAllCountries() {
        log.debug("Fetching all countries");

        List<Country> countries = countryRepository.findAll();
        return countryMapper.toDtoList(countries);
    }
}

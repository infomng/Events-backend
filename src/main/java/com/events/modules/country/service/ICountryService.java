package com.events.modules.country.service;

import com.events.modules.country.dto.CountryCreateCommandDto;
import com.events.modules.country.dto.CountryDto;
import com.events.modules.country.dto.CountryUpdateCommandDto;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for country management.
 * getAllCountries is public, other operations are ADMIN only.
 */
public interface ICountryService {

    /**
     * Create a new country from a PaysEnum value.
     * Only accessible by ADMIN users.
     *
     * @param command the country creation data
     * @return the created country
     */
    CountryDto createCountry(CountryCreateCommandDto command);

    /**
     * Update an existing country.
     * Only accessible by ADMIN users.
     *
     * @param id      the country ID
     * @param command the country update data
     * @return the updated country
     */
    CountryDto updateCountry(UUID id, CountryUpdateCommandDto command);

    /**
     * Delete a country by ID.
     * Will fail if the country is referenced by any users or events.
     * Only accessible by ADMIN users.
     *
     * @param id the country ID
     */
    void deleteCountry(UUID id);

    /**
     * Get a country by ID.
     * Public access.
     *
     * @param id the country ID
     * @return the country
     */
    CountryDto getCountryById(UUID id);

    /**
     * Get all countries.
     * Public access (no authentication required).
     *
     * @return list of all countries
     */
    List<CountryDto> getAllCountries();
}

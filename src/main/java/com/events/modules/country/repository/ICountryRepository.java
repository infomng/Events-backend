package com.events.modules.country.repository;

import com.events.modules.country.entity.Country;
import com.events.modules.country.enumeration.PaysEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ICountryRepository extends JpaRepository<Country, UUID> {

    /**
     * Find country by name (case-insensitive).
     */
    Optional<Country> findByNameIgnoreCase(String name);

    /**
     * Find country by code (case-insensitive).
     */
    Optional<Country> findByCodeIgnoreCase(String code);

    /**
     * Find country by PaysEnum.
     */
    Optional<Country> findByPaysEnum(PaysEnum paysEnum);

    /**
     * Check if a country exists by name (case-insensitive).
     */
    boolean existsByNameIgnoreCase(String name);

    /**
     * Check if a country exists by code (case-insensitive).
     */
    boolean existsByCodeIgnoreCase(String code);

    /**
     * Check if a country exists by PaysEnum.
     */
    boolean existsByPaysEnum(PaysEnum paysEnum);

    /**
     * Check if a country is referenced by any events.
     */
    @Query("SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END " +
           "FROM Event e WHERE e.country.id = :countryId")
    boolean isCountryUsedByEvents(@Param("countryId") UUID countryId);

    /**
     * Check if a country is referenced by any users.
     */
    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END " +
           "FROM User u WHERE u.country.id = :countryId")
    boolean isCountryUsedByUsers(@Param("countryId") UUID countryId);
}

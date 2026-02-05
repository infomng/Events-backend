package com.events.modules.country.controller;

import com.events.common.result.Result;
import com.events.modules.country.dto.CountryCreateCommandDto;
import com.events.modules.country.dto.CountryDto;
import com.events.modules.country.dto.CountryUpdateCommandDto;
import com.events.modules.country.service.ICountryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for country management.
 * GET endpoints are public, other operations require ADMIN role.
 */
@RestController
@RequestMapping("/api/v1/countries")
@RequiredArgsConstructor
@Tag(name = "Country Management", description = "Endpoints for managing countries")
public class CountryController {

    private final ICountryService countryService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Create a new country", description = "Create a new country from a PaysEnum value (ADMIN only)")
    public ResponseEntity<Result<CountryDto>> createCountry(@Valid @RequestBody CountryCreateCommandDto command) {
        CountryDto country = countryService.createCountry(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(Result.success(country));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update a country", description = "Update an existing country (ADMIN only)")
    public ResponseEntity<Result<CountryDto>> updateCountry(
            @PathVariable UUID id,
            @Valid @RequestBody CountryUpdateCommandDto command) {
        CountryDto country = countryService.updateCountry(id, command);
        return ResponseEntity.ok(Result.success(country));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Delete a country", description = "Delete a country if not referenced by users or events (ADMIN only)")
    public ResponseEntity<Result<Void>> deleteCountry(@PathVariable UUID id) {
        countryService.deleteCountry(id);
        return ResponseEntity.ok(Result.success());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get country by ID", description = "Retrieve a country by its ID (public)")
    public ResponseEntity<Result<CountryDto>> getCountryById(@PathVariable UUID id) {
        CountryDto country = countryService.getCountryById(id);
        return ResponseEntity.ok(Result.success(country));
    }

    @GetMapping
    @Operation(summary = "Get all countries", description = "Retrieve all countries (public, no authentication required)")
    public ResponseEntity<Result<List<CountryDto>>> getAllCountries() {
        List<CountryDto> countries = countryService.getAllCountries();
        return ResponseEntity.ok(Result.success(countries));
    }
}

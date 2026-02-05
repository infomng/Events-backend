package com.events.modules.country.controller;

import com.events.TestContexts.controllers.ControllerTestContext;
import com.events.modules.country.dto.CountryCreateCommandDto;
import com.events.modules.country.dto.CountryDto;
import com.events.modules.country.dto.CountryUpdateCommandDto;
import com.events.modules.country.enumeration.PaysEnum;
import com.events.modules.country.exception.CountryAlreadyExistsException;
import com.events.modules.country.exception.CountryInUseException;
import com.events.modules.country.exception.CountryNotFoundException;
import com.events.modules.country.service.ICountryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CountryController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("CountryController Integration Tests")
public class CountryControllerTest extends ControllerTestContext {

    @MockBean
    private ICountryService countryService;

    private CountryDto countryDto;
    private UUID countryId;

    @BeforeEach
    void setUp() {
        countryId = UUID.randomUUID();
        countryDto = new CountryDto(
                countryId,
                "France",
                "FR",
                PaysEnum.FRANCE,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    @Test
    @DisplayName("GET /api/v1/countries - Should return all countries (public access)")
    void getAllCountries_ShouldReturnAllCountries() throws Exception {
        // Given
        List<CountryDto> countries = List.of(countryDto);
        when(countryService.getAllCountries()).thenReturn(countries);

        // When & Then
        mockMvc.perform(get("/api/v1/countries"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.value[0].name").value("France"))
                .andExpect(jsonPath("$.value[0].code").value("FR"));

        verify(countryService).getAllCountries();
    }

    @Test
    @DisplayName("GET /api/v1/countries/{id} - Should return country by ID (public access)")
    void getCountryById_ShouldReturnCountry() throws Exception {
        // Given
        when(countryService.getCountryById(countryId)).thenReturn(countryDto);

        // When & Then
        mockMvc.perform(get("/api/v1/countries/{id}", countryId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.value.name").value("France"))
                .andExpect(jsonPath("$.value.code").value("FR"));

        verify(countryService).getCountryById(countryId);
    }

    @Test
    @DisplayName("GET /api/v1/countries/{id} - Should return 404 when country not found")
    void getCountryById_ShouldReturn404WhenNotFound() throws Exception {
        // Given
        when(countryService.getCountryById(countryId))
                .thenThrow(new CountryNotFoundException(countryId));

        // When & Then
        mockMvc.perform(get("/api/v1/countries/{id}", countryId))
                .andExpect(status().isNotFound());

        verify(countryService).getCountryById(countryId);
    }

    @Test
    @DisplayName("POST /api/v1/countries - Should create country (ADMIN only)")
    void createCountry_ShouldCreateSuccessfully() throws Exception {
        // Given
        CountryCreateCommandDto createDto = new CountryCreateCommandDto(PaysEnum.FRANCE);
        when(countryService.createCountry(any(CountryCreateCommandDto.class))).thenReturn(countryDto);

        // When & Then
        mockMvc.perform(post("/api/v1/countries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(createDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.value.name").value("France"))
                .andExpect(jsonPath("$.value.code").value("FR"));

        verify(countryService).createCountry(any(CountryCreateCommandDto.class));
    }

    @Test
    @DisplayName("POST /api/v1/countries - Should return 400 when PaysEnum is null")
    void createCountry_ShouldReturn400WhenPaysEnumIsNull() throws Exception {
        // Given
        String invalidJson = "{\"paysEnum\": null}";

        // When & Then
        mockMvc.perform(post("/api/v1/countries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());

        verify(countryService, never()).createCountry(any(CountryCreateCommandDto.class));
    }

    @Test
    @DisplayName("POST /api/v1/countries - Should return 400 when country already exists")
    void createCountry_ShouldReturn400WhenCountryExists() throws Exception {
        // Given
        CountryCreateCommandDto createDto = new CountryCreateCommandDto(PaysEnum.FRANCE);
        when(countryService.createCountry(any(CountryCreateCommandDto.class)))
                .thenThrow(new CountryAlreadyExistsException("France"));

        // When & Then
        mockMvc.perform(post("/api/v1/countries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(createDto)))
                .andExpect(status().isBadRequest());

        verify(countryService).createCountry(any(CountryCreateCommandDto.class));
    }

    @Test
    @DisplayName("PUT /api/v1/countries/{id} - Should update country (ADMIN only)")
    void updateCountry_ShouldUpdateSuccessfully() throws Exception {
        // Given
        CountryUpdateCommandDto updateDto = new CountryUpdateCommandDto(PaysEnum.GERMANY);
        CountryDto updatedDto = new CountryDto(
                countryId,
                "Germany",
                "DE",
                PaysEnum.GERMANY,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        when(countryService.updateCountry(eq(countryId), any(CountryUpdateCommandDto.class)))
                .thenReturn(updatedDto);

        // When & Then
        mockMvc.perform(put("/api/v1/countries/{id}", countryId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.value.name").value("Germany"))
                .andExpect(jsonPath("$.value.code").value("DE"));

        verify(countryService).updateCountry(eq(countryId), any(CountryUpdateCommandDto.class));
    }

    @Test
    @DisplayName("PUT /api/v1/countries/{id} - Should return 404 when country not found")
    void updateCountry_ShouldReturn404WhenNotFound() throws Exception {
        // Given
        CountryUpdateCommandDto updateDto = new CountryUpdateCommandDto(PaysEnum.GERMANY);
        when(countryService.updateCountry(eq(countryId), any(CountryUpdateCommandDto.class)))
                .thenThrow(new CountryNotFoundException(countryId));

        // When & Then
        mockMvc.perform(put("/api/v1/countries/{id}", countryId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(updateDto)))
                .andExpect(status().isNotFound());

        verify(countryService).updateCountry(eq(countryId), any(CountryUpdateCommandDto.class));
    }

    @Test
    @DisplayName("DELETE /api/v1/countries/{id} - Should delete country when not in use (ADMIN only)")
    void deleteCountry_ShouldDeleteSuccessfully() throws Exception {
        // Given
        doNothing().when(countryService).deleteCountry(countryId);

        // When & Then
        mockMvc.perform(delete("/api/v1/countries/{id}", countryId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true));

        verify(countryService).deleteCountry(countryId);
    }

    @Test
    @DisplayName("DELETE /api/v1/countries/{id} - Should return 404 when country not found")
    void deleteCountry_ShouldReturn404WhenNotFound() throws Exception {
        // Given
        doThrow(new CountryNotFoundException(countryId))
                .when(countryService).deleteCountry(countryId);

        // When & Then
        mockMvc.perform(delete("/api/v1/countries/{id}", countryId))
                .andExpect(status().isNotFound());

        verify(countryService).deleteCountry(countryId);
    }

    @Test
    @DisplayName("DELETE /api/v1/countries/{id} - Should return 400 when country is in use")
    void deleteCountry_ShouldReturn400WhenInUse() throws Exception {
        // Given
        doThrow(new CountryInUseException("Country is currently used by users"))
                .when(countryService).deleteCountry(countryId);

        // When & Then
        mockMvc.perform(delete("/api/v1/countries/{id}", countryId))
                .andExpect(status().isBadRequest());

        verify(countryService).deleteCountry(countryId);
    }
}

package com.events.modules.country.service;

import com.events.modules.country.dto.CountryCreateCommandDto;
import com.events.modules.country.dto.CountryDto;
import com.events.modules.country.dto.CountryUpdateCommandDto;
import com.events.modules.country.dto.mapper.ICountryMapper;
import com.events.modules.country.entity.Country;
import com.events.modules.country.enumeration.PaysEnum;
import com.events.modules.country.exception.CountryAlreadyExistsException;
import com.events.modules.country.exception.CountryInUseException;
import com.events.modules.country.exception.CountryNotFoundException;
import com.events.modules.country.repository.ICountryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CountryService Tests")
class CountryServiceTest {

    @Mock
    private ICountryRepository countryRepository;

    @Mock
    private ICountryMapper countryMapper;

    @InjectMocks
    private CountryService countryService;

    private Country country;
    private CountryDto countryDto;
    private CountryCreateCommandDto createCommandDto;
    private CountryUpdateCommandDto updateCommandDto;
    private UUID countryId;

    @BeforeEach
    void setUp() {
        countryId = UUID.randomUUID();

        country = Country.builder()
                .name("France")
                .code("FR")
                .paysEnum(PaysEnum.FRANCE)
                .build();

        countryDto = new CountryDto(
                countryId,
                "France",
                "FR",
                PaysEnum.FRANCE,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        createCommandDto = new CountryCreateCommandDto(PaysEnum.FRANCE);
        updateCommandDto = new CountryUpdateCommandDto(PaysEnum.GERMANY);
    }

    @Test
    @DisplayName("Should create country successfully from valid PaysEnum")
    void createCountry_ShouldCreateSuccessfully() {
        // Given
        when(countryRepository.existsByPaysEnum(createCommandDto.paysEnum())).thenReturn(false);
        when(countryMapper.toEntity(createCommandDto)).thenReturn(country);
        when(countryRepository.save(any(Country.class))).thenReturn(country);
        when(countryMapper.toDto(country)).thenReturn(countryDto);

        // When
        CountryDto result = countryService.createCountry(createCommandDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo("France");
        assertThat(result.code()).isEqualTo("FR");
        verify(countryRepository).existsByPaysEnum(createCommandDto.paysEnum());
        verify(countryRepository).save(any(Country.class));
        verify(countryMapper).toDto(country);
    }

    @Test
    @DisplayName("Should throw exception when creating country with duplicate PaysEnum")
    void createCountry_ShouldThrowExceptionWhenPaysEnumExists() {
        // Given
        when(countryRepository.existsByPaysEnum(createCommandDto.paysEnum())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> countryService.createCountry(createCommandDto))
                .isInstanceOf(CountryAlreadyExistsException.class)
                .hasMessageContaining("already exists");

        verify(countryRepository).existsByPaysEnum(createCommandDto.paysEnum());
        verify(countryRepository, never()).save(any(Country.class));
    }

    @Test
    @DisplayName("Should update country successfully")
    void updateCountry_ShouldUpdateSuccessfully() {
        // Given
        when(countryRepository.findById(countryId)).thenReturn(Optional.of(country));
        when(countryRepository.findByPaysEnum(updateCommandDto.paysEnum())).thenReturn(Optional.empty());
        when(countryRepository.save(any(Country.class))).thenReturn(country);
        when(countryMapper.toDto(country)).thenReturn(countryDto);

        // When
        CountryDto result = countryService.updateCountry(countryId, updateCommandDto);

        // Then
        assertThat(result).isNotNull();
        verify(countryRepository).findById(countryId);
        verify(countryMapper).updateEntityFromDto(updateCommandDto, country);
        verify(countryRepository).save(country);
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent country")
    void updateCountry_ShouldThrowExceptionWhenCountryNotFound() {
        // Given
        when(countryRepository.findById(countryId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> countryService.updateCountry(countryId, updateCommandDto))
                .isInstanceOf(CountryNotFoundException.class);

        verify(countryRepository).findById(countryId);
        verify(countryRepository, never()).save(any(Country.class));
    }

    @Test
    @DisplayName("Should throw exception when updating country with duplicate PaysEnum")
    void updateCountry_ShouldThrowExceptionWhenPaysEnumConflicts() throws Exception {
        // Given
        UUID otherCountryId = UUID.randomUUID();
        Country otherCountry = Country.builder()
                .name("Germany")
                .code("DE")
                .paysEnum(PaysEnum.GERMANY)
                .build();

        // Use reflection to set the id field
        var idField = Country.class.getSuperclass().getSuperclass().getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(otherCountry, otherCountryId);

        when(countryRepository.findById(countryId)).thenReturn(Optional.of(country));
        when(countryRepository.findByPaysEnum(updateCommandDto.paysEnum()))
                .thenReturn(Optional.of(otherCountry));

        // When & Then
        assertThatThrownBy(() -> countryService.updateCountry(countryId, updateCommandDto))
                .isInstanceOf(CountryAlreadyExistsException.class)
                .hasMessageContaining("already exists");

        verify(countryRepository).findById(countryId);
        verify(countryRepository, never()).save(any(Country.class));
    }

    @Test
    @DisplayName("Should delete country successfully when not in use")
    void deleteCountry_ShouldDeleteSuccessfully() {
        // Given
        when(countryRepository.findById(countryId)).thenReturn(Optional.of(country));
        when(countryRepository.isCountryUsedByUsers(countryId)).thenReturn(false);
        when(countryRepository.isCountryUsedByEvents(countryId)).thenReturn(false);

        // When
        countryService.deleteCountry(countryId);

        // Then
        verify(countryRepository).findById(countryId);
        verify(countryRepository).isCountryUsedByUsers(countryId);
        verify(countryRepository).isCountryUsedByEvents(countryId);
        verify(countryRepository).delete(country);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent country")
    void deleteCountry_ShouldThrowExceptionWhenCountryNotFound() {
        // Given
        when(countryRepository.findById(countryId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> countryService.deleteCountry(countryId))
                .isInstanceOf(CountryNotFoundException.class);

        verify(countryRepository).findById(countryId);
        verify(countryRepository, never()).delete(any(Country.class));
    }

    @Test
    @DisplayName("Should throw exception when deleting country used by users")
    void deleteCountry_ShouldThrowExceptionWhenUsedByUsers() {
        // Given
        when(countryRepository.findById(countryId)).thenReturn(Optional.of(country));
        when(countryRepository.isCountryUsedByUsers(countryId)).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> countryService.deleteCountry(countryId))
                .isInstanceOf(CountryInUseException.class)
                .hasMessageContaining("currently used by users");

        verify(countryRepository).findById(countryId);
        verify(countryRepository).isCountryUsedByUsers(countryId);
        verify(countryRepository, never()).delete(any(Country.class));
    }

    @Test
    @DisplayName("Should throw exception when deleting country used by events")
    void deleteCountry_ShouldThrowExceptionWhenUsedByEvents() {
        // Given
        when(countryRepository.findById(countryId)).thenReturn(Optional.of(country));
        when(countryRepository.isCountryUsedByUsers(countryId)).thenReturn(false);
        when(countryRepository.isCountryUsedByEvents(countryId)).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> countryService.deleteCountry(countryId))
                .isInstanceOf(CountryInUseException.class)
                .hasMessageContaining("currently used by events");

        verify(countryRepository).findById(countryId);
        verify(countryRepository).isCountryUsedByUsers(countryId);
        verify(countryRepository).isCountryUsedByEvents(countryId);
        verify(countryRepository, never()).delete(any(Country.class));
    }

    @Test
    @DisplayName("Should get country by ID successfully")
    void getCountryById_ShouldReturnCountry() {
        // Given
        when(countryRepository.findById(countryId)).thenReturn(Optional.of(country));
        when(countryMapper.toDto(country)).thenReturn(countryDto);

        // When
        CountryDto result = countryService.getCountryById(countryId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo("France");
        assertThat(result.code()).isEqualTo("FR");
        verify(countryRepository).findById(countryId);
        verify(countryMapper).toDto(country);
    }

    @Test
    @DisplayName("Should throw exception when getting non-existent country")
    void getCountryById_ShouldThrowExceptionWhenCountryNotFound() {
        // Given
        when(countryRepository.findById(countryId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> countryService.getCountryById(countryId))
                .isInstanceOf(CountryNotFoundException.class);

        verify(countryRepository).findById(countryId);
    }

    @Test
    @DisplayName("Should get all countries successfully")
    void getAllCountries_ShouldReturnAllCountries() {
        // Given
        List<Country> countries = List.of(country);
        List<CountryDto> countryDtos = List.of(countryDto);
        when(countryRepository.findAll()).thenReturn(countries);
        when(countryMapper.toDtoList(countries)).thenReturn(countryDtos);

        // When
        List<CountryDto> result = countryService.getAllCountries();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("France");
        assertThat(result.get(0).code()).isEqualTo("FR");
        verify(countryRepository).findAll();
        verify(countryMapper).toDtoList(countries);
    }
}

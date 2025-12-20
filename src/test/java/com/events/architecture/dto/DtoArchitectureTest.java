package com.events.architecture.dto;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import org.mapstruct.Mapper;


import java.util.Set;
import java.util.stream.Collectors;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static org.assertj.core.api.Assertions.assertThat;

@AnalyzeClasses(
        packages = "com.events",
        importOptions = ImportOption.DoNotIncludeTests.class
)
class DtoArchitectureTest {

    @ArchTest
    void all_dtos_must_be_records(JavaClasses classes) {
        Set<JavaClass> dtos = getDtos(classes);

        for (JavaClass dto : dtos) {
            assertThat(dto.isRecord())
                    .as("DTO %s doit être un record", dto.getName())
                    .isTrue();
        }
    }

    @ArchTest
    void all_dtos_must_end_with_dto(JavaClasses classes) {
        Set<JavaClass> dtos = getDtos(classes);
        for (JavaClass dto : dtos) {
            assertThat(dto.getName()).endsWith("Dto");
        }
    }

    @ArchTest
    void mappers_should_reside_in_dto_mapper_package(JavaClasses classes) {
        classes()
                .that().areAnnotatedWith(Mapper.class)
                .should().resideInAPackage("..dto.mapper..")
                .check(classes);
    }

    private static Set<JavaClass> getDtos(JavaClasses classes) {
        return classes.stream()
                .filter(c -> c.getPackageName().contains(".dto"))
                .filter(c -> !c.getName().contains("$"))
                .filter(c -> !c.getName().contains("Mapper"))
                .collect(Collectors.toSet());
    }

    private static Set<JavaClass> getMappers(JavaClasses classes) {
        return classes.stream()
                .filter(c -> c.getPackageName().contains(".mapper"))
                .collect(Collectors.toSet());
    }
}
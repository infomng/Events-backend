package com.events.architecture.service;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import jakarta.persistence.Entity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@AnalyzeClasses(packages = "com.events")
class ServiceArchitectureTest {

    @ArchTest
    void services_should_be_transactional(JavaClasses classes) {
        classes()
                .that().areAnnotatedWith(Service.class)
                .should().beAnnotatedWith(Transactional.class)
                .check(classes);
    }

    @ArchTest
    void service_methods_should_not_return_entities(JavaClasses classes) {
        classes.stream()
                .filter(c -> c.isAnnotatedWith(Service.class)
                        && List.of("UserService", "AuthService").contains(c.getName())) // exclude UserService as an example
                .forEach(service -> {
                    for (JavaMethod method : service.getMethods()) {
                        if (!method.getModifiers().contains(com.tngtech.archunit.core.domain.JavaModifier.PUBLIC)) {
                            continue; // skip non-public methods
                        }

                        Class<?> returnClass = method.getRawReturnType().reflect();

                        boolean isEntity = returnClass.isAnnotationPresent(Entity.class);

                        assertThat(isEntity)
                                .as("Method %s in %s must not return a JPA entity (%s)",
                                        method.getName(),
                                        service.getName(),
                                        returnClass.getSimpleName())
                                .isFalse();
                    }
                });
    }
}
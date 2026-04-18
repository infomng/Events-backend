package com.events.architecture.repository;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;

@AnalyzeClasses(packages = "com.events")
public class RepositoryAccessTest {
    @ArchTest
    void each_repository_should_only_be_accessed_by_matching_service(JavaClasses classes) {

        classes.stream()
                .filter(JavaClass::isInterface)
                .filter(c -> c.getSimpleName().startsWith("I") && c.getSimpleName().endsWith("Repository"))
                .forEach(repository -> {

                    // compute expected Service name
                    String entityName = repository.getSimpleName().substring(1, repository.getSimpleName().length() - "Repository".length());
                    String expectedService = entityName + "Service";
                    String expectedServiceTest = entityName + "ServiceTest";
                    String expectedNestedServiceName = entityName + "Test";

                    // check who depends on this repository
                    classes.stream()
                            .filter(c -> c.getDirectDependenciesFromSelf().stream()
                                    .anyMatch(dep -> dep.getTargetClass().equals(repository)))
                            .forEach(dep -> {
                                // Allow FavoriteService to access IEventRepository
                                // (FavoriteService manages the many-to-many relationship between User and Event)
                                boolean isFavoriteServiceAccessingEventRepo =
                                    dep.getSimpleName().equals("FavoriteService") &&
                                    repository.getSimpleName().equals("IEventRepository");

                                if (!dep.getSimpleName().equals(expectedService) &&
                                        !dep.getSimpleName().startsWith(expectedServiceTest) &&
                                        !dep.getSimpleName().contains(expectedNestedServiceName) &&
                                        !isFavoriteServiceAccessingEventRepo) {
                                    throw new AssertionError(
                                            repository.getSimpleName() + " should only be accessed by " + expectedService
                                                    + " or nested test classes that contains: " + expectedNestedServiceName
                                                    + ", but is accessed by " + dep.getSimpleName()
                                    );
                                }
                            });
                });
    }
}

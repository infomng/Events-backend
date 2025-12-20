package com.events.architecture.interfaces;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

@AnalyzeClasses(packages = "com.events")
public class InterfaceNamingTest {

    @ArchTest
    void interfaces_should_start_with_I(JavaClasses classes) {
        classes()
                .that().areInterfaces()
                .and().areNotAnnotations()
                .should().haveSimpleNameStartingWith("I")
                .check(classes);
    }
}

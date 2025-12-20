package com.events.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

@AnalyzeClasses(packages = "com.events")
public class EnumNamaingTest {

    @ArchTest
    void enum_class_should_end_with_Enum(JavaClasses classes) {
        classes()
                .that().areEnums()
                .should().haveSimpleNameEndingWith("Enum")
                .check(classes);
    }

}

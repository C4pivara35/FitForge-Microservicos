package br.fitforge.treinos;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class ArchitectureTests {

    @Test
    void featureRespeitaFronteiras() {
        var code = new ClassFileImporter().withImportOption(new ImportOption.DoNotIncludeTests())
                .importPackages("br.fitforge.treinos");
        classes().that().haveSimpleName("WorkoutController")
                .should().resideInAPackage("..workouts.controller").check(code);
        noClasses().that().resideInAPackage("..workouts.controller")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "..repository..", "org.springframework.data..",
                        "org.springframework.web.reactive.function.client..",
                        "com.azure.messaging.servicebus..").check(code);
        noClasses().that().resideInAPackage("..workouts.service")
                .should().dependOnClassesThat().resideInAPackage("..controller..").check(code);
    }
}

package br.fitforge.progresso;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class ArchitectureTests {

    @Test
    void featureRespeitaFronteiras() {
        var code = new ClassFileImporter().withImportOption(new ImportOption.DoNotIncludeTests())
                .importPackages("br.fitforge.progresso");
        classes().that().haveSimpleName("SessionController")
                .should().resideInAPackage("..sessions.controller").check(code);
        noClasses().that().resideInAPackage("..sessions.controller")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "..repository..", "org.springframework.data..",
                        "org.springframework.web.reactive.function.client..",
                        "com.azure.messaging.servicebus..").check(code);
        noClasses().that().resideInAPackage("..sessions.service")
                .should().dependOnClassesThat().resideInAPackage("..controller..").check(code);
    }
}

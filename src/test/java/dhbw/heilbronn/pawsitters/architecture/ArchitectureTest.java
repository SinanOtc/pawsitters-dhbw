package dhbw.heilbronn.pawsitters.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.library.dependencies.SlicesRuleDefinition;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Macht die MVC- / Layered-Architektur als Test ausführbar.
 * Jede Regel hier ist eine Architektur-Aussage, die der Build erzwingt —
 * Verstöße scheitern CI, Architektur ist enforced statt nur dokumentiert.
 *
 * DoNotIncludeTests: nur Produktionscode wird analysiert, Test-Hilfsklassen
 * sind bewusst ausgenommen (würden sonst Schicht-Regeln verletzen).
 */
@AnalyzeClasses(
        packages = "dhbw.heilbronn.pawsitters",
        importOptions = ImportOption.DoNotIncludeTests.class
)
public class ArchitectureTest {

    // === Schicht-Regeln ===

    @ArchTest
    static final ArchRule web_must_not_directly_access_repositories =
            noClasses().that().resideInAPackage("..web..")
                    .should().dependOnClassesThat().resideInAPackage("..repository..")
                    .because("Controller müssen über die Service-Schicht gehen — Repositories sind internes Persistence-Detail.");

    @ArchTest
    static final ArchRule domain_must_not_depend_on_other_layers =
            noClasses().that().resideInAPackage("..domain..")
                    .should().dependOnClassesThat().resideInAnyPackage(
                            "..web..", "..service..", "..repository..", "..config..", "..security..")
                    .because("Domain ist der Kern — andere Schichten dürfen von Domain abhängen, niemals umgekehrt.");

    @ArchTest
    static final ArchRule domain_must_not_depend_on_spring =
            noClasses().that().resideInAPackage("..domain..")
                    .should().dependOnClassesThat().resideInAPackage("org.springframework..")
                    .because("Domain-Modell bleibt framework-agnostisch (Jakarta-Persistence-Annotations sind ok, Spring nicht).");

    @ArchTest
    static final ArchRule repositories_must_not_depend_on_services_or_web =
            noClasses().that().resideInAPackage("..repository..")
                    .should().dependOnClassesThat().resideInAnyPackage("..service..", "..web..")
                    .because("Persistence-Schicht weiß nichts über Services oder Web — Dependency-Flow nur nach unten.");

    // === Lokations-Regel (Annotation → Package) ===

    @ArchTest
    static final ArchRule classes_annotated_controller_belong_in_web_controller_package =
            classes().that().areAnnotatedWith(Controller.class)
                    .should().resideInAPackage("..web.controller..");

    // === Namens-Konventionen ===

    @ArchTest
    static final ArchRule controllers_should_have_controller_suffix =
            classes().that().areAnnotatedWith(Controller.class)
                    .should().haveSimpleNameEndingWith("Controller");

    @ArchTest
    static final ArchRule services_in_service_package_should_have_service_suffix =
            classes().that().resideInAPackage("..service..")
                    .and().areAnnotatedWith(Service.class)
                    .should().haveSimpleNameEndingWith("Service");

    @ArchTest
    static final ArchRule repositories_should_have_repository_suffix =
            classes().that().resideInAPackage("..repository..")
                    .should().haveSimpleNameEndingWith("Repository");

    // === Strukturelle Regel ===

    @ArchTest
    static final ArchRule no_cycles_between_top_level_packages =
            SlicesRuleDefinition.slices()
                    .matching("dhbw.heilbronn.pawsitters.(*)..")
                    .should().beFreeOfCycles();
}

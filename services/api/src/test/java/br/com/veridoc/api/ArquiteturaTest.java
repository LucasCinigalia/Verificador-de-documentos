package br.com.veridoc.api;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.data.repository.Repository;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

/**
 * Regras de arquitetura verificadas a cada build (ADR 0005). O ArchUnit lê o bytecode
 * compilado e falha o teste se alguma dependência proibida aparecer.
 */
@AnalyzeClasses(packages = "br.com.veridoc.api", importOptions = ImportOption.DoNotIncludeTests.class)
class ArquiteturaTest {

    /** Cada subpacote de primeiro nível (documento, analise, decisao...) é um módulo. */
    @ArchTest
    static final ArchRule modulosSemCiclos = slices()
            .matching("br.com.veridoc.api.(*)..")
            .should().beFreeOfCycles();

    @ArchTest
    static final ArchRule compartilhadoNaoConheceOsDominios = noClasses()
            .that().resideInAPackage("..compartilhado..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "..documento..", "..analise..", "..decisao..", "..auditoria..");

    /**
     * Repositório público poderia ser usado por qualquer pacote. Package-private, só o
     * serviço do próprio domínio o enxerga, e o compilador garante isso.
     */
    @ArchTest
    static final ArchRule repositoriosSaoPrivadosDoDominio = classes()
            .that().areAssignableTo(Repository.class)
            .should().notBePublic();

    /** As regras de risco são lógica pura: sem HTTP e sem banco, por isso testam em milissegundos. */
    @ArchTest
    static final ArchRule regrasDeRiscoSemWebNemJpa = noClasses()
            .that().resideInAPackage("..decisao.regras..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "org.springframework.web..", "jakarta.persistence..", "org.springframework.data..");
}

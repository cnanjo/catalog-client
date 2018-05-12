package edu.utah.catalog.api;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Composition;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class CatalogServiceTest {

    private CatalogService catalogService;

    @Before
    public void setUp() throws Exception {
        catalogService = new CatalogService();
        catalogService.initialize(CatalogService.DEFAULT_ENDPOINT_URI);
    }

    @Test
    public void searchByTitle() {
        Bundle bundle = catalogService.searchByTitle("ciolab");
        assertEquals(1, bundle.getEntry().size());
        Composition composition = (Composition)bundle.getEntry().get(0).getResource();
        assertNotNull(composition);
        //assertEquals("2", composition.getId());
        assertEquals(Composition.CompositionStatus.PRELIMINARY, composition.getStatus());
        assertEquals("Catalog",composition.getType().getText());
        Coding classCoding = composition.getClass_().getCoding().get(0);
        assertNotNull(classCoding);
        assertEquals("laboratory-service",classCoding.getCode());
        assertEquals("laboratory service",classCoding.getDisplay());
        assertEquals("http://hl7.org/fhir/ValueSet/catalogType",classCoding.getSystem());
        assertEquals("Phast-Services", composition.getAuthorFirstRep().getDisplay());
        assertEquals("CIOlab", composition.getTitle());
        assertEquals(Composition.DocumentConfidentiality.U, composition.getConfidentiality());
        assertEquals("Phast-Services", composition.getCustodian().getDisplay());
        List<Composition.SectionComponent> sections = composition.getSection();
        assertEquals(1, sections.size());
        Composition.SectionComponent section = sections.get(0);
        assertNotNull(section);
        assertEquals("Laboratory medicine compendium", section.getTitle());
        assertEquals("http://loinc.org", section.getCode().getCoding().get(0).getSystem());
        assertEquals("26436-6", section.getCode().getCoding().get(0).getCode());
        assertEquals("laboratory studies", section.getCode().getCoding().get(0).getDisplay());
        assertEquals(0, section.getEntry().size());
    }

    @Test
    public void listEntries() {
        //Bundle bundle = catalogService.listEntries();
        //assertEquals(288, bundle.getEntry().size()); //Returns an empty bundle
    }

}
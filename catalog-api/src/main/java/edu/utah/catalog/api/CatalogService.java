package edu.utah.catalog.api;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Composition;
import org.hl7.fhir.r4.model.EntryDefinition;

import java.util.Map;

public class CatalogService {

    public static final String DEFAULT_ENDPOINT_URI = "http://jade.phast.fr/catalogs/api/fhir"; //This should be read from a properties file.
    public static final String DEFAULT_SERVER_BASE = "http://jade.phast.fr/catalogs/api/fhir/";
    public static final String SEARCH_PARAMETER_TYPE = "type";
    public static final String SEARCH_PARAMETER_PURPOSE = "purpose";
    public static final String SEARCH_PARAMETER_CLASSIFICATION = "classification:text";
    public static final String SEARCH_PARAMETER_COMPOSITION = "_has:Composition:_id";
    public static final String SEARCH_PARAMETER_REFERENCED_ITEM = "referencedItem:ActivityDefinition.name";

    private IGenericClient client;
    private FhirContext ctx = FhirContext.forR4();

    public CatalogService() {
        initialize(DEFAULT_ENDPOINT_URI);
    }

    public void initialize(String endpoint) {
        FhirContext ctx = FhirContext.forR4();
        if(false) {
            ctx.getRestfulClientFactory().setProxy("127.0.0.1", 8888);
        }
        client = ctx.newRestfulGenericClient(endpoint);
    }

    /**
     * Performs a search by title against a catalog repository
     *
     * @param title
     */
    public Bundle searchByTitle(String title) {
        return client
                .search()
                .forResource(Composition.class)
                .where(Composition.TITLE.matches().value(title))
                .returnBundle(Bundle.class)
                .execute();
    }

    /**
     * Performs a search by title against a catalog repository
     *
     */
    public Bundle searchEntries(String catalogId, Map<String,String> searchParameters) {
        String url = DEFAULT_SERVER_BASE + "EntryDefinition?";
        StringBuilder queryString = new StringBuilder();
        if(searchParameters.containsKey(SEARCH_PARAMETER_TYPE) && searchParameters.get(SEARCH_PARAMETER_TYPE).trim().length() > 0) {
            queryString.append("&").append(SEARCH_PARAMETER_TYPE).append("=").append(searchParameters.get(SEARCH_PARAMETER_TYPE));
        }
        if(searchParameters.containsKey(SEARCH_PARAMETER_PURPOSE) && searchParameters.get(SEARCH_PARAMETER_PURPOSE).trim().length() > 0) {
            queryString.append("&").append(SEARCH_PARAMETER_PURPOSE).append("=").append(searchParameters.get(SEARCH_PARAMETER_PURPOSE));
        }
        if(searchParameters.containsKey(SEARCH_PARAMETER_CLASSIFICATION) && searchParameters.get(SEARCH_PARAMETER_CLASSIFICATION).trim().length() > 0) {
            queryString.append("&").append(SEARCH_PARAMETER_CLASSIFICATION).append("=").append(searchParameters.get(SEARCH_PARAMETER_CLASSIFICATION));
        }
        if(searchParameters.containsKey(SEARCH_PARAMETER_REFERENCED_ITEM) && searchParameters.get(SEARCH_PARAMETER_REFERENCED_ITEM).trim().length() > 0) {
            queryString.append("&").append(SEARCH_PARAMETER_REFERENCED_ITEM).append("=").append(searchParameters.get(SEARCH_PARAMETER_REFERENCED_ITEM));
        }
        url = url + SEARCH_PARAMETER_COMPOSITION + "=" + catalogId.substring(catalogId.lastIndexOf("/") + 1) + queryString.toString();
        System.out.println("Search URL: " + url);
        return client.search()
                .byUrl(url)
                .returnBundle(Bundle.class)
                .execute();
    }

    /**
     * Returns all catalog from the server. Use with caution if the repository of catalogs is large.
     *
     * @return
     */
    public Bundle getCatalogs() {
        Bundle bundle = client.search()
                .forResource(Composition.class)
                .returnBundle(Bundle.class)
                .execute();
        return bundle;
    }

    public String testConnection() {
        return "Connection established";
    }

    public Bundle getEntryDefinitions(String catalogId) {
        String url = "http://jade.phast.fr/catalogs/api/fhir/EntryDefinition?" + SEARCH_PARAMETER_COMPOSITION + "=" + catalogId + "&purpose=orderable";
        System.out.println("Invoking: " + url);
        return client.search()
                .byUrl(url)
                .returnBundle(Bundle.class)
                .execute();
    }

    public Bundle getEntryDefinition(String entryId) {
        String url = "http://jade.phast.fr/catalogs/api/fhir/EntryDefinition?_id=" + entryId;
        return client.search()
                .byUrl(url)
                .returnBundle(Bundle.class)
                .execute();
    }
}

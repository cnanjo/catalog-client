package edu.utah.catalog.controller;

import edu.utah.catalog.api.CatalogService;
import org.apache.commons.lang.StringUtils;
import org.fujion.ancillary.IAutoWired;
import org.fujion.annotation.EventHandler;
import org.fujion.annotation.WiredComponent;
import org.fujion.component.*;
import org.fujion.event.Event;
import org.fujion.event.IEventListener;
import org.hl7.fhir.r4.model.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CatalogController implements IAutoWired {

    @Autowired
    private CatalogService catalogService;

    @WiredComponent
    private Combobox cbCatalog1;

    @WiredComponent
    private Combobox cbCatalog2;

    @WiredComponent
    private Combobox cbEntryDefType1;

    @WiredComponent
    private Combobox cbEntryDefType2;

    @WiredComponent
    private Combobox cbPurpose1;

    @WiredComponent
    private Combobox cbPurpose2;

    @WiredComponent
    private Textbox tbClassificationDisplay;

    @WiredComponent
    private Textbox tbActivityDefinitionName;

    @WiredComponent
    private Grid catalogGrid;

    @WiredComponent
    private Tabview catalogTabView;

    @WiredComponent
    private Tab catalogBrowserTab;

    @WiredComponent
    private Tab catalogEntryBrowserTab;

    @WiredComponent
    private Div catalogSearchResultGridDiv;

    public CatalogController() {
        System.out.println("################################## INITIALIZED");
    }

    @Override
    public void afterInitialized(BaseComponent root) {
        populateCatalogCombo(cbCatalog1);

        populateCatalogCombo(cbCatalog2);

        buildPurposeComboBox(cbPurpose1);

        buildPurposeComboBox(cbPurpose2);

        populateServiceCombo(cbEntryDefType1);

        populateServiceCombo(cbEntryDefType2);

        initializeCatalogGrid();
    }

    public void initializeCatalogGrid() {
        if(catalogGrid != null) {
            if(catalogGrid.getChildren() != null && catalogGrid.getChildren().size() > 0) {
                catalogGrid.destroyChildren();
            }
            catalogGrid.setTitle("Available Catalogs");
            Columns columns = new Columns();
            columns.addChild(new Column("ID"));
            columns.addChild(new Column("Status"));
            columns.addChild(new Column("Class"));
            columns.addChild(new Column("Author"));
            columns.addChild(new Column("Custodian"));
            columns.addChild(new Column("Action"));
            catalogGrid.addChild(columns);
            Rows rows = new Rows();
            populateCatalogGrid(rows);
            catalogGrid.addChild(rows);
        } else {
            System.out.println("Entry grid is null");
        }
    }

    public void populateCatalogGrid(Rows rows) {
        Bundle catalogBundle = catalogService.getCatalogs();

        List<Bundle.BundleEntryComponent> bundles = catalogBundle.getEntry();
        bundles.forEach(bundleEntry -> {
            Composition composition = (Composition)bundleEntry.getResource();
            Row row = new Row();

            //Title and ID
            Rowcell id = new Rowcell();
            id.setRowspan(1);
            id.setColspan(1);
            id.setLabel("" + composition.getTitle());
            id.setData(composition.getId());
            row.addChild(id);
            //Class
            Rowcell catalogClass = new Rowcell();
            catalogClass.setRowspan(1);
            catalogClass.setColspan(1);
            catalogClass.setLabel("" + composition.getClass_().getCodingFirstRep().getDisplay());
            row.addChild(catalogClass);
            //Status
            Rowcell status = new Rowcell();
            status.setRowspan(1);
            status.setColspan(1);
            status.setLabel("" + composition.getStatus().getDisplay());
            row.addChild(status);
            //Author
            Rowcell Author = new Rowcell();
            Author.setRowspan(1);
            Author.setColspan(1);
            Author.setLabel("" + composition.getAuthorFirstRep().getDisplay());
            row.addChild(Author);
            //Custodian
            Rowcell custodian = new Rowcell();
            custodian.setRowspan(1);
            custodian.setColspan(1);
            custodian.setLabel("" + composition.getCustodian().getDisplay());
            row.addChild(custodian);
            //Action
            Rowcell action = new Rowcell();
            action.setRowspan(1);
            action.setColspan(1);
            Button viewDetailsBtn = new Button("View Details");
            viewDetailsBtn.setName("viewCatalogDetailsBtn" + composition.getId().hashCode());
            viewDetailsBtn.addEventListener("click", event -> {
                Row selectedRow = event.getTarget().getAncestor(Row.class);
                Tab tab = buildCatalogDetailsTab(composition, selectedRow);
                catalogTabView.addChild(tab);
            });
            action.addChild(viewDetailsBtn);
            row.addChild(action);
            rows.addChild(row);
        });
    }

    protected Tab buildCatalogDetailsTab(Composition composition, Row selectedRow) {
        Rowcell catalogIdCell = (Rowcell)selectedRow.getFirstChild();
        Tab tab = new Tab(catalogIdCell.getLabel());
        tab.setSelected(true);
        Div content = new Div();
        tab.addChild(content);
        Groupbox catalogDetailsGroupbox = populateCatalogDetailsGroupbox(composition);
        content.addChild(catalogDetailsGroupbox);
        String catalogIdUnparsed = (String)catalogIdCell.getData();
        String catalogIdSuffix = catalogIdUnparsed.substring(catalogIdUnparsed.lastIndexOf("/") + 1);
        if(catalogIdSuffix != null) {
            try {
                Bundle entryBundle = catalogService.getEntryDefinitions(catalogIdSuffix);
                Grid entryDefinitionGrid = buildEntryDefinitionGrid(catalogIdSuffix, entryBundle);
                content.addChild(entryDefinitionGrid);
            } catch(Exception e) {
                System.out.println("Error: " + e.getMessage());
                //TODO Popup a message that no entries could be retrieved from the catalog.
            }
        }
        tab.setClosable(true);
        return tab;
    }

    public Groupbox populateCatalogDetailsGroupbox(Composition composition) {
        Groupbox groupbox = new Groupbox();
        groupbox.addClass("catalog-details-group-box");
        groupbox.setTitle("Catalog Details");
        groupbox.addChild(buildLabelDiv("Title:", composition.getTitle()));
        groupbox.addChild(buildLabelDiv("ID:", composition.getId()));
        groupbox.addChild(buildLabelDiv("Language:", composition.getLanguage()));
        groupbox.addChild(buildLabelDiv("Status:", composition.getStatus().getDisplay()));
        groupbox.addChild(buildLabelDiv("Type:", composition.getType().getText()));
        groupbox.addChild(buildLabelDiv("Class:", composition.getClass_().getCodingFirstRep().getDisplay()));
        groupbox.addChild(buildLabelDiv("Author:", composition.getAuthor().get(0).getDisplay()));
        groupbox.addChild(buildLabelDiv("Confidentiality:", composition.getConfidentiality().getDisplay()));
        groupbox.addChild(buildLabelDiv("Custodian:", composition.getCustodian().getDisplay()));
        groupbox.addChild(buildLabelDiv("Section Count:", ""+composition.getSection().size()));
        composition.getSection().forEach(sectionComponent -> {
            groupbox.addChild(buildLabelDiv("Section Name:", sectionComponent.getTitle()));
            groupbox.addChild(buildLabelDiv("Section Code:", sectionComponent.getCode().getCodingFirstRep().getDisplay()));
        });
        return groupbox;
    }

    protected Div buildLabelDiv(String name, String value) {
        Div div = new Div();
        div.addChild(buildNvpNameLabel(name));
        div.addChild(buildNvpValueLabel(value));
        return div;
    }

    protected Label buildNvpNameLabel(String label) {
        return buildLabel(label, "nvp-name-label");
    }

    protected Label buildNvpValueLabel(String label) {
        return buildLabel(label, "nvp-value-label");
    }

    protected Label buildLabel(String label, String classString) {
        Label lbl = new Label(label);
        lbl.addClass(classString);
        return lbl;
    }

    public Grid buildEntryDefinitionGrid(String catalogId, Bundle entryBundle) {
        Grid entryDefinitionGrid = new Grid();
        entryDefinitionGrid.setTitle("Entry List");
        Columns columns = new Columns();
        columns.addChild(new Column("ID"));
        columns.addChild(new Column("Type"));
        columns.addChild(new Column("Purpose"));
        columns.addChild(new Column("classification"));
        columns.addChild(new Column("status"));
        columns.addChild(new Column("Item Name"));
        columns.addChild(new Column("Loinc Code"));
        columns.addChild(new Column("Action"));
        entryDefinitionGrid.addChild(columns);
        Rows rows = new Rows();
        populateEntryDefinitionGridRows(rows, catalogId, entryBundle);
        entryDefinitionGrid.addChild(rows);
        return entryDefinitionGrid;
    }

    public void populateEntryDefinitionGridRows(Rows rows, String catalogId, Bundle entryBundle) {
        List<Bundle.BundleEntryComponent> bundleEntries = entryBundle.getEntry();
        bundleEntries.forEach(bundleEntry -> {
            if(bundleEntry.getResource() instanceof EntryDefinition) {
                EntryDefinition entryDefinition = (EntryDefinition) bundleEntry.getResource();
                List<Resource> contained = entryDefinition.getContained();
                //Total Hack - assumes always one resource of type activity definition
                ActivityDefinition aa = (ActivityDefinition)contained.get(0);

                Row row = new Row();

                //ID
                Rowcell id = new Rowcell();
                id.setRowspan(1);
                id.setColspan(1);
                id.setLabel("" + entryDefinition.getId());
                id.setData(entryDefinition.getId());
                row.addChild(id);
                rows.addChild(row);
                //Type
                Rowcell type = new Rowcell();
                type.setRowspan(1);
                type.setColspan(1);
                type.setLabel("" + entryDefinition.getType().getCodingFirstRep().getCode());
                row.addChild(type);
                //Purpose
                Rowcell purpose = new Rowcell();
                purpose.setRowspan(1);
                purpose.setColspan(1);
                purpose.setLabel("" + entryDefinition.getPurpose().getCodingFirstRep().getCode());
                row.addChild(purpose);
                //Classification
                Rowcell classification = new Rowcell();
                classification.setRowspan(1);
                classification.setColspan(1);
                classification.setLabel("" + entryDefinition.getClassification().get(0).getText());
                row.addChild(classification);
                //status
                Rowcell status = new Rowcell();
                status.setRowspan(1);
                status.setColspan(1);
                status.setLabel("" + entryDefinition.getStatus().getCodingFirstRep().getDisplay());
                row.addChild(status);
                //Item Name
                Rowcell activityDefinitionName = new Rowcell();
                activityDefinitionName.setRowspan(1);
                activityDefinitionName.setColspan(1);
                activityDefinitionName.setLabel(aa.getName());
                row.addChild(activityDefinitionName);
                //Item Code
                Rowcell activityDefinitionCode = new Rowcell();
                activityDefinitionCode.setRowspan(1);
                activityDefinitionCode.setColspan(1);
                activityDefinitionCode.setLabel(aa.getCode().getCodingFirstRep().getCode());
                row.addChild(activityDefinitionCode);
                //Action
                Rowcell action = new Rowcell();
                action.setRowspan(1);
                action.setColspan(1);
                Button button = new Button("View Details");
                row.addChild(button);

                button.addEventListener("click", event -> {
                    Row selectedRow = event.getTarget().getAncestor(Row.class);
                    Rowcell entryIdCell = (Rowcell)selectedRow.getChildAt(0);
                    String entryUrl = (String)entryIdCell.getData();
                    buildEntryDefinitionTab(entryUrl);
                });


                rows.addChild(row);
            }
        });
    }

    protected void buildEntryDefinitionTab(String entryUrl) {
        String entryId = getIdFromGetUrl(entryUrl);
        if(entryId != null) {
            Bundle selectedEntryBundle = catalogService.getEntryDefinition(entryId);
            Map<String,Resource> bundledResourceIndex = new HashMap<String,Resource>();
            selectedEntryBundle.getEntry().forEach(bundleEntryComponent -> {
                Resource resource = bundleEntryComponent.getResource();
                bundledResourceIndex.put(resource.getId(),resource);
            });
            EntryDefinition selectedEntry = (EntryDefinition)bundledResourceIndex.get(entryUrl);
            System.out.println(selectedEntry);
            Tab entryDefinitionTab = new Tab("Entry Details - " + selectedEntry.getClassification().get(0).getCodingFirstRep().getDisplay());
            catalogTabView.addChild(entryDefinitionTab);
            Div entryDiv = populateEntryDetails(selectedEntry,bundledResourceIndex);
            entryDefinitionTab.addChild(entryDiv);
            entryDefinitionTab.setClosable(true);
            entryDefinitionTab.setSelected(true);
        }
    }

    /**
     * Handler to retrieve catalog entries that match the search query by:
     *
     * Catalog ID
     * Entry type
     * Entry purpose
     * Entry classification
     *
     * @param event
     */
    @EventHandler(value = "click", target = "btnEntrySearch1") private void btnEntrySearch1ClickHandler(Event event) {
        Map<String,String> parameters = new HashMap<>();
        String catalogId = cbCatalog1.getSelectedItem().getValue();
        parameters.put(CatalogService.SEARCH_PARAMETER_TYPE, cbEntryDefType1.getSelectedItem().getValue());
        parameters.put(CatalogService.SEARCH_PARAMETER_PURPOSE, cbPurpose1.getSelectedItem().getValue());
        parameters.put(CatalogService.SEARCH_PARAMETER_CLASSIFICATION, tbClassificationDisplay.getValue());
        Bundle bundle = catalogService.searchEntries(catalogId, parameters);
        Grid grid = buildEntryDefinitionGrid(catalogId, bundle);
        catalogSearchResultGridDiv.destroyChildren();
        catalogSearchResultGridDiv.addChild(grid);
    }

    /**
     * Handler to retrieve catalog entries that match the search query by:
     *
     * Catalog ID
     * Entry type
     * Entry purpose
     * Entry referenced item name
     *
     * @param event
     */
    @EventHandler(value = "click", target = "btnEntrySearch2") private void btnEntrySearch2ClickHandler(Event event) {
        Map<String,String> parameters = new HashMap<>();
        String catalogId = cbCatalog2.getSelectedItem().getValue();
        parameters.put(CatalogService.SEARCH_PARAMETER_TYPE, cbEntryDefType2.getSelectedItem().getValue());
        parameters.put(CatalogService.SEARCH_PARAMETER_PURPOSE, cbPurpose2.getSelectedItem().getValue());
        parameters.put(CatalogService.SEARCH_PARAMETER_REFERENCED_ITEM,tbActivityDefinitionName.getValue());
        Bundle bundle = catalogService.searchEntries(catalogId, parameters);
        Grid grid = buildEntryDefinitionGrid(catalogId, bundle);
        catalogSearchResultGridDiv.destroyChildren();
        catalogSearchResultGridDiv.addChild(grid);
    }

    protected void buildPurposeComboBox(Combobox purposeCombo) {
        Comboitem blank = createBlankComboItem();
        blank.setSelected(true);
        purposeCombo.addChild(blank);

        Comboitem purpose = new Comboitem("orderable");
        purpose.setValue("orderable");
        purposeCombo.addChild(purpose);

        Comboitem supporting = new Comboitem("supporting");
        supporting.setValue("supporting");
        purposeCombo.addChild(supporting);
    }

    public void populateCatalogCombo(Combobox catalogCombo) {
        Comboitem blank = createBlankComboItem();
        blank.setSelected(true);
        catalogCombo.addChild(blank);

        Bundle bundle = catalogService.getCatalogs();

        List<Bundle.BundleEntryComponent> bundles = bundle.getEntry();
        bundles.forEach(bundleEntry -> {
            Composition composition = (Composition)bundleEntry.getResource();
            Comboitem item = new Comboitem(composition.getId());
            item.setValue(composition.getId());
            catalogCombo.addChild(item);
        });
    }

    public void populateServiceCombo(Combobox serviceCombo) {
        Comboitem blank = createBlankComboItem();
        blank.setSelected(true);
        serviceCombo.addChild(blank);

        Comboitem type = new Comboitem("diagnostic-service");
        type.setValue("diagnostic-service");
        serviceCombo.addChild(type);
    }

    protected Div populateEntryDetails(EntryDefinition entryDefinition, Map<String,Resource> bundledResourceIndex) {
        Div entryDiv = new Div();
        entryDiv.addClass("entry-div");
        Div details = new Div();
        details.addClass("entry-detail-div");
        entryDiv.addChild(details);
        Div urlDiv = new Div();
        urlDiv.addClass("entry-detail-url");
        details.addChild(urlDiv);
        urlDiv.addChild(buildNvpNameLabel("Entry URL:"));
        //urlDiv.addChild(buildNvpValueLabel(entryDefinition.getId()));
        Hyperlink entryLink = buildEntryDefinitionHyperlink(entryDefinition);
        urlDiv.addChild(entryLink);
        Div typeDiv = new Div();
        typeDiv.addClass("entry-detail-type");
        details.addChild(typeDiv);
        typeDiv.addChild(buildNvpNameLabel("Entry type:"));
        typeDiv.addChild(buildNvpValueLabel(entryDefinition.getType().getCodingFirstRep().getCode()));
        Div purposeDiv = new Div();
        purposeDiv.addClass("entry-detail-purpose");
        details.addChild(purposeDiv);
        purposeDiv.addChild(buildNvpNameLabel("Entry purpose:"));
        purposeDiv.addChild(buildNvpValueLabel(entryDefinition.getPurpose().getCodingFirstRep().getCode()));
        Div classificationDiv = new Div();
        classificationDiv.addClass("entry-detail-classification");
        details.addChild(classificationDiv);
        classificationDiv.addChild(buildNvpNameLabel("Entry classification:"));
        if(entryDefinition.getClassification().size() > 0) {
            classificationDiv.addChild(buildNvpValueLabel(entryDefinition.getClassification().get(0).getText()));
        } else {
            classificationDiv.addChild(buildNvpValueLabel("No entry classification specified"));
        }
        Div statusDiv = new Div();
        statusDiv.addClass("entry-detail-status");
        details.addChild(statusDiv);
        statusDiv.addChild(buildNvpNameLabel("Entry status:"));
        statusDiv.addChild(buildNvpValueLabel(entryDefinition.getStatus().getCodingFirstRep().getDisplay()));
        Div target = new Div();
        target.addClass("entry-detail-target");
        Resource targetResource = entryDefinition.getReferencedItemTarget();
        Groupbox content = null;
        if(targetResource != null) {
            content = handleTargetRendering(target, targetResource);
            entryDiv.addChild(target);
        } else {
            if(entryDefinition.getContained().size() > 0) {
                System.out.println("Target resource is null");
                content = handleTargetRendering(target, entryDefinition.getContained().get(0));
                entryDiv.addChild(target);
            }
        }
        content.addClass("entry-detail-related-entries");
        final Groupbox relatedEntriesGroupbox = content;
        entryDiv.addChild(content);
        List<EntryDefinition.EntryDefinitionRelatedEntryComponent> relatedEntries = entryDefinition.getRelatedEntry();
        relatedEntries.forEach(relatedEntry -> {
            Groupbox relatedEntryGroupbox = new Groupbox();
            relatedEntryGroupbox.setTitle("Relationship: " + relatedEntry.getRelationtype().getText());
            relatedEntryGroupbox.addClass("entry-detail-related-entry");
            relatedEntriesGroupbox.addChild(relatedEntryGroupbox);
            Reference reference = relatedEntry.getItem();
            Resource resource = bundledResourceIndex.get(reference.getReference());
            if(resource instanceof EntryDefinition) {
                relatedEntryGroupbox.addChild(populateEntryDetails((EntryDefinition)resource, bundledResourceIndex));
            }
        });
        return entryDiv;

    }

    private Hyperlink buildEntryDefinitionHyperlink(EntryDefinition entryDefinition) {
        Hyperlink entryLink = new Hyperlink();
        entryLink.setLabel(entryDefinition.getId());
        entryLink.addEventListener("click", new IEventListener() {
            @Override
            public void onEvent(Event event) {
                buildEntrySummaryTab(entryDefinition);
            }
        });
        return entryLink;
    }

    protected void buildEntrySummaryTab(EntryDefinition entryDefinition) {
        Tab tab = new Tab("Entry Summary");
        catalogTabView.addChild(tab);
        tab.setSelected(true);
        tab.setClosable(true);
        EntryDefinition entryDefinitionResource = catalogService.getEntryDefinitionById(entryDefinition.getId());
        tab.addChild(buildEntrySummary(entryDefinitionResource));
    }

    protected Groupbox handleTargetRendering(Div target, Resource targetResource) {
        Groupbox content = new Groupbox();
        if(targetResource instanceof ActivityDefinition) {
            ActivityDefinition activityDefinition = (ActivityDefinition)targetResource;
            content.setTitle("Activity definition: " + activityDefinition.getName());
            target.addChild(content);
            if(StringUtils.isNotBlank(activityDefinition.getCode().getCodingFirstRep().getCode())) {
                content.addChild(buildLabelDiv("Kind:", renderCodingAsString(activityDefinition.getCode().getCodingFirstRep())));
            }
        } else if(targetResource instanceof SpecimenDefinition) {
            SpecimenDefinition specimenDefinition = (SpecimenDefinition)targetResource;
            content.setTitle("Specimen Definition");
            target.addChild(content);
            if(StringUtils.isNotBlank(specimenDefinition.getTypeCollected().getCodingFirstRep().getCode())) {
                content.addChild(buildLabelDiv("Kind:", renderCodingAsString(specimenDefinition.getTypeCollected().getCodingFirstRep())));
            }
        } else if(targetResource instanceof ObservationDefinition) {
            ObservationDefinition observationDefinition = (ObservationDefinition)targetResource;
            content.setTitle("Observation Definition");
            target.addChild(content);
            if(StringUtils.isNotBlank(observationDefinition.getCode().getCode())) {
                content.addChild(buildLabelDiv("Code:",renderCodingAsString(observationDefinition.getCode())));
            }

        } else {
            System.out.println("Unknown referenced item");
        }
        return content;
    }

    public Div buildEntrySummary(EntryDefinition entryDefinition) {
        Div entrySummaryDiv = new Div();
        Groupbox groupbox = new Groupbox();
        entrySummaryDiv.addChild(groupbox);
        groupbox.setTitle("Summary");
        groupbox.addClass("search-form-groupbox");
        groupbox.addChild(buildLabelDiv("Entry ID:", entryDefinition.getId()));
        groupbox.addChild(buildLabelDiv("Entry Type:", renderCodingAsString(entryDefinition.getType().getCodingFirstRep())));
        groupbox.addChild(buildLabelDiv("Purpose:", renderCodingAsString(entryDefinition.getPurpose().getCodingFirstRep())));
        if(entryDefinition.getClassification().size() > 0) {
            groupbox.addChild(buildLabelDiv("Classification:", renderCodingAsString(entryDefinition.getClassification().get(0).getCodingFirstRep())));
        }
        Div referencedItemResourceDiv = new Div();
        Resource resource = (Resource)entryDefinition.getReferencedItemTarget();
        if(resource == null) {
            resource = entryDefinition.getContained().get(0);
        }
        if(resource != null) {
            Groupbox referenceGroupBox = handleTargetRendering(referencedItemResourceDiv, resource);
            groupbox.addChild(referenceGroupBox);
            referenceGroupBox.addChild(buildLabelDiv("Referenced Item URL:", entryDefinition.getReferencedItem().getReference()));
        }
        entryDefinition.getRelatedEntry().forEach(entryDefinitionRelatedEntryComponent -> {
            Div relatedEntryDiv = new Div();
            relatedEntryDiv.addChild(buildLabelDiv("Relationship:", renderCodingAsString(entryDefinitionRelatedEntryComponent.getRelationtype().getCodingFirstRep())));
            EntryDefinition definition = catalogService.getEntryDefinitionById(entryDefinitionRelatedEntryComponent.getItem().getReference());
            relatedEntryDiv.addChild(buildEntryDefinitionHyperlink(definition));
            groupbox.addChild(relatedEntryDiv);
        });
        return entrySummaryDiv;
    }

    protected String renderCodingAsString(Coding coding) {
        if(coding == null) {
            return "No code defined";
        }
        String codeSystem = coding.getSystem();
        String code = coding.getCode();
        String displayName = coding.getDisplay();
        return displayName + " (" + codeSystem + ", " + code + ")";
    }

    private Comboitem createBlankComboItem() {
        Comboitem comboitem = new Comboitem("Select a value");
        comboitem.setValue("");
        return comboitem;
    }

    /**
     * Returns the last part of a get URL for format [server-base]/[resource]/[id]
     *
     * @param getUrl
     * @return
     */
    private String getIdFromGetUrl(String getUrl) {
        if(getUrl != null && getUrl.lastIndexOf("/") > 0) {
            return getUrl.substring(getUrl.lastIndexOf("/") + 1);
        } else {
            return null;
        }
    }
}

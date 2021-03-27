import {Injectable} from '@angular/core';
import {FieldOption, PartField, PlasmidField, ProteinField, SeedField, StrainField} from "../../../models/part-field";
import {CustomField} from "../../../models/custom-field";

@Injectable({
    providedIn: 'root'
})

export class PartInformationService {

    constructor() {
    }

    private partFields: PartField[] = [
        new PartField('Name', 'name').setInputType('short'),
        new PartField('Alias', 'alias').setInputType('short'),
        new PartField('Principal Investigator', 'principalInvestigator').setInputType('withEmail'),
        new PartField('Funding Source', 'fundingSource'),
        new PartField('Status', 'status').setOptions(
            [
                new FieldOption("Complete", "Complete"),
                new FieldOption("In Progress", "In Progress"),
                new FieldOption("Abandoned", "Abandoned"),
                new FieldOption("Planned", "Planned")
            ]),
        new PartField("BioSafety Level").setOptions([
            new FieldOption("1", "Level 1"),
            new FieldOption("2", "Level 2"),
            new FieldOption("-1", "Restricted")
        ]),
        new PartField("Creator", "creator").setInputType("withEmail"),
        new PartField("Keywords").setInputType("long"),
        new PartField("External URL", "links").setInputType("long"),
        new PartField("Summary", "shortDescription").setInputType("long"),
        new PartField("References").setInputType("long"),
        new PartField("Intellectual Property").setInputType("long")
    ];

    private plasmidFields = [
        new PlasmidField('Backbone'),
        new PlasmidField('Circular', 'circular').setInputType('bool'),
        new PlasmidField('Origin of Replication', 'originOfReplication').setAutoComplete('ORIGIN_OF_REPLICATION'),
        new PlasmidField('Selection Markers', 'selectionMarkers', '').setAutoComplete('SELECTION_MARKERS'),
        new PlasmidField('Promoters', 'promoters').setAutoComplete('PROMOTERS'),
        new PlasmidField('Replicates In', 'replicatesIn').setAutoComplete('REPLICATES_IN'),
    ];

    private proteinFields = [
        new ProteinField('Organism').setInputType('long'),
        new ProteinField('Full Name').setInputType('long'),
        new ProteinField('Gene Name').setInputType('long'),
        new ProteinField('Uploaded From').setInputType('long')
    ];

    private seedFields = [
        new SeedField('Sent To ABRC', 'sentToABRC').setInputType('bool'),
        new SeedField('Plant Type', 'plantType').setOptions([
            new FieldOption('EMS', 'EMS'),
            new FieldOption('OVER_EXPRESSION', 'OVER_EXPRESSION'),
            new FieldOption('RNAI', 'RNAi'),
            new FieldOption('REPORTER', 'Reporter'),
            new FieldOption('T_DNA', 'T-DNA'),
            new FieldOption('OTHER', 'Other')
        ]),
        new SeedField('Selection Markers', 'selectionMarkers', '').setAutoComplete('SELECTION_MARKERS'),
        new SeedField('Generation').setOptions([
            new FieldOption('F1'),
            new FieldOption('F2'),
            new FieldOption('F3'),
            new FieldOption('M0'),
            new FieldOption('M1'),
            new FieldOption('M2'),
            new FieldOption('M3'),
            new FieldOption('T0'),
            new FieldOption('T1'),
            new FieldOption('T2'),
            new FieldOption('T3'),
            new FieldOption('T4'),
            new FieldOption('T5'),
        ]),
        new SeedField('Harvest Date').setInputType('date'),
        new SeedField('Homozygosity'),
        new SeedField('Ecotype')
    ];

    private strainFields = [
        new StrainField('Host'),
        new StrainField('Genotype/Phenotype', 'genotypePhenotype').setInputType('long'),
        new StrainField('Selection Markers', 'selectionMarkers', '').setAutoComplete('SELECTION_MARKERS'),
    ];

    // retrieve plasmid fields while checking and accounting for modifications to existing fields
    getPlasmidFields(customFields?: CustomField[]): PartField[] {
        return this.getSubSchemaFields(this.plasmidFields, 'plasmidData', customFields);
    }

    getStrainFields(customFields?: CustomField[]): PartField[] {
        return this.getSubSchemaFields(this.strainFields, 'strainData', customFields);
    }

    getProteinFields(customFields?: CustomField[]): PartField[] {
        return this.getSubSchemaFields(this.proteinFields, 'proteinData', customFields);
    }

    getSeedFields(customFields?: CustomField[]): PartField[] {
        return this.getSubSchemaFields(this.seedFields, 'seedData', customFields);
    }

    getSubSchemaFields(schemaFields: PartField[], subSchema: string, customFields?: CustomField[]): PartField[] {
        const fields: PartField[] = this.getPartFields();
        for (let i = 0; i < schemaFields.length; i += 1) {
            const partField = customFields ? this.checkSetCustomField(customFields, schemaFields[i], subSchema) : schemaFields[i];
            fields.push(partField);
        }
        return fields;
    }

    getPartFields(): PartField[] {
        const fields: PartField[] = [];
        for (let i = 0; i < this.partFields.length; i += 1)
            fields.push(this.partFields[i]);
        return fields;
    }

    private checkSetCustomField(customFields: CustomField[], partField: PartField, subSchema?: string): PartField {
        for (let customField of customFields) {
            if (partField.schema === this.convertToSchema(customField.existingField)) {
                partField.label = customField.label;
                return partField;
            }
        }
        return partField;
    }

    // converts an existing field to the Part object field
    convertToSchema(existingField: string) {
        switch (existingField) {
            case 'HOST':
                return 'host';

            case 'IP':
                return 'intellectualProperty';
        }
        return '';
    }
}
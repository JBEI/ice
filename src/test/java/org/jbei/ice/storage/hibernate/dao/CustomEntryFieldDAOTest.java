package org.jbei.ice.storage.hibernate.dao;

import org.jbei.ice.lib.dto.entry.EntryType;
import org.jbei.ice.lib.dto.entry.FieldType;
import org.jbei.ice.storage.hibernate.HibernateRepositoryTest;
import org.jbei.ice.storage.model.CustomEntryFieldModel;
import org.jbei.ice.storage.model.CustomEntryFieldOptionModel;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class CustomEntryFieldDAOTest extends HibernateRepositoryTest {

    private CustomEntryFieldDAO dao = new CustomEntryFieldDAO();

    @Test
    public void testCreate() {
        List<String> fieldStrings = new ArrayList<>(Arrays.asList("Expression", "Cloning", "Synthetic Biology", "CRISPR", "RNAi", "Other"));

        CustomEntryFieldModel field = new CustomEntryFieldModel();
        field.setLabel("Plasmid Use");
        field.setEntryType(EntryType.PLASMID);
        field.setFieldType(FieldType.MULTI_CHOICE);

        for (String fieldString : fieldStrings) {
            field.getCustomFieldLabels().add(new CustomEntryFieldOptionModel(fieldString));
        }
        field = dao.create(field);
        Assert.assertNotNull(field);
        Assert.assertEquals(field.getCustomFieldLabels().size(), fieldStrings.size());
    }

    @Test
    public void testGetLabelForType() {
        final String label = "CustomEntryFieldDAOTest.getLabelForType";
        CustomEntryFieldModel field = new CustomEntryFieldModel();
        field.setEntryType(EntryType.PART);
        field.setRequired(true);
        field.setLabel(label);

        field = dao.create(field);
        Assert.assertNotNull(field);

        Optional<CustomEntryFieldModel> match = dao.getLabelForType(EntryType.PART, label);
        Assert.assertTrue(match.isPresent());
        Assert.assertEquals(match.get().getEntryType(), field.getEntryType());
        Assert.assertEquals(match.get().getRequired(), field.getRequired());
        Assert.assertEquals(match.get().getLabel(), field.getLabel());

        Assert.assertFalse(dao.getLabelForType(EntryType.PLASMID, label).isPresent());
    }
}
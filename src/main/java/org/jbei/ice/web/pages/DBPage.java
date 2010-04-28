package org.jbei.ice.web.pages;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.jbei.ice.controllers.SequenceController;
import org.jbei.ice.lib.dao.DAO;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.managers.SequenceManager;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.models.Sequence;
import org.jbei.ice.lib.models.SequenceFeature;
import org.jbei.ice.lib.parsers.GeneralParser;
import org.jbei.ice.lib.utils.SequenceUtils;
import org.jbei.ice.lib.utils.Utils;
import org.jbei.ice.lib.vo.FeaturedDNASequence;
import org.jbei.ice.lib.vo.IDNASequence;

public class DBPage extends UnprotectedPage {
    public DBPage() {
        super();

        /*numberOfAllSequences();

        System.out.println("================================================");

        listInvalidSequenceUser();

        System.out.println("================================================");

        listInvalidSequenceSequence();

        System.out.println("================================================");

        checkingNotParsedSequence();

        System.out.println("================================================");

        checkingShortSequences();

        System.out.println("================================================");

        failedToReparseSequences();

        System.out.println("================================================");

        compareOldParsedAndNewlyParsedSequences();

        System.out.println("================================================");

        compareHashes();

        System.out.println("================================================");

        compareNumberOfFeatures();

        System.out.println("================================================");

        listSequencesWithoutSequenceUser();

        System.out.println("================================================");

        checkFeaturesHashes();

        System.out.println("================================================");

        //checkFeaturesExistanceByHashes();

        System.out.println("================================================");*/
        reuploadSequences();
    }

    private void numberOfAllSequences() {
        ArrayList<Sequence> allSequences = allSequences();

        System.out.println("Number of sequences: " + allSequences.size());
    }

    private void listInvalidSequenceUser() {
        System.out.println("Checking original sequences...");

        ArrayList<Sequence> allSequences = allSequences();

        for (int i = 0; i < allSequences.size(); i++) {
            Sequence sequence = allSequences.get(i);

            if (sequence.getSequenceUser() == null || sequence.getSequenceUser().isEmpty()
                    || sequence.getSequenceUser().length() < 50) {
                System.out.println("EntryId: " + sequence.getEntry().getId() + "; "
                        + sequence.getSequenceUser());
            }
        }
    }

    private void listInvalidSequenceSequence() {
        System.out.println("Checking parsed sequences...");

        ArrayList<Sequence> allSequences = allSequences();

        for (int i = 0; i < allSequences.size(); i++) {
            Sequence sequence = allSequences.get(i);

            if (sequence.getSequence() == null || sequence.getSequence().isEmpty()
                    || sequence.getSequence().length() < 50) {
                System.out.println("EntryId: " + sequence.getEntry().getId() + "; "
                        + sequence.getSequence());
            }
        }
    }

    private void checkingNotParsedSequence() {
        System.out.println("Checking not parsed sequence...");

        ArrayList<Sequence> allSequences = allSequences();

        for (int i = 0; i < allSequences.size(); i++) {
            Sequence sequence = allSequences.get(i);

            if (sequence.getSequenceUser() != null && !sequence.getSequenceUser().isEmpty()
                    && (sequence.getSequence() == null || sequence.getSequence().isEmpty())) {
                System.out.println("EntryId: " + sequence.getEntry().getId() + "; "
                        + sequence.getSequence());
            }
        }
    }

    private void checkingShortSequences() {
        System.out.println("Checking short sequences...");

        ArrayList<Sequence> allSequences = allSequences();

        for (int i = 0; i < allSequences.size(); i++) {
            Sequence sequence = allSequences.get(i);

            if (sequence.getSequence().length() < 100) {
                System.out.println("EntryId: " + sequence.getEntry().getId() + "; "
                        + sequence.getSequence());
            }
        }
    }

    private void failedToReparseSequences() {
        System.out.println("Failed to reparse sequences...");

        ArrayList<Sequence> allSequences = allSequences();

        for (int i = 0; i < allSequences.size(); i++) {
            Sequence sequence = allSequences.get(i);

            IDNASequence dnaSequence = GeneralParser.getInstance()
                    .parse(sequence.getSequenceUser());

            if (dnaSequence == null) {
                System.out.println("EntryId: " + sequence.getEntry().getId() + "; "
                        + sequence.getSequence());
            }
        }
    }

    private void compareOldParsedAndNewlyParsedSequences() {
        System.out.println("Compare old parsed and newly parsed sequences...");

        ArrayList<Sequence> allSequences = allSequences();

        for (int i = 0; i < allSequences.size(); i++) {
            Sequence sequence = allSequences.get(i);

            IDNASequence dnaSequence = GeneralParser.getInstance()
                    .parse(sequence.getSequenceUser());

            if (dnaSequence == null) {
                System.out.println("EntryId: " + sequence.getEntry().getId() + "; Skipped");

                continue;
            }

            if (!dnaSequence.getSequence().equals(sequence.getSequence())) {
                System.out.println("EntryId: " + sequence.getEntry().getId()
                        + "; Sequences doesn't much!");
                System.out.println("\tOld Sequence: " + sequence.getSequence());
                System.out.println("\tNew Sequence: " + dnaSequence.getSequence());
                System.out.println("\t-----");
            }
        }
    }

    private void compareHashes() {
        System.out.println("Compare hashes...");

        ArrayList<Sequence> allSequences = allSequences();

        for (int i = 0; i < allSequences.size(); i++) {
            Sequence sequence = allSequences.get(i);

            if (sequence.getFwdHash() == null || sequence.getFwdHash().isEmpty()
                    || sequence.getRevHash() == null || sequence.getRevHash().isEmpty()) {
                System.out.println("NULL HASHESSS!!!!");

                continue;
            }

            if (!sequence.getFwdHash().equals(
                SequenceUtils.calculateSequenceHash(sequence.getSequence()))) {

                System.out.println("EntryId: " + sequence.getEntry().getId() + "; Invalid hash!");
            }
        }
    }

    private void compareNumberOfFeatures() {
        System.out.println("Compare number of features...");

        ArrayList<Sequence> allSequences = allSequences();

        for (int i = 0; i < allSequences.size(); i++) {
            Sequence sequence = allSequences.get(i);

            int oldNumberOfFeatures = sequence.getSequenceFeatures().size();

            IDNASequence dnaSequence = GeneralParser.getInstance()
                    .parse(sequence.getSequenceUser());

            int newNumberOfFeatures = 0;
            if (dnaSequence instanceof FeaturedDNASequence) {
                FeaturedDNASequence featuredDNASequencedna = (FeaturedDNASequence) dnaSequence;

                newNumberOfFeatures = featuredDNASequencedna.getFeatures().size();
            }

            if (oldNumberOfFeatures != newNumberOfFeatures) {
                System.out.println("EntryId: " + sequence.getEntry().getId()
                        + "; Number of features doesn't much! : " + oldNumberOfFeatures + " - "
                        + newNumberOfFeatures);
            }
        }
    }

    private void listSequencesWithoutSequenceUser() {
        System.out.println("List sequences without sequenceUser...");

        ArrayList<Sequence> allSequences = allSequences();

        for (int i = 0; i < allSequences.size(); i++) {
            Sequence sequence = allSequences.get(i);

            if ((sequence.getSequenceUser() == null || sequence.getSequenceUser().isEmpty() || sequence
                    .getSequenceUser().equals(""))
                    && sequence.getSequence() != null
                    && !sequence.getSequence().isEmpty()
                    && !sequence.getSequence().equals("")) {

                System.out.println("EntryId: " + sequence.getEntry().getId());
            }
        }
    }

    private void checkFeaturesHashes() {
        System.out.println("Check feature hashes...");

        ArrayList<Sequence> allSequences = allSequences();

        for (int i = 0; i < allSequences.size(); i++) {
            Sequence sequence = allSequences.get(i);

            for (SequenceFeature sequenceFeature : sequence.getSequenceFeatures()) {
                if (!sequenceFeature.getFeature().getHash()
                        .equals(
                            SequenceUtils.calculateSequenceHash(sequenceFeature.getFeature()
                                    .getSequence()))) {
                    System.out.println("EntryId: " + sequence.getEntry().getId());
                    System.out.println(sequenceFeature.getFeature().getSequence());
                    System.out.println(sequenceFeature.getFeature().getHash());
                    System.out.println(SequenceUtils.calculateSequenceHash(sequenceFeature
                            .getFeature().getSequence()));
                    System.out.println("--------------");
                }
            }
        }
    }

    private void checkFeaturesExistanceByHashes() {
        System.out.println("Check feature existance from SequenceUser and db...");

        ArrayList<Sequence> allSequences = allSequences();

        for (int i = 0; i < allSequences.size(); i++) {
            Sequence sequence = allSequences.get(i);

            Map<String, SequenceFeature> sequenceFeaturesMap = new HashMap<String, SequenceFeature>();

            for (SequenceFeature sequenceFeature : sequence.getSequenceFeatures()) {
                sequenceFeaturesMap.put(sequenceFeature.getFeature().getHash(), sequenceFeature);

                /*System.out.println(sequenceFeature.getFeature().getHash() + " | "
                        + sequenceFeature.getStart() + " : " + sequenceFeature.getEnd() + " | "
                        + sequenceFeature.getFeature().getSequence());*/
            }

            //System.out.println("========================================================");

            IDNASequence dnaSequence = GeneralParser.getInstance()
                    .parse(sequence.getSequenceUser());

            if (dnaSequence instanceof FeaturedDNASequence) {
                FeaturedDNASequence featuredDNASequencedna = (FeaturedDNASequence) dnaSequence;

                Sequence reparsedSequence = SequenceController
                        .dnaSequenceToSequence(featuredDNASequencedna);

                for (SequenceFeature sequenceFeature : reparsedSequence.getSequenceFeatures()) {
                    String reparsedFeatureSequence = sequenceFeature.getFeature().getSequence();

                    String newFeatureHash = SequenceUtils
                            .calculateSequenceHash(reparsedFeatureSequence);

                    /*System.out.println(newFeatureHash + " | " + sequenceFeature.getStart() + " : "
                            + sequenceFeature.getEnd() + " | " + reparsedFeatureSequence);*/

                    if (!sequenceFeaturesMap.containsKey(newFeatureHash)) {
                        System.out.println("EntryId: " + sequence.getEntry().getId() + " | "
                                + reparsedFeatureSequence + " | " + newFeatureHash);
                    }
                }
            } else {
                if (sequenceFeaturesMap.size() > 0) {
                    System.out.println("WTF!");
                }
            }
        }
    }

    private void reuploadSequences() {
        System.out.println("Reuploading Features and SequenceFeatures tables in db...");

        ArrayList<Sequence> allSequences = allSequences();

        for (int i = 0; i < allSequences.size(); i++) {
            Sequence sequence = allSequences.get(i);

            if (sequence == null || sequence.getSequenceUser() == null
                    || sequence.getSequenceUser().isEmpty()) {
                continue;
            }

            try {
                IDNASequence dnaSequence = GeneralParser.getInstance().parse(
                    sequence.getSequenceUser());

                if (dnaSequence instanceof FeaturedDNASequence) {
                    Sequence reparsedSequence = SequenceController
                            .dnaSequenceToSequence(dnaSequence);

                    Entry entry = sequence.getEntry();

                    reparsedSequence.setEntry(entry);
                    reparsedSequence.setSequenceUser(sequence.getSequenceUser());

                    SequenceManager.deleteSequence(sequence);

                    SequenceManager.saveSequence(reparsedSequence);
                }
            } catch (ManagerException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        System.out.println("Reuploading DONE...");
    }

    @SuppressWarnings("unchecked")
    private ArrayList<Sequence> allSequences() {
        ArrayList<Sequence> sequences = null;

        Session session = DAO.newSession();
        try {
            Query query = session.createQuery("from " + Sequence.class.getName());

            List list = query.list();

            if (list != null) {
                sequences = (ArrayList<Sequence>) list;
            }
        } catch (HibernateException e) {
            System.out.println(Utils.stackTraceToString(e));
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }

        return sequences;
    }
}

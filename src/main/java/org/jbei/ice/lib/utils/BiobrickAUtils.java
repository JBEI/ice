package org.jbei.ice.lib.utils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jbei.ice.controllers.EntryController;
import org.jbei.ice.controllers.SequenceController;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.managers.SequenceManager;
import org.jbei.ice.lib.models.Account;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.models.Feature;
import org.jbei.ice.lib.models.Name;
import org.jbei.ice.lib.models.Part;
import org.jbei.ice.lib.models.Sequence;
import org.jbei.ice.lib.models.SequenceFeature;
import org.jbei.ice.lib.models.Part.AssemblyStandard;
import org.jbei.ice.lib.permissions.PermissionException;

public class BiobrickAUtils implements AssemblyUtils {

    /*
     * Biobrick A Assembly Standard (BBF RFC10, RFC20)
     *   EcorI and XbaI sites in Prefix
     *  (g^aatt c) gcggccgct (t^ctaga g)
     *  
     *  SpeI and PstI sites in Suffix
     *  t(a^ctag t)agcggccg (ctgca^g)
     *  See documentation directory for further information
     */
    // CONSTANTS:
    // always check for long prefix first when doing startsWith()
    // NOTE: BiobrickB has a cleaner implementation. See there if 
    // refactoring is desired.
    public static final String biobrickAPrefix = "gaattcgcggccgcttctagag"; // RFC10
    public static final int biobrickAPrefixMinimum2ndFeaturePosition = 16; // zero based
    public static final int biobrickAPrefixChop = 16; // characters to chop
    public static final String biobrickAPrefixFeatureName = "Biobrick A Long Prefix";
    public static final String biobrickAPrefix2 = "gaattcgcggccgcttctag"; // RFC20
    public static final int biobrickAPrefix2Minimum2ndFeaturePosition = 16;
    public static final int biobrickAPrefix2Chop = 16;
    public static final String biobrickAPrefix2FeatureName = "Biobrick A Short Prefix";
    public static final String biobrickASuffix = "tactagtagcggccgctgcag"; // RFC10
    public static final int biobrickASuffixMaximum1stFeaturePosition = -15; // position from end
    public static final int biobrickASuffixChop = 19;
    public static final int biobrickASuffixScarOffset = 21;
    public static final String biobrickASuffixFeatureName = "Biobrick A Short Suffix";
    public static final String biobrickASuffix2 = "tactagtagcggccgcctgcagg"; // RFC20
    public static final int biobrickASuffix2Maximum1stFeaturePosition = -17; // position from end
    public static final int biobrickASuffix2Chop = 21;
    public static final int biobrickASuffix2ScarOffset = 23;
    public static final String biobrickASuffix2FeatureName = "Biobrick A Long Suffix";
    public static final String biobrickAScarShort = "tactag";
    public static final String biobrickAScarShortFeatureName = "Biobrick A Short Scar";
    public static final String biobrickAScarLong = "tactagag";
    public static final String biobrickAScarLongFeatureName = "Biobrick A Long Scar";

    public AssemblyStandard determineAssemblyStandard(String partSequenceString) {
        AssemblyStandard result = null;
        result = determineBiobrickAAssemblyStandard(partSequenceString);
        return result;
    }

    public SequenceFeatureCollection determineAssemblyFeatures(Sequence partSequence)
            throws UtilityException {
        return determineBiobrickAFeatures(partSequence);
    }

    public Sequence join(Part part1, Part part2) throws UtilityException {
        return joinBiobrickA(part1, part2);
    }

    private static AssemblyStandard determineBiobrickAAssemblyStandard(String partSequenceString) {
        AssemblyStandard result = null;
        if (partSequenceString.startsWith(biobrickAPrefix)) {
            if (partSequenceString.endsWith(biobrickASuffix)) {
                result = AssemblyStandard.BIOBRICKA;
            } else if (partSequenceString.endsWith(biobrickASuffix2)) {
                result = AssemblyStandard.BIOBRICKA;
            }
        } else if (partSequenceString.startsWith(biobrickAPrefix2)) {
            if (partSequenceString.endsWith(biobrickASuffix)) {
                result = AssemblyStandard.BIOBRICKA;
            } else if (partSequenceString.endsWith(biobrickASuffix2)) {
                result = AssemblyStandard.BIOBRICKA;
            }
        }
        return result;
    }

    private static SequenceFeatureCollection determineBiobrickAFeatures(Sequence partSequence)
            throws UtilityException {
        //all positions are 0 based positions, not offsets
        String partSequenceString = partSequence.getSequence();
        int partSequenceLength = partSequenceString.length();
        SequenceFeatureCollection sequenceFeatures = new SequenceFeatureCollection();
        if (partSequenceString.startsWith(biobrickAPrefix)) {
            Feature feature = new Feature(biobrickAPrefixFeatureName, biobrickAPrefixFeatureName,
                    "", biobrickAPrefix, 1, "MISC_FEATURE");
            try {
                feature = SequenceManager.getReferenceFeature(feature);
            } catch (ControllerException e) {
                throw new UtilityException(e);
            }
            SequenceFeature sequenceFeature = new SequenceFeature(partSequence, feature, 1, 22, +1,
                    feature.getName(), feature.getDescription(), feature.getGenbankType());
            sequenceFeature.setFlag(SequenceFeature.Flag.PREFIX);
            sequenceFeatures.add(sequenceFeature);
        } else if (partSequenceString.startsWith(biobrickAPrefix2)) {
            Feature feature = new Feature(biobrickAPrefix2FeatureName, biobrickAPrefix2FeatureName,
                    "", biobrickAPrefix2, 1, "MISC_FEATURE");
            try {
                feature = SequenceManager.getReferenceFeature(feature);
            } catch (ControllerException e) {
                throw new UtilityException(e);

            }
            SequenceFeature sequenceFeature = new SequenceFeature(partSequence, feature, 1, 20, +1,
                    feature.getName(), feature.getDescription(), feature.getGenbankType());
            sequenceFeature.setFlag(SequenceFeature.Flag.PREFIX);
            sequenceFeatures.add(sequenceFeature);
        }
        if (partSequenceString.endsWith(biobrickASuffix)) {
            Feature feature = new Feature(biobrickASuffixFeatureName, biobrickASuffixFeatureName,
                    "", biobrickASuffix, 1, "MISC_FEATURE");
            try {
                feature = SequenceManager.getReferenceFeature(feature);
            } catch (ControllerException e) {
                throw new UtilityException(e);
            }
            SequenceFeature sequenceFeature = new SequenceFeature(partSequence, feature,
                    partSequenceLength - 20, partSequenceLength, +1, feature.getName(), feature
                            .getDescription(), feature.getGenbankType());
            sequenceFeature.setFlag(SequenceFeature.Flag.SUFFIX);
            sequenceFeatures.add(sequenceFeature);
        } else if (partSequenceString.endsWith(biobrickASuffix2)) {
            Feature feature = new Feature(biobrickASuffix2FeatureName, biobrickASuffix2FeatureName,
                    "", biobrickASuffix2, 1, "MISC_FEATURE");
            try {
                feature = SequenceManager.getReferenceFeature(feature);
            } catch (ControllerException e) {
                throw new UtilityException(e);
            }
            SequenceFeature sequenceFeature = new SequenceFeature(partSequence, feature,
                    partSequenceLength - 22, partSequenceLength, +1, feature.getName(), feature
                            .getDescription(), feature.getGenbankType());
            sequenceFeature.setFlag(SequenceFeature.Flag.SUFFIX);
            sequenceFeatures.add(sequenceFeature);
        }
        // determine inner feature
        // 
        int absoluteMinimumFeatureStart = 0;
        int absoluteMaximumFeatureEnd = partSequenceLength;
        int minimumFeatureStart = 0;
        int maximumFeatureEnd = 0;
        if (partSequenceString.startsWith(biobrickAPrefix)) {
            absoluteMinimumFeatureStart = biobrickAPrefixMinimum2ndFeaturePosition;
            minimumFeatureStart = 22;
        } else if (partSequenceString.startsWith(biobrickAPrefix2)) {
            absoluteMinimumFeatureStart = biobrickAPrefix2Minimum2ndFeaturePosition;
            minimumFeatureStart = 20;
        } else {
            throw new UtilityException("Unknown prefix for second part");
        }
        if (partSequenceString.endsWith(biobrickASuffix)) {
            absoluteMaximumFeatureEnd = partSequenceLength
                    + biobrickASuffixMaximum1stFeaturePosition - 1;
            maximumFeatureEnd = partSequenceLength - 21 - 1;
        } else if (partSequenceString.endsWith(biobrickASuffix2)) {
            absoluteMaximumFeatureEnd = partSequenceLength
                    + biobrickASuffix2Maximum1stFeaturePosition - 1;
            maximumFeatureEnd = partSequenceLength - 23 - 1;
        } else {
            throw new UtilityException("Unknown suffix for first part");
        }
        /*
        calculate inner feature by starting from normal inner boundary
        and extending that boundary as dictated by annotated feature,
        while checking that those annotations stay within the maximum and 
        minimum allowed.
        */
        if (partSequence != null) {
            Set<SequenceFeature> existingSequenceFeatures = partSequence.getSequenceFeatures();
            for (SequenceFeature sequenceFeature : existingSequenceFeatures) {
                int start = sequenceFeature.getStart() - 1;
                int end = sequenceFeature.getEnd() - 1;
                if ((start < minimumFeatureStart) && (start > absoluteMinimumFeatureStart)) {
                    minimumFeatureStart = start;
                }
                if ((end > maximumFeatureEnd) && (end < absoluteMaximumFeatureEnd)) {
                    maximumFeatureEnd = end;
                }
            }
        }
        Entry part = partSequence.getEntry();
        String featureName = part.getRecordId(); // uuid of the given part
        String featureDescription = featureName;
        String featureIdentification = part.getRecordId();
        Feature innerPartFeature = new Feature(featureName, featureDescription,
                featureIdentification, partSequenceString, 0, "MISC_FEATURE");
        SequenceFeature sequenceFeature = new SequenceFeature(partSequence, innerPartFeature,
                minimumFeatureStart + 1, maximumFeatureEnd + 1, +1, innerPartFeature.getName(),
                innerPartFeature.getDescription(), innerPartFeature.getGenbankType());
        sequenceFeature.setFlag(SequenceFeature.Flag.INNER);
        sequenceFeatures.add(sequenceFeature);
        // check if part has at least prefix, suffix, and one inner feature
        //
        int temp = 0;
        List<SequenceFeature> prefixFeature = sequenceFeatures.get(SequenceFeature.Flag.PREFIX);
        List<SequenceFeature> suffixFeature = sequenceFeatures.get(SequenceFeature.Flag.SUFFIX);
        List<SequenceFeature> innerFeature = sequenceFeatures.get(SequenceFeature.Flag.INNER);
        if (prefixFeature.size() == 1) {
            if (prefixFeature.get(0).getStart() == 1) {
                temp = temp + 1;
            }
        }
        if (suffixFeature.size() == 1) {
            if (suffixFeature.get(0).getEnd() == partSequenceLength) {
                temp = temp + 1;
            }
        }
        if (innerFeature.size() == 1) {
            temp = temp + 1;
        }
        if (temp != 3) {
            throw new UtilityException("Could not determine prefix, suffix, or inner feature");
        }
        return sequenceFeatures;
    }

    public Sequence joinBiobrickA(Part part1, Part part2) throws UtilityException {
        Sequence result = null;
        if (part1.getPackageFormat().equals(Part.AssemblyStandard.BIOBRICKA)
                && part2.getPackageFormat().equals(Part.AssemblyStandard.BIOBRICKA)) {
            Sequence part1Sequence = null;
            Sequence part2Sequence = null;
            try {
                part1Sequence = SequenceManager.getByEntry(part1);
                part2Sequence = SequenceManager.getByEntry(part2);
            } catch (ManagerException e) {
                throw new UtilityException(e);
            }
            // concat sequence string
            String joinedSequence = null;
            String part1SequenceString = part1Sequence.getSequence();
            String part2SequenceString = part2Sequence.getSequence();
            int prefixOffset = 0;
            int suffixOffset = 0;
            int scarLength = 6;
            int scarStart = -1;
            if (part2SequenceString.startsWith(biobrickAPrefix)) {
                prefixOffset = biobrickAPrefixChop;
                scarLength = 8;
            } else if (part2SequenceString.startsWith(biobrickAPrefix2)) {
                prefixOffset = biobrickAPrefix2Chop;
            } else {
                throw new UtilityException("Unknown prefix for second part");
            }
            if (part1SequenceString.endsWith(biobrickASuffix)) {
                suffixOffset = biobrickASuffixChop;
                scarStart = part1SequenceString.length() - biobrickASuffixScarOffset;
            } else if (part1SequenceString.endsWith(biobrickASuffix2)) {
                suffixOffset = biobrickASuffix2Chop;
                scarStart = part1SequenceString.length() - biobrickASuffix2ScarOffset;
            } else {
                throw new UtilityException("Unknown suffix for first part");
            }
            String firstPart = part1SequenceString.substring(0, part1SequenceString.length()
                    - suffixOffset);
            String secondPart = part2SequenceString.substring(prefixOffset);
            //            concatSequence = part1SequenceString.substring(0, part1SequenceString.length()
            //                    - suffixOffset);
            //            concatSequence = concatSequence + part2SequenceString.substring(prefixOffset - 1);
            joinedSequence = firstPart + secondPart;
            // end concat sequence
            // create new part
            Part newPart = new Part();
            // setting part info
            HashSet<Name> inputNames = new HashSet<Name>();
            String newPartNameString = part1.getOnePartNumber().getPartNumber() + "+"
                    + part2.getOnePartNumber().getPartNumber();
            inputNames.add(new Name(newPartNameString, newPart));
            newPart.setNames(inputNames);
            newPart.setShortDescription("Assembly of " + newPartNameString);
            newPart.setStatus("in progress");
            newPart.setBioSafetyLevel(Math
                    .max(part1.getBioSafetyLevel(), part2.getBioSafetyLevel()));
            newPart.setOwner(getAccount().getFullName());
            newPart.setOwnerEmail(getAccount().getEmail());
            newPart.setCreator(getAccount().getFullName());
            newPart.setCreatorEmail(getAccount().getEmail());
            EntryController entryController = new EntryController(getAccount());
            try {
                newPart = (Part) entryController.createEntry(newPart);
            } catch (ControllerException e) {
                throw new UtilityException(e);
            }
            SequenceController sequenceController = new SequenceController(getAccount());
            Sequence newPartSequence = new Sequence();
            newPartSequence.setEntry(newPart);
            newPartSequence.setSequence(joinedSequence);
            newPartSequence.setFwdHash(SequenceUtils.calculateSequenceHash(joinedSequence));
            newPartSequence.setRevHash(SequenceUtils
                    .calculateReverseComplementSequenceHash(joinedSequence));
            try {
                newPartSequence = sequenceController.save(newPartSequence);
            } catch (PermissionException e) {
                throw new UtilityException(e);
            } catch (ControllerException e) {
                throw new UtilityException(e);
            }
            // calculate and annotate biobrick sequencefeatures
            Set<SequenceFeature> newPartSequenceFeatures = newPartSequence.getSequenceFeatures();
            SequenceFeatureCollection newFeatures = determineBiobrickAFeatures(newPartSequence);
            // annotate subinner features and scar
            //
            SequenceFeatureCollection part1SequenceFeatures = (SequenceFeatureCollection) part1Sequence
                    .getSequenceFeatures();
            SequenceFeature part1InnerFeature = part1SequenceFeatures.get(
                SequenceFeature.Flag.INNER).get(0);
            SequenceFeatureCollection part2SequenceFeatures = (SequenceFeatureCollection) part2Sequence
                    .getSequenceFeatures();
            SequenceFeature part2InnerFeature = part2SequenceFeatures.get(
                SequenceFeature.Flag.INNER).get(0);
            // part1 inner feature as subinner feature 
            SequenceFeature temp = new SequenceFeature();
            temp.setSequence(newPartSequence);
            temp.setFeature(part1InnerFeature.getFeature());
            temp.setName(part1InnerFeature.getName());
            temp.setFlag(SequenceFeature.Flag.SUBINNER);
            temp.setStart(part1InnerFeature.getStart());
            temp.setEnd(part1InnerFeature.getEnd());
            temp.setStrand(part1InnerFeature.getStrand());
            newFeatures.add(temp);
            // part 2 inner feature as subinner feature
            temp = new SequenceFeature();
            int offset = scarStart + 2;
            temp.setSequence(newPartSequence);
            temp.setFeature(part2InnerFeature.getFeature());
            temp.setName(part2InnerFeature.getName());
            temp.setFlag(SequenceFeature.Flag.SUBINNER);
            temp.setStart(part2InnerFeature.getStart() - biobrickAPrefixMinimum2ndFeaturePosition
                    + offset);
            temp.setEnd(part2InnerFeature.getEnd() - biobrickAPrefixMinimum2ndFeaturePosition
                    + offset);
            temp.setStrand(part2InnerFeature.getStrand());
            newFeatures.add(temp);
            // scar
            temp = new SequenceFeature();
            temp.setSequence(newPartSequence);
            temp.setFlag(SequenceFeature.Flag.SCAR);
            temp.setStart(scarStart + 1);
            temp.setEnd(scarStart + scarLength);
            temp.setStrand(1);
            if (scarLength == 8) {
                try {
                    temp.setFeature(getBiobrickALongScarFeature());
                } catch (ControllerException e) {
                    throw new UtilityException(e);
                }
                temp.setName(biobrickAScarLongFeatureName);
            } else {
                try {
                    temp.setFeature(getBiobrickAShortScarFeature());
                } catch (ControllerException e) {
                    throw new UtilityException(e);
                }
                temp.setName(biobrickAScarShortFeatureName);
            }
            newFeatures.add(temp);
            newPartSequenceFeatures.addAll(newFeatures);
            try {
                newPartSequence = sequenceController.save(newPartSequence);
            } catch (PermissionException e) {
                throw new UtilityException(e);
            } catch (ControllerException e) {
                throw new UtilityException(e);
            }
            result = newPartSequence;
        }
        return result;
    }

    private static Feature getBiobrickAShortScarFeature() throws ControllerException {
        Feature feature = new Feature();
        feature.setAutoFind(1);
        feature.setDescription(biobrickAScarShortFeatureName);
        feature.setName(biobrickAScarShortFeatureName);
        feature.setGenbankType("MISC_FEATURE");
        feature.setSequence(biobrickAScarShort);
        feature.setHash(SequenceUtils.calculateSequenceHash(biobrickAScarShort));
        feature = SequenceManager.getReferenceFeature(feature);
        return feature;
    }

    private static Feature getBiobrickALongScarFeature() throws ControllerException {
        Feature feature = new Feature();
        feature.setAutoFind(1);
        feature.setDescription(biobrickAScarLongFeatureName);
        feature.setName(biobrickAScarLongFeatureName);
        feature.setGenbankType("MISC_FEATURE");
        feature.setSequence(biobrickAScarLong);
        feature.setHash(SequenceUtils.calculateSequenceHash(biobrickAScarLong));
        feature = SequenceManager.getReferenceFeature(feature);
        return feature;
    }

    private Account getAccount() {
        // TODO Auto-generated method stub
        return null;
    }

    public static void main(String[] args) {

        /*
        String part1Sequence = "gaattcgcggccgcttctagag--tactagtagcggccgctgcag";
        String part2Sequence = "gaattcgcggccgcttctag--tactagtagcggccgcctgcagg";

        BioBrickaAssembler assembler = new BioBrickaAssembler();

        try {
            System.out.println("###");
            System.out.println(assembler.concatSequence(part1Sequence, part2Sequence));
            System.out.println("###");
            System.out.println(assembler.concatSequence(part2Sequence, part1Sequence));
        } catch (UtilityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        */

    }

}

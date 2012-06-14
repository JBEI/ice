package org.jbei.ice.lib.utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.biojava.bio.molbio.RestrictionEnzyme;
import org.biojava.bio.molbio.RestrictionMapper;
import org.biojava.bio.seq.DNATools;
import org.biojava.bio.symbol.IllegalSymbolException;
import org.biojava.utils.SimpleThreadPool;
import org.jbei.ice.bio.enzymes.RestrictionEnzymesManager;
import org.jbei.ice.bio.enzymes.RestrictionEnzymesManagerException;
import org.jbei.ice.controllers.SequenceController;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.account.AccountController;
import org.jbei.ice.lib.entry.EntryController;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.managers.SequenceManager;
import org.jbei.ice.lib.models.AnnotationLocation;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.models.Feature;
import org.jbei.ice.lib.models.Name;
import org.jbei.ice.lib.models.Part;
import org.jbei.ice.lib.models.Part.AssemblyStandard;
import org.jbei.ice.lib.models.Sequence;
import org.jbei.ice.lib.models.SequenceFeature;
import org.jbei.ice.lib.models.SequenceFeature.AnnotationType;
import org.jbei.ice.lib.permissions.PermissionException;

/**
 * Biobrick (BBF RFC10, RFC20) assembly standard.
 * 
 * @author Timothy Ham, Zinovii Dmytriv
 * 
 */
public class BiobrickAUtils implements IAssemblyUtils {

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
    public static final int biobrickAPrefixChopPosition = 16; // zero based
    public static final String biobrickAPrefixFeatureName = "Biobrick A Long Prefix";
    public static final String biobrickAPrefix2 = "gaattcgcggccgcttctag"; // RFC20
    public static final int biobrickAPrefix2ChopPosition = 16;
    public static final String biobrickAPrefix2FeatureName = "Biobrick A Short Prefix";
    public static final String biobrickASuffix = "tactagtagcggccgctgcag"; // RFC10
    public static final int biobrickASuffixMaximum1stFeaturePosition = -15; // position from end
    public static final int biobrickASuffixChopPosition = 19;
    public static final String biobrickASuffixFeatureName = "Biobrick A Short Suffix";
    public static final String biobrickASuffix2 = "tactagtagcggccgcctgcagg"; // RFC20
    public static final int biobrickASuffix2Maximum1stFeaturePosition = -17; // position from end
    public static final int biobrickASuffix2ChopPosition = 21;
    public static final String biobrickASuffix2FeatureName = "Biobrick A Long Suffix";
    public static final String biobrickAScarShort = "tactag";
    public static final String biobrickAScarShortFeatureName = "Biobrick A Short Scar";
    public static final String biobrickAScarLong = "tactagag";
    public static final String biobrickAScarLongFeatureName = "Biobrick A Long Scar";

    @Override
    public AssemblyStandard determineAssemblyStandard(String partSequenceString)
            throws UtilityException {
        AssemblyStandard result = null;
        result = determineBiobrickAAssemblyStandard(partSequenceString);
        return result;
    }

    @Override
    public SequenceFeatureCollection determineAssemblyFeatures(Sequence partSequence)
            throws UtilityException {
        return determineBiobrickAFeatures(partSequence);
    }

    @Override
    public Sequence populateAssemblyFeatures(Sequence partSequence) throws UtilityException {
        SequenceFeatureCollection newSequenceFeatures = determineAssemblyFeatures(partSequence);
        Set<SequenceFeature> temp = partSequence.getSequenceFeatures();

        if (!(temp instanceof SequenceFeatureCollection)) {
            throw new UtilityException("Not A SequenceFeatureCollection");
        }
        SequenceFeatureCollection oldSequenceFeatures = (SequenceFeatureCollection) temp;
        for (SequenceFeature newSequenceFeature : newSequenceFeatures) {
            List<SequenceFeature> foundOldSequenceFeatures = oldSequenceFeatures
                    .getBySequence(newSequenceFeature.getFeature().getSequence());
            if (foundOldSequenceFeatures.size() > 1) {
                // multiple with same sequence. Remove them and replace with new feature
                oldSequenceFeatures.removeAll(foundOldSequenceFeatures);
                oldSequenceFeatures.add(newSequenceFeature);
            } else if (foundOldSequenceFeatures.size() == 1) {
                SequenceFeature foundOldSequenceFeature = foundOldSequenceFeatures.get(0);
                if (!(foundOldSequenceFeature.getAnnotationType() == newSequenceFeature
                        .getAnnotationType())) {
                    oldSequenceFeatures.remove(foundOldSequenceFeature);
                    oldSequenceFeatures.add(newSequenceFeature);
                }
            } else if (foundOldSequenceFeatures.size() == 0) {
                // maybe there is a part labeled INNER already that needs to be replaced
                List<SequenceFeature> foundOldInnerSequenceFeatures = oldSequenceFeatures
                        .get(AnnotationType.INNER);
                if (foundOldInnerSequenceFeatures.size() > 1) {
                    oldSequenceFeatures.removeAll(foundOldInnerSequenceFeatures);
                } else if (foundOldInnerSequenceFeatures.size() == 1) {
                    // inner exists, but is no longer valid
                    oldSequenceFeatures.remove(foundOldInnerSequenceFeatures.get(0));
                }
                oldSequenceFeatures.add(newSequenceFeature);
            }
        }
        try {
            SequenceManager.saveSequence(partSequence);
        } catch (ManagerException e) {
            throw new UtilityException(e);
        }
        return partSequence;
    }

    @Override
    public Sequence join(Sequence part1, Sequence part2) throws UtilityException {
        return joinBiobrickA(part1, part2);
    }

    @Override
    public int compareAssemblyAnnotations(SequenceFeatureCollection sequenceFeatures1,
            SequenceFeatureCollection sequenceFeatures2) {
        int result = 1;
        if (sequenceFeatures1 == null || sequenceFeatures2 == null) {
            return result;
        }
        SequenceFeature inner1 = null;
        SequenceFeature prefix1 = null;
        SequenceFeature suffix1 = null;
        SequenceFeature inner2 = null;
        SequenceFeature prefix2 = null;
        SequenceFeature suffix2 = null;
        List<SequenceFeature> temp = sequenceFeatures1.get(SequenceFeature.AnnotationType.INNER);
        if (temp.size() == 1) {
            inner1 = temp.get(0);
        }
        temp = sequenceFeatures1.get(SequenceFeature.AnnotationType.PREFIX);
        if (temp.size() == 1) {
            prefix1 = temp.get(0);
        }
        temp = sequenceFeatures1.get(SequenceFeature.AnnotationType.SUFFIX);
        if (temp.size() == 1) {
            suffix1 = temp.get(0);
        }
        temp = sequenceFeatures2.get(SequenceFeature.AnnotationType.INNER);
        if (temp.size() == 1) {
            inner2 = temp.get(0);
        }
        temp = sequenceFeatures2.get(SequenceFeature.AnnotationType.PREFIX);
        if (temp.size() == 1) {
            prefix2 = temp.get(0);
        }
        temp = sequenceFeatures2.get(SequenceFeature.AnnotationType.SUFFIX);
        if (temp.size() == 1) {
            suffix2 = temp.get(0);
        }
        int counter = 0;
        if (inner1 != null && inner2 != null) {
            if (inner1.getSequence().getFwdHash().equals(inner2.getSequence().getFwdHash())) {
                counter = counter + 1;
            }
        }
        if (prefix1 != null && prefix2 != null) {
            if (prefix1.getSequence().getFwdHash().equals(prefix2.getSequence().getFwdHash())) {
                counter = counter + 1;
            }
        }
        if (suffix1 != null && suffix2 != null) {
            if (suffix1.getSequence().getFwdHash().equals(suffix2.getSequence().getFwdHash())) {
                counter = counter + 1;
            }
        }
        if (counter == 3) {
            result = 0;
        }
        return result;
    }

    /**
     * Determine whether the given sequence string conforms to the proper Biobrick assembly format.
     * <p>
     * It checks for prefix, suffix, and scans for incomaptible restriction sites.
     * 
     * @param partSequenceString
     *            sequence string to test.
     * @return {@link AssemblyStandard}.
     * @throws UtilityException
     */
    private static AssemblyStandard determineBiobrickAAssemblyStandard(String partSequenceString)
            throws UtilityException {
        AssemblyStandard result = null;
        if (!isBiobrickACompatible(partSequenceString)) {
            return result;
        }
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

    /**
     * Check if the given sequence string can be assembled as a biobrick by having the correct
     * restriction sites.
     * 
     * @param sequenceString
     *            query string.
     * @return True if the given sequence is biobrick compatible.
     * @throws UtilityException
     */
    private static boolean isBiobrickACompatible(String sequenceString) throws UtilityException {
        boolean result = false;
        org.biojava.bio.seq.Sequence sequence = null;
        try {
            sequence = DNATools.createDNASequence(sequenceString, "temp");
        } catch (IllegalSymbolException e) {
            throw new UtilityException(e);
        }
        RestrictionMapper restrictionMapper = new RestrictionMapper(new SimpleThreadPool());

        ArrayList<RestrictionEnzyme> restrictionEnzymes = new ArrayList<RestrictionEnzyme>();
        RestrictionEnzymesManager restrictionEnzymeManager = null;
        try {
            restrictionEnzymeManager = RestrictionEnzymesManager.getInstance();
        } catch (RestrictionEnzymesManagerException e) {
            throw new UtilityException(e);
        }

        restrictionEnzymes.add(restrictionEnzymeManager.getBioJavaEnzyme("EcoRI"));
        restrictionEnzymes.add(restrictionEnzymeManager.getBioJavaEnzyme("XbaI"));
        restrictionEnzymes.add(restrictionEnzymeManager.getBioJavaEnzyme("SpeI"));
        restrictionEnzymes.add(restrictionEnzymeManager.getBioJavaEnzyme("PstI"));

        int counter = 0;
        for (RestrictionEnzyme restrictionEnzyme : restrictionEnzymes) {
            restrictionMapper.clearEnzymes();
            restrictionMapper.addEnzyme(restrictionEnzyme);
            org.biojava.bio.seq.Sequence annotatedSequence = restrictionMapper.annotate(sequence);
            if (annotatedSequence.countFeatures() == 1) {
                counter = counter + 1;
            }
        }
        if (counter == 4) {
            result = true;
        }
        return result;
    }

    /**
     * Annotate the given sequence string with biobrick format features, such as the prefix, suffix,
     * and the inner feature.
     * 
     * @param partSequence
     *            sequence to query.
     * @return {@link SequenceFeatureCollection}.
     * @throws UtilityException
     */
    private static SequenceFeatureCollection determineBiobrickAFeatures(Sequence partSequence)
            throws UtilityException {
        //all positions are 0 based positions, not offsets
        String partSequenceString = partSequence.getSequence();
        int partSequenceLength = partSequenceString.length();
        SequenceFeatureCollection sequenceFeatures = new SequenceFeatureCollection();
        if (partSequenceString.startsWith(biobrickAPrefix)) {
            Feature feature = new Feature(biobrickAPrefixFeatureName, "", biobrickAPrefix, 1,
                    "misc_feature");
            try {
                feature = SequenceManager.getReferenceFeature(feature);
            } catch (ControllerException e) {
                throw new UtilityException(e);
            }
            SequenceFeature sequenceFeature = new SequenceFeature(partSequence, feature, +1,
                    feature.getName(), feature.getGenbankType(),
                    SequenceFeature.AnnotationType.PREFIX);
            sequenceFeature.getAnnotationLocations().add(
                new AnnotationLocation(1, 22, sequenceFeature));
            sequenceFeatures.add(sequenceFeature);
        } else if (partSequenceString.startsWith(biobrickAPrefix2)) {
            Feature feature = new Feature(biobrickAPrefix2FeatureName, "", biobrickAPrefix2, 1,
                    "misc_feature");
            try {
                feature = SequenceManager.getReferenceFeature(feature);
            } catch (ControllerException e) {
                throw new UtilityException(e);
            }
            SequenceFeature sequenceFeature = new SequenceFeature(partSequence, feature, +1,
                    feature.getName(), feature.getGenbankType(),
                    SequenceFeature.AnnotationType.PREFIX);
            sequenceFeature.getAnnotationLocations().add(
                new AnnotationLocation(1, 20, sequenceFeature));
            sequenceFeatures.add(sequenceFeature);
        }
        if (partSequenceString.endsWith(biobrickASuffix)) {
            Feature feature = new Feature(biobrickASuffixFeatureName, "", biobrickASuffix, 1,
                    "misc_feature");
            try {
                feature = SequenceManager.getReferenceFeature(feature);
            } catch (ControllerException e) {
                throw new UtilityException(e);
            }
            SequenceFeature sequenceFeature = new SequenceFeature(partSequence, feature, +1,
                    feature.getName(), feature.getGenbankType(),
                    SequenceFeature.AnnotationType.SUFFIX);
            sequenceFeature.getAnnotationLocations()
                    .add(
                        new AnnotationLocation(partSequenceLength - 20, partSequenceLength,
                                sequenceFeature));
            sequenceFeatures.add(sequenceFeature);
        } else if (partSequenceString.endsWith(biobrickASuffix2)) {
            Feature feature = new Feature(biobrickASuffix2FeatureName, "", biobrickASuffix2, 1,
                    "misc_feature");
            try {
                feature = SequenceManager.getReferenceFeature(feature);
            } catch (ControllerException e) {
                throw new UtilityException(e);
            }
            SequenceFeature sequenceFeature = new SequenceFeature(partSequence, feature, +1,
                    feature.getName(), feature.getGenbankType(),
                    SequenceFeature.AnnotationType.SUFFIX);
            sequenceFeature.getAnnotationLocations()
                    .add(
                        new AnnotationLocation(partSequenceLength - 22, partSequenceLength,
                                sequenceFeature));
            sequenceFeatures.add(sequenceFeature);
        }
        // determine inner feature
        //
        int absoluteMinimumFeatureStart = 0;
        int absoluteMaximumFeatureEnd = partSequenceLength;
        int minimumFeatureStart = 0;
        int maximumFeatureEnd = 0;
        if (partSequenceString.startsWith(biobrickAPrefix)) {
            absoluteMinimumFeatureStart = biobrickAPrefixChopPosition;
            minimumFeatureStart = absoluteMinimumFeatureStart + 6;
        } else if (partSequenceString.startsWith(biobrickAPrefix2)) {
            absoluteMinimumFeatureStart = biobrickAPrefix2ChopPosition;
            minimumFeatureStart = absoluteMinimumFeatureStart + 4;
        } else {
            throw new UtilityException("Unknown prefix for second part");
        }
        if (partSequenceString.endsWith(biobrickASuffix)) {
            absoluteMaximumFeatureEnd = partSequenceLength
                    + biobrickASuffixMaximum1stFeaturePosition - 1;
            maximumFeatureEnd = partSequenceLength - 21 - 1;
            maximumFeatureEnd = absoluteMaximumFeatureEnd - 6;
        } else if (partSequenceString.endsWith(biobrickASuffix2)) {
            absoluteMaximumFeatureEnd = partSequenceLength
                    + biobrickASuffix2Maximum1stFeaturePosition - 1;
            maximumFeatureEnd = absoluteMaximumFeatureEnd - 6;
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
                int start = sequenceFeature.getUniqueGenbankStart() - 1;
                int end = sequenceFeature.getUniqueEnd() - 1;
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
        String featureIdentification = part.getRecordId();
        Feature innerPartFeature = new Feature(featureName, featureIdentification,
                partSequenceString.substring(minimumFeatureStart, maximumFeatureEnd + 1), 0,
                "misc_feature");
        SequenceFeature sequenceFeature = new SequenceFeature(partSequence, innerPartFeature, +1,
                innerPartFeature.getName(), innerPartFeature.getGenbankType(),
                SequenceFeature.AnnotationType.INNER);
        sequenceFeature.getAnnotationLocations()
                .add(
                    new AnnotationLocation(minimumFeatureStart + 1, maximumFeatureEnd + 1,
                            sequenceFeature));
        sequenceFeatures.add(sequenceFeature);
        // check if part has at least prefix, suffix, and one inner feature
        //
        int temp = 0;
        List<SequenceFeature> prefixFeature = sequenceFeatures
                .get(SequenceFeature.AnnotationType.PREFIX);
        List<SequenceFeature> suffixFeature = sequenceFeatures
                .get(SequenceFeature.AnnotationType.SUFFIX);
        List<SequenceFeature> innerFeature = sequenceFeatures
                .get(SequenceFeature.AnnotationType.INNER);
        if (prefixFeature.size() == 1) {
            if (prefixFeature.get(0).getUniqueGenbankStart() == 1) {
                temp = temp + 1;
            }
        }
        if (suffixFeature.size() == 1) {
            if (suffixFeature.get(0).getUniqueEnd() == partSequenceLength) {
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

    /**
     * Assemble two biobrick sequences together.
     * 
     * @param part1Sequence
     *            first sequence
     * @param part2Sequence
     *            second sequence
     * @return Assembled {@link Sequence}.
     * @throws UtilityException
     */
    public Sequence joinBiobrickA(Sequence part1Sequence, Sequence part2Sequence)
            throws UtilityException {
        Sequence result = null;
        Part part1 = (Part) part1Sequence.getEntry();
        Part part2 = (Part) part2Sequence.getEntry();

        if (part1.getPackageFormat().equals(Part.AssemblyStandard.BIOBRICKA)
                && part2.getPackageFormat().equals(Part.AssemblyStandard.BIOBRICKA)) {
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
            int prefixChopPosition = 0;
            int suffixChopPosition = 0;
            int scarLength = 6;
            int scarStartPosition = -1;
            if (part2SequenceString.startsWith(biobrickAPrefix)) {
                prefixChopPosition = biobrickAPrefixChopPosition;
                scarLength = 8;
            } else if (part2SequenceString.startsWith(biobrickAPrefix2)) {
                prefixChopPosition = biobrickAPrefix2ChopPosition;
            } else {
                throw new UtilityException("Unknown prefix for second part");
            }
            if (part1SequenceString.endsWith(biobrickASuffix)) {
                suffixChopPosition = part1SequenceString.length() - biobrickASuffixChopPosition;
                scarStartPosition = suffixChopPosition - 2;
            } else if (part1SequenceString.endsWith(biobrickASuffix2)) {
                suffixChopPosition = part1SequenceString.length() - biobrickASuffix2ChopPosition;
                scarStartPosition = suffixChopPosition - 2;
            } else {
                throw new UtilityException("Unknown suffix for first part");
            }
            String firstPart = part1SequenceString.substring(0, suffixChopPosition);
            String secondPart = part2SequenceString.substring(prefixChopPosition);
            //            concatSequence = part1SequenceString.substring(0, part1SequenceString.length()
            //                    suffixOffset);
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
            newPart.setPackageFormat(part1.getPackageFormat());
            newPart.setShortDescription("Assembly of " + newPartNameString);
            newPart.setStatus("in progress");
            newPart.setBioSafetyLevel(Math.max(part1.getBioSafetyLevel(), part2.getBioSafetyLevel()));
            newPart.setOwner("System");
            newPart.setOwnerEmail("System");
            newPart.setCreator("System");
            newPart.setCreatorEmail("System");
            EntryController entryController = null;
            AccountController controller = new AccountController();

            try {
                entryController = new EntryController();
                newPart = (Part) entryController.createEntry(newPart);
            } catch (ControllerException e) {
                throw new UtilityException(e);
            }
            SequenceController sequenceController = null;
            Sequence newPartSequence = new Sequence();
            newPartSequence.setEntry(newPart);
            newPartSequence.setSequence(joinedSequence);
            newPartSequence.setFwdHash(SequenceUtils.calculateSequenceHash(joinedSequence));
            newPartSequence.setRevHash(SequenceUtils
                    .calculateReverseComplementSequenceHash(joinedSequence));

            Set<SequenceFeature> newPartSequenceFeatures = newPartSequence.getSequenceFeatures();
            SequenceFeatureCollection newFeatures = new SequenceFeatureCollection();
            // annotate subinner features and scar
            //
            SequenceFeatureCollection part1SequenceFeatures = (SequenceFeatureCollection) part1Sequence
                    .getSequenceFeatures();
            SequenceFeature part1InnerFeature = part1SequenceFeatures.get(
                SequenceFeature.AnnotationType.INNER).get(0);
            SequenceFeatureCollection part2SequenceFeatures = (SequenceFeatureCollection) part2Sequence
                    .getSequenceFeatures();
            SequenceFeature part2InnerFeature = part2SequenceFeatures.get(
                SequenceFeature.AnnotationType.INNER).get(0);
            // part1 inner feature as subinner feature
            SequenceFeature temp = new SequenceFeature();
            temp.setSequence(newPartSequence);
            temp.setFeature(part1InnerFeature.getFeature());
            temp.setName(part1InnerFeature.getName());
            temp.setAnnotationType(SequenceFeature.AnnotationType.SUBINNER);
            temp.setGenbankType("misc_feature");
            temp.getAnnotationLocations().add(
                new AnnotationLocation(part1InnerFeature.getUniqueGenbankStart(), part1InnerFeature
                        .getUniqueEnd(), temp));
            temp.setStrand(part1InnerFeature.getStrand());
            newFeatures.add(temp);
            // part 2 inner feature as subinner feature
            temp = new SequenceFeature();
            temp.setSequence(newPartSequence);
            temp.setFeature(part2InnerFeature.getFeature());
            temp.setName(part2InnerFeature.getName());
            temp.setAnnotationType(SequenceFeature.AnnotationType.SUBINNER);
            temp.setGenbankType("misc_feature");
            int secondPartFeatureOffset = scarStartPosition - prefixChopPosition + 2;
            temp.getAnnotationLocations().add(
                new AnnotationLocation(part2InnerFeature.getUniqueGenbankStart()
                        + secondPartFeatureOffset, part2InnerFeature.getUniqueEnd()
                        + secondPartFeatureOffset, temp));
            temp.setStrand(part2InnerFeature.getStrand());
            newFeatures.add(temp);
            // scar
            temp = new SequenceFeature();
            temp.setSequence(newPartSequence);
            temp.setAnnotationType(SequenceFeature.AnnotationType.SCAR);
            temp.setGenbankType("misc_feature");
            temp.getAnnotationLocations()
                    .add(
                        new AnnotationLocation(scarStartPosition + 1, scarStartPosition
                                + scarLength, temp));
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
                sequenceController = new SequenceController(controller.getSystemAccount());
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

    /**
     * Retrieve biobrick short scar {@link Feature}.
     * 
     * @return {@link Feature}.
     * @throws ControllerException
     */
    private static Feature getBiobrickAShortScarFeature() throws ControllerException {
        Feature feature = new Feature();
        feature.setAutoFind(1);
        feature.setName(biobrickAScarShortFeatureName);
        feature.setGenbankType("misc_feature");
        feature.setSequence(biobrickAScarShort);
        feature.setHash(SequenceUtils.calculateSequenceHash(biobrickAScarShort));
        feature = SequenceManager.getReferenceFeature(feature);
        return feature;
    }

    /**
     * Retrieve biobrick long scar {@link Feature}.
     * 
     * @return {@link Feature}.
     * @throws ControllerException
     */
    private static Feature getBiobrickALongScarFeature() throws ControllerException {
        Feature feature = new Feature();
        feature.setAutoFind(1);
        feature.setName(biobrickAScarLongFeatureName);
        feature.setGenbankType("misc_feature");
        feature.setSequence(biobrickAScarLong);
        feature.setHash(SequenceUtils.calculateSequenceHash(biobrickAScarLong));
        feature = SequenceManager.getReferenceFeature(feature);
        return feature;
    }

    public static void main(String[] args) {

        String part1Sequence = "gaattcgcggccgcttctagag--gaattc--tactagtagcggccgctgcag";
        String part2Sequence = "gaattcgcggccgcttctag--tactagtagcggccgcctgcagg";

        try {
            System.out.println("" + isBiobrickACompatible(part1Sequence));
            System.out.println("" + isBiobrickACompatible(part2Sequence));
        } catch (UtilityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        /*
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

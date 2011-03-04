package org.jbei.ice.lib.utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.biojava.bio.molbio.RestrictionEnzyme;
import org.biojava.bio.molbio.RestrictionMapper;
import org.biojava.bio.seq.DNATools;
import org.biojava.bio.symbol.IllegalSymbolException;
import org.biojava.utils.SimpleThreadPool;
import org.jbei.ice.bio.enzymes.RestrictionEnzymesManager;
import org.jbei.ice.bio.enzymes.RestrictionEnzymesManagerException;
import org.jbei.ice.controllers.AccountController;
import org.jbei.ice.controllers.EntryController;
import org.jbei.ice.controllers.SequenceController;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.managers.SequenceManager;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.models.Feature;
import org.jbei.ice.lib.models.Name;
import org.jbei.ice.lib.models.Part;
import org.jbei.ice.lib.models.Sequence;
import org.jbei.ice.lib.models.SequenceFeature;
import org.jbei.ice.lib.models.Part.AssemblyStandard;
import org.jbei.ice.lib.models.SequenceFeature.AnnotationType;
import org.jbei.ice.lib.permissions.PermissionException;

public class BiobrickBUtils implements AssemblyUtils {

    public static final String biobrickBPrefix = "gaattc\\w*agatct";
    public static final String biobrickBPrefixFeatureName = "Biobrick B Prefix";
    public static final String biobrickBSuffix = "ggatcc\\w*ctcgag";
    public static final String biobrickBSuffixFeatureName = "Biobrick B Suffix";
    public static final String biobrickBScar = "ggatct";
    public static final String biobrickBScarFeatureName = "Biobrick B Scar";

    public AssemblyStandard determineAssemblyStandard(String partSequenceString)
            throws UtilityException {
        AssemblyStandard result = null;
        result = determineBiobrickBAssemblyStandard(partSequenceString);
        return result;
    }

    public SequenceFeatureCollection determineAssemblyFeatures(Sequence partSequence)
            throws UtilityException {
        return determineBiobrickBFeatures(partSequence);
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
                // maybe there is a part labeled INNER already
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

    public Sequence join(Sequence part1, Sequence part2) throws UtilityException {
        return joinBiobrickB(part1, part2);
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

    private static AssemblyStandard determineBiobrickBAssemblyStandard(String partSequenceString)
            throws UtilityException {
        AssemblyStandard result = null;
        if (!isBiobrickBCompatible(partSequenceString)) {
            return result;
        }
        try {
            if ((findBiobrickBPrefix(partSequenceString) != null)
                    && (findBiobrickBSuffix(partSequenceString) != null)) {
                result = AssemblyStandard.BIOBRICKB;
            }
        } catch (ControllerException e) {
            // don't worry.
        }
        return result;
    }

    private static boolean isBiobrickBCompatible(String sequenceString) throws UtilityException {
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
        restrictionEnzymes.add(restrictionEnzymeManager.getBioJavaEnzyme("BglII"));
        restrictionEnzymes.add(restrictionEnzymeManager.getBioJavaEnzyme("BamHI"));
        restrictionEnzymes.add(restrictionEnzymeManager.getBioJavaEnzyme("XhoI"));
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

    private static SimpleFeature findBiobrickBPrefix(String sequenceString)
            throws ControllerException {
        SimpleFeature result = null;
        Pattern pattern = Pattern.compile(biobrickBPrefix);
        Matcher matcher = pattern.matcher(sequenceString);
        ArrayList<String> prefixMatches = new ArrayList<String>();
        ArrayList<Integer> prefixStarts = new ArrayList<Integer>();
        ArrayList<Integer> prefixEnds = new ArrayList<Integer>();
        while (matcher.find()) {
            prefixMatches.add(matcher.group());
            prefixStarts.add(matcher.start());
            prefixEnds.add(matcher.end() - 1);
        }
        if (prefixMatches.size() > 1) {
            throw new ControllerException("Multiple prefixes found");
        } else if (prefixMatches.size() == 1) {
            result = new SimpleFeature(prefixMatches.get(0), prefixStarts.get(0), prefixEnds.get(0));
        }
        return result;
    }

    private static SimpleFeature findBiobrickBSuffix(String sequenceString)
            throws ControllerException {
        SimpleFeature result = null;
        Pattern pattern = Pattern.compile(biobrickBSuffix);
        Matcher matcher = pattern.matcher(sequenceString);
        ArrayList<String> prefixMatches = new ArrayList<String>();
        ArrayList<Integer> prefixStarts = new ArrayList<Integer>();
        ArrayList<Integer> prefixEnds = new ArrayList<Integer>();
        while (matcher.find()) {
            prefixMatches.add(matcher.group());
            prefixStarts.add(matcher.start());
            prefixEnds.add(matcher.end() - 1);
        }
        if (prefixMatches.size() > 1) {
            throw new ControllerException("Multiple suffix found");
        } else if (prefixMatches.size() == 1) {
            result = new SimpleFeature(prefixMatches.get(0), prefixStarts.get(0), prefixEnds.get(0));
        }
        return result;
    }

    private static SequenceFeatureCollection determineBiobrickBFeatures(Sequence partSequence)
            throws UtilityException {
        // all positions are 0 based positions, not offsets

        SequenceFeatureCollection sequenceFeatures = new SequenceFeatureCollection();

        try {

            String partSequenceString = partSequence.getSequence();
            int partSequenceLength = partSequenceString.length();
            // prefix
            SimpleFeature prefixMatch = findBiobrickBPrefix(partSequenceString);
            if (prefixMatch != null) {
                Feature feature = new Feature(biobrickBPrefixFeatureName,
                        biobrickBPrefixFeatureName, "", prefixMatch.getSequence(), 0,
                        "misc_feature");
                SequenceFeature sequenceFeature = new SequenceFeature(partSequence, feature,
                        prefixMatch.getStart() + 1, prefixMatch.getEnd() + 1, +1,
                        feature.getName(), feature.getDescription(), feature.getGenbankType(),
                        SequenceFeature.AnnotationType.PREFIX);

                sequenceFeatures.add(sequenceFeature);
            }
            // suffix
            SimpleFeature suffixMatch = findBiobrickBSuffix(partSequenceString);
            if (suffixMatch != null) {
                Feature feature = new Feature(biobrickBSuffixFeatureName,
                        biobrickBSuffixFeatureName, "", suffixMatch.getSequence(), 0,
                        "misc_feature");
                SequenceFeature sequenceFeature = new SequenceFeature(partSequence, feature,
                        suffixMatch.getStart() + 1, suffixMatch.getEnd() + 1, +1,
                        feature.getName(), feature.getDescription(), feature.getGenbankType(),
                        SequenceFeature.AnnotationType.SUFFIX);

                sequenceFeatures.add(sequenceFeature);
            }

            // determine inner feature. See BBa inner feature loop
            // 
            int absoluteMinimumFeatureStart = prefixMatch.getEnd() - 4;
            int absoluteMaximumFeatureEnd = suffixMatch.getStart() + 4;
            int minimumFeatureStart = prefixMatch.getEnd() + 1;
            int maximumFeatureEnd = suffixMatch.getStart() - 1;
            Set<SequenceFeature> existingSequenceFeatures = partSequence.getSequenceFeatures();
            for (SequenceFeature sequenceFeature : existingSequenceFeatures) {
                int start = sequenceFeature.getGenbankStart() - 1;
                int end = sequenceFeature.getEnd() - 1;
                if ((start < minimumFeatureStart) && (start > absoluteMinimumFeatureStart)) {
                    minimumFeatureStart = start;
                }
                if ((end > maximumFeatureEnd) && (end < absoluteMaximumFeatureEnd)) {
                    maximumFeatureEnd = end;
                }
            }
            Entry part = partSequence.getEntry();
            String featureName = part.getRecordId(); // uuid of the given part
            String featureDescription = featureName;
            String featureIdentification = part.getRecordId();
            Feature innerPartFeature = new Feature(featureName, featureDescription,
                    featureIdentification, partSequenceString.substring(minimumFeatureStart,
                        maximumFeatureEnd + 1), 0, "misc_feature");
            SequenceFeature sequenceFeature = new SequenceFeature(partSequence, innerPartFeature,
                    minimumFeatureStart + 1, maximumFeatureEnd + 1, +1, innerPartFeature.getName(),
                    innerPartFeature.getDescription(), innerPartFeature.getGenbankType(),
                    SequenceFeature.AnnotationType.INNER);

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
                if (prefixFeature.get(0).getGenbankStart() == 1) {
                    temp = temp + 1;
                }
            }
            if (suffixFeature.size() == 1) {
                if (suffixFeature.get(0).getEnd() == partSequenceLength)
                    temp = temp + 1;
            }
            if (innerFeature.size() == 1) {
                temp = temp + 1;
            }
            if (temp != 3) {
                throw new UtilityException("Could not determine prefix, suffix, or inner feature");
            }
        } catch (ControllerException e) {
            throw new UtilityException(e);
        }
        return sequenceFeatures;
    }

    private Sequence joinBiobrickB(Sequence part1Sequence, Sequence part2Sequence)
            throws UtilityException {
        Sequence result = null;
        Part part1 = (Part) part1Sequence.getEntry();
        Part part2 = (Part) part2Sequence.getEntry();
        if (part1.getPackageFormat().equals(Part.AssemblyStandard.BIOBRICKB)
                && part2.getPackageFormat().equals(Part.AssemblyStandard.BIOBRICKB)) {

            // concat sequence string
            String joinedSequence = null;
            String part1SequenceString = part1Sequence.getSequence();
            String part2SequenceString = part2Sequence.getSequence();
            int prefixChopPosition = 0;
            int suffixChopPosition = 0;
            int scarLength = 6;
            int scarStartPosition = -1;

            try {
                prefixChopPosition = findBiobrickBPrefix(part2SequenceString).getEnd() - 4;
                suffixChopPosition = findBiobrickBSuffix(part1SequenceString).getStart() + 1;
            } catch (ControllerException e1) {
                throw new UtilityException(e1);
            }

            String firstPart = part1SequenceString.substring(0, suffixChopPosition);
            String secondPart = part2SequenceString.substring(prefixChopPosition);
            joinedSequence = firstPart + secondPart;
            scarStartPosition = suffixChopPosition - 1;
            // end concat sequence
            // create new part
            Part newPart = new Part();
            // setting part info
            HashSet<Name> inputNames = new HashSet<Name>();
            String newPartNameString = part1.getOnePartNumber().getPartNumber() + "+"
                    + part2.getOnePartNumber().getPartNumber();
            inputNames.add(new Name(newPartNameString, newPart));
            newPart.setPackageFormat(part1.getPackageFormat());
            newPart.setNames(inputNames);
            newPart.setShortDescription("Assembly of " + newPartNameString);
            newPart.setStatus("in progress");
            newPart.setBioSafetyLevel(Math
                    .max(part1.getBioSafetyLevel(), part2.getBioSafetyLevel()));
            newPart.setOwner("System");
            newPart.setOwnerEmail("System");
            newPart.setCreator("System");
            newPart.setCreatorEmail("System");
            EntryController entryController = null;
            try {
                entryController = new EntryController(AccountController.getSystemAccount());
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
            temp.setGenbankStart(part1InnerFeature.getGenbankStart());
            temp.setEnd(part1InnerFeature.getEnd());
            temp.setStrand(part1InnerFeature.getStrand());
            newFeatures.add(temp);
            // part 2 inner feature as subinner feature
            temp = new SequenceFeature();
            temp.setSequence(newPartSequence);
            temp.setFeature(part2InnerFeature.getFeature());
            temp.setName(part2InnerFeature.getName());
            temp.setAnnotationType(SequenceFeature.AnnotationType.SUBINNER);
            temp.setGenbankType("misc_feature");
            int secondPartFeatureOffset = scarStartPosition - prefixChopPosition + 1;
            temp.setGenbankStart(part2InnerFeature.getGenbankStart() + secondPartFeatureOffset);
            temp.setEnd(part2InnerFeature.getEnd() + secondPartFeatureOffset);
            temp.setStrand(part2InnerFeature.getStrand());

            newFeatures.add(temp);
            // scar
            temp = new SequenceFeature();
            temp.setSequence(newPartSequence);
            temp.setAnnotationType(SequenceFeature.AnnotationType.SCAR);
            temp.setGenbankType("misc_feature");
            temp.setGenbankStart(scarStartPosition + 1);
            temp.setEnd(scarStartPosition + scarLength);
            temp.setStrand(1);
            try {
                temp.setFeature(getBiobrickBScarFeature());
            } catch (ControllerException e) {
                throw new UtilityException(e);
            }
            temp.setName(biobrickBScarFeatureName);
            newFeatures.add(temp);
            newPartSequenceFeatures.addAll(newFeatures);
            try {
                sequenceController = new SequenceController(AccountController.getSystemAccount());
                newPartSequence = sequenceController.save(newPartSequence);
            } catch (ControllerException e) {
                throw new UtilityException(e);
            } catch (PermissionException e) {
                throw new UtilityException(e);
            }
            result = newPartSequence;
        }
        return result;
    }

    private static Feature getBiobrickBScarFeature() throws ControllerException {
        Feature feature = new Feature();
        feature.setAutoFind(1);
        feature.setDescription(biobrickBScarFeatureName);
        feature.setName(biobrickBScarFeatureName);
        feature.setGenbankType("misc_feature");
        feature.setSequence(biobrickBScar);
        feature.setHash(SequenceUtils.calculateSequenceHash(biobrickBScar));
        feature = SequenceManager.getReferenceFeature(feature);
        return feature;
    }

    public static void main(String[] args) {
        String part1Sequence = "gaattcaaaagatct--ggatccaaactcgag";
        try {
            System.out.println("" + isBiobrickBCompatible(part1Sequence));
        } catch (UtilityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}

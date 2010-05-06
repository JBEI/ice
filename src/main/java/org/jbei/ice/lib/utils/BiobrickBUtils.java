package org.jbei.ice.lib.utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

public class BiobrickBUtils implements AssemblyUtils {

    public static final String biobrickBPrefix = "gaattc\\w*agatct";
    public static final String biobrickBPrefixFeatureName = "Biobrick B Prefix";
    public static final String biobrickBSuffix = "ggatcc\\w*ctcgag";
    public static final String biobrickBSuffixFeatureName = "Biobrick B Suffix";
    public static final String biobrickBScar = "ggatct";
    public static final String biobrickBScarFeatureName = "Biobrick B Scar";

    public AssemblyStandard determineAssemblyStandard(String partSequenceString) {
        AssemblyStandard result = null;
        result = determineBiobrickBAssemblyStandard(partSequenceString);
        return result;
    }

    public SequenceFeatureCollection determineAssemblyFeatures(Sequence partSequence)
            throws UtilityException {
        return determineBiobrickBFeatures(partSequence);
    }

    public Sequence join(Part part1, Part part2) throws UtilityException {
        return joinBiobrickB(part1, part2);
    }

    private static AssemblyStandard determineBiobrickBAssemblyStandard(String partSequenceString) {
        AssemblyStandard result = null;
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
        } else {
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
        } else {
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
                        "MISC_FEATURE");
                SequenceFeature sequenceFeature = new SequenceFeature(partSequence, feature,
                        prefixMatch.getStart() + 1, prefixMatch.getEnd() + 1, +1,
                        feature.getName(), feature.getDescription(), feature.getGenbankType());
                sequenceFeature.setFlag(SequenceFeature.Flag.PREFIX);
                sequenceFeatures.add(sequenceFeature);
            }
            // suffix
            SimpleFeature suffixMatch = findBiobrickBSuffix(partSequenceString);
            if (suffixMatch != null) {
                Feature feature = new Feature(biobrickBSuffixFeatureName,
                        biobrickBSuffixFeatureName, "", suffixMatch.getSequence(), 0,
                        "MISC_FEATURE");
                SequenceFeature sequenceFeature = new SequenceFeature(partSequence, feature,
                        suffixMatch.getStart() + 1, suffixMatch.getEnd() + 1, +1,
                        feature.getName(), feature.getDescription(), feature.getGenbankType());
                sequenceFeature.setFlag(SequenceFeature.Flag.SUFFIX);
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
                int start = sequenceFeature.getStart() - 1;
                int end = sequenceFeature.getEnd() - 1;
                if ((start < minimumFeatureStart) && (start > absoluteMinimumFeatureStart)) {
                    minimumFeatureStart = start;
                }
                if ((end > maximumFeatureEnd) && (end < absoluteMaximumFeatureEnd)) {
                    maximumFeatureEnd = end;
                }
            }
            Entry part = partSequence.getEntry();
            String featureName = "inner." + part.getRecordId(); // uuid of the given part
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

    private Sequence joinBiobrickB(Part part1, Part part2) throws UtilityException {
        Sequence result = null;
        if (part1.getPackageFormat().equals(Part.AssemblyStandard.BIOBRICKB)
                && part2.getPackageFormat().equals(Part.AssemblyStandard.BIOBRICKB)) {
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

            try {
                prefixOffset = findBiobrickBPrefix(part2SequenceString).getEnd() - 4;
                suffixOffset = findBiobrickBSuffix(part1SequenceString).getStart() + 1;
            } catch (ControllerException e1) {
                throw new UtilityException(e1);
            }

            String firstPart = part1SequenceString.substring(0, suffixOffset);
            String secondPart = part2SequenceString.substring(prefixOffset);
            joinedSequence = firstPart + secondPart;
            scarStart = suffixOffset - 1;
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
            } catch (ControllerException e) {
                throw new UtilityException(e);
            } catch (PermissionException e) {
                throw new UtilityException(e);
            }
            // calculate and annotate biobrick sequencefeatures
            Set<SequenceFeature> newPartSequenceFeatures = newPartSequence.getSequenceFeatures();
            SequenceFeatureCollection newFeatures = determineBiobrickBFeatures(newPartSequence);
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
            temp.setSequence(newPartSequence);
            temp.setFeature(part2InnerFeature.getFeature());
            temp.setName(part2InnerFeature.getName());
            temp.setFlag(SequenceFeature.Flag.SUBINNER);
            // TODO: recalculate this
            //int secondPartFeatureOffset = 
            temp.setStart(part2InnerFeature.getStart() + scarStart - prefixOffset + 1);
            temp.setEnd(part2InnerFeature.getEnd() + scarStart - prefixOffset + 1);
            temp.setStrand(part2InnerFeature.getStrand());
            newFeatures.add(temp);
            // scar
            temp = new SequenceFeature();
            temp.setSequence(newPartSequence);
            temp.setFlag(SequenceFeature.Flag.SCAR);
            temp.setStart(scarStart + 1);
            temp.setEnd(scarStart + scarLength);
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
                newPartSequence = sequenceController.save(newPartSequence);
            } catch (ControllerException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (PermissionException e) {
                throw new UtilityException(e);
            }
            result = newPartSequence;
        }
        return result;
    }

    private Account getAccount() {
        // TODO Auto-generated method stub
        return null;
    }

    private static Feature getBiobrickBScarFeature() throws ControllerException {
        Feature feature = new Feature();
        feature.setAutoFind(1);
        feature.setDescription(biobrickBScarFeatureName);
        feature.setName(biobrickBScarFeatureName);
        feature.setGenbankType("MISC_FEATURE");
        feature.setSequence(biobrickBScar);
        feature.setHash(SequenceUtils.calculateSequenceHash(biobrickBScar));
        feature = SequenceManager.getReferenceFeature(feature);
        return feature;
    }

}

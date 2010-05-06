package org.jbei.ice.controllers;

import java.util.ArrayList;
import java.util.Set;

import org.jbei.ice.controllers.common.Controller;
import org.jbei.ice.controllers.permissionVerifiers.EntryPermissionVerifier;
import org.jbei.ice.lib.managers.AccountManager;
import org.jbei.ice.lib.managers.EntryManager;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.managers.SequenceManager;
import org.jbei.ice.lib.models.Account;
import org.jbei.ice.lib.models.Part;
import org.jbei.ice.lib.models.Sequence;
import org.jbei.ice.lib.models.SequenceFeature;
import org.jbei.ice.lib.models.Part.AssemblyStandard;
import org.jbei.ice.lib.permissions.PermissionException;
import org.jbei.ice.lib.utils.AssemblyUtils;
import org.jbei.ice.lib.utils.BiobrickAUtils;
import org.jbei.ice.lib.utils.BiobrickBUtils;
import org.jbei.ice.lib.utils.RawAssemblyUtils;
import org.jbei.ice.lib.utils.SequenceFeatureCollection;
import org.jbei.ice.lib.utils.UtilityException;

public class AssemblyController extends Controller {
    private ArrayList<AssemblyUtils> assemblyUtils = new ArrayList<AssemblyUtils>();

    public AssemblyController(Account account) {
        super(account, new EntryPermissionVerifier());
        getAssemblyUtils().add(new BiobrickAUtils());
        getAssemblyUtils().add(new BiobrickBUtils());
        getAssemblyUtils().add(new RawAssemblyUtils());
    }

    public AssemblyStandard determineAssemblyStandard(Sequence partSequence)
            throws UtilityException {
        AssemblyStandard result = null;
        String partSequenceString = partSequence.getSequence();
        result = determineAssemblyStandard(partSequenceString);
        return result;
    }

    public AssemblyStandard determineAssemblyStandard(String partSequenceString) {
        AssemblyStandard result = null;
        int counter = 0;
        while (result == null && counter < getAssemblyUtils().size()) {
            result = getAssemblyUtils().get(counter).determineAssemblyStandard(partSequenceString);
            counter = counter + 1;
        }
        return result;
    }

    public SequenceFeatureCollection determineAssemblyFeatures(Sequence partSequence)
            throws UtilityException {

        SequenceFeatureCollection sequenceFeatures = null;

        String partSequenceString = partSequence.getSequence();

        AssemblyStandard standard = determineAssemblyStandard(partSequenceString);
        if (standard == AssemblyStandard.BIOBRICKA) {
            sequenceFeatures = getAssemblyUtils().get(0).determineAssemblyFeatures(partSequence);
        } else if (standard == AssemblyStandard.BIOBRICKB) {
            sequenceFeatures = getAssemblyUtils().get(1).determineAssemblyFeatures(partSequence);
        } else if (standard == AssemblyStandard.RAW) {
            sequenceFeatures = getAssemblyUtils().get(2).determineAssemblyFeatures(partSequence);
        }

        return sequenceFeatures;
    }

    public static void main(String[] args) {

        try {
            mainRunBiobrickBTest();
        } catch (PermissionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        //mainRunJoin();

        /*
        InnerFeature feature = (InnerFeature) SequenceManager.getFeature(1347);
        System.out.println(feature.toString());
        */
    }

    private static void mainRunBiobrickBTest() throws PermissionException {
        AssemblyController as = null;
        try {
            as = new AssemblyController(AccountManager.get(86));
        } catch (ManagerException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        try {
            Part part2 = (Part) EntryManager.get(4394);
            Sequence part2Sequence = SequenceManager.getByEntry(part2);
            Set<SequenceFeature> sequenceFeatures = part2Sequence.getSequenceFeatures();
            try {
                ////Part result = as.joinBiobrickB(part2, part2);

                SequenceFeatureCollection temp = as.determineAssemblyFeatures(part2Sequence);
                //sequenceFeatures.addAll(temp);
                //SequenceManager.saveSequence(part2Sequence);

                System.out.println("===\n" + part2.getOnePartNumber().getPartNumber() + ": "
                        + part2Sequence.getSequence().length());
                for (SequenceFeature item : temp) {

                    System.out.println(item.getName() + ": " + item.getStart() + ":"
                            + item.getEnd());
                }

            } catch (UtilityException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } finally {

            }
            //sequenceFeatures.addAll(determineAssemblyFeatures(part2));
            // SequenceManager.saveSequence(part2Sequence);
        } catch (ManagerException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    private static void mainRunJoin() {
        try {
            Part part1 = (Part) EntryManager.get(4394);
            Sequence part1Sequence = SequenceManager.getByEntry(part1);
            Set<SequenceFeature> sequenceFeatures = part1Sequence.getSequenceFeatures();
            //sequenceFeatures.addAll(determineAssemblyFeatures(part1));
            //SequenceManager.saveSequence(part1Sequence);
            AssemblyController as = new AssemblyController(AccountManager.get(86));
            ////Part newPart = as.joinBiobrickA(part1, part1);

            System.out.println(sequenceFeatures.size());
        } catch (ManagerException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void setAssemblyUtils(ArrayList<AssemblyUtils> assemblyUtils) {
        this.assemblyUtils = assemblyUtils;
    }

    public ArrayList<AssemblyUtils> getAssemblyUtils() {
        return assemblyUtils;
    }

}

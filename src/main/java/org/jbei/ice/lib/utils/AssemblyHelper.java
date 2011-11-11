package org.jbei.ice.lib.utils;

import org.jbei.ice.lib.vo.AssemblyBin;
import org.jbei.ice.lib.vo.AssemblyItem;
import org.jbei.ice.lib.vo.AssemblyTable;
import org.jbei.ice.lib.vo.Permutation;
import org.jbei.ice.lib.vo.PermutationSet;

/**
 * Help Biobrick Permutation Assembly.
 * 
 * @author Timothy Ham
 * 
 */
@Deprecated
public class AssemblyHelper {
    private static AssemblyTable assemblyTable;
    private static PermutationSet permutationSet;

    public static PermutationSet buildPermutationSet(AssemblyTable assemblyTable) {
        if (assemblyTable == null || assemblyTable.getItems() == null
                || assemblyTable.getItems().size() == 0) {
            return null;
        }

        AssemblyHelper.permutationSet = new PermutationSet();
        AssemblyHelper.assemblyTable = assemblyTable;

        assemble(0, new Permutation());

        return permutationSet;
    }

    private static void assemble(int binIndex, Permutation permutation) {
        if (binIndex == assemblyTable.getItems().size()) {
            if (binIndex == 0) {
                return;
            } else {
                permutationSet.addPermutation(permutation);

                return;
            }
        }

        AssemblyBin currentBin = assemblyTable.getItems().get(binIndex);
        int binSize = currentBin.getItems().size();

        if (binSize > 0) {
            for (int i = 0; i < binSize; i++) {
                AssemblyItem assemblyItem = currentBin.getItems().get(i);

                Permutation newPermutation = permutation.clone();

                newPermutation.addAssemblyItem(assemblyItem);

                assemble(binIndex + 1, newPermutation);
            }
        } else {
            assemble(binIndex + 1, permutation);
        }
    }
}

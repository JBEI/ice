package org.jbei.ice.lib.vo;

import java.util.ArrayList;
import java.util.List;

public class PermutationSet {
    private List<Permutation> permutations = new ArrayList<Permutation>();

    public PermutationSet() {
        super();
    }

    public List<Permutation> getPermutations() {
        return permutations;
    }

    public void setPermutations(List<Permutation> value) {
        permutations = value;
    }

    public void addPermutation(Permutation permutation) {
        permutations.add(permutation);
    }
}
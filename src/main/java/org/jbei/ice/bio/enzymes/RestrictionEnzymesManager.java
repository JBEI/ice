package org.jbei.ice.bio.enzymes;

import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

import org.biojava.bio.BioException;
import org.biojava.bio.molbio.RestrictionEnzymeManager;

public class RestrictionEnzymesManager {
    private static final String REBASE_DATABASE_RESOURCE_FILE = "/org/jbei/ice/bio/enzymes/link_withrefm";

    public static RestrictionEnzymesManager instance = null;

    private HashMap<String, RestrictionEnzyme> enzymesMap = new HashMap<String, RestrictionEnzyme>();

    private RestrictionEnzymesManager() throws RestrictionEnzymesManagerException {
        loadEnzymes();
    }

    public static RestrictionEnzymesManager getInstance() throws RestrictionEnzymesManagerException {
        if (instance == null) {
            instance = new RestrictionEnzymesManager();
        }

        return instance;
    }

    public Collection<RestrictionEnzyme> getEnzymes() {
        return enzymesMap.values();
    }

    public RestrictionEnzyme getEnzymeByName(String enzymeName) {
        if (enzymeName == null || enzymeName.isEmpty()) {
            return null;
        }

        RestrictionEnzyme lookupEnzyme = null;

        if (enzymesMap.containsKey(enzymeName)) {
            lookupEnzyme = enzymesMap.get(enzymeName);
        }

        return lookupEnzyme;
    }

    public org.biojava.bio.molbio.RestrictionEnzyme getBioJavaEnzyme(String name) {
        return RestrictionEnzymeManager.getEnzyme(name);
    }

    @SuppressWarnings("unchecked")
    private void loadEnzymes() throws RestrictionEnzymesManagerException {
        InputStream is = RestrictionEnzymesManager.class
                .getResourceAsStream(REBASE_DATABASE_RESOURCE_FILE);

        try {
            RestrictionEnzymeManager.loadEnzymeFile(is, false);

            Set<org.biojava.bio.molbio.RestrictionEnzyme> reSet = RestrictionEnzymeManager
                    .getAllEnzymes();

            for (org.biojava.bio.molbio.RestrictionEnzyme re : reSet) {
                RestrictionEnzyme restrictionEnzyme = null;

                if (re.getCutType() == org.biojava.bio.molbio.RestrictionEnzyme.CUT_COMPOUND) {
                    restrictionEnzyme = new RestrictionEnzyme(re.getName(), re.getRecognitionSite()
                            .seqString(), re.getCutType(), re.getForwardRegex(), re
                            .getReverseRegex(), re.getDownstreamCut()[0], re.getDownstreamCut()[1],
                            re.getUpstreamCut()[0], re.getUpstreamCut()[1]);
                } else {
                    restrictionEnzyme = new RestrictionEnzyme(re.getName(), re.getRecognitionSite()
                            .seqString(), re.getCutType(), re.getForwardRegex(), re
                            .getReverseRegex(), re.getDownstreamCut()[0], re.getDownstreamCut()[1],
                            -1, -1);
                }

                enzymesMap.put(restrictionEnzyme.getName(), restrictionEnzyme);
            }
        } catch (BioException e) {
            throw new RestrictionEnzymesManagerException("Failed to cache restriction enzymes!", e);
        }
    }
}

package org.jbei.ice.bio.enzymes;

import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

import org.biojava.bio.BioException;
import org.biojava.bio.molbio.RestrictionEnzymeManager;

/**
 * Methods for dealing with {@link RestrictionEnzyme}s.
 * 
 * @author Zinovii Dmytriv
 * 
 */
public class RestrictionEnzymesManager {
    private static final String REBASE_DATABASE_RESOURCE_FILE = "/org/jbei/ice/bio/enzymes/link_withrefm";

    private static RestrictionEnzymesManager instance = null;

    private final HashMap<String, RestrictionEnzyme> enzymesMap = new HashMap<String, RestrictionEnzyme>();

    private RestrictionEnzymesManager() throws RestrictionEnzymesManagerException {
        loadEnzymes();
    }

    /**
     * Retrieve a singleton instance of RestrictionEnzymesManager.
     * 
     * @return RestrictionEnzymesManager instance.
     * @throws RestrictionEnzymesManagerException
     */
    public static RestrictionEnzymesManager getInstance() throws RestrictionEnzymesManagerException {
        if (instance == null) {
            instance = new RestrictionEnzymesManager();
        }

        return instance;
    }

    /**
     * Retrieve known {@link RestrictionEnzyme}s.
     * 
     * @return Collection of RestrictionEnzymes.
     */
    public Collection<RestrictionEnzyme> getEnzymes() {
        return enzymesMap.values();
    }

    /**
     * Retrieve {@link RestrictionEnzyme} by its name.
     * 
     * @param enzymeName
     *            Name of the enzyme to query.
     * @return RestrictionEnzyme. Null if not found.
     */
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

    /**
     * Retrieve biojava RestrictionEnzyme by name.
     * 
     * @param name
     *            Name of the enzyme to query.
     * @return biojava RestrictionEnzyme.
     */
    public org.biojava.bio.molbio.RestrictionEnzyme getBioJavaEnzyme(String name) {
        return RestrictionEnzymeManager.getEnzyme(name);
    }

    /**
     * Load enzymes into memory.
     * <p>
     * Read the REBASE database from file.
     * 
     * @throws RestrictionEnzymesManagerException
     */
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
                            .seqString(), re.getCutType(), re.getForwardRegex(),
                            re.getReverseRegex(), re.getDownstreamCut()[0],
                            re.getDownstreamCut()[1], re.getUpstreamCut()[0],
                            re.getUpstreamCut()[1]);
                } else {
                    restrictionEnzyme = new RestrictionEnzyme(re.getName(), re.getRecognitionSite()
                            .seqString(), re.getCutType(), re.getForwardRegex(),
                            re.getReverseRegex(), re.getDownstreamCut()[0],
                            re.getDownstreamCut()[1], -1, -1);
                }

                enzymesMap.put(restrictionEnzyme.getName(), restrictionEnzyme);
            }
        } catch (BioException e) {
            throw new RestrictionEnzymesManagerException("Failed to cache restriction enzymes!", e);
        }
    }
}

package org.jbei.ice.lib.dto.sample;

/**
 * // todo : this needs to be split into locationtype and sampletype to differentiate between the two
 * // with sample types being top level objects only
 *
 * @author Hector Plahar
 */
public enum SampleType {

    PLATE96(true),
    ADDGENE(true),
    GENERIC(true),
    FREEZER(false),
    SHELF(true),
    BOX_INDEXED(false),
    BOX_UNINDEXED(false),
    PLATE81(true),
    WELL(false),
    TUBE(false),
    SCHEME(false);

    private boolean topLevel;

    SampleType(boolean isTopLevel) {
        this.topLevel = isTopLevel;
    }

    public boolean isTopLevel() {
        return this.topLevel;
    }

    public static SampleType toSampleType(String name) {
        for (SampleType type : SampleType.values()) {
            if (type.name().equalsIgnoreCase(name))
                return type;
        }

        return null;
    }
}

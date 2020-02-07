package org.jbei.ice.lib.parsers.genbank;

import org.apache.commons.lang3.StringUtils;

public enum GenbankTag {
    LOCUS,
    DEFINITION,
    ACCESSION,
    VERSION,
    NID,
    PROJECT,
    DBLINK,
    KEYWORDS,
    SEGMENT,
    SOURCE,
    ORGANISM,
    REFERENCE,
    COMMENT,
    FEATURES,
    BASE_COUNT,
    CONTIG,
    ORIGIN;

    public static GenbankTag getTagForString(String tagString) {
        if (StringUtils.isBlank(tagString))
            return null;

        for (GenbankTag tag : GenbankTag.values()) {
            if (tag.name().equals(tagString.trim().toUpperCase()))
                return tag;
        }

        return null;
    }
}

package org.jbei.ice.services.blazeds.vo;

import java.io.Serializable;

/**
 * Value object to store User Preferences for flex apps.
 * 
 * @author Zinovii Dmytriv
 * 
 */
public class UserPreferences implements Serializable {
    private static final long serialVersionUID = 2980171409897004909L;

    public static final int DEFAULT_BP_PER_ROW = 60;
    public static final int DEFAULT_SEQUENCE_FONT_SIZE = 11;
    public static final int DEFAULT_ORF_MINIMUM_LENGTH = 300;

    private int bpPerRow;
    private int sequenceFontSize;
    private int orfMinimumLength;

    // Constructors
    public UserPreferences() {
        bpPerRow = DEFAULT_BP_PER_ROW;
        sequenceFontSize = DEFAULT_SEQUENCE_FONT_SIZE;
        orfMinimumLength = DEFAULT_ORF_MINIMUM_LENGTH;
    }

    public UserPreferences(int bpPerRow, int sequenceFontSize, int orfMinimumLength) {
        super();

        this.bpPerRow = bpPerRow;
        this.sequenceFontSize = sequenceFontSize;
        this.orfMinimumLength = orfMinimumLength;
    }

    // Properties
    public int getBpPerRow() {
        return bpPerRow;
    }

    public void setBpPerRow(int bpPerRow) {
        this.bpPerRow = bpPerRow;
    }

    public int getSequenceFontSize() {
        return sequenceFontSize;
    }

    public void setSequenceFontSize(int sequenceFontSize) {
        this.sequenceFontSize = sequenceFontSize;
    }

    public int getOrfMinimumLength() {
        return orfMinimumLength;
    }

    public void setOrfMinimumLength(int orfMinimumLength) {
        this.orfMinimumLength = orfMinimumLength;
    }
}

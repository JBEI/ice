package org.jbei.ice.lib.vo;

public interface IPlasmidValueObject extends IEntryValueObject {
    String getBackbone();

    void setBackbone(String backbone);

    String getOriginOfReplication();

    void setOriginOfReplication(String originOfReplication);

    String getPromoters();

    void setPromoters(String promoters);

    boolean getCircular();

    void setCircular(boolean circular);
}
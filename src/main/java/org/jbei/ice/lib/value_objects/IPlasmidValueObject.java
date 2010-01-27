package org.jbei.ice.lib.value_objects;

public interface IPlasmidValueObject extends IEntryValueObject {

    public abstract String getBackbone();

    public abstract void setBackbone(String backbone);

    public abstract String getOriginOfReplication();

    public abstract void setOriginOfReplication(String originOfReplication);

    public abstract String getPromoters();

    public abstract void setPromoters(String promoters);

    public abstract boolean getCircular();

    public abstract void setCircular(boolean circular);

}
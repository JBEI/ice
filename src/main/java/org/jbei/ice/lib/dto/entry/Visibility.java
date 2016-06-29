package org.jbei.ice.lib.dto.entry;

/**
 * Type of visibility for entries in the system.
 * PERMANENTLY_DELETED  -> Entry has been permanently deleted
 * DELETED              -> Entry has been deleted
 * DRAFT                -> Entry is part of a bulk upload that is still being recorded
 * PENDING              -> Entry is part of a bulk upload that has been submitted and is pending approval by an admin
 * TRANSFERRED          -> Entry was transferred from another registry pending approval from admin
 * OK                   -> Entry is available to be viewed by those with adequate permissions
 *
 * @author Hector Plahar, Elena Aravina
 */
public enum Visibility {

    PERMANENTLY_DELETED(-2), DELETED(-1), DRAFT(0), PENDING(1), TRANSFERRED(2), OK(9);

    private final int value;

    Visibility(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

    public static Visibility valueToEnum(Integer value) {
        for (Visibility visibility : Visibility.values()) {
            if (value == visibility.getValue())
                return visibility;
        }
        return OK;
    }
}

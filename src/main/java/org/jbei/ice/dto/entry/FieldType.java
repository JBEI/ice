package org.jbei.ice.dto.entry;

public enum FieldType {
    EXISTING,           // modifying an existing field
    BOOLEAN,
    TEXT_INPUT,         // single input box from user
    MULTI_CHOICE,       // user selects from pre-defined options
    MULTI_CHOICE_PLUS   // user selects from pre-defined options or enters their own value
}

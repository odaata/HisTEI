package eu.emergingstandards.lists.schema;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ro.sync.contentcompletion.xml.WhatPossibleValuesHasAttributeContext;

/**
 * Created by mike on 3/7/14.
 */
public class SchemaListAttribute {

    protected final String attributeName;
    protected final String elementName;
    protected final String parentElementName;

    public SchemaListAttribute(String attributeName, String elementName, String parentElementName) {
        this.attributeName = attributeName;
        this.elementName = elementName;
        this.parentElementName = parentElementName;
    }

    public SchemaListAttribute(String attributeName, String elementName) {
        this(attributeName, elementName, null);
    }

    public SchemaListAttribute(String attributeName) {
        this(attributeName, null, null);
    }

    @NotNull
    public String getAttributeName() {
        return attributeName;
    }

    @Nullable
    public String getElementName() {
        return elementName;
    }

    @Nullable
    public String getParentElementName() {
        return parentElementName;
    }

    public boolean matches(WhatPossibleValuesHasAttributeContext context) {
        if (context == null || attributeName == null) return false;

        if (!attributeName.equals(context.getAttributeName())) return false;

        if (elementName != null) {
            String contextElementName = context.getParentElement().getQName();
            if (!elementName.equals(contextElementName)) return false;

            if (parentElementName != null) {
                if (!context.getElementStack().empty()) {
                    if (!parentElementName.equals(context.getGrandparentElement())) return false;
                }
            }
        }
        return true;
    }
}

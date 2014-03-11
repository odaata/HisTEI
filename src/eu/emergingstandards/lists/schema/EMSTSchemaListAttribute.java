package eu.emergingstandards.lists.schema;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ro.sync.contentcompletion.xml.WhatPossibleValuesHasAttributeContext;

/**
 * Created by mike on 3/7/14.
 */
public class EMSTSchemaListAttribute {

    protected final String attributeName;
    protected final String elementName;
    protected final String parentElementName;

    public EMSTSchemaListAttribute(String attributeName, String elementName, String parentElementName) {
        this.attributeName = attributeName;
        this.elementName = elementName;
        this.parentElementName = parentElementName;
    }

    public EMSTSchemaListAttribute(String attributeName, String elementName) {
        this(attributeName, elementName, null);
    }

    public EMSTSchemaListAttribute(String attributeName) {
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
//              If root element, no GrandparentElement, so don't try to get it - else throws error
//                String contextParentName = TEI_ELEMENT_NAME.equals(contextElementName) ? null : context.getGrandparentElement();
//                if (!parentElementName.equals(contextParentName)) return false;
            }
        }
        return true;
    }
}

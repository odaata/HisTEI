package info.histei.lists.schema;

import info.histei.commons.AbstractUniqueAttribute;
import ro.sync.contentcompletion.xml.WhatPossibleValuesHasAttributeContext;

/**
 * Created by mike on 3/7/14.
 */
public class SchemaListAttribute extends AbstractUniqueAttribute<WhatPossibleValuesHasAttributeContext> {

    public SchemaListAttribute(String attributeName, String elementName, String parentElementName) {
        super(attributeName, elementName, parentElementName);
    }

    public SchemaListAttribute(String attributeName, String elementName) {
        this(attributeName, elementName, null);
    }

    public SchemaListAttribute(String attributeName) {
        this(attributeName, null, null);
    }

    @Override
    public boolean matches(WhatPossibleValuesHasAttributeContext context) {
        if (context == null) return false;

        if (!attributeName.equals(context.getAttributeName())) return false;

        if (elementName != null) {
            if (!elementName.equals(context.getParentElement().getQName())) return false;

            if (parentElementName != null) {
                if (!context.getElementStack().empty()) {
                    if (!parentElementName.equals(context.getGrandparentElement())) return false;
                }
            }
        }
        return true;
    }
}

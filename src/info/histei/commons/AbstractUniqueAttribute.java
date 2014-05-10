package info.histei.commons;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ro.sync.contentcompletion.xml.WhatPossibleValuesHasAttributeContext;

/**
 * Created by mike on 5/9/14.
 */
public abstract class AbstractUniqueAttribute implements UniqueAttribute<WhatPossibleValuesHasAttributeContext> {
    protected final String attributeName;
    protected final String elementName;
    protected final String parentElementName;

    public AbstractUniqueAttribute(String elementName, String parentElementName, String attributeName) {
        this.elementName = elementName;
        this.parentElementName = parentElementName;
        this.attributeName = attributeName;
    }

    @Override
    @NotNull
    public String getAttributeName() {
        return attributeName;
    }

    @Override
    @Nullable
    public String getElementName() {
        return elementName;
    }

    @Override
    @Nullable
    public String getParentElementName() {
        return parentElementName;
    }

    @Override
    public abstract boolean matches(WhatPossibleValuesHasAttributeContext context);
}

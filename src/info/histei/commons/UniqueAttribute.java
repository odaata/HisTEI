package info.histei.commons;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ro.sync.contentcompletion.xml.WhatPossibleValuesHasAttributeContext;

/**
 * Created by mike on 5/9/14.
 */
public interface UniqueAttribute {
    @NotNull
    String getAttributeName();

    @Nullable
    String getElementName();

    @Nullable
    String getParentElementName();

    boolean matches(WhatPossibleValuesHasAttributeContext context);
}

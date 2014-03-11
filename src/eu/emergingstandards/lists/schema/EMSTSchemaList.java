package eu.emergingstandards.lists.schema;

import eu.emergingstandards.lists.EMSTList;
import eu.emergingstandards.lists.EMSTListItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ro.sync.contentcompletion.xml.CIValue;
import ro.sync.contentcompletion.xml.WhatPossibleValuesHasAttributeContext;

import java.util.List;

/**
 * Created by mike on 3/7/14.
 */
public interface EMSTSchemaList<I extends EMSTListItem> extends EMSTList<I> {

    @NotNull
    List<EMSTSchemaListAttribute> getAttributes();

    boolean matches(WhatPossibleValuesHasAttributeContext context);

    @Nullable
    List<CIValue> getCIValues();
}

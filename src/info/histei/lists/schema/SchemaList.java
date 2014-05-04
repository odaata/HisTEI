package info.histei.lists.schema;

import info.histei.lists.ListItem;
import info.histei.lists.Listable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ro.sync.contentcompletion.xml.CIValue;
import ro.sync.contentcompletion.xml.WhatPossibleValuesHasAttributeContext;

import java.util.List;

/**
 * Created by mike on 3/7/14.
 */
public interface SchemaList<I extends ListItem> extends Listable<I> {

    @NotNull
    List<SchemaListAttribute> getAttributes();

    boolean matches(WhatPossibleValuesHasAttributeContext context);

    @Nullable
    List<CIValue> getCIValues();
}

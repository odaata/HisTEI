package info.histei.lists;

import info.histei.lists.schema.SchemaList;
import org.jetbrains.annotations.Nullable;
import ro.sync.contentcompletion.xml.WhatPossibleValuesHasAttributeContext;
import ro.sync.ecss.extensions.api.AuthorAccess;

/**
 * Created by mike on 5/9/14.
 */
public interface ListProvider {
    AuthorAccess getAuthorAccess();

    void addListeners();

    void removeListeners();

    @Nullable
    SchemaList<? extends ListItem> getList(WhatPossibleValuesHasAttributeContext context);
}

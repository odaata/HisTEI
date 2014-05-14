package info.histei.lists.schema;

import info.histei.lists.ListItem;
import info.histei.lists.ListProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ro.sync.contentcompletion.xml.WhatPossibleValuesHasAttributeContext;
import ro.sync.ecss.extensions.api.AuthorAccess;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static info.histei.utils.OxygenUtils.getEditorLocation;

/**
 * Created by mike on 3/7/14.
 */
public class SchemaListProvider implements ListProvider<WhatPossibleValuesHasAttributeContext, SchemaList<SchemaListItem>> {

    private static final Map<URL, SchemaListProvider> providers = new HashMap<>();

    @Nullable
    public static SchemaListProvider get(URL editorLocation) {
        if (editorLocation != null) {
            return providers.get(editorLocation);
        }
        return null;
    }

    @NotNull
    public static SchemaListProvider add(AuthorAccess authorAccess) {
        URL editorLocation = getEditorLocation(authorAccess);
        SchemaListProvider provider = providers.get(editorLocation);

        if (provider == null) {
            provider = new SchemaListProvider(authorAccess);
            providers.put(editorLocation, provider);
        }
        return provider;
    }

    public static void remove(AuthorAccess authorAccess) {
        URL editorLocation = getEditorLocation(authorAccess);
        SchemaListProvider provider = providers.get(editorLocation);

        if (provider != null) {
            provider.removeListeners();
            providers.remove(editorLocation);
        }
    }

    private final AuthorAccess authorAccess;
    private final List<SchemaList<SchemaListItem>> lists = StaticSchemaList.getAll();

    private SchemaListProvider(AuthorAccess authorAccess) {
        this.authorAccess = authorAccess;

        lists.add(new MediaSchemaList(authorAccess));
        lists.add(new HandSchemaList(authorAccess));
    }

    @Override
    public final AuthorAccess getAuthorAccess() {
        return authorAccess;
    }

    @Override
    @Nullable
    public SchemaList<SchemaListItem> getList(WhatPossibleValuesHasAttributeContext context) {
        synchronized (lists) {
            for (SchemaList<SchemaListItem> list : lists) {
                if (list.matches(context)) {
                    return list;
                }
            }
        }
        return null;
    }

    public void addListeners() {
        for (SchemaList<? extends ListItem> list : lists) {
            if (list instanceof ReferenceSchemaList<?>) {
                ((ReferenceSchemaList<?>) list).addListener();
            }

        }
    }

    public void removeListeners() {
        for (SchemaList<? extends ListItem> list : lists) {
            if (list instanceof ReferenceSchemaList<?>) {
                ((ReferenceSchemaList<?>) list).removeListener();
            }

        }
    }

}

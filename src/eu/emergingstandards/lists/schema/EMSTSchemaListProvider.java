package eu.emergingstandards.lists.schema;

import eu.emergingstandards.lists.EMSTListItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ro.sync.contentcompletion.xml.WhatPossibleValuesHasAttributeContext;
import ro.sync.ecss.extensions.api.AuthorAccess;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import static eu.emergingstandards.utils.EMSTOxygenUtils.getEditorLocation;

/**
 * Created by mike on 3/7/14.
 */
public class EMSTSchemaListProvider {

    private static final Map<URL, EMSTSchemaListProvider> providers = new WeakHashMap<>();

    @Nullable
    public static EMSTSchemaListProvider get(URL editorLocation) {
        return providers.get(editorLocation);
    }

    @NotNull
    public static EMSTSchemaListProvider add(AuthorAccess authorAccess) {
        URL editorLocation = getEditorLocation(authorAccess);
        EMSTSchemaListProvider provider = providers.get(editorLocation);

        if (provider == null) {
            provider = new EMSTSchemaListProvider(authorAccess);
            providers.put(editorLocation, provider);
        }
        return provider;
    }

    public static void remove(AuthorAccess authorAccess) {
        URL editorLocation = getEditorLocation(authorAccess);
        EMSTSchemaListProvider provider = providers.get(editorLocation);

        if (provider != null) {
            provider.removeListeners();
            providers.remove(editorLocation);
        }
    }

    private final AuthorAccess authorAccess;
    private final List<EMSTSchemaList<? extends EMSTListItem>> lists = EMSTStaticSchemaList.getAll();

    private EMSTSchemaListProvider(AuthorAccess authorAccess) {
        this.authorAccess = authorAccess;

        lists.add(new EMSTMediaSchemaList(authorAccess));
        lists.add(new EMSTHandSchemaList(authorAccess));
    }

    public final AuthorAccess getAuthorAccess() {
        return authorAccess;
    }

    public void addListeners() {
        for (EMSTSchemaList<? extends EMSTListItem> list : lists) {
            if (list instanceof EMSTReferenceSchemaList<?>) {
                ((EMSTReferenceSchemaList<?>) list).addListener();
            }

        }
    }

    public void removeListeners() {
        for (EMSTSchemaList<? extends EMSTListItem> list : lists) {
            if (list instanceof EMSTReferenceSchemaList<?>) {
                ((EMSTReferenceSchemaList<?>) list).removeListener();
            }

        }
    }

    @Nullable
    public EMSTSchemaList<? extends EMSTListItem> getList(WhatPossibleValuesHasAttributeContext context) {
        for (EMSTSchemaList<? extends EMSTListItem> list : lists) {
            if (list.matches(context)) {
                return list;
            }
        }
        return null;
    }

}

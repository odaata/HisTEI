package info.histei.extensions;

import info.histei.lists.ListItem;
import info.histei.lists.schema.SchemaList;
import info.histei.lists.schema.SchemaListProvider;
import info.histei.utils.OxygenUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ro.sync.contentcompletion.xml.*;

import java.net.URL;
import java.util.List;

/**
 * Created by mike on 3/6/14.
 */
public class HTSchemaManagerFilter implements SchemaManagerFilter {

    private final URL editorLocation;
    private SchemaListProvider schemaListProvider;

    public HTSchemaManagerFilter() {
        editorLocation = OxygenUtils.getCurrentEditorLocation();
    }

    @NotNull
    public URL getEditorLocation() {
        return editorLocation;
    }

    @Nullable
    public SchemaListProvider getSchemaListProvider() {
        if (schemaListProvider == null) {
            schemaListProvider = SchemaListProvider.get(getEditorLocation());
        }
        return schemaListProvider;
    }

    @Override
    public List<CIElement> filterElements(List<CIElement> ciElements, WhatElementsCanGoHereContext context) {
        return ciElements;
    }

    @Override
    public List<CIAttribute> filterAttributes(List<CIAttribute> ciAttributes, WhatAttributesCanGoHereContext context) {
        return ciAttributes;
    }

    @Override
    public List<CIValue> filterAttributeValues(List<CIValue> ciValues, WhatPossibleValuesHasAttributeContext context) {
        SchemaListProvider provider = getSchemaListProvider();

        if (provider != null) {
            SchemaList<? extends ListItem> list = provider.getList(context);
            if (list != null) {
                ciValues = list.getCIValues();
            }
        }
        return ciValues;
    }

    @Override
    public List<CIValue> filterElementValues(List<CIValue> ciValues, Context context) {
        return ciValues;
    }

    @Override
    public String getDescription() {
        return "Emerging Standards Schema Manager Filter.";
    }
}

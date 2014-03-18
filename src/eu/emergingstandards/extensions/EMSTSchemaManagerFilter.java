package eu.emergingstandards.extensions;

import eu.emergingstandards.lists.EMSTListItem;
import eu.emergingstandards.lists.schema.EMSTSchemaList;
import eu.emergingstandards.lists.schema.EMSTSchemaListProvider;
import eu.emergingstandards.utils.EMSTOxygenUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ro.sync.contentcompletion.xml.*;

import java.net.URL;
import java.util.List;

/**
 * Created by mike on 3/6/14.
 */
public class EMSTSchemaManagerFilter implements SchemaManagerFilter {

    private final URL editorLocation;
    private EMSTSchemaListProvider schemaListProvider;

    public EMSTSchemaManagerFilter() {
        editorLocation = EMSTOxygenUtils.getCurrentEditorLocation();
    }

    @NotNull
    public URL getEditorLocation() {
        return editorLocation;
    }

    @Nullable
    public EMSTSchemaListProvider getSchemaListProvider() {
        if (schemaListProvider == null) {
            schemaListProvider = EMSTSchemaListProvider.get(getEditorLocation());
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
        EMSTSchemaListProvider provider = getSchemaListProvider();

        if (provider != null) {
            EMSTSchemaList<? extends EMSTListItem> list = provider.getList(context);
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

package info.histei.extensions;

import info.histei.lists.schema.SchemaList;
import info.histei.lists.schema.SchemaListItem;
import info.histei.lists.schema.SchemaListProvider;
import info.histei.utils.OxygenUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ro.sync.contentcompletion.xml.*;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * Created by mike on 3/6/14.
 */
public class HTSchemaManagerFilter implements SchemaManagerFilter {

    private SchemaListProvider schemaListProvider;

    @Nullable
    public SchemaListProvider getSchemaListProvider(String systemID) {
      if (schemaListProvider == null) {
        try {
          URL url = new URL(systemID);
          schemaListProvider = SchemaListProvider.get(url);
        } catch (MalformedURLException e) {
          // Shouldn't happen. The system Id is obtained from Oxygen API.
          e.printStackTrace();
        }
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
        SchemaListProvider provider = getSchemaListProvider(context.getSystemID());

        if (provider != null) {
            SchemaList<SchemaListItem> list = provider.getList(context);
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
        return "HisTEI Schema Manager Filter.";
    }
}

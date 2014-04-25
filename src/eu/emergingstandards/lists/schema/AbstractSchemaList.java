package eu.emergingstandards.lists.schema;

import eu.emergingstandards.lists.ListItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ro.sync.contentcompletion.xml.CIValue;
import ro.sync.contentcompletion.xml.WhatPossibleValuesHasAttributeContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by mike on 3/6/14.
 */
public abstract class AbstractSchemaList<I extends ListItem> implements SchemaList<I> {

    private final List<I> items = Collections.synchronizedList(new ArrayList<I>());
    private final List<CIValue> ciValues = new ArrayList<>();

    @NotNull
    @Override
    public final List<I> getItems() {
        return items;
    }

    @Override
    public final boolean matches(WhatPossibleValuesHasAttributeContext context) {
        for (SchemaListAttribute attribute : getAttributes()) {
            if (attribute.matches(context)) {
                return true;
            }
        }
        return false;
    }

    @NotNull
    @Override
    public abstract List<SchemaListAttribute> getAttributes();

    @Nullable
    @Override
    public final List<CIValue> getCIValues() {
        if (items.isEmpty()) return null;

        if (ciValues.isEmpty()) {
            synchronized (items) {
                for (I item : items) {
                    ciValues.add(new CIValue(item.getValue(), item.getTooltip()));
                }
            }
        }
        return ciValues;
    }

    public synchronized void reset() {
        items.clear();
        ciValues.clear();
    }
}

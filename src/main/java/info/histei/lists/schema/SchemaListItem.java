package info.histei.lists.schema;

import info.histei.lists.ListItemAdapter;
import org.jetbrains.annotations.Nullable;

/**
 * Created by mike on 3/9/14.
 */
public class SchemaListItem extends ListItemAdapter {

    @Nullable
    public static SchemaListItem get(String value, String tooltip) {
        if (value != null) {
            return new SchemaListItem(value, tooltip);
        } else {
            return null;
        }
    }

    protected SchemaListItem(String value, String tooltip) {
        super(value, null, tooltip);
    }

    @Override
    public String toString() {
        return getValue();
    }
}

package eu.emergingstandards.lists.schema;

import eu.emergingstandards.lists.EMSTListItemAdapter;
import org.jetbrains.annotations.Nullable;

/**
 * Created by mike on 3/9/14.
 */
public class EMSTSchemaListItem extends EMSTListItemAdapter {

    @Nullable
    public static EMSTSchemaListItem get(String value, String tooltip) {
        if (value != null) {
            return new EMSTSchemaListItem(value, tooltip);
        } else {
            return null;
        }
    }

    protected EMSTSchemaListItem(String value, String tooltip) {
        super(value, null, tooltip);
    }
}

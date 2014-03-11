package eu.emergingstandards.contextual_info;

import eu.emergingstandards.lists.EMSTListItemAdapter;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static eu.emergingstandards.utils.EMSTSaxonUtils.getChildText;
import static eu.emergingstandards.utils.EMSTUtils.emptyToNull;
import static eu.emergingstandards.utils.EMSTUtils.nullToEmpty;

/**
 * Created by mike on 2/10/14.
 */
public class EMSTContextualItem extends EMSTListItemAdapter {

    protected static final String TYPE_ATTRIB_NAME = "type";

    @Nullable
    public static EMSTContextualItem get(EMSTContextualType contextualType, XdmNode item) {
        EMSTContextualItem contextualItem = null;

        String value = emptyToNull(item.getAttributeValue(new QName(VALUE_ATTRIB_NAME)));

        if (value != null && contextualType != null) {
            value = contextualType.getKey() + ":" + value;

            String label = nullToEmpty(getChildText(item, LABEL_ELEMENT_NAME));
            if (label.isEmpty()) label = value;

            String tooltip = nullToEmpty(getChildText(item, TOOLTIP_ELEMENT_NAME));

            String type = nullToEmpty(item.getAttributeValue(new QName(TYPE_ATTRIB_NAME)));

            contextualItem = new EMSTContextualItem(value, label, tooltip, contextualType, type);
        }
        return contextualItem;
    }

    /* Instance Members*/

    protected EMSTContextualType contextualType;
    protected String type;

    protected EMSTContextualItem(String value, String label, String tooltip,
                                 EMSTContextualType contextualType, String type) {
        super(value, label, tooltip);
        this.contextualType = contextualType;
        this.type = type;
    }

    @NotNull
    public EMSTContextualType getContextualType() {
        return contextualType;
    }

    @NotNull
    public String getType() {
        return type;
    }
}

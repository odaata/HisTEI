package info.histei.contextual_info;

import info.histei.lists.ListItemAdapter;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static info.histei.utils.MainUtils.emptyToNull;
import static info.histei.utils.MainUtils.nullToEmpty;
import static info.histei.utils.SaxonUtils.getChildText;

/**
 * Created by mike on 2/10/14.
 */
public class ContextualItem extends ListItemAdapter {

    protected static final String TYPE_ATTRIB_NAME = "type";

    @Nullable
    public static ContextualItem get(ContextualType contextualType, XdmNode item) {
        ContextualItem contextualItem = null;

        String value = emptyToNull(item.getAttributeValue(new QName(VALUE_ATTRIB_NAME)));

        if (value != null && contextualType != null) {
            value = contextualType.getKey() + ":" + value;

            String label = nullToEmpty(getChildText(item, LABEL_ELEMENT_NAME));
            if (label.isEmpty()) label = value;

            String tooltip = nullToEmpty(getChildText(item, TOOLTIP_ELEMENT_NAME));

            String type = nullToEmpty(item.getAttributeValue(new QName(TYPE_ATTRIB_NAME)));

            contextualItem = new ContextualItem(value, label, tooltip, contextualType, type);
        }
        return contextualItem;
    }

    /* Instance Members*/

    protected ContextualType contextualType;
    protected String type;

    protected ContextualItem(String value, String label, String tooltip,
                             ContextualType contextualType, String type) {
        super(value, label, tooltip);
        this.contextualType = contextualType;
        this.type = type;
    }

    @NotNull
    public ContextualType getContextualType() {
        return contextualType;
    }

    @NotNull
    public String getType() {
        return type;
    }
}

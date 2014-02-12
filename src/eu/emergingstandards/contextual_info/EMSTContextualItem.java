package eu.emergingstandards.contextual_info;

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
public class EMSTContextualItem {

    private static final String VALUE_ATTRIB_NAME = "value";
    private static final String TYPE_ATTRIB_NAME = "type";
    private static final String LABEL_ELEMENT_NAME = "label";
    private static final String TOOLTIP_ELEMENT_NAME = "tooltip";

    @Nullable
    public static EMSTContextualItem get(EMSTContextualType contextualType, XdmNode item) {
        String value = emptyToNull(item.getAttributeValue(new QName(VALUE_ATTRIB_NAME)));

        if (value != null && contextualType != null) {
            return new EMSTContextualItem(value, contextualType, item);
        } else {
            return null;
        }
    }

    /* Instance Members*/

    private EMSTContextualType contextualType;
    private String value;
    private String type;
    private String label;
    private String tooltip;

    protected EMSTContextualItem(String value, EMSTContextualType contextualType, XdmNode item) {
        this.contextualType = contextualType;
        this.value = value;
        this.type = nullToEmpty(item.getAttributeValue(new QName(TYPE_ATTRIB_NAME)));

        this.label = nullToEmpty(getChildText(item, LABEL_ELEMENT_NAME));
        if (this.label.isEmpty()) this.label = value;

        this.tooltip = nullToEmpty(getChildText(item, TOOLTIP_ELEMENT_NAME));
    }

    @NotNull
    public EMSTContextualType getContextualType() {
        return contextualType;
    }

    @NotNull
    public String getValue() {
        return getContextualType().getKey() + ":" + value;
    }

    @NotNull
    public String getType() {
        return type;
    }

    @NotNull
    public String getLabel() {
        return label;
    }

    @NotNull
    public String getTooltip() {
        return tooltip;
    }
}

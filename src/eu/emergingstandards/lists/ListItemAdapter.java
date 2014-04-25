package eu.emergingstandards.lists;

import org.jetbrains.annotations.NotNull;

import static eu.emergingstandards.utils.MainUtils.nullToEmpty;

/**
 * Created by mike on 3/4/14.
 */
public class ListItemAdapter implements ListItem {

    protected static final String VALUE_ATTRIB_NAME = "value";
    protected static final String LABEL_ELEMENT_NAME = "label";
    protected static final String TOOLTIP_ELEMENT_NAME = "tooltip";

    protected String value;
    protected String label;
    protected String tooltip;

    protected ListItemAdapter(String value, String label, String tooltip) {
        this.value = nullToEmpty(value);
        this.label = nullToEmpty(label);
        this.tooltip = nullToEmpty(tooltip);
    }

    @NotNull
    @Override
    public String getValue() {
        return value;
    }

    @NotNull
    @Override
    public String getLabel() {
        return label;
    }

    @NotNull
    @Override
    public String getTooltip() {
        return tooltip;
    }

    @Override
    public String toString() {
        return getLabel();
    }
}

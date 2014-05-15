package info.histei.contextual_info;

import info.histei.lists.ListItemAdapter;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmNode;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static info.histei.utils.SaxonUtils.getChildText;

/**
 * Created by mike on 2/10/14.
 */
public class ContextualItem extends ListItemAdapter {

    private static final Logger logger = Logger.getLogger(ContextualItem.class.getName());

    //  Regexp info for retrieving IDs
    private static final String TYPE_REGEX = "(" + StringUtils.join(ContextualType.getKeys(), "|") + ")";
    private static final String ID_REGEX = "(\\S+)";
    private static final Pattern REF_PATTERN = Pattern.compile(TYPE_REGEX + ":" + ID_REGEX);

    protected static final String TYPE_ATTRIB_NAME = "type";

    @Nullable
    public static ContextualItem get(ContextualType contextualType, XdmNode item) {
        ContextualItem contextualItem = null;

        String value = StringUtils.trimToNull(item.getAttributeValue(new QName(VALUE_ATTRIB_NAME)));

        if (value != null && contextualType != null) {
            value = contextualType.getKey() + ":" + value;

            String label = StringUtils.trimToEmpty(getChildText(item, LABEL_ELEMENT_NAME));
            if (label.isEmpty()) label = value;

            String tooltip = StringUtils.trimToEmpty(getChildText(item, TOOLTIP_ELEMENT_NAME));

            String type = StringUtils.trimToEmpty(item.getAttributeValue(new QName(TYPE_ATTRIB_NAME)));

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

    @NotNull
    public String getRefID() {
        String id = "";
        String value = getValue();

        if (!value.isEmpty()) {
            Matcher matcher = REF_PATTERN.matcher(value);
            if (matcher.matches()) {
                id = matcher.group(2);
            }
        }
        return id;
    }

    @Nullable
    public URL getURL() {
        ContextualInfo info = ContextualInfo.get(getContextualType());
        URL url = info.getURL();

        if (url != null) {
            try {
                String id = getRefID();
                if (!id.isEmpty()) url = new URL(url, "#" + id);
            } catch (MalformedURLException e) {
                logger.error(e, e);
            }
        }
        return url;
    }
}

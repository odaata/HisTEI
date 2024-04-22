package info.histei.extensions;

import info.histei.utils.MainUtils;
import info.histei.utils.OxygenUtils;
import info.histei.utils.XMLUtils;
import ro.sync.ecss.css.LabelContent;
import ro.sync.ecss.css.StaticContent;
import ro.sync.ecss.css.Styles;
import ro.sync.ecss.extensions.api.StylesFilter;
import ro.sync.ecss.extensions.api.node.AuthorElement;
import ro.sync.ecss.extensions.api.node.AuthorNode;

import java.util.HashMap;
import java.util.Map;

import static info.histei.commons.TEINamespace.FACSIMILE_ELEMENT_NAME;

/**
 * Created by mike on 1/6/14.
 * <p/>
 * Implements custom styling for the HisTEI project
 * This is primarily useful for generating combo boxes from external contextual information XML files
 */
public class HTStylesFilter implements StylesFilter {

    @Override
    public Styles filter(Styles styles, AuthorNode authorNode) {
        addFacsimileElement(styles, authorNode);

        return styles;
    }

    /*
    *   Adds a label for the xml:base attribute - only way to decode the URL for human consumption is via Java
    *       so this function just adds a label with the url decoded
    * */
    private void addFacsimileElement(Styles styles, AuthorNode authorNode) {
        if (!styles.isInline() && FACSIMILE_ELEMENT_NAME.equals(authorNode.getName())) {
            AuthorElement authorElement = OxygenUtils.castAuthorElement(authorNode);

            if (authorElement != null) {
                String xmlBase = OxygenUtils.getAttrValue(authorElement.getAttribute(XMLUtils.XML_BASE_ATTRIB_NAME));
                if (xmlBase != null) {
                    Map<String, Object> labelProps = new HashMap<>();

                    labelProps.put("text", MainUtils.decodeURL(xmlBase));
                    labelProps.put("styles", "* { font-weight:normal; }");
                    StaticContent[] mixedContent = new StaticContent[]{new LabelContent(labelProps)};

                    styles.setProperty(Styles.KEY_MIXED_CONTENT, mixedContent);
                }
            }
        }
    }

    @Override
    public String getDescription() {
        return "HisTEI styles filter.";
    }
}

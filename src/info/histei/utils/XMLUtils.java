package info.histei.utils;

import info.histei.commons.NamespaceType;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by mike on 2/6/14.
 */
public class XMLUtils {

//    private static final Logger logger = Logger.getLogger(XMLUtils.class.getName());

    public static final String XML_ID_ATTRIB_NAME = "xml:id";
    public static final String XML_BASE_ATTRIB_NAME = "xml:base";
    public static final String XML_LANG_ATTRIB_NAME = "xml:lang";
    public static final String XML_NS_ATTRIB_NAME = "xmlns";

    @NotNull
    public static String castQNameToXMLString(QName qName) {
        String name = "";

        if (qName != null) {
            name = MainUtils.nullToEmpty(qName.getLocalPart());
            if (!name.isEmpty()) {
                String prefix = MainUtils.nullToEmpty(qName.getPrefix());
                if (!prefix.isEmpty()) {
                    name = prefix + ":" + name;
                }
            }
        }
        return name;
    }

    @NotNull
    public static String createElement(String elementName) {
        return createElement(NamespaceType.TEI.getURLID(), elementName, null);
    }

    @NotNull
    public static String createElement(String elementName, Map<String, String> attributes) {
        return createElement(NamespaceType.TEI.getURLID(), elementName, attributes);
    }

    @NotNull
    public static String createElement(String namespace, String elementName, Map<String, String> attributes) {
        String element = "";
        namespace = MainUtils.emptyToNull(namespace);
        elementName = MainUtils.emptyToNull(elementName);
        List<String> createdAttributes;

        if (namespace != null && elementName != null) {
            if (attributes != null) {
                createdAttributes = new ArrayList<>(attributes.size() + 1);
                for (String name : attributes.keySet()) {
                    String value = MainUtils.emptyToNull(attributes.get(name));
                    if (value != null) {
                        String attr = createAttribute(name, value);
                        createdAttributes.add(attr);
                    }
                }
            } else {
                createdAttributes = new ArrayList<>(1);
            }
            createdAttributes.add(0, createAttribute(XML_NS_ATTRIB_NAME, namespace));
            String allAttributes = StringUtils.join(createdAttributes, " ");
            element = "<" + elementName + " " + allAttributes + "/>";
        }
        return element;
    }

    @NotNull
    private static String createAttribute(String name, String value) {
        return name + "=\"" + StringEscapeUtils.escapeXml(value) + "\"";
    }

    private XMLUtils() {
    }
}

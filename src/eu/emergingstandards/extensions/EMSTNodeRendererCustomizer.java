package eu.emergingstandards.extensions;

import eu.emergingstandards.commons.EMSTIcon;
import ro.sync.exml.workspace.api.node.customizer.BasicRenderingInformation;
import ro.sync.exml.workspace.api.node.customizer.NodeRendererCustomizerContext;
import ro.sync.exml.workspace.api.node.customizer.XMLNodeRendererCustomizer;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by mike on 2/9/14.
 */
public class EMSTNodeRendererCustomizer extends XMLNodeRendererCustomizer {

    public static final Map<String, EMSTIconNode> iconNodes = new HashMap<>(50);

    private static class EMSTIconNode {

        public String nodeName;
        public String iconName;
        public String attribName;
        public Map<String, String> attribValues = new HashMap<>();

        private EMSTIconNode(String nodeName, String iconName, String attribName, Map<String, String> attribValues) {
            this.nodeName = nodeName;
            this.iconName = iconName;
            this.attribName = attribName;
            this.attribValues = attribValues;
        }

        private EMSTIconNode(String nodeName, String iconName) {
            this(nodeName, iconName, null, null);
        }

        private EMSTIconNode(String nodeName) {
            this(nodeName, nodeName, null, null);
        }
    }

    static {
        Map<String, String> initMap;
//      TEI Main Structural Nodes
        iconNodes.put("TEI", new EMSTIconNode("TEI", "tei"));
        iconNodes.put("teiHeader", new EMSTIconNode("teiHeader", "tei_header"));
        iconNodes.put("title", new EMSTIconNode("title"));
//      Facsimile stuff
        iconNodes.put("facsimile", new EMSTIconNode("facsimile", "folder_image"));
        iconNodes.put("graphic", new EMSTIconNode("graphic"));
        iconNodes.put("media", new EMSTIconNode("media", "pdf"));
//      Main Text Area
        iconNodes.put("text", new EMSTIconNode("text"));
        iconNodes.put("body", new EMSTIconNode("body"));
//      Divs
        initMap = new HashMap<>();
        initMap.put("address", "div_address");
        initMap.put("entry", "div_entry");
        iconNodes.put("div", new EMSTIconNode("div", "div", "type", initMap));
//      Paragraph - level
        iconNodes.put("p", new EMSTIconNode("p", "paragraph"));
        iconNodes.put("fw", new EMSTIconNode("fw", "forme_work"));
        iconNodes.put("head", new EMSTIconNode("head"));
//      Div subsections
        iconNodes.put("opener", new EMSTIconNode("opener"));
        iconNodes.put("closer", new EMSTIconNode("closer"));
        iconNodes.put("dateline", new EMSTIconNode("dateline"));
        iconNodes.put("salute", new EMSTIconNode("salute"));
        iconNodes.put("signed", new EMSTIconNode("signed"));
        iconNodes.put("postscript", new EMSTIconNode("postscript"));
//      seg@function
        initMap = new HashMap<>();
        initMap.put("salute", "salute");
        initMap.put("formulaic", "formulaic");
        iconNodes.put("seg", new EMSTIconNode("seg", null, "function", initMap));
//      Breaks
        iconNodes.put("lb", new EMSTIconNode("lb", "line_break"));
        iconNodes.put("pb", new EMSTIconNode("pb", "page_break"));
//      Hands
        iconNodes.put("handNote", new EMSTIconNode("handNote", "hand"));
        iconNodes.put("handShift", new EMSTIconNode("handShift", "hand_shift"));
//      Links
        iconNodes.put("ptr", new EMSTIconNode("ptr", "link"));
        iconNodes.put("ref", new EMSTIconNode("ref", "link"));
//      Editorial Stuff
        iconNodes.put("abbr", new EMSTIconNode("abbr", "abbreviation"));
        iconNodes.put("expan", new EMSTIconNode("expan", "expansion"));
        iconNodes.put("foreign", new EMSTIconNode("foreign"));
        iconNodes.put("pc", new EMSTIconNode("pc", "punctuation"));
//      note@type for storing Archive info in Org - otherwise normal note icon
        initMap = new HashMap<>();
        initMap.put("archive", "repository");
        initMap.put("collection", "collection");
        initMap.put("inventory", "inventory");
        iconNodes.put("note", new EMSTIconNode("note", "note", "type", initMap));
//      Primary Sources stuff
        iconNodes.put("add", new EMSTIconNode("add", "addition"));
        iconNodes.put("del", new EMSTIconNode("del", "deletion"));
        iconNodes.put("gap", new EMSTIconNode("gap"));
        iconNodes.put("supplied", new EMSTIconNode("supplied"));
        iconNodes.put("unclear", new EMSTIconNode("unclear"));
//      Text decorations/styles
        initMap = new HashMap<>();
        initMap.put("bold", "bold");
        initMap.put("italic", "italic");
        initMap.put("subscript", "subscript");
        initMap.put("superscript", "superscript");
        initMap.put("underline", "underline");
        iconNodes.put("hi", new EMSTIconNode("hi", null, "rend", initMap));
//      Contextual Information
        iconNodes.put("date", new EMSTIconNode("date"));
//      Person
        iconNodes.put("person", new EMSTIconNode("person"));
        iconNodes.put("persName", new EMSTIconNode("persName", "person"));
//      Place
        iconNodes.put("place", new EMSTIconNode("place"));
        iconNodes.put("placeName", new EMSTIconNode("placeName", "place"));
        iconNodes.put("district", new EMSTIconNode("district", "place"));
        iconNodes.put("settlement", new EMSTIconNode("settlement", "place"));
        iconNodes.put("region", new EMSTIconNode("region", "place"));
        iconNodes.put("country", new EMSTIconNode("country", "place"));
        iconNodes.put("bloc", new EMSTIconNode("bloc", "place"));
//      Org
        iconNodes.put("org", new EMSTIconNode("org"));
        iconNodes.put("orgName", new EMSTIconNode("orgName", "org"));
        iconNodes.put("repository", new EMSTIconNode("repository"));

        initMap = new HashMap<>();
        initMap.put("archive", "repository");
        initMap.put("collection", "collection");
        initMap.put("inventory", "inventory");
        iconNodes.put("note", new EMSTIconNode("seg", null, "function", initMap));
//      Genre
        iconNodes.put("category", new EMSTIconNode("category"));
        iconNodes.put("catRef", new EMSTIconNode("catRef", "category"));
    }

    @Override
    public BasicRenderingInformation getRenderingInformation(NodeRendererCustomizerContext context) {
        BasicRenderingInformation renderingInfo = new BasicRenderingInformation();

        EMSTIconNode iconNode = iconNodes.get(context.getNodeName());
        if (iconNode != null) {
            String iconName = null;

            if (iconNode.attribName != null) {
                String attrValue = context.getAttributeValue(iconNode.attribName);
                iconName = iconNode.attribValues.get(attrValue);
            }

            if (iconName == null) {
                iconName = iconNode.iconName;
            }

            if (iconName != null) {
                EMSTIcon icon = EMSTIcon.get(iconName);
                if (icon != null) {
                    renderingInfo.setIconPath(icon.getPath());
                }
            }
        }
        return renderingInfo;
    }

    @Override
    public String getDescription() {
        return "EMST Node Renderer Customizer";
    }

}

package info.histei.extensions;

import info.histei.commons.Icon;
import ro.sync.exml.workspace.api.node.customizer.BasicRenderingInformation;
import ro.sync.exml.workspace.api.node.customizer.NodeRendererCustomizerContext;
import ro.sync.exml.workspace.api.node.customizer.XMLNodeRendererCustomizer;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by mike on 2/9/14.
 */
public class HTNodeRendererCustomizer extends XMLNodeRendererCustomizer {

    public static final Map<String, HTIconNode> iconNodes = new HashMap<>(75);

    private static class HTIconNode {

        public String nodeName;
        public String iconName;
        public String attribName;
        public Map<String, String> attribValues = new HashMap<>();

        private HTIconNode(String nodeName, String iconName, String attribName, Map<String, String> attribValues) {
            this.nodeName = nodeName;
            this.iconName = iconName;
            this.attribName = attribName;
            this.attribValues = attribValues;
        }

        private HTIconNode(String nodeName, String iconName) {
            this(nodeName, iconName, null, null);
        }

        private HTIconNode(String nodeName) {
            this(nodeName, nodeName, null, null);
        }
    }

    static {
        Map<String, String> initMap;
//      TEI Main Structural Nodes
        iconNodes.put("TEI", new HTIconNode("TEI", "tei"));
        iconNodes.put("teiHeader", new HTIconNode("teiHeader", "tei_header"));
        iconNodes.put("title", new HTIconNode("title"));
        iconNodes.put("respStmt", new HTIconNode("respStmt", "resp"));
//      Manuscript Description - collection
        iconNodes.put("collection", new HTIconNode("collection", "collection"));
//      Revision Description - change
        iconNodes.put("change", new HTIconNode("change", "change"));
//      Facsimile stuff
        iconNodes.put("facsimile", new HTIconNode("facsimile", "folder_image"));
        iconNodes.put("graphic", new HTIconNode("graphic"));
        iconNodes.put("media", new HTIconNode("media", "pdf"));
//      Main Text Area
        iconNodes.put("text", new HTIconNode("text"));
        iconNodes.put("body", new HTIconNode("body"));
//      Divs
        initMap = new HashMap<>();
        initMap.put("address", "div_address");
        initMap.put("entry", "div_entry");
        iconNodes.put("div", new HTIconNode("div", "div", "type", initMap));
//      Paragraph - level
        iconNodes.put("p", new HTIconNode("p", "paragraph"));
        iconNodes.put("fw", new HTIconNode("fw", "forme_work"));
        iconNodes.put("head", new HTIconNode("head"));
        iconNodes.put("label", new HTIconNode("label"));
//      Div subsections
        iconNodes.put("opener", new HTIconNode("opener"));
        iconNodes.put("closer", new HTIconNode("closer"));
        iconNodes.put("dateline", new HTIconNode("dateline"));
        iconNodes.put("salute", new HTIconNode("salute"));
        iconNodes.put("signed", new HTIconNode("signed"));
        iconNodes.put("postscript", new HTIconNode("postscript"));
//      Linking
        iconNodes.put("gloss", new HTIconNode("gloss"));
        iconNodes.put("ab", new HTIconNode("ab"));
        iconNodes.put("term", new HTIconNode("term"));
//      Annotations
        iconNodes.put("s", new HTIconNode("sentence"));
        iconNodes.put("cl", new HTIconNode("clause"));
        iconNodes.put("phr", new HTIconNode("phrase"));
        iconNodes.put("w", new HTIconNode("word"));
//      seg@function
        initMap = new HashMap<>();
        initMap.put("salute", "salute");
        initMap.put("formulaic", "formulaic");
        initMap.put("", "seg");
        iconNodes.put("seg", new HTIconNode("seg", null, "function", initMap));
//      Breaks
        iconNodes.put("lb", new HTIconNode("lb", "line_break"));
        iconNodes.put("pb", new HTIconNode("pb", "page_break"));
//      Hands
        iconNodes.put("handNote", new HTIconNode("handNote", "hand"));
        iconNodes.put("handShift", new HTIconNode("handShift", "hand_shift"));
//      Links
        iconNodes.put("ptr", new HTIconNode("ptr", "link"));
        iconNodes.put("ref", new HTIconNode("ref", "link"));
//      Editorial Stuff
        iconNodes.put("abbr", new HTIconNode("abbr", "abbreviation"));
        iconNodes.put("expan", new HTIconNode("expan", "expansion"));
        iconNodes.put("foreign", new HTIconNode("foreign"));
//      note@type for storing Archive info in Org - otherwise normal note icon
        initMap = new HashMap<>();
        initMap.put("archive", "repository");
        initMap.put("collection", "collection");
        initMap.put("inventory", "inventory");
        iconNodes.put("note", new HTIconNode("note", "note", "type", initMap));
//      Primary Sources stuff
        iconNodes.put("add", new HTIconNode("add", "addition"));
        iconNodes.put("del", new HTIconNode("del", "deletion"));
        iconNodes.put("gap", new HTIconNode("gap"));
        iconNodes.put("supplied", new HTIconNode("supplied"));
        iconNodes.put("unclear", new HTIconNode("unclear"));
//      Text decorations/styles
        initMap = new HashMap<>();
        initMap.put("bold", "bold");
        initMap.put("italic", "italic");
        initMap.put("subscript", "subscript");
        initMap.put("superscript", "superscript");
        initMap.put("underline", "underline");
        iconNodes.put("hi", new HTIconNode("hi", null, "rend", initMap));

//      Contextual Information
        iconNodes.put("date", new HTIconNode("date"));
//      Person
        iconNodes.put("listPerson", new HTIconNode("listPerson"));
        iconNodes.put("person", new HTIconNode("person"));
        iconNodes.put("persName", new HTIconNode("persName", "person"));
//      Place
        iconNodes.put("listPlace", new HTIconNode("listPlace"));
        iconNodes.put("place", new HTIconNode("place"));
        iconNodes.put("placeName", new HTIconNode("placeName", "place"));
        iconNodes.put("district", new HTIconNode("district", "place"));
        iconNodes.put("settlement", new HTIconNode("settlement", "place"));
        iconNodes.put("region", new HTIconNode("region", "place"));
        iconNodes.put("country", new HTIconNode("country", "place"));
        iconNodes.put("bloc", new HTIconNode("bloc", "place"));
//      Org
        iconNodes.put("listOrg", new HTIconNode("listOrg"));
        iconNodes.put("org", new HTIconNode("org"));
        iconNodes.put("orgName", new HTIconNode("orgName", "org"));
        iconNodes.put("repository", new HTIconNode("repository"));

        initMap = new HashMap<>();
        initMap.put("archive", "repository");
        initMap.put("collection", "collection");
        initMap.put("inventory", "inventory");
        iconNodes.put("note", new HTIconNode("seg", null, "function", initMap));
//      Genre
        iconNodes.put("taxonomy", new HTIconNode("taxonomy"));
        iconNodes.put("category", new HTIconNode("category"));
        iconNodes.put("catRef", new HTIconNode("catRef", "category"));
//      Relations
        iconNodes.put("listRelation", new HTIconNode("listRelation"));
        initMap = new HashMap<>();
        initMap.put("spouse", "relation_spouse");
        initMap.put("parent", "relation_parent");
        iconNodes.put("relation", new HTIconNode("relation", "relation", "name", initMap));
//      Events
        initMap = new HashMap<>();
        initMap.put("marriage", "event_marriage");
        iconNodes.put("event", new HTIconNode("event", "event", "type", initMap));
//      Bibl
        iconNodes.put("bibl", new HTIconNode("bibl"));
        iconNodes.put("biblStruct", new HTIconNode("biblStruct", "bibl"));
        iconNodes.put("biblFull", new HTIconNode("biblFull", "bibl"));
    }

    @Override
    public BasicRenderingInformation getRenderingInformation(NodeRendererCustomizerContext context) {
        BasicRenderingInformation renderingInfo = new BasicRenderingInformation();

        HTIconNode iconNode = iconNodes.get(context.getNodeName());
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
                Icon icon = Icon.get(iconName);
                if (icon != null) {
                    renderingInfo.setIconPath(icon.getPath());
                }
            }
        }
        return renderingInfo;
    }

    @Override
    public String getDescription() {
        return "HisTEI Node Renderer Customizer";
    }

}

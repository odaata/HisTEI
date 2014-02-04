package eu.emergingstandards.contextual_info;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ro.sync.ecss.extensions.api.node.AuthorNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by mike on 2/3/14.
 */
public class EMSTContextualElementProperties {

    private static final Map<String, EMSTContextualElementProperties> ELEMENTS = new HashMap<>();

    static {
        List<EMSTContextualElementProperties> properties = new ArrayList<>();

//        persName element
        properties.add(new EMSTContextualElementProperties(
                "persName", EMSTContextualType.PERSON, "ref", "person"
        ));

//        handNote element
        properties.add(new EMSTContextualElementProperties(
                "handNote", EMSTContextualType.PERSON, "scribeRef", "person"
        ));

//        placeName element
        properties.add(new EMSTContextualElementProperties(
                "placeName", EMSTContextualType.PLACE, "ref", "place"
        ));

//        place elements - they are always displayed with a combo box, i.e. no sourceParent
        EMSTContextualElementProperties props = new EMSTContextualElementProperties(
                "district", EMSTContextualType.PLACE, "ref", ""
        );
        properties.add(props);
        properties.add(new EMSTContextualElementProperties("settlement", props));
        properties.add(new EMSTContextualElementProperties("region", props));
        properties.add(new EMSTContextualElementProperties("country", props));
        properties.add(new EMSTContextualElementProperties("bloc", props));

//        orgName element
        properties.add(new EMSTContextualElementProperties(
                "orgName", EMSTContextualType.ORGANIZATION, "ref", "org"
        ));

//        repository element
        properties.add(new EMSTContextualElementProperties(
                "repository", EMSTContextualType.ORGANIZATION, "ref", ""
        ));

//        genre element
        properties.add(new EMSTContextualElementProperties(
                "catRef", EMSTContextualType.GENRE, "target", ""
        ));

//        Store the properties in the HashMap with elementName as the key
        for (EMSTContextualElementProperties p : properties) {
            ELEMENTS.put(p.getElementName(), p);
        }
    }

    @NotNull
    public static Map<String, EMSTContextualElementProperties> getElements() {
        return ELEMENTS;
    }

    @Nullable
    public static EMSTContextualElementProperties get(AuthorNode authorNode) {
        if (authorNode != null)
            return ELEMENTS.get(authorNode.getName());
        else
            return null;
    }

    @NotNull
    public static Map<String, EMSTContextualElementProperties> filter(EMSTContextualType type) {
        Map<String, EMSTContextualElementProperties> elements = new HashMap<>();

        if (type != null) {
            for (EMSTContextualElementProperties props : ELEMENTS.values()) {
                if (props.type == type) {
                    elements.put(props.elementName, props);
                }
            }
        }
        return elements;
    }

    /* Instance Members */

    private String elementName;
    private EMSTContextualType type;
    private String refAttributeName;
    private String sourceParent;

    private EMSTContextualElementProperties(String elementName, EMSTContextualType type,
                                            String refAttributeName, String sourceParent) {
        this.elementName = elementName;
        this.type = type;
        this.refAttributeName = refAttributeName;
        this.sourceParent = sourceParent;
    }

    private EMSTContextualElementProperties(String elementName, EMSTContextualElementProperties props) {
        this.elementName = elementName;
        this.type = props.getType();
        this.refAttributeName = props.getRefAttributeName();
        this.sourceParent = props.getSourceParent();
    }

    private EMSTContextualElementProperties(Map<String, String> props) {
        this.elementName = props.get("elementName");
        this.type = EMSTContextualType.get(props.get("type"));
        this.refAttributeName = props.get("refAttributeName");
        this.sourceParent = props.get("sourceParent");
    }

    @NotNull
    public String getElementName() {
        return elementName;
    }

    @NotNull
    public EMSTContextualType getType() {
        return type;
    }

    @NotNull
    public String getRefAttributeName() {
        return refAttributeName;
    }

    @NotNull
    public String getSourceParent() {
        return sourceParent;
    }
}

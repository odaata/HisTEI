package eu.emergingstandards.contextual_info;

import eu.emergingstandards.utils.EMSTUtils;
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

//        Base elements with source Parents (i.e. where they aren't displayed as combo boxes)
//        persName element
        properties.add(new EMSTContextualElementProperties(
                "persName", EMSTContextualType.PERSON, "ref", "person", null));

//        placeName element
        properties.add(new EMSTContextualElementProperties(
                "placeName", EMSTContextualType.PLACE, "ref", "place", null));

//        orgName element
        properties.add(new EMSTContextualElementProperties(
                "orgName", EMSTContextualType.ORGANIZATION, "ref", "org", null));

//        People-related references

//        handNote element - no filter (as of yet)
        properties.add(new EMSTContextualElementProperties(
                "handNote", EMSTContextualType.PERSON, "scribeRef"));


//        Place-related references - they are always displayed with a combo box, i.e. no sourceParent

//        place elements with syntactic sugar, i.e. filter = element name
        EMSTContextualElementProperties props = new EMSTContextualElementProperties(
                "district", EMSTContextualType.PLACE, "ref", null, "district");
        properties.add(props);
        properties.add(new EMSTContextualElementProperties("settlement", props));
        properties.add(new EMSTContextualElementProperties("region", props));
        properties.add(new EMSTContextualElementProperties("country", props));
        properties.add(new EMSTContextualElementProperties("bloc", props));


//        Org-related references

//        repository element - same syntactic sugar as places (though I don't think the TEI necessarily condones this)
        properties.add(new EMSTContextualElementProperties(
                "repository", EMSTContextualType.ORGANIZATION, "ref", null, "repository"));


//        Genre-related references - different from other lists - linked to taxonomy

//        catRef element
        properties.add(new EMSTContextualElementProperties(
                "catRef", EMSTContextualType.GENRE, "target", null, "EMST_GENRES"));


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
    public static Map<String, EMSTContextualElementProperties> filter(EMSTContextualType contextualType) {
        Map<String, EMSTContextualElementProperties> elements = new HashMap<>();

        if (contextualType != null) {
            for (EMSTContextualElementProperties props : ELEMENTS.values()) {
                if (props.contextualType == contextualType) {
                    elements.put(props.elementName, props);
                }
            }
        }
        return elements;
    }

    /* Instance Members */

    private String elementName;
    private EMSTContextualType contextualType;
    private String refAttributeName;
    private String sourceParent;
    private String typeFilter;

    protected EMSTContextualElementProperties(String elementName, EMSTContextualType contextualType, String refAttributeName,
                                              String sourceParent, String typeFilter) {
        this.elementName = elementName;
        this.contextualType = contextualType;
        this.refAttributeName = refAttributeName;
        this.sourceParent = EMSTUtils.nullToEmpty(sourceParent);
        this.typeFilter = EMSTUtils.nullToEmpty(typeFilter);
    }

    private EMSTContextualElementProperties(String elementName, EMSTContextualType contextualType, String refAttributeName) {
        this(elementName, contextualType, refAttributeName, null, null);
    }

    private EMSTContextualElementProperties(String elementName, EMSTContextualElementProperties props) {
        this(elementName, props.getContextualType(), props.getRefAttributeName(),
                props.getSourceParent(), elementName);
    }

    /*private EMSTContextualElementProperties(Map<String, String> props) {
        this.elementName = props.get("elementName");
        this.contextualType = EMSTContextualType.get(props.get("contextualType"));
        this.refAttributeName = props.get("refAttributeName");
        this.sourceParent = props.get("sourceParent");
        this.typeFilter = props.get("typeFilter");
    }*/

    @NotNull
    public String getElementName() {
        return elementName;
    }

    @NotNull
    public EMSTContextualType getContextualType() {
        return contextualType;
    }

    @NotNull
    public String getRefAttributeName() {
        return refAttributeName;
    }

    @NotNull
    public String getSourceParent() {
        return sourceParent;
    }

    @NotNull
    public String getTypeFilter() {
        return typeFilter;
    }
}

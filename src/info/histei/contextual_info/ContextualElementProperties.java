package info.histei.contextual_info;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ro.sync.ecss.extensions.api.node.AuthorNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static info.histei.utils.MainUtils.nullToEmpty;

/**
 * Created by mike on 2/3/14.
 */
public class ContextualElementProperties {

    private static final Map<String, ContextualElementProperties> ELEMENTS = new HashMap<>();

    static {
        List<ContextualElementProperties> properties = new ArrayList<>();

//        Base elements with source Parents (i.e. where they aren't displayed as combo boxes)
//        persName element
        properties.add(new ContextualElementProperties(
                "persName", ContextualType.PERSON, "ref", "person", null));

//        placeName element
        properties.add(new ContextualElementProperties(
                "placeName", ContextualType.PLACE, "ref", "place", null));

//        orgName element
        properties.add(new ContextualElementProperties(
                "orgName", ContextualType.ORGANIZATION, "ref", "org", null));

//        People-related references

//        handNote element - no filter (as of yet)
        properties.add(new ContextualElementProperties(
                "handNote", ContextualType.PERSON, "scribeRef"));


//        Place-related references - they are always displayed with a combo box, i.e. no sourceParent

//        place elements with syntactic sugar, i.e. filter = element name
        ContextualElementProperties props = new ContextualElementProperties(
                "district", ContextualType.PLACE, "ref", null, "district");
        properties.add(props);
        properties.add(new ContextualElementProperties("settlement", props));
        properties.add(new ContextualElementProperties("region", props));
        properties.add(new ContextualElementProperties("country", props));
        properties.add(new ContextualElementProperties("bloc", props));

//        Org-related references

//        repository element - same syntactic sugar as places (though I don't think the TEI necessarily condones this)
        properties.add(new ContextualElementProperties(
                "repository", ContextualType.ORGANIZATION, "ref", null, "repository"));


//        Genre-related references - different from other lists - linked to taxonomy

//        catRef element
        properties.add(new ContextualElementProperties(
                "catRef", ContextualType.GENRE, "target", null, "EMST_GENRES"));


//        Store the properties in the HashMap with elementName as the key
        for (ContextualElementProperties p : properties) {
            ELEMENTS.put(p.getElementName(), p);
        }
    }

    @NotNull
    public static Map<String, ContextualElementProperties> getElements() {
        return ELEMENTS;
    }

    @Nullable
    public static ContextualElementProperties get(AuthorNode authorNode) {
        if (authorNode != null) {
            return ELEMENTS.get(authorNode.getName());
        } else {
            return null;
        }
    }

    @NotNull
    public static Map<String, ContextualElementProperties> filter(ContextualType contextualType) {
        Map<String, ContextualElementProperties> elements = new HashMap<>();

        if (contextualType != null) {
            for (ContextualElementProperties props : ELEMENTS.values()) {
                if (props.contextualType == contextualType) {
                    elements.put(props.elementName, props);
                }
            }
        }
        return elements;
    }

    /* Instance Members */

    private String elementName;
    private ContextualType contextualType;
    private String refAttributeName;
    private String sourceParent;
    private String typeFilter;

    protected ContextualElementProperties(String elementName, ContextualType contextualType, String refAttributeName,
                                          String sourceParent, String typeFilter) {
        this.elementName = elementName;
        this.contextualType = contextualType;
        this.refAttributeName = refAttributeName;
        this.sourceParent = nullToEmpty(sourceParent);
        this.typeFilter = nullToEmpty(typeFilter);
    }

    private ContextualElementProperties(String elementName, ContextualType contextualType, String refAttributeName) {
        this(elementName, contextualType, refAttributeName, null, null);
    }

    private ContextualElementProperties(String elementName, ContextualElementProperties props) {
        this(elementName, props.getContextualType(), props.getRefAttributeName(),
                props.getSourceParent(), elementName);
    }

    @NotNull
    public String getElementName() {
        return elementName;
    }

    @NotNull
    public ContextualType getContextualType() {
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

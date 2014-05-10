package info.histei.contextual_info;

import info.histei.commons.AbstractUniqueAttribute;
import info.histei.utils.MainUtils;
import info.histei.utils.XMLUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ro.sync.ecss.extensions.api.editor.AuthorInplaceContext;
import ro.sync.ecss.extensions.api.node.AuthorElement;
import ro.sync.ecss.extensions.api.node.AuthorNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mike on 5/9/14.
 */
public class ContextualAttribute extends AbstractUniqueAttribute<AuthorInplaceContext> {

    private static final List<ContextualAttribute> ATTRIBUTES = new ArrayList<>();

    static {
//        Base elements with source Parents (i.e. where they aren't displayed as combo boxes)
//        persName element
        ATTRIBUTES.add(new ContextualAttribute("ref", "persName", ContextualType.PERSON));

//        placeName element
        ATTRIBUTES.add(new ContextualAttribute("ref", "placeName", ContextualType.PLACE));

//        orgName element
        ATTRIBUTES.add(new ContextualAttribute("ref", "orgName", ContextualType.ORGANIZATION));


//        People-related references

//        handNote element - no filter (as of yet)
        ATTRIBUTES.add(new ContextualAttribute("scribeRef", "handNote", ContextualType.PERSON));


//        Place-related references - they are always displayed with a combo box, i.e. no sourceParent

//        place elements with syntactic sugar, i.e. filter = element name
        ATTRIBUTES.add(new ContextualAttribute("ref", "district", ContextualType.PLACE, "district"));
        ATTRIBUTES.add(new ContextualAttribute("ref", "settlement", ContextualType.PLACE, "settlement"));
        ATTRIBUTES.add(new ContextualAttribute("ref", "region", ContextualType.PLACE, "region"));
        ATTRIBUTES.add(new ContextualAttribute("ref", "country", ContextualType.PLACE, "country"));
        ATTRIBUTES.add(new ContextualAttribute("ref", "bloc", ContextualType.PLACE, "bloc"));

//        Org-related references

//        repository element - same syntactic sugar as places (though I don't think the TEI necessarily condones this)
        ATTRIBUTES.add(new ContextualAttribute("ref", "repository", ContextualType.ORGANIZATION, "repository"));


//        Genre-related references - different from other lists - linked to taxonomy

//        catRef element
        ATTRIBUTES.add(new ContextualAttribute("target", "catRef", ContextualType.GENRE, "EMST_GENRES"));


//        Annotations-related references - linked to interGrp

//        cl element
        ATTRIBUTES.add(new ContextualAttribute("ana", "cl", ContextualType.ANNOTATION, "clause"));

//        phr element
        ATTRIBUTES.add(new ContextualAttribute("ana", "phr", ContextualType.ANNOTATION, "phrase"));
        ATTRIBUTES.add(new ContextualAttribute("function", "phr", ContextualType.ANNOTATION, "function"));

//        w element
        ATTRIBUTES.add(new ContextualAttribute("ana", "w", ContextualType.ANNOTATION, "pos"));

    }

    @Nullable
    public static ContextualAttribute get(AuthorInplaceContext context) {
        if (context != null) {
            for (ContextualAttribute attribute : ATTRIBUTES) {
                if (attribute.matches(context)) {
                    return attribute;
                }
            }
        }
        return null;
    }

    protected final ContextualType contextualType;
    protected final String typeFilter;

    protected ContextualAttribute(String attributeName, String elementName, String parentElementName,
                                  ContextualType contextualType, String typeFilter) {
        super(attributeName, elementName, parentElementName);
        this.contextualType = contextualType;
        this.typeFilter = MainUtils.nullToEmpty(typeFilter);
    }

    protected ContextualAttribute(String attributeName, String elementName,
                                  ContextualType contextualType, String typeFilter) {
        this(attributeName, elementName, null, contextualType, typeFilter);
    }

    protected ContextualAttribute(String attributeName, String elementName, ContextualType contextualType) {
        this(attributeName, elementName, null, contextualType, null);
    }

    @NotNull
    public ContextualType getContextualType() {
        return contextualType;
    }

    @NotNull
    public String getTypeFilter() {
        return typeFilter;
    }

    @Override
    public boolean matches(AuthorInplaceContext context) {
        if (context == null) return false;

        if (!attributeName.equals(XMLUtils.castQNameToXMLString(context.getAttributeToEditQName()))) return false;

        if (elementName != null) {
            AuthorElement authorElement = context.getElem();
            if (authorElement == null) return false;

            if (!elementName.equals(authorElement.getName())) return false;

            if (parentElementName != null) {
                AuthorNode parentNode = authorElement.getParent();
                if (parentNode == null || !parentElementName.equals(parentNode.getName())) return false;
            }
        }
        return true;
    }


}

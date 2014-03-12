package eu.emergingstandards.contextual_info;

import eu.emergingstandards.lists.styled.EMSTAbstractStyledList;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.node.AuthorElement;
import ro.sync.ecss.extensions.api.node.AuthorNode;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static eu.emergingstandards.utils.EMSTOxygenUtils.*;

/**
 * Created by mike on 2/3/14.
 */
public class EMSTContextualStyledList extends EMSTAbstractStyledList<EMSTContextualItem> {

    private static final Logger logger = Logger.getLogger(EMSTContextualStyledList.class.getName());

    //  Regexp info for retrieving IDs
    private static final String TYPE_REGEX = "(" + StringUtils.join(EMSTContextualType.getKeys(), "|") + ")";
    private static final String ID_REGEX = "(\\S+)";
    private static final Pattern REF_PATTERN = Pattern.compile(TYPE_REGEX + ":" + ID_REGEX);

    private static final Map<AuthorNode, EMSTContextualStyledList> authorNodes = new WeakHashMap<>();

    //  Author page is required, but can only be set when available (later in startup sequence)
//      Even if the page isn't available, ContextualElement serves the function of locating nodes that will need controls
//      And Oxygen needs this info as early as possible, so have to return an object even if page is not yet available
    @Nullable
    public static EMSTContextualStyledList get(AuthorNode authorNode) {
        EMSTContextualStyledList contextualStyledList;

        synchronized (authorNodes) {
            contextualStyledList = authorNodes.get(authorNode);
        }

        if (contextualStyledList == null) {
            EMSTContextualElementProperties elementProperties = EMSTContextualElementProperties.get(authorNode);
            if (elementProperties != null) {
                AuthorNode parent = authorNode.getParent();
                if (parent == null || !elementProperties.getSourceParent().equals(parent.getName())) {
                    EMSTContextualType contextualType = elementProperties.getContextualType();
                    EMSTContextualInfo info = EMSTContextualInfo.get(contextualType);

                    contextualStyledList =
                            new EMSTContextualStyledList(castAuthorElement(authorNode), info, elementProperties);

                    synchronized (authorNodes) {
                        authorNodes.put(authorNode, contextualStyledList);
                    }
                }
            }
        }

        if (contextualStyledList != null && contextualStyledList.getAuthorPage() == null) {
            contextualStyledList.setAuthorPage(getCurrentAuthorEditorPage());
        }
        return contextualStyledList;
    }

    @Nullable
    public static EMSTContextualStyledList get(AuthorAccess authorAccess) {
        return get(getCurrentAuthorNode(authorAccess));
    }

    /* Instance Members */

    private final EMSTContextualInfo contextualInfo;
    private final EMSTContextualElementProperties elementProperties;

    protected EMSTContextualStyledList(AuthorElement authorElement, EMSTContextualInfo contextualInfo,
                                       EMSTContextualElementProperties properties) {
        super(authorElement);
        this.contextualInfo = contextualInfo;
        this.elementProperties = properties;

        this.contextualInfo.addListener(this);
    }

    @NotNull
    @Override
    public List<EMSTContextualItem> getItems() {
        return contextualInfo.getItems(elementProperties.getTypeFilter());
    }

    @NotNull
    @Override
    public String getEditPropertyQualified() {
        return getRefAttributeName();
    }

    @NotNull
    public String getRefAttributeName() {
        return elementProperties.getRefAttributeName();
    }

    @NotNull
    public String getRefID() {
        String id = "";
        AuthorElement element = getAuthorElement();

        if (element != null) {
            String value = getAttrValue(element.getAttribute(getRefAttributeName()));
            if (value != null) {
                Matcher matcher = REF_PATTERN.matcher(value);
                if (matcher.matches()) {
                    id = matcher.group(2);
                }
            }
        }
        return id;
    }

    @Nullable
    public URL getURL() {
        URL url = contextualInfo.getURL();

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

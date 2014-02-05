package eu.emergingstandards.contextual_info;

import eu.emergingstandards.utils.EMSTOxygenUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.node.AuthorElement;
import ro.sync.ecss.extensions.api.node.AuthorNode;
import ro.sync.exml.workspace.api.editor.page.author.WSAuthorEditorPage;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by mike on 2/3/14.
 */
public class EMSTContextualElement {

    private static final Logger logger = Logger.getLogger(EMSTContextualElement.class.getName());

    //  Regexp info for retrieving IDs
    private static final String TYPE_REGEX = "(" + StringUtils.join(EMSTContextualType.getKeys(), "|") + ")";
    private static final String ID_REGEX = "(\\S+)";
    public static final Pattern REF_PATTERN = Pattern.compile(TYPE_REGEX + ":" + ID_REGEX);

    //    private static final Map<AuthorNode, WeakReference<EMSTContextualElement>> authorNodes = new WeakHashMap<>();
    private static final Map<AuthorNode, EMSTContextualElement> authorNodes = new WeakHashMap<>();

    @Nullable
    public static synchronized EMSTContextualElement get(AuthorNode authorNode) {
        EMSTContextualElement contextualElement;

        synchronized (authorNodes) {
            contextualElement = getElement(authorNode);

            if (contextualElement == null) {
                EMSTContextualElementProperties elementProperties = EMSTContextualElementProperties.get(authorNode);
                if (elementProperties != null) {
                    AuthorNode parent = authorNode.getParent();
                    if (parent == null || !elementProperties.getSourceParent().equals(parent.getName())) {
                        EMSTContextualInfo info = EMSTContextualInfo.get(elementProperties.getType());

                        if (info != null) {
                            contextualElement = new EMSTContextualElement(info, authorNode, elementProperties);

//                        authorNodes.put(authorNode, new WeakReference<>(contextualElement));
                            authorNodes.put(authorNode, contextualElement);
                        }
                    }
                }
            }
        }
        return contextualElement;
    }

    @Nullable
    public static EMSTContextualElement get(AuthorAccess authorAccess) {
        AuthorNode currentNode = EMSTOxygenUtils.getCurrentAuthorNode(authorAccess);

        if (currentNode != null) {
            return get(currentNode);
        } else {
            return null;
        }
    }

    @Nullable
    public static EMSTContextualElement getElement(AuthorNode authorNode) {
        EMSTContextualElement contextualElement = null;

        if (authorNode != null) {
            contextualElement = authorNodes.get(authorNode);
            /*WeakReference<EMSTContextualElement> ref;
            synchronized (authorNodes) {
                ref = authorNodes.get(authorNode);
            }

            if (ref != null) {
                contextualElement = ref.get();
            }*/
        }
        return contextualElement;
    }

    @NotNull
    public static Map<AuthorNode, EMSTContextualElement> getAuthorNodes() {
        Map<AuthorNode, EMSTContextualElement> nodes = new WeakHashMap<>();

        synchronized (authorNodes) {
            for (AuthorNode authorNode : authorNodes.keySet()) {
//                nodes.put(authorNode, authorNodes.get(authorNode).get());
                nodes.put(authorNode, authorNodes.get(authorNode));
            }
        }
        return nodes;
    }

    @NotNull
    public static Map<AuthorNode, EMSTContextualElement> getAuthorNodes(EMSTContextualType type) {
        Map<AuthorNode, EMSTContextualElement> nodes = getAuthorNodes();

        if (type != null) {
            Map<AuthorNode, EMSTContextualElement> filteredNodes = new WeakHashMap<>();
            EMSTContextualElement element;

            synchronized (authorNodes) {
                for (AuthorNode authorNode : authorNodes.keySet()) {
                    element = nodes.get(authorNode);
                    if (element != null && element.getType() == type) {
                        filteredNodes.put(authorNode, element);
                    }
                }
            }
            nodes = filteredNodes;
        }
        return nodes;
    }

    public static void refreshAuthorNodes(WSAuthorEditorPage page, EMSTContextualType type) {
        if (page != null) {
            for (AuthorNode authorNode : getAuthorNodes(type).keySet()) {
                page.refresh(authorNode);
            }
        }
    }

    /* Instance Members */

    private EMSTContextualType type;
    private EMSTContextualInfo contextualInfo;
    private AuthorElement authorElement;
    private EMSTContextualElementProperties elementProperties;

    protected EMSTContextualElement(EMSTContextualInfo contextualInfo,
                                    AuthorNode authorNode,
                                    EMSTContextualElementProperties properties) {

        this.contextualInfo = contextualInfo;
        this.type = properties.getType();
        this.authorElement = EMSTOxygenUtils.castAuthorElement(authorNode);
        this.elementProperties = properties;
    }

    @NotNull
    public EMSTContextualType getType() {
        return type;
    }

    @NotNull
    public EMSTContextualInfo getContextualInfo() {
        return contextualInfo;
    }

    @NotNull
    public AuthorElement getAuthorElement() {
        return authorElement;
    }

    @NotNull
    public EMSTContextualElementProperties getElementProperties() {
        return elementProperties;
    }

    @NotNull
    public String getRefAttributeName() {
        return elementProperties.getRefAttributeName();
    }

    @NotNull
    public String getRefID() {
        String id = "";

        String value = EMSTOxygenUtils.getAttrValue(authorElement.getAttribute(getRefAttributeName()));
        if (value != null) {
            Matcher matcher = REF_PATTERN.matcher(value);
            if (matcher.matches()) {
                id = matcher.group(2);
            }
        }
        return id;
    }

    @Nullable
    public URL getURL() {
        return getURL(false);
    }

    @Nullable
    public URL getURL(boolean appendID) {
        URL url = null;

        Path srcPath = contextualInfo.getSourcePath();
        if (srcPath != null) {
            try {
                url = srcPath.toUri().toURL();

                if (appendID) {
                    String id = getRefID();
                    if (!id.isEmpty())
                        url = new URL(srcPath.toUri().toURL(), "#" + id);
                }
            } catch (MalformedURLException e) {
                logger.error(e, e);
            }
        }
        return url;
    }

    @NotNull
    @Deprecated
    public String getEditProperty() {
        String editProperty = "";

        String refAttributeName = getRefAttributeName();
        if (!refAttributeName.isEmpty()) editProperty = "@" + refAttributeName;

        return editProperty;
    }

    @NotNull
    public String getEditPropertyQualified() {
        return getRefAttributeName();
    }

    @NotNull
    public String getOxygenValues() {
        return StringUtils.join(contextualInfo.getValues(), ",");
    }

    @NotNull
    public String getOxygenLabels() {
        return StringUtils.join(EMSTOxygenUtils.escapeCommas(contextualInfo.getLabels()), ",");
    }

    @NotNull
    public Pattern getRefPattern() {
        return REF_PATTERN;
    }
}

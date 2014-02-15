package eu.emergingstandards.contextual_info;

import eu.emergingstandards.events.EMSTRefreshEventListener;
import eu.emergingstandards.utils.EMSTOxygenUtils;
import eu.emergingstandards.utils.EMSTUtils;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static eu.emergingstandards.utils.EMSTOxygenUtils.*;

/**
 * Created by mike on 2/3/14.
 */
public class EMSTContextualElement implements EMSTRefreshEventListener {

    private static final Logger logger = Logger.getLogger(EMSTContextualElement.class.getName());

    //  Regexp info for retrieving IDs
    private static final String TYPE_REGEX = "(" + StringUtils.join(EMSTContextualType.getKeys(), "|") + ")";
    private static final String ID_REGEX = "(\\S+)";
    public static final Pattern REF_PATTERN = Pattern.compile(TYPE_REGEX + ":" + ID_REGEX);

    private static final Map<AuthorNode, EMSTContextualElement> authorNodes = new WeakHashMap<>();


    @Nullable
    public static EMSTContextualElement get(AuthorAccess authorAccess) {
        return get(getCurrentAuthorNode(authorAccess));
    }


    //  Author page is required, but can only be set when available (later in startup sequence)
//      Even if the page isn't available, ContextualElement serves the function of locating nodes that will need controls
//      And Oxygen needs this info as early as possible, so have to return an object even if page is not yet available
    @Nullable
    public static EMSTContextualElement get(AuthorNode authorNode) {
        EMSTContextualElement contextualElement;

        synchronized (authorNodes) {
            contextualElement = authorNodes.get(authorNode);
        }

        if (contextualElement == null) {
            EMSTContextualElementProperties elementProperties = EMSTContextualElementProperties.get(authorNode);
            if (elementProperties != null) {
                AuthorNode parent = authorNode.getParent();
                if (parent == null || !elementProperties.getSourceParent().equals(parent.getName())) {
                    EMSTContextualInfo info = EMSTContextualInfo.get(elementProperties.getContextualType());

                    contextualElement = new EMSTContextualElement(authorNode, info, elementProperties);

                    synchronized (authorNodes) {
                        authorNodes.put(authorNode, contextualElement);
                    }
                }
            }
        }

        if (contextualElement != null && contextualElement.authorPage == null) {
            contextualElement.authorPage = EMSTOxygenUtils.getCurrentAuthorEditorPage();
        }
        return contextualElement;
    }

    /* Instance Members */

    private WSAuthorEditorPage authorPage;
    private final AuthorElement authorElement;

    private final EMSTContextualType contextualType;
    private final EMSTContextualInfo contextualInfo;
    private final EMSTContextualElementProperties elementProperties;

    protected EMSTContextualElement(AuthorNode authorNode, EMSTContextualInfo contextualInfo,
                                    EMSTContextualElementProperties properties) {

        this.authorElement = castAuthorElement(authorNode);
        this.contextualType = properties.getContextualType();
        this.contextualInfo = contextualInfo;
        this.elementProperties = properties;

        contextualInfo.addListener(this);
    }

    @NotNull
    public WSAuthorEditorPage getAuthorPage() {
        return authorPage;
    }

    public void setAuthorPage(WSAuthorEditorPage newPage) {
        this.authorPage = newPage;
    }

    @NotNull
    public AuthorElement getAuthorElement() {
        return authorElement;
    }

    @NotNull
    public EMSTContextualType getContextualType() {
        return contextualType;
    }

    @NotNull
    public EMSTContextualInfo getContextualInfo() {
        return contextualInfo;
    }

    @NotNull
    public EMSTContextualElementProperties getElementProperties() {
        return elementProperties;
    }

    @NotNull
    public Pattern getRefPattern() {
        return REF_PATTERN;
    }

    @NotNull
    public String getRefAttributeName() {
        return elementProperties.getRefAttributeName();
    }

    @NotNull
    public String getRefID() {
        String id = "";

        String value = getAttrValue(authorElement.getAttribute(getRefAttributeName()));
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
                url = EMSTUtils.castPathToURL(srcPath);

                if (appendID) {
                    String id = getRefID();
                    if (!id.isEmpty())
                        url = new URL(url, "#" + id);
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
    public List<EMSTContextualItem> getItems() {
        return contextualInfo.getItems(elementProperties.getTypeFilter());
    }

    @NotNull
    public String getOxygenValues() {
        List<String> values = new ArrayList<>();

        for (EMSTContextualItem item : getItems()) {
            values.add(item.getValue());
        }
        return StringUtils.join(values, ",");
    }

    @NotNull
    public String getOxygenLabels() {
        List<String> labels = new ArrayList<>();

        for (EMSTContextualItem item : getItems()) {
            labels.add(item.getLabel());
        }
        return StringUtils.join(escapeCommas(labels), ",");
    }

    @NotNull
    public String getOxygenTooltips() {
        List<String> tooltips = new ArrayList<>();

        for (EMSTContextualItem item : getItems()) {
            tooltips.add(item.getTooltip());
        }
        return StringUtils.join(escapeCommas(tooltips), ",");
    }

    @Override
    public void refresh() {
        if (authorPage != null && authorElement != null) {
            authorPage.refresh(authorElement);
        }
    }
}

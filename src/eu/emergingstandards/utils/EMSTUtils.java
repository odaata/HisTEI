package eu.emergingstandards.utils;

import eu.emergingstandards.exceptions.EMSTFileMissingException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ro.sync.ecss.dom.wrappers.AuthorElementDomWrapper;
import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.AuthorOperationException;
import ro.sync.ecss.extensions.api.node.AttrValue;
import ro.sync.ecss.extensions.api.node.AuthorElement;
import ro.sync.ecss.extensions.api.node.AuthorNode;
import ro.sync.exml.workspace.api.PluginWorkspace;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;
import ro.sync.exml.workspace.api.editor.WSEditor;
import ro.sync.exml.workspace.api.editor.page.WSEditorPage;
import ro.sync.exml.workspace.api.editor.page.author.WSAuthorEditorPage;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Created by mike on 1/13/14.
 */
public class EMSTUtils {

    public static String XML_ID_ATTR_NAME = "xml:id";
    public static String TEI_NAMESPACE = "http://www.tei-c.org/ns/1.0";
    public static String XML_NAMESPACE = "http://www.w3.org/XML/1998/namespace";
    public static Map<String, String> NAMESPACES = new HashMap<>(2);
    static {
        NAMESPACES.put("tei", TEI_NAMESPACE);
        NAMESPACES.put("xml", XML_NAMESPACE);
    }

    @NotNull
    public static String escapeComma(String value) {
        return value.replace(",", "${comma}");
    }

    @NotNull
    public static List<String> escapeCommas(List<String> values) {
        boolean hasValues = (values != null);
        List<String> escapedValues =
                new ArrayList<>(hasValues ? values.size() : 0);

        if (hasValues) {
            for (String value : values) {
                escapedValues.add(escapeComma(value));
            }
        }
        return escapedValues;
    }

    @Nullable
    public static WSAuthorEditorPage getCurrentAuthorEditorPage() {
        WSAuthorEditorPage page = null;
        WSEditor wsEditor =
                PluginWorkspaceProvider.getPluginWorkspace().getCurrentEditorAccess(PluginWorkspace.MAIN_EDITING_AREA);
        if (wsEditor != null) {
            WSEditorPage currentPage = wsEditor.getCurrentPage();
            if (currentPage instanceof WSAuthorEditorPage) {
                page = (WSAuthorEditorPage) currentPage;
            }
        }
        return page;
    }

    @Nullable
    public static AuthorAccess getCurrentAuthorAccess() {
        AuthorAccess access = null;
        WSAuthorEditorPage page = getCurrentAuthorEditorPage();
        if (page != null) {
            access = page.getAuthorAccess();
        }
        return access;
    }

    @Nullable
    public static List<AuthorNode> getAuthorNodes(String xpath, AuthorAccess authorAccess) {
        List<AuthorNode> authorNodes = new ArrayList<>();

        Object[] results = null;
        try {
            if (authorAccess != null) {
                results = authorAccess.getDocumentController().evaluateXPath(xpath, false, false, false, false);
                if (results != null && results.length > 0) {
                    for (Object node : results) {
                        authorNodes.add(((AuthorElementDomWrapper) node).getWrappedAuthorNode());
                    }
                }
            }
        } catch (AuthorOperationException e) {
        }
        return authorNodes;
    }

    @Nullable
    public static AuthorNode getAuthorNode(String xpath, AuthorAccess authorAccess) {
        AuthorNode authorNode = null;

        List<AuthorNode> nodes = getAuthorNodes(xpath, authorAccess);
        if (nodes.size() > 0) {
            authorNode = nodes.get(0);
        }
        return authorNode;
    }

    @Nullable
    public static AuthorNode getCurrentAuthorNode(AuthorAccess authorAccess) {
        return getAuthorNode(".", authorAccess);
    }

    @Nullable
    public static AuthorElement castAuthorElement(AuthorNode authorNode) {
        AuthorElement authorElement = null;

        if (authorNode != null && authorNode.getType() == AuthorNode.NODE_TYPE_ELEMENT) {
            authorElement = (AuthorElement) authorNode;
        }
        return authorElement;
    }

    @Nullable
    public static AuthorElement getAuthorElement(String xpath, AuthorAccess authorAccess) {
        return castAuthorElement(getAuthorNode(xpath, authorAccess));
    }

    @Nullable
    public static AuthorElement getCurrentAuthorElement(AuthorAccess authorAccess) {
        return getAuthorElement(".", authorAccess);
    }

    @NotNull
    public static List<AuthorElement> getContentElements(AuthorElement authorElement) {
        List<AuthorElement> elements = new ArrayList<>();

        if (authorElement != null) {
            for (AuthorNode node : authorElement.getContentNodes()) {
                AuthorElement element = EMSTUtils.castAuthorElement(node);
                if (element != null) {
                    elements.add(element);
                }
            }
        }
        return elements;
    }

    @Nullable
    public static Path expandOxygenPath(String originalPath, AuthorAccess authorAccess) {
        Path path = null;

        if (authorAccess != null) {
            path = Paths.get(authorAccess.getUtilAccess().expandEditorVariables(
                    originalPath, authorAccess.getEditorAccess().getEditorLocation()));

            if (path != null && !Files.exists(path)) path = null;
        }
        return path;
    }

    public static void openURL(AuthorAccess authorAccess, URL url) throws EMSTFileMissingException {
        if (url != null) {
            Path path = castURLtoPath(url);
            if (path != null) {
                if (!Files.exists(path)) {
                    throw new EMSTFileMissingException("The file could not be found!", url.toString());
                } else {
                    authorAccess.getWorkspaceAccess().openInExternalApplication(url, true);
                }
            }
        }
    }

    @NotNull
    public static List<String> getAttribValues(String value) {
        List<String> values = new ArrayList<>();

        if (value != null) {
            values = Arrays.asList(value.split("\\s+"));
        }
        return values;
    }

    @Nullable
    public static String getAttrValue(AttrValue attrValue) {
        String value = null;

        if (attrValue != null) {
            value = attrValue.getValue().trim();

            if (value.isEmpty()) value = null;
        }
        return value;
    }

    @Nullable
    public static Path castURLtoPath(URL url) {
        Path path = null;

        if (url != null) {
            try {
                String urlPath = URLDecoder.decode(url.getPath(), "UTF-8");
                path = Paths.get(urlPath);
            } catch (UnsupportedEncodingException e) {

            }
        }

        return path;
    }
}

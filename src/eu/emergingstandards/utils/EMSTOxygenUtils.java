package eu.emergingstandards.utils;

import eu.emergingstandards.exceptions.EMSTFileMissingException;
import org.apache.log4j.Logger;
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

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static eu.emergingstandards.utils.EMSTUtils.castURLToPath;
import static eu.emergingstandards.utils.EMSTUtils.emptyToNull;

/**
 * Created by mike on 1/13/14.
 */
public final class EMSTOxygenUtils {

    private static final Logger logger = Logger.getLogger(EMSTOxygenUtils.class.getName());

    public static final String XML_ID_ATTRIB_NAME = "xml:id";
    public static final String XML_BASE_ATTRIB_NAME = "xml:base";

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

        Object[] results;
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
            logger.error(e, e);
        }
        return authorNodes;
    }

    @Nullable
    public static AuthorNode getAuthorNode(String xpath, AuthorAccess authorAccess) {
        AuthorNode authorNode = null;

        List<AuthorNode> nodes = getAuthorNodes(xpath, authorAccess);
        if (nodes != null && nodes.size() > 0) {
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
                AuthorElement element = castAuthorElement(node);
                if (element != null) {
                    elements.add(element);
                }
            }
        }
        return elements;
    }

    @NotNull
    public static List<String> getAttrValues(AttrValue attrValue) {
        List<String> values = new ArrayList<>();

        String value = getAttrValue(attrValue);
        if (value != null) {
            values = Arrays.asList(value.split("\\s+"));
        }
        return values;
    }

    @Nullable
    public static String getAttrValue(AttrValue attrValue) {
        String value = null;

        if (attrValue != null) {
            value = emptyToNull(attrValue.getValue());
        }
        return value;
    }

    @Nullable
    public static Path expandOxygenPath(String originalPath, AuthorAccess authorAccess) {
        Path path = null;

        if (authorAccess != null) {
            path = Paths.get(authorAccess.getUtilAccess().expandEditorVariables(
                    originalPath, authorAccess.getEditorAccess().getEditorLocation()));

//            if (path != null && !Files.exists(path)) path = null;
        }
        return path;
    }

    @Nullable
    public static String makeRelative(AuthorAccess authorAccess, URL url) {
        String relativePath = null;

        if (authorAccess != null) {
            relativePath = emptyToNull(
                    authorAccess.getUtilAccess().makeRelative(
                            authorAccess.getEditorAccess().getEditorLocation(), url)
            );
        }
        return relativePath;
    }

    public static void openURL(AuthorAccess authorAccess, URL url) throws EMSTFileMissingException {
        if (url != null) {
            Path path = castURLToPath(url);
            if (path != null) {
                if (!Files.exists(path)) {
                    throw new EMSTFileMissingException("The file could not be found!", url);
                } else {
                    authorAccess.getWorkspaceAccess().openInExternalApplication(url, true);
                }
            }
        }
    }

    public static void showErrorMessage(AuthorAccess authorAccess, String message) {
        if (authorAccess != null) {
            authorAccess.getWorkspaceAccess().showErrorMessage(message);
        }
    }

    private EMSTOxygenUtils() {
    }

}

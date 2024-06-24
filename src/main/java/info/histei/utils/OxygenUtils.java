package info.histei.utils;

import info.histei.exceptions.HTFileMissingException;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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

import javax.swing.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static info.histei.utils.MainUtils.castURLToPath;

/**
 * Created by mike on 1/13/14.
 */
public final class OxygenUtils {

    private static final Logger logger = LogManager.getLogger(OxygenUtils.class.getName());

    @NotNull
    public static String escapeComma(String value) {
        String escaped = StringUtils.trimToNull(value);

        if (escaped != null) {
            return escaped.replace(",", "${comma}");
        } else {
            return "";
        }
    }

    @NotNull
    public static List<String> escapeCommas(List<String> values) {
        List<String> escapedValues = new ArrayList<>();

        if (values != null) {
            for (String value : values) {
                if (value != null) {
                    escapedValues.add(escapeComma(value));
                }
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
    public static List<AuthorElement> getAuthorElements(String xpath, AuthorAccess authorAccess) {
        List<AuthorElement> authorElements = new ArrayList<>();

        List<AuthorNode> authorNodes = getAuthorNodes(xpath, authorAccess);
        if (authorNodes != null) {
            for (AuthorNode node : authorNodes) {
                AuthorElement element = castAuthorElement(node);
                if (element != null) {
                    authorElements.add(element);
                }
            }
        }
        return authorElements;
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
            value = StringUtils.trimToNull(attrValue.getValue());
        }
        return value;
    }

    @Nullable
    public static Path expandOxygenPath(String originalPath, AuthorAccess authorAccess) {
        Path path = null;

        if (authorAccess != null) {
            path = Paths.get(authorAccess.getUtilAccess().expandEditorVariables(
                    originalPath, getCurrentEditorLocation()));
        }
        return path;
    }

    @Nullable
    public static URL getEditorLocation(AuthorAccess authorAccess) {
        return authorAccess.getEditorAccess().getEditorLocation();
    }

    @Nullable
    public static URL getCurrentEditorLocation() {
    	URL editorLocation = null;
        WSEditor currentEditorAccess = PluginWorkspaceProvider.getPluginWorkspace().getCurrentEditorAccess(PluginWorkspace.MAIN_EDITING_AREA);
        if (currentEditorAccess != null) {
        	editorLocation = currentEditorAccess.getEditorLocation();
        }
		return editorLocation;
    }

    @Nullable
    public static String makeRelative(AuthorAccess authorAccess, URL baseURL, URL childURL) {
        String relativePath = null;

        if (authorAccess != null) {
            relativePath = StringUtils.trimToNull(authorAccess.getUtilAccess().makeRelative(baseURL, childURL));
            if (relativePath == null || relativePath.equals(".")) {
                relativePath = null;
            }
        }
        return relativePath;
    }

    @Nullable
    public static String makeRelativeToCurrentEditor(AuthorAccess authorAccess, URL url) {
        String relativePath = null;

        if (authorAccess != null) {
            relativePath = makeRelative(authorAccess, authorAccess.getEditorAccess().getEditorLocation(), url);
        }
        return relativePath;
    }

    public static void openURL(AuthorAccess authorAccess, URL url) throws HTFileMissingException {
        if (url != null) {
            Path path = castURLToPath(url);
            if (path != null) {
                if (!Files.exists(path)) {
                    throw new HTFileMissingException("The file could not be found!", url);
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

    public static void refreshPage(final WSAuthorEditorPage page) {
        if (page != null) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    page.refresh();
                }
            });
        }
    }

    public static void refreshCurrentPage() {
        refreshPage(getCurrentAuthorEditorPage());
    }

    public static void refreshNode(final WSAuthorEditorPage page, final AuthorNode node) {
        if (page != null && node != null) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    page.refresh(node);
                }
            });
        }
    }

    public static void refreshCurrentNode() {
        refreshNode(getCurrentAuthorEditorPage(), getCurrentAuthorNode(getCurrentAuthorAccess()));
    }

    private OxygenUtils() {
    }

}

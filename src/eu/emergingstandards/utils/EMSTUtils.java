package eu.emergingstandards.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ro.sync.ecss.dom.wrappers.AuthorElementDomWrapper;
import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.AuthorOperationException;
import ro.sync.ecss.extensions.api.node.AuthorNode;
import ro.sync.exml.workspace.api.PluginWorkspace;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;
import ro.sync.exml.workspace.api.editor.WSEditor;
import ro.sync.exml.workspace.api.editor.page.WSEditorPage;
import ro.sync.exml.workspace.api.editor.page.author.WSAuthorEditorPage;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by mike on 1/13/14.
 */
public class EMSTUtils {

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
    public static Object[] evaluateXPath(String xpath, AuthorAccess authorAccess) {
        Object[] results = null;
        try {
            if (authorAccess != null)
                results = authorAccess.getDocumentController().evaluateXPath(xpath, false, false, false, false);
        } catch (AuthorOperationException e) {

        }
        return results;
    }

    @Nullable
    public static AuthorNode getCurrentAuthorNode(AuthorAccess authorAccess) {
        AuthorNode currentNode = null;
        try {
            Object[] xpathResults = evaluateXPath(".", authorAccess);
            if (xpathResults != null && xpathResults.length > 0) {
                currentNode = ((AuthorElementDomWrapper) xpathResults[0]).getWrappedAuthorNode();
            }
        } catch (Exception e) {
//            e.printStackTrace();
        }
        return currentNode;
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
}

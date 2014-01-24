package eu.emergingstandards.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.exml.workspace.api.PluginWorkspace;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;
import ro.sync.exml.workspace.api.editor.WSEditor;
import ro.sync.exml.workspace.api.editor.page.WSEditorPage;
import ro.sync.exml.workspace.api.editor.page.author.WSAuthorEditorPage;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by mike on 1/13/14.
 */
public class EMSTUtils {

    @NotNull
    public static String escapeComma(String value) {
        return value.replace(",", "${comma}");
    }

    @NotNull
    public static List<String> escapeCommas(List<String> values) {
        boolean hasValues = (values != null);
        List<String> escapedValues =
                new ArrayList<>( hasValues ? values.size() : 0 );

        if (hasValues) {
            for (String value : values) {
                escapedValues.add(escapeComma(value));
            }
        }
        return escapedValues;
    }

    @Nullable
    public static WSAuthorEditorPage getCurrentAuthorEditorPage(){
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
    public static Path expandOxygenPath(String originalPath, AuthorAccess authorAccess){
        Path path = null;

        if (authorAccess != null) {
            path = Paths.get(authorAccess.getUtilAccess().expandEditorVariables(
                    originalPath, authorAccess.getEditorAccess().getEditorLocation()));

            if (!Files.exists(path)) path = null;
        }
        return path;
    }
}

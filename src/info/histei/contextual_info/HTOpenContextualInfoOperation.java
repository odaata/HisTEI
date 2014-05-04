package info.histei.contextual_info;

import info.histei.utils.OxygenUtils;
import ro.sync.ecss.extensions.api.*;
import ro.sync.exml.editor.EditorPageConstants;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;

import java.net.URL;

/**
 * Created by mike on 1/20/14.
 */
public class HTOpenContextualInfoOperation implements AuthorOperation {

    @Override
    public void doOperation(AuthorAccess authorAccess, ArgumentsMap argumentsMap) throws IllegalArgumentException, AuthorOperationException {
        ContextualStyledList element = ContextualStyledList.get(authorAccess);
        if (element != null) {
            URL sourceURL = element.getURL();
            if (sourceURL != null) {
                PluginWorkspaceProvider.getPluginWorkspace().open(sourceURL, EditorPageConstants.PAGE_AUTHOR);
            } else {
                OxygenUtils.showErrorMessage(authorAccess, "The file could not be found!");
            }
        }
    }

    @Override
    public ArgumentDescriptor[] getArguments() {
        return new ArgumentDescriptor[0];
    }

    @Override
    public String getDescription() {
        return "Open the referenced contextual information file for editing.";
    }
}

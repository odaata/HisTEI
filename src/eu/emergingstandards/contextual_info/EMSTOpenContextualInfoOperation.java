package eu.emergingstandards.contextual_info;

import eu.emergingstandards.utils.EMSTOxygenUtils;
import ro.sync.ecss.extensions.api.*;
import ro.sync.exml.editor.EditorPageConstants;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;

import java.net.URL;

/**
 * Created by mike on 1/20/14.
 */
public class EMSTOpenContextualInfoOperation implements AuthorOperation {

//    private static Logger logger = Logger.getLogger(EMSTOpenContextualInfoOperation.class.getName());

    @Override
    public void doOperation(AuthorAccess authorAccess, ArgumentsMap argumentsMap) throws IllegalArgumentException, AuthorOperationException {
        EMSTContextualElement element = EMSTContextualElement.get(authorAccess);
        if (element != null) {
            URL sourceURL = element.getURL();
            if (sourceURL != null) {
                PluginWorkspaceProvider.getPluginWorkspace().open(sourceURL, EditorPageConstants.PAGE_AUTHOR);
            } else {
                EMSTOxygenUtils.showErrorMessage(authorAccess, "The file could not be found!");
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

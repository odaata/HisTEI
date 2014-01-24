package eu.emergingstandards.contextual_info;

import org.apache.log4j.Logger;
import ro.sync.ecss.dom.wrappers.AuthorElementDomWrapper;
import ro.sync.ecss.extensions.api.*;
import ro.sync.ecss.extensions.api.node.AuthorNode;
import ro.sync.exml.editor.EditorPageConstants;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;

import java.net.URL;

/**
 * Created by mike on 1/20/14.
 */
public class EMSTOpenContextualInfoOperation implements AuthorOperation {

    private static Logger logger = Logger.getLogger(EMSTOpenContextualInfoOperation.class.getName());

    @Override
    public void doOperation(AuthorAccess authorAccess, ArgumentsMap argumentsMap) throws IllegalArgumentException, AuthorOperationException {
        Object[] evaluateXPath = authorAccess.getDocumentController().evaluateXPath(".", false, false, false, false);

        if(evaluateXPath.length > 0) {
            AuthorNode currentNode = ((AuthorElementDomWrapper) evaluateXPath[0]).getWrappedAuthorNode();
            EMSTContextualInfo info = EMSTContextualInfo.get(currentNode);
            if (info != null && info.getSourcePath() != null) {
                URL sourceURL = info.getURL(currentNode);
                if (sourceURL != null)
                    PluginWorkspaceProvider.getPluginWorkspace().open(sourceURL, EditorPageConstants.PAGE_AUTHOR);
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

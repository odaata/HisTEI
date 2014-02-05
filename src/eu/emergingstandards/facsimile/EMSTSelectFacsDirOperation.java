package eu.emergingstandards.facsimile;

import ro.sync.ecss.extensions.api.*;
import ro.sync.ecss.extensions.api.node.AttrValue;
import ro.sync.ecss.extensions.api.node.AuthorElement;
import ro.sync.ecss.extensions.api.node.AuthorNode;

import java.io.File;

import static eu.emergingstandards.utils.EMSTOxygenUtils.XML_BASE_ATTRIB_NAME;
import static eu.emergingstandards.utils.EMSTOxygenUtils.getCurrentAuthorNode;
import static eu.emergingstandards.utils.EMSTUtils.castFileToURL;
import static eu.emergingstandards.utils.EMSTUtils.decodeURL;

/**
 * Created by mike on 1/25/14.
 */
public class EMSTSelectFacsDirOperation implements AuthorOperation {

    @Override
    public void doOperation(AuthorAccess authorAccess, ArgumentsMap argumentsMap) throws IllegalArgumentException, AuthorOperationException {
        AuthorNode currentNode = getCurrentAuthorNode(authorAccess);
        if (currentNode != null && "facsimile".equals(currentNode.getName())) {
            File dir = authorAccess.getWorkspaceAccess().chooseDirectory();
            if (dir != null) {
                String relativePath = authorAccess.getUtilAccess().makeRelative(
                        authorAccess.getEditorAccess().getEditorLocation(),
                        castFileToURL(dir)
                );
//                  Update the xml:base attribute
                authorAccess.getDocumentController().setAttribute(
                        XML_BASE_ATTRIB_NAME,
                        new AttrValue(relativePath.equals(".") ? null : decodeURL(relativePath)),
                        (AuthorElement) currentNode
                );
            }
        }
    }

    @Override
    public ArgumentDescriptor[] getArguments() {
        return new ArgumentDescriptor[0];
    }

    @Override
    public String getDescription() {
        return "Select the directory where the facsimile images and media for the current text are located.";
    }
}


package eu.emergingstandards.facsimile;

import eu.emergingstandards.utils.EMSTUtils;
import ro.sync.ecss.extensions.api.*;
import ro.sync.ecss.extensions.api.node.AttrValue;
import ro.sync.ecss.extensions.api.node.AuthorElement;
import ro.sync.ecss.extensions.api.node.AuthorNode;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLDecoder;

/**
 * Created by mike on 1/25/14.
 */
public class EMSTSelectFacsDirOperation implements AuthorOperation {

    @Override
    public void doOperation(AuthorAccess authorAccess, ArgumentsMap argumentsMap) throws IllegalArgumentException, AuthorOperationException {
        AuthorNode currentNode = EMSTUtils.getCurrentAuthorNode(authorAccess);
        if (currentNode != null && "facsimile".equals(currentNode.getName())) {
            File dir = authorAccess.getWorkspaceAccess().chooseDirectory();
            if (dir != null) {
                try {
                    String relativePath = authorAccess.getUtilAccess().makeRelative(
                            authorAccess.getEditorAccess().getEditorLocation(),
                            dir.toURI().toURL()
                    );
//                  Update the xml:base attribute
                    authorAccess.getDocumentController().setAttribute(
                            "xml:base",
                            new AttrValue(relativePath.equals(".") ? null : URLDecoder.decode(relativePath, "UTF-8")),
                            (AuthorElement) currentNode
                    );
                } catch (MalformedURLException | UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
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


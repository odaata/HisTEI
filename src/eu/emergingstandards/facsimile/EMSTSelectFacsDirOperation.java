package eu.emergingstandards.facsimile;

import eu.emergingstandards.exceptions.EMSTException;
import ro.sync.ecss.extensions.api.*;

import java.io.File;

/**
 * Created by mike on 1/25/14.
 */
public class EMSTSelectFacsDirOperation implements AuthorOperation {

    @Override
    public void doOperation(AuthorAccess authorAccess, ArgumentsMap argumentsMap) throws IllegalArgumentException, AuthorOperationException {
        EMSTFacsimile facsimile = EMSTFacsimile.get(authorAccess);

        if (facsimile != null) {
            File dir = authorAccess.getWorkspaceAccess().chooseDirectory();
            if (dir != null) {
                try {
                    facsimile.setBaseDirectory(dir);

                    if (facsimile.getXMLBase() != null) {
                        int choice = authorAccess.getWorkspaceAccess().showConfirmDialog("Update graphic/media elements",
                                "Would you like to update the graphic and media references with the contents of the new directory?\n" +
                                        "This will delete all the existing references in the document!",
                                new String[]{"Yes", "No"}, new int[]{0, 1}
                        );
                        if (choice == 0) {
                            facsimile.updateMediaElements();
                        }
                    }
                } catch (EMSTException e) {
                    e.notifyOxygenUser(authorAccess);
                }
            }
        }

        /*AuthorNode currentNode = getCurrentAuthorNode(authorAccess);
        if (currentNode != null && "facsimile".equals(currentNode.getName())) {
            File dir = authorAccess.getWorkspaceAccess().chooseDirectory();
            if (dir != null) {
                String relativePath = authorAccess.getUtilAccess().makeRelative(
                        authorAccess.getEditorAccess().getEditorLocation(),
                        castFileToURL(dir)
                );
//                  Update the xml:base attribute
                authorAccess.getDocumentController().setAttribute(
                        EMSTXMLUtils.XML_BASE_ATTRIB_NAME,
                        new AttrValue(relativePath.equals(".") ? null : decodeURL(relativePath)),
                        (AuthorElement) currentNode
                );
            }
        }*/

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


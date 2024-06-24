package info.histei.facsimile;

import info.histei.exceptions.HTException;
import info.histei.utils.OxygenUtils;
import ro.sync.ecss.extensions.api.*;

import java.io.File;

/**
 * Created by mike on 1/25/14.
 */
public class HTSelectFacsDirOperation implements AuthorOperation {

    @Override
    public void doOperation(AuthorAccess authorAccess, ArgumentsMap argumentsMap) throws IllegalArgumentException, AuthorOperationException {
        int choice;
        Facsimile facsimile = Facsimile.get(authorAccess);

        if (facsimile == null) {
            choice = authorAccess.getWorkspaceAccess().showConfirmDialog(
                    "Add <facsimile> element?",
                    "There is no <facsimile> element in this document. Would you like to create one?",
                    new String[]{"Yes", "No"}, new int[]{0, 1}
            );

            if (choice == 0) {
                try {
                    facsimile = Facsimile.createFacsimileElement(authorAccess);
                } catch (HTException e) {
                    e.notifyOxygenUser(authorAccess);
                }
            }
        }

        if (facsimile != null) {
            File dir = authorAccess.getWorkspaceAccess().chooseDirectory();

            if (dir != null) {
                try {
                    facsimile.setBaseDirectory(dir);

                    choice = authorAccess.getWorkspaceAccess().showConfirmDialog(
                            "Update <graphic>/<media> elements?",
                            "Would you like to update the <graphic> and <media> references with the contents of the new directory?\n\n" +
                                    "WARNING: This will delete all existing references in this document and break existing links to page breaks and any other references!",
                            new String[]{"Yes", "No"}, new int[]{0, 1}
                    );
                    if (choice == 0) {
                        facsimile.updateMediaElements();
                        OxygenUtils.refreshCurrentPage();
                    }
                } catch (HTException e) {
                    e.notifyOxygenUser(authorAccess);
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


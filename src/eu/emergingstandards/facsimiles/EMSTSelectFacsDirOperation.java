package eu.emergingstandards.facsimiles;

import org.apache.commons.io.FilenameUtils;
import ro.sync.ecss.extensions.api.*;

import java.io.File;

/**
 * Created by mike on 1/25/14.
 */
public class EMSTSelectFacsDirOperation implements AuthorOperation {

    @Override
    public void doOperation(AuthorAccess authorAccess, ArgumentsMap argumentsMap) throws IllegalArgumentException, AuthorOperationException {
        File dir = authorAccess.getWorkspaceAccess().chooseDirectory();
        if (dir != null) {
            for (File file : dir.listFiles()) {
                String ext = FilenameUtils.getExtension(file.getName());

            }
        }
    }

    @Override
    public ArgumentDescriptor[] getArguments() {
        return new ArgumentDescriptor[0];
    }

    @Override
    public String getDescription() {
        return "Select the directory where the facsimiles of the current text are located.";
    }
}


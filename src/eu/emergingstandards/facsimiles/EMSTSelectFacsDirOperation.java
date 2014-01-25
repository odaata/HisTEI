package eu.emergingstandards.facsimiles;

import ro.sync.ecss.extensions.api.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Created by mike on 1/25/14.
 */
public class EMSTSelectFacsDirOperation implements AuthorOperation {

    @Override
    public void doOperation(AuthorAccess authorAccess, ArgumentsMap argumentsMap) throws IllegalArgumentException, AuthorOperationException {
        File dir = authorAccess.getWorkspaceAccess().chooseDirectory();
        if (dir != null) {
            for (File file : dir.listFiles()) {
                try {
                    String mediaType = Files.probeContentType(file.toPath());
                } catch (IOException e) {
                    continue;
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
        return "Select the directory where the facsimiles of the current text are located.";
    }
}


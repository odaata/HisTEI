package eu.emergingstandards.facsimile;

import ro.sync.ecss.extensions.api.*;

/**
 * Created by mike on 1/25/14.
 */
public class EMSTSelectFacsDirOperation implements AuthorOperation {

    @Override
    public void doOperation(AuthorAccess authorAccess, ArgumentsMap argumentsMap) throws IllegalArgumentException, AuthorOperationException {

    }

    @Override
    public ArgumentDescriptor[] getArguments() {
        return new ArgumentDescriptor[0];
    }

    @Override
    public String getDescription() {
        return "Select the directory where the facsimile of the current text are located.";
    }
}


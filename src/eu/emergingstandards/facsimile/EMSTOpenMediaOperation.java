package eu.emergingstandards.facsimile;

import eu.emergingstandards.utils.EMSTUtils;
import ro.sync.ecss.extensions.api.*;
import ro.sync.ecss.extensions.api.node.AuthorNode;

/**
 * Created by mike on 1/29/14.
 */
public class EMSTOpenMediaOperation implements AuthorOperation {
    @Override
    public void doOperation(AuthorAccess authorAccess, ArgumentsMap argumentsMap) throws IllegalArgumentException, AuthorOperationException {
        AuthorNode currentNode = EMSTUtils.getCurrentAuthorNode(authorAccess);

    }

    @Override
    public ArgumentDescriptor[] getArguments() {
        return new ArgumentDescriptor[0];
    }

    @Override
    public String getDescription() {
        return "Open graphics or other media from the current node.";
    }
}

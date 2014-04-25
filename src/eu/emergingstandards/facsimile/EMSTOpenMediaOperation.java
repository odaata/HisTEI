package eu.emergingstandards.facsimile;

import eu.emergingstandards.exceptions.EMSTFileMissingException;
import ro.sync.ecss.extensions.api.*;

/**
 * Created by mike on 1/29/14.
 */
public class EMSTOpenMediaOperation implements AuthorOperation {
    @Override
    public void doOperation(AuthorAccess authorAccess, ArgumentsMap argumentsMap) throws IllegalArgumentException, AuthorOperationException {
        MediaElement mediaElement = Facsimile.getMediaElement(authorAccess);

        if (mediaElement != null) {
            try {
                mediaElement.open();
            } catch (EMSTFileMissingException e) {
                e.notifyOxygenUser(authorAccess);
            }
        }
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

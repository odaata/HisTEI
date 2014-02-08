package eu.emergingstandards.facsimile;

import eu.emergingstandards.exceptions.EMSTFileMissingException;
import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.node.AuthorElement;

/**
 * Created by mike on 2/7/14.
 */
public interface EMSTMediaElement {

    AuthorAccess getAuthorAccess();

    AuthorElement getAuthorElement();

    EMSTMediaType getType();

    EMSTFacsimile getFacsimile();

    void open() throws EMSTFileMissingException;

}

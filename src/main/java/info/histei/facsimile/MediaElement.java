package info.histei.facsimile;

import info.histei.exceptions.HTFileMissingException;
import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.node.AuthorElement;

/**
 * Created by mike on 2/7/14.
 */
public interface MediaElement {

    AuthorAccess getAuthorAccess();

    AuthorElement getAuthorElement();

    MediaType getType();

    Facsimile getFacsimile();

    void open() throws HTFileMissingException;

}

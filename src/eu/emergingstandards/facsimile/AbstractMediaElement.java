package eu.emergingstandards.facsimile;

import eu.emergingstandards.exceptions.EMSTFileMissingException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.node.AuthorElement;

/**
 * Created by mike on 2/7/14.
 */
public abstract class AbstractMediaElement implements MediaElement {

    protected AuthorAccess authorAccess;
    protected AuthorElement authorElement;
    protected MediaType type;
    protected Facsimile facsimile;

    protected AbstractMediaElement(AuthorAccess authorAccess, AuthorElement authorElement,
                                   MediaType type) {
        this.authorAccess = authorAccess;
        this.authorElement = authorElement;
        this.type = type;
    }

    @Override
    @NotNull
    public AuthorAccess getAuthorAccess() {
        return authorAccess;
    }

    @Override
    @NotNull
    public AuthorElement getAuthorElement() {
        return authorElement;
    }

    @Override
    @NotNull
    public MediaType getType() {
        return type;
    }

    @Override
    @Nullable
    public Facsimile getFacsimile() {
        return Facsimile.get(authorAccess);
    }

    @Override
    public abstract void open() throws EMSTFileMissingException;
}

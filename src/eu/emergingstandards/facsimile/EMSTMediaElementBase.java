package eu.emergingstandards.facsimile;

import eu.emergingstandards.exceptions.EMSTFileMissingException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.node.AuthorElement;

/**
 * Created by mike on 2/7/14.
 */
public class EMSTMediaElementBase implements EMSTMediaElement {

    protected AuthorAccess authorAccess;
    protected AuthorElement authorElement;
    protected EMSTMediaType type;
    protected EMSTFacsimile facsimile;

    protected EMSTMediaElementBase(AuthorAccess authorAccess, AuthorElement authorElement,
                                   EMSTMediaType type) {
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
    public EMSTMediaType getType() {
        return type;
    }

    @Override
    @Nullable
    public EMSTFacsimile getFacsimile() {
        return EMSTFacsimile.get(authorAccess);
    }

    @Override
    public void open() throws EMSTFileMissingException {

    }
}

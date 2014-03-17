package eu.emergingstandards.lists.schema;

import eu.emergingstandards.lists.EMSTListItem;
import org.jetbrains.annotations.NotNull;
import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.node.AuthorNode;

/**
 * Created by mike on 3/10/14.
 */
public interface EMSTReferenceSchemaList<I extends EMSTListItem> extends EMSTSchemaList<I> {

    @NotNull
    AuthorAccess getAuthorAccess();

    boolean isEdited(AuthorNode editedNode);

    void refresh();

    void addListener();

    void removeListener();

}

package eu.emergingstandards.lists.schema;

import eu.emergingstandards.lists.EMSTListItem;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import ro.sync.ecss.extensions.api.*;
import ro.sync.ecss.extensions.api.node.AuthorDocument;
import ro.sync.ecss.extensions.api.node.AuthorNode;

/**
 * Created by mike on 3/7/14.
 */
public abstract class EMSTAbstractReferenceSchemaList<I extends EMSTListItem> extends EMSTAbstractSchemaList<I>
        implements EMSTReferenceSchemaList<I>, AuthorListener {

    private static final Logger logger = Logger.getLogger(EMSTAbstractReferenceSchemaList.class.getName());

    protected final AuthorAccess authorAccess;
//    private final Map<URL, List<I>> items = new HashMap<>();

    protected EMSTAbstractReferenceSchemaList(AuthorAccess authorAccess) {
        this.authorAccess = authorAccess;

        refresh();
        addListener();
    }

    @NotNull
    @Override
    public final AuthorAccess getAuthorAccess() {
        return authorAccess;
    }

    @Override
    public final void addListener() {
        getAuthorAccess().getDocumentController().addAuthorListener(this);
    }

    @Override
    public final void removeListener() {
        getAuthorAccess().getDocumentController().removeAuthorListener(this);
    }

    protected void handleAuthorEvent(AuthorNode editedNode) {
        if (editedNode != null) {
            if (isEdited(editedNode)) {
                synchronized (this) {
                    reset();

                    refresh();
                }
            }
        }
    }

    @Override
    public abstract boolean isEdited(AuthorNode editedNode);

    @Override
    public abstract void refresh();

//    AuthorListener Methods

    @Override
    public void beforeContentDelete(DocumentContentDeletedEvent documentContentDeletedEvent) {
    }

    @Override
    public void beforeAttributeChange(AttributeChangedEvent attributeChangedEvent) {
    }

    @Override
    public void beforeContentInsert(DocumentContentInsertedEvent documentContentInsertedEvent) {
    }

    @Override
    public void beforeDoctypeChange() {
    }

    @Override
    public void beforeAuthorNodeStructureChange(AuthorNode authorNode) {
    }

    @Override
    public void beforeAuthorNodeNameChange(AuthorNode authorNode) {
    }

    @Override
    public void attributeChanged(AttributeChangedEvent attributeChangedEvent) {
        if (attributeChangedEvent != null)
            handleAuthorEvent(attributeChangedEvent.getOwnerAuthorNode());
    }

    @Override
    public void authorNodeNameChanged(AuthorNode authorNode) {
        if (authorNode != null)
            handleAuthorEvent(authorNode);
    }

    @Override
    public void authorNodeStructureChanged(AuthorNode authorNode) {
        if (authorNode != null)
            handleAuthorEvent(authorNode);
    }

    @Override
    public void documentChanged(AuthorDocument authorDocument, AuthorDocument authorDocument2) {
    }

    @Override
    public void contentDeleted(DocumentContentDeletedEvent documentContentDeletedEvent) {
        if (documentContentDeletedEvent != null)
            handleAuthorEvent(documentContentDeletedEvent.getParentNode());
    }

    @Override
    public void contentInserted(DocumentContentInsertedEvent documentContentInsertedEvent) {
        if (documentContentInsertedEvent != null)
            handleAuthorEvent(documentContentInsertedEvent.getParentNode());
    }

    @Override
    public void doctypeChanged() {
    }

}

package eu.emergingstandards.lists.styled;

import eu.emergingstandards.lists.EMSTList;
import eu.emergingstandards.lists.EMSTListItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ro.sync.ecss.extensions.api.node.AuthorElement;
import ro.sync.exml.workspace.api.editor.page.author.WSAuthorEditorPage;

/**
 * Created by mike on 3/4/14.
 */
public interface EMSTStyledList<I extends EMSTListItem> extends EMSTList<I> {

    @Nullable
    AuthorElement getAuthorElement();

    @Nullable
    WSAuthorEditorPage getAuthorPage();

    void setAuthorPage(WSAuthorEditorPage newAuthorPage);

    @NotNull
    String getEditPropertyQualified();

    @NotNull
    String getOxygenValues();

    @NotNull
    String getOxygenLabels();

    @NotNull
    String getOxygenTooltips();

}

package eu.emergingstandards.lists.styled;

import eu.emergingstandards.lists.ListItem;
import eu.emergingstandards.lists.Listable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ro.sync.ecss.extensions.api.node.AuthorElement;
import ro.sync.exml.workspace.api.editor.page.author.WSAuthorEditorPage;

/**
 * Created by mike on 3/4/14.
 */
public interface StyledList<I extends ListItem> extends Listable<I> {

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

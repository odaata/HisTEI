package eu.emergingstandards.lists.styled;

import eu.emergingstandards.events.EMSTRefreshEventListener;
import eu.emergingstandards.lists.EMSTListItem;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ro.sync.ecss.extensions.api.node.AuthorElement;
import ro.sync.exml.workspace.api.editor.page.author.WSAuthorEditorPage;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import static eu.emergingstandards.utils.EMSTOxygenUtils.escapeCommas;

/**
 * Created by mike on 3/4/14.
 */
public abstract class EMSTAbstractStyledList<I extends EMSTListItem>
        implements EMSTStyledList, EMSTRefreshEventListener {

    private final WeakReference<AuthorElement> authorElement;
    private WeakReference<WSAuthorEditorPage> authorPage;

    protected EMSTAbstractStyledList(AuthorElement authorElement) {
        this.authorElement = new WeakReference<>(authorElement);
    }

    @Nullable
    @Override
    public final AuthorElement getAuthorElement() {
        return authorElement.get();
    }

    @Nullable
    @Override
    public final WSAuthorEditorPage getAuthorPage() {
        if (authorPage != null) {
            return authorPage.get();
        } else {
            return null;
        }
    }

    @Override
    public final void setAuthorPage(WSAuthorEditorPage newAuthorPage) {
        if (newAuthorPage != null) {
            authorPage = new WeakReference<>(newAuthorPage);
        }
    }

    @NotNull
    @Override
    public abstract List<I> getItems();

    @NotNull
    @Override
    public abstract String getEditPropertyQualified();

    @NotNull
    @Override
    public String getOxygenValues() {
        List<String> values = new ArrayList<>();

        for (I item : getItems()) {
            values.add(item.getValue());
        }
        return StringUtils.join(values, ",");
    }

    @NotNull
    @Override
    public String getOxygenLabels() {
        List<String> labels = new ArrayList<>();

        for (I item : getItems()) {
            labels.add(item.getLabel());
        }
        return StringUtils.join(escapeCommas(labels), ",");
    }

    @NotNull
    @Override
    public String getOxygenTooltips() {
        List<String> tooltips = new ArrayList<>();

        for (I item : getItems()) {
            tooltips.add(item.getTooltip());
        }
        return StringUtils.join(escapeCommas(tooltips), ",");
    }

//    EMSTRefreshEventListener Method

    @Override
    public void refresh() {
        WSAuthorEditorPage page = getAuthorPage();
        AuthorElement element = getAuthorElement();

        if (page != null && element != null) {
            page.refresh(element);
        }
    }
}

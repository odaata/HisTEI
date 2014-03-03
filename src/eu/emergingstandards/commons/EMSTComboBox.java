package eu.emergingstandards.commons;

import ro.sync.ecss.component.editor.ComboBoxEditor;
import ro.sync.ecss.extensions.api.CursorType;
import ro.sync.ecss.extensions.api.editor.AuthorInplaceContext;
import ro.sync.ecss.extensions.api.editor.InplaceRenderer;
import ro.sync.ecss.extensions.api.editor.RendererLayoutInfo;

/**
 * Created by mike on 2/25/14.
 */
public class EMSTComboBox extends ComboBoxEditor implements InplaceRenderer {

    @Override
    public Object getRendererComponent(AuthorInplaceContext authorInplaceContext) {
        return null;
    }

    @Override
    public CursorType getCursorType(AuthorInplaceContext authorInplaceContext, int i, int i2) {
        return null;
    }

    @Override
    public CursorType getCursorType(int i, int i2) {
        return null;
    }

    @Override
    public String getTooltipText(AuthorInplaceContext authorInplaceContext, int i, int i2) {
        return null;
    }

    @Override
    public RendererLayoutInfo getRenderingInfo(AuthorInplaceContext authorInplaceContext) {
        return null;
    }

    private void prepareComponents(AuthorInplaceContext context, boolean forEditing) {

    }
}

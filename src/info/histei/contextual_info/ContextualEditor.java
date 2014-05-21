package info.histei.contextual_info;

import info.histei.commons.HTComboBox;
import info.histei.commons.Icon;
import info.histei.utils.MainUtils;
import info.histei.utils.OxygenUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ro.sync.ecss.extensions.api.CursorType;
import ro.sync.ecss.extensions.api.editor.*;
import ro.sync.ecss.extensions.commons.editor.InplaceEditorUtil;
import ro.sync.exml.editor.EditorPageConstants;
import ro.sync.exml.view.graphics.Rectangle;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by mike on 5/10/14.
 */
public class ContextualEditor extends AbstractInplaceEditor implements InplaceRenderer {

//    private static final Logger logger = Logger.getLogger(ContextualEditor.class.getName());

    public final static String PROPERTY_SHOW_BUTTON = "showButton";

    private final static int VGAP = 0;  // vertical gap in panel layout
    private final static int HGAP = 7;  // horizontal gap

    private final JPanel panel;
    private final HTComboBox<ContextualItem> comboBox;
    private final JButton editButton;

    private final java.awt.Font defaultFont;

    public ContextualEditor() {
        panel = new JPanel(new BorderLayout(HGAP, VGAP));
        comboBox = new HTComboBox<>();

        comboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fireEditingOccured();
            }
        });

        comboBox.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                fireCommitValue(getEditingEvent());
//                stopEditing();
            }
        });

        comboBox.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    // ESC must cancel the edit.
                    e.consume();
                    cancelEditing();
                } else if (e.getKeyCode() == KeyEvent.VK_ENTER || e.getKeyCode() == KeyEvent.VK_TAB) {
                    // An ENTER or a TAB commits the value.
                    e.consume();
                    fireNextEditLocationRequested();
                }
            }
        });

        editButton = new JButton();
        editButton.setToolTipText("Open Contextual Info for editing...");
        editButton.setHorizontalAlignment(SwingConstants.CENTER);
        editButton.setHorizontalTextPosition(SwingConstants.CENTER);
        editButton.setFocusable(false);

        panel.add(comboBox, BorderLayout.CENTER);
        panel.add(editButton, BorderLayout.EAST);
        panel.setOpaque(false);

        defaultFont = comboBox.getFont();
    }

    @Override
    public String getDescription() {
        return "Contextual info combobox control for CSS styles";
    }

    /* Renderer Methods */

    @Override
    public Object getRendererComponent(AuthorInplaceContext context) {
        prepareComponents(context);

        return panel;
    }

    @Override
    public RendererLayoutInfo getRenderingInfo(AuthorInplaceContext context) {
        prepareComponents(context);

        return computeRenderingInfo(context);
    }

    @Override
    public String getTooltipText(AuthorInplaceContext context, int i, int i2) {
        return null;
    }

    private RendererLayoutInfo computeRenderingInfo(AuthorInplaceContext context) {
        final java.awt.Dimension preferredSize = comboBox.getPreferredSize();

        int width = preferredSize.width;

        Integer columns = (Integer) context.getArguments().get(InplaceEditorArgumentKeys.PROPERTY_COLUMNS);
        if (columns != null && columns > 0) {
            FontMetrics fontMetrics = comboBox.getFontMetrics(comboBox.getFont());
            width = (columns * fontMetrics.charWidth('w'));
//            comboBox.setWidthChars(columns);
        }
        // Add width for button and gap
        width += HGAP + editButton.getPreferredSize().width;
//        int width = comboBox.getPreferredSize().width + HGAP + editButton.getPreferredSize().width;

        return new RendererLayoutInfo(
                comboBox.getBaseline(preferredSize.width, preferredSize.height),
                new ro.sync.exml.view.graphics.Dimension(width, preferredSize.height)
        );
    }

    /* Editor methods */

    @Override
    public Object getEditorComponent(final AuthorInplaceContext context,
                                     ro.sync.exml.view.graphics.Rectangle rectangle,
                                     ro.sync.exml.view.graphics.Point point) {
        prepareComponents(context);

        editButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                URL sourceURL = null;

                ContextualItem currentItem = (ContextualItem) comboBox.getSelectedItem();
                if (currentItem != null) {
                    sourceURL = currentItem.getURL();
                } else {
                    ContextualInfo info = getContextualInfo(context);
                    if (info != null) {
                        sourceURL = info.getURL();
                    }
                }

                if (sourceURL != null) {
                    PluginWorkspaceProvider.getPluginWorkspace().open(sourceURL, EditorPageConstants.PAGE_AUTHOR);
                } else {
                    OxygenUtils.showErrorMessage(context.getAuthorAccess(), "The file could not be found!");
                }
            }
        });

        return panel;
    }

    @Override
    public Rectangle getScrollRectangle() {
        return null;
    }

    @Override
    public void requestFocus() {
        comboBox.requestFocus();
    }

    @Override
    @Nullable
    public Object getValue() {
        String value = null;
        ContextualItem item = (ContextualItem) comboBox.getSelectedItem();

        if (item != null) {
            value = StringUtils.trimToNull(item.getValue());
        }
        return value;
    }

    @NotNull
    private EditingEvent getEditingEvent() {
        return new EditingEvent((String) getValue());
    }

    @Override
    public void stopEditing() {
        fireEditingStopped(getEditingEvent());
    }

    @Override
    public void cancelEditing() {
        fireEditingCanceled();
    }

    @Nullable
    public ContextualInfo getContextualInfo(AuthorInplaceContext context) {
        ContextualInfo info = null;

        ContextualAttribute attribute = ContextualAttribute.get(context);
        if (attribute != null) {
            info = ContextualInfo.get(attribute.getContextualType());
        }
        return info;
    }

    private void prepareComponents(AuthorInplaceContext context) {
        List<ContextualItem> items = new ArrayList<>();

        ContextualAttribute attribute = ContextualAttribute.get(context);
        if (attribute != null) {
            ContextualInfo info = ContextualInfo.get(attribute.getContextualType());

            items = info.getItems(attribute.getTypeFilter());
        }

        comboBox.setItems(items);

        String value = StringUtils.trimToEmpty((String) context.getArguments().get(InplaceEditorArgumentKeys.INITIAL_VALUE));
        ContextualItem foundItem = null;

        if (!value.isEmpty()) {
            for (ContextualItem item : items) {
                if (item.getValue().equals(value)) {
                    foundItem = item;
                    comboBox.setSelectedItem(foundItem);
                    break;
                }
            }
        }

        if (foundItem == null) {
            comboBox.setSelectedIndex(-1);
        }

        String fontInheritProperty = StringUtils.trimToEmpty((String) context.getArguments().get(InplaceEditorArgumentKeys.PROPERTY_FONT_INHERIT));
        boolean fontInherit = !fontInheritProperty.isEmpty() && Boolean.parseBoolean(fontInheritProperty);
        ro.sync.exml.view.graphics.Font font = context.getStyles().getFont();
        java.awt.Font currentFont = fontInherit ? new java.awt.Font(font.getName(), font.getStyle(), font.getSize()) : defaultFont;

        comboBox.setFont(currentFont);
        editButton.setFont(currentFont);

        InplaceEditorUtil.relayout(comboBox, context);

        String showButtonProperty = StringUtils.trimToEmpty((String) context.getArguments().get(PROPERTY_SHOW_BUTTON));
        boolean showButton = showButtonProperty.isEmpty() || Boolean.parseBoolean(showButtonProperty);
        editButton.setVisible(showButton);

        if (editButton.isVisible()) {
            setButtonFace(context);
            ro.sync.exml.view.graphics.Point relMousePos = context.getRelativeMouseLocation();
            boolean rollover = false;
            if (relMousePos != null) {
                RendererLayoutInfo renderInfo = computeRenderingInfo(context);
                panel.setSize(renderInfo.getSize().width, renderInfo.getSize().height);
                // Unless we do the layout we can't determine the component under the mouse.
                panel.doLayout();

                Component componentAt = panel.getComponentAt(relMousePos.x, relMousePos.y);
                rollover = componentAt == editButton;
            }
            editButton.getModel().setRollover(rollover);
        }
    }

    private void setButtonFace(AuthorInplaceContext context) {
        if (editButton.getIcon() == null && StringUtils.trimToEmpty(editButton.getText()).isEmpty()) {
            Icon icon = Icon.get("document_edit");
            String iconPath = icon != null ? icon.getPath() : null;
            if (iconPath != null) {
                Path expandedPath = OxygenUtils.expandOxygenPath(iconPath, context.getAuthorAccess());
                if (expandedPath != null) {
                    ImageIcon iconImage = new ImageIcon(MainUtils.castPathToURL(expandedPath));
                    editButton.setIcon(iconImage);
                    int width = iconImage.getIconWidth() + (editButton.getPreferredSize().height - iconImage.getIconHeight());
                    editButton.setPreferredSize(new java.awt.Dimension(width, iconImage.getIconHeight()));
                }
            }

            if (editButton.getIcon() == null) {
                editButton.setText("Edit...");
            }
        }
    }

    @Override
    public CursorType getCursorType(AuthorInplaceContext context, int x, int y) {
        return CursorType.CURSOR_NORMAL;
    }

    @Override
    public CursorType getCursorType(int x, int y) {
        return null;
    }

}
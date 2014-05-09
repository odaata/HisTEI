package info.histei.contextual_info;

import info.histei.commons.Icon;
import org.apache.log4j.Logger;
import ro.sync.ecss.extensions.api.CursorType;
import ro.sync.ecss.extensions.api.editor.AbstractInplaceEditor;
import ro.sync.ecss.extensions.api.editor.AuthorInplaceContext;
import ro.sync.ecss.extensions.api.editor.InplaceRenderer;
import ro.sync.ecss.extensions.api.editor.RendererLayoutInfo;
import ro.sync.exml.view.graphics.Rectangle;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

// TODO: Create custom CSS ComboBox Control - this is the skeleton

/**
 * Created by mike on 2/25/14.
 */
public class ContextualInfoComboBox extends AbstractInplaceEditor implements InplaceRenderer {

    private static final Logger logger = Logger.getLogger(ContextualInfoComboBox.class.getName());

    private final static int VGAP = 0;  // vertical gap in panel layout
    private final static int HGAP = 5;  // horizontal gap
    private static final String UNDO_MANAGER_PROPERTY = "undo-manager-property";

    private final JPanel panel;
    private JComboBox<ArrayList<ContextualItem>> comboBox;
    private final JButton editButton;

//    private final java.awt.Font defaultFont;

    public ContextualInfoComboBox() {
        panel = new JPanel(new BorderLayout(HGAP, VGAP));

        Icon icon = Icon.get("document_edit");
        String iconPath = icon != null ? icon.getPath() : null;
        if (iconPath != null) {
            editButton = new JButton(new ImageIcon(iconPath));
        } else {
            editButton = new JButton("Edit...");
        }

        comboBox = new JComboBox<>();
        comboBox.addActionListener();
        comboBox.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void removeUpdate(DocumentEvent e) {
                fireEditingOccured();
            }
            @Override
            public void insertUpdate(DocumentEvent e) {
                fireEditingOccured();
            }
            @Override
            public void changedUpdate(DocumentEvent e) {
                fireEditingOccured();
            }
        });

        panel.add(comboBox, BorderLayout.CENTER);
        panel.add(editButton,BorderLayout.EAST);
        panel.setOpaque(false);

        comboBox.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    // ESC must cancel the edit.
                    e.consume();
                    cancelEditing();
                } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    // An ENTER commits the value.
                    e.consume();
                    stopEditing(true);
                }
            }
        });

        editButton.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    // ESC must cancel the edit.
                    e.consume();
                    cancelEditing();
                }
            }
        });

        FocusListener focusListener = new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                if (e.getOppositeComponent() != editButton
                        && e.getOppositeComponent() != comboBox
                        && !isBrowsing) {
                    // The focus is outside the components of this editor.
                    fireEditingStopped(new EditingEvent(comboBox.getText(), e.getOppositeComponent() == null));
                }
            }
        };

        editButton.addFocusListener(focusListener);
        comboBox.addFocusListener(focusListener);

        Insets originalInsets = comboBox.getMargin();
        Insets imposedInsets = null;
        if(originalInsets != null) {
            imposedInsets = (Insets) originalInsets.clone();
        } else {
            //EXM-27442 - Maybe Nimbus LF, set some margins.
            imposedInsets = new Insets(1, 1, 1, 1);
        }
        if (IS_WIN32 && IS_ECLIPSE) {
            // On Eclipse the text field text should not flicker
            imposedInsets.top = -1;
            imposedInsets.left += 3;
        }

        comboBox.setMargin(imposedInsets);

        defaultFont = comboBox.getFont();
    }

    @Override
    public Object getRendererComponent(AuthorInplaceContext context) {
        /*prepareComboBox(context);
        return getComboBox();*/
        return null;
    }

    @Override
    public CursorType getCursorType(AuthorInplaceContext context, int x, int y) {
        return CursorType.CURSOR_NORMAL;
    }

    @Override
    public CursorType getCursorType(int x, int y) {
        return null;
    }

    @Override
    public String getTooltipText(AuthorInplaceContext context, int x, int y) {
        return null;
    }

    @Override
    public RendererLayoutInfo getRenderingInfo(AuthorInplaceContext context) {
        /*prepareComboBox(context);

        return computeRenderingInfo(context);*/
        return null;
    }

    @Override
    public String getDescription() {
        return "Contextual info combobox control for CSS styles";
    }

    @Override
    public Object getEditorComponent(AuthorInplaceContext authorInplaceContext, Rectangle rectangle, ro.sync.exml.view.graphics.Point point) {
        return null;
    }

    @Override
    public Rectangle getScrollRectangle() {
        return null;
    }

    @Override
    public void requestFocus() {

    }

    @Override
    public Object getValue() {
        return null;
    }

    @Override
    public void stopEditing() {

    }

    @Override
    public void cancelEditing() {

    }

    /*private RendererLayoutInfo computeRenderingInfo(AuthorInplaceContext context) {
        final java.awt.Dimension preferredSize = getComboBox().getPreferredSize();

        // Get width
        int width = getComboBox().getPreferredSize().width;

        Integer columns = (Integer) context.getArguments().get(InplaceEditorArgumentKeys.PROPERTY_COLUMNS);
        if (columns != null && columns > 0) {
            FontMetrics fontMetrics = getComboBox().getFontMetrics(getComboBox().getFont());
            width = columns * fontMetrics.charWidth('w');
        }

        return new RendererLayoutInfo(
            getComboBox().getBaseline(preferredSize.width, preferredSize.height),
            new Dimension(width, preferredSize.height)
        );
    }

    private void prepareComboBox(AuthorInplaceContext context) {
        String value = MainUtils.nullToEmpty((String) context.getArguments().get(InplaceEditorArgumentKeys.INITIAL_VALUE));
        getComboBox().setSelectedItem(value);
    }*/
}

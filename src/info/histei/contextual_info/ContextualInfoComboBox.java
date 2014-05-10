package info.histei.contextual_info;

import info.histei.commons.Icon;
import info.histei.utils.MainUtils;
import info.histei.utils.OxygenUtils;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import ro.sync.ecss.extensions.api.editor.AuthorInplaceContext;
import ro.sync.ecss.extensions.api.editor.InplaceEditorArgumentKeys;
import ro.sync.ecss.extensions.api.editor.InplaceEditorRendererAdapter;
import ro.sync.ecss.extensions.api.editor.RendererLayoutInfo;
import ro.sync.exml.view.graphics.Dimension;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.nio.file.Path;
import java.util.List;

/**
 * Created by mike on 5/10/14.
 */
public class ContextualInfoComboBox extends InplaceEditorRendererAdapter {

    private static final Logger logger = Logger.getLogger(ContextualInfoComboBox.class.getName());

    private final static int VGAP = 0;  // vertical gap in panel layout
    private final static int HGAP = 5;  // horizontal gap

    private final JPanel panel = new JPanel(new BorderLayout(HGAP, VGAP));
    private final JComboBox<ContextualItem> comboBox = new JComboBox<>();;
    private final JButton editButton = new JButton();

    private List<ContextualItem> items;

    public ContextualInfoComboBox() {
        comboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                stopEditing();
            }
        });

        editButton.setHorizontalAlignment(SwingConstants.CENTER);
        editButton.setHorizontalTextPosition(SwingConstants.CENTER);
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

        panel.add(comboBox, BorderLayout.CENTER);
        panel.add(editButton, BorderLayout.EAST);
        panel.setOpaque(false);
    }

    @Override
    public String getDescription() {
        return "Contextual info combobox control for CSS styles";
    }

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
    public Object getEditorComponent(AuthorInplaceContext context, ro.sync.exml.view.graphics.Rectangle rectangle, ro.sync.exml.view.graphics.Point point) {
        prepareComponents(context);

        return panel;
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
            value = item.getValue();
        }
        return value;
    }

    private RendererLayoutInfo computeRenderingInfo(AuthorInplaceContext context) {
        final java.awt.Dimension preferredSize = comboBox.getPreferredSize();

        // Get width
        int width = comboBox.getPreferredSize().width;

        Integer columns = (Integer) context.getArguments().get(InplaceEditorArgumentKeys.PROPERTY_COLUMNS);
        if (columns != null && columns > 0) {
            FontMetrics fontMetrics = comboBox.getFontMetrics(comboBox.getFont());
            width = columns * fontMetrics.charWidth('w');
        }

        return new RendererLayoutInfo(
                comboBox.getBaseline(preferredSize.width, preferredSize.height),
                new Dimension(width, preferredSize.height)
        );
    }

    private void prepareComponents(AuthorInplaceContext context) {
        ContextualAttribute attribute = ContextualAttribute.get(context);
        if (attribute != null) {
            ContextualInfo info = ContextualInfo.get(attribute.getContextualType());

            items = info.getItems(attribute.getTypeFilter());
            ContextualItem[] itemsArray = items.toArray(new ContextualItem[items.size()]);
            comboBox.setModel(new DefaultComboBoxModel<>(itemsArray));

            String value = MainUtils.nullToEmpty((String) context.getArguments().get(InplaceEditorArgumentKeys.INITIAL_VALUE));
            if (!value.isEmpty()) {
                for (ContextualItem item : items) {
                    if (item.getValue().equals(value)) {
                        comboBox.setSelectedItem(item);
                        break;
                    }
                }
            }
        }

        setButtonFace(context);
    }

    private void setButtonFace(AuthorInplaceContext context) {
        if (editButton.getIcon() == null && MainUtils.nullToEmpty(editButton.getText()).isEmpty()) {
            Icon icon = Icon.get("document_edit");
            String iconPath = icon != null ? icon.getPath() : null;
            if (iconPath != null) {
                Path expandedPath = OxygenUtils.expandOxygenPath(iconPath, context.getAuthorAccess());
                if (expandedPath != null) {
                    editButton.setIcon(new ImageIcon(MainUtils.castPathToURL(expandedPath)));
                }
            }

            if (editButton.getIcon() == null) {
                editButton.setText("Edit...");
            }
        }
    }

}
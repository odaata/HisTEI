package info.histei.commons;

import com.google.common.collect.Ordering;
import com.google.common.primitives.Ints;
import info.histei.lists.ListItem;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

@SuppressWarnings("unchecked")

/**
 * Created by mike on 5/11/14.
 */
public class HTComboBox<E extends ListItem> extends JComboBox<E> {

    private final static int MIN_WIDTH_CHARS = 5;

    private class ToolTipRenderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList list, Object value,
                                                      int index, boolean isSelected, boolean cellHasFocus) {

            JComponent comp = (JComponent) super.getListCellRendererComponent(list,
                    value, index, isSelected, cellHasFocus);

            if (index > -1 && value != null) {
                E currentItem = items.get(index);
                if (currentItem != null && !currentItem.getTooltip().isEmpty()) {
                    list.setToolTipText(currentItem.getTooltip());
                }
            }
            return comp;
        }
    }

    protected List<E> items;
    protected int widthChars = MIN_WIDTH_CHARS;

    public HTComboBox() {
        setRenderer(new ToolTipRenderer());
        resetWidthChars();
    }

    public HTComboBox(List<E> items) {
        this();
        setItems(items);
    }

    @Nullable
    public List<E> getItems() {
        return items;
    }

    public void setItems(List<E> items) {
        this.items = items != null ? items : new ArrayList<E>();
        setModel(new DefaultComboBoxModel<>(new Vector<>(this.items)));

        resetWidthChars();
    }

    public int getWidthChars() {
        return widthChars;
    }

    public void setWidthChars(int chars) {
        chars = chars < MIN_WIDTH_CHARS ? MIN_WIDTH_CHARS : chars;
        int maxLabelChars = getMaxLabelLength();
        widthChars = chars < maxLabelChars ? maxLabelChars : chars;

        FontMetrics fontMetrics = getFontMetrics(getFont());
        int width = (widthChars * fontMetrics.charWidth('w'));


        Dimension dim = new Dimension(width, getPreferredSize().height);
        setPreferredSize(dim);
    }

    public void resetWidthChars() {
        setWidthChars(0);
    }

    private int getMaxLabelLength() {
        int labelLength = 0;

        if (items != null && !items.isEmpty()) {
            Ordering<E> ordering = new Ordering<E>() {
                @Override
                public int compare(E left, E right) {
                    return Ints.compare(left.getLabel().length(), right.getLabel().length());
                }
            };
            E maxItem = ordering.max(items);
            if (maxItem != null) {
                labelLength = maxItem.getLabel().length();
            }
        }
        return labelLength;
    }
}

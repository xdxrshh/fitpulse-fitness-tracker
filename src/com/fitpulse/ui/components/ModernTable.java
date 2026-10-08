package com.fitpulse.ui.components;

import com.fitpulse.ui.theme.UITheme;
import java.awt.*;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableModel;

/**
 * Modern styled JTable with dark sleek headers, striped alternating rows, and generous cell padding.
 */
public class ModernTable extends JTable {

    public ModernTable(TableModel model) {
        super(model);
        setRowHeight(36);
        setShowGrid(false);
        setIntercellSpacing(new Dimension(0, 0));
        setSelectionBackground(new Color(51, 65, 85));
        setSelectionForeground(UITheme.TEXT_MAIN);
        setFont(UITheme.FONT_BODY);
        setBackground(UITheme.BG_CARD);
        setForeground(UITheme.TEXT_MAIN);

        // Header styling
        JTableHeader header = getTableHeader();
        header.setFont(UITheme.FONT_BOLD);
        header.setBackground(UITheme.BG_DARK);
        header.setForeground(UITheme.TEXT_MUTED);
        header.setPreferredSize(new Dimension(0, 38));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.BORDER_COLOR));

        // Default cell renderer with alternating row colors
        setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (isSelected) {
                    c.setBackground(new Color(55, 65, 81));
                    c.setForeground(Color.WHITE);
                } else {
                    c.setBackground(row % 2 == 0 ? UITheme.BG_CARD : new Color(25, 34, 48));
                    c.setForeground(UITheme.TEXT_MAIN);
                }
                setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                return c;
            }
        });
    }
}

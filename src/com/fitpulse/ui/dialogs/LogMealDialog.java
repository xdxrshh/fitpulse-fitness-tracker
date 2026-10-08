package com.fitpulse.ui.dialogs;

import com.fitpulse.model.enums.MealType;
import com.fitpulse.service.NutritionService;
import com.fitpulse.ui.components.ModernButton;
import com.fitpulse.ui.theme.UITheme;
import java.awt.*;
import java.time.LocalDateTime;
import javax.swing.*;

/**
 * Modal dialog for recording nutrition meals and macros.
 */
public class LogMealDialog extends JDialog {
    private final int userId;
    private final NutritionService nutritionService;
    private final Runnable onMealSaved;

    private JTextField txtMealName;
    private JComboBox<MealType> cmbMealType;
    private JTextField txtCalories;
    private JTextField txtProtein;
    private JTextField txtCarbs;
    private JTextField txtFat;

    public LogMealDialog(Frame parent, int userId, NutritionService nutritionService, Runnable onMealSaved) {
        super(parent, "Log Meal & Nutrition — FitPulse", true);
        this.userId = userId;
        this.nutritionService = nutritionService;
        this.onMealSaved = onMealSaved;
        initUI();
    }

    private void initUI() {
        setSize(420, 480);
        setLocationRelativeTo(getParent());
        setResizable(false);
        getContentPane().setBackground(UITheme.BG_DARK);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(UITheme.BG_DARK);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        JLabel title = new JLabel("Log Meal Nutrition");
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(UITheme.TEXT_MAIN);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(title);
        panel.add(Box.createVerticalStrut(15));

        panel.add(createLabel("Meal Name (e.g. Protein Oatmeal, Chicken Bowl):"));
        txtMealName = createTextField("Grilled Chicken Breast");
        panel.add(txtMealName);
        panel.add(Box.createVerticalStrut(10));

        panel.add(createLabel("Meal Type:"));
        cmbMealType = new JComboBox<>(MealType.values());
        cmbMealType.setFont(UITheme.FONT_BODY);
        cmbMealType.setBackground(UITheme.BG_INPUT);
        cmbMealType.setForeground(UITheme.TEXT_MAIN);
        cmbMealType.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        cmbMealType.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(cmbMealType);
        panel.add(Box.createVerticalStrut(10));

        panel.add(createLabel("Energy / Calories (kcal):"));
        txtCalories = createTextField("450");
        panel.add(txtCalories);
        panel.add(Box.createVerticalStrut(10));

        // Macros in a 3-column row
        JPanel macroPanel = new JPanel(new GridLayout(1, 3, 8, 0));
        macroPanel.setOpaque(false);
        macroPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        macroPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65));

        txtProtein = createTextField("40.0");
        txtCarbs = createTextField("15.0");
        txtFat = createTextField("12.0");

        JPanel pP = new JPanel(new BorderLayout(0, 4));
        pP.setOpaque(false);
        pP.add(createLabel("Protein (g):"), BorderLayout.NORTH);
        pP.add(txtProtein, BorderLayout.CENTER);

        JPanel cP = new JPanel(new BorderLayout(0, 4));
        cP.setOpaque(false);
        cP.add(createLabel("Carbs (g):"), BorderLayout.NORTH);
        cP.add(txtCarbs, BorderLayout.CENTER);

        JPanel fP = new JPanel(new BorderLayout(0, 4));
        fP.setOpaque(false);
        fP.add(createLabel("Fat (g):"), BorderLayout.NORTH);
        fP.add(txtFat, BorderLayout.CENTER);

        macroPanel.add(pP);
        macroPanel.add(cP);
        macroPanel.add(fP);

        panel.add(macroPanel);
        panel.add(Box.createVerticalStrut(20));

        ModernButton btnSave = new ModernButton("Log Meal", UITheme.ACCENT_PRIMARY);
        btnSave.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btnSave.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnSave.addActionListener(e -> saveMeal());
        panel.add(btnSave);

        add(panel);
    }

    private void saveMeal() {
        try {
            String name = txtMealName.getText().trim();
            MealType type = (MealType) cmbMealType.getSelectedItem();
            int cals = Integer.parseInt(txtCalories.getText().trim());
            double p = Double.parseDouble(txtProtein.getText().trim());
            double c = Double.parseDouble(txtCarbs.getText().trim());
            double f = Double.parseDouble(txtFat.getText().trim());

            nutritionService.logMeal(userId, name, type, cals, p, c, f, LocalDateTime.now());
            JOptionPane.showMessageDialog(this, "Meal logged successfully!", "Logged", JOptionPane.INFORMATION_MESSAGE);
            if (onMealSaved != null) onMealSaved.run();
            dispose();
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(this, "Please enter valid numbers for calories and macros.",
                    "Input Error", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JLabel createLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(UITheme.FONT_SMALL);
        l.setForeground(UITheme.TEXT_MUTED);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private JTextField createTextField(String val) {
        JTextField tf = new JTextField(val);
        tf.setFont(UITheme.FONT_BODY);
        tf.setBackground(UITheme.BG_INPUT);
        tf.setForeground(UITheme.TEXT_MAIN);
        tf.setCaretColor(Color.WHITE);
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));
        tf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        tf.setAlignmentX(Component.LEFT_ALIGNMENT);
        return tf;
    }
}

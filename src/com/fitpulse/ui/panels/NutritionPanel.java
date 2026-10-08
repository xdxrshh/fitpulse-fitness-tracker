package com.fitpulse.ui.panels;

import com.fitpulse.model.MealLog;
import com.fitpulse.model.User;
import com.fitpulse.service.NutritionService;
import com.fitpulse.ui.components.ModernButton;
import com.fitpulse.ui.components.ModernTable;
import com.fitpulse.ui.components.StatCard;
import com.fitpulse.ui.dialogs.LogMealDialog;
import com.fitpulse.ui.theme.UITheme;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

/**
 * Nutrition tracking panel managing daily meals, calorie intake, and macronutrient balance.
 */
public class NutritionPanel extends JPanel {
    private final Frame parentFrame;
    private final User currentUser;
    private final NutritionService nutritionService;

    private StatCard cardTotalCal;
    private StatCard cardProtein;
    private StatCard cardCarbs;
    private StatCard cardFat;

    private DefaultTableModel tableModel;
    private ModernTable table;

    public NutritionPanel(Frame parent, User user, NutritionService nutritionService) {
        this.parentFrame = parent;
        this.currentUser = user;
        this.nutritionService = nutritionService;

        setLayout(new BorderLayout(14, 14));
        setBackground(UITheme.BG_DARK);
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        initHeader();
        initStats();
        initTable();
        loadNutritionData();
    }

    private void initHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);

        JLabel lblTitle = new JLabel("Daily Nutrition & Calorie Tracking");
        lblTitle.setFont(UITheme.FONT_TITLE);
        lblTitle.setForeground(UITheme.TEXT_MAIN);

        JLabel lblSub = new JLabel("Track your macronutrients (Protein, Carbs, Fat) and total energy intake for today.");
        lblSub.setFont(UITheme.FONT_BODY);
        lblSub.setForeground(UITheme.TEXT_MUTED);

        left.add(lblTitle);
        left.add(Box.createVerticalStrut(4));
        left.add(lblSub);

        ModernButton btnLogMeal = new ModernButton("+ Log Meal", UITheme.ACCENT_PRIMARY);
        btnLogMeal.addActionListener(e -> {
            LogMealDialog dlg = new LogMealDialog(parentFrame, currentUser.getId(), nutritionService, this::loadNutritionData);
            dlg.setVisible(true);
        });

        header.add(left, BorderLayout.WEST);
        header.add(btnLogMeal, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);
    }

    private void initStats() {
        JPanel grid = new JPanel(new GridLayout(1, 4, 14, 0));
        grid.setOpaque(false);
        grid.setPreferredSize(new Dimension(0, 110));

        cardTotalCal = new StatCard("Total Calories", "0 kcal", "Today's intake", UITheme.ACCENT_PRIMARY);
        cardProtein = new StatCard("Protein", "0 g", "Muscle synthesis", UITheme.ACCENT_TEAL);
        cardCarbs = new StatCard("Carbohydrates", "0 g", "Energy stores", UITheme.ACCENT_WARNING);
        cardFat = new StatCard("Dietary Fat", "0 g", "Hormonal health", UITheme.ACCENT_SUCCESS);

        grid.add(cardTotalCal);
        grid.add(cardProtein);
        grid.add(cardCarbs);
        grid.add(cardFat);

        JPanel centerPanel = new JPanel(new BorderLayout(0, 14));
        centerPanel.setOpaque(false);
        centerPanel.add(grid, BorderLayout.NORTH);

        add(centerPanel, BorderLayout.CENTER);
    }

    private void initTable() {
        JPanel tableContainer = new JPanel(new BorderLayout(0, 10));
        tableContainer.setOpaque(false);

        JLabel lblTableTitle = new JLabel("Today's Food & Meal Diary");
        lblTableTitle.setFont(UITheme.FONT_SUBTITLE);
        lblTableTitle.setForeground(UITheme.TEXT_MAIN);
        tableContainer.add(lblTableTitle, BorderLayout.NORTH);

        String[] cols = {"ID", "Meal Name", "Type", "Calories", "Protein (g)", "Carbs (g)", "Fat (g)", "Time Logged"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new ModernTable(tableModel);
        table.getColumnModel().getColumn(0).setMaxWidth(50);

        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(UITheme.BG_CARD);
        scroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1));
        tableContainer.add(scroll, BorderLayout.CENTER);

        // Add to center
        BorderLayout layout = (BorderLayout) getLayout();
        Component centerComp = layout.getLayoutComponent(BorderLayout.CENTER);
        if (centerComp instanceof JPanel) {
            ((JPanel) centerComp).add(tableContainer, BorderLayout.CENTER);
        }
    }

    public void loadNutritionData() {
        try {
            int totalCal = nutritionService.getTodayCalorieIntake(currentUser.getId());
            Map<String, Double> macros = nutritionService.getTodayMacroBreakdown(currentUser.getId());
            List<MealLog> meals = nutritionService.getUserMealsForToday(currentUser.getId());

            cardTotalCal.setValue(totalCal + " kcal");
            cardProtein.setValue(String.format("%.1f g", macros.getOrDefault("proteinGrams", 0.0)));
            cardCarbs.setValue(String.format("%.1f g", macros.getOrDefault("carbsGrams", 0.0)));
            cardFat.setValue(String.format("%.1f g", macros.getOrDefault("fatGrams", 0.0)));

            tableModel.setRowCount(0);
            DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("hh:mm a");
            for (MealLog m : meals) {
                tableModel.addRow(new Object[]{
                        m.getId(),
                        m.getMealName(),
                        m.getMealType(),
                        m.getCalories() + " kcal",
                        m.getProteinGrams() + " g",
                        m.getCarbsGrams() + " g",
                        m.getFatGrams() + " g",
                        m.getLoggedAt().format(timeFmt)
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to load nutrition data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ExpenseTracker extends JFrame {
    private JTextField amountField, categoryField, descField;
    private JTable table;
    private DefaultTableModel model;

    private Connection conn;

    public ExpenseTracker() {
        setTitle("Expense Tracker");
        setSize(700, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Connect to DB
        connectToDatabase();

        // UI Components
        amountField = new JTextField(10);
        categoryField = new JTextField(10);
        descField = new JTextField(15);

        JButton addButton = new JButton("Add Expense");
        addButton.addActionListener(e -> addExpense());

        model = new DefaultTableModel(new String[]{"ID", "Amount", "Category", "Description", "Date"}, 0);
        table = new JTable(model);

        loadExpenses();

        JPanel inputPanel = new JPanel();
        inputPanel.add(new JLabel("Amount:"));
        inputPanel.add(amountField);
        inputPanel.add(new JLabel("Category:"));
        inputPanel.add(categoryField);
        inputPanel.add(new JLabel("Description:"));
        inputPanel.add(descField);
        inputPanel.add(addButton);

        add(inputPanel, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    private void connectToDatabase() {
        try {
            // Load SQLite JDBC driver
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection("jdbc:sqlite:expenses.db");
            Statement stmt = conn.createStatement();
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS expenses (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    amount REAL,
                    category TEXT,
                    description TEXT,
                    date TEXT
                )
            """);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database connection failed.");
            System.exit(1);
        }
    }

    private void addExpense() {
        try {
            double amount = Double.parseDouble(amountField.getText());
            String category = categoryField.getText();
            String description = descField.getText();
            String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            PreparedStatement ps = conn.prepareStatement("INSERT INTO expenses (amount, category, description, date) VALUES (?, ?, ?, ?)");
            ps.setDouble(1, amount);
            ps.setString(2, category);
            ps.setString(3, description);
            ps.setString(4, date);
            ps.executeUpdate();

            model.addRow(new Object[]{getLastInsertId(), amount, category, description, date});

            amountField.setText("");
            categoryField.setText("");
            descField.setText("");
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(this, "Please enter a valid amount.");
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to add expense.");
        }
    }

    private void loadExpenses() {
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM expenses");
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getDouble("amount"),
                        rs.getString("category"),
                        rs.getString("description"),
                        rs.getString("date")
                });
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private int getLastInsertId() throws SQLException {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT last_insert_rowid() AS last_id");
        return rs.next() ? rs.getInt("last_id") : -1;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ExpenseTracker().setVisible(true));
    }
}

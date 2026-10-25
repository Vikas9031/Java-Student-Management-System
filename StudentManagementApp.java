import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

public class StudentManagementApp extends JFrame {

    private JTextField rollField;
    private JTextField nameField;
    private JTextField courseField;
    private JTable table;
    private DefaultTableModel model;

    private int selectedId = -1;

    private final String url = "jdbc:mysql://localhost:3306/student_db";
    private final String user = "root";

    public StudentManagementApp() {
        setTitle("Student Management System");
        setSize(850, 550);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        createUI();
        loadStudents();
    }

    private Connection getConnection() throws SQLException {
        // Password Java source code mein nahi hai; Windows environment variable se aayega.
        String password = System.getenv("DB_PASSWORD");

        if (password == null || password.isBlank()) {
            throw new SQLException(
                "DB_PASSWORD environment variable set nahi hai. " +
                "VS Code terminal mein pehle $env:DB_PASSWORD=\"your_mysql_password\" chalayein."
            );
        }

        return DriverManager.getConnection(url, user, password);
    }

    private void createUI() {
        setLayout(new BorderLayout(10, 10));

        JLabel heading = new JLabel("Student Management System", JLabel.CENTER);
        heading.setFont(new Font("Arial", Font.BOLD, 24));

        JPanel form = new JPanel(new GridLayout(3, 2, 8, 8));
        form.setBorder(BorderFactory.createTitledBorder("Student Details"));

        rollField = new JTextField();
        nameField = new JTextField();
        courseField = new JTextField();

        form.add(new JLabel("Roll Number:"));
        form.add(rollField);
        form.add(new JLabel("Student Name:"));
        form.add(nameField);
        form.add(new JLabel("Course:"));
        form.add(courseField);

        JButton addButton = new JButton("Add Student");
        JButton updateButton = new JButton("Update");
        JButton deleteButton = new JButton("Delete");
        JButton searchButton = new JButton("Search Roll No");
        JButton clearButton = new JButton("Clear");
        JButton refreshButton = new JButton("Refresh");

        JPanel buttons = new JPanel(new FlowLayout());
        buttons.add(addButton);
        buttons.add(updateButton);
        buttons.add(deleteButton);
        buttons.add(searchButton);
        buttons.add(clearButton);
        buttons.add(refreshButton);

        JPanel topPanel = new JPanel(new BorderLayout(5, 5));
        topPanel.add(heading, BorderLayout.NORTH);
        topPanel.add(form, BorderLayout.CENTER);
        topPanel.add(buttons, BorderLayout.SOUTH);

        model = new DefaultTableModel(
            new String[] {"ID", "Roll Number", "Name", "Course"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        add(topPanel, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = table.getSelectedRow();

                if (row >= 0) {
                    selectedId = Integer.parseInt(model.getValueAt(row, 0).toString());
                    rollField.setText(model.getValueAt(row, 1).toString());
                    nameField.setText(model.getValueAt(row, 2).toString());
                    courseField.setText(model.getValueAt(row, 3).toString());
                }
            }
        });

        addButton.addActionListener(e -> addStudent());
        updateButton.addActionListener(e -> updateStudent());
        deleteButton.addActionListener(e -> deleteStudent());
        searchButton.addActionListener(e -> searchStudent());
        clearButton.addActionListener(e -> clearFields());
        refreshButton.addActionListener(e -> loadStudents());
    }

    private boolean validateInput() {
        if (rollField.getText().trim().isEmpty()
                || nameField.getText().trim().isEmpty()
                || courseField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields.");
            return false;
        }
        return true;
    }

    private void addStudent() {
        if (!validateInput()) {
            return;
        }

        String sql = "INSERT INTO students (roll_no, name, course) VALUES (?, ?, ?)";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, rollField.getText().trim());
            ps.setString(2, nameField.getText().trim());
            ps.setString(3, courseField.getText().trim());
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Student added successfully!");
            clearFields();
            loadStudents();
        } catch (SQLException ex) {
            showDatabaseError(ex);
        }
    }

    private void updateStudent() {
        if (selectedId == -1) {
            JOptionPane.showMessageDialog(this, "Select a student from the table first.");
            return;
        }
        if (!validateInput()) {
            return;
        }

        String sql = "UPDATE students SET roll_no=?, name=?, course=? WHERE id=?";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, rollField.getText().trim());
            ps.setString(2, nameField.getText().trim());
            ps.setString(3, courseField.getText().trim());
            ps.setInt(4, selectedId);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Student updated successfully!");
            clearFields();
            loadStudents();
        } catch (SQLException ex) {
            showDatabaseError(ex);
        }
    }

    private void deleteStudent() {
        if (selectedId == -1) {
            JOptionPane.showMessageDialog(this, "Select a student from the table first.");
            return;
        }

        int choice = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to delete this student?",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION
        );

        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        String sql = "DELETE FROM students WHERE id=?";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, selectedId);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Student deleted successfully!");
            clearFields();
            loadStudents();
        } catch (SQLException ex) {
            showDatabaseError(ex);
        }
    }

    private void searchStudent() {
        String roll = rollField.getText().trim();

        if (roll.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter a roll number to search.");
            return;
        }

        String sql = "SELECT id, roll_no, name, course FROM students WHERE roll_no=?";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, roll);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    selectedId = rs.getInt("id");
                    rollField.setText(rs.getString("roll_no"));
                    nameField.setText(rs.getString("name"));
                    courseField.setText(rs.getString("course"));
                    JOptionPane.showMessageDialog(this, "Student found!");
                } else {
                    selectedId = -1;
                    JOptionPane.showMessageDialog(this, "Student not found.");
                }
            }
        } catch (SQLException ex) {
            showDatabaseError(ex);
        }
    }

    private void loadStudents() {
        if (model == null) {
            return;
        }

        model.setRowCount(0);
        String sql = "SELECT id, roll_no, name, course FROM students ORDER BY id";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                model.addRow(new Object[] {
                    rs.getInt("id"),
                    rs.getString("roll_no"),
                    rs.getString("name"),
                    rs.getString("course")
                });
            }
        } catch (SQLException ex) {
            showDatabaseError(ex);
        }
    }

    private void clearFields() {
        rollField.setText("");
        nameField.setText("");
        courseField.setText("");
        selectedId = -1;
        table.clearSelection();
    }

    private void showDatabaseError(SQLException ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(
            this,
            "Database Error:\n" + ex.getMessage(),
            "Error",
            JOptionPane.ERROR_MESSAGE
        );
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            StudentManagementApp app = new StudentManagementApp();
            app.setVisible(true);
        });
    }
}

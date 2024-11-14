package com.cecilia.registrationform;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import javax.swing.table.DefaultTableModel;

public class RegistrationForm extends JFrame {
    private JTextField nameField, mobileField, addressField;
    private JRadioButton male, female;
    private JComboBox<String> dobDay, dobMonth, dobYear;
    private JCheckBox termsCheck;
    private JButton submitButton, resetButton;
    private JTable dataTable;
    private ButtonGroup genderGroup;

    public RegistrationForm() {
        setTitle("Registration Form");
        setSize(500, 400);
        setLayout(new BorderLayout());

        JPanel inputPanel = new JPanel(new GridLayout(7, 2));
        
        inputPanel.add(new JLabel("Name:"));
        nameField = new JTextField();
        inputPanel.add(nameField);

        inputPanel.add(new JLabel("Mobile:"));
        mobileField = new JTextField();
        inputPanel.add(mobileField);

        inputPanel.add(new JLabel("Gender:"));
        male = new JRadioButton("Male");
        female = new JRadioButton("Female");
        genderGroup = new ButtonGroup();
        genderGroup.add(male);
        genderGroup.add(female);
        JPanel genderPanel = new JPanel();
        genderPanel.add(male);
        genderPanel.add(female);
        inputPanel.add(genderPanel);

        inputPanel.add(new JLabel("DOB:"));
        dobDay = new JComboBox<>(new String[] {"1", "2", "3", "...", "31"});
        dobMonth = new JComboBox<>(new String[] {"Jan", "Feb", "Mar", "..."});
        dobYear = new JComboBox<>(new String[] {"1990", "1991", "1992", "..."});
        JPanel dobPanel = new JPanel();
        dobPanel.add(dobDay);
        dobPanel.add(dobMonth);
        dobPanel.add(dobYear);
        inputPanel.add(dobPanel);

        inputPanel.add(new JLabel("Address:"));
        addressField = new JTextField();
        inputPanel.add(addressField);

        termsCheck = new JCheckBox("Accept Terms And Conditions");
        inputPanel.add(termsCheck);

        submitButton = new JButton("Submit");
        resetButton = new JButton("Reset");
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(submitButton);
        buttonPanel.add(resetButton);

        inputPanel.add(buttonPanel);

        add(inputPanel, BorderLayout.WEST);

        dataTable = new JTable();
        JScrollPane tableScrollPane = new JScrollPane(dataTable);
        add(tableScrollPane, BorderLayout.CENTER);

        submitButton.addActionListener(e -> submitData());
        resetButton.addActionListener(e -> resetForm());

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);

        createDatabase();  // Creating database when the form is initialized
    }

    private void createDatabase() {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:registration.db")) {
            String createTableQuery = "CREATE TABLE IF NOT EXISTS users (" +
                                      "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                      "name TEXT NOT NULL, " +
                                      "mobile TEXT, " +
                                      "gender TEXT, " +
                                      "dob TEXT, " +
                                      "address TEXT)";
            try (Statement stmt = conn.createStatement()) {
                stmt.execute(createTableQuery);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error creating the database or table: " + e.getMessage());
        }
    }

    private void submitData() {
        String name = nameField.getText();
        String mobile = mobileField.getText();
        String gender = male.isSelected() ? "Male" : "Female";
        String dob = dobDay.getSelectedItem() + "-" + dobMonth.getSelectedItem() + "-" + dobYear.getSelectedItem();
        String address = addressField.getText();

        if (!termsCheck.isSelected()) {
            JOptionPane.showMessageDialog(this, "Please accept the terms and conditions.");
            return;
        }

        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DriverManager.getConnection("jdbc:sqlite:registration.db");
            String query = "INSERT INTO users (name, mobile, gender, dob, address) VALUES (?, ?, ?, ?, ?)";
            pstmt = conn.prepareStatement(query);
            pstmt.setString(1, name);
            pstmt.setString(2, mobile);
            pstmt.setString(3, gender);
            pstmt.setString(4, dob);
            pstmt.setString(5, address);
            
            pstmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Data submitted successfully!");
            loadDataToTable();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error saving data: " + e.getMessage());
        } finally {
            try {
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void loadDataToTable() {
        DefaultTableModel tableModel = new DefaultTableModel();
        tableModel.addColumn("ID");
        tableModel.addColumn("Name");
        tableModel.addColumn("Mobile");
        tableModel.addColumn("Gender");
        tableModel.addColumn("DOB");
        tableModel.addColumn("Address");

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:registration.db");
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM users")) {
            
            while (rs.next()) {
                Object[] row = new Object[6];
                row[0] = rs.getInt("id");
                row[1] = rs.getString("name");
                row[2] = rs.getString("mobile");
                row[3] = rs.getString("gender");
                row[4] = rs.getString("dob");
                row[5] = rs.getString("address");
                tableModel.addRow(row);
            }

            dataTable.setModel(tableModel); 
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading data: " + e.getMessage());
        }
    }

    private void resetForm() {
        nameField.setText("");
        mobileField.setText("");
        genderGroup.clearSelection();
        dobDay.setSelectedIndex(0);
        dobMonth.setSelectedIndex(0);
        dobYear.setSelectedIndex(0);
        addressField.setText("");
        termsCheck.setSelected(false);
    }

    public static void main(String[] args) {
        new RegistrationForm();
    }
}
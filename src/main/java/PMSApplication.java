import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class PMSApplication extends JFrame {

    private JTable studentTable;
    private JTable subjectTable;
    private DefaultTableModel studentTableModel;
    private DefaultTableModel subjectTableModel;
    private JTextArea infoArea;

    public PMSApplication() {
        setTitle("Student-Subject Management System");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        JPanel mainPanel = new JPanel(new GridLayout(1, 2, 10, 10));

        JPanel studentPanel = createStudentPanel();
        mainPanel.add(studentPanel);

        JPanel subjectPanel = createSubjectPanel();
        mainPanel.add(subjectPanel);

        infoArea = new JTextArea(5, 50);
        infoArea.setEditable(false);
        infoArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane infoScroll = new JScrollPane(infoArea);

        JPanel controlPanel = createControlPanel();

        add(mainPanel, BorderLayout.CENTER);
        add(infoScroll, BorderLayout.SOUTH);
        add(controlPanel, BorderLayout.NORTH);

        loadData();
    }

    private JPanel createStudentPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Students"));

        String[] columns = {"ID", "Name"};
        studentTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        studentTable = new JTable(studentTableModel);
        JScrollPane scrollPane = new JScrollPane(studentTable);

        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createSubjectPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Subjects"));


        String[] columns = {"ID", "Name", "Description"};
        subjectTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        subjectTable = new JTable(subjectTableModel);
        JScrollPane scrollPane = new JScrollPane(subjectTable);

        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

        JButton addSubjectBtn = new JButton("Add Subject to Student");
        JButton removeSubjectBtn = new JButton("Remove Subject from Student");
        JButton viewStudentSubjectsBtn = new JButton("View Student's Subjects");
        JButton refreshBtn = new JButton("Refresh");

        addSubjectBtn.addActionListener(e -> addSubjectToStudent());
        removeSubjectBtn.addActionListener(e -> removeSubjectFromStudent());
        viewStudentSubjectsBtn.addActionListener(e -> viewStudentSubjects());
        refreshBtn.addActionListener(e -> loadData());

        panel.add(addSubjectBtn);
        panel.add(removeSubjectBtn);
        panel.add(viewStudentSubjectsBtn);
        panel.add(refreshBtn);

        return panel;
    }

    private void loadData() {
        studentTableModel.setRowCount(0);
        List<Student> students = DatabaseManager.getAllStudents();
        for (Student student : students) {
            studentTableModel.addRow(new Object[]{
                    student.getId(),
                    student.getFirstName() + " " + student.getLastName()
            });
        }

        subjectTableModel.setRowCount(0);
        List<Subject> subjects = DatabaseManager.getAllSubjects();
        for (Subject subject : subjects) {
            subjectTableModel.addRow(new Object[]{
                    subject.getId(),
                    subject.getName(),
                    subject.getDescription()
            });
        }

        infoArea.setText("Data loaded successfully!");
    }

    private void addSubjectToStudent() {
        int studentRow = studentTable.getSelectedRow();
        int subjectRow = subjectTable.getSelectedRow();

        if (studentRow == -1 || subjectRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select both a student and a subject!",
                    "Selection Required",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        Long studentId = (Long) studentTableModel.getValueAt(studentRow, 0);
        Long subjectId = (Long) subjectTableModel.getValueAt(subjectRow, 0);

        DatabaseManager.addSubjectToStudent(studentId, subjectId);

        String studentName = (String) studentTableModel.getValueAt(studentRow, 1);
        String subjectName = (String) subjectTableModel.getValueAt(subjectRow, 1);

        infoArea.setText("Added subject '" + subjectName + "' to student '" + studentName + "'");
    }

    private void removeSubjectFromStudent() {
        int studentRow = studentTable.getSelectedRow();
        int subjectRow = subjectTable.getSelectedRow();

        if (studentRow == -1 || subjectRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select both a student and a subject!",
                    "Selection Required",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        Long studentId = (Long) studentTableModel.getValueAt(studentRow, 0);
        Long subjectId = (Long) subjectTableModel.getValueAt(subjectRow, 0);

        DatabaseManager.removeSubjectFromStudent(studentId, subjectId);

        String studentName = (String) studentTableModel.getValueAt(studentRow, 1);
        String subjectName = (String) subjectTableModel.getValueAt(subjectRow, 1);

        infoArea.setText("Removed subject '" + subjectName + "' from student '" + studentName + "'");
    }

    private void viewStudentSubjects() {
        int studentRow = studentTable.getSelectedRow();

        if (studentRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a student!",
                    "Selection Required",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        Long studentId = (Long) studentTableModel.getValueAt(studentRow, 0);
        Student student = DatabaseManager.getStudentById(studentId);
        DatabaseManager.refresh(student);

        StringBuilder sb = new StringBuilder();
        sb.append(student.getFirstName()).append(" ").append(student.getLastName())
                .append("'s Subjects:\n");
        sb.append("----------------------------------------\n");

        if (student.getSubjects().isEmpty()) {
            sb.append("No subjects enrolled yet.\n");
        } else {
            for (Subject subject : student.getSubjects()) {
                sb.append("- ").append(subject.getName()).append("\n");
            }
        }

        infoArea.setText(sb.toString());
    }

    public static void main(String[] args) {
        // Initialize database
        DatabaseManager.initialize();

        // Initialize sample data if needed
        initializeSampleData();

        // Run GUI
        SwingUtilities.invokeLater(() -> {
            PMSApplication app = new PMSApplication();
            app.setVisible(true);
        });

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            DatabaseManager.close();
        }));
    }

    private static void initializeSampleData() {
        List<Student> existingStudents = DatabaseManager.getAllStudents();
        List<Subject> existingSubjects = DatabaseManager.getAllSubjects();

        if (existingStudents.isEmpty()) {
            Student s1 = new Student("Evgenev", "Ali");
            Student s2 = new Student("John", "Smith");
            Student s3 = new Student("Emma", "Johnson");
            Student s4 = new Student("Michael", "Brown");
            Student s5 = new Student("Sarah", "Davis");

            DatabaseManager.saveStudent(s1);
            DatabaseManager.saveStudent(s2);
            DatabaseManager.saveStudent(s3);
            DatabaseManager.saveStudent(s4);
            DatabaseManager.saveStudent(s5);
        }

        if (existingSubjects.isEmpty()) {
            Subject sub1 = new Subject("Math", "Mathematics and Calculus");
            Subject sub2 = new Subject("Physics", "Classical and Modern Physics");
            Subject sub3 = new Subject("Chemistry", "Organic and Inorganic Chemistry");
            Subject sub4 = new Subject("Computer Science", "Programming and Algorithms");
            Subject sub5 = new Subject("English", "Literature and Writing");

            DatabaseManager.saveSubject(sub1);
            DatabaseManager.saveSubject(sub2);
            DatabaseManager.saveSubject(sub3);
            DatabaseManager.saveSubject(sub4);
            DatabaseManager.saveSubject(sub5);
        }
    }
}
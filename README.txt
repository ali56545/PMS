# STUDENT-SUBJECT MANAGEMENT SYSTEM (PMS)
## Complete Technical Documentation

================================================================================
TABLE OF CONTENTS
================================================================================
1. Project Overview
2. System Architecture
3. Database Design
4. Java Classes Detailed Explanation
5. Configuration Files
6. Setup Instructions
7. How Everything Works Together
8. Troubleshooting Guide

================================================================================
1. PROJECT OVERVIEW
================================================================================

This is a Java-based Project Management System (PMS) that manages relationships
between Students and Subjects using a PostgreSQL database with a Swing GUI.

KEY FEATURES:
- Many-to-Many relationship: Multiple students can enroll in multiple subjects
- 5 pre-populated students and 5 subjects
- GUI interface for managing enrollments
- PostgreSQL database with JPA/Hibernate ORM
- CRUD operations through a user-friendly interface

TECHNOLOGIES:
- Java 8+
- PostgreSQL (relational database)
- Hibernate 5.6.15 (ORM framework)
- JPA 2.2 (Java Persistence API)
- Swing (GUI framework)
- Maven (dependency management)

================================================================================
2. SYSTEM ARCHITECTURE
================================================================================

ARCHITECTURE PATTERN: Layered Architecture

[Presentation Layer - GUI]
         |
         v
[Business Logic Layer - DatabaseManager]
         |
         v
[Persistence Layer - JPA/Hibernate]
         |
         v
[Database Layer - PostgreSQL]

COMPONENT BREAKDOWN:

1. ENTITY LAYER (Student.java, Subject.java)
   - Represents database tables as Java objects
   - Contains JPA annotations for ORM mapping
   - Manages bidirectional relationships

2. DATA ACCESS LAYER (DatabaseManager.java)
   - Provides CRUD operations
   - Manages transactions
   - Acts as a bridge between GUI and database

3. PRESENTATION LAYER (PMSApplication.java)
   - Swing-based GUI
   - Event handling
   - Data visualization in tables

4. CONFIGURATION LAYER (persistence.xml, pom.xml)
   - Database connection settings
   - Hibernate configuration
   - Maven dependencies

================================================================================
3. DATABASE DESIGN
================================================================================

DATABASE SCHEMA:

TABLE: students
+------------+--------------+------+-----+---------+----------------+
| Field      | Type         | Null | Key | Default | Extra          |
+------------+--------------+------+-----+---------+----------------+
| id         | SERIAL       | NO   | PRI | NULL    | auto_increment |
| first_name | VARCHAR(100) | NO   |     | NULL    |                |
| last_name  | VARCHAR(100) | NO   |     | NULL    |                |
+------------+--------------+------+-----+---------+----------------+

TABLE: subjects
+-------------+--------------+------+-----+---------+----------------+
| Field       | Type         | Null | Key | Default | Extra          |
+-------------+--------------+------+-----+---------+----------------+
| id          | SERIAL       | NO   | PRI | NULL    | auto_increment |
| name        | VARCHAR(100) | NO   | UNI | NULL    |                |
| description | VARCHAR(255) | YES  |     | NULL    |                |
+-------------+--------------+------+-----+---------+----------------+

TABLE: student_subjects (Junction Table for Many-to-Many)
+------------+---------+------+-----+---------+-------+
| Field      | Type    | Null | Key | Default | Extra |
+------------+---------+------+-----+---------+-------+
| student_id | INTEGER | NO   | PRI | NULL    |       |
| subject_id | INTEGER | NO   | PRI | NULL    |       |
+------------+---------+------+-----+---------+-------+
Foreign Keys:
- student_id REFERENCES students(id) ON DELETE CASCADE
- subject_id REFERENCES subjects(id) ON DELETE CASCADE

RELATIONSHIP EXPLANATION:

MANY-TO-MANY RELATIONSHIP:
- One student can take many subjects
- One subject can have many students
- Implemented using a junction table (student_subjects)

Example Data Flow:
Student "Evgenev Ali" (id=1) enrolls in "Math" (id=1)
Result: INSERT INTO student_subjects (student_id, subject_id) VALUES (1, 1);

CASCADE DELETE:
- If a student is deleted, all their enrollments are automatically deleted
- If a subject is deleted, all enrollments for that subject are deleted

================================================================================
4. JAVA CLASSES DETAILED EXPLANATION
================================================================================

--------------------------------------------------------------------------------
4.1 Student.java - Entity Class
--------------------------------------------------------------------------------

PURPOSE:
Represents a student record in the database. Maps to the 'students' table.

KEY ANNOTATIONS:

@Entity
- Marks this class as a JPA entity (database table)
- Hibernate will manage this class and create/update the table

@Table(name = "students")
- Specifies the table name in the database
- Without this, table would be named "Student" (class name)

@Id
- Marks the primary key field
- Every entity must have exactly one @Id field

@GeneratedValue(strategy = GenerationType.IDENTITY)
- Automatic ID generation
- IDENTITY: Database generates ID using auto-increment (SERIAL in PostgreSQL)
- Other strategies: AUTO, SEQUENCE, TABLE

@Column(name = "first_name", nullable = false)
- Maps Java field to database column
- name: Column name in database (snake_case)
- nullable = false: Creates NOT NULL constraint in database

@ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
- Defines many-to-many relationship with Subject
- cascade: Operations that propagate to related entities
  * PERSIST: When saving student, also save new subjects
  * MERGE: When updating student, also update subjects
  * Other options: REMOVE, REFRESH, DETACH, ALL

@JoinTable
- Creates junction table for many-to-many relationship
- name: "student_subjects" (junction table name)
- joinColumns: Column for this entity (student_id)
- inverseJoinColumns: Column for related entity (subject_id)

FIELDS:

private Long id;
- Primary key
- Long (not long) because can be null before persisting
- Auto-generated by database

private String firstName;
private String lastName;
- Student's name
- Maps to first_name and last_name in database

private Set<Subject> subjects = new HashSet<>();
- Collection of subjects this student is enrolled in
- Set: No duplicate subjects allowed
- HashSet: Fast lookup and insertion
- Initialized to avoid NullPointerException

METHODS:

public Student() {}
- Default constructor
- REQUIRED by JPA/Hibernate
- Hibernate uses reflection to create instances

public Student(String firstName, String lastName) {...}
- Convenience constructor for creating students
- Does not set ID (database generates it)

public void addSubject(Subject subject) {
    this.subjects.add(subject);
    subject.getStudents().add(this);
}
- Helper method for bidirectional relationship
- Adds subject to student's collection
- IMPORTANT: Also adds student to subject's collection
- Maintains consistency on both sides of relationship

public void removeSubject(Subject subject) {...}
- Removes subject from both sides of relationship
- Ensures data consistency

--------------------------------------------------------------------------------
4.2 Subject.java - Entity Class
--------------------------------------------------------------------------------

PURPOSE:
Represents a subject/course in the database. Maps to 'subjects' table.

KEY DIFFERENCES FROM STUDENT:

@Column(name = "name", nullable = false, unique = true)
- unique = true: Creates UNIQUE constraint in database
- No two subjects can have the same name
- Database will reject duplicate insertions

@Column(name = "description")
- No nullable constraint: field is optional
- Can be NULL in database

@ManyToMany(mappedBy = "subjects")
- INVERSE SIDE of the relationship
- mappedBy: Points to the field in Student that owns the relationship
- Subject does NOT create a @JoinTable
- Uses the same junction table created by Student
- This prevents creating duplicate junction tables

WHY mappedBy?

In a bidirectional many-to-many:
- ONE side must be the "owner" (defines @JoinTable)
- OTHER side is "inverse" (uses mappedBy)
- Owner side: Student (has @JoinTable)
- Inverse side: Subject (has mappedBy)

Without mappedBy, both sides would try to create their own junction tables:
- student_subjects (from Student)
- subject_students (from Subject)
This would cause data inconsistency!

--------------------------------------------------------------------------------
4.3 DatabaseManager.java - Data Access Layer
--------------------------------------------------------------------------------

PURPOSE:
Manages all database operations. Acts as a singleton to provide centralized
database access throughout the application.

DESIGN PATTERN: Singleton + Static Methods

CORE COMPONENTS:

private static EntityManagerFactory emf;
- Factory for creating EntityManager instances
- EXPENSIVE to create: Should be created once per application
- Thread-safe: Can be shared across the application
- Reads persistence.xml configuration

private static EntityManager em;
- Main interface for database operations
- Manages entity lifecycle (persist, merge, remove, find)
- NOT thread-safe: Should not be shared across threads
- In production, use EntityManager per request/transaction

INITIALIZATION:

public static void initialize() {
    emf = Persistence.createEntityManagerFactory("PMSPersistenceUnit");
    em = emf.createEntityManager();
}

WHAT HAPPENS:
1. Persistence.createEntityManagerFactory():
   - Searches for META-INF/persistence.xml in classpath
   - Finds persistence-unit named "PMSPersistenceUnit"
   - Reads database connection properties
   - Loads Hibernate configuration
   - Connects to PostgreSQL
   - Validates entity classes
   - Creates/updates database schema (if hbm2ddl.auto = update)

2. emf.createEntityManager():
   - Creates EntityManager for database operations
   - This is the object we use for CRUD operations

TRANSACTION MANAGEMENT:

EntityTransaction tx = em.getTransaction();
tx.begin();
// ... database operations ...
tx.commit();

WHY TRANSACTIONS?

ACID Properties:
- Atomicity: All operations succeed or all fail (no partial updates)
- Consistency: Database remains in valid state
- Isolation: Concurrent transactions don't interfere
- Durability: Committed changes are permanent

Example without transaction:
1. Insert student (succeeds)
2. Insert subject (fails)
Result: Student inserted but no subject! Database inconsistent!

Example with transaction:
1. tx.begin()
2. Insert student
3. Insert subject (fails)
4. tx.rollback()
Result: Neither inserted! Database remains consistent!

CRUD OPERATIONS:

1. CREATE (persist):
public static void saveStudent(Student student) {
    EntityTransaction tx = em.getTransaction();
    try {
        tx.begin();
        em.persist(student);
        tx.commit();
    } catch (Exception e) {
        if (tx.isActive()) tx.rollback();
        e.printStackTrace();
    }
}

em.persist(student):
- Marks entity as managed
- Generates SQL: INSERT INTO students (first_name, last_name) VALUES (?, ?);
- Database auto-generates ID
- After commit, student.getId() will have the generated ID

2. READ (find):
public static Student getStudentById(Long id) {
    return em.find(Student.class, id);
}

em.find(Student.class, id):
- Generates SQL: SELECT * FROM students WHERE id = ?;
- Returns null if not found
- Returns managed entity if found

3. READ ALL (query):
public static List<Student> getAllStudents() {
    TypedQuery<Student> query = em.createQuery("SELECT s FROM Student s", Student.class);
    return query.getResultList();
}

JPQL vs SQL:
- JPQL: SELECT s FROM Student s (uses Java class name)
- SQL: SELECT * FROM students (uses table name)
- Hibernate translates JPQL to SQL based on entity mappings

4. UPDATE (merge):
em.merge(student);
- Updates existing entity in database
- Generates SQL: UPDATE students SET first_name = ?, last_name = ? WHERE id = ?;
- Also updates junction table if relationships changed

5. DELETE (remove):
em.remove(student);
- Deletes entity from database
- Generates SQL: DELETE FROM students WHERE id = ?;
- CASCADE DELETE removes related records in junction table

RELATIONSHIP OPERATIONS:

public static void addSubjectToStudent(Long studentId, Long subjectId) {
    EntityTransaction tx = em.getTransaction();
    try {
        tx.begin();
        Student student = em.find(Student.class, studentId);
        Subject subject = em.find(Subject.class, subjectId);
        
        if (student != null && subject != null) {
            student.addSubject(subject);
            em.merge(student);
        }
        tx.commit();
    } catch (Exception e) {
        if (tx.isActive()) tx.rollback();
        e.printStackTrace();
    }
}

STEP-BY-STEP:
1. Start transaction
2. Load student from database
3. Load subject from database
4. Call student.addSubject(subject):
   - Adds subject to student's Set
   - Adds student to subject's Set (bidirectional)
5. em.merge(student):
   - Hibernate detects relationship change
   - Generates SQL: INSERT INTO student_subjects VALUES (?, ?);
6. Commit transaction

REFRESH OPERATION:

public static void refresh(Object entity) {
    em.refresh(entity);
}

WHY REFRESH?
- Reloads entity from database
- Updates entity with latest database state
- Useful for loading lazy-loaded collections

Example:
Student student = em.find(Student.class, 1);
// student.getSubjects() might not be loaded yet (lazy loading)
em.refresh(student);
// Now student.getSubjects() is fully loaded from database

--------------------------------------------------------------------------------
4.4 PMSApplication.java - GUI Application
--------------------------------------------------------------------------------

PURPOSE:
Swing-based graphical user interface for managing student-subject relationships.

ARCHITECTURE: Model-View-Controller (MVC)
- Model: Student, Subject entities
- View: Swing components (JTable, JButton, etc.)
- Controller: Event handlers and DatabaseManager calls

CLASS HIERARCHY:
public class PMSApplication extends JFrame

extends JFrame:
- Inherits from Swing's main window class
- Provides window functionality (title, size, close button)
- Can add components using layout managers

GUI COMPONENTS:

1. JTable - Data Display:
private JTable studentTable;
private JTable subjectTable;

JTable:
- Displays data in rows and columns
- Similar to Excel spreadsheet
- Uses TableModel for data management

2. TableModel - Data Management:
private DefaultTableModel studentTableModel;

DefaultTableModel:
- Stores table data (rows and columns)
- addRow(): Adds new row
- setRowCount(0): Clears all rows
- getValueAt(row, col): Gets cell value
- isCellEditable(): Controls if user can edit cells

3. JTextArea - Information Display:
private JTextArea infoArea;

JTextArea:
- Multi-line text display
- Used for showing messages and student's subjects

LAYOUT MANAGERS:

1. BorderLayout:
setLayout(new BorderLayout(10, 10));

Divides window into 5 regions:
- NORTH (top)
- SOUTH (bottom)
- EAST (right)
- WEST (left)
- CENTER (middle)

Each region can contain one component.

2. GridLayout:
new GridLayout(1, 2, 10, 10)

Parameters:
- rows: 1
- columns: 2
- hgap: 10px horizontal spacing
- vgap: 10px vertical spacing

Creates a grid where all cells are equal size.

3. FlowLayout:
new FlowLayout(FlowLayout.CENTER, 10, 10)

Arranges components in a row, wrapping to next line if needed.

INITIALIZATION:

public PMSApplication() {
    setTitle("Student-Subject Management System");
    setSize(900, 600);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setLayout(new BorderLayout(10, 10));
    ...
}

setTitle():
- Window title shown in title bar

setSize(width, height):
- Window dimensions in pixels

setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE):
- Exits application when window closes
- Other options: DO_NOTHING_ON_CLOSE, HIDE_ON_CLOSE, DISPOSE_ON_CLOSE

CREATING PANELS:

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

BREAKDOWN:
1. Create panel with BorderLayout
2. Add titled border ("Students")
3. Define table columns: ["ID", "Name"]
4. Create TableModel with 0 initial rows
5. Override isCellEditable to prevent editing
6. Create JTable with the model
7. Wrap table in JScrollPane for scrolling
8. Add scroll pane to panel
9. Return panel

EVENT HANDLING:

addSubjectBtn.addActionListener(e -> addSubjectToStudent());

Lambda Expression Explained:
- e: ActionEvent parameter (click event)
- ->: Lambda operator
- addSubjectToStudent(): Method to call

Equivalent anonymous class:
addSubjectBtn.addActionListener(new ActionListener() {
    @Override
    public void actionPerformed(ActionEvent e) {
        addSubjectToStudent();
    }
});

DATA LOADING:

private void loadData() {
    // Clear tables
    studentTableModel.setRowCount(0);
    
    // Load students from database
    List<Student> students = DatabaseManager.getAllStudents();
    for (Student student : students) {
        studentTableModel.addRow(new Object[]{
            student.getId(),
            student.getFirstName() + " " + student.getLastName()
        });
    }
    
    // Load subjects similarly...
}

FLOW:
1. Clear existing table data
2. Query database for all students
3. For each student:
   - Create array: [id, "firstName lastName"]
   - Add as new row to table
4. Table automatically updates display

USER INTERACTIONS:

1. ADD SUBJECT TO STUDENT:

private void addSubjectToStudent() {
    // Get selected rows
    int studentRow = studentTable.getSelectedRow();
    int subjectRow = subjectTable.getSelectedRow();
    
    // Validation
    if (studentRow == -1 || subjectRow == -1) {
        JOptionPane.showMessageDialog(this, 
            "Please select both a student and a subject!", 
            "Selection Required", 
            JOptionPane.WARNING_MESSAGE);
        return;
    }
    
    // Get IDs from table
    Long studentId = (Long) studentTableModel.getValueAt(studentRow, 0);
    Long subjectId = (Long) subjectTableModel.getValueAt(subjectRow, 0);
    
    // Update database
    DatabaseManager.addSubjectToStudent(studentId, subjectId);
    
    // Show confirmation
    String studentName = (String) studentTableModel.getValueAt(studentRow, 1);
    String subjectName = (String) subjectTableModel.getValueAt(subjectRow, 1);
    infoArea.setText("Added subject '" + subjectName + "' to student '" + studentName + "'");
}

STEP-BY-STEP:
1. Get selected row indices (-1 if nothing selected)
2. Validate selections (show error if none)
3. Extract IDs from first column (column 0)
4. Call database method to create relationship
5. Extract names from second column (column 1)
6. Display success message

2. VIEW STUDENT'S SUBJECTS:

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

WHY refresh()?
- Hibernate uses lazy loading for collections
- student.getSubjects() might not be loaded initially
- refresh() forces Hibernate to load subjects from database
- Without refresh(), getSubjects() might return empty set

StringBuilder:
- Efficient string concatenation
- Better than: String s = s + "text" (creates many String objects)
- append() modifies existing StringBuilder
- toString() converts to final String

MAIN METHOD:

public static void main(String[] args) {
    // Initialize database connection
    DatabaseManager.initialize();
    
    // Create sample data if database is empty
    initializeSampleData();
    
    // Launch GUI on Event Dispatch Thread
    SwingUtilities.invokeLater(() -> {
        PMSApplication app = new PMSApplication();
        app.setVisible(true);
    });
    
    // Cleanup on shutdown
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
        DatabaseManager.close();
    }));
}

SwingUtilities.invokeLater():
- Swing is NOT thread-safe
- All GUI operations must run on Event Dispatch Thread (EDT)
- invokeLater() schedules code to run on EDT
- Ensures proper GUI initialization

ShutdownHook:
- Runs when JVM shuts down
- Ensures database connections are closed
- Prevents connection leaks
- Good practice for resource cleanup

SAMPLE DATA INITIALIZATION:

private static void initializeSampleData() {
    List<Student> existingStudents = DatabaseManager.getAllStudents();
    
    if (existingStudents.isEmpty()) {
        // Create students
        Student s1 = new Student("Evgenev", "Ali");
        Student s2 = new Student("John", "Smith");
        // ... more students
        
        DatabaseManager.saveStudent(s1);
        DatabaseManager.saveStudent(s2);
        // ... save all
    }
    
    // Similar for subjects...
}

PURPOSE:
- Checks if database is empty
- Only creates data if no existing data
- Prevents duplicate data on restart
- Useful for development and testing

================================================================================
5. CONFIGURATION FILES
================================================================================

--------------------------------------------------------------------------------
5.1 pom.xml - Maven Configuration
--------------------------------------------------------------------------------

<?xml version="1.0" encoding="UTF-8"?>
- XML declaration
- Specifies XML version and character encoding

<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="...">
- Root element with XML namespaces
- Defines valid structure for Maven POM

<modelVersion>4.0.0</modelVersion>
- POM model version (always 4.0.0)

PROJECT COORDINATES:

<groupId>com.pms</groupId>
- Organization/group identifier
- Usually reverse domain name
- Like Java package naming

<artifactId>student-subject-pms</artifactId>
- Project name/identifier
- Unique within groupId

<version>1.0-SNAPSHOT</version>
- Project version
- SNAPSHOT: Development version (not released)
- Release versions don't have SNAPSHOT

PROPERTIES:

<properties>
    <maven.compiler.source>1.8</maven.compiler.source>
    <maven.compiler.target>1.8</maven.compiler.target>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
</properties>

compiler.source/target:
- Java version for compilation
- source: Language features available to use
- target: Bytecode version to generate
- 1.8 = Java 8

DEPENDENCIES:

<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <version>42.7.3</version>
</dependency>

PostgreSQL JDBC Driver:
- Allows Java to connect to PostgreSQL
- Implements JDBC standard
- Translates Java database calls to PostgreSQL protocol

<dependency>
    <groupId>org.hibernate</groupId>
    <artifactId>hibernate-core</artifactId>
    <version>5.6.15.Final</version>
</dependency>

Hibernate ORM:
- Object-Relational Mapping framework
- Converts Java objects to database tables
- Generates SQL automatically
- Manages relationships and transactions

<dependency>
    <groupId>javax.persistence</groupId>
    <artifactId>javax.persistence-api</artifactId>
    <version>2.2</version>
</dependency>

JPA API:
- Standard specification for ORM in Java
- Provides annotations: @Entity, @Table, @Id, etc.
- Hibernate is an implementation of JPA
- Using JPA makes code portable (can switch to EclipseLink, etc.)

<dependency>
    <groupId>javax.xml.bind</groupId>
    <artifactId>jaxb-api</artifactId>
    <version>2.3.1</version>
</dependency>

JAXB:
- Java Architecture for XML Binding
- Required by Hibernate for Java 9+
- Java 8 includes it by default
- Java 9+ removed it from core, needs explicit dependency

--------------------------------------------------------------------------------
5.2 persistence.xml - JPA Configuration
--------------------------------------------------------------------------------

LOCATION: src/main/resources/META-INF/persistence.xml
- MUST be in META-INF folder
- Loaded automatically by JPA
- Contains database connection and Hibernate settings

<persistence xmlns="..." version="2.1">
- Root element for JPA configuration
- version="2.1": JPA specification version

<persistence-unit name="PMSPersistenceUnit" transaction-type="RESOURCE_LOCAL">
- Persistence unit: Named configuration set
- name: Used in code to reference this configuration
- transaction-type:
  * RESOURCE_LOCAL: Application manages transactions
  * JTA: Container manages transactions (used in Java EE servers)

<provider>org.hibernate.jpa.HibernatePersistenceProvider</provider>
- Specifies JPA implementation
- Hibernate is the provider
- Other options: EclipseLink, OpenJPA

<class>Student</class>
<class>Subject</class>
- Lists entity classes to manage
- Can auto-discover without listing (if enabled)
- Explicit listing is more reliable

JDBC CONNECTION PROPERTIES:

<property name="javax.persistence.jdbc.driver" value="org.postgresql.Driver"/>
- JDBC driver class name
- PostgreSQL driver implementation

<property name="javax.persistence.jdbc.url" 
          value="jdbc:postgresql://localhost:5432/pms_db"/>
- Database connection URL
- Format: jdbc:postgresql://host:port/database
- localhost: Database on same machine
- 5432: Default PostgreSQL port (might be 5433 on some systems)
- pms_db: Database name

<property name="javax.persistence.jdbc.user" value="postgres"/>
<property name="javax.persistence.jdbc.password" value="your_password"/>
- Database credentials
- IMPORTANT: Change password to match your PostgreSQL setup

HIBERNATE PROPERTIES:

<property name="hibernate.dialect" 
          value="org.hibernate.dialect.PostgreSQLDialect"/>
- Database-specific SQL dialect
- Hibernate generates SQL optimized for PostgreSQL
- Other dialects: MySQL8Dialect, Oracle12cDialect, etc.

<property name="hibernate.hbm2ddl.auto" value="update"/>
- Schema generation strategy:
  * create: Drop and create tables every time (DESTROYS DATA!)
  * create-drop: Create on startup, drop on shutdown
  * update: Update schema without losing data (RECOMMENDED for development)
  * validate: Only check if schema matches (RECOMMENDED for production)
  * none: Do nothing

<property name="hibernate.show_sql" value="true"/>
- Print SQL statements to console
- Useful for debugging
- Set to false in production

<property name="hibernate.format_sql" value="true"/>
- Format SQL with line breaks and indentation
- Makes SQL readable in logs

================================================================================
6. SETUP INSTRUCTIONS
================================================================================

PREREQUISITES:
1. Java JDK 8 or higher
2. PostgreSQL 12 or higher
3. Maven 3.6 or higher
4. IntelliJ IDEA (Community or Ultimate)

STEP 1: INSTALL POSTGRESQL

Windows:
- Download from postgresql.org
- Run installer
- Remember the password you set

Linux (Debian/Ubuntu):
sudo apt update
sudo apt install postgresql postgresql-contrib

STEP 2: CREATE DATABASE

Open terminal/psql:

Windows:
- Open SQL Shell (psql)

Linux:
sudo -u postgres psql

In psql console:
CREATE DATABASE pms_db;
\c pms_db

Run the setup script (copy all CREATE and INSERT statements from setup_database.sql)

Set password:
ALTER USER postgres PASSWORD 'your_password';

Exit:
\q

STEP 3: CLONE/DOWNLOAD PROJECT

git clone <repository-url>
cd student-subject-pms

STEP 4: OPEN IN INTELLIJ

1. File → Open
2. Select project folder (containing pom.xml)
3. Click OK
4. Wait for Maven to download dependencies

STEP 5: CONFIGURE DATABASE PASSWORD

1. Navigate to: src/main/resources/META-INF/persistence.xml
2. Find line:
   <property name="javax.persistence.jdbc.password" value="your_password"/>
3. Replace "your_password" with your actual PostgreSQL password
4. Check port (5432 or 5433) and update if needed

STEP 6: PROJECT STRUCTURE

Ensure proper folder structure:
src/
├── main/
│   ├── java/
│   │   ├── Student.java
│   │   ├── Subject.java
│   │   ├── DatabaseManager.java
│   │   └── PMSApplication.java
│   └── resources/
│       └── META-INF/
│           └── persistence.xml

Mark folders in IntelliJ:
- Right-click src/main/java → Mark Directory as → Sources Root
- Right-click src/main/resources → Mark Directory as → Resources Root

STEP 7: BUILD PROJECT

Option 1 - IntelliJ:
- Right-click pom.xml → Maven → Reload Project
- Build → Rebuild Project

Option 2 - Terminal:
mvn clean install

STEP 8: RUN APPLICATION

1. Open PMSApplication.java
2. Right-click on the file or main method
3. Click "Run 'PMSApplication.main()'"

If successful, GUI window will open with students and subjects tables.

================================================================================
7. HOW EVERYTHING WORKS TOGETHER
================================================================================

COMPLETE FLOW - FROM STARTUP TO DATABASE UPDATE:

PHASE 1: APPLICATION STARTUP
----------------------------

1. main() method executes
   └─> DatabaseManager.initialize()
       └─> Persistence.createEntityManagerFactory("PMSPersistenceUnit")
           ├─> Searches for META-INF/persistence.xml
           ├─> Reads database connection properties
           ├─> Loads PostgreSQL JDBC driver
           ├─> Connects to jdbc:postgresql://localhost:5432/pms_db
           ├─> Validates Student and Subject entity classes
           └─> Checks database schema
               ├─> If tables don't exist: Creates them
               └─> If tables exist: Updates if needed (based on hbm2ddl.auto)

2. initializeSampleData()
   └─> DatabaseManager.getAllStudents()
       └─> If empty: Creates 5 students and 5 subjects
           └─> Generates SQL INSERTs
               └─> Commits to database

3. SwingUtilities.invokeLater()
   └─> Creates PMSApplication on Event Dispatch Thread
       ├─> Initializes GUI components
       ├─> Creates tables with empty models
       └─> loadData()
           ├─> DatabaseManager.getAllStudents()
           │   └─> Hibernate executes: SELECT * FROM students
           ├─> DatabaseManager.getAllSubjects()
           │   └─> Hibernate executes: SELECT * FROM subjects
           └─> Populates JTables with data

4. app.setVisible(true)
   └─> Window appears on screen

PHASE 2: USER INTERACTION - ADDING SUBJECT TO STUDENT
------------------------------------------------------

1. User selects student row (e.g., "Evgenev Ali")
   └─> studentTable.getSelectedRow() returns row index

2. User selects subject row (e.g., "Math")
   └─> subjectTable.getSelectedRow() returns row index

3. User clicks "Add Subject to Student" button
   └─> ActionListener fires
       └─> addSubjectToStudent() method executes

4. Get IDs from selected rows:
   studentId = studentTableModel.getValueAt(row, 0)  // Column 0 = ID
   subjectId = subjectTableModel.getValueAt(row, 0)

5. Call database layer:
   DatabaseManager.addSubjectToStudent(studentId, subjectId)
   
6. Inside DatabaseManager:
   ├─> tx.begin()  // Start transaction
   ├─> student = em.find(Student.class, studentId)
   │   └─> Hibernate: SELECT * FROM students WHERE id = ?
   ├─> subject = em.find(Subject.class, subjectId)
   │   └─> Hibernate: SELECT * FROM subjects WHERE id = ?
   ├─> student.addSubject(subject)
   │   ├─> this.subjects.add(subject)  // Add to student's Set
   │   └─> subject.getStudents().add(this)  // Add to subject's Set (bidirectional)
   ├─> em.merge(student)
   │   └─> Hibernate detects relationship change
   │       └─> Generates SQL:
   │           INSERT INTO student_subjects (student_id, subject_id) 
   │           VALUES (1, 1);
   └─> tx.commit()  // Commit transaction
       └─> Changes are permanently saved to database

7. Display confirmation message in infoArea

PHASE 3: VIEWING STUDENT'S SUBJECTS
------------------------------------

1. User selects student row
2. User clicks "View Student's Subjects"
3. viewStudentSubjects() executes:
   ├─> Get studentId from table
   ├─> student = DatabaseManager.getStudentById(studentId)
   │   └─> Hibernate: SELECT * FROM students WHERE id = ?
   ├─> DatabaseManager.refresh(student)
   │   └─> Forces loading of subjects collection
   │       └─> Hibernate executes JOIN:
   │           SELECT subjects.* 
   │           FROM subjects
   │           JOIN student_subjects ON subjects.id = subject_id
   │           WHERE student_id = ?
   ├─> Iterate through student.getSubjects()
   └─> Build and display text in infoArea

DATABASE STATE CHANGES:

Initial State (after setup):
students:
+----+------------+-----------+
| id | first_name | last_name |
+----+------------+-----------+
| 1  | Evgenev    | Ali       |
| 2  | John       | Smith     |
+----+------------+-----------+

subjects:
+----+----------+------------------+
| id | name     | description      |
+----+----------+------------------+
| 1  | Math     | Mathematics...   |
| 2  | Physics  | Classical...     |
+----+----------+------------------+

student_subjects (junction table):
Empty initially (if no sample enrollments)

After adding Math to Evgenev:
student_subjects:
+------------+------------+
| student_id | subject_id |
+------------+------------+
| 1          | 1          |
+------------+------------+

After adding Physics to Evgenev:
+------------+------------+
| student_id | subject_id |
+------------+------------+
| 1          | 1          |
| 1          | 2          |
+------------+------------+

After adding Math to John:
+------------+------------+
| student_id | subject_id |
+------------+------------+
| 1          | 1          |
| 1          | 2          |
| 2          | 1          |
+------------+------------+

This demonstrates MANY-TO-MANY:
- Student 1 (Evgenev) has 2 subjects
- Student 2 (John) has 1 subject
- Subject 1 (Math) has 2 students
- Subject 2 (Physics) has 1 student

HIBERNATE SQL GENERATION EXAMPLE:

When you call: DatabaseManager.addSubjectToStudent(1, 1)

Hibernate generates and executes:
1. SELECT student0_.id as id1_1_0_, 
          student0_.first_name as first_na2_1_0_, 
          student0_.last_name as last_nam3_1_0_ 
   FROM students student0_ 
   WHERE student0_.id=1

2. SELECT subject0_.id as id1_2_0_, 
          subject0_.description as descript2_2_0_, 
          subject0_.name as name3_2_0_ 
   FROM subjects subject0_ 
   WHERE subject0_.id=1

3. INSERT INTO student_subjects (student_id, subject_id) 
   VALUES (1, 1)

All within a single transaction!

================================================================================
8. TROUBLESHOOTING GUIDE
================================================================================

PROBLEM: "cannot find symbol: class Entity"
CAUSE: Maven dependencies not loaded
SOLUTION:
1. Right-click pom.xml → Maven → Reload Project
2. Wait for dependencies to download
3. If still failing: File → Invalidate Caches and Restart

PROBLEM: "Could not find any META-INF/persistence.xml"
CAUSE: File not in correct location or not marked as resource
SOLUTION:
1. Ensure file is at: src/main/resources/META-INF/persistence.xml
2. Right-click src/main/resources → Mark Directory as → Resources Root
3. Build → Rebuild Project

PROBLEM: "password authentication failed for user postgres"
CAUSE: Wrong password in persistence.xml
SOLUTION:
1. Find your PostgreSQL password:
   - Windows: Check pgAdmin saved connections
   - Linux: sudo -u postgres psql (then set new password)
2. Update persistence.xml with correct password
3. Verify database exists: psql -U postgres -l

PROBLEM: "database pms_db does not exist"
CAUSE: Database not created
SOLUTION:
sudo -u postgres psql
CREATE DATABASE pms_db;
\q

PROBLEM: "Connection refused" or "Could not connect to server"
CAUSE: PostgreSQL not running or wrong port
SOLUTION:
1. Check if PostgreSQL is running:
   - Windows: Services → postgresql-x64-XX → Start
   - Linux: sudo systemctl status postgresql
2. Check port (usually 5432 or 5433):
   - Linux: sudo -u postgres psql -c "SHOW port;"
3. Update port in persistence.xml if different

PROBLEM: GUI shows empty tables
CAUSE: Data not created or connection issue
SOLUTION:
1. Check console for Hibernate logs
2. Verify database connection settings
3. Check if tables exist:
   psql -U postgres -d pms_db
   \dt
4. If tables empty, run initializeSampleData() or SQL script

PROBLEM: Changes not saved to database
CAUSE: Transaction not committed or connection issue
SOLUTION:
1. Check for exceptions in console
2. Verify tx.commit() is called
3. Check if database user has write permissions:
   psql -U postgres
   GRANT ALL PRIVILEGES ON DATABASE pms_db TO postgres;

PROBLEM: "Table already exists" error
CAUSE: hbm2ddl.auto set to "create" with existing tables
SOLUTION:
1. Change to "update" in persistence.xml
2. Or drop existing tables:
   psql -U postgres -d pms_db
   DROP TABLE student_subjects CASCADE;
   DROP TABLE students CASCADE;
   DROP TABLE subjects CASCADE;

PROBLEM: Port conflict (5432 or 5433)
CAUSE: Multiple PostgreSQL installations or non-standard configuration
SOLUTION:
1. Find actual port:
   sudo -u postgres psql -c "SHOW port;"
2. Update persistence.xml:
   <property name="javax.persistence.jdbc.url" 
             value="jdbc:postgresql://localhost:ACTUAL_PORT/pms_db"/>

PROBLEM: Can't create META-INF folder in Windows
CAUSE: Windows restrictions on certain folder names
SOLUTION:
1. Use terminal: mkdir src\main\resources\META-INF
2. Or create as "meta-inf" then rename to "META-INF"
3. Or use Linux/WSL

================================================================================
END OF DOCUMENTATION
================================================================================

For additional help or questions:
1. Check Hibernate documentation: https://hibernate.org/orm/documentation
2. Check JPA specification: https://jcp.org/en/jsr/detail?id=338
3. Check PostgreSQL documentation: https://www.postgresql.org/docs/
4. Review IntelliJ IDEA help: https://www.jetbrains.com/help/idea/

This project demonstrates:
- Entity-Relationship modeling
- ORM with Hibernate
- Many-to-Many relationships
- Transaction management
- MVC architecture
- Swing GUI development
- Maven project structure
- Database design and normalization

Good luck with your project!
import javax.persistence.*;
import java.util.List;

public class DatabaseManager {

    private static EntityManagerFactory emf;
    private static EntityManager em;

    // Initialize the database connection
    public static void initialize() {
        emf = Persistence.createEntityManagerFactory("PMSPersistenceUnit");
        em = emf.createEntityManager();
    }

    // Close the database connection
    public static void close() {
        if (em != null) em.close();
        if (emf != null) emf.close();
    }

    // Save or update a student
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

    // Save or update a subject
    public static void saveSubject(Subject subject) {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(subject);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            e.printStackTrace();
        }
    }

    // Add a subject to a student
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

    // Remove a subject from a student
    public static void removeSubjectFromStudent(Long studentId, Long subjectId) {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Student student = em.find(Student.class, studentId);
            Subject subject = em.find(Subject.class, subjectId);

            if (student != null && subject != null) {
                student.removeSubject(subject);
                em.merge(student);
            }
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            e.printStackTrace();
        }
    }

    // Get all students
    public static List<Student> getAllStudents() {
        TypedQuery<Student> query = em.createQuery("SELECT s FROM Student s", Student.class);
        return query.getResultList();
    }

    // Get all subjects
    public static List<Subject> getAllSubjects() {
        TypedQuery<Subject> query = em.createQuery("SELECT s FROM Subject s", Subject.class);
        return query.getResultList();
    }

    // Get student by ID
    public static Student getStudentById(Long id) {
        return em.find(Student.class, id);
    }

    // Get subject by ID
    public static Subject getSubjectById(Long id) {
        return em.find(Subject.class, id);
    }

    // Refresh entity to get updated data
    public static void refresh(Object entity) {
        em.refresh(entity);
    }
}
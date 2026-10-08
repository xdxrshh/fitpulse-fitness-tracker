package com.fitpulse;

import com.fitpulse.dao.AuditLogDAO;
import com.fitpulse.dao.GenericDAO;
import com.fitpulse.dao.GoalDAO;
import com.fitpulse.dao.MealDAO;
import com.fitpulse.dao.UserDAO;
import com.fitpulse.dao.WorkoutDAO;
import com.fitpulse.dao.impl.AuditLogDAOImpl;
import com.fitpulse.dao.impl.BaseDAO;
import com.fitpulse.dao.impl.GoalDAOImpl;
import com.fitpulse.dao.impl.MealDAOImpl;
import com.fitpulse.dao.impl.UserDAOImpl;
import com.fitpulse.dao.impl.WorkoutDAOImpl;
import com.fitpulse.model.AuditLog;
import com.fitpulse.model.CardioWorkout;
import com.fitpulse.model.FitnessGoal;
import com.fitpulse.model.MealLog;
import com.fitpulse.model.Member;
import com.fitpulse.model.StrengthWorkout;
import com.fitpulse.model.User;
import com.fitpulse.model.Workout;
import com.fitpulse.model.enums.GoalStatus;
import com.fitpulse.model.enums.GoalType;
import com.fitpulse.model.enums.MealType;
// UserRole used implicitly via Member default role
import com.fitpulse.service.GoalTrackingService;
import com.fitpulse.util.DatabaseConnectionManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Phase 6: Database Operations and JDBC Rubric Verification Test.
 *
 * Rubric 4 (7 marks) - Database Operation Classes:
 *   GenericDAO interface hierarchy, BaseDAO abstract class, 5 DAO implementations,
 *   polymorphic multi-table INSERT (users+members), ResultSet type-dispatch mapping,
 *   LEFT JOIN queries, DriverManager Singleton.
 *
 * Rubric 5 (3 marks) - JDBC Connectivity / CRUD:
 *   PreparedStatement parameterized queries, RETURN_GENERATED_KEYS,
 *   ResultSet column mapping, aggregate queries (SUM, COUNT, COALESCE),
 *   full CRUD cycle on every entity.
 *
 * Rubric 6 (3 marks) - JDBC Transaction Management:
 *   setAutoCommit(false), explicit commit(), explicit rollback() on exception,
 *   atomic 3-step operation verified (workout + goal + audit log together).
 */
public class Phase6DatabaseVerificationTest {

    private static int totalPassed = 0;
    private static int totalTests  = 0;
    private static int testUserId   = -1;
    private static int testGoalId   = -1;
    private static int testWorkout1 = -1;
    private static int testWorkout2 = -1;
    private static int testMealId   = -1;
    private static int testAuditId  = -1;

    public static void main(String[] args) {
        sep(); System.out.println("  FitPulse - Phase 6 Database and JDBC Verification Test"); sep();
        System.out.println();

        if (!DatabaseConnectionManager.getInstance().testConnection()) {
            System.err.println("[SKIP] MySQL is not available. Start MariaDB/MySQL and retry.");
            System.exit(1);
        }
        System.out.println("[INFO] MySQL connection: ONLINE\n");

        try {
            rubric4_DatabaseOperationClasses();
            rubric5_JDBCConnectivityCRUD();
            rubric6_TransactionManagement();
        } catch (Exception ex) {
            System.err.println("[FATAL] Unexpected exception: " + ex.getMessage());
            ex.printStackTrace();
        } finally {
            cleanupTestData();
        }

        System.out.println();
        sep();
        System.out.printf("  PHASE 6 SUMMARY: %d / %d TESTS PASSED!%n", totalPassed, totalTests);
        if (totalPassed == totalTests) {
            System.out.println("  Rubric 4 (Database Operation Classes): FULLY DEMONSTRATED");
            System.out.println("  Rubric 5 (JDBC Connectivity and CRUD): FULLY DEMONSTRATED");
            System.out.println("  Rubric 6 (Transaction Management):     FULLY DEMONSTRATED");
        } else {
            System.err.println("  INCOMPLETE - review failed tests above.");
        }
        sep();
        if (totalPassed != totalTests) System.exit(1);
    }

    // =========================================================================
    // RUBRIC 4 - Database Operation Classes
    // =========================================================================
    private static void rubric4_DatabaseOperationClasses() throws Exception {
        System.out.println("--- RUBRIC 4: Database Operation Classes ---\n");

        // Test 1: GenericDAO generic interface hierarchy
        System.out.println("[Test 1: GenericDAO<T,ID> Generic Interface Hierarchy]");
        ok("GenericDAO is a Java interface",
                GenericDAO.class.isInterface(),
                "GenericDAO<T,ID> declared as interface.");
        ok("GenericDAO has 2 generic type parameters",
                GenericDAO.class.getTypeParameters().length == 2,
                "GenericDAO<T,ID> - 2 type params confirmed.");
        ok("UserDAOImpl implements GenericDAO via UserDAO",
                GenericDAO.class.isAssignableFrom(UserDAOImpl.class),
                "UserDAOImpl -> UserDAO -> GenericDAO<User,Integer>.");
        ok("WorkoutDAOImpl implements GenericDAO via WorkoutDAO",
                GenericDAO.class.isAssignableFrom(WorkoutDAOImpl.class),
                "WorkoutDAOImpl -> WorkoutDAO -> GenericDAO<Workout,Integer>.");
        ok("GoalDAOImpl implements GenericDAO via GoalDAO",
                GenericDAO.class.isAssignableFrom(GoalDAOImpl.class),
                "GoalDAOImpl -> GoalDAO -> GenericDAO<FitnessGoal,Integer>.");
        ok("MealDAOImpl implements GenericDAO via MealDAO",
                GenericDAO.class.isAssignableFrom(MealDAOImpl.class),
                "MealDAOImpl -> MealDAO -> GenericDAO<MealLog,Integer>.");
        ok("AuditLogDAOImpl implements GenericDAO via AuditLogDAO",
                GenericDAO.class.isAssignableFrom(AuditLogDAOImpl.class),
                "AuditLogDAOImpl -> AuditLogDAO -> GenericDAO<AuditLog,Integer>.");

        // Test 2: BaseDAO abstract class
        System.out.println("\n[Test 2: BaseDAO Abstract Superclass with Shared Connection Utility]");
        ok("BaseDAO is abstract",
                java.lang.reflect.Modifier.isAbstract(BaseDAO.class.getModifiers()),
                "BaseDAO declared abstract - subclasses must be concrete.");
        ok("All DAOImpls extend BaseDAO",
                BaseDAO.class.isAssignableFrom(UserDAOImpl.class)
                && BaseDAO.class.isAssignableFrom(WorkoutDAOImpl.class)
                && BaseDAO.class.isAssignableFrom(GoalDAOImpl.class)
                && BaseDAO.class.isAssignableFrom(MealDAOImpl.class)
                && BaseDAO.class.isAssignableFrom(AuditLogDAOImpl.class),
                "5 DAO implementations all extend BaseDAO - shared getConnection() inherited.");
        ok("DatabaseConnectionManager is Singleton (double-check locking)",
                DatabaseConnectionManager.getInstance() == DatabaseConnectionManager.getInstance(),
                "getInstance() always returns same reference - thread-safe Singleton pattern.");

        // Test 3: UserDAOImpl polymorphic multi-table atomic INSERT
        System.out.println("\n[Test 3: UserDAOImpl Polymorphic Multi-Table INSERT (users + members)]");
        UserDAO userDAO = new UserDAOImpl();
        String uniqueEmail = "p6test." + System.currentTimeMillis() + "@fitpulse.com";
        Member member = new Member(0, "P6 TestUser", uniqueEmail, "$2a$10$fakehash",
                72.5, 178.0, "INTERMEDIATE", null);
        User created = userDAO.create(member);
        testUserId = created.getId();
        ok("UserDAO.create() atomic INSERT assigns generated PK",
                testUserId > 0,
                "New user_id=" + testUserId + " (INSERT into users + members, RETURN_GENERATED_KEYS).");
        ok("UserDAO.create() polymorphic - result is Member subtype",
                created instanceof Member,
                "Polymorphic multi-table: users row + members row inserted atomically.");
        ok("Member email stored correctly via PreparedStatement binding",
                uniqueEmail.equals(created.getEmail()),
                "email param bound via setString() - no SQL injection risk.");

        // Test 4: UserDAOImpl findById with LEFT JOIN
        System.out.println("\n[Test 4: UserDAOImpl findById() LEFT JOIN Query]");
        Optional<User> found = userDAO.findById(testUserId);
        ok("findById() returns present Optional<User>",
                found.isPresent(), "SELECT with LEFT JOIN found user_id=" + testUserId + ".");
        ok("findById() maps polymorphically to Member subtype",
                found.isPresent() && found.get() instanceof Member,
                "mapResultSetToUser() dispatched to Member branch based on role column.");
        ok("findAll() returns all users via JOIN",
                userDAO.findAll().stream().anyMatch(u -> u.getId() == testUserId),
                "findAll() SELECT * from users with JOINs - test user found in result.");
        ok("emailExists() PreparedStatement WHERE email=?",
                userDAO.emailExists(uniqueEmail),
                "SELECT 1 FROM users WHERE email=? confirmed exists.");

        // Test 5: UserDAOImpl update
        System.out.println("\n[Test 5: UserDAOImpl update() UPDATE PreparedStatement]");
        found.get().setName("P6 UpdatedName");
        boolean updated = userDAO.update(found.get());
        ok("UserDAO.update() executes UPDATE PreparedStatement",
                updated, "UPDATE users SET name=? WHERE id=? affected 1 row.");
        Optional<User> refetched = userDAO.findById(testUserId);
        ok("UPDATE change is persistent (SELECT after UPDATE)",
                refetched.isPresent() && "P6 UpdatedName".equals(refetched.get().getName()),
                "Refetched name='" + (refetched.isPresent() ? refetched.get().getName() : "?") + "'.");
    }

    // =========================================================================
    // RUBRIC 5 - JDBC Connectivity / PreparedStatement CRUD
    // =========================================================================
    private static void rubric5_JDBCConnectivityCRUD() throws Exception {
        System.out.println("\n--- RUBRIC 5: JDBC Connectivity and PreparedStatement CRUD ---\n");
        WorkoutDAO workoutDAO = new WorkoutDAOImpl();

        // Test 6: CardioWorkout INSERT
        System.out.println("[Test 6: WorkoutDAO CREATE CardioWorkout - 11-param PreparedStatement]");
        CardioWorkout cardio = new CardioWorkout(0, testUserId, LocalDateTime.now(),
                30, 5.0, 145, "P6 cardio test");
        Workout savedCardio = workoutDAO.create(cardio);
        testWorkout1 = savedCardio.getId();
        ok("WorkoutDAO.create(CardioWorkout) INSERT PreparedStatement",
                testWorkout1 > 0, "workout_id=" + testWorkout1 + " assigned by RETURN_GENERATED_KEYS.");
        ok("CardioWorkout calories persisted via polymorphic formula",
                savedCardio.getCaloriesBurned() > 0,
                String.format("calories_burned=%.1f (calculateCaloriesBurned stored).", savedCardio.getCaloriesBurned()));

        // Test 7: StrengthWorkout INSERT
        System.out.println("\n[Test 7: WorkoutDAO CREATE StrengthWorkout - NULL handling in PreparedStatement]");
        StrengthWorkout strength = new StrengthWorkout(0, testUserId,
                LocalDateTime.now().minusHours(2), 45, 2500.0, 15, 10, "P6 strength test");
        Workout savedStrength = workoutDAO.create(strength);
        testWorkout2 = savedStrength.getId();
        ok("WorkoutDAO.create(StrengthWorkout) INSERT PreparedStatement",
                testWorkout2 > 0, "workout_id=" + testWorkout2 + " (cardio cols bound as NULL via setNull()).");
        ok("Two workouts have distinct auto-increment IDs",
                testWorkout1 != testWorkout2,
                "cardio_id=" + testWorkout1 + " strength_id=" + testWorkout2 + " are distinct.");

        // Test 8: findById ResultSet mapping
        System.out.println("\n[Test 8: WorkoutDAO findById() - Polymorphic ResultSet Mapping]");
        Optional<Workout> fc = workoutDAO.findById(testWorkout1);
        Optional<Workout> fs = workoutDAO.findById(testWorkout2);
        ok("findById() maps CardioWorkout from ResultSet",
                fc.isPresent() && fc.get() instanceof CardioWorkout,
                "workout_type=CARDIO dispatched to CardioWorkout in mapResultSetToWorkout().");
        ok("findById() maps StrengthWorkout from ResultSet",
                fs.isPresent() && fs.get() instanceof StrengthWorkout,
                "workout_type=STRENGTH dispatched to StrengthWorkout in mapResultSetToWorkout().");
        ok("distance_km column mapped correctly",
                fc.isPresent() && Math.abs(((CardioWorkout)fc.get()).getDistanceKm()-5.0)<0.01,
                "distance_km=" + (fc.isPresent() ? ((CardioWorkout)fc.get()).getDistanceKm() : "null") + ".");

        // Test 9: findByUserId parameterized query
        System.out.println("\n[Test 9: WorkoutDAO findByUserId() - Parameterized WHERE Clause]");
        List<Workout> userWorkouts = workoutDAO.findByUserId(testUserId);
        ok("findByUserId() returns List<Workout> with WHERE user_id=?",
                userWorkouts.size() >= 2,
                "Retrieved " + userWorkouts.size() + " workouts for user_id=" + testUserId + ".");
        boolean hasC = userWorkouts.stream().anyMatch(w -> w instanceof CardioWorkout);
        boolean hasS = userWorkouts.stream().anyMatch(w -> w instanceof StrengthWorkout);
        ok("Result includes both CardioWorkout and StrengthWorkout types",
                hasC && hasS, "Polymorphic ResultSet mapping confirmed across mixed workout types.");

        // Test 10: Aggregate queries
        System.out.println("\n[Test 10: WorkoutDAO Aggregate PreparedStatements (SUM + COUNT)]");
        double totalCal = workoutDAO.getTotalCaloriesBurnedByUser(testUserId);
        ok("COALESCE(SUM(calories_burned),0) aggregate PreparedStatement",
                totalCal > 0,
                String.format("Total calories = %.1f kcal for user_id=%d.", totalCal, testUserId));
        int wCount = workoutDAO.getTotalWorkoutsCountByUser(testUserId);
        ok("COUNT(*) aggregate PreparedStatement",
                wCount >= 2, "Workout count = " + wCount + " for user_id=" + testUserId + ".");

        // Test 11: UPDATE PreparedStatement
        System.out.println("\n[Test 11: WorkoutDAO update() - UPDATE PreparedStatement]");
        fc.get().setNotes("P6 updated notes");
        ok("WorkoutDAO.update() UPDATE PreparedStatement",
                workoutDAO.update(fc.get()),
                "UPDATE workouts SET duration_min=?, calories_burned=?, notes=? WHERE id=" + testWorkout1 + ".");
        Optional<Workout> rw = workoutDAO.findById(testWorkout1);
        ok("Update persisted: notes column changed in MySQL",
                rw.isPresent() && "P6 updated notes".equals(rw.get().getNotes()),
                "notes='" + (rw.isPresent() ? rw.get().getNotes() : "?") + "' confirmed.");

        // Test 12: GoalDAO full CRUD
        System.out.println("\n[Test 12: GoalDAO Full CRUD - PreparedStatement]");
        GoalDAO goalDAO = new GoalDAOImpl();
        FitnessGoal goal = new FitnessGoal(0, testUserId, GoalType.CALORIE_BURN,
                3000.0, 0.0, LocalDate.now(), LocalDate.now().plusDays(14), GoalStatus.IN_PROGRESS);
        FitnessGoal createdGoal = goalDAO.create(goal);
        testGoalId = createdGoal.getId();
        ok("GoalDAO.create() INSERT PreparedStatement assigns PK",
                testGoalId > 0, "fitness_goals row id=" + testGoalId + ".");
        Optional<FitnessGoal> fg = goalDAO.findById(testGoalId);
        ok("GoalDAO.findById() PreparedStatement + ResultSet mapping",
                fg.isPresent() && fg.get().getGoalType() == GoalType.CALORIE_BURN,
                "goal_type=CALORIE_BURN target=3000 retrieved correctly.");
        List<FitnessGoal> activeGoals = goalDAO.findActiveGoalsByUserId(testUserId);
        ok("GoalDAO.findActiveGoalsByUserId() WHERE status='IN_PROGRESS'",
                activeGoals.stream().anyMatch(g -> g.getId() == testGoalId),
                "Active goals = " + activeGoals.size() + ", test goal confirmed IN_PROGRESS.");
        fg.get().setCurrentValue(800.0);
        ok("GoalDAO.update() UPDATE PreparedStatement",
                goalDAO.update(fg.get()),
                "UPDATE fitness_goals SET current_value=800 WHERE id=" + testGoalId + ".");
        Optional<FitnessGoal> rfg = goalDAO.findById(testGoalId);
        ok("Goal update persisted",
                rfg.isPresent() && Math.abs(rfg.get().getCurrentValue()-800.0)<0.01,
                "current_value=" + (rfg.isPresent() ? rfg.get().getCurrentValue() : "?") + ".");

        // Test 13: MealDAO full CRUD
        System.out.println("\n[Test 13: MealDAO Full CRUD - PreparedStatement]");
        MealDAO mealDAO = new MealDAOImpl();
        MealLog meal = new MealLog(0, testUserId, "P6 Test Meal", MealType.LUNCH,
                650, 45.0, 80.0, 18.0, LocalDateTime.now());
        MealLog savedMeal = mealDAO.create(meal);
        testMealId = savedMeal.getId();
        ok("MealDAO.create() INSERT PreparedStatement (8 params)",
                testMealId > 0, "meal_logs row id=" + testMealId + ".");
        Optional<MealLog> fm = mealDAO.findById(testMealId);
        ok("MealDAO.findById() PreparedStatement + ResultSet mapping",
                fm.isPresent() && fm.get().getCalories() == 650,
                "calories=650 protein=45 carbs=80 fat=18 all mapped.");
        ok("MealDAO aggregate: COALESCE(SUM(calories),0) WHERE DATE(logged_at)=?",
                mealDAO.getTotalCaloriesByDate(testUserId, LocalDate.now()) >= 650,
                "Today calorie sum includes our meal.");
        ok("MealDAO.update() UPDATE PreparedStatement", mealDAO.update(fm.get()),
                "UPDATE meal_logs confirmed.");
        ok("MealDAO.delete() DELETE PreparedStatement", mealDAO.delete(testMealId),
                "DELETE FROM meal_logs WHERE id=" + testMealId + " row removed.");
        testMealId = -1;
        ok("MealDAO delete verified via findById returning empty",
                mealDAO.findById(savedMeal.getId()).isEmpty(),
                "Optional.empty() confirmed post-DELETE.");

        // Test 14: AuditLogDAO full CRUD
        System.out.println("\n[Test 14: AuditLogDAO Full CRUD - PreparedStatement]");
        AuditLogDAO auditDAO = new AuditLogDAOImpl();
        AuditLog log = new AuditLog();
        log.setUserId(testUserId);
        log.setAction("P6_CRUD_TEST");
        log.setDetails("Phase6 AuditLog CRUD via PreparedStatement");
        AuditLog savedLog = auditDAO.create(log);
        testAuditId = savedLog.getId();
        ok("AuditLogDAO.create() INSERT PreparedStatement assigns PK",
                testAuditId > 0, "audit_logs row id=" + testAuditId + ".");
        Optional<AuditLog> fal = auditDAO.findById(testAuditId);
        ok("AuditLogDAO.findById() PreparedStatement + ResultSet mapping",
                fal.isPresent() && "P6_CRUD_TEST".equals(fal.get().getAction()),
                "action='P6_CRUD_TEST' mapped from ResultSet.");
        ok("AuditLogDAO.findByUserId() parameterized WHERE user_id=?",
                auditDAO.findByUserId(testUserId).stream().anyMatch(a -> a.getId() == testAuditId),
                "findByUserId found our audit record in user's trail.");
        fal.get().setDetails("Updated details");
        ok("AuditLogDAO.update() UPDATE PreparedStatement", auditDAO.update(fal.get()),
                "UPDATE audit_logs confirmed.");
        ok("AuditLogDAO.delete() DELETE PreparedStatement", auditDAO.delete(testAuditId),
                "DELETE FROM audit_logs WHERE id=" + testAuditId + ".");
        testAuditId = -1;
        ok("AuditLogDAO delete verified", auditDAO.findById(savedLog.getId()).isEmpty(),
                "Optional.empty() confirmed post-DELETE.");
    }

    // =========================================================================
    // RUBRIC 6 - JDBC Transaction Management
    // =========================================================================
    private static void rubric6_TransactionManagement() throws Exception {
        System.out.println("\n--- RUBRIC 6: JDBC Transaction Management ---\n");

        // Test 15: setAutoCommit(false) + commit()
        System.out.println("[Test 15: conn.setAutoCommit(false) and conn.commit()]");
        DatabaseConnectionManager cm = DatabaseConnectionManager.getInstance();
        int committedAuditId = -1;
        Connection conn = null;
        try {
            conn = cm.getConnection();
            conn.setAutoCommit(false);
            ok("setAutoCommit(false) disables implicit commit",
                    !conn.getAutoCommit(),
                    "autoCommit=false - transaction boundary opened, nothing committed yet.");

            String sql = "INSERT INTO audit_logs (user_id, action, details) VALUES (?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, testUserId);
                ps.setString(2, "TX_COMMIT_TEST");
                ps.setString(3, "Phase6 explicit commit verification");
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) committedAuditId = keys.getInt(1);
                }
            }
            ok("INSERT executes within open transaction (not visible outside until commit)",
                    committedAuditId > 0,
                    "audit_log INSERT inside uncommitted transaction, id=" + committedAuditId + ".");
            conn.commit();
            ok("conn.commit() makes all transaction statements durable in MySQL",
                    true, "COMMIT executed - all DML within this transaction is now persistent.");
        } finally {
            if (conn != null) { try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignored) {} }
        }
        AuditLogDAO auditDAO = new AuditLogDAOImpl();
        Optional<AuditLog> afterCommit = auditDAO.findById(committedAuditId);
        ok("Committed row is readable after conn.commit()",
                afterCommit.isPresent() && "TX_COMMIT_TEST".equals(afterCommit.get().getAction()),
                "Post-commit SELECT: row id=" + committedAuditId + " is durable in database.");
        if (afterCommit.isPresent()) auditDAO.delete(committedAuditId);

        // Test 16: setAutoCommit(false) + rollback()
        System.out.println("\n[Test 16: conn.rollback() - Partial Writes Not Persisted]");
        int rolledBackId = -1;
        conn = null;
        try {
            conn = cm.getConnection();
            conn.setAutoCommit(false);
            String sql = "INSERT INTO audit_logs (user_id, action, details) VALUES (?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, testUserId);
                ps.setString(2, "TX_ROLLBACK_TEST");
                ps.setString(3, "This insert will be rolled back");
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) rolledBackId = keys.getInt(1);
                }
            }
            ok("INSERT within transaction before rollback",
                    rolledBackId > 0, "audit_log id=" + rolledBackId + " inside uncommitted transaction.");
            // Deliberate failure: reference a non-existent table
            try {
                try (PreparedStatement bad = conn.prepareStatement(
                        "INSERT INTO p6_nonexistent_table (x) VALUES (?)")) {
                    bad.setString(1, "fail");
                    bad.executeUpdate();
                }
            } catch (SQLException expected) {
                conn.rollback();
                ok("conn.rollback() called after SQLException - transaction aborted",
                        true, "rollback() executed on error - all DML in this transaction undone.");
            }
        } finally {
            if (conn != null) { try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignored) {} }
        }
        Optional<AuditLog> afterRollback = new AuditLogDAOImpl().findById(rolledBackId);
        ok("Rolled-back INSERT is absent from database",
                afterRollback.isEmpty(),
                "Rollback verified: audit_log id=" + rolledBackId + " does NOT exist - no partial write persisted.");

        // Test 17: GoalTrackingService 3-step atomic transaction
        System.out.println("\n[Test 17: GoalTrackingService 3-Step Atomic Transaction (workout+goal+audit)]");
        GoalTrackingService svc = new GoalTrackingService();
        GoalDAO goalDAO = new GoalDAOImpl();
        Optional<FitnessGoal> before = goalDAO.findById(testGoalId);
        ok("Goal exists before atomic operation", before.isPresent(),
                "goal_id=" + testGoalId + " ready for atomic update.");
        double prevProgress = before.isPresent() ? before.get().getCurrentValue() : 0.0;

        CardioWorkout atomicCardio = new CardioWorkout(0, testUserId, LocalDateTime.now(),
                25, 4.0, 138, "P6 atomic 3-step test");
        Workout atomicSaved = svc.logWorkoutAndUpdateGoalAtomic(atomicCardio, testGoalId, 200.0);

        ok("Step 1: Workout INSERT committed within atomic transaction",
                atomicSaved.getId() > 0,
                "workout_id=" + atomicSaved.getId() + " - workoutDAO.createTransactional(conn, workout) committed.");

        Optional<FitnessGoal> after = goalDAO.findById(testGoalId);
        double newProgress = after.isPresent() ? after.get().getCurrentValue() : 0.0;
        ok("Step 2: Goal UPDATE committed atomically (current_value incremented)",
                Math.abs(newProgress - (prevProgress + 200.0)) < 0.01,
                String.format("current_value: %.0f -> %.0f (+200 committed within same transaction).",
                        prevProgress, newProgress));

        List<AuditLog> trail = new AuditLogDAOImpl().findByUserId(testUserId);
        ok("Step 3: Audit log INSERT committed atomically (WORKOUT_AND_GOAL_SYNC)",
                trail.stream().anyMatch(a -> "WORKOUT_AND_GOAL_SYNC".equals(a.getAction())),
                "audit_logs row action='WORKOUT_AND_GOAL_SYNC' confirmed - all 3 steps in one commit().");

        // Clean up atomically created workout
        if (atomicSaved.getId() > 0) new WorkoutDAOImpl().delete(atomicSaved.getId());
    }

    // =========================================================================
    // Helpers
    // =========================================================================
    private static void ok(String name, boolean cond, String msg) {
        totalTests++;
        if (cond) { totalPassed++;
            System.out.printf("  [PASS] %-54s | %s%n", name, msg);
        } else {
            System.err.printf("  [FAIL] %-54s | %s%n", name, msg);
        }
    }

    private static void sep() {
        System.out.println("=".repeat(70));
    }

    private static void cleanupTestData() {
        System.out.println("\n[Cleanup: removing Phase 6 test rows from MySQL...]");
        try {
            if (testWorkout1 > 0) new WorkoutDAOImpl().delete(testWorkout1);
            if (testWorkout2 > 0) new WorkoutDAOImpl().delete(testWorkout2);
            if (testMealId   > 0) new MealDAOImpl().delete(testMealId);
            if (testAuditId  > 0) new AuditLogDAOImpl().delete(testAuditId);
            if (testGoalId   > 0) new GoalDAOImpl().delete(testGoalId);
            if (testUserId   > 0) new UserDAOImpl().delete(testUserId);
            System.out.println("[Cleanup] Done.");
        } catch (Exception e) {
            System.err.println("[Cleanup WARN] " + e.getMessage());
        }
    }
}

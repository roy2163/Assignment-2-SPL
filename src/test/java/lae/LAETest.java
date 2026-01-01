package lae;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import parser.ComputationNode;
import parser.ComputationNodeType;
import spl.lae.LinearAlgebraEngine;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


public class LAETest {
    private LinearAlgebraEngine lae;
    private final int NUM_THREADS = 2; // מספיק ת'רדים כדי לבדוק מקביליות בסיסית

    @BeforeEach
    void setUp() {
        lae = new LinearAlgebraEngine(NUM_THREADS);
    }

    @AfterEach
    void tearDown() throws InterruptedException {
        if (lae != null) {
            lae.shutdown();
        }
    }

    // --- Happy Path Tests (בדיקות תקינות) ---

    @Test
    void testSimpleAddition() {
        // הכנה: A + B
        double[][] dataA = {{1.0, 2.0}, {3.0, 4.0}};
        double[][] dataB = {{5.0, 6.0}, {7.0, 8.0}};
        
        ComputationNode nodeA = new ComputationNode(dataA);
        ComputationNode nodeB = new ComputationNode(dataB);
        
        // יצירת עץ עם List לפי המימוש החדש שלך
        ComputationNode root = new ComputationNode(ComputationNodeType.ADD, List.of(nodeA, nodeB));

        // הרצה
        ComputationNode resultNode = lae.run(root);

        // בדיקה: {{6, 8}, {10, 12}}
        double[][] result = resultNode.getMatrix();
        assertEquals(6.0, result[0][0]);
        assertEquals(8.0, result[0][1]);
        assertEquals(10.0, result[1][0]);
        assertEquals(12.0, result[1][1]);
    }

    @Test
    void testSimpleMultiplication() {
        // הכנה: A * B (מטריצות 2x2)
        // A = {{1, 2}, {3, 4}}
        // B = {{2, 0}, {1, 2}}
        double[][] dataA = {{1.0, 2.0}, {3.0, 4.0}};
        double[][] dataB = {{2.0, 0.0}, {1.0, 2.0}};

        ComputationNode nodeA = new ComputationNode(dataA);
        ComputationNode nodeB = new ComputationNode(dataB);
        
        ComputationNode root = new ComputationNode(ComputationNodeType.MULTIPLY, List.of(nodeA, nodeB));

        ComputationNode resultNode = lae.run(root);
        double[][] result = resultNode.getMatrix();

        // ציפייה: {{4, 4}, {10, 8}}
        assertEquals(4.0, result[0][0]);
        assertEquals(4.0, result[0][1]);
        assertEquals(10.0, result[1][0]);
        assertEquals(8.0, result[1][1]);
    }

    @Test
    void testTranspose() {
        // הכנה: A^T (וקטור שורה הופך לוקטור עמודה)
        double[][] data = {{1.0, 2.0, 3.0}};
        
        ComputationNode node = new ComputationNode(data);
        ComputationNode root = new ComputationNode(ComputationNodeType.TRANSPOSE, List.of(node));

        ComputationNode resultNode = lae.run(root);
        double[][] result = resultNode.getMatrix();

        assertEquals(3, result.length); // 3 שורות
        assertEquals(1, result[0].length); // עמודה 1
        assertEquals(1.0, result[0][0]);
        assertEquals(3.0, result[2][0]);
    }

    @Test
    void testComplexTreeExecution() {
        // בדיקת אינטגרציה: (A + B)^T
        // שלב 1 חיבור, שלב 2 שחלוף
        double[][] dataA = {{1.0, 2.0}};
        double[][] dataB = {{3.0, 4.0}};
        
        ComputationNode nodeA = new ComputationNode(dataA);
        ComputationNode nodeB = new ComputationNode(dataB);
        
        // יצירת עץ היררכי
        ComputationNode addNode = new ComputationNode(ComputationNodeType.ADD, List.of(nodeA, nodeB));
        ComputationNode root = new ComputationNode(ComputationNodeType.TRANSPOSE, List.of(addNode));

        // ה-LAE אמור לזהות ש-addNode הוא ה-resolvable הראשון, לפתור אותו, ואז לפתור את ה-root
        ComputationNode resultNode = lae.run(root);
        
        // ציפייה: {{4}, {6}}
        double[][] result = resultNode.getMatrix();
        assertEquals(4.0, result[0][0]);
        assertEquals(6.0, result[1][0]);
    }

    // --- Validation & Edge Cases (בדיקות שגיאות) ---

    @Test
    void testAdd_IncorrectChildCount_ShouldThrowException() {
        ComputationNode nodeA = new ComputationNode(new double[][]{{1}});
        
        // יצירת ADD עם ילד אחד בלבד (רשימה בגודל 1)
        ComputationNode root = new ComputationNode(ComputationNodeType.ADD, List.of(nodeA));

        // ה-LAE בודק בתוך ה-CASE ADD את גודל הרשימה
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            lae.loadAndCompute(root);
        });
        
        assertTrue(exception.getMessage().contains("exactly 2 children"), 
                   "Should complain about number of children");
    }

    @Test
    void testMultiply_IncorrectChildType_ShouldThrowException() {
        // יצירת MULTIPLY שהילדים שלו הם לא MATRIX (אלא אופרטורים שעדיין לא חושבו)
        // שים לב: זה בודק את ההגנה הפנימית של loadAndCompute
        ComputationNode nodeA = new ComputationNode(new double[][]{{1}});
        ComputationNode unfinishedOp = new ComputationNode(ComputationNodeType.ADD, List.of(nodeA, nodeA));
        
        ComputationNode root = new ComputationNode(ComputationNodeType.MULTIPLY, List.of(nodeA, unfinishedOp));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            lae.loadAndCompute(root);
        });
        
        assertTrue(exception.getMessage().contains("must be of type MATRIX"), 
                   "Should ensure operands are resolved before computing");
    }

    @Test
    void testRun_NullRoot() {
        assertThrows(IllegalArgumentException.class, () -> lae.run(null));
    }
}

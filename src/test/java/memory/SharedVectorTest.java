package memory;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SharedVectorTest {

    @Test
    void testGetAndLength() {
        double[] data = {1.0, 2.0, 3.0};
        SharedVector v = new SharedVector(data, VectorOrientation.ROW_MAJOR);
        assertEquals(3, v.length());
        assertEquals(1.0, v.get(0));
        assertEquals(3.0, v.get(2));
    }

    @Test
    void testTranspose() {
        double[] data = {1.0, 2.0};
        SharedVector v = new SharedVector(data, VectorOrientation.ROW_MAJOR);
        assertEquals(VectorOrientation.ROW_MAJOR, v.getOrientation());
        
        v.transpose();
        assertEquals(VectorOrientation.COLUMN_MAJOR, v.getOrientation());
        
        v.transpose();
        assertEquals(VectorOrientation.ROW_MAJOR, v.getOrientation());
    }

    @Test
    void testAdd() {
        // חיבור רגיל ותקין
        SharedVector v1 = new SharedVector(new double[]{1, 2}, VectorOrientation.ROW_MAJOR);
        SharedVector v2 = new SharedVector(new double[]{3, 4}, VectorOrientation.ROW_MAJOR);
        
        v1.add(v2);
        
        assertEquals(4.0, v1.get(0));
        assertEquals(6.0, v1.get(1));
    }

    @Test
    void testAddLengthMismatch() {
        // בדיקת הודעת השגיאה המעודכנת שלך ב-add
        SharedVector v1 = new SharedVector(new double[]{1, 2}, VectorOrientation.ROW_MAJOR);
        SharedVector v2 = new SharedVector(new double[]{1, 2, 3}, VectorOrientation.ROW_MAJOR);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            v1.add(v2);
        });
        
        // כאן מעודכן לפי הקוד ששלחת:
        assertEquals("Error: Illegal operation: dimension mismatch", exception.getMessage());
    }

    @Test
    void testNegate() {
        SharedVector v = new SharedVector(new double[]{1, -2}, VectorOrientation.ROW_MAJOR);
        v.negate();
        assertEquals(-1.0, v.get(0));
        assertEquals(2.0, v.get(1));
    }

    @Test
    void testDotProductSuccess() {
        // מכפלה סקלרית תקינה (שורה * עמודה)
        SharedVector row = new SharedVector(new double[]{1, 2}, VectorOrientation.ROW_MAJOR);
        SharedVector col = new SharedVector(new double[]{3, 4}, VectorOrientation.COLUMN_MAJOR);
        
        double result = row.dot(col); // 1*3 + 2*4 = 11
        assertEquals(11.0, result);
    }

    @Test
    void testDotProductLengthMismatch() {
        SharedVector row = new SharedVector(new double[]{1, 2}, VectorOrientation.ROW_MAJOR);
        SharedVector col = new SharedVector(new double[]{1, 2, 3}, VectorOrientation.COLUMN_MAJOR);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            row.dot(col);
        });

        assertEquals("Vectors must be of the same length to compute dot product.", exception.getMessage());
    }

    @Test
    void testDotProductSameOrientation() {
        // ניסיון לכפול שורה בשורה (אמור להיכשל)
        SharedVector v1 = new SharedVector(new double[]{1, 2}, VectorOrientation.ROW_MAJOR);
        SharedVector v2 = new SharedVector(new double[]{3, 4}, VectorOrientation.ROW_MAJOR);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            v1.dot(v2);
        });
        
        assertEquals("Vectors must be of different orientation to compute dot product.", exception.getMessage());
    }

    @Test
    void testVecMatMulSuccess() {
        // וקטור: [1, 2]
        SharedVector v = new SharedVector(new double[]{1, 2}, VectorOrientation.ROW_MAJOR);
        // מטריצה: [[3, 4], [5, 6]]
        SharedMatrix m = new SharedMatrix(new double[][]{{3, 4}, {5, 6}});

        v.vecMatMul(m);
        
        // [1*3 + 2*5, 1*4 + 2*6] = [13, 16]
        assertEquals(13.0, v.get(0));
        assertEquals(16.0, v.get(1));
    }

    @Test
    void testVecMatMulWrongVectorOrientation() {
        // וקטור עמודה לא יכול להכפיל מטריצה
        SharedVector colVec = new SharedVector(new double[]{1, 2}, VectorOrientation.COLUMN_MAJOR);
        SharedMatrix matrix = new SharedMatrix(new double[][]{{1, 2}, {3, 4}});

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            colVec.vecMatMul(matrix);
        });

        assertEquals("Vector must be in ROW_MAJOR orientation for vector-matrix multiplication.", exception.getMessage());
    }

    @Test
    void testVecMatMulDimensionMismatch() {
        // וקטור באורך 2 מול מטריצה עם 3 שורות
        SharedVector rowVec = new SharedVector(new double[]{1, 2}, VectorOrientation.ROW_MAJOR);
        SharedMatrix matrix = new SharedMatrix(new double[][]{
            {1, 2, 3}, 
            {4, 5, 6}, 
            {7, 8, 9}
        });

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            rowVec.vecMatMul(matrix);
        });
        
        String expectedMsg = "error: Illegal operation: dimensions mismatch";
        assertEquals(expectedMsg, exception.getMessage());
    }

    @Test
    void testGetOutOfBounds() {
        SharedVector v = new SharedVector(new double[]{10}, VectorOrientation.ROW_MAJOR);
        
        IndexOutOfBoundsException exception = assertThrows(IndexOutOfBoundsException.class, () -> {
            v.get(5);
        });
        
        assertEquals("Index 5 is out of bounds for vector of length 1", exception.getMessage());
    }
}
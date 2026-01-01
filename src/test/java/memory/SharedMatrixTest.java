package memory;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SharedMatrixTest {

    @Test
    void testLoadAndReadRowMajor() {
        double[][] data = {{1, 2}, {3, 4}};
        SharedMatrix m = new SharedMatrix(data);
        
        assertEquals(VectorOrientation.ROW_MAJOR, m.getOrientation());
        assertEquals(2, m.length());
        
        double[][] result = m.readRowMajor();
        assertArrayEquals(data[0], result[0]);
        assertArrayEquals(data[1], result[1]);
    }

    @Test
    void testLoadColumnMajor() {
        double[][] data = {{1, 2}, {3, 4}};
        SharedMatrix m = new SharedMatrix();
        m.loadColumnMajor(data);
        
        assertEquals(VectorOrientation.COLUMN_MAJOR, m.getOrientation());
        assertEquals(2, m.length());
        
        SharedVector col0 = m.get(0);
        assertEquals(1.0, col0.get(0));
        assertEquals(3.0, col0.get(1));
    }

    @Test
    void testReadRowMajorFromColumnMajor() {
        double[][] data = {{1, 2}, {3, 4}};
        SharedMatrix m = new SharedMatrix();
        m.loadColumnMajor(data);
        
        double[][] result = m.readRowMajor();
        assertArrayEquals(data[0], result[0]);
        assertArrayEquals(data[1], result[1]);
    }

    @Test
    void testLoadRowMajorAndReadRowMajorSquare() {
        double[][] data = {
            {1.0, 2.0},
            {3.0, 4.0}
        };
        
        SharedMatrix matrix = new SharedMatrix();
        matrix.loadRowMajor(data);
        
        assertEquals(VectorOrientation.ROW_MAJOR, matrix.getOrientation());
        
        double[][] result = matrix.readRowMajor();
        
        assertEquals(2, result.length);
        assertEquals(2, result[0].length);
        assertArrayEquals(data[0], result[0]);
        assertArrayEquals(data[1], result[1]);
    }

    @Test
    void testLoadRowMajorAndReadRowMajorRectangular() {
        double[][] data = {
            {1.0, 2.0, 3.0},
            {4.0, 5.0, 6.0}
        };
        
        SharedMatrix matrix = new SharedMatrix();
        matrix.loadRowMajor(data);
        
        assertEquals(2, matrix.length());
        assertEquals(3, matrix.get(0).length());
        
        double[][] result = matrix.readRowMajor();
        
        assertEquals(2, result.length);
        assertEquals(3, result[0].length);
        
        assertArrayEquals(data[0], result[0]);
        assertArrayEquals(data[1], result[1]);
    }

    @Test
    void testLoadRowMajorAndReadRowMajorTallRectangle() {
        double[][] data = {
            {1.0},
            {2.0},
            {3.0}
        };
        
        SharedMatrix matrix = new SharedMatrix();
        matrix.loadRowMajor(data);
        
        double[][] result = matrix.readRowMajor();
        
        assertEquals(3, result.length);
        assertEquals(1, result[0].length);
        
        assertEquals(1.0, result[0][0]);
        assertEquals(2.0, result[1][0]);
        assertEquals(3.0, result[2][0]);
    }

    @Test
    void testReadRowMajorWorksOnColumnMajorData() {
        double[][] data = {
            {1.0, 2.0},
            {3.0, 4.0}
        };
        
        SharedMatrix matrix = new SharedMatrix();
        matrix.loadColumnMajor(data);
        
        assertEquals(VectorOrientation.COLUMN_MAJOR, matrix.getOrientation());
        
        double[][] result = matrix.readRowMajor();
        
        assertArrayEquals(data[0], result[0]);
        assertArrayEquals(data[1], result[1]);
    }
}
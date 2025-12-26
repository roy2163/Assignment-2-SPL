package memory;

public class SharedMatrix {

    private volatile SharedVector[] vectors = {}; // underlying vectors

    public SharedMatrix() {
        // TODO: initialize empty matrix
    }

    public SharedMatrix(double[][] matrix) {
        // TODO: construct matrix as row-major SharedVectors
        if (matrix == null) {
            throw new IllegalArgumentException("Matrix data cannot be null");
        }
        if (matrix.length == 0) {
            throw new IllegalArgumentException("Matrix cannot be empty");
        }
        vectors = new SharedVector[matrix.length];
        for (int i = 0; i < matrix.length; i++) {
            SharedVector vec = new SharedVector(matrix[i], VectorOrientation.ROW_MAJOR);
            vectors[i] = vec;
        }
    }

    public void loadRowMajor(double[][] matrix) {
        // TODO: replace internal data with new row-major matrix
        if (matrix == null) {
            throw new IllegalArgumentException("Matrix data cannot be null");
        }
        if (matrix.length == 0) {
            throw new IllegalArgumentException("Matrix cannot be empty");
        }
        vectors = new SharedVector[matrix.length];
        for (int i = 0; i < matrix.length; i++) {
            SharedVector vec = new SharedVector(matrix[i], VectorOrientation.ROW_MAJOR);
            vectors[i] = vec;
        }
    }

    public void loadColumnMajor(double[][] matrix) {
        // TODO: replace internal data with new column-major matrix
        if (matrix == null) {
            throw new IllegalArgumentException("Matrix data cannot be null");
        }
        if (matrix.length == 0) {
            throw new IllegalArgumentException("Matrix cannot be empty");
        }
        vectors = new SharedVector[matrix[0].length];
        for(int j = 0; j < matrix[0].length; j++) {
            double[] currentColumn = new double[matrix.length];
            for (int i = 0; i < matrix.length; i++) {
                currentColumn[i] = matrix[i][j];
            }
            SharedVector vec = new SharedVector(currentColumn, VectorOrientation.COLUMN_MAJOR);
            vectors[j] = vec;
        }
    }

    public double[][] readRowMajor() {
        // TODO: return matrix contents as a row-major double[][]
        if (vectors.length == 0) {
            return new double[0][0];
        }
        double[][] result = null;
        int numCols;
        int numRows;
        acquireAllVectorReadLocks(vectors);
        try{
            if(vectors[0].getOrientation() == VectorOrientation.ROW_MAJOR){
                numRows = vectors.length;
                numCols = vectors[0].length();
                result = new double[numRows][numCols];
            }
            else{
                numRows = vectors[0].length();
                numCols = vectors.length;
                result = new double[numRows][numCols];
            }


            for (int i = 0; i < vectors.length; i++) {
                SharedVector vec = vectors[i];
                if(vec.getOrientation() == VectorOrientation.ROW_MAJOR) {
                    for (int j = 0; j < numCols; j++) {
                        result[i][j] = vec.get(j);
                    }
                }
                else{
                    for (int j = 0; j < numRows; j++) {
                        result[j][i] = vec.get(j);
                    }
                }
                
            }
        }
        finally{
            releaseAllVectorReadLocks(vectors);
        }
        return result;
    }

    public SharedVector get(int index) {
        // TODO: return vector at index
        if(index < 0 || index >= vectors.length){
            throw new IndexOutOfBoundsException("Index out of bounds: " + index);
        }
        return vectors[index];
    }

    public int length() {
        // TODO: return number of stored vectors
        return vectors.length;
    }

    public VectorOrientation getOrientation() {
        // TODO: return orientation
        if (vectors.length == 0){ throw new IllegalStateException("Matrix is empty"); } 
        return vectors[0].getOrientation();
    }

    private void acquireAllVectorReadLocks(SharedVector[] vecs) {
        // TODO: acquire read lock for each vector
        for(int i = 0; i < vecs.length; i++){
            vecs[i].readLock();
        }
    }

    private void releaseAllVectorReadLocks(SharedVector[] vecs) {
        // TODO: release read locks
        for(int i = 0; i < vecs.length; i++){
            vecs[i].readUnlock();
        }
    }

    private void acquireAllVectorWriteLocks(SharedVector[] vecs) {
        // TODO: acquire write lock for each vector
        for(int i = 0; i < vecs.length; i++){
            vecs[i].writeLock();
        }
    }

    private void releaseAllVectorWriteLocks(SharedVector[] vecs) {
        // TODO: release write locks
        for(int i = 0; i < vecs.length; i++){
            vecs[i].writeUnlock();
        }
    }
}

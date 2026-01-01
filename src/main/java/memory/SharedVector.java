package memory;

import java.util.concurrent.locks.ReadWriteLock;

public class SharedVector {

    private double[] vector;
    private VectorOrientation orientation;
    private ReadWriteLock lock = new java.util.concurrent.locks.ReentrantReadWriteLock();

    public SharedVector(double[] vector, VectorOrientation orientation) {
        this.vector = vector;
        this.orientation = orientation;
    }

    public double get(int index) {
        // TODO: return element at index (read-locked)
        lock.readLock().lock();
        try{
            if(index < 0 || index >= this.vector.length) {
                throw new IndexOutOfBoundsException("Index " + index + " is out of bounds for vector of length " + this.vector.length);
            }
            return this.vector[index];
        } finally {
            lock.readLock().unlock();
        }
    }

    public int length() {
        return this.vector.length;
    }

    public VectorOrientation getOrientation() {
        return this.orientation;
    }

    public void writeLock() {
        lock.writeLock().lock();
    }

    public void writeUnlock() {
        lock.writeLock().unlock();
    }

    public void readLock() {
        lock.readLock().lock();
    }

    public void readUnlock() {
        lock.readLock().unlock();
    }

    public void transpose() {
        writeLock();
        try{
            if(this.orientation == VectorOrientation.ROW_MAJOR) {
                this.orientation = VectorOrientation.COLUMN_MAJOR;
            } else {
                this.orientation = VectorOrientation.ROW_MAJOR;
            }
        } finally {
            writeUnlock();
        }

    }

    public void add(SharedVector other) {
        if(other.length() != this.length()) {
            throw new IllegalArgumentException("Error: Illegal operation: dimension mismatch");
        }
        writeLock();
        other.readLock();
        try{
            for (int i = 0; i < this.length(); i++) {
                this.vector[i] += other.vector[i];
            }
        }
        finally {
            writeUnlock();
            other.readUnlock();
        }
    }

    public void negate() {
        writeLock();
        try{
            for (int i = 0; i < this.length(); i++) {
               this.vector[i] = -this.vector[i];
            }
        }
        finally{    
            writeUnlock();
        }

    }

    public double dot(SharedVector other) {
        if(other.length() != this.length()) {
            throw new IllegalArgumentException("Vectors must be of the same length to compute dot product.");
        }        
        if(this.orientation != other.getOrientation()) {
            throw new IllegalArgumentException("Vectors must be of different orientation to compute dot product.");
        }
        readLock();
        other.readLock();
        try{
            double result = 0.0;
            for (int i = 0; i < this.length(); i++) {
                result += this.vector[i] * other.vector[i];
            }
            return result;
        } 
        finally {
            readUnlock();
            other.readUnlock();
        }

    }

    private void validateDimensions(int vLen, int mRows, int mCols) {
        if (this.getOrientation() == VectorOrientation.ROW_MAJOR && vLen != mRows) {
            throw new IllegalArgumentException("Row vector length (" + vLen + ") must match matrix rows (" + mRows + ")");
        }
        if (this.getOrientation() == VectorOrientation.COLUMN_MAJOR && vLen != mCols) {
            throw new IllegalArgumentException("Column vector length (" + vLen + ") must match matrix columns (" + mCols + ")");
        }
    }
    public void vecMatMul(SharedMatrix matrix) {
        VectorOrientation mOrient = matrix.getOrientation();
        if(this.getOrientation() != VectorOrientation.ROW_MAJOR){
            throw new IllegalArgumentException("Vector must be in ROW_MAJOR orientation for vector-matrix multiplication.");
        }
        int mRows = (matrix.getOrientation() == VectorOrientation.COLUMN_MAJOR) ? matrix.get(0).length() : matrix.length();
        int mCols = (matrix.getOrientation() == VectorOrientation.COLUMN_MAJOR) ? matrix.length() : matrix.get(0).length();
        validateDimensions(this.length(), mRows, mCols);

        double[] result;

        writeLock();
        try{
            if(mOrient == VectorOrientation.COLUMN_MAJOR){
                result = new double[matrix.length()];
                for (int i = 0; i < matrix.length(); i++) {
                    result[i] = this.dot(matrix.get(i));
                }
            }
            else{
                int resultLen = matrix.get(0).length();
                int matrixInternalListSize = matrix.length();
                result = new double[resultLen];
                for (int i = 0; i < resultLen; i++) {
                    double sum = 0.0;

                    for (int j = 0; j < matrixInternalListSize; j++) {
                        sum += this.get(j) * matrix.get(j).get(i);
                    }
                    result[i] = sum;
                }
            }
            this.vector = result;
        }
        finally{
            writeUnlock();
        }
    }
}

package bid.mythesis.factorization;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Matrix for working with matrices over GF(2) space
 * @author David Salac
 */
public class MatrixGF2 {
    private byte matrix[][];
    int rowsCount;
    int colsCount;

    /**
     * Fill the matrix of size rows x cols with zeros
     * @param rows Number of rows in new matrix
     * @param cols Number of columns in new matrix
     */
    public MatrixGF2(int rows, int cols) {
        matrix = new byte[rows][cols];
        this.rowsCount = rows;
        this.colsCount = cols;
    }
    /**
     * Swap two rows in matrix
     * @param rowNr1 First position of element to be swapped
     * @param rowNr2 Second position of element to be swapped
     */
    public void swapRows(int rowNr1, int rowNr2) {
        if(rowNr1 == rowNr2) return;
        byte temp[] = new byte[this.getColsCount()];
        for(int i = 0; i < this.getColsCount(); i++) {
            temp[i] = matrix[rowNr1][i];
            matrix[rowNr1][i] = matrix[rowNr2][i];
            matrix[rowNr2][i] = temp[i];
        }
    }
    
    /**
     * Standard Gaussian elimination
     * @param toRowNr1 Target row
     * @param fromRowNr2 Source row of added data
     */
    public void addToRowAnotherRow(int toRowNr1, int fromRowNr2) {
        for(int i = 0; i < this.getColsCount(); i++) {
            matrix[toRowNr1][i] = (byte)(((int)matrix[toRowNr1][i] + (int)matrix[fromRowNr2][i])%2);
            if(matrix[toRowNr1][i] < 0)
                matrix[toRowNr1][i] = (byte)((-1) * (int)matrix[toRowNr1][i]);
        }
    }
    /**
     * Returns new equivalent matrix in reduce echelon form
     * @return Matrix in pivoted format (Only ones in (pseudo)diagonal and zero above and under diagonal)
     */
    public MatrixGF2 pivoting() {
        MatrixGF2 m = this.copy();
        //FORWARD
        for(int diag = 0; diag < Integer.min(m.getRowsCount(), m.getColsCount()); diag++) {
            int colValue = m.getColsCount();
            int rowToSwap = diag;
            if (m.getElement(diag, diag) == 0)  {
                for(int row = diag; row < m.getRowsCount(); row++) {
                    int actRowValue = 0;
                    for(byte b : m.matrix[row]) {
                        if(b == 1) break;
                        actRowValue++;
                    }
                    if(actRowValue <= colValue) {
                        colValue = actRowValue;
                        rowToSwap = row;
                    }
                }
                m.swapRows(diag, rowToSwap);
            }
            for(int row = diag + 1; row < m.getRowsCount(); row++) {
                if(m.getElement(row, colValue != m.getColsCount() ? colValue : diag ) == 1) {
                    m.addToRowAnotherRow(row, diag);

                }
            }
            
            
        }
        
        //BACKWARD
        m = m.copyWithoutZeroRows();
        for(int row = m.getRowsCount() - 1; row > 0; row--) {
            int col = m.getRowValue(row);
            for(int nr = row - 1; nr >= 0; nr--) {
                if(m.getElement(nr, col) == 1) {
                    m.addToRowAnotherRow(nr, row);
                }
            }
        }
        
        /*
        //FORWARD
        for(int diag = 0; diag < Integer.min(m.getRowsCount(), m.getColsCount()); diag++) {
            if(m.getElement(diag, diag) == 1) {
                continue;
            } else {
                int maxRowCount = diag;
                int pivotNr = 0;
                for(int row = diag; row < m.getRowsCount(); row ++) {
                    int tempPivot = 0;
                    for(int i = 0; i < m.getColsCount(); i++) {
                        if(m.getElement(row, i) != 0) {
                            tempPivot = m.getColsCount() - i;
                            break;
                        }
                    }
                    if(tempPivot > pivotNr) {
                        pivotNr = tempPivot;
                        maxRowCount = row;
                    }
                }
                m.swapRows(maxRowCount, diag);
            }
        }
        /*
        
        //BACKWARD AGAIN
        for(int diag = 0; diag < Integer.min (m.getRowsCount(), m.getColsCount()); diag++ ) {
            int colIndex = 0;
            for(byte b : m.matrix[diag]) {
                if(b == 1) break;
                colIndex++;
            }
            colIndex = Integer.min(colIndex, m.getColsCount() - 1);
            for(int row = diag - 1; row >= 0; row --) {
                if(m.getElement(row, colIndex) > 0) {
                    m.addToRowAnotherRow(row, diag);
                }
            }
        }
        
        /*
        //BACKWARD AGAIN (for m x n matrix where n > m)
        for(int diag = Integer.min(m.getRowsCount() - 1, m.getColsCount() - 1); diag > 0; diag-- ) {
            int pivotPos = 0;
            for(int i = 0; i < m.getColsCount(); i++) {
                if(m.getElement(diag, i) != 0) {
                    pivotPos = i;
                    break;
                }
            }
            for(int row = diag - 1; row >= 0; row --) {
                if(m.getElement(row, pivotPos) > 0) {
                    m.addToRowAnotherRow(row, diag);
                }
            }
        }*/
        return m.copyWithoutZeroRows();
    }
    
    /**
     * Fill current matrix with random numbers
     */
    public void fillWithRandNumbers() {
        Random randomGenerator = new SecureRandom();
        for(int rowIndex = 0; rowIndex < this.rowsCount; rowIndex++) {
            for(int colIndex = 0; colIndex < this.colsCount; colIndex++) {
                setElement(rowIndex, colIndex, (byte)randomGenerator.nextInt());
            }
        }
    }
    /**
     * Change data of selected row with new ones
     * @param insertData New data to be inserted
     * @param row Number of row to be changed
     */
    public void insertRow(byte[] insertData, int row) {
        if(row >= 0 && row < getRowsCount() && insertData.length != getColsCount()) return;
        for(int col = 0; col < getColsCount(); col ++) {
            setElement(row, col, insertData[col]);
        }
    }
    /**
     * Change data of selected row with new ones
     * @param insertData New data to be inserted (as integers)
     * @param row Number of row to be changed
     */
    public void insertRow(Integer[] insertData, int row) {
        if(row >= 0 && row < getRowsCount() && insertData.length != getColsCount()) return;
        for(int col = 0; col < getColsCount(); col ++) {
            setElement(row, col, insertData[col] );
        }
    }
    /**
     * Get count of row in matrix
     * @return Count of row in matrix
     */
    public int getRowsCount() {
        return this.rowsCount;
    }
    /**
     * Get count of columns in matrix
     * @return Count of columns in matrix
     */
    public int getColsCount() {
        return this.colsCount;
    }
    /**
     * Change value of some element in matrix
     * @param rows Row nr. of element
     * @param cols Columns nr. of element
     * @param value New value to be inserted
     */
    public void setElement(int rows, int cols, byte value) {
        matrix[rows][cols] = (byte)(((int)value > 0 ? (int)value : (-1) * (int)value) % 2);
    }
    /**
     * Change value of some element in matrix
     * @param rows Row nr. of element
     * @param cols Columns nr. of element
     * @param value New value to be inserted
     */
    public void setElement(int rows, int cols, int value) {
        this.setElement(rows, cols, (byte)((value > 0 ? value : (-1) * value) % 2));
    }
    /**
     * Returns value of element
     * @param rows Position of element (row)
     * @param cols Position of element (column)
     * @return Selected value
     */
    public byte getElement(int rows, int cols) {
        return matrix[rows][cols];
    }
    /**
     * Naive algorithm for multiplying of two matrices
     * @param m Factor of multiplication
     * @return Result of multiplication
     */
    public MatrixGF2 multiply(MatrixGF2 m){
        if(this.colsCount != m.rowsCount) {
            return null;
        }
        MatrixGF2 prod = new MatrixGF2(this.rowsCount, m. colsCount);
        /*for(int rowIndex = 0; rowIndex < this.rowsCount; rowIndex++) {
            for(int colIndex = 0; colIndex < m.colsCount; colIndex++) {
                int value = 0;
                for(int i = 0; i < this.colsCount; i++) {
                    value += (int)this.getElement(rowIndex, i) * (int)m.getElement(i, colIndex);
                }
                prod.setElement(rowIndex, colIndex, (byte)(value));
            }
        }*/
        for(int colIndex = 0; colIndex < m.colsCount; colIndex++) {
            for(int rowIndex = 0; rowIndex < this.rowsCount; rowIndex++) {
                int value = 0;
                for(int i = 0; i < m.rowsCount; i++) {
                    value += (m.getElement(i, colIndex) * this.getElement(rowIndex, i));
                }
                prod.setElement(rowIndex, colIndex, value);
            }
        }
        return prod;
    }
    /**
     * Transpose matrix
     * @return Transposed matrix
     */
    public MatrixGF2 transpose() {
        MatrixGF2 trans = new MatrixGF2(this.colsCount, this.rowsCount);
        for(int rowIndex = 0; rowIndex < this.rowsCount; rowIndex++) {
            for(int colIndex = 0; colIndex < this.colsCount; colIndex++) {
                trans.setElement(colIndex, rowIndex, this.getElement(rowIndex, colIndex));
            }
        }
        return trans;
    }
    /**
     * Create a deep copy of current matrix
     * @return Copy of matrix
     */
    public MatrixGF2 copy() {
        MatrixGF2 copy = new MatrixGF2(this.rowsCount, this.colsCount);
        for(int rowIndex = 0; rowIndex < this.rowsCount; rowIndex++) {
            for(int colIndex = 0; colIndex < this.colsCount; colIndex++) {
                copy.setElement(rowIndex, colIndex, this.getElement(rowIndex, colIndex));
            }
        }
        return copy;
    }
    /**
     * Copies current matrix without rows that contains only zeros
     * @return Returns copy of matrix without zero rows
     */
    public MatrixGF2 copyWithoutZeroRows() {
        int zeroRowCount = 0;
        for(int row = getRowsCount() - 1; row > 0; row --) {
            boolean toDelete = true;
            for(int col = 0; col < this.getColsCount(); col ++) {
                if(this.getElement(row, col) != 0) {
                    toDelete = false;
                    break;
                }
            }
            if(toDelete)
                zeroRowCount++;
        }
        if(this.rowsCount - zeroRowCount == 0)
            return null;
        MatrixGF2 copy = new MatrixGF2(this.rowsCount - zeroRowCount, this.colsCount);
        for(int rowIndex = 0; rowIndex < this.rowsCount - zeroRowCount; rowIndex++) {
            for(int colIndex = 0; colIndex < this.colsCount; colIndex++) {
                copy.setElement(rowIndex, colIndex, this.getElement(rowIndex, colIndex));
            }
        }
        return copy;
    }
    /**
     * Check if current matrix contains only zero elements
     * @return True if current matrix contains only zeros, otherwise false
     */
    public boolean isZeroMatrix() {
        for(int rowIndex = 0; rowIndex < this.rowsCount; rowIndex++) {
            for(int colIndex = 0; colIndex < this.colsCount; colIndex++) {
                if(this.getElement(rowIndex, colIndex) != 0)
                    return false;
            }
        }
        return true;
    }
    /**
     * Returns current matrix with added columns from other matrix
     * @param m Matrix to be added
     * @return New matrix that contains columns of current matrix and matrix m
     */
    public MatrixGF2 rightJoin(MatrixGF2 m) {
        MatrixGF2 retVal = new MatrixGF2( Integer.max(this.getRowsCount(), m.getRowsCount()), this.getColsCount() + m.getColsCount());
        for(int row = 0; row < this.getRowsCount(); row ++ ) {
            for(int cols = 0; cols < this.getColsCount(); cols++) {
                retVal.setElement(row, cols, this.getElement(row, cols));
            }
        }
        for(int row = 0; row < m.getRowsCount(); row ++ ) {
            for(int cols = 0; cols < m.getColsCount(); cols++) {
                retVal.setElement(row, cols + this.getColsCount(), m.getElement(row, cols));
            }
        }
        return retVal;
    }
    /**
     * Join other matrix to bottom of current matrix
     * @param m Matrix to be joined
     * @return New matrix with rows of current matrix and matrix m
     */
    public MatrixGF2 bottomJoin(MatrixGF2 m) {
        MatrixGF2 retVal = new MatrixGF2( m.getRowsCount() + this.getRowsCount(), Integer.max(m.getColsCount(), this.getColsCount()) );
        for(int col = 0; col < this.getColsCount(); col++) {
            for(int row = 0; row < this.getRowsCount(); row++) {
                retVal.setElement(row, col,   this.getElement(row, col)   );
            }
        }
        for(int col = 0; col < m.getColsCount(); col++) {
            for(int row = 0; row < m.getRowsCount(); row++) {
                retVal.setElement(row + this.getRowsCount(), col,  m.getElement(row, col)  );
            }
        }
        return retVal;
    }
    /*public MatrixGF2 getRandomElementOfNullspace() {
        Random randomGenerator = new SecureRandom();
        //Check if algorithm does not freeze (maximum number of iterations equals 10^10;
        for(long freeze = 0; freeze < 10000000000L; freeze++) {
            byte randVector[] = new byte[this.colsCount];
            randomGenerator.nextBytes(randVector); 
            MatrixGF2 randV = new MatrixGF2( colsCount , 1);
            int trivialTest = 0;
            for(int i = 0; i < colsCount; i ++) {
                randV.setElement(i, 0, randVector[i]);
                trivialTest += (int)randV.getElement(i, 0);
            }
            MatrixGF2 potNullvector = this.multiply(randV);
            if(potNullvector.isZeroMatrix() && trivialTest > 0)
                return randV;
        }
        return null;
    }*/
    /**
     * Create copy of current matrix with inserted zero row after chosen row
     * @param afterRow After this row the new zero row will be inserted
     * @return Matrix with inserted zero row
     */
    public MatrixGF2 insertZeroRow(int afterRow) {
        MatrixGF2 newMatrix = new MatrixGF2(rowsCount + 1, colsCount);
        if(afterRow < 0 || afterRow >= rowsCount) return null;
        for(int row = 0; row < rowsCount; row++) {
            int nRow = row;
            if(nRow > afterRow) { nRow = row + 1; }
            for(int col = 0; col < colsCount; col ++) {
                newMatrix.setElement(nRow, col, getElement(row, col));
            }
        }
        return newMatrix;
    }
    /**
     * Select whole column
     * @param index Position of column
     * @return The value of column as matrix 1 x n
     */
    public MatrixGF2 getColumn(int index) {
        if(index < 0 || index >= getColsCount() ) return null;
        MatrixGF2 col = new MatrixGF2(getRowsCount(), 1);
        for(int i = 0; i < col.getRowsCount(); i++) {
            col.setElement(i, 0, this.getElement(i, index));
        }
        return col;
    }
    
    /**
     * Compute null space (kernel) of current matrix using Gaussian elimination
     * @return Null space of current matrix
     */
    public MatrixGF2 getNullspace() {
        MatrixGF2 m = pivoting();
        List<Integer> columns = new ArrayList<>();
        int lastColumn = 0;
        int offset = 0;
        for(int diag = 0; diag < Integer.min(m.getColsCount(), m.getRowsCount()); diag++) {
            if(diag + offset >= m.getColsCount()) break;
            if(m.getElement(diag, diag + offset) == 0) {
                columns.add(diag + offset);
                lastColumn = diag + offset;
                offset++;
                diag--;
            }
        }
        
        for(int col = m.getRowValue(m.getRowsCount() - 1) + 1; col < m.getColsCount(); col++) {
            columns.add(col);
        }
        
        if(columns.isEmpty()) { return null; }
        
        //Inserting zeros rows 
        for(Integer i : columns) {
            if(i > 0)
                m = m.insertZeroRow(i-1);
        }
        
        
        
        MatrixGF2 nullsp = new MatrixGF2(m.getColsCount(), columns.size());
        for (int i = 0; i < columns.size(); i++) {
            MatrixGF2 nspRow = m.getColumn(columns.get(i));
            for(int k = 0; k < nspRow.getRowsCount(); k++) {
                nullsp.setElement(k, i, nspRow.getElement(k,0));
            }
            nullsp.setElement(columns.get(i), i, 1);
        }
        return nullsp;
    }
    
    /**
     * Returns the position of first non-zero element in matrix
     * @param row Number of row in matrix
     * @return Index of first 1 in row (f. e. 001011 -> 2)
     */
    public int getRowValue(int row) {
        if(row < 0 || row >= this.getRowsCount()) return -1;
        for(int val = 0; val < this.getColsCount(); val++) {
            if(this.getElement(row, val) == 1) return val;
        }
        return this.getColsCount() - 1;
    }
    
    /**
     * Print the matrix
     * @param insertRowValue Printed text contains the "value" of each row
     */
    public void printMatrix(boolean insertRowValue) {
        for(int rowIndex = 0; rowIndex < this.rowsCount; rowIndex++) {
            boolean notFind = true;
            for(int colIndex = 0; colIndex < this.colsCount; colIndex++) {
                System.out.print(getElement(rowIndex, colIndex));
            }
            if(insertRowValue)
                System.out.print(" - " + Integer.toString(this.getRowValue(rowIndex)));
            System.out.println();
        }
    }
    
    /**
     * Just print the matrix content
     */
    public void printMatrix() {
        printMatrix(false);
    }
    
    /**
     * Print matrix content in form that could be used in GNU Octave
     */
    public void printOctaveLikeMatrix() {
        System.out.print("[");
        for(int rowIndex = 0; rowIndex < this.rowsCount; rowIndex++) {
            for(int colIndex = 0; colIndex < this.colsCount; colIndex++) {
                System.out.print(Byte.toString(getElement(rowIndex, colIndex)) + " ");
            }
            if(rowIndex < this.rowsCount - 1)
                System.out.print(";\n");
        }
        System.out.print("]");
    }   
}

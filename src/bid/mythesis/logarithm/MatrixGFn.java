package bid.mythesis.logarithm;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * Working with matrix over GF(n) for some integer n > 2
 * @author David Salac
 */
public class MatrixGFn {
    private final BigInteger modulus; //Modulus

    private BigInteger [] n; //Factors of modulus
    private BigInteger matrix[][][];//First is row, second is col, third is modulus (index as above)
    private final int rowCount; private final int columnCount;
    
    /**
     * Return the value of modulus
     * @return modulus value
     */
    public BigInteger getModulus() {
        return this.modulus;
    }

    /**
     * Count of rows in the matrix
     * @return count of rows
     */
    public int getRowsCount() {
        return this.rowCount;
    }

    /**
     * Count of columns in the matrix
     * @return count of columns
     */
    public int getColsCount() {
        return this.columnCount;
    }

    /**
     * Set the element to the defined value
     * @param row Number of row
     * @param col Number of column
     * @param value New value
     */
    public void setElement(int row, int col, BigInteger value) {
        for(int i = 0; i < n.length; i++) {
            this.matrix[row][col][i] = value.mod(n[i]);
        }
    }
    
    /**
     * Insert new values to defined row by factoring of fetched number
     * @param row Number of the row
     * @param number The number to be factorized
     * @param rightSide Value of right side of equation
     * @param factorBase Factor base
     * @return true if input is smooth over factor base, false if it not
     */
    public boolean insertRowOverFactorBase(int row, BigInteger number, BigInteger rightSide, BigInteger[] factorBase) {
        if(factorBase.length + 1 != columnCount) return false;
        BigInteger[] res = new BigInteger[factorBase.length];
        BigInteger temp = new BigInteger(number.toString());
        for(int i = 0; i < factorBase.length; i++) {
            res[i] = new BigInteger("0");
            if(temp.mod(factorBase[i]).equals(BigInteger.ZERO)) {
                for(int exp = 1; exp < 4096; exp++) {
                    if( ! temp.mod(factorBase[i].pow(exp)).equals(BigInteger.ZERO)) {
                        exp = exp - 1; 
                        res[i] = new BigInteger(Integer.toString(exp));
                        temp = temp.divide(factorBase[i].pow(exp));
                        break;
                    }
                }
            }
        }
        if(temp.equals(BigInteger.ONE)) {
            this.setElement(row, factorBase.length, rightSide);
            for(int i = 0; i < factorBase.length; i ++) {
                this.setElement(row, i, res[i]);
            }
            return true;
        }
        return false;
    }
    
    /**
     * Deep copy of matrix
     * @return deep copy of matrix
     */
    public MatrixGFn copy() {
        MatrixGFn temp = new MatrixGFn(rowCount, columnCount, modulus, n);
        for(int i = 0; i < rowCount; i++) {
            for(int j = 0; j < columnCount; j++) {
                for(int k = 0; k < n.length; k ++) {
                    temp.matrix[i][j][k] = new BigInteger(this.matrix[i][j][k].toString());
                }
            }
        }
        return temp;
    }
    
    private void multipleRow(int row, int dimension, BigInteger factor) {
        for(int i = 0; i < columnCount; i++) {
            this.matrix[row][i][dimension] = factor.multiply(this.matrix[row][i][dimension]).mod(n[dimension]);
        }
    }
    
    private int rowAmount(int row, int dimension) {
        for(int i = 0; i < columnCount; i ++ ) {
            if(! this.getElement(row, i, dimension).equals(BigInteger.ZERO)) {
                return i;
            }
        }
        return 0;
    }
    private void addMultipleOfRowToAnother(int sourceRow, int targetRow, BigInteger factor, int dimension) {
        if(factor.equals(BigInteger.ZERO))
            return;
        for(int col = 0; col < this.columnCount; col++) {
            this.matrix[targetRow][col][dimension] = this.matrix[targetRow][col][dimension].add(factor.multiply(this.matrix[sourceRow][col][dimension])).mod(n[dimension]);
        }
    }

    /**
     * Get matrix in reduce echelon form
     * @return reduce echelon form of matrix
     */
    public MatrixGFn reduceEchelonForm() {
        MatrixGFn m = this.copy();
        for(int dimension = 0; dimension < n.length; dimension ++) {
            //FORWARD:
            for(int diag = 0; diag < m.getRowsCount() - 1; diag++) {
                if(m.getElement(diag, diag, dimension).equals(BigInteger.ZERO) || !n[dimension].gcd(m.getElement(diag, diag, dimension)).equals(BigInteger.ONE) ) {
                    for(int row = diag + 1; row < m.rowCount; row++) {
                        if(m.rowAmount(row, dimension) == diag && n[dimension].gcd(m.getElement(row, diag, dimension)).equals(BigInteger.ONE) ) {
                            m.swapRows(row, diag, dimension);
                            break;
                        }
                    }
                }
                m.multipleRow(diag, dimension, m.getElement(diag, diag, dimension).modInverse(n[dimension]) );

                for(int row = diag + 1; row < m.rowCount; row ++) {
                    if( ! m.getElement(row, diag, dimension).equals(BigInteger.ZERO)) {
                        m.addMultipleOfRowToAnother(diag, row, 
                                n[dimension].subtract( m.getElement(row, diag, dimension) ), 
                                dimension);
                    }
                } 
            }
            
            //BACKWARD:
            for(int diag = m.getRowsCount() - 1; diag > 0; diag --) {
                for(int row = diag - 1; row >= 0; row--) {
                    if(! m.getElement(row, diag, dimension).equals(BigInteger.ZERO)) {
                        m.addMultipleOfRowToAnother(diag, row, 
                                n[dimension].subtract( m.getElement(row, diag, dimension) ), 
                                dimension);
                    }
                }
            }
        }
        return m;
    }
    
    /**
     * Solve the equation set
     * @return Values of variables
     */
    public BigInteger[] solve() {
        MatrixGFn pivoting = this.reduceEchelonForm();
        BigInteger [] res = new BigInteger[rowCount];
        for(int row = 0; row < rowCount; row++) {
            res[row] = new BigInteger( pivoting.getElement(row, columnCount - 1).toString() );// System.out.println("*"+res[row]);
        }
        return res;
    }
    
    private void swapRows(int rowOld, int rowNew, int dimension) {
        BigInteger[] temp = new BigInteger[this.columnCount];
        for(int i = 0; i < this.columnCount; i++) {
            temp[i] = new BigInteger(this.matrix[rowOld][i][dimension].toString());
            this.matrix[rowOld][i][dimension] = new BigInteger(this.matrix[rowNew][i][dimension].toString());
            this.matrix[rowNew][i][dimension] = new BigInteger(temp[i].toString());
        }
    }
    
    private BigInteger getElement(int row, int col, int dimension) {
        return matrix[row][col][dimension];
    }
    
    /**
     * Get the element on some coordinate
     * @param row Row coordinate
     * @param col Column coordinate
     * @return The value on coordinate (row, col)
     */
    public BigInteger getElement(int row, int col) {
        BigInteger solution = new BigInteger("0");
        BigInteger M = this.modulus;
        for(int i = 0; i < n.length; i ++) { 
            BigInteger Mi = M.divide(n[i]); 
            BigInteger MiOverline = Mi.modInverse(n[i]);
            solution = solution.add(Mi.multiply(MiOverline).multiply(matrix[row][col][i]));
        }
        return solution.mod(M);
    }
    
    private final void factorizeN() {
        BigInteger modulusTemp = new BigInteger(this.modulus.toString());
        List<BigInteger> piList = new ArrayList<>();
        for(BigInteger p = new BigInteger("2"); p.compareTo(modulus) < 1; p = p.add(BigInteger.ONE)) { 
            if(modulusTemp.mod(p).compareTo(BigInteger.ZERO) == 0) {
                for(int alphai = 1; alphai < 4096; alphai++) {
                    if( (modulusTemp.mod(p.pow(alphai))).compareTo(BigInteger.ZERO) != 0 ) {
                        alphai -= 1; 
                        piList.add(p.pow(alphai));
                        modulusTemp = modulusTemp.divide(p.pow(alphai));
                        if(modulusTemp.isProbablePrime(100) || modulusTemp.compareTo(BigInteger.ONE) == 0) {
                            if(modulusTemp.compareTo(BigInteger.ONE) != 0) {
                                piList.add(modulusTemp);
                            }
                            
                            this.n = new BigInteger[piList.size()];
                            for(int i = 0; i < piList.size(); i++) {
                                n[i] = piList.get(i);
                            }
                            return;
                        }
                        break;
                    }
                }
            }
        }
    }
    
    /**
     * Create an instance of class
     * @param rowCount
     * @param columnCount
     * @param modulus
     */
    public MatrixGFn(int rowCount, int columnCount, BigInteger modulus) {
        this.rowCount = rowCount; this.columnCount = columnCount;
        this.modulus = new BigInteger(modulus.toString());
        this.factorizeN();
        this.matrix = new BigInteger[rowCount][columnCount][this.n.length];
    }
    private MatrixGFn(int rowCount, int columnCount, BigInteger modulus, BigInteger[] n) {
        this.rowCount = rowCount; this.columnCount = columnCount;
        this.modulus = new BigInteger(modulus.toString());
        this.matrix = new BigInteger[rowCount][columnCount][n.length];
        this.n = new BigInteger[n.length];
        for(int i = 0; i < n.length; i ++) {
            this.n[i] = new BigInteger(n[i].toString());
        }
    }
    
    /**
     * Print the matrix to the standard output
     */
    public void printMatrix() {
        for(int i = 0; i < rowCount; i++) {
            for(int j = 0; j < columnCount; j++) {
                for(int k = 0; k < n.length; k ++) {
                    System.out.print(matrix[i][j][k] + ".");
                }
                System.out.print(" \t ");
            }
            System.out.println();
        }
    }

    /**
     * Print one dimension of the matrix to standard output
     * @param dimension The dimension
     */
    public void printMatrix(int dimension) {
        if(dimension < 0) {
            for(int i = 0; i < rowCount; i++) {
                for(int j = 0; j < columnCount; j++) {
                    if(this.matrix[i][j][0] != null) {
                        System.out.print(this.getElement(i, j));
                    } else {
                        System.out.print("-");
                    }
                    System.out.print(" \t ");
                }
                System.out.println();
            }
        } else {
            for(int i = 0; i < rowCount; i++) {
                for(int j = 0; j < columnCount; j++) {
                    System.out.print(matrix[i][j][dimension] + ".");

                    System.out.print(" \t ");
                }
                System.out.println();
            }
        }
    }
    
    
}

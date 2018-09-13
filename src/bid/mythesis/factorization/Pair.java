package bid.mythesis.factorization;

import java.util.Objects;

/**
 * Generic class for tuple of two element
 * @author David Salac
 * @param <T1> Type of left element
 * @param <T2> Type of right element
 */
public class Pair<T1, T2> {
    private final T1 lElement;
    private final T2 rElement;

    /**
     * Set elements of tuple
     * @param left left variable
     * @param right right variable
     */
    public Pair(T1 left, T2 right) {
        this.lElement = left;
        this.rElement = right;
    }
    
    /**
     * Get the value of left variable
     * @return Value of left side
     */
    public T1 getLeft() { return lElement; }
    
    /**
     * Get the value of right variable
     * @return Value of right side
     */
    public T2 getRight() { return rElement; }
    
    /**
     * For purpose of comparison
     * @param o Another object
     * @return True if o has same data as this object
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Pair)) {
            return false;
        }
        Pair pairo = (Pair) o;
        return  lElement.equals(pairo.getLeft()) 
                && rElement.equals(pairo.getRight());
    }

    /**
     * Get hash code of current class
     * @return Hash of this class
     */
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + Objects.hashCode(this.lElement);
        hash = 59 * hash + Objects.hashCode(this.rElement);
        return hash;
    }
}
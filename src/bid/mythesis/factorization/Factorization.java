/**
 * Package for solving of integer factorization problem
 */
package bid.mythesis.factorization;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Class for integer factorization problem containing fundamental functionality
 * @author David Salac
 */
public abstract class Factorization implements Runnable {
    private final BigInteger number;
    
    /**
     * For algorithm's effectiveness
     */
    protected static final BigInteger BIGINTEGER_TWO = new BigInteger("2");
    //Maximal number of iterations of each method

    /**
     * Maximal number of iterations
     */
    protected long maxIterations;
    /**
     * Returns value of integer to be factorized
     * @return Value of integer which to be factored
     */
    public final BigInteger getN() { 
        return this.number;
    }
    /**
     * Create instance of class for factorization of integer n
     * @param n Integer to be factored
     */
    public Factorization(BigInteger n) {
        this.number = new BigInteger(n.toString());
        this.maxIterations = 1000000000L;
    }
    
    /**
     * Proceed the computation of square root of n using Newton formula
     * @param n Base integer to extract
     * @param poly Polynomial coefficient
     * @param precision Precision of computation
     * @return Square root of n
     */
    private static BigDecimal computeBigIntegerSqrt(BigDecimal n, BigDecimal poly, BigDecimal precision) {
        //Simple method for computing square root of number using Newton's method (recurent function)
        BigDecimal digits = new BigDecimal(150);
        BigDecimal funcMult = poly.multiply(new BigDecimal(2));
        BigDecimal func = poly.pow(2).add(n.negate());
        BigDecimal polynomial = func.divide(funcMult, 2 * digits.intValue(), RoundingMode.HALF_DOWN);
        polynomial = poly.add(polynomial.negate());
        BigDecimal sqrt = polynomial.pow(2);
        BigDecimal sqrtPrec = sqrt.subtract(n);
        sqrtPrec = sqrtPrec.abs();
        if (sqrtPrec.compareTo(precision) <= -1) {
            return polynomial;
        }
        return computeBigIntegerSqrt(n, polynomial, precision);
    }
    /**
     * Returns value of square root of n
     * @param n Base integer
     * @return Square root of n
     */
    protected static BigInteger bigIntegerSqrt(BigInteger n){
        BigDecimal digits = new BigDecimal(150);
        BigDecimal prepare = new BigDecimal(10).pow(digits.intValue());
        return new BigInteger(computeBigIntegerSqrt(new BigDecimal(n.toString()),new BigDecimal(1),new BigDecimal(1).divide(prepare)).setScale(0, RoundingMode.CEILING).toString());
    }
    /**
     * Sorts the generic map
     * @param <K> Key type
     * @param <V> Value type
     * @param map Map set to be sorted
     * @return Sorted Map (descending)
     */
    protected static <K, V extends Comparable<? super V>> Map<K, V> sortByValue( Map<K, V> map )
    {
        List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>( map.entrySet() );
        Collections.sort( list, new Comparator<Map.Entry<K, V>>()
        {
            public int compare( Map.Entry<K, V> o1, Map.Entry<K, V> o2 )
            {
                return (-1)*(o1.getValue()).compareTo( o2.getValue() );
            }
        } );

        Map<K, V> result = new LinkedHashMap<K, V>();
        for (Map.Entry<K, V> entry : list)
        {
            result.put( entry.getKey(), entry.getValue() );
        }
        return result;
    }
    
    /**
     * Finds k root of n using bisection method
     * @param n Base integer value
     * @param k Degree of the root
     * @param pivot Current pivot value
     * @param pivotUp Upper bound
     * @param pivotDown Lower bound
     * @return Value of k root of n
     */
    public static BigInteger kRootOfNByBisectionMethod(BigInteger n, int k, BigInteger pivot, BigInteger pivotUp, BigInteger pivotDown) {
        //Finding pivot^k
        BigInteger find = pivot.pow(k);
        //---------------
        if(pivotUp.subtract(pivotDown).compareTo(BigInteger.ONE) <= 0)
            return pivot;
        if( find.compareTo(n) > 0 ) {
            BigInteger newPivot = (pivot.add(pivotDown)).divide(new BigInteger("2"));
            return kRootOfNByBisectionMethod(n, k, newPivot,pivot, pivotDown);
        }
        else if(find.compareTo(n) < 0) {
            BigInteger newPivot = (pivot.add(pivotUp)).divide(new BigInteger("2"));
            return kRootOfNByBisectionMethod(n, k, newPivot, pivotUp, pivot);
        }
        else
            return pivot;
    }
    /**
     * Finds the k root of n
     * @param n Base integer
     * @param k Degree of root
     * @return Returns k root of n
     */
    protected static BigInteger kRootOfN(BigInteger n, int k) {
        return kRootOfNByBisectionMethod(n, k, n.divide(new BigInteger("2")), n, BigInteger.ZERO);
    }
    
    /**
     * Returns value of polynomial f(x) mod n
     * @param x Variable of polynomial
     * @param polynomial Definition of polynomial a_n x^n ... a_0
     * @param number Modulus n
     * @return Returns value f(x) mod n
     */
    protected static BigInteger polyVal(BigInteger x, BigInteger[] polynomial, BigInteger number) {
        BigInteger value = new BigInteger("0");
        for(int i = 0; i < polynomial.length; i++) {
            value = value.add( (polynomial[i].multiply(x.pow(i))).mod(number) );
        }
        return value.mod(number);
    }
    /**
     * Returns exponents of numbers on factor base
     * @param n Number for which it is computed
     * @param factorBase Array of numbers (commonly prime)
     * @return Exponent for each factor in FB or null (if n is not smooth over FB)
     */
    protected static Integer[] getFactorBaseCoeficients(BigInteger n, BigInteger [] factorBase) {
        BigInteger nr = new BigInteger(n.toString());
        Integer[] exp = new Integer[factorBase.length];
        for(int i = 0; i < factorBase.length; i++) {
            exp[i] = 0;
            for(int maxExp = 1; maxExp < 4096; maxExp++) {
                if(nr.mod(factorBase[i].pow(maxExp)).compareTo(BigInteger.ZERO) == 0 ) continue;
                exp[i] = maxExp - 1;
                nr = nr.divide(factorBase[i].pow(exp[i]));
                break;
            }
        }
        if(nr.compareTo(BigInteger.ONE) != 0) return null;
        return exp;
    }
    
    /**
     * Generate factor base suitable for Dixon's method or Quadratic Sieve based on probability of incidence of each factor
     * @param number Modulus n
     * @param fbSize Size of required factor base
     * @param randomSetSize Cardinality of random set
     * @return Factor base with k (k = fbSize) elements (prime numbers)
     */
    protected static BigInteger[] generateProbabilisticFactorBase(BigInteger number, int fbSize, int randomSetSize) {
        Random randomGenerator = new SecureRandom();

        //Prime factors numerosity:
        Map<BigInteger, Integer> numerosity = new HashMap<>();
                
        BigInteger sqrtN = Factorization.bigIntegerSqrt(number); 
        BigInteger x, x2;
        
        for(int i = 0; i < randomSetSize; i++) { 
            //Generate odd x (pseudorandom x^e mod n for random x, res. is in interval sqrt(n) ... n)
            x = new BigInteger(number.bitCount(), randomGenerator);
            for(int e = 3; e < 100; e++) {
                x = x.modPow(new BigInteger(Integer.toString(e)), number);
                if(x.compareTo(sqrtN) > 0) {
                    break;
                }
            }
            if(x.compareTo(BigInteger.ONE) == 0) {
                i--; continue;
            }
            if(x.mod(new BigInteger("2")).compareTo(BigInteger.ZERO) == 0) 
                x = x.add(BigInteger.ONE);
            //------------------
            
            //Find x^2 mod n:
            x2 = x.modPow(new BigInteger("2"), number);
            
            //Find factors of x^2 mod n (partial brute force with fixed factor base):
            BruteForce xf = new BruteForce(x2, 100000);
            PrimeToExp[] factors = xf.bruteForceFactorizationIncExp();
            if(factors == null) { i --; continue; }
            
            for(PrimeToExp factorExp : factors) {
                BigInteger factor = factorExp.getP();
                if(numerosity.containsKey(factor)) {
                    int currentCount = numerosity.get(factor);
                    currentCount++;
                    numerosity.put(factor, currentCount);
                }
                else {
                    numerosity.put(factor, 1);
                }
            }
        }

        //Factor base (FB):
        int factorBaseSize = Integer.min(fbSize, numerosity.size());
        BigInteger factorBase [] = new BigInteger[factorBaseSize];
        numerosity = sortByValue(numerosity);
        int currentFBsize = 0;
        for(Map.Entry<BigInteger, Integer> set : numerosity.entrySet()) {
            if(currentFBsize < factorBaseSize) {
                factorBase[currentFBsize] = set.getKey();
                currentFBsize++;
            } else break;
        }
        return factorBase;
    }
    
    /**
     * Proceeds selected numeric method for factoring of integer
     * @return Returns factorization of number
     */
    public abstract BigInteger [] commitMethod();
        
    /**
     * Create suitable instance of class Factorization
     * @param n Number to be factorized
     * @return Instance of class Factorization with implemented proper method
     */
    public static Factorization initInstance(BigInteger n) {
        int nBitCount = n.bitLength();
        Factorization method = null;
        if(nBitCount < 20) { 
            method = new BruteForce(n);
        }
        else if (nBitCount < 30) { 
            method = new PollardRho(n);
        }
        else if (nBitCount < 50) { 
            method = new Dixon(n);
        }
        else if (nBitCount < 70) { 
            method = new QuadraticSieve(n);
        }
        //For else return null
        return method;
    }
    
    /**
     * Proceeds integer factorization method and show the results
     */
    @Override
    public void run() { 
        BigInteger [] factors = this.commitMethod();
        for(BigInteger b : factors) {
            System.out.println(b);
        }
    }
}

package com.system.kripkestructure;

import com.system.BDDSingleFactory;
import com.system.util.SingleLogger;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import net.sf.javabdd.BDD;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 *
 * @author Alexander Erin <arcquim@gmail.com>
 */
public class AtomicPredicate {
    
    private static volatile int counter = 0;

    private final int predicateNumber;
    private final String condition;
    private BDD predicateBDD;
    private Deque<String> postfixFormula;
    private int length;
    private Map<Variable, String> variablesValues;
    private Map<String, Variable> variablesNames;
    
    private static final String TRUE = "true";
    private static final String FALSE = "false";
    
    private static Logger logger;
    
    static {
        logger = SingleLogger.getLogger();
    }
    
    /**
     *
     * @param condition A condition that represents this atomic predicate; it's
     * equivalent to condition using in <i>if</i> or <i>while</i>.
     */
    public AtomicPredicate(String condition) {
        this.condition = condition;
        predicateNumber = counter++;
    }
    
    /**
     *
     * @return BDD representing characteristic function of this atomic predicate 
     * on states of Kripke structure.
     */
    public BDD getPredicateBDD() {
        if (predicateBDD == null) {
            predicateBDD = BDDSingleFactory.getInstanse().zero();
        }
        return predicateBDD;
    }

    /**
     *
     * @return A number of this atomic predicate.
     */
    public int getPredicateNumber() {
        return predicateNumber;
    }

    /**
     *
     * @return String representing the condition has been given in a constructor 
     * of this AtomicPredicate instance.
     */
    public String getCondition() {
        return condition;
    }
    
    /**
     *
     * @param parser An object of ConditionParser class
     * @return true if predicate if successfully parsed; false otherwise
     */
    protected boolean toPostfix(ConditionParser parser) {
        postfixFormula = parser.parseCondition(condition);
        return postfixFormula != null;
    }
    
    /**
     *
     * @param variablesValues Map where each variable of this atomic predicate
     * has its appropriate value
     * @param calculator An object of Calculator class
     * @return
     */
    protected Boolean calculate(Map<Variable, String> variablesValues, 
            Calculator calculator) {
        String value = calculator.calculate(variablesValues, condition, 
                new ArrayDeque<>(postfixFormula));
        if (value == null) {
            return null;
        }
        if (value.equals(TRUE)) {
            return true;
        }
        if (value.equals(FALSE)) {
            return false;
        }
        logger.log(Level.ERROR, condition + " is not a condition");
        return null;
    }
    
    /**
     *
     * @param BDDToAdd A BDD to add to BDD of this atomic predicate.
     */
    protected void addToBDD(BDD BDDToAdd) {
        if (predicateBDD == null) {
            predicateBDD = BDDToAdd;
        }
        predicateBDD = predicateBDD.or(BDDToAdd);
    }
}

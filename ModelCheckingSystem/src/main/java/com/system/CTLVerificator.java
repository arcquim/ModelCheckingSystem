package com.system;

import com.system.kripkestructure.Variable;
import com.system.util.SingleLogger;
import java.util.ArrayList;
import java.util.List;
import net.sf.javabdd.BDD;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 *
 * @author Alexander Erin <arcquim@gmail.com>
 */
public class CTLVerificator {
    
    private static final String EMPTY = "The model or property is empty.";
    private static final String NO_VERIFICATION = "There was no verification,"
            + " but counterexamples has been requested.";
    
    private BDD propertyBDD;
    private BDD startStatesBDD;
    private List<Variable> variables;
    private BDD counterexample;
    private BDDStringParser parser;
    private VerificationResult verificationResult;
    
    private static Logger logger;
    
    static {
        logger = SingleLogger.getLogger();
    }

    /**
     *
     * @param propertyBDD BDD representing characteristic function of 
     * states of Kripke structure where checking CTL (LTL is not supported) 
     * property formula turns to TRUE.
     * 
     */
    public void setPropertyBDD(BDD propertyBDD) {
        this.propertyBDD = propertyBDD;
    }

    /**
     *
     * @param startStatesBDD BDD representing characteristic function of start 
     * states of Kripke structure.
     */
    public void setStartStatesBDD(BDD startStatesBDD) {
        this.startStatesBDD = startStatesBDD;
    }

    /**
     *
     * @param variables List of program variables.
     */
    public void setVariables(List<Variable> variables) {
        this.variables = variables;
    }

    /**
     *
     * @return Result of verification.
     */
    public VerificationResult getVerificationResult() {
        if (isModelValid()) {
            return verify();
        }
        logger.log(Level.ERROR, EMPTY);
        return VerificationResult.EMPTY_MODEL_OR_PROPERTY;
    }
    
    /**
     *
     * @param numberOfExamples Number of counterexamples required. Maximum 
     * number of  exampes returning to invoking code is 1000 now; -1 is 
     * equivalent to maximum number of variables (1000 now). This parameter 
     * should be positive of -1.
     * @return List of counterexamples, which size is less or equals 
     * numberOfExamples, but more than 0, if property doen't holds on 
     * Kripke structure; empty list if property holds; null if there wasn't 
     * any verification (method getVerificationResult hasn't been invoked 
     * before).
     * @throws IllegalArgumentException
     */
    public List<String> getCounterexamples(int numberOfExamples) 
            throws IllegalArgumentException {
        if(numberOfExamples < -1 || numberOfExamples == 0) {
            throw new IllegalArgumentException();
        }
        if (counterexample == null) {
            logger.log(Level.INFO, NO_VERIFICATION);
            return null;
        }
        if (counterexample.isZero()) {
            return new ArrayList<>();
        }
        if (parser == null) {
            parser = new BDDStringParser(variables);
        }
        return parser.parse(counterexample.toString(), numberOfExamples);
    }
    
    private boolean isModelValid() {
        return !(propertyBDD == null || startStatesBDD == null || 
                variables == null || variables.isEmpty());
    }
    
    private VerificationResult verify() {
        if (counterexample == null) {
            BDD disjunction = propertyBDD.or(startStatesBDD);
            counterexample = disjunction.xor(propertyBDD);
            if (disjunction.equals(propertyBDD)) {
                verificationResult = VerificationResult.PROPERTY_HOLDS;
            }
            else {
                verificationResult = VerificationResult.PROPERTY_NOT_HOLDS;
            }
        }
        return verificationResult;
    }
}

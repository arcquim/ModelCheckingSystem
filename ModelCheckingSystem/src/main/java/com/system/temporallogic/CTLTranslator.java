package com.system.temporallogic;

import com.system.BDDSingleFactory;
import com.system.util.SingleLogger;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 *
 * @author Alexander Erin <arcquim@gmail.com>
 */
public class CTLTranslator implements TemporalLogicTranslator {
    
    private static final String AND = "AND";
    private static final String OR = "OR";
    private static final String NOT = "NOT";
    private static final String XOR = "XOR";
    private static final String AX = "AX";
    private static final String EX = "EX";
    private static final String AF = "AF";
    private static final String EF = "EF";
    private static final String AU = "AU";
    private static final String EU = "EU";
    private static final String AG = "AG";
    private static final String EG = "EG";
    private final int lowBorder;
    private int highBorder;
    
    private final String formula;
    private BDD statesTransitionsBDD;
    private List<BDD> atomicPredicatesBDD;
    private BDD result;
    private Stack<BDD> keeper;
    private Deque<Object> postfixFormula;
    private BDDFactory factory;
    private static Logger logger;
    
    static {
        logger = SingleLogger.getLogger();
    }

    /**
     *
     * @param formula CTL formula to translate into BDD
     * Constructor of CTLTranslator class
     */
    public CTLTranslator(String formula) {
        lowBorder = 0;
        this.formula = formula;
    }
    
    /**
     *
     * @return BDD to which the CTL formula given in constructor is translated
     * @throws IllegalArgumentException if the CTL formula is bad-written one
     */
    @Override
    public BDD getBDDResult() throws IllegalArgumentException {
        if (postfixFormula == null) {
            postfixFormula = new CTLFormulaParser(formula).getPostfixFormula();
        }
        translateToBDD();
        return result;
    }

    /**
     *
     * @param statesTransitionBDD BDD representing characteristic function of transitions 
     * between program states
     * @throws IllegalArgumentException if statesTransitionBDD argument is null
     */
    @Override
    public void setStatesTransitions(BDD statesTransitionBDD) {
        if (statesTransitionBDD == null) {
            throw new IllegalArgumentException();
        }
        this.statesTransitionsBDD = statesTransitionBDD;
    }

    /**
     *
     * @param atomicPredicatesBDD List of BDDs representing characteristic 
     * function of atomic predicates
     * @throws IllegalArgumentException if atomicPredicatesBDD argument is null 
     * or empty
     */
    @Override
    public void setAtomicPredicates(List<BDD> atomicPredicatesBDD) {
        int length = atomicPredicatesBDD.size();
        if (length == 0) {
            throw new IllegalArgumentException();
        }
        this.atomicPredicatesBDD = atomicPredicatesBDD;
        highBorder = length - 1;
    }

    private void translateToBDD() throws IllegalArgumentException {
        if (atomicPredicatesBDD == null || atomicPredicatesBDD.isEmpty() ||
                statesTransitionsBDD == null) {
            throw new IllegalArgumentException();
        }
        keeper = new Stack<>();
        ElementState currentState;
        BDD operand, leftOperand, rightOperand;
        logger.log(Level.INFO, "Starting translation of " + formula);
        factory = BDDSingleFactory.getInstanse();
        while(!postfixFormula.isEmpty()) {
            Object element = postfixFormula.pollFirst();
            if (element == null) {
                logger.log(Level.INFO, "Reached the end of a postfix formula");
                return;
            }
            currentState = determineState(element);
            switch(currentState) {
                case OPERAND:
                    logger.log(Level.INFO, "Translating operand number " + element);
                    keeper.push(atomicPredicatesBDD.get((Integer) element));
                    break;
                    
                case NOT:
                    if (keeper.isEmpty()) {
                        logger.log(Level.ERROR, "Wrong formula");
                        logger.log(Level.INFO, "Translation cancelled");
                        throw new IllegalArgumentException();
                    }
                    operand = keeper.pop();
                    if (operand == null) {
                        logger.log(Level.ERROR, "Empty element");
                        throw new IllegalArgumentException();
                    }
                    logger.log(Level.INFO, "Translating " + element + " operator");
                    keeper.push(translateNot(operand));
                    break;
                    
                case AND:
                    if (keeper.size() < 2) {
                        logger.log(Level.ERROR, "Wrong formula");
                        logger.log(Level.INFO, "Translation cancelled");
                        throw new IllegalArgumentException();
                    }
                    rightOperand = keeper.pop();
                    leftOperand = keeper.pop();
                    if (rightOperand == null || leftOperand == null) {
                        logger.log(Level.ERROR, "Empty element");
                        throw new IllegalArgumentException();
                    }
                    logger.log(Level.INFO, "Translating " + element + " operator");
                    keeper.push(translateAnd(leftOperand, rightOperand));
                    break;
                    
                case OR:
                    if (keeper.size() < 2) {
                        logger.log(Level.ERROR, "Wrong formula");
                        logger.log(Level.INFO, "Translation cancelled");
                        throw new IllegalArgumentException();
                    }
                    rightOperand = keeper.pop();
                    leftOperand = keeper.pop();
                    if (rightOperand == null || leftOperand == null) {
                        logger.log(Level.ERROR, "Empty element");
                        throw new IllegalArgumentException();
                    }
                    logger.log(Level.INFO, "Translating " + element + " operator");
                    keeper.push(translateOr(leftOperand, rightOperand));
                    break;
                    
                case XOR:
                    if (keeper.size() < 2) {
                        logger.log(Level.ERROR, "Wrong formula");
                        logger.log(Level.INFO, "Translation cancelled");
                        throw new IllegalArgumentException();
                    }
                    rightOperand = keeper.pop();
                    leftOperand = keeper.pop();
                    if (rightOperand == null || leftOperand == null) {
                        logger.log(Level.ERROR, "Empty element");
                        throw new IllegalArgumentException();
                    }
                    logger.log(Level.INFO, "Translating " + element + " operator");
                    keeper.push(translateXor(leftOperand, rightOperand));
                    break;
                    
                case AX:
                    if (keeper.isEmpty()) {
                        logger.log(Level.ERROR, "Wrong formula");
                        logger.log(Level.INFO, "Translation cancelled");
                        throw new IllegalArgumentException();
                    }
                    operand = keeper.pop();
                    if (operand == null) {
                        logger.log(Level.ERROR, "Empty element");
                        throw new IllegalArgumentException();
                    }
                    logger.log(Level.INFO, "Translating " + element + " operator");
                    keeper.push(translateAx(operand));
                    break;
                    
                case EX:
                    if (keeper.isEmpty()) {
                        logger.log(Level.ERROR, "Wrong formula");
                        logger.log(Level.INFO, "Translation cancelled");
                        throw new IllegalArgumentException();
                    }
                    operand = keeper.pop();
                    if (operand == null) {
                        logger.log(Level.ERROR, "Empty element");
                        throw new IllegalArgumentException();
                    }
                    logger.log(Level.INFO, "Translating " + element + " operator");
                    keeper.push(translateEx(operand));
                    break;
                    
                case AF:
                    if (keeper.isEmpty()) {
                        logger.log(Level.ERROR, "Wrong formula");
                        logger.log(Level.INFO, "Translation cancelled");
                        throw new IllegalArgumentException();
                    }
                    operand = keeper.pop();
                    if (operand == null) {
                        logger.log(Level.ERROR, "Empty element");
                        throw new IllegalArgumentException();
                    }
                    logger.log(Level.INFO, "Translating " + element + " operator");
                    keeper.push(translateAf(operand));
                    break;
                    
                case EF:
                    if (keeper.isEmpty()) {
                        logger.log(Level.ERROR, "Wrong formula");
                        logger.log(Level.INFO, "Translation cancelled");
                        throw new IllegalArgumentException();
                    }
                    operand = keeper.pop();
                    if (operand == null) {
                        logger.log(Level.ERROR, "Empty element");
                        throw new IllegalArgumentException();
                    }
                    logger.log(Level.INFO, "Translating " + element + " operator");
                    keeper.push(translateEf(operand));
                    break;
                    
                case AG:
                    if (keeper.isEmpty()) {
                        logger.log(Level.ERROR, "Wrong formula");
                        logger.log(Level.INFO, "Translation cancelled");
                        throw new IllegalArgumentException();
                    }
                    operand = keeper.pop();
                    if (operand == null) {
                        logger.log(Level.ERROR, "Empty element");
                        throw new IllegalArgumentException();
                    }
                    logger.log(Level.INFO, "Translating " + element + " operator");
                    keeper.push(translateAg(operand));
                    break;
                    
                case EG:
                    if (keeper.isEmpty()) {
                        logger.log(Level.ERROR, "Wrong formula");
                        logger.log(Level.INFO, "Translation cancelled");
                        throw new IllegalArgumentException();
                    }
                    operand = keeper.pop();
                    if (operand == null) {
                        logger.log(Level.ERROR, "Empty element");
                        throw new IllegalArgumentException();
                    }
                    logger.log(Level.INFO, "Translating " + element + " operator");
                    keeper.push(translateEg(operand));
                    break;
                    
                case AU:
                    if (keeper.size() < 2) {
                        logger.log(Level.ERROR, "Wrong formula");
                        logger.log(Level.INFO, "Translation cancelled");
                        throw new IllegalArgumentException();
                    }
                    rightOperand = keeper.pop();
                    leftOperand = keeper.pop();
                    if (rightOperand == null || leftOperand == null) {
                        logger.log(Level.ERROR, "Empty element");
                        throw new IllegalArgumentException();
                    }
                    logger.log(Level.INFO, "Translating " + element + " operator");
                    keeper.push(translateAu(leftOperand, rightOperand));
                    break;
                    
                case EU:
                    if (keeper.size() < 2) {
                        logger.log(Level.ERROR, "Wrong formula");
                        logger.log(Level.INFO, "Translation cancelled");
                        throw new IllegalArgumentException();
                    }
                    rightOperand = keeper.pop();
                    leftOperand = keeper.pop();
                    if (rightOperand == null || leftOperand == null) {
                        logger.log(Level.ERROR, "Empty element");
                        throw new IllegalArgumentException();
                    }
                    logger.log(Level.INFO, "Translating " + element + " operator");
                    keeper.push(translateEu(leftOperand, rightOperand));
                    break;
            }
        }
        if (keeper.size() != 1) {
            logger.log(Level.ERROR, "Wrong formula");
            logger.log(Level.INFO, "Translation cancelled");
            throw new IllegalArgumentException();
        }
        logger.log(Level.INFO, "Formula is successfully translated");
        result = keeper.pop();
    }
    
    private boolean isOperator(Object element) {
        return element instanceof String;
    }

    private ElementState determineState(Object element) {
        if (isOperator(element)) {
            switch ((String) element) {
                case AND:
                    return ElementState.AND;
                    
                case OR:
                    return ElementState.OR;
                    
                case XOR:
                    return ElementState.XOR;
                    
                case NOT:
                    return ElementState.NOT;
                    
                case AX:
                    return ElementState.AX;
                    
                case EX:
                    return ElementState.EX;
                    
                case AF:
                    return ElementState.AF;
                    
                case EF:
                    return ElementState.EF;
                    
                case AU:
                    return ElementState.AU;
                    
                case EU:
                    return ElementState.EU;
                    
                case AG:
                    return ElementState.AG;
                    
                case EG:
                    return ElementState.EG;
            }
        }
        else {
            Integer number = (Integer) element;
            if (number >= lowBorder && number <= highBorder) {
                return ElementState.OPERAND;
            }
        }
        logger.log(Level.ERROR, element.toString() + " is not a valid number of atomic predicate");
        throw new IllegalArgumentException();
    }

    private BDD shiftBDD(BDD operand) {
        if (operand.isOne() || operand.isZero()) {
            return operand;
        }
        String stringBDD = operand.toString();
        int numberOfVariables = factory.varNum();
        int shift = numberOfVariables / 2;
        BDD resultBDD = factory.one();
        Map<Integer, Integer> valuableVariables = new HashMap<>();
        String[] tempSplitedBDD = stringBDD.split("<");
        boolean BDDCreated = false;
        for (int i = 0; i < tempSplitedBDD.length; i++) {
            if (tempSplitedBDD[i].isEmpty()) {
                continue;
            }
            tempSplitedBDD[i] = tempSplitedBDD[i].trim().replaceAll(">", "");
            String[] tempResult = tempSplitedBDD[i].split(", ");
            for (int j = 0; j < tempResult.length; j++) {
                if (tempResult[j].isEmpty()) {
                    continue;
                }
                String[] variableAndValue = tempResult[j].trim().split(":");
                boolean firstOccurance = true;
                Integer variableNumber = 0, variableValue = 0;
                for (int k = 0; k < variableAndValue.length; k++) {
                    if (variableAndValue[k].isEmpty()) {
                        continue;
                    }
                    if (firstOccurance) {
                        firstOccurance = false;
                        variableNumber = Integer.parseInt(variableAndValue[k]) + shift;
                    }
                    else {
                        variableValue = Integer.parseInt(variableAndValue[k]);
                    }
                }
                valuableVariables.put(variableNumber, variableValue);
            }
            if (!BDDCreated) {
                BDDCreated = true;
                Set<Integer> keySet = valuableVariables.keySet();
                Iterator<Integer> keySetIterator = keySet.iterator();
                if (keySetIterator.hasNext()) {
                    int variable = keySetIterator.next();
                    int value = valuableVariables.get(variable);
                    if (value == 1) {
                        resultBDD = factory.ithVar(variable);
                    }
                    else {
                        resultBDD = factory.ithVar(variable).not();
                    }
                }
                while (keySetIterator.hasNext()) {
                    int variable = keySetIterator.next();
                    int value = valuableVariables.get(variable);
                    if (value == 1) {
                        resultBDD = resultBDD.and(factory.ithVar(variable));
                    }
                    else {
                        resultBDD = resultBDD.and(factory.ithVar(variable).not());
                    }
                }
            }
            else {
                Set<Integer> keySet = valuableVariables.keySet();
                Iterator<Integer> keySetIterator = keySet.iterator();
                BDD tempBDD = factory.one();
                if (keySetIterator.hasNext()) {
                    int variable = keySetIterator.next();
                    int value = valuableVariables.get(variable);
                    if (value == 1) {
                        tempBDD = factory.ithVar(variable);
                    }
                    else {
                        tempBDD = factory.ithVar(variable).not();
                    }
                }
                while (keySetIterator.hasNext()) {
                    int variable = keySetIterator.next();
                    int value = valuableVariables.get(variable);
                    if (value == 1) {
                        tempBDD = tempBDD.and(factory.ithVar(variable));
                    }
                    else {
                        tempBDD = tempBDD.and(factory.ithVar(variable).not());
                    }
                }
                resultBDD = resultBDD.or(tempBDD);
            }
        }
        return resultBDD;
    }
    
    private enum ElementState {
        AND,
        OR,
        XOR,
        NOT,
        AX, EX,
        AF, EF,
        AU, EU,
        AG, EG,
        OPERAND
    }
    
    private BDD translateNot(BDD operand) {
        return operand.not();
    }
    
    private BDD translateAnd(BDD leftOperand, BDD rightOperand) {
        return leftOperand.and(rightOperand);
    }
    
    private BDD translateOr(BDD leftOperand, BDD rightOperand) {
        return leftOperand.or(rightOperand);
    }
    
    private BDD translateXor(BDD leftOperand, BDD rightOperand) {
        return leftOperand.xor(rightOperand);
    }
    
    private BDD translateAx(BDD operand) {
        return translateEx(operand.not()).not();
    }
    
    private BDD translateEx(BDD operand) {
        if (operand.isZero()) {
            return operand;
        }
        BDD shiftedOperand = shiftBDD(operand);
        return backwardImage(statesTransitionsBDD.and(shiftedOperand));
    }
    
    private BDD translateAf(BDD operand) {
        BDD resultBDD = translateAx(operand).or(operand);
        BDD previousBDD = operand;
        while (!previousBDD.equals(resultBDD)) {
            previousBDD = resultBDD;
            resultBDD = translateAx(resultBDD).or(resultBDD);
        }
        return resultBDD;
    }
    
    private BDD translateEf(BDD operand) {
        BDD resultBDD = translateEx(operand).or(operand);
        BDD previousBDD = operand;
        String resultString = resultBDD.toString(), previousString = previousBDD.toString();
        while (!previousBDD.equals(resultBDD)) {
            previousBDD = resultBDD;
            resultBDD = translateEx(resultBDD).or(resultBDD);
        }
        return resultBDD;
    }
    
    private BDD translateAg(BDD operand) {
        BDD resultBDD = translateAx(operand).and(operand);
        BDD previousBDD = operand;
        while (!previousBDD.equals(resultBDD)) {
            previousBDD = resultBDD;
            resultBDD = translateAx(resultBDD).and(resultBDD);
        }
        return resultBDD;
    }
    
    private BDD translateEg(BDD operand) {
        BDD resultBDD = translateEx(operand).and(operand);
        BDD previousBDD = operand;
        while (!previousBDD.equals(resultBDD)) {
            previousBDD = resultBDD;
            resultBDD = translateEx(resultBDD).and(resultBDD);
        }
        return resultBDD;
    }
    
    private BDD translateAu(BDD leftOperand, BDD rightOperand) {
        BDD resultBDD = rightOperand.or(leftOperand.and(translateAx(rightOperand)));
        BDD previousBDD = rightOperand;
        while (!previousBDD.equals(resultBDD)) {
            previousBDD = resultBDD;
            resultBDD = rightOperand.or(leftOperand.and(translateAx(resultBDD)));
        }
        return resultBDD;
    }
    
    private BDD translateEu(BDD leftOperand, BDD rightOperand) {
        BDD resultBDD = rightOperand.or(leftOperand.and(translateEx(rightOperand)));
        BDD previousBDD = rightOperand;
        while (!previousBDD.equals(resultBDD)) {
            previousBDD = resultBDD;
            resultBDD = rightOperand.or(leftOperand.and(translateEx(resultBDD)));
        }
        return resultBDD;
    }
    
    private BDD backwardImage(BDD operand) {
        int numberOfVariables = factory.varNum();
        for (int i = numberOfVariables / 2; i < numberOfVariables; i++) {
            operand = operand.exist(factory.ithVar(i));
        }
        return operand;
    }
}

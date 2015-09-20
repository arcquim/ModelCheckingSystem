package com.system.kripkestructure;

import com.system.BDDSingleFactory;
import com.system.util.SingleLogger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 *
 * @author Alexander Erin <arcquim@gmail.com>
 */
public class KripkeStructureTranslator {
    
    private List<String> operators;
    private List<Variable> variables;
    private List<AtomicPredicate> atomicPredicates;
    private Calculator calculator;
    private int variablesNumber;
    private int doubleVariablesNumber;
    private boolean isTranslated = false;
    
    private int variablesNumberBDD;
    private int halfVariablesNumberBDD;
    private BDDFactory factory;
    private BDD statesTransitionBDD;
    private BDD startStatesBDD;
    private BDD currentTransitionBDD;
    private BDD currentStateBDD;
    private Map<Integer, List<Object>> table;
    private Integer[] statesNumbers;
    private ProgramTableBuilder tableBuilder;
    private ConditionParser conditionParser;
    private ExpressionParser expressionParser;
    
    private static final String ZERO_STRING = "0";
    private static final String ONE_STRING = "1";
    private static final String EXCLUDED = "EXCLUDED";
    private static final String UNDEFINED = "UNDEFINED";
    private static final String DEFINED = "DEFINED";
    private static final String FIXED = "FIXED";
    private static final String READ_STATE = "READ ";
    private static final String ASSIGN_STATE = "ASSIGN ";
    private static final String IF_STATE = "IF ";
    private static final String ELSE_STATE = "ELSE ";
    private static final String WHILE_STATE = "WHILE ";
    private static final String ENDS = " ENDS";
    private static final String TRUE = "true";
    private static final String FALSE = "false";
    private static final char ZERO_CHAR = '0';
    private static final char ONE_CHAR = '1';
    private static final char SEMICOLON = ';';
    private static final int PC_SIZE = 8;
    private static final int INTEGER_SIZE = 16;
    private static final int READ_SHIFT = READ_STATE.length();
    private static final int ASSIGN_SHIFT = ASSIGN_STATE.length();
    private static final int IF_SHIFT = IF_STATE.length();
    private static final int ELSE_SHIFT = ELSE_STATE.length();
    private static final int WHILE_SHIFT = WHILE_STATE.length();
    private static final int ENDS_SHIFT = ENDS.length();
    private static Logger logger;
    
    static {
        logger = SingleLogger.getLogger();
    }
    
    /**
     *
     * @param operators List of strings, where each string represents an operator 
     * of a program (<i>if</i>, <i>while</i>, <i>else</i>, open or close figured 
     * brackets are operators too).
     * @param variables List of Variables instances representing variables of 
     * given program.
     * @param atomicPredicates List of AtomicPredicates.
     */
    public KripkeStructureTranslator(List<String> operators, List<Variable> variables,
            List<AtomicPredicate> atomicPredicates) {
        if (operators == null || operators.isEmpty() || variables == null ||
                variables.isEmpty() || atomicPredicates == null ||
                atomicPredicates.isEmpty()) {
            throw new IllegalArgumentException();
        }
        this.operators = operators;
        this.variables = variables;
        this.atomicPredicates = atomicPredicates;
        variablesNumber = variables.size();
        doubleVariablesNumber = variablesNumber * 2;
        tableBuilder = new ProgramTableBuilder(variables, operators);
    }

    /**
     *
     * @return Given list of AtomicPredicates.
     */
    public List<AtomicPredicate> getAtomicPredicates() {
        if (isTranslated) {
            return atomicPredicates;
        }
        return null;
    }

    /**
     *
     * @return BDD representing start states of the Kripke structure.
     */
    public BDD getStartStatesBDD() {
        if (isTranslated) {
            return startStatesBDD;
        }
        return null;
    }

    /**
     *
     * @return BDD representing transitions between states of the Kripke structure.
     */
    public BDD getStatesTransitionBDD() {
        if (isTranslated) {
            return statesTransitionBDD;
        }
        return null;
    }
    
    /**
     *
     * @return True if translation was successful (so you can get its results 
     * via getters); false otherwise (then see logs for details).
     */
    public boolean tryTranslate() {
        tableBuilder.buildTable();
        table = tableBuilder.getStatesTable();
        if (table == null) {
            isTranslated = false;
            return isTranslated;
        }
        statesNumbers = table.keySet().toArray(new Integer[0]);
        Arrays.sort(statesNumbers);
        
        isTranslated = translateProgram();
        return isTranslated;
    }

    private void setVariablesNumberBDD() {
        variablesNumberBDD = PC_SIZE;
        List<Object> node = table.get(0);
        for (int i = 0; i < variables.size(); i++) {
            variablesNumberBDD += variables.get(i).getSize();
        }
        halfVariablesNumberBDD = variablesNumberBDD;
        variablesNumberBDD *= 2;
    }

    private void setFactory() {
        BDDSingleFactory.create(variablesNumberBDD * 900000, variablesNumberBDD * 20000);
        factory = BDDSingleFactory.getInstanse();
        factory.setVarNum(variablesNumberBDD);
    }
    
    private void setConditionParser() {
        this.conditionParser = new ConditionParser(variables);
    }
    
    private void setExpressionParser() {
        this.expressionParser = new ExpressionParser(variables);
    }
    
    private void setCalculator() {
        this.calculator = new Calculator();
    }

    private boolean translateProgram() {
        setVariablesNumberBDD();
        setFactory();
        setConditionParser();
        setExpressionParser();
        setCalculator();
        if (!prepareAtomicPredicates()) {
            return false;
        }
        int thisPC = statesNumbers[0];
        statesNumbers = null;
        List<Object> current = new ArrayList<>();
        for (int i = 0; i < variablesNumber; i++) {
            current.add(null);
        }
        setStartState(thisPC);
        statesTransitionBDD = factory.zero();
        StateType stateType;
        try {
            stateType = getNextStateType(thisPC);
        }
        catch (IllegalArgumentException ex) {
            logger.log(Level.INFO, "Cancelling translation");
            return false;
        }
        switch (stateType) {
            case ASSIGN:
                return translateAssign(current, thisPC);
            
            case ELSE:
                logger.log(Level.ERROR, "Else without if");
                logger.log(Level.INFO, "Cancelling translation");
                return false;
                
            case END_OF_GROUP:
                logger.log(Level.ERROR, "Close bracket without context");
                logger.log(Level.INFO, "Cancelling translation");
                return false;
                
            case END_OF_PROGRAM:
                logger.log(Level.ERROR, "Empty program");
                logger.log(Level.INFO, "Cancelling translation");
                return false;
                
            case IF:
                logger.log(Level.INFO, "Transalting if");
                return translateIf(current, thisPC);
                
            case READ:
                logger.log(Level.INFO, "Translating read");
                return translateRead(current, thisPC);
                
            case WHILE:
                logger.log(Level.INFO, "Translating while");
                return translateWhile(current, thisPC);
        }
        return false;
    }
    
    private boolean prepareAtomicPredicates() {
        for (int i = 0; i < atomicPredicates.size(); i++) {
            logger.log(Level.INFO, "Preparing predicate number " + i);
            if (!atomicPredicates.get(i).toPostfix(conditionParser)) {
                logger.log(Level.INFO, "Cancelling translation");
                return false;
            }
        }
        return true;
    }
    
    private void addToStatesTransitionBDD() {
        statesTransitionBDD.orWith(currentTransitionBDD);
    }
    
    private void addToPredicatesBDD(Map<Variable, String> currentValues) {
        for (int i = 0; i < atomicPredicates.size(); i++) {
            Boolean value = atomicPredicates.get(i).calculate(currentValues, 
                    calculator);
            if (value != null && value) {
                atomicPredicates.get(i).addToBDD(currentStateBDD);
            }
        }
    }
    
    private boolean translateIf(List<Object> current, int pc) {
        currentStateBDD = translateState(current, pc);
        currentTransitionBDD = translateState(current, pc);
        List<Object> transition = table.get(pc);
        String comment = (String)transition.get(doubleVariablesNumber + 2);
        int semicolonPosition = comment.lastIndexOf(SEMICOLON);
        String condition = comment.substring(IF_SHIFT, semicolonPosition);
        int thenPC = (Integer)transition.get(doubleVariablesNumber + 1);
        int elsePC = Integer.parseInt(comment.substring(semicolonPosition + ELSE_SHIFT + 1));
        Map<Variable, String> variableValues = new HashMap<>();
        for (int i = 0; i < variables.size(); i++) {
            if (current.get(i) == null) {
                continue;
            }
            variableValues.put(variables.get(i), current.get(i).toString());
        }
        String conditionValueString = calculator.calculate(variableValues, condition, 
                conditionParser.parseCondition(condition));
        if (conditionValueString == null) {
            logger.log(Level.ERROR, "Bad condition " + condition);
            return false;
        }
        boolean conditionValue;
        if (conditionValueString.equals(TRUE)) {
            conditionValue = true;
        }
        else {
            if (conditionValueString.equals(FALSE)) {
                conditionValue = false;
            }
            else {
                logger.log(Level.ERROR, "Bad condition " + condition);
                return false;
            }
        }
        if (conditionValue) {
            currentTransitionBDD.andWith(translateNextState(current, thenPC));
            addToStatesTransitionBDD();
            addToPredicatesBDD(variableValues);
            variableValues.clear();
            variableValues = null;
            StateType stateType;
            try {
                stateType = getNextStateType(thenPC);
            }
            catch (IllegalArgumentException ex) {
                logger.log(Level.INFO, "Cancelling translation");
                return false;
            }
            switch (stateType) {
                case ASSIGN:
                    logger.log(Level.INFO, "Translating assign");
                    return translateAssign(current, thenPC);

                case ELSE:
                    logger.log(Level.INFO, "Translating else");
                    return translateElse(current, thenPC);

                case END_OF_GROUP:
                    logger.log(Level.INFO, "Translating end of group");
                    return translateEndOfGroup(current, thenPC);

                case END_OF_PROGRAM:
                    logger.log(Level.INFO, "Translating end of program");
                    return translateEndOfProgram(current, thenPC);

                case IF:
                    logger.log(Level.INFO, "Transalting if");
                    return translateIf(current, thenPC);

                case READ:
                    logger.log(Level.INFO, "Translating read");
                    return translateRead(current, thenPC);

                case WHILE:
                    logger.log(Level.INFO, "Translating while");
                    return translateWhile(current, thenPC);
            }
        }
        else {
            currentTransitionBDD.andWith(translateNextState(current, elsePC));
            addToStatesTransitionBDD();
            addToPredicatesBDD(variableValues);
            variableValues.clear();
            variableValues = null;
            StateType stateType;
            try {
                stateType = getNextStateType(elsePC);
            }
            catch (IllegalArgumentException ex) {
                logger.log(Level.INFO, "Cancelling translation");
                return false;
            }
            switch (stateType) {
                case ASSIGN:
                    logger.log(Level.INFO, "Translating assign");
                    return translateAssign(current, elsePC);

                case ELSE:
                    logger.log(Level.INFO, "Translating else");
                    return translateElse(current, elsePC);

                case END_OF_GROUP:
                    logger.log(Level.INFO, "Translating end of group");
                    return translateEndOfGroup(current, elsePC);

                case END_OF_PROGRAM:
                    logger.log(Level.INFO, "Translating end of program");
                    return translateEndOfProgram(current, elsePC);

                case IF:
                    logger.log(Level.INFO, "Transalting if");
                    return translateIf(current, elsePC);

                case READ:
                    logger.log(Level.INFO, "Translating read");
                    return translateRead(current, elsePC);

                case WHILE:
                    logger.log(Level.INFO, "Translating while");
                    return translateWhile(current, elsePC);
            }
        }
        return false;
    }
    
    private boolean translateElse(List<Object> current, int pc) {
        currentStateBDD = translateState(current, pc);
        currentTransitionBDD = translateState(current, pc);
        List<Object> transition = table.get(pc);
        int nextPC = (Integer)transition.get(doubleVariablesNumber + 1);
        currentTransitionBDD.andWith(translateNextState(current, nextPC));
        addToStatesTransitionBDD();
        Map<Variable, String> variableValues = new HashMap<>();
        for (int i = 0; i < variables.size(); i++) {
            if (current.get(i) == null) {
                continue;
            }
            variableValues.put(variables.get(i), current.get(i).toString());
        }
        addToPredicatesBDD(variableValues);
        variableValues.clear();
        variableValues = null;
        
        StateType stateType;
        try {
            stateType = getNextStateType(nextPC);
        }
        catch (IllegalArgumentException ex) {
            logger.log(Level.INFO, "Cancelling translation");
            return false;
        }
        switch (stateType) {
            case ASSIGN:
                logger.log(Level.INFO, "Translating assign");
                return translateAssign(current, nextPC);
            
            case ELSE:
                logger.log(Level.INFO, "Translating else");
                return translateElse(current, nextPC);
                
            case END_OF_GROUP:
                logger.log(Level.INFO, "Translating end of group");
                return translateEndOfGroup(current, nextPC);
                
            case END_OF_PROGRAM:
                logger.log(Level.INFO, "Translating end of program");
                return translateEndOfProgram(current, nextPC);
                
            case IF:
                logger.log(Level.INFO, "Transalting if");
                return translateIf(current, nextPC);
                
            case READ:
                logger.log(Level.INFO, "Translating read");
                return translateRead(current, nextPC);
                
            case WHILE:
                logger.log(Level.INFO, "Translating while");
                return translateWhile(current, nextPC);
        }
        return false;
    }
    
    private boolean translateWhile(List<Object> current, int pc) {
        currentStateBDD = translateState(current, pc);
        currentTransitionBDD = translateState(current, pc);
        List<Object> transition = table.get(pc);
        String comment = (String)transition.get(doubleVariablesNumber + 2);
        int semicolonPosition = comment.lastIndexOf(SEMICOLON);
        String condition = comment.substring(WHILE_SHIFT, semicolonPosition);
        Integer thenPC = (Integer)transition.get(doubleVariablesNumber + 1);
        Integer elsePC = Integer.parseInt(comment.substring(semicolonPosition + ELSE_SHIFT + 1));
        Map<Variable, String> variableValues = new HashMap<>();
        for (int i = 0; i < variables.size(); i++) {
            if (current.get(i) == null) {
                continue;
            }
            variableValues.put(variables.get(i), current.get(i).toString());
        }
        String conditionValueString = calculator.calculate(variableValues, condition, 
                conditionParser.parseCondition(condition));
        if (conditionValueString == null) {
            logger.log(Level.ERROR, "Bad condition " + condition);
            return false;
        }
        boolean conditionValue;
        if (conditionValueString.equals(TRUE)) {
            conditionValue = true;
        }
        else {
            if (conditionValueString.equals(FALSE)) {
                conditionValue = false;
            }
            else {
                logger.log(Level.ERROR, "Bad condition " + condition);
                return false;
            }
        }
        if (conditionValue) {
            currentTransitionBDD.andWith(translateNextState(current, thenPC));
            addToStatesTransitionBDD();
            addToPredicatesBDD(variableValues);
            variableValues.clear();
            variableValues = null;
            StateType stateType;
            try {
                stateType = getNextStateType(thenPC);
            }
            catch (IllegalArgumentException ex) {
                logger.log(Level.INFO, "Cancelling translation");
                return false;
            }
            switch (stateType) {
                case ASSIGN:
                    logger.log(Level.INFO, "Translating assign");
                    return translateAssign(current, thenPC);

                case ELSE:
                    logger.log(Level.INFO, "Translating else");
                    return translateElse(current, thenPC);

                case END_OF_GROUP:
                    logger.log(Level.INFO, "Translating end of group");
                    return translateEndOfGroup(current, thenPC);

                case END_OF_PROGRAM:
                    logger.log(Level.INFO, "Translating end of program");
                    return translateEndOfProgram(current, thenPC);

                case IF:
                    logger.log(Level.INFO, "Transalting if");
                    return translateIf(current, thenPC);

                case READ:
                    logger.log(Level.INFO, "Translating read");
                    return translateRead(current, thenPC);

                case WHILE:
                    logger.log(Level.INFO, "Translating while");
                    return translateWhile(current, thenPC);
            }
        }
        else {
            currentTransitionBDD.andWith(translateNextState(current, elsePC));
            addToStatesTransitionBDD();
            addToPredicatesBDD(variableValues);
            variableValues.clear();
            variableValues = null;
            StateType stateType;
            try {
                stateType = getNextStateType(elsePC);
            }
            catch (IllegalArgumentException ex) {
                logger.log(Level.INFO, "Cancelling translation");
                return false;
            }
            switch (stateType) {
                case ASSIGN:
                    logger.log(Level.INFO, "Translating assign");
                    return translateAssign(current, elsePC);

                case ELSE:
                    logger.log(Level.INFO, "Translating else");
                    return translateElse(current, elsePC);

                case END_OF_GROUP:
                    logger.log(Level.INFO, "Translating end of group");
                    return translateEndOfGroup(current, elsePC);

                case END_OF_PROGRAM:
                    logger.log(Level.INFO, "Translating end of program");
                    return translateEndOfProgram(current, elsePC);

                case IF:
                    logger.log(Level.INFO, "Transalting if");
                    return translateIf(current, elsePC);

                case READ:
                    logger.log(Level.INFO, "Translating read");
                    return translateRead(current, elsePC);

                case WHILE:
                    logger.log(Level.INFO, "Translating while");
                    return translateWhile(current, elsePC);
            }
        }
        return false;
    }
    
    private boolean translateAssign(List<Object> current, int pc) {
        currentStateBDD = translateState(current, pc);
        currentTransitionBDD = translateState(current, pc);
        List<Object> nextState = table.get(pc);
        String comment = (String)nextState.get(doubleVariablesNumber + 2);
        int nextPC = (Integer)nextState.get(doubleVariablesNumber + 1);
        int variableToAssign = Integer.parseInt(comment.substring(ASSIGN_SHIFT));
        List<Object> next = new ArrayList<>(current);
        Map<Variable, String> variableValues = new HashMap<>();
        for (int i = 0; i < variables.size(); i++) {
            if (current.get(i) == null) {
                continue;
            }
            variableValues.put(variables.get(i), current.get(i).toString());
        }
        String value = calculator.calculate(variableValues, comment, 
                expressionParser.parseExpression((String)(table.get(pc).
                        get(variablesNumber + variableToAssign))));
        if (value == null) {
            logger.log(Level.ERROR, "Cancelling transaltion");
            return false;
        }
        switch (variables.get(variableToAssign).getType()) {
            case INTEGER:
                Short integerValue;
                try {
                    integerValue = Short.parseShort(value);
                }
                catch (NumberFormatException ex) {
                    logger.log(Level.ERROR, ex);
                    return false;
                }
                next.set(variableToAssign, integerValue);
                break;
        }
        currentTransitionBDD.andWith(translateNextState(next, nextPC));
        addToStatesTransitionBDD();
        addToPredicatesBDD(variableValues);
        variableValues.clear();
        variableValues = null;
        current = null;
        StateType stateType;
        try {
            stateType = getNextStateType(nextPC);
        }
        catch (IllegalArgumentException ex) {
            logger.log(Level.INFO, "Cancelling translation");
            return false;
        }
        switch (stateType) {
            case ASSIGN:
                logger.log(Level.INFO, "Translating assign");
                return translateAssign(next, nextPC);
            
            case ELSE:
                logger.log(Level.INFO, "Translating else");
                return translateElse(next, nextPC);
                
            case END_OF_GROUP:
                logger.log(Level.INFO, "Translating end of group");
                return translateEndOfGroup(next, nextPC);
                
            case END_OF_PROGRAM:
                logger.log(Level.INFO, "Translating end of program");
                return translateEndOfProgram(next, nextPC);
                
            case IF:
                logger.log(Level.INFO, "Transalting if");
                return translateIf(next, nextPC);
                
            case READ:
                logger.log(Level.INFO, "Translating read");
                return translateRead(next, nextPC);
                
            case WHILE:
                logger.log(Level.INFO, "Translating while");
                return translateWhile(next, nextPC);
        }
        return false;
    }
    
    private boolean translateRead(List<Object> current, int pc) {
        currentStateBDD = translateState(current, pc);
        currentTransitionBDD = translateState(current, pc);
        String currentForDebug = currentTransitionBDD.toString();
        List<Object> nextState = table.get(pc);
        String comment = (String)nextState.get(doubleVariablesNumber + 2);
        int nextPC = (Integer)nextState.get(doubleVariablesNumber + 1);
        int variableToRead = Integer.parseInt(comment.substring(READ_SHIFT));
        List<Object> next = new ArrayList<>(current);
        next.set(variableToRead, null);
        currentTransitionBDD.andWith(translateNextState(next, nextPC));
        addToStatesTransitionBDD();
        Map<Variable, String> variableValues = new HashMap<>();
        for (int i = 0; i < variables.size(); i++) {
            if (current.get(i) == null) {
                continue;
            }
            variableValues.put(variables.get(i), current.get(i).toString());
        }
        addToPredicatesBDD(variableValues);
        variableValues.clear();
        variableValues = null;
        current = null;
        
        Variable variable = variables.get(variableToRead);
        StateType stateType;
        try {
            stateType = getNextStateType(nextPC);
        }
        catch (IllegalArgumentException ex) {
            logger.log(Level.INFO, "Cancelling translation");
            return false;
        }
        switch (stateType) {
            case ASSIGN:
                logger.log(Level.INFO, "Translating assign");
                switch (variable.getType()) {
                    case INTEGER:
                        for (short i = Short.MIN_VALUE; i < Short.MAX_VALUE; i++) {
                            next.set(variableToRead, i);
                            if (!translateAssign(next, nextPC)) {
                                return false;
                            }
                        }
                        next.set(variableToRead, Short.MAX_VALUE);
                        return translateAssign(next, nextPC);
                }
                break;
            
            case ELSE:
                logger.log(Level.INFO, "Translating else");
                switch (variable.getType()) {
                    case INTEGER:
                        for (short i = Short.MIN_VALUE; i < Short.MAX_VALUE; i++) {
                            next.set(variableToRead, i);
                            if (!translateElse(next, nextPC)) {
                                return false;
                            }
                        }
                        next.set(variableToRead, Short.MAX_VALUE);
                        return translateElse(next, nextPC);
                }
                break;
                
            case END_OF_GROUP:
                logger.log(Level.INFO, "Translating end of group");
                switch (variable.getType()) {
                    case INTEGER:
                        for (short i = Short.MIN_VALUE; i < Short.MAX_VALUE; i++) {
                            next.set(variableToRead, i);
                            if (!translateEndOfGroup(next, nextPC)) {
                                return false;
                            }
                        }
                        next.set(variableToRead, Short.MAX_VALUE);
                        return translateEndOfGroup(next, nextPC);
                }
                break;
                
            case END_OF_PROGRAM:
                logger.log(Level.INFO, "Translating end of program");
                switch (variable.getType()) {
                    case INTEGER:
                        for (short i = Short.MIN_VALUE; i < Short.MAX_VALUE; i++) {
                            next.set(variableToRead, i);
                            if (!translateEndOfProgram(next, nextPC)) {
                                return false;
                            }
                        }
                        next.set(variableToRead, Short.MAX_VALUE);
                        return translateEndOfProgram(next, nextPC);
                }
                break;
                
            case IF:
                logger.log(Level.INFO, "Transalting if");
                switch (variable.getType()) {
                    case INTEGER:
                        for (short i = Short.MIN_VALUE; i < Short.MAX_VALUE; i++) {
                            next.set(variableToRead, i);
                            if (!translateIf(next, nextPC)) {
                                return false;
                            }
                        }
                        next.set(variableToRead, Short.MAX_VALUE);
                        return translateIf(next, nextPC);
                }
                break;
                
            case READ:
                logger.log(Level.INFO, "Translating read");
                switch (variable.getType()) {
                    case INTEGER:
                        for (short i = Short.MIN_VALUE; i < Short.MAX_VALUE; i++) {
                            next.set(variableToRead, i);
                            if (!translateRead(next, nextPC)) {
                                return false;
                            }
                        }
                        next.set(variableToRead, Short.MAX_VALUE);
                        return translateRead(next, nextPC);
                }
                break;
                
            case WHILE:
                logger.log(Level.INFO, "Translating while");
                switch (variable.getType()) {
                    case INTEGER:
                        for (short i = Short.MIN_VALUE; i < Short.MAX_VALUE; i++) {
                            next.set(variableToRead, i);
                            if (!translateWhile(next, nextPC)) {
                                return false;
                            }
                        }
                        next.set(variableToRead, Short.MAX_VALUE);
                        return translateWhile(next, nextPC);
                }
                break;
        }
        return false;
    }
    
    private boolean translateEndOfGroup(List<Object> current, int pc) {
        currentStateBDD = translateState(current, pc);
        currentTransitionBDD = translateState(current, pc);
        List<Object> transition = table.get(pc);
        String comment = (String)transition.get(doubleVariablesNumber + 2);
        Map<Variable, String> variableValues = new HashMap<>();
        for (int i = 0; i < variables.size(); i++) {
            if (current.get(i) == null) {
                continue;
            }
            variableValues.put(variables.get(i), current.get(i).toString());
        }
        if (!comment.startsWith(WHILE_STATE)) {
            Integer nextPC = (Integer)transition.get(doubleVariablesNumber + 1);
            currentTransitionBDD.andWith(translateNextState(current, nextPC));
            addToStatesTransitionBDD();
            addToPredicatesBDD(variableValues);
            variableValues.clear();
            variableValues = null;
            
            StateType stateType;
            try {
                stateType = getNextStateType(nextPC);
            }
            catch (IllegalArgumentException ex) {
                logger.log(Level.INFO, "Cancelling translation");
                return false;
            }
            switch (stateType) {
                case ASSIGN:
                    logger.log(Level.INFO, "Translating assign");
                    return translateAssign(current, nextPC);

                case ELSE:
                    logger.log(Level.INFO, "Translating else");
                    return translateElse(current, nextPC);

                case END_OF_GROUP:
                    logger.log(Level.INFO, "Translating end of group");
                    return translateEndOfGroup(current, nextPC);

                case END_OF_PROGRAM:
                    logger.log(Level.INFO, "Translating end of program");
                    return translateEndOfProgram(current, nextPC);

                case IF:
                    logger.log(Level.INFO, "Transalting if");
                    return translateIf(current, nextPC);

                case READ:
                    logger.log(Level.INFO, "Translating read");
                    return translateRead(current, nextPC);

                case WHILE:
                    logger.log(Level.INFO, "Translating while");
                    return translateWhile(current, nextPC);
            }
        }
        else {
            Integer nextPC = Integer.parseInt(comment.substring(WHILE_SHIFT, 
                    comment.length() - ENDS_SHIFT));
            currentTransitionBDD.andWith(translateNextState(current, nextPC));
            addToStatesTransitionBDD();
            addToPredicatesBDD(variableValues);
            variableValues.clear();
            variableValues = null;
            return translateWhile(current, nextPC);
        }
        return false;
    }
    
    private boolean translateEndOfProgram(List<Object> current, int pc) {
        currentStateBDD = translateState(current, pc);
        currentTransitionBDD = translateState(current, pc);
        currentTransitionBDD.andWith(translateNextState(current, pc));
        addToStatesTransitionBDD();
        Map<Variable, String> variableValues = new HashMap<>();
        for (int i = 0; i < variables.size(); i++) {
            if (current.get(i) == null) {
                continue;
            }
            variableValues.put(variables.get(i), current.get(i).toString());
        }
        addToPredicatesBDD(variableValues);
        variableValues.clear();
        variableValues = null;
        current = null;
        return true;
    }
    
    private BDD translateInteger(int startBDDVariable, int endBDDVariable, short value) {
        String binaryString = Integer.toBinaryString(0xFFFF & value);
        int binaryLength = binaryString.length();
        for (int i = INTEGER_SIZE; i > binaryLength; i--) {
            binaryString = ZERO_STRING + binaryString;
        }
        BDD resultBdd = binaryString.charAt(0) == ZERO_CHAR ? 
                factory.nithVar(startBDDVariable) : factory.ithVar(startBDDVariable);
        for (int i = startBDDVariable + 1; i < endBDDVariable; i++) {
            if (binaryString.charAt(i - startBDDVariable) == ZERO_CHAR) { 
                resultBdd.andWith(factory.nithVar(i));
            }
            else {
                resultBdd.andWith(factory.ithVar(i));
            }
        }
        return resultBdd;
    }
    
    private BDD translatePC(int startBDDVariable, int endBDDVariable, int value) {
        String binaryString = Integer.toBinaryString(value);
        int binaryLength = binaryString.length();
        for (int i = PC_SIZE; i > binaryLength; i--) {
            binaryString = ZERO_STRING + binaryString;
        }
        BDD resultBdd = binaryString.charAt(0) == ZERO_CHAR ? 
                factory.nithVar(startBDDVariable) : factory.ithVar(startBDDVariable);
        for (int i = startBDDVariable + 1; i < endBDDVariable; i++) {
            if (binaryString.charAt(i - startBDDVariable) == ZERO_CHAR) { 
                resultBdd.andWith(factory.nithVar(i));
            }
            else {
                resultBdd.andWith(factory.ithVar(i));
            }
        }
        return resultBdd;
    }
    
    private BDD translateState(List<Object> state, int pc) {
        BDD stateResult = translatePC(0, PC_SIZE, pc);
        int currentLength = state.size(), start = PC_SIZE, end = PC_SIZE;
        for (int i = 0; i < currentLength; i++) {
            Object currentVariable = state.get(i);
            if (currentVariable == null) {
                switch (variables.get(i).getType()) {
                    case INTEGER:
                        start += INTEGER_SIZE;
                        break;
                }
            }
            else {
                switch (variables.get(i).getType()) {
                    case INTEGER:
                        end = start + INTEGER_SIZE;
                        stateResult.andWith(translateInteger(start, end, 
                                (Short)currentVariable));
                        break;
                }
                start = end;
            }
        }
        return stateResult;
    }
    
    private BDD translateNextState(List<Object> state, int pc) {
        BDD stateResult = translatePC(halfVariablesNumberBDD, 
                halfVariablesNumberBDD + PC_SIZE, pc);
        int currentLength = state.size(), start = PC_SIZE + halfVariablesNumberBDD, 
                end = PC_SIZE + halfVariablesNumberBDD;
        for (int i = 0; i < currentLength; i++) {
            Object currentVariable = state.get(i);
            if (currentVariable == null) {
                switch (variables.get(i).getType()) {
                    case INTEGER:
                        start += INTEGER_SIZE;
                        break;
                }
            }
            else {
                switch (variables.get(i).getType()) {
                    case INTEGER:
                        end = start + INTEGER_SIZE;
                        stateResult.andWith(translateInteger(start, end, 
                                (Short)currentVariable));
                        break;
                }
                start = end;
            }
        }
        return stateResult;
    }
    
    private enum StateType {
        READ,
        ASSIGN,
        IF,
        ELSE,
        WHILE,
        END_OF_PROGRAM, 
        END_OF_GROUP
    }
    
    private StateType getNextStateType(int pc) {
        List<Object> nextNode = table.get(pc);
        if (nextNode == null) {
            return StateType.END_OF_PROGRAM;
        }
        String comment = (String)nextNode.get(nextNode.size() - 1);
        if (comment.startsWith(ASSIGN_STATE)) {
            return StateType.ASSIGN;
        }
        if (comment.startsWith(ELSE_STATE)) {
            if (comment.endsWith(ENDS)) {
                return StateType.END_OF_GROUP;
            }
            return StateType.ELSE;
        }
        if (comment.startsWith(IF_STATE)) {
            if (comment.endsWith(ENDS)) {
                return StateType.END_OF_GROUP;
            }
            return StateType.IF;
        }
        if (comment.startsWith(WHILE_STATE)) {
            if (comment.endsWith(ENDS)) {
                return StateType.END_OF_GROUP;
            }
            return StateType.WHILE;
        }
        if (comment.startsWith(READ_STATE)) {
            return StateType.READ;
        }
        throw new IllegalArgumentException();
    }
    
    private void setStartState(int pc) {
        startStatesBDD = translatePC(0, PC_SIZE, pc);
    }
}

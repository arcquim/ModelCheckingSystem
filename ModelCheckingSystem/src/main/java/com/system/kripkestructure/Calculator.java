package com.system.kripkestructure;

import com.system.util.SingleLogger;
import java.util.Deque;
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 *
 * @author Alexander Erin <arcquim@gmail.com>
 */
class Calculator {
    
    private Map<Variable, String> variablesValues;
    private Map<String, Variable> variablesNames;
    
    private static final String EQUAL = "==";
    private static final String GREATER = ">";
    private static final String GREATER_EQUAL = ">=";
    private static final String LESS = "<";
    private static final String LESS_EQUAL = "<=";
    private static final String NOT_EQUAL = "!=";
    private static final String NOT = "!";
    private static final String AND = "&&";
    private static final String OR = "||";
    private static final String PLUS = "+";
    private static final String MINUS = "-";
    private static final String ASTERISK = "*";
    private static final String SLASH = "/";
    private static final String UNARY_MINUS = "U-";
    private static final String TRUE = "true";
    private static final String FALSE = "false";
    private static final String INTEGER = "^\\-?\\d+$";
    
    private static Logger logger;
    
    static {
        logger = SingleLogger.getLogger();
    }
    
    public String calculate(Map<Variable, String> variablesValuesMap, 
            String infixFormula, Deque<String> postfixFormula) {
        if (postfixFormula == null || postfixFormula.isEmpty()) {
            return null;
        }
        Stack<String> result = new Stack<>();
        this.variablesValues = variablesValuesMap;
        variablesNames = new HashMap<>();
        Set<Variable> variables = variablesValues.keySet();
        for (Variable variable : variables) {
            variablesNames.put(variable.getName(), variable);
        }
        String currentElement;
        FormulaState currentState;
        String rightOperand, leftOperand, operand;
        do {
            currentElement = postfixFormula.pollFirst();
            currentState = determineState(currentElement);
            switch (currentState) {
                case BINARY_ARITHMETIC_OPERATOR:
                case BINARY_COMPARE_OPERATOR:
                    try {
                        rightOperand = result.pop();
                        leftOperand = result.pop();
                        result.push(operateBinaryNonLogic(leftOperand, rightOperand, 
                                currentElement));
                    }
                    catch (EmptyStackException | IllegalArgumentException ex) {
                        return null;
                    }
                    break;
                    
                case BINARY_LOGIC_OPERATOR:
                    try {
                        rightOperand = result.pop();
                        leftOperand = result.pop();
                        result.push(operateBinaryLogic(leftOperand, rightOperand, 
                                (String)currentElement));
                    }
                    catch (EmptyStackException | IllegalArgumentException ex) {
                        return null;
                    }
                    break;
                    
                case CONSTANT:
                    try {
                        result.push(processConstant(currentElement));
                    }
                    catch (IllegalArgumentException ex) {
                        return null;
                    }
                    break;
                    
                case UNARY_ARITHMETIC_OPERATOR:
                    try {
                        operand = result.pop();
                        result.push(operateUnaryArithmetic(operand));
                    }
                    catch (EmptyStackException | IllegalArgumentException ex) {
                        return null;
                    }
                    break;
                    
                case UNARY_LOGIC_OPERATOR:
                    try {
                        operand = result.pop();
                        result.push(operateUnaryLogic(operand));
                    }
                    catch (EmptyStackException | IllegalArgumentException ex) {
                        return null;
                    }
                    break;
                    
                case VARIABLE:
                    try {
                        result.push(processVariable(currentElement));
                    }
                    catch (IllegalArgumentException ex) {
                        return null;
                    }
                    break;
            }
        }
        while (!postfixFormula.isEmpty());
        if (result.size() != 1) {
            logger.log(Level.ERROR, "Error processing " + infixFormula);
            return null;
        }
        return result.pop();
    }

    private enum FormulaState {
        BINARY_LOGIC_OPERATOR,
        BINARY_COMPARE_OPERATOR,
        UNARY_LOGIC_OPERATOR,
        BINARY_ARITHMETIC_OPERATOR,
        UNARY_ARITHMETIC_OPERATOR,
        CONSTANT,
        VARIABLE
    }
    
    private FormulaState determineState(String element) {
        if (element.matches(INTEGER)) {
            return FormulaState.CONSTANT;
        }
        
        switch (element) {
            case AND:
            case OR:
                return FormulaState.BINARY_LOGIC_OPERATOR;
                
            case ASTERISK:
            case MINUS:
            case PLUS:
            case SLASH:
                return FormulaState.BINARY_ARITHMETIC_OPERATOR;
                
            case EQUAL:
            case GREATER:
            case GREATER_EQUAL:
            case LESS:
            case LESS_EQUAL:
            case NOT_EQUAL:
                return FormulaState.BINARY_COMPARE_OPERATOR;
                
            case NOT:
                return FormulaState.UNARY_LOGIC_OPERATOR;
                
            case UNARY_MINUS:
                return FormulaState.UNARY_ARITHMETIC_OPERATOR;
                
            default:
                return FormulaState.VARIABLE;
        }
    }
    
    private String operateBinaryNonLogic(String leftOperand, String rightOperand, String operation) {
        FormulaState leftOperandState = determineState(leftOperand);
        FormulaState rightOperandState = determineState(rightOperand);
        Variable leftVariable = null, rightVariable = null;
        if (leftOperandState != FormulaState.CONSTANT || rightOperandState != FormulaState.CONSTANT) {
            throw new IllegalArgumentException();
        }
        
        Object[] leftValueAndType = getValueAndType(leftOperand);
        if (leftValueAndType == null) {
            logger.log(Level.ERROR, "Unknown constant " + leftOperand);
            throw new IllegalArgumentException();
        }
        switch ((String) leftValueAndType[1]) {
            case "INTEGER":
                Short realLeftValue = Short.parseShort(leftOperand);
                Object[] rightValueAndType = getValueAndType(rightOperand);
                if (rightValueAndType == null) {
                    logger.log(Level.ERROR, "Unknown constant " + rightOperand);
                    throw new IllegalArgumentException();
                }
                switch ((String)rightValueAndType[1]) {
                    case "INTEGER":
                        switch (operation) {
                            case MINUS:
                                return Short.toString((short) (realLeftValue - Short.parseShort(rightOperand)));

                            case PLUS:
                                return Short.toString((short) (realLeftValue + Short.parseShort(rightOperand)));

                            case SLASH:
                                return Short.toString((short) (realLeftValue / Short.parseShort(rightOperand)));

                            case ASTERISK:
                                return Short.toString((short) (realLeftValue * Short.parseShort(rightOperand)));

                            case EQUAL:
                                return Boolean.toString(realLeftValue == Short.parseShort(rightOperand));
                                
                            case NOT_EQUAL:
                                return Boolean.toString(realLeftValue != Short.parseShort(rightOperand));
                                
                            case GREATER:
                                return Boolean.toString(realLeftValue > Short.parseShort(rightOperand));
                                
                            case GREATER_EQUAL:
                                return Boolean.toString(realLeftValue >= Short.parseShort(rightOperand));
                                
                            case LESS:
                                return Boolean.toString(realLeftValue < Short.parseShort(rightOperand));
                                
                            case LESS_EQUAL:
                                return Boolean.toString(realLeftValue <= Short.parseShort(rightOperand));
                            
                            default:
                                logger.log(Level.ERROR, "Unknown operation " + operation);
                                throw new IllegalArgumentException();
                        }
                }
        }
        logger.log(Level.ERROR, "Unknown operation or operand");
        throw new IllegalArgumentException();
    }
    
    private String operateBinaryLogic(String leftOperand, String rightOperand, String operation) {
        if (!leftOperand.equals(TRUE) && !leftOperand.equals(FALSE) || 
                !rightOperand.equals(TRUE) && !rightOperand.equals(FALSE)) {
            logger.log(Level.ERROR, "Unknown boolean value");
            throw new IllegalArgumentException();
        }
        Boolean leftValue = Boolean.parseBoolean(leftOperand), 
                rightValue = Boolean.parseBoolean(rightOperand);
        switch (operation) {
            case AND:
                return Boolean.toString(leftValue && rightValue);
                
            case OR:
                return Boolean.toString(leftValue || rightValue);
        }
        logger.log(Level.ERROR, "Unknown operation " + operation);
        throw new IllegalArgumentException();
    }

    private String processConstant(String currentElement) {
        Object[] valueAndType = getValueAndType(currentElement);
        if (valueAndType == null) {
            throw new IllegalArgumentException();
        }
        return currentElement;
    }

    private String operateUnaryArithmetic(String operand) {
        FormulaState operandState = determineState(operand);
        
        if (operandState != FormulaState.CONSTANT) {
            logger.log(Level.ERROR, "Unknown operand " + operand);
            throw new IllegalArgumentException();
        }
        Object[] valueAndType = getValueAndType(operand);
        if (valueAndType == null) {
            logger.log(Level.ERROR, "Unknown operand " + operand);
            throw new IllegalArgumentException();
        }
        switch ((String) valueAndType[1]) {
            case "INTEGER":
                return Short.toString((short)-Short.parseShort(operand));
        }
        logger.log(Level.ERROR, "Unknown operand " + operand);
        throw new IllegalArgumentException();
    }

    private String operateUnaryLogic(String operand) {
        if (operand.equals(TRUE) && operand.equals(FALSE)) {
            logger.log(Level.ERROR, "Unknown boolean value");
            throw new IllegalArgumentException();
        }
        Boolean value = Boolean.parseBoolean(operand);
        return Boolean.toString(!value);
    }

    private String processVariable(String currentElement) {
        Variable variable = variablesNames.get(currentElement);
        if (variable == null) {
            logger.log(Level.ERROR, "Unknown variable " + currentElement);
            throw new IllegalArgumentException();
        }
        String value = variablesValues.get(variable);
        if (value == null) {
            logger.log(Level.ERROR, "Unknown variable " + currentElement);
            throw new IllegalArgumentException();
        }
        return value;
    }
    
    private Object[] getValueAndType(String constant) {
        if (constant.matches(INTEGER)) {
            return new Object[] { Short.parseShort(constant), "INTEGER" };
        }
        return null;
    }
}

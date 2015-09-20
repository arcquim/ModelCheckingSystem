package com.system.temporallogic;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.EmptyStackException;
import java.util.Stack;

/**
 *
 * @author Alexander Erin <arcquim@gmail.com>
 */
class CTLFormulaParser {
    
    private static final char LEFT_BRACKET = '(';
    private static final char RIGHT_BRACKET = ')';
    private static final String OPEN_BRACKET = "(";
    private static final String CLOSE_BRACKET = ")";
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
    private static final String DIGIT = "^\\d+$";
    private static final String POSSIBLE_STRING_1 = "^[AENOX]$";
    private static final String POSSIBLE_STRING_2 = "^AN|NO|XO$";
    
    private String formula;
    private Deque<Object> result;
    private Stack<String> temp;
    
    /**
     *
     * @param formula infix CTL formula to parse
     * @throws IllegalArgumentException if formula is null or empty
     * Constructor of CTLFormulaParser class
     */
    public CTLFormulaParser(String formula) {
        if (formula == null || formula.isEmpty()) {
            throw new IllegalArgumentException();
        }
        this.formula = formula.toUpperCase();
    }
    
    /**
     * 
     * @return postfix formula
     * @throws IllegalArgumentException if infix formula is bad-written
     */
    public Deque<Object> getPostfixFormula() throws IllegalArgumentException {
        if (result == null) {
            fromInfixToPostfix();
        }
        return result;
    }
    
    private void fromInfixToPostfix() throws IllegalArgumentException {
        result = new ArrayDeque<>();
        temp = new Stack<>();
        int stringCounter = 0;
        int stringLength = formula.length();
        String currentKeyword = "";
        SingleCharacterState currentSingleState = SingleCharacterState.START_STATE;
        SingleCharacterState previousSingleState = SingleCharacterState.START_STATE;
        char currentSymbol;
        while (stringCounter < stringLength) {
            currentSymbol = formula.charAt(stringCounter);
            currentSingleState = determineState(currentSymbol);
            switch(currentSingleState) {
                case LETTER:
                    if (previousSingleState == SingleCharacterState.CLOSE_BRACKET
                            || previousSingleState == SingleCharacterState.DIGIT) {
                        throw new IllegalArgumentException();
                    }
                    currentKeyword = currentKeyword + currentSymbol;
                    break;
                    
                case DIGIT:
                    if (!currentKeyword.isEmpty() && 
                            previousSingleState != SingleCharacterState.DIGIT) {
                        throw new IllegalArgumentException();
                    }
                    currentKeyword = currentKeyword + currentSymbol;
                    break;
                    
                case SPACE:
                    if (previousSingleState != SingleCharacterState.SPACE) {
                        addElement(currentKeyword);
                        currentKeyword = "";
                    }
                    break;
                    
                case OPEN_BRACKET:
                    if (previousSingleState == SingleCharacterState.CLOSE_BRACKET
                            || previousSingleState == SingleCharacterState.DIGIT) {
                        throw new IllegalArgumentException();
                    }
                    addElement(currentKeyword);
                    addElement(OPEN_BRACKET);
                    currentKeyword = "";
                    break;
                    
                case CLOSE_BRACKET:
                    if (previousSingleState == SingleCharacterState.OPEN_BRACKET
                            || previousSingleState == SingleCharacterState.LETTER) {
                        throw new IllegalArgumentException();
                    }
                    addElement(currentKeyword);
                    addElement(CLOSE_BRACKET);
                    currentKeyword = "";
                    break;
                        
            }
            previousSingleState = currentSingleState;
            stringCounter++;
        }
        addElement(currentKeyword);
        finishResult();
    }
    
    private void addElement(String element) throws IllegalArgumentException {
        MultiCharacterState state = determineState(element);
        MultiCharacterState topStackState;
        String topStackElement;
        switch(state) {
            case NUMBER:
                result.addLast(Integer.parseInt(element));
                return;
                
            case LOGIC_UNARY_OPERATOR:
            case TEMPORAL_UNARY_OPERATOR:
            case OPEN_BRACKET:
                temp.push(element);
                return;
                
            case TEMPORAL_BINARY_OPERATOR:
                try {
                    topStackElement = temp.peek();
                }
                catch(EmptyStackException ex) {
                    temp.push(element);
                    return;
                }
                topStackState = determineState(topStackElement);
                while (topStackState == MultiCharacterState.TEMPORAL_UNARY_OPERATOR ||
                        topStackState == MultiCharacterState.LOGIC_UNARY_OPERATOR ||
                        topStackState == MultiCharacterState.TEMPORAL_BINARY_OPERATOR) {
                    result.addLast(topStackElement);
                    temp.pop();
                    try {
                        topStackElement = temp.peek();
                        topStackState = determineState(topStackElement);
                    }
                    catch(EmptyStackException ex) {
                        temp.push(element);
                        return;
                    }
                }
                temp.push(element);
                return;
                
            case LOGIC_BINARY_OPERATOR:
                try {
                    topStackElement = temp.peek();
                }
                catch(EmptyStackException ex) {
                    temp.push(element);
                    return;
                }
                topStackState = determineState(topStackElement);
                if (element.equals(AND)) {
                    while (topStackElement.equals(AND) ||
                            topStackState == MultiCharacterState.TEMPORAL_UNARY_OPERATOR ||
                            topStackState == MultiCharacterState.LOGIC_UNARY_OPERATOR ||
                            topStackState == MultiCharacterState.TEMPORAL_BINARY_OPERATOR) {
                        result.addLast(topStackElement);
                        temp.pop();
                        try {
                            topStackElement = temp.peek();
                            topStackState = determineState(topStackElement);
                        }
                        catch(EmptyStackException ex) {
                            temp.push(element);
                            return;
                        }
                    }
                }
                else {
                    while (topStackState == MultiCharacterState.TEMPORAL_UNARY_OPERATOR ||
                            topStackState == MultiCharacterState.LOGIC_UNARY_OPERATOR ||
                            topStackState == MultiCharacterState.TEMPORAL_BINARY_OPERATOR ||
                            topStackState == MultiCharacterState.LOGIC_BINARY_OPERATOR) {
                        result.addLast(topStackElement);
                        temp.pop();
                        try {
                            topStackElement = temp.peek();
                            topStackState = determineState(topStackElement);
                        }
                        catch(EmptyStackException ex) {
                            temp.push(element);
                            return;
                        }
                    }
                }
                temp.push(element);
                return;
                
            case CLOSE_BRACKET:
                try {
                    topStackElement = temp.peek();
                }
                catch(EmptyStackException ex) {
                    throw new IllegalArgumentException();
                }
                while (determineState(topStackElement) != 
                        MultiCharacterState.OPEN_BRACKET) {
                    result.addLast(topStackElement);
                    temp.pop();
                    try {
                        topStackElement = temp.peek();
                    }
                    catch(EmptyStackException ex) {
                        throw new IllegalArgumentException();
                    }
                }
                temp.pop();
                try {
                    topStackElement = temp.peek();
                }
                catch(EmptyStackException ex) {
                    return;
                }
                topStackState = determineState(topStackElement);
                if (topStackState == MultiCharacterState.LOGIC_UNARY_OPERATOR ||
                        topStackState == MultiCharacterState.TEMPORAL_UNARY_OPERATOR) {
                    result.addLast(topStackElement);
                    temp.pop();
                }
        }
    }

    private void finishResult() throws IllegalArgumentException {
        String topStackElement;
        MultiCharacterState currentState;
        while(true) {
            try {
                topStackElement = temp.pop();
                currentState = determineState(topStackElement);
                switch(currentState) {
                    case LOGIC_BINARY_OPERATOR:
                    case TEMPORAL_BINARY_OPERATOR:
                    case LOGIC_UNARY_OPERATOR:
                    case TEMPORAL_UNARY_OPERATOR:
                        result.addLast(topStackElement);
                        break;
                    
                    default:
                        throw new IllegalArgumentException();
                }
            }
            catch (EmptyStackException ex) {
                return;
            }
        }
    }
    
    private enum SingleCharacterState {
        LETTER,
        DIGIT,
        SPACE,
        OPEN_BRACKET,
        CLOSE_BRACKET,
        START_STATE
    }
    
    private SingleCharacterState determineState(char symbol) {
        if (symbol >= 'A' && symbol <= 'Z') {
            return SingleCharacterState.LETTER;
        }
        if (symbol >= '0' && symbol <= '9') {
            return SingleCharacterState.DIGIT;
        }
        if (symbol == ' ') {
            return SingleCharacterState.SPACE;
        }
        if (symbol == LEFT_BRACKET) {
            return SingleCharacterState.OPEN_BRACKET;
        }
        if (symbol == RIGHT_BRACKET) {
            return SingleCharacterState.CLOSE_BRACKET;
        }
        throw new IllegalArgumentException();
    }
    
    private enum MultiCharacterState {
        LOGIC_UNARY_OPERATOR,
        LOGIC_BINARY_OPERATOR,
        TEMPORAL_UNARY_OPERATOR,
        TEMPORAL_BINARY_OPERATOR,
        NUMBER,
        POSSIBLE_STRING,
        OPEN_BRACKET,
        CLOSE_BRACKET
    }
    
    private MultiCharacterState determineState(String currentKeyword) {
        if (currentKeyword == null) {
            throw new IllegalArgumentException();
        }
        if (currentKeyword.equals(AND) || currentKeyword.equals(OR) || 
                currentKeyword.equals(XOR)) {
            return MultiCharacterState.LOGIC_BINARY_OPERATOR;
        }
        if (currentKeyword.equals(NOT)) {
            return MultiCharacterState.LOGIC_UNARY_OPERATOR;
        }
        if (currentKeyword.equals(AX) || currentKeyword.equals(EX) ||
                currentKeyword.equals(AF) || currentKeyword.equals(EF) ||
                currentKeyword.equals(AG) || currentKeyword.equals(EG)) {
            return MultiCharacterState.TEMPORAL_UNARY_OPERATOR;
        }
        if (currentKeyword.equals(AU) || currentKeyword.equals(EU)) {
            return MultiCharacterState.TEMPORAL_BINARY_OPERATOR;
        }
        if (currentKeyword.equals(OPEN_BRACKET)) {
            return MultiCharacterState.OPEN_BRACKET;
        }
        if (currentKeyword.equals(CLOSE_BRACKET)) {
            return MultiCharacterState.CLOSE_BRACKET;
        }
        if (currentKeyword.matches(DIGIT)) {
            return MultiCharacterState.NUMBER;
        }
        if (currentKeyword.matches(POSSIBLE_STRING_1) || 
                currentKeyword.matches(POSSIBLE_STRING_2) || 
                currentKeyword.isEmpty()) {
            return MultiCharacterState.POSSIBLE_STRING;
        }
        throw new IllegalArgumentException();
    }
}
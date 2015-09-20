package com.system.kripkestructure;

import com.system.util.SingleLogger;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.EmptyStackException;
import java.util.List;
import java.util.Stack;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 *
 * @author Alexander Erin <arcquim@gmail.com>
 */
class ConditionParser {
    
    private List<String> variablesNames;
    private static Logger logger;
    
    private static final char OPEN_BRACKET_CHAR = '(';
    private static final char CLOSE_BRACKET_CHAR = ')';
    private static final char EQUAL_CHAR = '=';
    private static final char GREATER_CHAR = '>';
    private static final char LESS_CHAR = '<';
    private static final char EXCLAMATION_CHAR = '!';
    private static final char ASTERISK_CHAR = '*';
    private static final char SLASH_CHAR = '/';
    private static final char MINUS_CHAR = '-';
    private static final char PLUS_CHAR = '+';
    private static final char VERTICAL_SLASH_CHAR = '|';
    private static final char AMPERSAND_CHAR = '&';
    private static final String OPEN_BRACKET = "(";
    private static final String CLOSE_BRACKET = ")";
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
    private static final String UNARY = "U";
    private static final String DIGIT = "^\\-?\\d+$";
    private static final String WHITESPACE = "\\s";
    private static final String LETTER = "\\w";
    
    private Deque<String> result;
    private Stack<String> temp;
    
    static {
        logger = SingleLogger.getLogger();
    }
    
    public ConditionParser(List<Variable> variables) {
        variablesNames = new ArrayList<>();
        for (int i = 0; i < variables.size(); i++) {
            variablesNames.add(variables.get(i).getName());
        }
    }
    
    public Deque<String> parseCondition(String condition) {
        if (condition == null || condition.isEmpty()) {
            logger.log(Level.ERROR, "Empty condition");
            return null;
        }
        result = new ArrayDeque<>();
        temp = new Stack<>();
        CharacterState currentState;
        CharacterState previousState = CharacterState.START;
        CharacterState lastNonSpaceState = CharacterState.START;
        String currentString = "";
        char currentSymbol;
        int counter = 0, length = condition.length();
        do {
            currentSymbol = condition.charAt(counter);
            currentState = determineState(currentSymbol);
            switch (currentState) {
                case AMPERSAND:
                    if (previousState != CharacterState.SPACE && 
                            previousState != CharacterState.AMPERSAND &&
                            previousState != CharacterState.CLOSE_BRACKET &&
                            previousState != CharacterState.DIGIT &&
                            previousState != CharacterState.LETTER) {
                        logger.log(Level.ERROR, "Error near " + counter + " symbol");
                        return null;
                    }
                    if (previousState != CharacterState.AMPERSAND) {
                        if (!addElement(currentString)) {
                            return null;
                        }
                        currentString = "" + currentSymbol;
                    }
                    else {
                        currentString += currentSymbol;
                        if (!addElement(currentString)) {
                            return null;
                        }
                        currentString = "";
                    }
                    break;
                    
                case ASTERISK:
                    if (previousState != CharacterState.SPACE && 
                            previousState != CharacterState.CLOSE_BRACKET &&
                            previousState != CharacterState.LETTER &&
                            previousState != CharacterState.DIGIT) {
                        logger.log(Level.ERROR, "Error near " + counter + " symbol");
                        return null;
                    }
                    if (!addElement(currentString)) {
                            return null;
                        }
                    if (!addElement(ASTERISK)) {
                            return null;
                        }
                    currentString = "";
                    break;
                    
                case CLOSE_BRACKET:
                    if (previousState != CharacterState.SPACE &&
                            previousState != CharacterState.DIGIT &&
                            previousState != CharacterState.LETTER) {
                        logger.log(Level.ERROR, "Error near " + counter + " symbol");
                        return null;
                    }
                    if (!addElement(currentString)) {
                            return null;
                        }
                    if (!addElement(CLOSE_BRACKET)) {
                            return null;
                        }
                    currentString = "";
                    break;
                    
                case DIGIT:
                case LETTER:
                    if (previousState == CharacterState.CLOSE_BRACKET ||
                            previousState == CharacterState.EXCLAMATION) {
                        logger.log(Level.ERROR, "Error near " + counter + " symbol");
                        return null;
                    }
                    if (previousState != CharacterState.DIGIT &&
                            previousState != CharacterState.LETTER) {
                        if (!addElement(currentString)) {
                            return null;
                        }
                        currentString = "";
                    }
                    currentString += currentSymbol;
                    break;
                    
                case EQUAL:
                    if (previousState == CharacterState.AMPERSAND ||
                            previousState == CharacterState.ASTERISK || 
                            previousState == CharacterState.MINUS || 
                            previousState == CharacterState.OPEN_BRACKET ||
                            previousState == CharacterState.PLUS || 
                            previousState == CharacterState.SLASH ||
                            previousState == CharacterState.VERTICAL_SLASH) {
                        logger.log(Level.ERROR, "Error near " + counter + " symbol");
                        return null;
                    }
                    if (previousState != CharacterState.EQUAL &&
                            previousState != CharacterState.EXCLAMATION &&
                            previousState != CharacterState.GREATER &&
                            previousState != CharacterState.LESS) {
                        if (!addElement(currentString)) {
                            return null;
                        }
                        currentString = "" + currentSymbol;
                    }
                    else {
                        currentString += currentSymbol;
                        if (!addElement(currentString)) {
                            return null;
                        }
                        currentString = "";
                    }
                    break;
                    
                case EXCLAMATION:
                    if (lastNonSpaceState == CharacterState.AMPERSAND || 
                            lastNonSpaceState == CharacterState.EXCLAMATION ||
                            lastNonSpaceState == CharacterState.START ||
                            lastNonSpaceState == CharacterState.VERTICAL_SLASH) {
                        if (!addElement(currentString)) {
                            return null;
                        }
                        if (!addElement(NOT)) {
                            return null;
                        }
                        currentString = "";
                    }
                    else {
                        if (previousState != CharacterState.DIGIT &&
                                previousState != CharacterState.CLOSE_BRACKET &&
                                previousState != CharacterState.LETTER &&
                                previousState != CharacterState.SPACE) {
                            logger.log(Level.ERROR, "Error near " + counter + " symbol");
                            return null;
                        }
                        else {
                            if (!addElement(currentString)) {
                                return null;
                            }
                            currentString = "" + currentSymbol;
                        }
                    }
                    break;
                    
                case GREATER:
                case LESS:
                    if (previousState != CharacterState.CLOSE_BRACKET &&
                            previousState != CharacterState.DIGIT &&
                            previousState != CharacterState.LETTER &&
                            previousState != CharacterState.SPACE) {
                        logger.log(Level.ERROR, "Error near " + counter + " symbol");
                        return null;
                    }
                    if (!addElement(currentString)) {
                        return null;
                    }
                    currentString = "" + currentSymbol;
                    break;
                    
                case MINUS:
                    if (lastNonSpaceState == CharacterState.AMPERSAND ||
                            lastNonSpaceState == CharacterState.ASTERISK ||
                            lastNonSpaceState == CharacterState.EQUAL ||
                            lastNonSpaceState == CharacterState.GREATER ||
                            lastNonSpaceState == CharacterState.LESS ||
                            lastNonSpaceState == CharacterState.MINUS ||
                            lastNonSpaceState == CharacterState.OPEN_BRACKET ||
                            lastNonSpaceState == CharacterState.PLUS || 
                            lastNonSpaceState == CharacterState.SLASH ||
                            lastNonSpaceState == CharacterState.START ||
                            lastNonSpaceState == CharacterState.VERTICAL_SLASH) {
                        if (!addElement(currentString)) {
                            return null;
                        }
                        if (!addElement(UNARY + MINUS)) {
                            return null;
                        }
                        currentString = "";
                    }
                    else {
                        if (previousState != CharacterState.SPACE && 
                            previousState != CharacterState.CLOSE_BRACKET &&
                            previousState != CharacterState.LETTER &&
                            previousState != CharacterState.DIGIT) {
                                logger.log(Level.ERROR, "Error near " + counter + " symbol");
                                return null;
                            }
                            if (!addElement(currentString)) {
                                return null;
                            }
                            if (!addElement(MINUS)) {
                                return null;
                            }
                            currentString = "";
                            break;
                    }
                    break;
                    
                case OPEN_BRACKET:
                    if (previousState == CharacterState.CLOSE_BRACKET ||
                            previousState == CharacterState.DIGIT ||
                            previousState == CharacterState.LETTER) {
                        logger.log(Level.ERROR, "Error near " + counter + " symbol");
                        return null;
                    }
                    if (!addElement(currentString)) {
                        return null;
                    }
                    if (!addElement(OPEN_BRACKET)) {
                        return null;
                    }
                    currentString = "";
                    break;
                    
                case PLUS:
                    if (previousState != CharacterState.CLOSE_BRACKET &&
                            previousState != CharacterState.DIGIT &&
                            previousState != CharacterState.LETTER &&
                            previousState != CharacterState.SPACE) {
                        logger.log(Level.ERROR, "Error near " + counter + " symbol");
                        return null;
                    }
                    if (!addElement(currentString)) {
                        return null;
                    }
                    if (!addElement(PLUS)) {
                        return null;
                    }
                    currentString = "";
                    break;
                    
                case SLASH:
                    if (previousState != CharacterState.CLOSE_BRACKET &&
                            previousState != CharacterState.DIGIT &&
                            previousState != CharacterState.LETTER &&
                            previousState != CharacterState.SPACE) {
                        logger.log(Level.ERROR, "Error near " + counter + " symbol");
                        return null;
                    }
                    if (!addElement(currentString)) {
                        return null;
                    }
                    if (!addElement(SLASH)) {
                        return null;
                    }
                    currentString = "";
                    break;
                    
                case SPACE:
                    if (previousState != CharacterState.SPACE) {
                        if (!addElement(currentString)) {
                            return null;
                        }
                    }
                    currentString = "";
                    break;
                    
                case VERTICAL_SLASH:
                    if (previousState != CharacterState.SPACE && 
                            previousState != CharacterState.VERTICAL_SLASH &&
                            previousState != CharacterState.CLOSE_BRACKET &&
                            previousState != CharacterState.DIGIT &&
                            previousState != CharacterState.LETTER) {
                        logger.log(Level.ERROR, "Error near " + counter + " symbol");
                        return null;
                    }
                    if (previousState != CharacterState.VERTICAL_SLASH) {
                        if (!addElement(currentString)) {
                            return null;
                        }
                        currentString = "" + currentSymbol;
                    }
                    else {
                        currentString += currentSymbol;
                        if (!addElement(currentString)) {
                            return null;
                        }
                        currentString = "";
                    }
                    break;
            }
            previousState = currentState;
            if (currentState != CharacterState.SPACE) {
                lastNonSpaceState = currentState;
            }
        }
        while (++counter < length);
        if (!addElement(currentString)) {
            return null;
        }
        try {
            finishResult();
        }
        catch (IllegalArgumentException ex) {
            logger.log(Level.ERROR, "Brackets are not conformed");
            return null;
        }
        return result;
    }
    
    private void finishResult() throws IllegalArgumentException {
        String topStackElement;
        ConditionState currentState;
        while(true) {
            try {
                topStackElement = temp.pop();
                currentState = determineState(topStackElement);
                switch(currentState) {
                    case CLOSE_BRACKET:
                    case CONSTANT:
                    case OPEN_BRACKET:
                    case VARIABLE:
                        throw new IllegalArgumentException();
                    
                    default:
                        result.addLast(topStackElement);
                        break;
                }
            }
            catch (EmptyStackException ex) {
                return;
            }
        }
    }
    
    private boolean addElement(String element) {
        if (element == null || element.isEmpty()) {
            return true;
        }
        ConditionState state, topStackState;
        String topStackElement;
        try {
            state = determineState(element);
        }
        catch (IllegalArgumentException ex) {
            logger.log(Level.ERROR, "Illegal element: " + element);
            return false;
        }
        switch (state) {
            case CONSTANT:
                result.addLast(element);
                break;
                
            case VARIABLE:
                result.addLast(element);
                break;
                
            case NOT:
            case OPEN_BRACKET:
            case UNARY_MINUS:
                temp.push(element);
                break;
                
            case CLOSE_BRACKET:
                try {
                    topStackElement = temp.peek();
                }
                catch(EmptyStackException ex) {
                    logger.log(Level.INFO, "Error in expression");
                    return false;
                }
                while (determineState(topStackElement) != 
                        ConditionState.OPEN_BRACKET) {
                    result.addLast(topStackElement);
                    temp.pop();
                    try {
                        topStackElement = temp.peek();
                    }
                    catch(EmptyStackException ex) {
                        logger.log(Level.INFO, "Error in expression");
                        return false;
                    }
                }
                temp.pop();
                try {
                    topStackElement = temp.peek();
                }
                catch(EmptyStackException ex) {
                    return true;
                }
                topStackState = determineState(topStackElement);
                if (topStackState == ConditionState.NOT ||
                        topStackState == ConditionState.UNARY_MINUS) {
                    result.addLast(topStackElement);
                    temp.pop();
                }
                break;
                
            case EQUAL:
            case GREATER:
            case GREATER_EQUAL:
            case LESS:
            case LESS_EQUAL:
            case NOT_EQUAL:    
                try {
                    topStackElement = temp.peek();
                }
                catch(EmptyStackException ex) {
                    temp.push(element);
                    return true;
                }
                topStackState = determineState(topStackElement);
                while (topStackState == ConditionState.ASTERISK ||
                        topStackState == ConditionState.EQUAL || 
                        topStackState == ConditionState.GREATER ||
                        topStackState == ConditionState.GREATER_EQUAL ||
                        topStackState == ConditionState.LESS ||
                        topStackState == ConditionState.LESS_EQUAL ||
                        topStackState == ConditionState.MINUS ||
                        topStackState == ConditionState.NOT_EQUAL ||
                        topStackState == ConditionState.PLUS || 
                        topStackState == ConditionState.SLASH || 
                        topStackState == ConditionState.UNARY_MINUS) {
                    result.addLast(topStackElement);
                    temp.pop();
                    try {
                        topStackElement = temp.peek();
                        topStackState = determineState(topStackElement);
                    }
                    catch(EmptyStackException ex) {
                        temp.push(element);
                        return true;
                    }
                }
                temp.push(element);
                break;
            
            case ASTERISK:
            case SLASH:
                try {
                    topStackElement = temp.peek();
                }
                catch(EmptyStackException ex) {
                    temp.push(element);
                    return true;
                }
                topStackState = determineState(topStackElement);
                while (topStackState == ConditionState.ASTERISK ||
                        topStackState == ConditionState.SLASH || 
                        topStackState == ConditionState.UNARY_MINUS) {
                    result.addLast(topStackElement);
                    temp.pop();
                    try {
                        topStackElement = temp.peek();
                        topStackState = determineState(topStackElement);
                    }
                    catch(EmptyStackException ex) {
                        temp.push(element);
                        return true;
                    }
                }
                temp.push(element);
                break;
            
            case MINUS:
            case PLUS:
                try {
                    topStackElement = temp.peek();
                }
                catch(EmptyStackException ex) {
                    temp.push(element);
                    return true;
                }
                topStackState = determineState(topStackElement);
                while (topStackState == ConditionState.ASTERISK ||
                        topStackState == ConditionState.MINUS ||
                        topStackState == ConditionState.PLUS || 
                        topStackState == ConditionState.SLASH || 
                        topStackState == ConditionState.UNARY_MINUS) {
                    result.addLast(topStackElement);
                    temp.pop();
                    try {
                        topStackElement = temp.peek();
                        topStackState = determineState(topStackElement);
                    }
                    catch(EmptyStackException ex) {
                        temp.push(element);
                        return true;
                    }
                }
                temp.push(element);
                break;
            
            case AND:
                try {
                    topStackElement = temp.peek();
                }
                catch(EmptyStackException ex) {
                    temp.push(element);
                    return true;
                }
                topStackState = determineState(topStackElement);
                while (topStackState == ConditionState.ASTERISK ||
                        topStackState == ConditionState.EQUAL || 
                        topStackState == ConditionState.GREATER ||
                        topStackState == ConditionState.GREATER_EQUAL ||
                        topStackState == ConditionState.LESS ||
                        topStackState == ConditionState.LESS_EQUAL ||
                        topStackState == ConditionState.MINUS ||
                        topStackState == ConditionState.NOT_EQUAL ||
                        topStackState == ConditionState.PLUS || 
                        topStackState == ConditionState.SLASH || 
                        topStackState == ConditionState.UNARY_MINUS ||
                        topStackState == ConditionState.AND ||
                        topStackState == ConditionState.NOT) {
                    result.addLast(topStackElement);
                    temp.pop();
                    try {
                        topStackElement = temp.peek();
                        topStackState = determineState(topStackElement);
                    }
                    catch(EmptyStackException ex) {
                        temp.push(element);
                        return true;
                    }
                }
                temp.push(element);
                break;
            
            case OR:
                try {
                    topStackElement = temp.peek();
                }
                catch(EmptyStackException ex) {
                    temp.push(element);
                    return true;
                }
                topStackState = determineState(topStackElement);
                while (topStackState == ConditionState.ASTERISK ||
                        topStackState == ConditionState.EQUAL || 
                        topStackState == ConditionState.GREATER ||
                        topStackState == ConditionState.GREATER_EQUAL ||
                        topStackState == ConditionState.LESS ||
                        topStackState == ConditionState.LESS_EQUAL ||
                        topStackState == ConditionState.MINUS ||
                        topStackState == ConditionState.NOT_EQUAL ||
                        topStackState == ConditionState.PLUS || 
                        topStackState == ConditionState.SLASH || 
                        topStackState == ConditionState.UNARY_MINUS ||
                        topStackState == ConditionState.AND ||
                        topStackState == ConditionState.NOT ||
                        topStackState == ConditionState.OR) {
                    result.addLast(topStackElement);
                    temp.pop();
                    try {
                        topStackElement = temp.peek();
                        topStackState = determineState(topStackElement);
                    }
                    catch(EmptyStackException ex) {
                        temp.push(element);
                        return true;
                    }
                }
                temp.push(element);
                break;
        }
        return true;
    }
    
    private enum ConditionState {
        OPEN_BRACKET,
        CLOSE_BRACKET,
        EQUAL,
        GREATER,
        GREATER_EQUAL,
        LESS,
        LESS_EQUAL,
        NOT_EQUAL,
        NOT,
        AND,
        OR,
        PLUS,
        MINUS,
        ASTERISK,
        SLASH,
        UNARY_MINUS,
        CONSTANT,
        VARIABLE
    }
    
    private ConditionState determineState(String element) {
        if (element == null) {
            throw new IllegalArgumentException();
        }
        if (element.equals(UNARY + MINUS)) {
            return ConditionState.UNARY_MINUS;
        }
        if (element.equals(OPEN_BRACKET)) {
            return ConditionState.OPEN_BRACKET;
        }
        if (element.equals(CLOSE_BRACKET)) {
            return ConditionState.CLOSE_BRACKET;
        }
        if (element.equals(EQUAL)) {
            return ConditionState.EQUAL;
        }
        if (element.equals(GREATER)) {
            return ConditionState.GREATER;
        }
        if (element.equals(GREATER_EQUAL)) {
            return ConditionState.GREATER_EQUAL;
        }
        if (element.equals(LESS)) {
            return ConditionState.LESS;
        }
        if (element.equals(LESS_EQUAL)) {
            return ConditionState.LESS_EQUAL;
        }
        if (element.equals(NOT_EQUAL)) {
            return ConditionState.NOT_EQUAL;
        }
        if (element.equals(NOT)) {
            return ConditionState.NOT;
        }
        if (element.equals(AND)) {
            return ConditionState.AND;
        }
        if (element.equals(OR)) {
            return ConditionState.OR;
        }
        if (element.equals(PLUS)) {
            return ConditionState.PLUS;
        }
        if (element.equals(MINUS)) {
            return ConditionState.MINUS;
        }
        if (element.equals(ASTERISK)) {
            return ConditionState.ASTERISK;
        }
        if (element.equals(SLASH)) {
            return ConditionState.SLASH;
        }
        if (element.matches(DIGIT)) {
            return ConditionState.CONSTANT;
        }
        if (variablesNames.contains(element)) {
            return ConditionState.VARIABLE;
        }
        throw new IllegalArgumentException();
    }
    
    private enum CharacterState {
        SPACE,
        DIGIT,
        LETTER,
        EQUAL,
        EXCLAMATION,
        GREATER,
        LESS,
        ASTERISK,
        PLUS,
        MINUS,
        SLASH,
        START,
        AMPERSAND,
        VERTICAL_SLASH, 
        OPEN_BRACKET,
        CLOSE_BRACKET
    }
    
    private CharacterState determineState(char element) {
        switch(element) {
            case AMPERSAND_CHAR:
                return CharacterState.AMPERSAND;
                
            case ASTERISK_CHAR:
                return CharacterState.ASTERISK;
                
            case CLOSE_BRACKET_CHAR:
                return CharacterState.CLOSE_BRACKET;
                
            case EQUAL_CHAR:
                return CharacterState.EQUAL;
                
            case EXCLAMATION_CHAR:
                return CharacterState.EXCLAMATION;
                
            case GREATER_CHAR:
                return CharacterState.GREATER;
                
            case LESS_CHAR:
                return CharacterState.LESS;
                
            case MINUS_CHAR:
                return CharacterState.MINUS;
                
            case OPEN_BRACKET_CHAR:
                return CharacterState.OPEN_BRACKET;
                
            case PLUS_CHAR:
                return CharacterState.PLUS;
                
            case SLASH_CHAR:
                return CharacterState.SLASH;
                
            case VERTICAL_SLASH_CHAR:
                return CharacterState.VERTICAL_SLASH;
        }
        if (element >= '0' && element <= '9') {
            return CharacterState.DIGIT;
        }
        if (new Character(element).toString().matches(WHITESPACE)) {
            return CharacterState.SPACE;
        }
        if (new Character(element).toString().matches(LETTER)) {
            return CharacterState.LETTER;
        }
        throw new IllegalArgumentException();
    }
}

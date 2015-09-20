package com.system;

import com.system.kripkestructure.Variable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Alexander Erin <arcquim@gmail.com>
 */
class BDDStringParser {
    
    private final List<Variable> variables;
    private List<String> result;
    private int maxExamples;
    private int numberOfBDDVariables;
    private final int numberOfVariables;
    
    private static final int MAX_CAPACITY = 1000;
    private static final int BASE = 2;
    private static final char OPEN_BRACKET = '<';
    private static final char CLOSE_BRACKET = '>';
    private static final char COLON = ':';
    private static final char SPACE = ' ';
    private static final char COMMA = ',';
    private static final char ASTERISK = '*';
    private static final char ZERO = '0';
    private static final char ONE = '1';
    private static final String EQUATION = "=";
    private static final String COMMA_SPACE = ", ";
    
    /**
     *
     * @param variables List of program variables.
     * Constructs a BDDStringParser object.
     */
    public BDDStringParser(List<Variable> variables) {
        this.variables = variables;
        numberOfBDDVariables = 0;
        for (Variable item : variables) {
            numberOfBDDVariables += item.getSize();
        }
        numberOfVariables = variables.size();
    }
    
    /**
     *
     * @param BDDString String representing path to ONE node in BDD. Examples 
     * of BDDString: "<>" (constant ONE BDD), <0:1, 3:1> (BDD reaches ONE node 
     * on values 1 of variables number 0 and number 3; other variables don't 
     * affect the result of boolean function), <0:1, 1:0, 2:0><0:1, 1:1> 
     * (there are only these paths to reach ONE node)
     * @param numberOfExamples Number of examples required. Maximum number of 
     * exampes giving to invoking code is 1000 now; -1 is equivalent to maximum 
     * number of variables.
     * @return List of examples, which size is less or equals numberOfExamples.
     * @throws IllegalArgumentException
     */
    public List<String> parse(String BDDString, int numberOfExamples) 
            throws IllegalArgumentException {
        if (numberOfExamples == -1 || numberOfExamples > MAX_CAPACITY) {
            maxExamples = MAX_CAPACITY;
        }
        else {
            maxExamples = numberOfExamples;
        }
        result = new LinkedList<>();
        int examplesCounter = 1;
        int stringCounter = 0;
        int length = BDDString.length();
        States previousState;
        States currentState = States.SPACE;
        States lastSeparator = States.CLOSE_BRACKET;
        int previousBDDVariable = -1;
        int currentBDDVariable = 0;
        String variablesExample = "";
        do {
            char currentSymbol = BDDString.charAt(stringCounter);
            previousState = currentState;
            currentState = determineState(currentSymbol);
            switch(currentState) {
                case OPEN_BRACKET:
                    currentBDDVariable = 0;
                    previousBDDVariable = 0;
                    variablesExample = "";
                    lastSeparator = States.OPEN_BRACKET;
                    break;
                    
                case SPACE:
                    break;
                    
                case CLOSE_BRACKET:
                    if (lastSeparator == States.OPEN_BRACKET) {
                        variablesExample = produceAsterisks(numberOfBDDVariables);
                        addMaximumOfExamples(variablesExample);
                        return result;
                    }
                    if (lastSeparator != States.COLON && previousState != States.DIGIT) {
                        throw new IllegalArgumentException();
                    }
                    variablesExample += produceAsterisks(numberOfBDDVariables - currentBDDVariable - 1);
                    addMaximumOfExamples(variablesExample);
                    examplesCounter = result.size();
                    break;
                    
                case COLON:
                    if (lastSeparator != States.OPEN_BRACKET && lastSeparator != States.COMMA
                            || currentBDDVariable >= numberOfBDDVariables 
                            || (currentBDDVariable <= previousBDDVariable
                            && lastSeparator != States.OPEN_BRACKET)) {
                        throw new IllegalArgumentException();
                    }
                    lastSeparator = States.COLON;
                    break;
                
                case DIGIT:
                    if (lastSeparator != States.OPEN_BRACKET && lastSeparator != States.COMMA
                            && lastSeparator != States.COLON) {
                        throw new IllegalArgumentException();
                    }
                    if (lastSeparator != States.COLON) {
                        if (previousState != States.DIGIT) {
                            previousBDDVariable = currentBDDVariable;
                            currentBDDVariable = currentSymbol - ZERO;
                        }
                        else {
                            currentBDDVariable = currentBDDVariable * 10 + currentSymbol - ZERO;
                        }
                    }
                    else {
                        if (currentSymbol != ZERO && currentSymbol != ONE || previousState != States.COLON) {
                            throw new IllegalArgumentException();
                        }
                        if (currentBDDVariable - previousBDDVariable <= 1) {
                            variablesExample += currentSymbol;
                        }
                        else {
                            variablesExample += produceAsterisks(currentBDDVariable - previousBDDVariable - 1);
                            variablesExample += currentSymbol;
                        }
                    }
                    break;
                    
                case COMMA:
                    lastSeparator = States.COMMA;
                    break;
            }
        }
        while (++stringCounter < length && examplesCounter <= maxExamples);
        return result;
    }
    
    private enum States {
        OPEN_BRACKET,
        CLOSE_BRACKET,
        DIGIT,
        COLON,
        SPACE,
        COMMA
    }
    
    private boolean isDigit(char symbol) {
        return symbol >= '0' && symbol <= '9';
    }
    
    private States determineState(char symbol) {
        switch(symbol) {
                case OPEN_BRACKET:
                    return States.OPEN_BRACKET;
                    
                case SPACE:
                    return States.SPACE;
                    
                case COMMA:
                    return States.COMMA;
                    
                case CLOSE_BRACKET:
                    return States.CLOSE_BRACKET;
                    
                case COLON:
                    return States.COLON;
                    
                default:
                    if (isDigit(symbol)) {
                        return States.DIGIT;
                    }
                    throw new IllegalArgumentException();
            }
    }
    
    private void addMaximumOfExamples(String variablesExample) {
        int maximumNumberOfExamples = maxExamples - result.size();
        if(maximumNumberOfExamples <= 0) {
            return;
        }
        int length = variablesExample.length();
        List<Integer> nonDigitPositions = new ArrayList<>();
        char[] exampleTemplate = variablesExample.toCharArray();
        for(int i = 0; i < length; i++) {
            if(exampleTemplate[i] == ASTERISK) {
                nonDigitPositions.add(i);
            }
        }
        int numberOfPositions = nonDigitPositions.size();
        if (numberOfPositions == 0) {
            result.add(buildExample(variablesExample));
            return;
        }
        char[] combination = new char[numberOfPositions];
        for (int i = 0; i < numberOfPositions; i++) {
            combination[i] = ZERO;
        }
        int examplesCounter = 0;
        while(examplesCounter < maximumNumberOfExamples) {
            for (int i = 0; i < numberOfPositions; i++) {
                exampleTemplate[nonDigitPositions.get(i)] = combination[i];
            }
            result.add(buildExample(new String(exampleTemplate)));
            examplesCounter++;
            int i = numberOfPositions - 1;
            while(i >= 0 && combination[i] != ZERO) {
                combination[i--] = ZERO;
            }
            if (i == -1) {
                return;
            }
            combination[i] = ONE;
        }
    }
    
    private String buildExample(String exampleBillet) {
        String example = "";
        int variableStart = 0;
        for(Variable item : variables) {
            example += item.getName() + EQUATION;
            String value = exampleBillet.substring(variableStart, 
                    variableStart + item.getSize());
            variableStart += item.getSize();
            example += getDecimalNumber(value, item) + COMMA_SPACE;
        }
        return example.substring(0, example.length() - 2);
    }
    
    private String produceAsterisks(int numberOfAsterisks) {
        char[] asteriskArray = new char[numberOfAsterisks];
        for (int i = 0; i < asteriskArray.length; i++) {
            asteriskArray[i] = ASTERISK;
        }
        return String.copyValueOf(asteriskArray);
    }
    
    private String getDecimalNumber(String binaryNumber, Variable variable) {
        switch(variable.getType()) {
            case INTEGER:
                if (binaryNumber.charAt(0) == ONE) {
                    if (binaryNumber.matches("10+")) {
                        Short decimalInteger = Short.MIN_VALUE;
                        return decimalInteger.toString();
                    }
                    char[] charBinaryNumber = binaryNumber.toCharArray();
                    int i = variable.getSize() - 1;
                    while (i >= 0 && charBinaryNumber[i] != ONE) {
                        charBinaryNumber[i--] = ONE;
                    }
                    charBinaryNumber[i] = ZERO;
                    for (i = 0; i < charBinaryNumber.length; i++) {
                        if (charBinaryNumber[i] == ZERO) {
                            charBinaryNumber[i] = ONE;
                        }
                        else {
                            charBinaryNumber[i] = ZERO;
                        }
                    }
                    Short decimalInteger = (short)-Short.parseShort(new String(charBinaryNumber), BASE);
                    return decimalInteger.toString();
                }
                else {
                    Short decimalInteger = Short.parseShort(binaryNumber, BASE);
                    return decimalInteger.toString();
                }
            
            case PC:
                if (binaryNumber.charAt(0) == ONE) {
                    if (binaryNumber.matches("10+")) {
                        Byte decimalPC = Byte.MIN_VALUE;
                        return decimalPC.toString();
                    }
                    char[] charBinaryNumber = binaryNumber.toCharArray();
                    int i = variable.getSize() - 1;
                    while (i >= 0 && charBinaryNumber[i] != ONE) {
                        charBinaryNumber[i--] = ONE;
                    }
                    charBinaryNumber[i] = ZERO;
                    for (i = 0; i < charBinaryNumber.length; i++) {
                        if (charBinaryNumber[i] == ZERO) {
                            charBinaryNumber[i] = ONE;
                        }
                        else {
                            charBinaryNumber[i] = ZERO;
                        }
                    }
                    Byte decimalPC = (byte)-Byte.parseByte(new String(charBinaryNumber), BASE);
                    return decimalPC.toString();
                }
                else {
                    Byte decimalPC = Byte.parseByte(binaryNumber, BASE);
                    return decimalPC.toString();
                }
            
            
            default:
                return null;
        }
    }
}

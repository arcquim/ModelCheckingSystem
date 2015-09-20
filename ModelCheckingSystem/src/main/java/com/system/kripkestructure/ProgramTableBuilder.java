package com.system.kripkestructure;

import com.system.util.SingleLogger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 *
 * @author Alexander Erin <arcquim@gmail.com>
 */
class ProgramTableBuilder {
    
    private final List<Variable> variables;
    private List<String> variablesNames;
    private final List<String> operators;
    private List<List<Object>> table;
    private Map<Integer, List<Object>> statesTable;
    private int pc = 0;
    private int counter;
    
    private static final String IF = "^if\\s*\\([\\w\\+\\-\\*/><!=\\|&\\(\\)\\s]+\\)$";
    private static final String WHILE = "^while\\s*\\([\\w\\+\\-\\*/><!=\\|&\\(\\)\\s]+\\)$";
    private static final String ELSE = "else";
    private static final String BEGIN = "{";
    private static final String END = "}";
    private static final String EXPRESSION = "^[a-zA-Z]\\w*\\s*=\\s*"
            + "[\\(\\w\\+\\-\\*/\\)\\s]+$";
    private static final String READ = "^read\\s*\\(\\s*[a-zA-Z]\\w*\\s*\\)$";
    private static final String POSSIBLE = "^[\\(\\w\\+\\-\\*/\\)\\s]+$";
    private static final String UNDEFINED = "UNDEFINED";
    private static final String DEFINED = "DEFINED";
    private static final String FIXED = "FIXED";
    private static final String READ_STATE = "READ ";
    private static final String ASSIGN_STATE = "ASSIGN ";
    private static final String IF_STATE = "IF ";
    private static final String ELSE_STATE = "ELSE ";
    private static final String WHILE_STATE = "WHILE ";
    private static final String OPEN_BRACKET_STATE = "{";
    private static final String CLOSE_BRACKET_STATE = "}";
    private static final String SEMICOLON = ";";
    private int numberOfVariables;
    private int doubleNumberOfVariables;
    private static Logger logger;
    
    static {
        logger = SingleLogger.getLogger();
    }

    public Map<Integer, List<Object>> getStatesTable() {
        return statesTable;
    }
    
    public ProgramTableBuilder(List<Variable> variables, List<String> operators) {
        this.variables = variables;
        this.operators = operators;
        numberOfVariables = variables.size();
        doubleNumberOfVariables = numberOfVariables * 2;
    }
    
    public List<List<Object>> buildTable() {
        setVariablesNames();
        table = new ArrayList<>();
        statesTable = new HashMap<>();
        counter = 0;
        if (translateOperatorsBlock(operators.size())) {
            return table;
        }
        return null;
    }
    
    private void setVariablesNames() {
        variablesNames = new ArrayList<>(numberOfVariables);
        for (int i = 0; i < numberOfVariables; i++) {
            variablesNames.add(variables.get(i).getName());
        }
    }
    
    private enum ProgramState {
        IF,
        ELSE,
        WHILE,
        READ,
        ASSIGNMENT,
        BEGIN,
        END,
        POSSIBLE_EXPRESSION
    }
    
    private ProgramState determineState(String operator) {
        if (operator == null) {
            throw new IllegalArgumentException();
        }
        if (operator.matches(IF)) {
            return ProgramState.IF;
        }
        if (operator.matches(WHILE)) {
            return ProgramState.WHILE;
        }
        if (operator.matches(ELSE)) {
            return ProgramState.ELSE;
        }
        if (operator.equals(BEGIN)) {
            return ProgramState.BEGIN;
        }
        if (operator.equals(END)) {
            return ProgramState.END;
        }
        if (operator.matches(EXPRESSION)) {
            return ProgramState.ASSIGNMENT;
        }
        if (operator.matches(READ)) {
            return ProgramState.READ;
        }
        if (operator.isEmpty() || operator.matches(POSSIBLE)) {
            return ProgramState.POSSIBLE_EXPRESSION;
        }
        logger.log(Level.ERROR, "Error operator: " + operator);
        throw new IllegalArgumentException();
    }
    
    private void clear() {
        table.clear();
        table = null;
        statesTable.clear();
        statesTable = null;
    }
    
    private boolean translateOperatorsBlock(int finish) {
        ProgramState currentState;
        String currentOperator;
        while (counter < finish) {
            currentOperator = operators.get(counter);
            try {
                currentState = determineState(currentOperator);
            }
            catch(IllegalArgumentException ex) {
                clear();
                return false;
            }
            switch(currentState) {
                case READ:
                    if (!translateRead()) {
                        clear();
                        return false;
                    }
                    break;

                case BEGIN:
                    clear();
                    logger.log(Level.ERROR, "Unsuitable open bracket ({)");
                    return false;
                    
                case POSSIBLE_EXPRESSION:
                    break;
                    
                case END:
                    clear();
                    logger.log(Level.ERROR, "Unsuitable close bracket (})");
                    return false;
                    
                case ASSIGNMENT:
                    if (!translateAssignment()) {
                        clear();
                        return false;
                    }
                    break;
                    
                case IF:
                    if (!translateIf()) {
                        clear();
                        return false;
                    }
                    break;
                    
                case WHILE:
                    if (!translateWhile()) {
                        clear();
                        return false;
                    }
                    break;
                    
                case ELSE:
                    clear();
                    logger.log(Level.ERROR, "Unsuitable ELSE");
                    return false;
                    
            }
            counter++;
        }
        return true;
    }
    
    private boolean translateRead() {
        String innerVariable = operators.get(counter).substring(4).trim();
        innerVariable = innerVariable.substring(1, innerVariable.length() - 1).trim();
        int variableToRead = findVariable(innerVariable);
        if (variableToRead == -1) {
            logger.log(Level.ERROR, "There is no " + innerVariable + " variable");
            return false;
        }
        List<Object> currentNode = fillCurrentCells();
        for (int i = numberOfVariables; i < numberOfVariables + variableToRead; i++) {
            currentNode.add(currentNode.get(i - numberOfVariables));
        }
        currentNode.add(DEFINED);
        for (int i = numberOfVariables + variableToRead + 1; i < doubleNumberOfVariables; i++) {
            currentNode.add(currentNode.get(i - numberOfVariables));
        }
        table.add(currentNode);
        statesTable.put(pc, currentNode);
        currentNode.add(pc);
        currentNode.add(++pc);
        currentNode.add(READ_STATE + variableToRead);
        return true;
    }

    private boolean translateAssignment() {
        String[] expressionParts = operators.get(counter).split("=");
        int size = 2;
        if (expressionParts.length != size) {
            logger.log(Level.ERROR, "Wrong assignment: " + operators.get(counter));
            return false;
        }
        for (int i = 0; i < size; i++) {
            expressionParts[i] = expressionParts[i].trim();
        }
        int variableToAssign = findVariable(expressionParts[0]);
        if (variableToAssign == -1) {
            logger.log(Level.ERROR, "There is no " + expressionParts[0] + " variable");
            return false;
        }
        if (!isExpressionAllowed(expressionParts[1])) {
            logger.log(Level.ERROR, "Unknown identificator in " + expressionParts[1]);
            return false;
        }
        List<Object> currentNode = fillCurrentCells();
        for (int i = numberOfVariables; i < numberOfVariables + variableToAssign; i++) {
            currentNode.add(currentNode.get(i - numberOfVariables));
        }
        currentNode.add(expressionParts[1]);
        for (int i = numberOfVariables + variableToAssign + 1; i < doubleNumberOfVariables; i++) {
            currentNode.add(currentNode.get(i - numberOfVariables));
        }
        table.add(currentNode);
        statesTable.put(pc, currentNode);
        currentNode.add(pc);
        currentNode.add(++pc);
        currentNode.add(ASSIGN_STATE + variableToAssign);
        return true;
    }
    
    private boolean translateIf() {
        String condition = operators.get(counter).substring(2).trim();
        condition = condition.substring(1, condition.length() - 1).trim();
        if (!isConditionAllowed(condition)) {
            logger.log(Level.ERROR, "Unknown identificator in " + condition);
            return false;
        }
        int ifCounter = counter, bracketsNumber = 0;
        int pcShift = 2, counterShift = 1;
        boolean isOperatorFirst = true;
        
        //find end of this IF
        do {
            if (++ifCounter >= operators.size()) {
                logger.log(Level.ERROR, "Error after " + operators.get(counter));
                return false;
            }
            String innerOperator = operators.get(ifCounter);
            
            //skip empty strings
            while (innerOperator.isEmpty()) {
                if (++ifCounter >= operators.size()) {
                    logger.log(Level.ERROR, "Error after " + operators.get(counter));
                    return false;
                }
                innerOperator = operators.get(ifCounter);
            }
            
            //process operator
            if (innerOperator.equals(OPEN_BRACKET_STATE)) {
                if (isOperatorFirst) {
                    isOperatorFirst = false;
                    counterShift += ifCounter - counter;
                }
                bracketsNumber++;
            }
            else {
                if (isOperatorFirst) {
                    logger.log(Level.ERROR, "There is no '{' after IF near " + condition);
                    return false;
                }
                if (innerOperator.equals(CLOSE_BRACKET_STATE)) {
                    if (bracketsNumber <= 0) {
                        logger.log(Level.ERROR, "Error after " + operators.get(ifCounter));
                        return false;
                    }
                    else {
                        bracketsNumber--;
                        pcShift++;
                    }
                }
                else {
                    if (determineState(innerOperator) != ProgramState.POSSIBLE_EXPRESSION) {
                        pcShift++;
                    }
                }
            }
        }
        while (bracketsNumber > 0);
        
        
        List<Object> currentNode = fillCurrentCells();
        for (int i = numberOfVariables; i < doubleNumberOfVariables; i++) {
            currentNode.add(currentNode.get(i - numberOfVariables));
        }
        table.add(currentNode);
        statesTable.put(pc, currentNode);
        currentNode.add(pc);
        currentNode.add(pc + 1);
        
        counter += counterShift;
        int pcForElse = pc++;
        
        //process ELSE
        int elseCounter = ifCounter;
        //check end of program
        if (++elseCounter >= operators.size()) {
            if (!translateOperatorsBlock(ifCounter)) {
                return false;
            }
            List<Object> thenEndedNode = fillCurrentCells();
            for (int i = numberOfVariables; i < doubleNumberOfVariables; i++) {
                thenEndedNode.add(thenEndedNode.get(i - numberOfVariables));
            }
            table.add(thenEndedNode);
            statesTable.put(pc, thenEndedNode);
            currentNode.add(IF_STATE + condition + SEMICOLON + ELSE_STATE + (pc + 1));
            thenEndedNode.add(pc);
            thenEndedNode.add(++pc);
            thenEndedNode.add(IF_STATE + pcForElse + " ENDS");
            fixLastNode(currentNode, thenEndedNode);
            return true;
        }
        String innerOperator = operators.get(elseCounter);
            
        //skip empty strings
        while (innerOperator.isEmpty()) {
                if (++elseCounter >= operators.size()) {
                    if (!translateOperatorsBlock(ifCounter)) {
                        return false;
                    }
                    List<Object> thenEndedNode = fillCurrentCells();
                    for (int i = numberOfVariables; i < doubleNumberOfVariables; i++) {
                        thenEndedNode.add(thenEndedNode.get(i - numberOfVariables));
                    }
                    table.add(thenEndedNode);
                    statesTable.put(pc, thenEndedNode);
                    currentNode.add(IF_STATE + condition + SEMICOLON + ELSE_STATE + (pc + 1));
                    thenEndedNode.add(pc);
                    thenEndedNode.add(++pc);
                    thenEndedNode.add(IF_STATE + pcForElse + " ENDS");
                    fixLastNode(currentNode, thenEndedNode);
                    return true;
            }
            innerOperator = operators.get(elseCounter);
        }
        
        int pcElseShift = 2, counterElseShift = 1;
        boolean isThereElse = false;
        if (innerOperator.equals(ELSE)) {
            isThereElse = true;
            bracketsNumber = 0;
            isOperatorFirst = true;
            
            //find end of this ELSE
            do {
                if (++elseCounter >= operators.size()) {
                    logger.log(Level.ERROR, "Error after " + operators.get(counter));
                    return false;
                }
                innerOperator = operators.get(elseCounter);

                //skip empty strings
                while (innerOperator.isEmpty()) {
                    if (++elseCounter >= operators.size()) {
                        logger.log(Level.ERROR, "Error after " + operators.get(counter));
                        return false;
                    }
                    innerOperator = operators.get(elseCounter);
                }

                //process operator
                if (innerOperator.equals(OPEN_BRACKET_STATE)) {
                    if (isOperatorFirst) {
                        isOperatorFirst = false;
                        counterElseShift += elseCounter - ifCounter;
                    }
                    bracketsNumber++;
                }
                else {
                    if (isOperatorFirst) {
                        logger.log(Level.ERROR, "There is no '{' after ELSE from IF " + condition);
                        return false;
                    }
                    if (innerOperator.equals(CLOSE_BRACKET_STATE)) {
                        if (bracketsNumber <= 0) {
                            logger.log(Level.ERROR, "Error after " + operators.get(elseCounter));
                            return false;
                        }
                        else {
                            bracketsNumber--;
                            pcElseShift++;
                        }
                    }
                    else {
                        if (determineState(innerOperator) != ProgramState.POSSIBLE_EXPRESSION) {
                            pcElseShift++;
                        }
                    }
                }
            }
            while (bracketsNumber > 0);
        }

        if (isThereElse) {
            List<Object> currentElseNode = fillCurrentCells();
            for (int i = numberOfVariables; i < doubleNumberOfVariables; i++) {
                currentElseNode.add(currentElseNode.get(i - numberOfVariables));
            }
            
            if (!translateOperatorsBlock(ifCounter)) {
                return false;
            }
            
            List<Object> thenEndedNode = fillCurrentCells();
            for (int i = numberOfVariables; i < doubleNumberOfVariables; i++) {
                thenEndedNode.add(thenEndedNode.get(i - numberOfVariables));
            }
            table.add(thenEndedNode);
            statesTable.put(pc, thenEndedNode);
            thenEndedNode.add(pc);
            thenEndedNode.add(pc++ + pcElseShift);
            thenEndedNode.add(IF_STATE + pcForElse + " ENDS");
            
            table.add(currentElseNode);
            statesTable.put(pc, currentElseNode);
            currentNode.add(IF_STATE + condition + SEMICOLON + ELSE_STATE + pc);
            currentElseNode.add(pc);
            currentElseNode.add(++pc);
            currentElseNode.add(ELSE_STATE + pcForElse);
            counter += counterElseShift;
            if (!translateOperatorsBlock(elseCounter)) {
                return false;
            }
            List<Object> elseEndedNode = fillCurrentCells();
            for (int i = numberOfVariables; i < doubleNumberOfVariables; i++) {
                elseEndedNode.add(elseEndedNode.get(i - numberOfVariables));
            }
            table.add(elseEndedNode);
            statesTable.put(pc, elseEndedNode);
            elseEndedNode.add(pc);
            elseEndedNode.add(++pc);
            elseEndedNode.add(ELSE_STATE + pcForElse + " ENDS");
            fixBothNodes(thenEndedNode, elseEndedNode);
        }
        else {
            if (!translateOperatorsBlock(ifCounter)) {
                return false;
            }
            List<Object> thenEndedNode = fillCurrentCells();
            for (int i = numberOfVariables; i < doubleNumberOfVariables; i++) {
                thenEndedNode.add(thenEndedNode.get(i - numberOfVariables));
            }
            table.add(thenEndedNode);
            statesTable.put(pc, thenEndedNode);
            currentNode.add(IF_STATE + condition + SEMICOLON + ELSE_STATE + (pc + 1));
            thenEndedNode.add(pc);
            thenEndedNode.add(++pc);
            thenEndedNode.add(IF_STATE + pcForElse + " ENDS");
            fixLastNode(currentNode, thenEndedNode);
        }
        return true;
    }

    private boolean translateWhile() {
        String condition = operators.get(counter).substring(5).trim();
        condition = condition.substring(1, condition.length() - 1).trim();
        if (!isConditionAllowed(condition)) {
            logger.log(Level.ERROR, "Unknown identificator in " + condition);
            return false;
        }
        int whileCounter = counter, bracketsNumber = 0;
        int pcShift = 2, counterShift = 1;
        boolean isOperatorFirst = true;
        
        //find end of this WHILE
        do {
            if (++whileCounter >= operators.size()) {
                logger.log(Level.ERROR, "Error after " + operators.get(counter));
                return false;
            }
            String innerOperator = operators.get(whileCounter);
            
            //skip empty strings
            while (innerOperator.isEmpty()) {
                if (++whileCounter >= operators.size()) {
                    logger.log(Level.ERROR, "Error after " + operators.get(counter));
                    return false;
                }
                innerOperator = operators.get(whileCounter);
            }
            
            //process operator
            if (innerOperator.equals(OPEN_BRACKET_STATE)) {
                if (isOperatorFirst) {
                    isOperatorFirst = false;
                    counterShift += whileCounter - counter;
                }
                bracketsNumber++;
            }
            else {
                if (isOperatorFirst) {
                    logger.log(Level.ERROR, "There is no '{' after WHILE near " + condition);
                    return false;
                }
                if (innerOperator.equals(CLOSE_BRACKET_STATE)) {
                    if (bracketsNumber <= 0) {
                        logger.log(Level.ERROR, "Error after " + operators.get(whileCounter));
                        return false;
                    }
                    else {
                        bracketsNumber--;
                        pcShift++;
                    }
                }
                else {
                    if (determineState(innerOperator) != ProgramState.POSSIBLE_EXPRESSION) {
                        pcShift++;
                    }
                }
            }
        }
        while (bracketsNumber > 0);
        
        List<Object> currentNode = fillCurrentCells();
        for (int i = numberOfVariables; i < doubleNumberOfVariables; i++) {
            currentNode.add(currentNode.get(i - numberOfVariables));
        }
        table.add(currentNode);
        statesTable.put(pc, currentNode);
        currentNode.add(pc);
        currentNode.add(++pc);
        counter += counterShift;
        int pcForEndOfWhile = pc - 1;
        if (!translateOperatorsBlock(whileCounter)) {
            return false;
        }
        
        List<Object> whileEndedNode = fillCurrentCells();
        for (int i = numberOfVariables; i < doubleNumberOfVariables; i++) {
            whileEndedNode.add(whileEndedNode.get(i - numberOfVariables));
        }
        table.add(whileEndedNode);
        statesTable.put(pc, whileEndedNode);
        currentNode.add(WHILE_STATE + condition + SEMICOLON + ELSE_STATE + (pc + 1));
        whileEndedNode.add(pc);
        whileEndedNode.add(++pc);
        whileEndedNode.add(WHILE_STATE + pcForEndOfWhile + " ENDS");
        fixLastNode(currentNode, whileEndedNode);
        return true;
    }
    
    private List<Object> fillCurrentCells() {
        List<Object> currentNode = new ArrayList<>();
        if (table.isEmpty()) {
            for (int i = 0; i < numberOfVariables; i++) {
                currentNode.add(UNDEFINED);
            }
        }
        else {
            List<Object> previousNode = table.get(table.size() - 1);
            for (int i = 0; i < numberOfVariables; i++) {
                currentNode.add(previousNode.get(i + numberOfVariables));
            }
        }
        return currentNode;
    }
    
    private int findVariable(String variableName) {
        int variableNotFound = -1;
        for (int i = 0; i < variablesNames.size(); i++) {
            if (variablesNames.get(i).equals(variableName)) {
                return i;
            }
        }
        return variableNotFound;
    }
    
    private boolean isExpressionAllowed(String expression) {
        String[] parts = expression.split("[\\+\\-\\*/]");
        return arePartsValid(parts);
    }
    
    private boolean isConditionAllowed(String condition) {
        String[] parts = condition.split("[\\+\\-\\*/=!><&\\|]");
        return arePartsValid(parts);
    }
    
    private boolean arePartsValid(String[] parts) {
        for (int i = 0; i < parts.length; i++) {
            parts[i] = parts[i].trim();
            if (parts[i].isEmpty()) {
                continue;
            }
            while (parts[i].charAt(0) == '(') {
                parts[i] = parts[i].substring(1).trim();
            }
            while (parts[i].charAt(parts[i].length() - 1) == ')') {
                parts[i] = parts[i].substring(0, parts[i].length() - 1).trim();
            }
            if (!variablesNames.contains(parts[i]) && !parts[i].isEmpty() && !parts[i].matches("^\\d+$")) {
                return false;
            }
        }
        return true;
    }
    
    private void fixBothNodes(List<Object> earlyNode, List<Object> lateNode) {
        for (int i = numberOfVariables; i < doubleNumberOfVariables; i++) {
            if (!earlyNode.get(i).equals(lateNode.get(i))) {
                earlyNode.set(i, FIXED);
                lateNode.set(i, FIXED);
            }
        }
    }
    
    private void fixLastNode(List<Object> earlyNode, List<Object> lateNode) {
        for (int i = numberOfVariables; i < doubleNumberOfVariables; i++) {
            if (!earlyNode.get(i).equals(lateNode.get(i))) {
                lateNode.set(i, FIXED);
            }
        }
    }
}

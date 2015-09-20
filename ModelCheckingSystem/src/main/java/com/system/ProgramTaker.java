package com.system;

import com.system.kripkestructure.IntegerVariable;
import com.system.kripkestructure.Variable;
import com.system.util.SingleLogger;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 *
 * @author Alexander Erin <arcquim@gmail.com>
 */
class ProgramTaker {
    
    private static final String SEMICOLON = ";";
    private static final String OPEN_BRACKET = "\\{";
    private static final String CLOSE_BRACKET = "\\}";
    private static final String OPEN_BRACKET_TO_REPLACE = ";\\{;";
    private static final String CLOSE_BRACKET_TO_REPLACE = ";\\};";
    private static final String WHITESPACES_TO_REMOVE = "[^\\S ]";
    private static final String INTEGER_VARIABLE = "^int +[a-zA-Z]\\w*$";
    private static final String IF = "if";
    private static final String WHILE = "while";
    private static final String INT = "int";
    private static final String ELSE = "else";
    private static final String READ = "read";
    private String program;
    private List<String> operators;
    private List<Variable> variables;
    private static Logger logger;
    
    static {
        logger = SingleLogger.getLogger();
    }
    
    public ProgramTaker(String program) {
        if (program == null || program.isEmpty()) {
            throw new IllegalArgumentException();
        }
        this.program = program;
    }
    
    public List<String> splitProgram() {
        if (operators != null) {
            return operators;
        }
        String modifiedProgram = program.replaceAll(OPEN_BRACKET, OPEN_BRACKET_TO_REPLACE)
                .replaceAll(CLOSE_BRACKET, CLOSE_BRACKET_TO_REPLACE);
        operators = new LinkedList<>();
        variables = new ArrayList<>();
        String[] splitedProgram = modifiedProgram.split(SEMICOLON);
        for (int i = 0; i < splitedProgram.length; i++) {
            splitedProgram[i] = splitedProgram[i].trim();
            if (splitedProgram[i].matches(INTEGER_VARIABLE)) {
                String variableName = splitedProgram[i].substring(3).trim();
                if (variableName.equals(IF) || variableName.equals(ELSE) ||
                        variableName.equals(WHILE) || variableName.equals(READ) ||
                        variableName.equals(INT)) {
                    logger.log(Level.ERROR, "Forbidden name of variable" + variableName);
                    operators = null;
                    variables = null;
                    break;
                }
                variables.add(new IntegerVariable(variableName));
            }
            else {
                operators.add(splitedProgram[i].replaceAll(WHITESPACES_TO_REMOVE, "").trim());
            }
        }
        return operators;
    }
    
    public List<Variable> getProgramVariables() {
        if (operators == null) {
            splitProgram();
        }
        return variables;
    }
}

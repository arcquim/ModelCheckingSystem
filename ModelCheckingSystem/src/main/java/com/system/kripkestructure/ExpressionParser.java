package com.system.kripkestructure;

import com.system.util.SingleLogger;
import java.util.Deque;
import java.util.List;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 *
 * @author Alexander Erin <arcquim@gmail.com>
 */
class ExpressionParser {
 
    private List<Variable> variables;
    private ConditionParser conditionParser;
    
    private static final String CONDITION = ".[!=<>&|].";
    
    private static Logger logger;
    
    static {
        logger = SingleLogger.getLogger();
    }
    
    public ExpressionParser(List<Variable> variables) {
        this.variables = variables;
        conditionParser = new ConditionParser(variables);
    }
    
    public Deque<String> parseExpression(String expression) {
        if (expression == null || expression.isEmpty() || expression.matches(CONDITION)) {
            logger.log(Level.ERROR, "String is empty or not an expression");
            return null;
        }
        return conditionParser.parseCondition(expression);
    }
}

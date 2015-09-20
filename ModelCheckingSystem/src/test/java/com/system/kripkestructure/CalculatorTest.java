/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.system.kripkestructure;

import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Alexander Erin <arcquim@gmail.com>
 */
public class CalculatorTest {
    
    public CalculatorTest() {
    }

    /**
     * Test of calculate method, of class Calculator, simple case.
     */
    @Test
    public void simpleTestCalculate() {
        List<Variable> variables = new ArrayList<>();
        variables.add(new IntegerVariable("a"));
        variables.add(new IntegerVariable("b"));
        variables.add(new IntegerVariable("c"));
        variables.add(new IntegerVariable("d"));
        ConditionParser conditionParser = new ConditionParser(variables);
        Map<Variable, String> variablesValuesMap = new HashMap<>();
        variablesValuesMap.put(variables.get(0), "-19");
        variablesValuesMap.put(variables.get(3), "100");
        variablesValuesMap.put(variables.get(1), "1019");
        variablesValuesMap.put(variables.get(2), "0");
        String infixFormula = "(a + 35) * c";
        Deque<String> postfixFormula = new ConditionParser(variables).parseCondition(infixFormula);
        Calculator instance = new Calculator();
        String expResult = "0";
        String result = instance.calculate(variablesValuesMap, infixFormula, postfixFormula);
        assertEquals(expResult, result);
    }
    
    /**
     * Test of calculate method, of class Calculator, simple case No. 2.
     */
    @Test
    public void simpleTestCalculate2() {
        List<Variable> variables = new ArrayList<>();
        variables.add(new IntegerVariable("a"));
        variables.add(new IntegerVariable("b"));
        variables.add(new IntegerVariable("c"));
        variables.add(new IntegerVariable("d"));
        ConditionParser conditionParser = new ConditionParser(variables);
        Map<Variable, String> variablesValuesMap = new HashMap<>();
        variablesValuesMap.put(variables.get(0), "-19");
        variablesValuesMap.put(variables.get(3), "100");
        variablesValuesMap.put(variables.get(1), "1019");
        variablesValuesMap.put(variables.get(2), "0");
        String infixFormula = "((a + b) * a -d/10) * 1 - 10";
        Deque<String> postfixFormula = new ConditionParser(variables).parseCondition(infixFormula);
        Calculator instance = new Calculator();
        String expResult = "-19020";
        String result = instance.calculate(variablesValuesMap, infixFormula, postfixFormula);
        assertEquals(expResult, result);
    }
    
    /**
     * Test of calculate method, of class Calculator, medium case.
     */
    @Test
    public void mediumTestCalculate() {
        List<Variable> variables = new ArrayList<>();
        variables.add(new IntegerVariable("a"));
        variables.add(new IntegerVariable("b"));
        variables.add(new IntegerVariable("c"));
        variables.add(new IntegerVariable("d"));
        ConditionParser conditionParser = new ConditionParser(variables);
        Map<Variable, String> variablesValuesMap = new HashMap<>();
        variablesValuesMap.put(variables.get(0), "-19");
        variablesValuesMap.put(variables.get(3), "100");
        variablesValuesMap.put(variables.get(1), "1019");
        variablesValuesMap.put(variables.get(2), "0");
        String infixFormula = "((a + b) * a -d/10) * 1 - 10 < 1 && a <= -19";
        Deque<String> postfixFormula = new ConditionParser(variables).parseCondition(infixFormula);
        Calculator instance = new Calculator();
        String expResult = "true";
        String result = instance.calculate(variablesValuesMap, infixFormula, postfixFormula);
        assertEquals(expResult, result);
    }
    
    /**
     * Test of calculate method, of class Calculator, medium case No. 2.
     */
    @Test
    public void mediumTestCalculate2() {
        List<Variable> variables = new ArrayList<>();
        variables.add(new IntegerVariable("a"));
        variables.add(new IntegerVariable("b"));
        variables.add(new IntegerVariable("c"));
        variables.add(new IntegerVariable("d"));
        ConditionParser conditionParser = new ConditionParser(variables);
        Map<Variable, String> variablesValuesMap = new HashMap<>();
        variablesValuesMap.put(variables.get(0), "10");
        variablesValuesMap.put(variables.get(3), "-1");
        variablesValuesMap.put(variables.get(1), "15");
        variablesValuesMap.put(variables.get(2), "-2");
        String infixFormula = "a > b && b*c+a==d * 20||c <= d + 5";
        Deque<String> postfixFormula = new ConditionParser(variables).parseCondition(infixFormula);
        Calculator instance = new Calculator();
        String expResult = "true";
        String result = instance.calculate(variablesValuesMap, infixFormula, postfixFormula);
        assertEquals(expResult, result);
    }
    
    /**
     * Test of calculate method, of class Calculator, medium case No. 3.
     */
    @Test
    public void mediumTestCalculate3() {
        List<Variable> variables = new ArrayList<>();
        variables.add(new IntegerVariable("a"));
        variables.add(new IntegerVariable("b"));
        variables.add(new IntegerVariable("c"));
        variables.add(new IntegerVariable("d"));
        ConditionParser conditionParser = new ConditionParser(variables);
        Map<Variable, String> variablesValuesMap = new HashMap<>();
        variablesValuesMap.put(variables.get(0), "10");
        variablesValuesMap.put(variables.get(3), "-1");
        variablesValuesMap.put(variables.get(1), "15");
        variablesValuesMap.put(variables.get(2), "-2");
        String infixFormula = "!(a > b && b*c+a==d * 20||c <= d + 5)";
        Deque<String> postfixFormula = new ConditionParser(variables).parseCondition(infixFormula);
        Calculator instance = new Calculator();
        String expResult = "false";
        String result = instance.calculate(variablesValuesMap, infixFormula, postfixFormula);
        assertEquals(expResult, result);
    }
    
    /**
     * Test of calculate method, of class Calculator, difficult case.
     */
    @Test
    public void difficultTestCalculate() {
        List<Variable> variables = new ArrayList<>();
        variables.add(new IntegerVariable("a"));
        variables.add(new IntegerVariable("b"));
        variables.add(new IntegerVariable("c"));
        variables.add(new IntegerVariable("d"));
        ConditionParser conditionParser = new ConditionParser(variables);
        Map<Variable, String> variablesValuesMap = new HashMap<>();
        variablesValuesMap.put(variables.get(0), "5");
        variablesValuesMap.put(variables.get(3), "101");
        variablesValuesMap.put(variables.get(1), "-8");
        variablesValuesMap.put(variables.get(2), "1");
        String infixFormula = "(a >= (2 * b - 3 / c) ) && ( (d / a != (-b) * c) ||  !(a > -7) ||  ((-3 - b) <= 10 * a) && 2 * c > 1)";
        Deque<String> postfixFormula = new ConditionParser(variables).parseCondition(infixFormula);
        Calculator instance = new Calculator();
        String expResult = "true";
        String result = instance.calculate(variablesValuesMap, infixFormula, postfixFormula);
        assertEquals(expResult, result);
    }
}

package com.system.kripkestructure;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Alexander Erin <arcquim@gmail.com>
 */
public class ProgramTableBuilderTest {
    
    private static final String UNDEFINED = "UNDEFINED";
    private static final String DEFINED = "DEFINED";
    private static final String FIXED = "FIXED";
    private static final String READ_STATE = "READ ";
    private static final String ASSIGN_STATE = "ASSIGN ";
    private static final String IF_STATE = "IF ";
    private static final String ELSE_STATE = "ELSE ";
    private static final String WHILE_STATE = "WHILE ";
    private static final String SEMICOLON = ";";
    
    public ProgramTableBuilderTest() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of getStatesTable method, of class ProgramTableBuilder, simple case.
     */
    @Test
    public void simpleTestOfGetStatesTable() {
        List<Variable> variables = buildVariableForSimpleTest();
        List<String> operators = buildOperatorsForSimpleTest();
        ProgramTableBuilder instance = new ProgramTableBuilder(variables, operators);
        Map<Integer, List<Object>> expResult = buildExpMapForSimpleTest();
        instance.buildTable();
        Map<Integer, List<Object>> result = instance.getStatesTable();
        assertEquals(expResult, result);
    }
    
    private Map<Integer, List<Object>> buildExpMapForSimpleTest() {
        Map<Integer, List<Object>> expResult = new HashMap<>();
        expResult.put(0, Arrays.asList(new Object[]{UNDEFINED, UNDEFINED, UNDEFINED, DEFINED, 0, 1, READ_STATE + "1"}));
        expResult.put(1, Arrays.asList(new Object[]{UNDEFINED, DEFINED, DEFINED, DEFINED, 1, 2, READ_STATE + "0"}));
        expResult.put(2, Arrays.asList(new Object[]{DEFINED, DEFINED, "b", DEFINED, 2, 3, ASSIGN_STATE + "0"}));
        expResult.put(3, Arrays.asList(new Object[]{"b", DEFINED, "b", DEFINED, 3, 4, IF_STATE + "a==b" + SEMICOLON + ELSE_STATE + 7}));
        expResult.put(4, Arrays.asList(new Object[]{"b", DEFINED, "b/2", DEFINED, 4, 5, ASSIGN_STATE + "0"}));
        expResult.put(5, Arrays.asList(new Object[]{"b/2", DEFINED, "b/2", "5", 5, 6, ASSIGN_STATE + "1"}));
        expResult.put(6, Arrays.asList(new Object[]{"b/2", "5", FIXED, FIXED, 6, 11, IF_STATE + "3 ENDS"}));
        expResult.put(7, Arrays.asList(new Object[]{"b", DEFINED, "b", DEFINED, 7, 8, ELSE_STATE + "3"}));
        expResult.put(8, Arrays.asList(new Object[]{"b", DEFINED, "-5", DEFINED, 8, 9, ASSIGN_STATE + "0"}));
        expResult.put(9, Arrays.asList(new Object[]{"-5", DEFINED, "-5", "0", 9, 10, ASSIGN_STATE + "1"}));
        expResult.put(10, Arrays.asList(new Object[]{"-5", "0", FIXED, FIXED, 10, 11, ELSE_STATE + "3 ENDS"}));
        return expResult;
    }
    
    private List<List<Object>> buildExpListForSimpleTest() {
        List<List<Object>> expResult = new ArrayList<>();
        expResult.add(Arrays.asList(new Object[]{UNDEFINED, UNDEFINED, UNDEFINED, DEFINED, 0, 1, READ_STATE + "1"}));
        expResult.add(Arrays.asList(new Object[]{UNDEFINED, DEFINED, DEFINED, DEFINED, 1, 2, READ_STATE + "0"}));
        expResult.add(Arrays.asList(new Object[]{DEFINED, DEFINED, "b", DEFINED, 2, 3, ASSIGN_STATE + "0"}));
        expResult.add(Arrays.asList(new Object[]{"b", DEFINED, "b", DEFINED, 3, 4, IF_STATE + "a==b" + SEMICOLON + ELSE_STATE + 7}));
        expResult.add(Arrays.asList(new Object[]{"b", DEFINED, "b/2", DEFINED, 4, 5, ASSIGN_STATE + "0"}));
        expResult.add(Arrays.asList(new Object[]{"b/2", DEFINED, "b/2", "5", 5, 6, ASSIGN_STATE + "1"}));
        expResult.add(Arrays.asList(new Object[]{"b/2", "5", FIXED, FIXED, 6, 11, IF_STATE + "3 ENDS"}));
        expResult.add(Arrays.asList(new Object[]{"b", DEFINED, "b", DEFINED, 7, 8, ELSE_STATE + "3"}));
        expResult.add(Arrays.asList(new Object[]{"b", DEFINED, "-5", DEFINED, 8, 9, ASSIGN_STATE + "0"}));
        expResult.add(Arrays.asList(new Object[]{"-5", DEFINED, "-5", "0", 9, 10, ASSIGN_STATE + "1"}));
        expResult.add(Arrays.asList(new Object[]{"-5", "0", FIXED, FIXED, 10, 11, ELSE_STATE + "3 ENDS"}));
        return expResult;
    }
    
    private List<Variable> buildVariableForSimpleTest() {
        List<Variable> variables = new ArrayList<>();
        variables.add(new IntegerVariable("a"));
        variables.add(new IntegerVariable("b"));
        return variables;
    }
    
    private List<String> buildOperatorsForSimpleTest() {
        List<String> operators = new ArrayList<>();
        operators.add("");
        operators.add("read(b)");
        operators.add("read(a)");
        operators.add("a =  b");
        operators.add("if (a==b)");
        operators.add("{");
        operators.add("a = b/2");
        operators.add("b = 5");
        operators.add("}");
        operators.add("else");
        operators.add("{");
        operators.add("a =  -5");
        operators.add("b=  0");
        operators.add("}");
        return operators;
    }

    /**
     * Test of buildTable method, of class ProgramTableBuilder, simple case.
     */
    @Test
    public void simpleTestOfBuildTable() {
        List<Variable> variables = buildVariableForSimpleTest();
        List<String> operators = buildOperatorsForSimpleTest();
        ProgramTableBuilder instance = new ProgramTableBuilder(variables, operators);
        List<List<Object>> expResult = buildExpListForSimpleTest();
        List<List<Object>> result = instance.buildTable();
        assertEquals(expResult, result);
    }
    
    /**
     * Test of getStatesTable method, of class ProgramTableBuilder, medium case.
     */
    @Test
    public void mediumTestOfGetStatesTable() {
        List<Variable> variables = buildVariableForMediumTest();
        List<String> operators = buildOperatorsForMediumTest();
        ProgramTableBuilder instance = new ProgramTableBuilder(variables, operators);
        Map<Integer, List<Object>> expResult = buildExpMapForMediumTest();
        instance.buildTable();
        Map<Integer, List<Object>> result = instance.getStatesTable();
        assertEquals(expResult, result);
    }
    
    /**
     * Test of buildTable method, of class ProgramTableBuilder, medium case.
     */
    @Test
    public void mediumTestOfBuildTable() {
        List<Variable> variables = buildVariableForMediumTest();
        List<String> operators = buildOperatorsForMediumTest();
        ProgramTableBuilder instance = new ProgramTableBuilder(variables, operators);
        List<List<Object>> expResult = buildExpListForMediumTest();
        List<List<Object>> result = instance.buildTable();
        assertEquals(expResult, result);
    }
    
    private Map<Integer, List<Object>> buildExpMapForMediumTest() {
        Map<Integer, List<Object>> expResult = new HashMap<>();
        expResult.put(0, Arrays.asList(new Object[]{UNDEFINED, UNDEFINED, UNDEFINED, DEFINED, UNDEFINED, UNDEFINED, 0, 1, READ_STATE + "0"}));
        expResult.put(1, Arrays.asList(new Object[]{DEFINED, UNDEFINED, UNDEFINED, DEFINED, UNDEFINED, UNDEFINED, 1, 2, IF_STATE + "a>=5 && a<=10" + SEMICOLON + ELSE_STATE + 4}));
        expResult.put(2, Arrays.asList(new Object[]{DEFINED, UNDEFINED, UNDEFINED, DEFINED, "a * a + 4 - 3/a", UNDEFINED, 2, 3, ASSIGN_STATE + "1"}));
        expResult.put(3, Arrays.asList(new Object[]{DEFINED, "a * a + 4 - 3/a", UNDEFINED, DEFINED, FIXED, UNDEFINED, 3, 4, IF_STATE + "1 ENDS"}));
        expResult.put(4, Arrays.asList(new Object[]{DEFINED, FIXED, UNDEFINED, DEFINED, FIXED, UNDEFINED, 4, 5, IF_STATE + "b  > 3" + SEMICOLON + ELSE_STATE + 7}));
        expResult.put(5, Arrays.asList(new Object[]{DEFINED, FIXED, UNDEFINED, DEFINED, FIXED, UNDEFINED, 5, 6, READ_STATE + "0"}));
        expResult.put(6, Arrays.asList(new Object[]{DEFINED, FIXED, UNDEFINED, DEFINED, FIXED, UNDEFINED, 6, 18, IF_STATE + "4 ENDS"}));
        expResult.put(7, Arrays.asList(new Object[]{DEFINED, FIXED, UNDEFINED, DEFINED, FIXED, UNDEFINED, 7, 8, ELSE_STATE + "4"}));
        expResult.put(8, Arrays.asList(new Object[]{DEFINED, FIXED, UNDEFINED, DEFINED, FIXED, UNDEFINED, 8, 9, WHILE_STATE + "b >= 0" + SEMICOLON + ELSE_STATE + 17}));
        expResult.put(9, Arrays.asList(new Object[]{DEFINED, FIXED, UNDEFINED, DEFINED, "b - 1", UNDEFINED, 9, 10, ASSIGN_STATE + "1"}));
        expResult.put(10, Arrays.asList(new Object[]{DEFINED, "b - 1", UNDEFINED, DEFINED, "b - 1", UNDEFINED, 10, 11, IF_STATE + "b >= 1"+ SEMICOLON + ELSE_STATE + 13}));
        expResult.put(11, Arrays.asList(new Object[]{DEFINED, "b - 1", UNDEFINED, DEFINED, "2", UNDEFINED, 11, 12, ASSIGN_STATE + "1"}));
        expResult.put(12, Arrays.asList(new Object[]{DEFINED, "2", UNDEFINED, DEFINED, FIXED, UNDEFINED, 12, 16, IF_STATE + "10 ENDS"}));
        expResult.put(13, Arrays.asList(new Object[]{DEFINED, "b - 1", UNDEFINED, DEFINED, "b - 1", UNDEFINED, 13, 14, ELSE_STATE + "10"}));
        expResult.put(14, Arrays.asList(new Object[]{DEFINED, "b - 1", UNDEFINED, DEFINED, "0", UNDEFINED, 14, 15, ASSIGN_STATE + "1"}));
        expResult.put(15, Arrays.asList(new Object[]{DEFINED, "0", UNDEFINED, DEFINED, FIXED, UNDEFINED, 15, 16, ELSE_STATE + "10 ENDS"}));
        expResult.put(16, Arrays.asList(new Object[]{DEFINED, FIXED, UNDEFINED, DEFINED, FIXED, UNDEFINED, 16, 17, WHILE_STATE + "8 ENDS"}));
        expResult.put(17, Arrays.asList(new Object[]{DEFINED, FIXED, UNDEFINED, DEFINED, FIXED, UNDEFINED, 17, 18, ELSE_STATE + "4 ENDS"}));
        expResult.put(18, Arrays.asList(new Object[]{DEFINED, FIXED, UNDEFINED, "a + b/4", FIXED, UNDEFINED, 18, 19, ASSIGN_STATE + "0"}));
        expResult.put(19, Arrays.asList(new Object[]{"a + b/4", FIXED, UNDEFINED, "a + b/4", FIXED, DEFINED, 19, 20, READ_STATE + "2"}));
        expResult.put(20, Arrays.asList(new Object[]{"a + b/4", FIXED, DEFINED, "a + b/4", FIXED, DEFINED, 20, 21, WHILE_STATE + "c < 3" + SEMICOLON + ELSE_STATE + 24}));
        expResult.put(21, Arrays.asList(new Object[]{"a + b/4", FIXED, DEFINED, "a + b/4", FIXED, "c - 1", 21, 22, ASSIGN_STATE + "2"}));
        expResult.put(22, Arrays.asList(new Object[]{"a + b/4", FIXED, "c - 1", "a + b/4", "c / 2", "c - 1", 22, 23, ASSIGN_STATE + "1"}));
        expResult.put(23, Arrays.asList(new Object[]{"a + b/4", "c / 2", "c - 1", "a + b/4", FIXED, FIXED, 23, 24, WHILE_STATE + "20 ENDS"}));
        return expResult;
    }
    
    private List<List<Object>> buildExpListForMediumTest() {
        List<List<Object>> expResult = new ArrayList<>();
        expResult.add(Arrays.asList(new Object[]{UNDEFINED, UNDEFINED, UNDEFINED, DEFINED, UNDEFINED, UNDEFINED, 0, 1, READ_STATE + "0"}));
        expResult.add(Arrays.asList(new Object[]{DEFINED, UNDEFINED, UNDEFINED, DEFINED, UNDEFINED, UNDEFINED, 1, 2, IF_STATE + "a>=5 && a<=10" + SEMICOLON + ELSE_STATE + 4}));
        expResult.add(Arrays.asList(new Object[]{DEFINED, UNDEFINED, UNDEFINED, DEFINED, "a * a + 4 - 3/a", UNDEFINED, 2, 3, ASSIGN_STATE + "1"}));
        expResult.add(Arrays.asList(new Object[]{DEFINED, "a * a + 4 - 3/a", UNDEFINED, DEFINED, FIXED, UNDEFINED, 3, 4, IF_STATE + "1 ENDS"}));
        expResult.add(Arrays.asList(new Object[]{DEFINED, FIXED, UNDEFINED, DEFINED, FIXED, UNDEFINED, 4, 5, IF_STATE + "b  > 3" + SEMICOLON + ELSE_STATE + 7}));
        expResult.add(Arrays.asList(new Object[]{DEFINED, FIXED, UNDEFINED, DEFINED, FIXED, UNDEFINED, 5, 6, READ_STATE + "0"}));
        expResult.add(Arrays.asList(new Object[]{DEFINED, FIXED, UNDEFINED, DEFINED, FIXED, UNDEFINED, 6, 18, IF_STATE + "4 ENDS"}));
        expResult.add(Arrays.asList(new Object[]{DEFINED, FIXED, UNDEFINED, DEFINED, FIXED, UNDEFINED, 7, 8, ELSE_STATE + "4"}));
        expResult.add(Arrays.asList(new Object[]{DEFINED, FIXED, UNDEFINED, DEFINED, FIXED, UNDEFINED, 8, 9, WHILE_STATE + "b >= 0" + SEMICOLON + ELSE_STATE + 17}));
        expResult.add(Arrays.asList(new Object[]{DEFINED, FIXED, UNDEFINED, DEFINED, "b - 1", UNDEFINED, 9, 10, ASSIGN_STATE + "1"}));
        expResult.add(Arrays.asList(new Object[]{DEFINED, "b - 1", UNDEFINED, DEFINED, "b - 1", UNDEFINED, 10, 11, IF_STATE + "b >= 1" + SEMICOLON + ELSE_STATE + 13}));
        expResult.add(Arrays.asList(new Object[]{DEFINED, "b - 1", UNDEFINED, DEFINED, "2", UNDEFINED, 11, 12, ASSIGN_STATE + "1"}));
        expResult.add(Arrays.asList(new Object[]{DEFINED, "2", UNDEFINED, DEFINED, FIXED, UNDEFINED, 12, 16, IF_STATE + "10 ENDS"}));
        expResult.add(Arrays.asList(new Object[]{DEFINED, "b - 1", UNDEFINED, DEFINED, "b - 1", UNDEFINED, 13, 14, ELSE_STATE + "10"}));
        expResult.add(Arrays.asList(new Object[]{DEFINED, "b - 1", UNDEFINED, DEFINED, "0", UNDEFINED, 14, 15, ASSIGN_STATE + "1"}));
        expResult.add(Arrays.asList(new Object[]{DEFINED, "0", UNDEFINED, DEFINED, FIXED, UNDEFINED, 15, 16, ELSE_STATE + "10 ENDS"}));
        expResult.add(Arrays.asList(new Object[]{DEFINED, FIXED, UNDEFINED, DEFINED, FIXED, UNDEFINED, 16, 17, WHILE_STATE + "8 ENDS"}));
        expResult.add(Arrays.asList(new Object[]{DEFINED, FIXED, UNDEFINED, DEFINED, FIXED, UNDEFINED, 17, 18, ELSE_STATE + "4 ENDS"}));
        expResult.add(Arrays.asList(new Object[]{DEFINED, FIXED, UNDEFINED, "a + b/4", FIXED, UNDEFINED, 18, 19, ASSIGN_STATE + "0"}));
        expResult.add(Arrays.asList(new Object[]{"a + b/4", FIXED, UNDEFINED, "a + b/4", FIXED, DEFINED, 19, 20, READ_STATE + "2"}));
        expResult.add(Arrays.asList(new Object[]{"a + b/4", FIXED, DEFINED, "a + b/4", FIXED, DEFINED, 20, 21, WHILE_STATE + "c < 3" + SEMICOLON + ELSE_STATE + 24}));
        expResult.add(Arrays.asList(new Object[]{"a + b/4", FIXED, DEFINED, "a + b/4", FIXED, "c - 1", 21, 22, ASSIGN_STATE + "2"}));
        expResult.add(Arrays.asList(new Object[]{"a + b/4", FIXED, "c - 1", "a + b/4", "c / 2", "c - 1", 22, 23, ASSIGN_STATE + "1"}));
        expResult.add(Arrays.asList(new Object[]{"a + b/4", "c / 2", "c - 1", "a + b/4", FIXED, FIXED, 23, 24, WHILE_STATE + "20 ENDS"}));
        return expResult;
    }
    
    private List<Variable> buildVariableForMediumTest() {
        List<Variable> variables = new ArrayList<>();
        variables.add(new IntegerVariable("a"));
        variables.add(new IntegerVariable("b"));
        variables.add(new IntegerVariable("c"));
        return variables;
    }
    
    private List<String> buildOperatorsForMediumTest() {
        List<String> operators = new ArrayList<>();
        operators.add("read ( a )");
        operators.add("if(a>=5 && a<=10)");
        operators.add("{");
        operators.add("");
        operators.add("");
        operators.add("b=  a * a + 4 - 3/a");
        operators.add("");
        operators.add("}");
        operators.add("");
        operators.add("if (b  > 3)");
        operators.add("");
        operators.add("");
        operators.add("");
        operators.add("{");
        operators.add("read(a)");
        operators.add("}");
        operators.add("else");
        operators.add("{");
        operators.add("while (b >= 0)");
        operators.add("{");
        operators.add("b = b - 1");
        operators.add("if ( b >= 1)");
        operators.add("{");
        operators.add("b = 2");
        operators.add("}");
        operators.add("else");
        operators.add("{");
        operators.add("b = 0");
        operators.add("}");
        operators.add("}");
        operators.add("}");
        operators.add("");
        operators.add("a = a + b/4");
        operators.add("read(c)");
        operators.add("while (c < 3)");
        operators.add("{");
        operators.add("c = c - 1");
        operators.add("b = c / 2");
        operators.add("}");
        operators.add("b");
        return operators;
    }
    
    /**
     * Test of getStatesTable method, of class ProgramTableBuilder, difficult case.
     */
    @Test
    public void difficultTestOfGetStatesTable() {
        List<Variable> variables = buildVariableForDifficultTest();
        List<String> operators = buildOperatorsForDifficultTest();
        ProgramTableBuilder instance = new ProgramTableBuilder(variables, operators);
        Map<Integer, List<Object>> expResult = buildExpMapForDifficultTest();
        instance.buildTable();
        Map<Integer, List<Object>> result = instance.getStatesTable();
        assertEquals(expResult, result);
    }
    
    /**
     * Test of buildTable method, of class ProgramTableBuilder, difficult case.
     */
    @Test
    public void difficultTestOfBuildTable() {
        List<Variable> variables = buildVariableForDifficultTest();
        List<String> operators = buildOperatorsForDifficultTest();
        ProgramTableBuilder instance = new ProgramTableBuilder(variables, operators);
        List<List<Object>> expResult = buildExpListForDifficultTest();
        List<List<Object>> result = instance.buildTable();
        assertEquals(expResult, result);
    }
    
    private Map<Integer, List<Object>> buildExpMapForDifficultTest() {
        Map<Integer, List<Object>> expResult = new HashMap<>();
        expResult.put(0, Arrays.asList(new Object[]{UNDEFINED, UNDEFINED, UNDEFINED, UNDEFINED, DEFINED, UNDEFINED, UNDEFINED, UNDEFINED, 0, 1, READ_STATE + "0"}));
        expResult.put(1, Arrays.asList(new Object[]{DEFINED, UNDEFINED, UNDEFINED, UNDEFINED, DEFINED, UNDEFINED, UNDEFINED, "0", 1, 2, ASSIGN_STATE + "3"}));
        expResult.put(2, Arrays.asList(new Object[]{DEFINED, UNDEFINED, UNDEFINED, "0", DEFINED, UNDEFINED, "12", "0", 2, 3, ASSIGN_STATE + "2"}));
        expResult.put(3, Arrays.asList(new Object[]{DEFINED, UNDEFINED, "12", "0", "1", UNDEFINED, "12", "0", 3, 4, ASSIGN_STATE + "0"}));
        expResult.put(4, Arrays.asList(new Object[]{"1", UNDEFINED, "12", "0", "1", UNDEFINED, "12", "0", 4, 5, WHILE_STATE + "i < 10 && c != 16" + SEMICOLON + ELSE_STATE + 25}));
        expResult.put(5, Arrays.asList(new Object[]{"1", UNDEFINED, "12", "0", "1", DEFINED, "12", "0", 5, 6, READ_STATE + "1"}));
        expResult.put(6, Arrays.asList(new Object[]{"1", DEFINED, "12", "0", "a + b", DEFINED, "12", "0", 6, 7, ASSIGN_STATE + "0"}));
        expResult.put(7, Arrays.asList(new Object[]{"a + b", DEFINED, "12", "0", "a + b", DEFINED, "10", "0", 7, 8, ASSIGN_STATE + "2"}));
        expResult.put(8, Arrays.asList(new Object[]{"a + b", DEFINED, "10", "0", "a + b", DEFINED, "10", "0", 8, 9, WHILE_STATE + "(c != 100) ||  (a + b == 10) && (b + c != 14)" + SEMICOLON + ELSE_STATE + 23}));
        expResult.put(9, Arrays.asList(new Object[]{"a + b", DEFINED, "10", "0", "a + b", DEFINED, "10", "0", 9, 10, IF_STATE + "b != 6" + SEMICOLON + ELSE_STATE + 13}));
        expResult.put(10, Arrays.asList(new Object[]{"a + b", DEFINED, "10", "0", "a + b", DEFINED, "c * a", "0", 10, 11, ASSIGN_STATE + "2"}));
        expResult.put(11, Arrays.asList(new Object[]{"a + b", DEFINED, "c * a", "0", "a + b", "b + a + c", "c * a", "0", 11, 12, ASSIGN_STATE + "1"}));
        expResult.put(12, Arrays.asList(new Object[]{"a + b", "b + a + c", "c * a", "0", "a + b", FIXED, FIXED, "0", 12, 13, IF_STATE + "9 ENDS"}));
        expResult.put(13, Arrays.asList(new Object[]{"a + b", FIXED, FIXED, "0", "a + b", FIXED, FIXED, "0", 13, 14, WHILE_STATE + "a > 19" + SEMICOLON + ELSE_STATE + 22}));
        expResult.put(14, Arrays.asList(new Object[]{"a + b", FIXED, FIXED, "0", "a + b", FIXED, FIXED, "0", 14, 15, IF_STATE + "c == 23" + SEMICOLON + ELSE_STATE + 17}));
        expResult.put(15, Arrays.asList(new Object[]{"a + b", FIXED, FIXED, "0", "a - c", FIXED, FIXED, "0", 15, 16, ASSIGN_STATE + "0"}));
        expResult.put(16, Arrays.asList(new Object[]{"a - c", FIXED, FIXED, "0", FIXED, FIXED, FIXED, "0", 16, 21, IF_STATE + "14 ENDS"}));
        expResult.put(17, Arrays.asList(new Object[]{"a + b", FIXED, FIXED, "0", "a + b", FIXED, FIXED, "0", 17, 18, ELSE_STATE + "14"}));
        expResult.put(18, Arrays.asList(new Object[]{"a + b", FIXED, FIXED, "0", "a + b", FIXED, "c - 10", "0", 18, 19, ASSIGN_STATE + "2"}));
        expResult.put(19, Arrays.asList(new Object[]{"a + b", FIXED, "c - 10", "0", "a - c + 1", FIXED, "c - 10", "0", 19, 20, ASSIGN_STATE + "0"}));
        expResult.put(20, Arrays.asList(new Object[]{"a - c + 1", FIXED, "c - 10", "0", FIXED, FIXED, FIXED, "0", 20, 21, ELSE_STATE + "14 ENDS"}));
        expResult.put(21, Arrays.asList(new Object[]{FIXED, FIXED, FIXED, "0", FIXED, FIXED, FIXED, "0", 21, 22, WHILE_STATE + "13 ENDS"}));
        expResult.put(22, Arrays.asList(new Object[]{FIXED, FIXED, FIXED, "0", FIXED, FIXED, FIXED, "0", 22, 23, WHILE_STATE + "8 ENDS"}));
        expResult.put(23, Arrays.asList(new Object[]{FIXED, FIXED, FIXED, "0", FIXED, FIXED, FIXED, "i + 1", 23, 24, ASSIGN_STATE + "3"}));
        expResult.put(24, Arrays.asList(new Object[]{FIXED, FIXED, FIXED, "i + 1", FIXED, FIXED, FIXED, FIXED, 24, 25, WHILE_STATE + "4 ENDS"}));
        return expResult;
    }
    
    private List<List<Object>> buildExpListForDifficultTest() {
        List<List<Object>> expResult = new ArrayList<>();
        expResult.add(Arrays.asList(new Object[]{UNDEFINED, UNDEFINED, UNDEFINED, UNDEFINED, DEFINED, UNDEFINED, UNDEFINED, UNDEFINED, 0, 1, READ_STATE + "0"}));
        expResult.add(Arrays.asList(new Object[]{DEFINED, UNDEFINED, UNDEFINED, UNDEFINED, DEFINED, UNDEFINED, UNDEFINED, "0", 1, 2, ASSIGN_STATE + "3"}));
        expResult.add(Arrays.asList(new Object[]{DEFINED, UNDEFINED, UNDEFINED, "0", DEFINED, UNDEFINED, "12", "0", 2, 3, ASSIGN_STATE + "2"}));
        expResult.add(Arrays.asList(new Object[]{DEFINED, UNDEFINED, "12", "0", "1", UNDEFINED, "12", "0", 3, 4, ASSIGN_STATE + "0"}));
        expResult.add(Arrays.asList(new Object[]{"1", UNDEFINED, "12", "0", "1", UNDEFINED, "12", "0", 4, 5, WHILE_STATE + "i < 10 && c != 16" + SEMICOLON + ELSE_STATE + 25}));
        expResult.add(Arrays.asList(new Object[]{"1", UNDEFINED, "12", "0", "1", DEFINED, "12", "0", 5, 6, READ_STATE + "1"}));
        expResult.add(Arrays.asList(new Object[]{"1", DEFINED, "12", "0", "a + b", DEFINED, "12", "0", 6, 7, ASSIGN_STATE + "0"}));
        expResult.add(Arrays.asList(new Object[]{"a + b", DEFINED, "12", "0", "a + b", DEFINED, "10", "0", 7, 8, ASSIGN_STATE + "2"}));
        expResult.add(Arrays.asList(new Object[]{"a + b", DEFINED, "10", "0", "a + b", DEFINED, "10", "0", 8, 9, WHILE_STATE + "(c != 100) ||  (a + b == 10) && (b + c != 14)" + SEMICOLON + ELSE_STATE + 23}));
        expResult.add(Arrays.asList(new Object[]{"a + b", DEFINED, "10", "0", "a + b", DEFINED, "10", "0", 9, 10, IF_STATE + "b != 6" + SEMICOLON + ELSE_STATE + 13}));
        expResult.add(Arrays.asList(new Object[]{"a + b", DEFINED, "10", "0", "a + b", DEFINED, "c * a", "0", 10, 11, ASSIGN_STATE + "2"}));
        expResult.add(Arrays.asList(new Object[]{"a + b", DEFINED, "c * a", "0", "a + b", "b + a + c", "c * a", "0", 11, 12, ASSIGN_STATE + "1"}));
        expResult.add(Arrays.asList(new Object[]{"a + b", "b + a + c", "c * a", "0", "a + b", FIXED, FIXED, "0", 12, 13, IF_STATE + "9 ENDS"}));
        expResult.add(Arrays.asList(new Object[]{"a + b", FIXED, FIXED, "0", "a + b", FIXED, FIXED, "0", 13, 14, WHILE_STATE + "a > 19" + SEMICOLON + ELSE_STATE + 22}));
        expResult.add(Arrays.asList(new Object[]{"a + b", FIXED, FIXED, "0", "a + b", FIXED, FIXED, "0", 14, 15, IF_STATE + "c == 23" + SEMICOLON + ELSE_STATE + 17}));
        expResult.add(Arrays.asList(new Object[]{"a + b", FIXED, FIXED, "0", "a - c", FIXED, FIXED, "0", 15, 16, ASSIGN_STATE + "0"}));
        expResult.add(Arrays.asList(new Object[]{"a - c", FIXED, FIXED, "0", FIXED, FIXED, FIXED, "0", 16, 21, IF_STATE + "14 ENDS"}));
        expResult.add(Arrays.asList(new Object[]{"a + b", FIXED, FIXED, "0", "a + b", FIXED, FIXED, "0", 17, 18, ELSE_STATE + "14"}));
        expResult.add(Arrays.asList(new Object[]{"a + b", FIXED, FIXED, "0", "a + b", FIXED, "c - 10", "0", 18, 19, ASSIGN_STATE + "2"}));
        expResult.add(Arrays.asList(new Object[]{"a + b", FIXED, "c - 10", "0", "a - c + 1", FIXED, "c - 10", "0", 19, 20, ASSIGN_STATE + "0"}));
        expResult.add(Arrays.asList(new Object[]{"a - c + 1", FIXED, "c - 10", "0", FIXED, FIXED, FIXED, "0", 20, 21, ELSE_STATE + "14 ENDS"}));
        expResult.add(Arrays.asList(new Object[]{FIXED, FIXED, FIXED, "0", FIXED, FIXED, FIXED, "0", 21, 22, WHILE_STATE + "13 ENDS"}));
        expResult.add(Arrays.asList(new Object[]{FIXED, FIXED, FIXED, "0", FIXED, FIXED, FIXED, "0", 22, 23, WHILE_STATE + "8 ENDS"}));
        expResult.add(Arrays.asList(new Object[]{FIXED, FIXED, FIXED, "0", FIXED, FIXED, FIXED, "i + 1", 23, 24, ASSIGN_STATE + "3"}));
        expResult.add(Arrays.asList(new Object[]{FIXED, FIXED, FIXED, "i + 1", FIXED, FIXED, FIXED, FIXED, 24, 25, WHILE_STATE + "4 ENDS"}));
        return expResult;
    }
    
    private List<Variable> buildVariableForDifficultTest() {
        List<Variable> variables = new ArrayList<>();
        variables.add(new IntegerVariable("a"));
        variables.add(new IntegerVariable("b"));
        variables.add(new IntegerVariable("c"));
        variables.add(new IntegerVariable("i"));
        return variables;
    }
    
    private List<String> buildOperatorsForDifficultTest() {
        List<String> operators = new ArrayList<>();
        operators.add("read(a)");
        operators.add("i = 0");
        operators.add("c = 12");
        operators.add("a = 1");
        operators.add("while (i < 10 && c != 16)");
        operators.add("{");
        operators.add("read(b)");
        operators.add("a = a + b");
        operators.add("c = 10");
        operators.add("while ( (c != 100) ||  (a + b == 10) && (b + c != 14) )");
        operators.add("{");
        operators.add("if (b != 6)");
        operators.add("{");
        operators.add("c = c * a");
        operators.add("b = b + a + c");
        operators.add("}");
        operators.add("while(a > 19)");
        operators.add("{");
        operators.add("if (c == 23)");
        operators.add("{");
        operators.add("a = a - c");
        operators.add("}");
        operators.add("else");
        operators.add("{");
        operators.add("c = c - 10");
        operators.add("a = a - c + 1");
        operators.add("}");
        operators.add("}");
        operators.add("}");
        operators.add("i = i + 1");
        operators.add("}");
        return operators;
    }
}

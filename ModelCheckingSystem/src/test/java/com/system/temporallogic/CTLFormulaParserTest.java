/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.system.temporallogic;

import java.util.ArrayDeque;
import java.util.Deque;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Alexander Erin <arcquim@gmail.com>
 */
public class CTLFormulaParserTest {
    
    public CTLFormulaParserTest() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of getPostfixFormula method, of class CTLFormulaParser,
 simple case
     */
    @Test
    public void testSimpleCase() {
        String formula = "EX(1 AND 5)";
        CTLFormulaParser instance = new CTLFormulaParser(formula);
        Deque<Object> expResult = new ArrayDeque<>();
        expResult.addLast(1);
        expResult.addLast(5);
        expResult.addLast("AND");
        expResult.addLast("EX");
        Deque<Object> result = instance.getPostfixFormula();
        assertArrayEquals(expResult.toArray(), result.toArray());
    }
    
    private Deque<Object> buildExpectedResultMedium() {
        Deque<Object> expResult = new ArrayDeque<>();
        expResult.addLast(0);
        expResult.addLast("NOT");
        expResult.addLast(6);
        expResult.addLast("NOT");
        expResult.addLast(1);
        expResult.addLast("EF");
        expResult.addLast(3);
        expResult.addLast("EF");
        expResult.addLast("NOT");
        expResult.addLast("AND");
        expResult.addLast("AG");
        expResult.addLast("AND");
        expResult.addLast(3);
        expResult.addLast("NOT");
        expResult.addLast("AF");
        expResult.addLast("AND");
        expResult.addLast("OR");
        expResult.addLast("EX");
        expResult.addLast("AF");
        return expResult;
    }
    
    @Test
    public void testMediumCase() {
        String formula = "AF EX(NOT 0 OR NOT 6 AND AG   ( EF 1 AND NOT EF 3) AND AF NOT 3)";
        CTLFormulaParser instance = new CTLFormulaParser(formula);
        Deque<Object> expResult = buildExpectedResultMedium();
        Deque<Object> result = instance.getPostfixFormula();
        assertArrayEquals(expResult.toArray(), result.toArray());
    }
    
    private Deque<Object> buildExpectedResultDifficult() {
        Deque<Object> expResult = new ArrayDeque<>();
        expResult.addLast(0);
        expResult.addLast("AX");
        expResult.addLast(2);
        expResult.addLast(6);
        expResult.addLast("XOR");
        expResult.addLast(3);
        expResult.addLast(4);
        expResult.addLast("AND");
        expResult.addLast("OR");
        expResult.addLast("EG");
        expResult.addLast("AF");
        expResult.addLast("AU");
        expResult.addLast(6);
        expResult.addLast("EU");
        return expResult;
    }
    
    @Test
    public void testDifficultCase() {
        String formula = "AX 0 AU AF EG (2 XOR 6 OR 3 AND 4) EU 6";
        CTLFormulaParser instance = new CTLFormulaParser(formula);
        Deque<Object> expResult = buildExpectedResultDifficult();
        Deque<Object> result = instance.getPostfixFormula();
        assertArrayEquals(expResult.toArray(), result.toArray());
    }
}

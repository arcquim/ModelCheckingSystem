/*package com.system;

import com.system.kripkestructure.IntegerVariable;
import com.system.kripkestructure.Variable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Alexander Erin <arcquim@gmail.com>
 *
public class BDDStringParserTest {
    
    private BDDStringParser instance;
    private List<Variable> variables;
    
    /**
     * Default conctructor
     *
    public BDDStringParserTest() {
    }
    
    /**
     * Set up the tests initializing a variable list and an instance of BDDStringParser
     *
    @Before
    public void setUp() {
        variables = new ArrayList<>();
        variables.add(new IntegerVariable("a1"));
        variables.add(new IntegerVariable("b1"));
        instance = new BDDStringParser(variables);
    }
    
    /**
     * Clears variable list
     *
    @After
    public void tearDown() {
        variables.clear();
        variables = null;
    }

    /**
     * Test of parse method, of class BDDStringParser.
     * Thi method assumes that the BDD is ONE constant.
     *
    @Test
    public void testConstantBDD() {
        String BDDString = "<>";
        int numberOfExamples = 1;
        List<String> expResult = new LinkedList<>();
        expResult.add("a1=0, b1=0");
        List<String> result = instance.parse(BDDString, numberOfExamples);
        assertEquals(expResult, result);
    }
    
    private List<String> buildExpectedResultForOneBlockTest() {
        List<String> expResult = new LinkedList<>();
        
        //000
        Integer a1Expected = -251658241;
        Integer b1Expected = -4;
        expResult.add("a1=" + a1Expected.toString() + ", b1=" + b1Expected.toString());
        
        
        //001
        a1Expected = -251658241;
        b1Expected = -3;
        expResult.add("a1=" + a1Expected.toString() + ", b1=" + b1Expected.toString());
        
        //010
        a1Expected = -218103809;
        b1Expected = -4;
        expResult.add("a1=" + a1Expected.toString() + ", b1=" + b1Expected.toString());
        
        //011
        a1Expected = -218103809;
        b1Expected = -3;
        expResult.add("a1=" + a1Expected.toString() + ", b1=" + b1Expected.toString());
        
        //100
        a1Expected = -117440513;
        b1Expected = -4;
        expResult.add("a1=" + a1Expected.toString() + ", b1=" + b1Expected.toString());
        
        //101
        a1Expected = -117440513;
        b1Expected = -3;
        expResult.add("a1=" + a1Expected.toString() + ", b1=" + b1Expected.toString());
        
        //110
        a1Expected = -83886081;
        b1Expected = -4;
        expResult.add("a1=" + a1Expected.toString() + ", b1=" + b1Expected.toString());
        
        //111
        a1Expected = -83886081;
        b1Expected = -3;
        expResult.add("a1=" + a1Expected.toString() + ", b1=" + b1Expected.toString());
        
        return expResult;
    }
    
    /**
     * Test of parse method, of class BDDStringParser.
     * This method assumes that the BDD is not a constant, has three free variables 
     * and consists of only one block (string like <...>).
     *
    @Test
    public void testOneBlockBDD() {
        int numberOfExamples = 20;
        String BDDString = "<0:1, 1:1, 2:1, 3:1, 5:0, 7:0, 8:1, ";
        for (int i = 9; i < 62; i++) {
            BDDString += i;
            BDDString += ":1, ";
        }
        BDDString += "62:0>";
        List<String> result = instance.parse(BDDString, numberOfExamples);
        assertEquals(buildExpectedResultForOneBlockTest(), result);
    }
    
    /**
     * Test of parse method, of class BDDStringParse.
     * This method assumes thar the BDD is not a constant, has four blocks 
     * and some free variables.
     *
    @Test
    public void testMultiBlockBDD() {
        int numberOfExamples = 30;
        String BDDString = "<0:1, 1:1, 2:1, 3:1, 5:0, 7:0, 8:1, ";
        for (int i = 9; i < 62; i++) {
            BDDString += i;
            BDDString += ":1, ";
        }
        BDDString += "62:0>";
        
        BDDString += "<0:0, ";
        for (int i = 1; i < 63; i++) {
            BDDString += i;
            BDDString += ":1, ";
        }
        BDDString += "63:1>";
        
        BDDString += "<0:1, ";
        for (int i = 1; i < 63; i++) {
            BDDString += i;
            BDDString += ":0, ";
        }
        BDDString += "63:0>";
        
        BDDString += "<0:0, ";
        for (int i = 1; i < 62; i++) {
            BDDString += i;
            BDDString += ":0, ";
        }
        BDDString += "63:0>";
        
        List<String> expResult = buildExpectedResultForOneBlockTest();
        expResult.add("a1=" + Integer.MAX_VALUE + ", b1=-1");
        expResult.add("a1=" + Integer.MIN_VALUE + ", b1=0");
        expResult.add("a1=0, b1=0");
        expResult.add("a1=0, b1=2");
        List<String> result = instance.parse(BDDString, numberOfExamples);
        assertEquals(expResult, result);
    }
}
*/
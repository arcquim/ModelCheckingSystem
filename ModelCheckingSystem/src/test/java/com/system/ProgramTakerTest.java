package com.system;

import java.util.LinkedList;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Alexander Erin <arcquim@gmail.com>
 */
public class ProgramTakerTest {
    
    public ProgramTakerTest() {
    }

    /**
     * Test of splitProgram method, of class ProgramTaker.
     */
    @Test
    public void testSplitProgram() {
        String program = "a = 12;\n if (a == 3) {\n a = a + a; \n} \n else {\n a = 2; a; \n} \n";
        List<String> expResult = new LinkedList<>();
        expResult.add("a = 12");
        expResult.add("if (a == 3)");
        expResult.add("{");
        expResult.add("a = a + a");
        expResult.add("");
        expResult.add("}");
        expResult.add("else");
        expResult.add("{");
        expResult.add("a = 2");
        expResult.add("a");
        expResult.add("");
        expResult.add("}");
        expResult.add("");
        ProgramTaker programTaker = new ProgramTaker(program);
        List<String> result = programTaker.splitProgram();
        assertEquals(expResult, result);
    }
    
}

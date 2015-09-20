package com.system;

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Alexander Erin <arcquim@gmail.com>
 */
public class ControllerTest {
    
    public ControllerTest() {
    }

    /**
     * Test of startVerification method, of class Controller.
     */
    @Test
    public void testStartVerification() {
        Controller instance = new Controller();
        instance.setProgram("int a; int b; read(a); b = a");
        List<String> atomicPredicates = new ArrayList<>();
        atomicPredicates.add("a != b");
        instance.setAtomicPredicates(atomicPredicates);
        instance.setCTLFormula("EF 0");
        CTLVerificator verificator = instance.startVerification();
        VerificationResult result = verificator.getVerificationResult();
        VerificationResult expResult = VerificationResult.PROPERTY_NOT_HOLDS;
        assertEquals(expResult, result);
    }
    
}

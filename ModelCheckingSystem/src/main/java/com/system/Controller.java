package com.system;

import com.system.kripkestructure.AtomicPredicate;
import com.system.kripkestructure.KripkeStructureTranslator;
import com.system.kripkestructure.ProgramCounterVariable;
import com.system.kripkestructure.Variable;
import com.system.temporallogic.CTLTranslator;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import net.sf.javabdd.BDD;

/**
 *
 * @author Alexander Erin <arcquim@gmail.com>
 */
public class Controller {
    
    private String program;
    private List<String> conditions;
    private List<AtomicPredicate> atomicPredicates;
    private String formula;
    
    /**
     *
     * @param program String representing program written in a simple C-like 
     * language; it has only <i>int</i> variables, <i>if</i> and <i>if-else</i> 
     * statements, <i>while</i> statement, assign statement (like a = b * 6), 
     * <i>read</i> operator (for example, read(i)).
     */
    public void setProgram(String program) {
        if (program == null) {
            throw new NullPointerException();
        }
        if (program.isEmpty()) {
            throw new IllegalArgumentException();
        }
        this.program = program;
    }
    
    /**
     *
     * @param conditions Atomic predicates taking part in CTL formula; for 
     * example, if there are variables a and b, then a + b >= b* b - 7 is atomic 
     * predicate; it is equivalent to condition in <i>if</i> or <i>while</i>.
     */
    public void setAtomicPredicates(List<String> conditions) {
        if (conditions == null) {
            throw new NullPointerException();
        }
        if (conditions.isEmpty()) {
            throw new IllegalArgumentException();
        }
        this.conditions = conditions;
    }
    
    /**
     *
     * @param formula CTL formula to verify.
     */
    public void setCTLFormula(String formula) {
        if (formula == null) {
            throw new NullPointerException();
        }
        if (formula.isEmpty()) {
            throw new IllegalArgumentException();
        }
        this.formula = formula;
    }
    
    /**
     *
     * @return CTLVerificator instance, if there was no errors during 
     * translation; null otherwise.
     * @throws IllegalArgumentException if arguments passed on setters are not
     * correct.
     */
    public CTLVerificator startVerification() {
        try {
            CTLVerificator verificator = new CTLVerificator();
            ProgramTaker programTaker = new ProgramTaker(program);
            List<Variable> variables = programTaker.getProgramVariables();
            List<String> operators = programTaker.splitProgram();
            if (variables == null || operators == null) {
                return null;
            }
            atomicPredicates = new LinkedList<>();
            for (int i = 0; i < conditions.size(); i++) {
                atomicPredicates.add(new AtomicPredicate(conditions.get(i)));
            }
            KripkeStructureTranslator structureTranslator = new 
                KripkeStructureTranslator(operators, variables, atomicPredicates);

            if (!structureTranslator.tryTranslate()) {
                return null;
            }

            CTLTranslator translator = new CTLTranslator(formula);
            List<BDD> atomicPredicatesBDD = new ArrayList<>();
            for (int i = 0; i < atomicPredicates.size(); i++) {
                atomicPredicatesBDD.add(atomicPredicates.get(i).getPredicateBDD());
            }
            translator.setAtomicPredicates(atomicPredicatesBDD);
            translator.setStatesTransitions(structureTranslator.getStatesTransitionBDD());
            verificator.setPropertyBDD(translator.getBDDResult());
            verificator.setStartStatesBDD(structureTranslator.getStartStatesBDD());
            variables.add(0, new ProgramCounterVariable("pc"));
            verificator.setVariables(variables);
            return verificator;
        }
        catch (IllegalArgumentException | NullPointerException ex) {
            return null;
        }
    }
}

package com.system.temporallogic;

import java.util.List;
import net.sf.javabdd.BDD;

/**
 *
 * @author Alexander Erin <arcquim@gmail.com>
 */
public interface TemporalLogicTranslator {
    
    /**
     *
     * @return BDD representing the characteristic function on Kripke structure 
     * states of this temporal logic formula.
     */
    BDD getBDDResult();

    /**
     *
     * @param statesTransitionBDD BDD representing the characteristic function 
     * of transitions between states of Kripke structure.
     */
    void setStatesTransitions(BDD statesTransitionBDD);

    /**
     *
     * @param atomicPredicatesBDD List of BDD representing the characteristic 
     * function of states of Kripke structure for atomic predicates used in 
     * the temporal logic formula.
     */
    void setAtomicPredicates(List<BDD> atomicPredicatesBDD);
    
}

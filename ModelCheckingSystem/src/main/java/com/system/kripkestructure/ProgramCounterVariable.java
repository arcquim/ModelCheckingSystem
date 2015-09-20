package com.system.kripkestructure;

/**
 *
 * @author Alexander Erin <arcquim@gmail.com>
 */
public class ProgramCounterVariable implements Variable {
    
    private final String name;
    private static final VariableType TYPE = VariableType.PC;
    private static final int SIZE = 8;
    
    /**
     *
     * @param name A name of this IntegerVariable.
     */
    public ProgramCounterVariable(String name) {
        this.name = name;
    }

    /**
     *
     * @return A name of this ProgramCounterVariable.
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     *
     * @return VariableType.PC
     */
    @Override
    public VariableType getType() {
        return TYPE;
    }
    
    /**
     *
     * @return Number of bits for variable of <i>PC</i> type; it's 8 for now.
     */
    @Override
    public int getSize() {
        return SIZE;
    }
    
    @Override
    public int hashCode() {
        return name.hashCode();
    }
    
    @Override
    public boolean equals(Object another) {
        if (another == null || !(another instanceof IntegerVariable)) {
            return false;
        }
        return name.equals(((IntegerVariable)another).getName());
    }
    
}

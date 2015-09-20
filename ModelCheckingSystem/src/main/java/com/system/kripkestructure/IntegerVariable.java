package com.system.kripkestructure;

/**
 *
 * @author Alexander Erin <arcquim@gmail.com>
 */
public class IntegerVariable implements Variable {
    
    private final String name;
    private static final VariableType TYPE = VariableType.INTEGER;
    private static final int SIZE = 16;
    
    /**
     *
     * @param name A name of this IntegerVariable.
     */
    public IntegerVariable(String name) {
        this.name = name;
    }

    /**
     *
     * @return A name of this IntegerVariable.
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     *
     * @return VariableType.INTEGER
     */
    @Override
    public VariableType getType() {
        return TYPE;
    }
    
    /**
     *
     * @return Number of bits for variable of <i>int</i> type; it's 16 for now.
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

package com.system.kripkestructure;

/**
 *
 * @author Alexander Erin <arcquim@gmail.com>
 */
public interface Variable {
    
    /**
     *
     * @return Name of Variable instance.
     */
    String getName();

    /**
     *
     * @return Type of Variable instance; it contains in enum VariableType.
     */
    VariableType getType();

    /**
     *
     * @return Number of bits for a variable of its type.
     */
    int getSize();

    /**
     *
     * @return Hash code of this Variable.
     */
    @Override
    int hashCode();

    /**
     *
     * @param other An object to compare with.
     * @return True if other and this are the same variables (having the same 
     * type and the same name).
     */
    @Override
    boolean equals(Object other);
}

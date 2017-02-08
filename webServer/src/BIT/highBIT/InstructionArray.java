/* InstructionArray.java
 * Part of BIT -- Bytecode Instrumenting Tool
 *
 * Copyright (c) 1997, The Regents of the University of Colorado. All
 * Rights Reserved.
 * 
 * Permission to use and copy this software and its documentation for
 * NON-COMMERCIAL purposes and without fee is hereby granted provided
 * that this copyright notice appears in all copies. If you wish to use
 * or wish to have others use BIT for commercial purposes please contact,
 * Stephen V. O'Neil, Director, Office of Technology Transfer at the
 * University of Colorado at Boulder (303) 492-5647.
 *  
 * By downloading BIT, the User agrees and acknowledges that in no event
 * will the Regents of the University of Colorado be liable for any
 * damages including lost profits, lost savings or other indirect,
 * incidental, special or consequential damages arising out of the use or
 * inability to use the BIT software.
 * 
 * BIT was invented by Han Bok Lee at the University of Colorado in
 * Boulder, Colorado.
 */

package BIT.highBIT;
import java.util.Enumeration;

/**
 * Represents an array to hold instructions.
 * The reason for creating this class to represent instructions array
 * is that we want to hide as much as we can about the implementation 
 * to the user.  Also, walking through an array should be faster than
 * walking through a Vector.
 * <br>
 * 
 * @author  <a href="mailto:hanlee@cs.colorado.edu">Han B. Lee</a>
 * @see Instruction
 **/
public class InstructionArray {
    Instruction instructions[];

    /**
     * Create a new array given a Java array of instructions.
     *
     * @param   array size
     * @param   array of instructions
     */
    public InstructionArray(int size_, Instruction instructions_[]) {
        instructions = new Instruction[size_];

        for (int i = 0; i < size_; i++) {
            try {
                instructions[i] = (Instruction) instructions_[i].clone();
            } catch (CloneNotSupportedException e) {
            }
        }
    }

    /** 
     * Returns an enumerator for the elements in this array class.
     *
     * @return      new enumeration for the elements in this array
     * @see         java.util.Enumeration
     */
    public final synchronized Enumeration elements() {
	return new InstructionArrayEnumerator(instructions);
    }
    
    /**
     * Returns the instruction at ith index.
     *
     * @param       index of the wanted instruction
     * @return      the instruction at that index
     * @see         Instruction
     */
    public Instruction elementAt(int i) {
        return (Instruction) instructions[i];
    }
//cjk
    public void setElementAt(Instruction inst, int i) {
	instructions[i] = inst;
        return; 
    }

    /**
     * returns the actual array of instructions if user needs it.
     *
     * @return      array of instructions
     * @see         Instruction
     */
    public Instruction[] getInstructions() {
        return instructions;
    }

    /**
     * returns the size of this array.
     *
     * @return      an integer representing the size of this array.
     */
    public int size() {
        return instructions.length;
    }

    /**
     * return the first instruction.
     *
     * @return      the first element in the instructions array
     * @see         Instruction
     */
    public Instruction firstElement() {
        return (Instruction) instructions[0];
    }

    /**
     * Modifies the index of instructions in this array to reflect the
     * changes that resulted from adding instructions to another array.
     * For use by BIT internally.
     */
    public void updateModifiedIndex(int start_instr, int increment) {
        for (int i = start_instr; i < instructions.length; i++) {
            instructions[i].setModifiedIndex(instructions[i].getModifiedIndex() + increment);
        }
    }
}

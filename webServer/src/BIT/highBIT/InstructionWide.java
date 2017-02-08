/* InstructionWide.java
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

import BIT.lowBIT.*;
import java.io.*;

/**
 * Represents wide instrucion.
 * 
 * @author  <a href="mailto:hanlee@cs.colorado.edu">Han B. Lee</a>
 * @see Instruction
 **/
public class InstructionWide extends Instruction {
    
    /** 
     * Represents the <opcode> following wide opcode.
     */
    protected int op;
    
    /**
     * Represents the upper byte of the index to a local variable.
     */
    protected int indexbyte1;
    
    /**
     * Represents the lower byte of the index to a local variable.
     */
    protected int indexbyte2;
    
    /**
     * Represents the upper byte of the constant if <opcode> is iinc.
     * Otherwise, this variable is unused.
     */
    protected int constbyte1;
    
    /**
     * Represents the lower byte of the constant if <opcode> is iinc.
     * Otherwise, this variable is unused.
     */
    protected int constbyte2;
    
    
    /**
     * Constructor for InstructionWide class.
     *
     * @param	opcode	the opcode of this instruction
     * @param	iStream	the data input stream
     */
    public InstructionWide(int opcode, DataInputStream iStream, int offset, Routine routine) {
        super(opcode, offset, routine);
        try {
            op = iStream.readUnsignedByte();
            indexbyte1 = iStream.readUnsignedByte();
            indexbyte2 = iStream.readUnsignedByte();
            
    	    /*if(System.getProperty("DEBUGROUTINE") != null) {
			System.err.println(routine.getMethodName()
				+ ": Wide instruction opcode: "
 				+ op + " index: " 
				+ ((indexbyte1 << 8) | indexbyte2));
	    }*/
//bug fix cjk 8/31/99 op below was opcode which was incorrect
            if (op == InstructionTable.iinc) {
                constbyte1 = iStream.readUnsignedByte();
                constbyte2 = iStream.readUnsignedByte();
    	        /*if(System.getProperty("DEBUGROUTINE") != null) {
			System.err.println("Wide instruction is inc: "
				+ ((constbyte1 << 8) | constbyte2));
		}*/
            }
        }
        catch (IOException e) {
            System.out.println("Error reading code buffer");
        }
    }
    
    /**
     * Returns the type of this instruction.
     */
    public short getInstructionType() {
        return InstructionTable.InstructionTypeTable[op];
    }
    
    /**
	     * Returns the length (in bytes) of this instruction including opcode.
	     */
	    public int getLength() {
        if (op == InstructionTable.iinc) 
            return 6;
        else
            return 4;
    }
//cjk bug the following was missing
    /*public void write(DataOutputStream oStream) {
	super.write(oStream);
        try {
            oStream.writeByte(op);
		System.err.println(routine.getMethodName() 
			+ ": Writing wide inst: " + op + " opcode "
			+ opcode);
            oStream.writeByte(indexbyte1);
            oStream.writeByte(indexbyte2);
            if (op == InstructionTable.iinc) {
		System.err.println("Writing iinc wide inst: " + op + " opcode "
			+ opcode);
            	oStream.writeByte(constbyte1);
            	oStream.writeByte(constbyte2);
	    }
        } catch (IOException e) {
            System.out.println("Error writing to code buffer");
        }
    }*/


}

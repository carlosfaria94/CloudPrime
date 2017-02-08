/* InstructionQuadOperand.java
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

import java.io.*;

/**
* Represents a Java Virtual Machine instrucion that takes four operands.
* 
* @author  <a href="mailto:hanlee@cs.colorado.edu">Han B. Lee</a>
* @see Instruction
**/
public class InstructionQuadOperand extends Instruction {
        /** 
         * Represents the first operand.
	 */
	int operand1;
	
	/**
         * Represents the second operand.
	 */
	int operand2;
	
	/**
         * Represents the third operand.
	 */
	int operand3;
	
	/**
         * Represents the fourth operand.
	 */
	int operand4;
	
	/**
	 * Constructor for InstructionQuadOperand class.
	 *
	 * @param	opcode	the opcode of this instruction
	 * @param	iStream	the data input stream where the operands are gotten from
	 */
	public InstructionQuadOperand(int opcode, DataInputStream iStream, int offset, Routine routine) {
		super(opcode, offset, routine);
		try {
			this.operand1 = iStream.readUnsignedByte();
			this.operand2 = iStream.readUnsignedByte();
			this.operand3 = iStream.readUnsignedByte();
			this.operand4 = iStream.readUnsignedByte();
		}
		catch (IOException e) {
			System.out.println("Error reading from code buffer");
		}
	}
	
	public void write(DataOutputStream oStream) {
		super.write(oStream);
		try {
			oStream.writeByte(operand1);
			oStream.writeByte(operand2);
			oStream.writeByte(operand3);
			oStream.writeByte(operand4);
		}
		catch(IOException e) {
			System.out.println("Error writing to code buffer");
		}
	}
	
	
	/**
	 * Returns the 32 bit value resulting from operand1, operand2, operand3
	 * and operand4
	 */
	public int getOperandValue() {
		return ((operand1 << 24) | (operand2 << 16) |
			(operand3 << 8) | operand4);
	}
	public void setCpoolIndex(int value) {
    		operand1 = (value >>> 8) & 0xff;
    		operand2 = (value >>> 0) & 0xff;

	}
	
	
	/* Returns the index into the constant pool that points to the 
	 * CONSTANT_InterfaceMethodRef_Info entry for this interface call 
	 * invokeinterface 2bytes 2bytes: the first is for the index into the cpool
	 * the second is for the number 0-255 (the number of stack elements pushed by caller)
	 */
	public int getDoubleOperandValue() {
		return (int) (short)((operand1 << 8) | operand2);
	}
	
	/**
 	 * Returns the length (in bytes) of this instruction including opcode.
	 */
	public int getLength() {
		return 5;
	}
}

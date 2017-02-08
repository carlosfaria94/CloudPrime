/* InstructionSingleOperand.java
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
* Represents a Java Virtual Machine instrucion that takes one operand.
* 
* @author  <a href="mailto:hanlee@cs.colorado.edu">Han B. Lee</a>
* @see Instruction
**/
public class InstructionSingleOperand extends Instruction {
  /** Represents the first and only operand.
   */
  int operand1;
  
  /**
   * Constructor for InstructionSingleOperand class.
   *
   * @param	opcode	the opcode of this instruction
   * @param	iStream	the data input stream where the operands are gotten from
   */
  public InstructionSingleOperand(int opcode, DataInputStream iStream, int offset, Routine routine) {
    super(opcode, offset, routine);
    try {
      operand1 = iStream.readUnsignedByte();
    }
    catch(IOException e) {
      System.out.println("Error reading code buffer");
    }
  }

  public InstructionSingleOperand(int opcode, byte operand1, Routine routine) {
    super(opcode, routine);
    this.operand1 = operand1;
  }
  
  public void write(DataOutputStream oStream) {
    super.write(oStream);
    try {
      oStream.writeByte(operand1);
    }
    catch(IOException e) {
      System.out.println("Error writing to code buffer");
    }
  }

  /**
   * Returns the 8 bit value resulting from operand1.
   */

  public int getOperandValue() {
    return operand1;
  }
  public void setCpoolIndex(int value) {
    operand1 = (value >>> 0) & 0xff;  //just to be consistent with the other subclasses
  }

  /**
   * Returns the length (in bytes) of this instruction including opcode.
   */
  public int getLength() {
    return 2;
  }  
}

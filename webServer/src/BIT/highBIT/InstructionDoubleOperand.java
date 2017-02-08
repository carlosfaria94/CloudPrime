/* InstructionDoubleOperand.java
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
* Represents a Java Virtual Machine instrucion that takes two operands.
* 
* @author  <a href="mailto:hanlee@cs.colorado.edu">Han B. Lee</a>
* @see Instruction
**/
public class InstructionDoubleOperand extends Instruction {
  
  /**
   * Represents the first operand.
   */
  int operand1;
  /**
   * Represents the second operand.
   */
  int operand2;
  /**
   * Constructor for InstructionDoubleOperand class.
   *
   * @param	opcode	the opcode of this instruction
   * @param	iStream	the data input stream where the operands are gotten from
   */
  public InstructionDoubleOperand(int opcode, DataInputStream iStream, int offset, Routine routine) {
    super(opcode, offset, routine);
    try {
      operand1 = iStream.readUnsignedByte();
      operand2 = iStream.readUnsignedByte();
    }
    catch (IOException e) {
      System.out.println("Error reading from code buffer");
    }
  }

  public InstructionDoubleOperand(int opcode, short operand, Routine routine) {
    super(opcode, routine);
    operand1 = (operand >>> 8) & 0xff;  //shift right 8 with 0 extension (not sign)
    operand2 = (operand >>> 0) & 0xff;  //shift right 0 with 0 extension (not sign)
	// & 0xff ensures that the top 8 bits of each operand are 0'd out
  }

  public void write(DataOutputStream oStream) {
    super.write(oStream);
    try {
      oStream.writeByte(operand1);
      oStream.writeByte(operand2);
    }
    catch(IOException e) {
      System.out.println("Error writing to code buffer");
    }
  }

  /**
   * Returns the 16 bit value resulting from operand1 and operand2.
   */

  public int getOperandValue() {
	//shift operand1 to the left 8 then add in operand 2 to the low end
    return (int) (short)((operand1 << 8) | operand2);
  }

  /**
   * Sets the 16 bit value arg to operand1 and operand2.
   */
  public void setOperandValue(int value) {
    operand1 = (value >>> 8) & 0xff;
    operand2 = (value >>> 0) & 0xff;
  }
//cjk
  public void setCpoolIndex(int value) {
	System.err.println("Correctly executing in child");
    operand1 = (value >>> 8) & 0xff;
    operand2 = (value >>> 0) & 0xff;
  }

  /**
   * Returns the length (in bytes) of this instruction including opcode.
   */
  public int getLength() {
    return 3;
  }
  
}

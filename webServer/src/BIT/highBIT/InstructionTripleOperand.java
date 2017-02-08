/* InstructionTripleOperand.java
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
* Represents a Java Virtual Machine instrucion that takes three operands.
* 
* @author  <a href="mailto:hanlee@cs.colorado.edu">Han B. Lee</a>
* @see Instruction
**/
public class InstructionTripleOperand extends Instruction {
  
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
   * Constructor for InstructionTripleOperand class.
   *
   * @param	opcode	the opcode of this instruction
   * @param	iStream	the data input stream where the operands are gotten from
   */
  public InstructionTripleOperand(int opcode, DataInputStream iStream, int offset, Routine routine) {
    super(opcode, offset, routine);
    try {
      operand1 = iStream.readUnsignedByte();
      operand2 = iStream.readUnsignedByte();
      operand3 = iStream.readUnsignedByte();
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
    }
    catch(IOException e) {
      System.out.println("Error writing to code buffer");
    }
  }

  public int getOperand(int i) {
      switch (i) {
      case 1: return operand1;
      case 2: return operand2;
      case 3: return operand3;
      default: 
          System.out.println("index has to be between 1 and 3");
          break;
      }
      return -1;
  }

  /**
   * Returns the length (in bytes) of this instruction including opcode.
   */
  public int getLength() {
    return 4;
  }
  
  /* returns the index (multinewarray) into the cpool at 
   * which the CONSTANT_Class_Info is 
   * operand 3 is the 1byte that holds 0-255 the number of dimensions
   */
  public int getOperandValue() {
    return (int) (short)((operand1 << 8) | operand2);
  }
  public void setCpoolIndex(int value) {
    operand1 = (value >>> 8) & 0xff;
    operand2 = (value >>> 0) & 0xff;
  }
}

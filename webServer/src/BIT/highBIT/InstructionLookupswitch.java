/* InstructionLookupswitch.java
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
* Represents lookupswitch JVM instruction.
* 
* @author  <a href="mailto:hanlee@cs.colorado.edu">Han B. Lee</a>
* @see Instruction
**/
public class InstructionLookupswitch extends Instruction {
  /**
   * Represents the number of pads in the instruction.
   *
   * This number is chosen so that the following byte begins
   * at an address that is a multiple of four bytes from the start
   * of code buffer.
   */
  int padNumber;
  
   /**
    * defaultbyte1 through defaultbyte4 form a signed 32-bit default value.
    */
  int defaultbyte1;
  int defaultbyte2;
  int defaultbyte3;
  int defaultbyte4;
  
  /**
   * npairs1 through npairs4 form a signed 32-bit pairs value.
   */
  int npairs1;
  int npairs2;
  int npairs3;
  int npairs4;
  
  /**
   * Represents the array of match offsets
   */
  int matches[];
  int offsets[];

  int match1[];
  int match2[];
  int match3[];
  int match4[];

  int offset1[];
  int offset2[];
  int offset3[];
  int offset4[];

  int def;
  int npairs;

  /**
   * Constructor for InstructionLookupswitch class.
   *
   * @param	opcode	the opcode of this instruction
   * @param	iStream	the data input stream where the operands are gotten from
   * @param	byte_count represents the number of bytes read so far from the
   * beginning of the code buffer (this is needed to compute number of pads)
   */
  public InstructionLookupswitch(int opcode, DataInputStream iStream, int offset, Routine routine) {
    super(opcode, offset, routine);
    padNumber = 3 - (offset % 4);
    try {
      for (int i = 0; i < padNumber; i++) {
        byte dummy = (byte) iStream.readUnsignedByte();
      }
      defaultbyte1 = iStream.readUnsignedByte();
      defaultbyte2 = iStream.readUnsignedByte();
      defaultbyte3 = iStream.readUnsignedByte();
      defaultbyte4 = iStream.readUnsignedByte();
      npairs1 = iStream.readUnsignedByte();
      npairs2 = iStream.readUnsignedByte();
      npairs3 = iStream.readUnsignedByte();
      npairs4 = iStream.readUnsignedByte();

      def = (defaultbyte1 << 24) | (defaultbyte2 << 16) |
        (defaultbyte3 << 8) | defaultbyte4;
      npairs = (npairs1 << 24) | (npairs2 << 16) | 
        (npairs3 << 8) | npairs4;
      matches = new int[npairs];
      offsets = new int[npairs];
      match1 = new int[npairs];
      match2 = new int[npairs];
      match3 = new int[npairs];
      match4 = new int[npairs];
      offset1 = new int[npairs];
      offset2 = new int[npairs];
      offset3 = new int[npairs];
      offset4 = new int[npairs];

      for (int i = 0; i < npairs; i++) {
        match1[i] = iStream.readUnsignedByte();
        match2[i] = iStream.readUnsignedByte();
        match3[i] = iStream.readUnsignedByte();
        match4[i] = iStream.readUnsignedByte();
        offset1[i] = iStream.readUnsignedByte();
        offset2[i] = iStream.readUnsignedByte();
        offset3[i] = iStream.readUnsignedByte();
        offset4[i] = iStream.readUnsignedByte();

        matches[i] = (match1[i] << 24) | (match2[i] << 16) |
          (match3[i] << 8) | match4[i];
        offsets[i] = (offset1[i] << 24) | (offset2[i] << 16) |
          (offset3[i] << 8) | offset4[i];
      }
    }
    catch (IOException e) {
      System.out.println("Error reading code buffer");
    }
  }
  
  public void write(DataOutputStream oStream) {
    super.write(oStream);
    try {
      for (int i = 0; i < padNumber; i++)
        oStream.writeByte(0);
      oStream.writeByte(defaultbyte1);
      oStream.writeByte(defaultbyte2);
      oStream.writeByte(defaultbyte3);
      oStream.writeByte(defaultbyte4);
      oStream.writeByte(npairs1);
      oStream.writeByte(npairs2);
      oStream.writeByte(npairs3);
      oStream.writeByte(npairs4);
      
      for (int i = 0; i < npairs; i++) {
        oStream.writeByte(match1[i]);
        oStream.writeByte(match2[i]);
        oStream.writeByte(match3[i]);
        oStream.writeByte(match4[i]);
        
        oStream.writeByte(offset1[i]);
        oStream.writeByte(offset2[i]);
        oStream.writeByte(offset3[i]);
        oStream.writeByte(offset4[i]);
      }
    }
    catch(IOException e) {
      System.out.println("Error writing to code buffer");
    }
  }
  
  /**
   * Returns the length (in bytes) of this instruction including opcode.
   */
  public int getLength() {
    return (9 + npairs * 8 + padNumber);
  }

  public int [] getOffsets() {
    return offsets;
  }

  public int getNpairs() {
    return npairs;
  }
  
  public int getDefault() {
    return def;
  }

  public void setDefault(int def) {
    this.def = def;
    defaultbyte1 = (def >>> 24) & 0xff;
    defaultbyte2 = (def >>> 16) & 0xff;
    defaultbyte3 = (def >>> 8) & 0xff;
    defaultbyte4 = (def >>>  0) & 0xff;
  }
}

/* InstructionTableswitch.java
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
* Represents tableswitch JVM instruction.
* 
* @author  <a href="mailto:hanlee@cs.colorado.edu">Han B. Lee</a>
* @see Instruction
**/
public class InstructionTableswitch extends Instruction {
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
   * lowbyte1 through lowbyte4 form a signed 32-bit low value.
   */
  int lowbyte1;
  int lowbyte2;
  int lowbyte3;
  int lowbyte4;
  
  /**
   * high1 through high4 form a signed 32-bit high value.
   */
  int highbyte1;
  int highbyte2;
  int highbyte3;
  int highbyte4;
  
  /**
   * Represents the array of jump offsets
   */
  int jump_offsets[];

  int offset1[], offset2[], offset3[], offset4[];

  int def;
  int low;
  int high;
  /**
   * Constructor for Instructiontableswitch class.
   *
   * @param	opcode	the opcode of this instruction
   * @param	iStream	the data input stream where the operands are gotten from
   * @param	byte_count represents the number of bytes read so far from the
   * beginning of the code buffer (this is needed to compute number of pads)
   */
  public InstructionTableswitch(int opcode, DataInputStream iStream, int offset, Routine routine) {
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
      
      lowbyte1 = iStream.readUnsignedByte();
      lowbyte2 = iStream.readUnsignedByte();
      lowbyte3 = iStream.readUnsignedByte();
      lowbyte4 = iStream.readUnsignedByte();
      
      highbyte1 = iStream.readUnsignedByte();
      highbyte2 = iStream.readUnsignedByte();
      highbyte3 = iStream.readUnsignedByte();
      highbyte4 = iStream.readUnsignedByte();
      
      def = (defaultbyte1 << 24) | (defaultbyte2 << 16) |
        (defaultbyte3 << 8) | defaultbyte4;
      high = (highbyte1 << 24) | (highbyte2 << 16) | 
        (highbyte3 << 8) | highbyte4;
      low = (lowbyte1 << 24) | (lowbyte2 << 16) | 
        (lowbyte3 << 8) | lowbyte4;
      jump_offsets = new int[(high - low + 1)];
      offset1 = new int[(high - low + 1)];
      offset2 = new int[(high - low + 1)];
      offset3 = new int[(high - low + 1)];
      offset4 = new int[(high - low + 1)];

      for (int i = 0; i < (high - low + 1); i++) {
        offset1[i] = iStream.readUnsignedByte();
        offset2[i] = iStream.readUnsignedByte();
        offset3[i] = iStream.readUnsignedByte();
        offset4[i] = iStream.readUnsignedByte();

        jump_offsets[i] = (offset1[i] << 24) | (offset2[i] << 16) |
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
      
      oStream.writeByte(lowbyte1);
      oStream.writeByte(lowbyte2);
      oStream.writeByte(lowbyte3);
      oStream.writeByte(lowbyte4);
      
      oStream.writeByte(highbyte1);
      oStream.writeByte(highbyte2);
      oStream.writeByte(highbyte3);
      oStream.writeByte(highbyte4);
      
      for (int i = 0; i < (high - low + 1); i++) {
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
    return (13 + jump_offsets.length * 4 + padNumber);
  }

  public int getJumpOffsetCount() {
    return (high - low + 1);
  }

  public int [] getJumpOffsets() {
    return jump_offsets;
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

  public int getLow() {
    return low;
  }

  public int getHigh() {
    return high;
  }
//Added for size routines/other -cjk
  public int [] getOffsets() { 
    return jump_offsets;
  }
}

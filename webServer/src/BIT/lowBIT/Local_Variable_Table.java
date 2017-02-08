/* Local_Variable_Table.java
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


package BIT.lowBIT;

import java.io.*;

public class Local_Variable_Table {
  // data members
  
  // start_pc is an index into the code array where this local
  //   variable starts
  public short start_pc;
  
  // length is the length of this local variable, so the local
  //   variable starts at start_pc and ends at start_pc + length
  public short length;
  
  // name_index is an index into the constant_pool table and 
  //   indicates the name of this local varaible
  public short name_index;
  
  // descriptor_index is an index into the constant_pool table and
  //   indicates the local variable descriptor
  public short descriptor_index;
  
  // this local variable has to be at index in its method's local
  //   variables.
  public short index;
  
  // constructor
  public Local_Variable_Table(DataInputStream iStream) throws IOException {
    start_pc = (short) iStream.readUnsignedShort();
    length = (short) iStream.readUnsignedShort();
    name_index = (short) iStream.readUnsignedShort();
    descriptor_index = (short) iStream.readUnsignedShort();
    index = (short) iStream.readUnsignedShort();
  }

  public void write(DataOutputStream oStream) throws IOException {
    oStream.writeShort((int) start_pc);
    oStream.writeShort((int) length);
    oStream.writeShort((int) name_index);
    oStream.writeShort((int) descriptor_index);
    oStream.writeShort((int) index);
  }
}

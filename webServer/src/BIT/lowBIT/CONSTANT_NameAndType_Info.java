/* CONSTANT_NameAndType_Info.java
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

public class CONSTANT_NameAndType_Info extends Cp_Info {
  // data member
  // index into a Java field name or method name in the constant pool table
  public short name_index;
  
  // index into a Java field descriptor or method descriptor
  // in the constant pool table
  public short descriptor_index;
  
  // constructor
  public CONSTANT_NameAndType_Info(DataInputStream iStream, byte tag) 
    throws IOException {
    this.tag = tag;
    name_index = (short) iStream.readUnsignedShort();
    descriptor_index = (short) iStream.readUnsignedShort();
  }

  public CONSTANT_NameAndType_Info(short name_index, short descriptor_index) {
    this.tag = Constant.CONSTANT_NameAndType;
    this.name_index = name_index;
    this.descriptor_index = descriptor_index;
  }

  public void write(DataOutputStream oStream)
    throws IOException {
      oStream.writeByte((int) tag);
      oStream.writeShort((int) name_index);
      oStream.writeShort((int) descriptor_index);
  }

  public boolean equals(Object obj) {
    if (obj instanceof CONSTANT_NameAndType_Info) {
      return ((name_index == ((CONSTANT_NameAndType_Info)obj).name_index) && 
              (descriptor_index == ((CONSTANT_NameAndType_Info)obj).descriptor_index));
    }
    return false;
    
  }
  public int size() { return 5; }
}

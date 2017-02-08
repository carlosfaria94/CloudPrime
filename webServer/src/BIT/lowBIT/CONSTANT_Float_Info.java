/* CONSTANT_Float_Info.java
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

public class CONSTANT_Float_Info extends Cp_Info {
  // data member
  // bytes contains the value of the float constant
  public int bytes;
  
  // constructor
  public CONSTANT_Float_Info(DataInputStream iStream, byte tag)
    throws IOException {
    this.tag = tag;
    bytes = iStream.readInt();
  }

  public void write(DataOutputStream oStream)
    throws IOException {
      oStream.writeByte((int) tag);
      oStream.writeInt(bytes);
  }
  public int size() { return 5; }
}


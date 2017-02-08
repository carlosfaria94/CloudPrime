/* CONSTANT_Utf8_Info.java
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

public class CONSTANT_Utf8_Info extends Cp_Info {
  // data member
  // length gives the number of bytes in the bytes array
  public int length;
  
  // bytes array contains the bytes of the string
  public byte bytes[];
  
  // constructor
  public CONSTANT_Utf8_Info(DataInputStream iStream, byte tag)
    throws IOException {
    Integer b1, b2;
    this.tag = tag;
    length = iStream.readUnsignedShort();
    bytes = new byte[length];
    iStream.readFully(bytes);
  }

  public CONSTANT_Utf8_Info(byte[] b) {
    tag = Constant.CONSTANT_Utf8;
    bytes = new byte[b.length];
    if (b.length > Short.MAX_VALUE) { //max val a short can be
	System.err.println("ERROR: CONSTANT_Utf8_Info - overflow: " + b.length + 
		" must be less than maximum short value: " + Short.MAX_VALUE);
	System.err.flush();
	System.exit(1);
    }
    length = b.length;
    System.arraycopy(b,0,bytes,0,length);
  }
    
  public CONSTANT_Utf8_Info(String s) {
    this.tag = Constant.CONSTANT_Utf8;
  	int slength = s.length();
	  byte temp[] = new byte[slength * 3];
    length = 0;
  	for (int i = 0; i < slength; i++) {
      int c = s.charAt(i);
      if ((c >= 0x0001) && (c <= 0x007f)) {
        temp[length++] = (byte) c;
      } else if (c > 0x07ff) {
        temp[length++] = (byte) (0xe0 | ((c >> 12) & 0x0f));
        temp[length++] = (byte) (0x80 | ((c >> 6) & 0x3f));
        temp[length++] = (byte) (0x80 | ((c >> 0) & 0x3f));
      } else {
        temp[length++] = (byte) (0xc0 | ((c >> 6) & 0x1f));
        temp[length++] = (byte) (0x80 | ((c >> 0) & 0x3f));
      }
    }

    bytes = new byte[length];
    System.arraycopy(temp, 0, bytes, 0, length);
  }
	  
  public void write(DataOutputStream oStream)
    throws IOException {
      oStream.writeByte((int) tag);
      oStream.writeShort(length);
      oStream.write(bytes, 0, length);
  }

  public boolean equals(Object obj) {
    if (obj instanceof CONSTANT_Utf8_Info) {
      if (bytes.length != ((CONSTANT_Utf8_Info) obj).bytes.length)
        return false;
      for (int i = 0; i < bytes.length; i++) {
        if (bytes[i] != ((CONSTANT_Utf8_Info) obj).bytes[i])
          return false;
      }
      return true;
    }
    return false;
  }
  public int size() { return(3 + this.length); }
}



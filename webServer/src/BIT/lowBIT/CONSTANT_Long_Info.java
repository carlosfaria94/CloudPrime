/* CONSTANT_Long_Info.java
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

public class CONSTANT_Long_Info extends Cp_Info {
  // data member
  // the two fields high_bytes and low_bytes together
  // contain the value of the long constant
  public int high_bytes;
  public int low_bytes;
  
  // constructor
  public CONSTANT_Long_Info(DataInputStream iStream, byte tag) 
    throws IOException {
    this.tag = tag;
    high_bytes = iStream.readInt();
    low_bytes = iStream.readInt();
  }

  public void write(DataOutputStream oStream)
    throws IOException {
      oStream.writeByte((int) tag);
      oStream.writeInt(high_bytes);
      oStream.writeInt(low_bytes);
  }
  public int size() { return 9; }
  public long getValue() { 

/*  I don't know why the following doesn't work - I get -1 with Long.MAX_VALUE
	long full;
        full = high_bytes;                      //  assign data into big storage 
        full = full << 32;                    // shift it left 32 bits 
		//the following gets a numeric overflow by the compiler
		//so does 0x7FFFFFFF00000000;  which is MAX_VALUE
        //full = full & 0xFFFFFFFF00000000;     // paranoid -- make sure right bits  are 0
                                        // are zero 
        full = full | low_bytes;         
	return(full);
*/

	String high = Integer.toBinaryString(high_bytes);
	String low = Integer.toBinaryString(low_bytes);
	System.err.println("high: " + high + " low: " + low);
	long val = 0;
	int mult = 0;
	for (int i = (low.length()-1); i >= 0; i--) {
		if (low.charAt(i) == '1') val += (Math.round(Math.pow(2,mult)));
		mult++;
	}
	for (int i = (high.length()-1); i >= 0; i--) {
		if (high.charAt(i) == '1') val += (Math.round(Math.pow(2,mult)));
		mult++;
	}
	return(val);
  }
}



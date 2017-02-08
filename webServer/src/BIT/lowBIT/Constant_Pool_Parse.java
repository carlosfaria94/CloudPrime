/* Constant_Pool_Parse.java
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

public class Constant_Pool_Parse {
  public Constant_Pool_Parse(Cp_Info[] constant_pool, DataInputStream iStream) 
    throws IOException {
    try {
      for (int i = 1; i < constant_pool.length; i++) {
        byte tag = (byte) iStream.readUnsignedByte();
        switch (tag) {
        case Constant.CONSTANT_Class:
          constant_pool[i] = new CONSTANT_Class_Info(iStream, tag);
          break;
        case Constant.CONSTANT_Fieldref:
          constant_pool[i] = new CONSTANT_Fieldref_Info(iStream, tag);
          break;
        case Constant.CONSTANT_Methodref:
          constant_pool[i] = new CONSTANT_Methodref_Info(iStream, tag);
          break;
        case Constant.CONSTANT_InterfaceMethodref:
          constant_pool[i] = new CONSTANT_InterfaceMethodref_Info(iStream, tag);
          break;
        case Constant.CONSTANT_String:
          constant_pool[i] = new CONSTANT_String_Info(iStream, tag);
          break;
        case Constant.CONSTANT_Integer:
          constant_pool[i] = new CONSTANT_Integer_Info(iStream, tag);
          break;
        case Constant.CONSTANT_Float:
          constant_pool[i] = new CONSTANT_Float_Info(iStream, tag);
          break;
        case Constant.CONSTANT_Long:
          constant_pool[i++] = new CONSTANT_Long_Info(iStream, tag);
		      constant_pool[i] = new CONSTANT_Null_Info(iStream, tag);
          break;
        case Constant.CONSTANT_Double:
          constant_pool[i++] = new CONSTANT_Double_Info(iStream, tag);
		      constant_pool[i] = new CONSTANT_Null_Info(iStream, tag);
		  break;
        case Constant.CONSTANT_NameAndType:
          constant_pool[i] = new CONSTANT_NameAndType_Info(iStream, tag);
          break;
        case Constant.CONSTANT_Utf8:
          constant_pool[i] = new CONSTANT_Utf8_Info(iStream, tag);
          break;
        default:
          throw new ClassFileException("Invalid constant pool tag: " + tag);
        }
      }
    }
    catch (ClassFileException e) {
      System.out.println(e.getMessage());
    }
  }
}

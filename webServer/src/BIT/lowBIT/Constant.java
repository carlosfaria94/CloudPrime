/* Constant.java
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

/**
 * This class has various useful constants.
 */
public class Constant {
  // tag constants
  public final static byte CONSTANT_Class = 7;
  public final static byte CONSTANT_Fieldref = 9;
  public final static byte CONSTANT_Methodref = 10;
  public final static byte CONSTANT_InterfaceMethodref = 11;
  public final static byte CONSTANT_String = 8;
  public final static byte CONSTANT_Integer = 3;
  public final static byte CONSTANT_Float = 4;
  public final static byte CONSTANT_Long = 5;
  public final static byte CONSTANT_Double = 6;
  public final static byte CONSTANT_NameAndType = 12;
  public final static byte CONSTANT_Utf8 = 1;
  
  // field access flag constants
  public final static short ACC_PUBLIC = 0x0001;
  public final static short ACC_PRIVATE = 0x0002;
  public final static short ACC_PROTECTED = 0x0004;
  public final static short ACC_STATIC = 0x0008;
  public final static short ACC_FINAL = 0x0010;
  public final static short ACC_VOLATILE = 0x0040;
  public final static short ACC_TRANSIENT = 0x0080;
  
  // class access flag constants
  public final static short ACC_SUPER = 0x0020;
  public final static short ACC_INTERFACE = 0x0200;
  public final static short ACC_ABSTRACT = 0x0400;
  
  // method access flag constants
  public final static short ACC_SYNCHRONIZED = 0x0020;
  public final static short ACC_NATIVE = 0x0100;
}

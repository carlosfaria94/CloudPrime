/* Attribute_Info_Parse.java
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

public class Attribute_Info_Parse {
  /** Constructor takes an input stream and parses several different attributes.
   *  If there is an unknown attribute, then it simply ignores it.
   *  <br>
   *  @see java.io.DataInputStream
   */
  public Attribute_Info_Parse(Attribute_Info[] attributes, Cp_Info[] constant_pool, 
    DataInputStream iStream) throws IOException {
    for (int i = 0; i < attributes.length; i++) {
      short attribute_name_index = (short) iStream.readUnsignedShort();
      CONSTANT_Utf8_Info attribute_name = (CONSTANT_Utf8_Info)
        constant_pool[attribute_name_index];
//cjk
      String attribute_string = new String(attribute_name.bytes);
      if (System.getProperty("CJKDEBUG3") != null) 
      System.err.println("READ BIT parsing: " + attribute_string + " at: " +attribute_name_index);
      // since switch does not work on String object
      if (attribute_string.equals("SourceFile")) {
        attributes[i] = new SourceFile_Attribute(iStream, attribute_name_index, constant_pool);
      }
      else if (attribute_string.equals("ConstantValue")) {
        attributes[i] = new ConstantValue_Attribute(iStream, attribute_name_index, constant_pool);
      }
      else if (attribute_string.equals("Code")) {
        attributes[i] = new Code_Attribute(constant_pool, iStream, attribute_name_index);
      }
      else if (attribute_string.equals("Exceptions")) {
        attributes[i] = new Exceptions_Attribute(iStream, attribute_name_index, constant_pool);
      }
      else if (attribute_string.equals("LineNumberTable")) {
        attributes[i] = new LineNumberTable_Attribute(iStream, attribute_name_index);
      }
      else if (attribute_string.equals("LocalVariableTable")) {
        attributes[i] = new LocalVariableTable_Attribute(iStream, attribute_name_index);
      }
      else if (attribute_string.equals("Synthetic")) {
        attributes[i] = new Synthetic_Attribute(iStream, attribute_name_index);
      }
      else if (attribute_string.equals("Deprecated")) {
        attributes[i] = new Deprecated_Attribute(iStream, attribute_name_index);
      }
      else if (attribute_string.equals("InnerClasses")) {
        attributes[i] = new InnerClasses_Attribute(iStream, attribute_name_index, constant_pool);
      }
      else if (attribute_string.equals("StackAllocLocalVariables")) {
        attributes[i] = new StackAllocLocalVariables_Attribute(iStream, attribute_name_index);
      }
      else {			// if unrecognizable attribute, just ignore them
        attributes[i] = new Unknown_Attribute(iStream, attribute_name_index);  
      }
    }
  }
}




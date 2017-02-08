/* Field_Info.java
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

public class Field_Info {
  public int count = 0;
  // data members
  // access_flags is a mask that describes access permission to and 
  // properties of a field
  public short access_flags;
  
  // name_index represents a Java field name
  public short name_index;
  
  // descriptor_index represents a Java field descriptor
  public short descriptor_index;
  
  // attribute_count indeicates the number of additional
  // attributes in this field
  public short attribute_count;
  
  // attributes array
  public Attribute_Info attributes[];
  
  // constructor
  public Field_Info(Cp_Info[] constant_pool, DataInputStream iStream)
    throws IOException {
    access_flags = (short) iStream.readUnsignedShort();
    name_index = (short) iStream.readUnsignedShort();
    descriptor_index = (short) iStream.readUnsignedShort();
    attribute_count = (short) iStream.readUnsignedShort();
    //System.err.println("Field Info: di: " + descriptor_index + " ni: " + name_index);
//cjk
    attributes = new Attribute_Info[attribute_count];
    
    if (System.getProperty("CJKDEBUG3") != null) 
      System.err.println("Calling parse for field attributes");
    Attribute_Info_Parse ai_parse = new Attribute_Info_Parse(attributes, constant_pool, iStream);
  }

  public void write(Cp_Info[] constant_pool, DataOutputStream oStream)
    throws IOException {
    oStream.writeShort((int) access_flags);
    oStream.writeShort((int) name_index);
    oStream.writeShort((int) descriptor_index);
    oStream.writeShort((int) attribute_count);

    //System.err.println("Writing field_info: flags, ni, di, ac: " + 
	//access_flags + " " + name_index + " " + descriptor_index + " " + attribute_count);
    for (int i = 0; i < attribute_count; i++) {
      //System.err.println("attribute: " + i);
      CONSTANT_Utf8_Info attribute_name = (CONSTANT_Utf8_Info)
        constant_pool[attributes[i].attribute_name_index];
      String attribute_string = new String(attribute_name.bytes);
      //System.err.println("attribute name index: " + attributes[i].attribute_name_index
	//+ " " + attribute_string);
      // since switch does not work on String object
      //if (attribute_string.equals("SourceFile")) {
        //((SourceFile_Attribute) attributes[i]).write(oStream);
      //}
      if (attribute_string.equals("ConstantValue")) {
        ((ConstantValue_Attribute) attributes[i]).write(oStream);
      }
      else if (attribute_string.equals("Synthetic")) {
        ((Synthetic_Attribute) attributes[i]).write(oStream);
      }
      else if (attribute_string.equals("Deprecated")) {
        ((Deprecated_Attribute) attributes[i]).write(oStream);
      }
      //else if (attribute_string.equals("Code")) {
        //((Code_Attribute) attributes[i]).write(constant_pool, oStream);
      //}
      //else if (attribute_string.equals("Exceptions")) {
        //((Exceptions_Attribute) attributes[i]).write(oStream);
      //}
      //else if (attribute_string.equals("LineNumberTable")) {
        //((LineNumberTable_Attribute) attributes[i]).write(oStream);
      //}
      //else if (attribute_string.equals("LocalVariableTable")) {
        //((LocalVariableTable_Attribute) attributes[i]).write(oStream);
      //}
      else {			// if unrecognizable attribute, just ignore them
        ((Unknown_Attribute) attributes[i]).write(oStream);
      }
    }
  }

  public int size() {
      int temp_size = 0;

      for (int i = 0; i < attribute_count; i++) {
       temp_size += attributes[i].size();
      }

      return temp_size + 8;
  }

}

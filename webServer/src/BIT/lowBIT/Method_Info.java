/* Method_Info.java
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
import java.util.Vector;

public class Method_Info {
  /* cjk */
  short default_flags = 25; /* public,static,final */

  // data members
  public int count = 0;
  
  // access_flags is a mask that describes access permission to and
  //   properties of a method or instance initialization method
  public short access_flags;
  
  // name_index is an index into the constant_pool table
  //   and indicates the name of the method 
  public short name_index;
  
  // descriptor_index is an index into the constant_pool table
  //   and indicates the method descriptor
  public short descriptor_index;
  
  // attribute_count indicates the number of attributes of this method
  public short attribute_count;
  
  // attributes[] is an array of attributes for this method
  public Attribute_Info attributes[];
  
  // constructors
  public Method_Info(Cp_Info[] constant_pool, DataInputStream iStream)
    throws IOException {
    access_flags = (short) iStream.readUnsignedShort();
    name_index = (short) iStream.readUnsignedShort();
    descriptor_index = (short) iStream.readUnsignedShort();
    attribute_count = (short) iStream.readUnsignedShort();
    attributes = new Attribute_Info[attribute_count];
//cjk
    Attribute_Info_Parse ai_parse = new Attribute_Info_Parse(attributes, constant_pool, iStream);
   
  }
//cjk
  public Method_Info(int nindex, int dindex, int cindex, byte newcodeblock[], short maxstack, short maxlocals) throws IOException {
    access_flags = default_flags;
    name_index = (short) nindex;
    descriptor_index = (short) dindex;
    attribute_count = (short) 1;
    attributes = new Attribute_Info[attribute_count];
    attributes[0] = new Code_Attribute(cindex, newcodeblock, maxstack, maxlocals);
  }

  public void write(Cp_Info[] constant_pool, DataOutputStream oStream)
    throws IOException {
	//System.err.println("MI flags, ni, di, ac: " + access_flags + " " + name_index
		//+ " " + descriptor_index + " " + attribute_count);
    oStream.writeShort((int) access_flags);
    if (System.getProperty("CJKDEBUG3") != null) 
      System.err.println("\tWRITING: " + access_flags);
    oStream.writeShort((int) name_index);
    if (System.getProperty("CJKDEBUG3") != null) 
      System.err.println("\tWRITING: " + name_index);
    oStream.writeShort((int) descriptor_index);
    if (System.getProperty("CJKDEBUG3") != null) 
      System.err.println("\tWRITING: " + descriptor_index);
    oStream.writeShort((int) attribute_count);
    if (System.getProperty("CJKDEBUG3") != null) 
      System.err.println("\tWRITING: " + attribute_count);
    
    for (int i = 0; i < attribute_count; i++) {
      CONSTANT_Utf8_Info attribute_name = (CONSTANT_Utf8_Info)
        constant_pool[attributes[i].attribute_name_index];
      String attribute_string = new String(attribute_name.bytes);
      // since switch does not work on String object
      //if (attribute_string.equals("SourceFile")) {
        //((SourceFile_Attribute) attributes[i]).write(oStream);
      //}
      //else if (attribute_string.equals("ConstantValue")) {
        //((ConstantValue_Attribute) attributes[i]).write(oStream);
      //}
      if (attribute_string.equals("Synthetic")) {
        ((Synthetic_Attribute) attributes[i]).write(oStream);
         if (System.getProperty("CJKDEBUG3") != null)
         System.err.println("\tWRITING meth synthetic");
      }
      else if (attribute_string.equals("Deprecated")) {
        ((Deprecated_Attribute) attributes[i]).write(oStream);
         if (System.getProperty("CJKDEBUG3") != null)
         System.err.println("\tWRITING meth dep");
      }
      else if (attribute_string.equals("Code")) {
         if (System.getProperty("CJKDEBUG3") != null)
         System.err.println("\tWRITING meth code");
        ((Code_Attribute) attributes[i]).write(constant_pool, oStream);
      }
      else if (attribute_string.equals("Exceptions")) {
         if (System.getProperty("CJKDEBUG3") != null)
         System.err.println("\tWRITING meth exceptions");
        ((Exceptions_Attribute) attributes[i]).write(oStream);
      }
      //else if (attribute_string.equals("LineNumberTable")) {
        //((LineNumberTable_Attribute) attributes[i]).write(oStream);
      //}
      //else if (attribute_string.equals("LocalVariableTable")) {
        //((LocalVariableTable_Attribute) attributes[i]).write(oStream);
      //}
      else {			// if unrecognizable attribute, just ignore them
         if (System.getProperty("CJKDEBUG3") != null)
         System.err.println("\tWRITING meth unknown");
        ((Unknown_Attribute) attributes[i]).write(oStream);
      }
    }
  }
  public int ldsize() {  /* Do not use this for the size of the local data */

      /* this is NOT the size of local data used by the method, it is the 
       * size of the method info structure without the code attribute
       */
      System.err.println("ERROR in Method_Info.java: do not use ldsize to get the size of the local data.");
      int temp_size = 0;
      int code_len = 0;
      for (int i = 0; i < attribute_count; i++) {
       	temp_size += attributes[i].size();
	if (attributes[i] instanceof Code_Attribute) {
		code_len+=((Code_Attribute)attributes[i]).code_length;
	}
      }
      temp_size=temp_size-code_len;

      return temp_size + 8;
  }
  public int method_info_size() {  
      /* this is the total size of the method info structure 
       */
      int temp_size = 0;
      for (int i = 0; i < attribute_count; i++) {
       temp_size += attributes[i].size();
      }

      return temp_size + 8;
  }
  public int method_info_size_code_as_only_attrib() {  
      int temp_size = 0;
      for (int i = 0; i < attribute_count; i++) {
          if (attributes[i] instanceof Code_Attribute) {
              temp_size += attributes[i].size();
          }
      }

      return temp_size + 8;
  }
  public int method_info_size_no_code() {  
      /* this is the total size of the method info structure 
       */
      int temp_size = 0;
      for (int i = 0; i < attribute_count; i++) {
          if ((!(attributes[i] instanceof Code_Attribute)) && (!(attributes[i] instanceof Exceptions_Attribute))) {
              temp_size += attributes[i].size();
          }
      }

      return temp_size + 8;
  }

  public int code_length() {
      /* this is the total size of the bytecode size only of the code in the method info structure 
       */
      for (int i = 0; i < attribute_count; i++) {
       if (attributes[i] instanceof Code_Attribute) {
         return ((Code_Attribute)attributes[i]).code_length;
       }
      }
      return 0;
  }
  public int size() {
     /* this is the size of the code and exception attribute only in the method info structure 
	this is the data necessary to execute the method
     */
     /* method size is 12 + code_length + attribute.size() for each attribute */
     /* attribute.size() is just attribute_length+6 */
     int temp_size = 0;
     for (int i = 0; i < attribute_count; i++) {
       if (attributes[i] instanceof Code_Attribute || attributes[i] instanceof Exceptions_Attribute) {
         temp_size += (attributes[i].attribute_length+6); /* 6 init bytes for name and length */
       }
      }
      return temp_size;
  }
  public Code_Attribute getCodeAttrib() {
      for (int j = 0; j < attribute_count; j++) {
          if (attributes[j] instanceof Code_Attribute) {
	      return (Code_Attribute)attributes[j];
          }
      }
      return null;
  }
  public LocalVariableTable_Attribute getLVTAttrib() {
      for (int j = 0; j < attribute_count; j++) {
          if (attributes[j] instanceof Code_Attribute) {
	      Code_Attribute ca = (Code_Attribute)attributes[j];
      	      for (int i = 0; i < ca.attribute_count; i++) {
                  if (ca.attributes[i] instanceof LocalVariableTable_Attribute) {
		      return (LocalVariableTable_Attribute)ca.attributes[i];
		  }
	      }
          }
      }
      return null;
  }
  public Local_Variable_Table[] getLVT() {
      for (int j = 0; j < attribute_count; j++) {
          if (attributes[j] instanceof Code_Attribute) {
	      Code_Attribute ca = (Code_Attribute)attributes[j];
      	      for (int i = 0; i < ca.attribute_count; i++) {
                  if (ca.attributes[i] instanceof LocalVariableTable_Attribute) {
		      return ((LocalVariableTable_Attribute)ca.attributes[i]).local_variable_table;
		  }
	      }
          }
      }
      return null;
  }



}

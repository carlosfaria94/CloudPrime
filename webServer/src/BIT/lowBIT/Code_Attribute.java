/* Code_Attribute.java
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

/**
 * One of the attributes that represents the code inside a method.
 * This attribute contains the actual code buffer as well as information
 * about exception tables.
 * <br>
 * @see Attribute_Info
 */
public class Code_Attribute extends Attribute_Info {
  /**
   * max_stack indicates the maximum number of words on the operand
   * statck at any point during execution of this method.
   */
  public short max_stack;
  /** 
   * max_locals indicates the number of local vairables used by this method.
   */
  public short max_locals;
  /**  
   * code-length indicates the number of bytes in the code array for this 
   * method.
   */
  public int code_length;
  /**
   * code array is the code (what else? :).
   */
  public byte code[];
  /**
   * exception_table_length indicates the number of entries in the
   * exception_table table.
   */
  public short exception_table_length;
  /**
   * exceptions is an array of exception_table.
   *
   * @see   Exception_Table
   */
  public Exception_Table exceptions[];
  /**
   * attribute_count indicates the number of attributes in attributes array.
   */
  public short attribute_count;
  /** 
   * attributes is an array of attribute_info.
   * 
   * @see Attribute_Info
   */
  public Attribute_Info attributes[];
  
  /**
   * Parses this attribute given a DataInputStream.
   *
   * @param     constant pool table for the class
   * @param     DataInputStream to be parsed
   * @param     the index of this attribute into the constant pool
   */
  public Code_Attribute(Cp_Info[] constant_pool, DataInputStream iStream,
    short attribute_name_index) throws IOException {
    this.attribute_name_index = attribute_name_index;
    attribute_length = iStream.readInt();
    if (System.getProperty("CJKDEBUG3") != null) 
    System.err.println("READ BIT parsing len: " + attribute_length);
    max_stack = (short) iStream.readUnsignedShort();
    max_locals = (short) iStream.readUnsignedShort();
    code_length = iStream.readInt();
    if (System.getProperty("CJKDEBUG3") != null) 
    System.err.println("READ BIT parsing mxs,mxl,codelen: " + max_stack + " " + max_locals + " " +code_length);
    code = new byte[code_length];
    iStream.readFully(code);
    exception_table_length = (short) iStream.readUnsignedShort();
    if (System.getProperty("CJKDEBUG3") != null) 
    System.err.println("READ BIT parsing exlen: " + exception_table_length);
    exceptions = new Exception_Table[exception_table_length];
    
    for (int i = 0; i < exception_table_length; i++) {
      exceptions[i] = new Exception_Table(iStream, constant_pool);
    }
    
    attribute_count = (short) iStream.readUnsignedShort();
    if (System.getProperty("CJKDEBUG3") != null) 
    System.err.println("READ BIT parsing attcount: " + attribute_count);
    attributes = new Attribute_Info[attribute_count];
    
    if (System.getProperty("CJKDEBUG3") != null) 
      System.err.println("Calling parse for code attributes");
    Attribute_Info_Parse ai_parse = new Attribute_Info_Parse(attributes, constant_pool, iStream);
    int size = 12+code_length;
    if (System.getProperty("CJKDEBUG3") != null) 
    System.err.print("CJKSIZE: 12 + codelen: " + code_length);
    for (int i=0;i < attribute_count; i++) {
	size+= attributes[i].size();
        if (System.getProperty("CJKDEBUG3") != null) 
        System.err.print(" + attrib: " + attributes[i].size() + " i: " + i);
    }
    if (System.getProperty("CJKDEBUG3") != null) 
    System.err.println("\nCJKSIZE: " + size);
  }
  public Code_Attribute(int attribute_name_index, byte newcodeblock[], short maxstack, short maxlocals) {
    this.attribute_name_index = (short)attribute_name_index;
    attribute_length = 13; /* max_stack#2, max_locals#2, code_length#4, extablen#2, attrcount#2 */
    max_stack = maxstack;
    max_locals = maxlocals;
    code_length = newcodeblock.length;
    code = new byte[code_length];
    System.arraycopy(code,0,newcodeblock,0,code_length);
    exception_table_length = 0;
    exceptions = null;
    attribute_count = 0;
    attributes = null;
  }

  /**
   * Given an output stream, it outputs its data to that output stream.
   *
   * @param     constant pool table
   * @param     output stream where to output the data
   */
  public void write(Cp_Info[] constant_pool, DataOutputStream oStream)
    throws IOException {
	//System.err.println("Writing code attrib ani: " + attribute_name_index);
    oStream.writeShort((int) attribute_name_index);
    if (System.getProperty("CJKDEBUG3") != null) 
         System.err.println("\tWRITING code:" + attribute_name_index);
    oStream.writeInt(attribute_length);
    if (System.getProperty("CJKDEBUG3") != null) 
         System.err.println("\tWRITING code att len:" + attribute_length);
    oStream.writeShort((int) max_stack);
    if (System.getProperty("CJKDEBUG3") != null) 
         System.err.println("\tWRITING code:" + max_stack);
    oStream.writeShort((int) max_locals);
    if (System.getProperty("CJKDEBUG3") != null) 
         System.err.println("\tWRITING code:" + max_locals);
    oStream.writeInt(code_length);
    if (System.getProperty("CJKDEBUG3") != null) 
         System.err.println("\tWRITING code:" + code_length);
    oStream.write(code, 0, code_length);
    if (System.getProperty("CJKDEBUG3") != null) 
         System.err.println("\tWRITING code");
    oStream.writeShort((int) exception_table_length);
    if (System.getProperty("CJKDEBUG3") != null) 
         System.err.println("\tWRITING code:" + exception_table_length);

    if (System.getProperty("CJKDEBUG3") != null) 
         System.err.println("\tWRITING exceptions");
    for (int i = 0; i < exception_table_length; i++) {
    if (System.getProperty("CJKDEBUG3") != null) 
	System.err.println("Writing code attrib exc: " + exceptions[i]);
      exceptions[i].write(oStream);
    }

    oStream.writeShort((int) attribute_count);
    if (System.getProperty("CJKDEBUG3") != null) 
         System.err.println("\tWRITING code:" + attribute_count);
    for (int i = 0; i < attribute_count; i++) {
    if (System.getProperty("CJKDEBUG3") != null) 
	System.err.println("Writing code attrib attrib: " + attributes[i].attribute_name_index);
      CONSTANT_Utf8_Info attribute_name = (CONSTANT_Utf8_Info)
        constant_pool[attributes[i].attribute_name_index];
      String attribute_string = new String(attribute_name.bytes);
      if (attribute_string.equals("LineNumberTable")) {
        ((LineNumberTable_Attribute) attributes[i]).write(oStream);
      }
      else if (attribute_string.equals("LocalVariableTable")) {
        ((LocalVariableTable_Attribute) attributes[i]).write(oStream);
      }
      else if (attribute_string.equals("StackAllocLocalVariables")) {
      if (System.getProperty("CJKDEBUG3") != null) 
	System.err.println("Writing out SALV");
        ((StackAllocLocalVariables_Attribute) attributes[i]).write(oStream);
      }
      else {			// if unrecognizable attribute, just ignore them
        ((Unknown_Attribute) attributes[i]).write(oStream);
      }
    }
  }

  public StackAllocLocalVariables_Attribute getSAAttrib() {
      for (int i = 0; i < attribute_count; i++) {
          if (attributes[i] instanceof StackAllocLocalVariables_Attribute) {
	      return (StackAllocLocalVariables_Attribute)attributes[i];
          }
      }
      return null;
  }
  public void updateAttributeLength() {
      attribute_length = 12+code_length;
      for (int i=0;i < attribute_count; i++) {
          attribute_length+= attributes[i].size();
      }
  }
  public Attribute_Info addAttribute(String attribname, Cp_Info cpool[]) {
      Attribute_Info ai = null;
      Local_Variable_Table lvt[] = null;
      if (attribname.equals("StackAllocLocalVariables")) {
	  for (int i=0; i < attribute_count; i++) {
	      if (attributes[i] instanceof StackAllocLocalVariables_Attribute) return null;
	      if (attributes[i] instanceof LocalVariableTable_Attribute) {
	          lvt = ((LocalVariableTable_Attribute)attributes[i]).local_variable_table;
	      }
	  }
	  if (lvt == null) {
	      return null;
	  }
          Attribute_Info newattrib[] = new Attribute_Info[attribute_count+1];
	  System.arraycopy(attributes,0,newattrib,0,attribute_count);
	  ai = newattrib[attribute_count] = new StackAllocLocalVariables_Attribute(lvt,cpool);
	  attributes = newattrib;
	  attribute_count++;
          attribute_length = 12+code_length;
      }
      return ai;
  }
}

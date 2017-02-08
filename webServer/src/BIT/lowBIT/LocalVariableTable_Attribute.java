/* LocalVariableTable_Attribute.java
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

public class LocalVariableTable_Attribute extends Attribute_Info {
  // data members
  
  // local_variable_table_length indicates the number of entries in the
  //   local_variable_table array
  public short local_variable_table_length;
  
  public Local_Variable_Table local_variable_table[];
  
  // cosntructor
  public LocalVariableTable_Attribute(DataInputStream iStream, short attribute_name_index) 
    throws IOException {
    this.attribute_name_index = attribute_name_index;
    attribute_length = iStream.readInt();
    if (System.getProperty("CJKDEBUG3") != null) 
    System.err.println("READ BIT parsing len: " + attribute_length);
    local_variable_table_length = (short) iStream.readUnsignedShort();
    local_variable_table = new Local_Variable_Table[local_variable_table_length];
    if (System.getProperty("CJKDEBUG3") != null) 
    System.err.println("LVT reading sizes, attri_len: " + attribute_length + " tablelen: " 
        + local_variable_table_length +" nameindex: " + attribute_name_index);
    
    for (int i = 0; i < local_variable_table_length; i++) {
      local_variable_table[i] = new Local_Variable_Table(iStream);
    }
  }

  public void write(DataOutputStream oStream) throws IOException {
    oStream.writeShort((int) attribute_name_index);
    oStream.writeInt(attribute_length);
    oStream.writeShort((int) local_variable_table_length);
    if (System.getProperty("CJKDEBUG3") != null) 
    System.err.println("LVT writing sizes, attri_len: " + attribute_length + " tablelen: " 
        + local_variable_table_length +" nameindex: " + attribute_name_index);

    for (int i = 0; i < local_variable_table_length; i++) {
      local_variable_table[i].write(oStream);
    }
  }
}

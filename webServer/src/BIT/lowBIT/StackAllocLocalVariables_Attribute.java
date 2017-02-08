/* StackAllocLocalVariables_Attribute.java
 * Part of BIT -- Bytecode Instrumenting Tool
 * Developed by Chandra Krintz
 */


package BIT.lowBIT;

import java.io.*;
import java.util.*;

public class StackAllocLocalVariables_Attribute extends Attribute_Info {
  
  public short table_length;
  public Local_Variable_Table table[];

  // constructor
  /* this assumes that cpool has been extended by one and passed in to be updated */
  /* list is a Vector of Local_Variable_Tables */
  public StackAllocLocalVariables_Attribute(Local_Variable_Table lvt[], Vector list, Cp_Info cpool[]) {

    attribute_length = 2 + (10*list.size()); 
    if ((cpool.length-1) > 32767) {
	System.err.println("ERROR In conversion to short! StackAllocLocalVariables_Attribute.java");
    }
    attribute_name_index = (short)(cpool.length-1);
    cpool[attribute_name_index] = new CONSTANT_Utf8_Info("StackAllocLocalVariables");
    
    table = new Local_Variable_Table[list.size()];
    int index = 0;
    for (int i=0; i < list.size(); i++) {
	for (int j=0; j < lvt.length; j++) {
	    if ((Local_Variable_Table)list.elementAt(i) == lvt[j]) { /* found one */
		table[index++] = lvt[j];
		break;
	    }
	}
    }    
  }
  public StackAllocLocalVariables_Attribute(Local_Variable_Table lvt[], Cp_Info cpool[]) {

    if ((cpool.length-1) > 32767) {
	System.err.println("ERROR In conversion to short! StackAllocLocalVariables_Attribute.java");
    }
    attribute_name_index = (short)(cpool.length-1);
    cpool[attribute_name_index] = new CONSTANT_Utf8_Info("StackAllocLocalVariables");
    attribute_length = 2; /* 2-tablelen*/
    table_length = 0;
    table = null;
    
  }
  public StackAllocLocalVariables_Attribute(DataInputStream iStream, short attribute_name_index) 
    throws IOException {
    this.attribute_name_index = attribute_name_index;
    attribute_length = iStream.readInt();
    if (System.getProperty("CJKDEBUG3") != null) 
    System.err.println("READ BIT parsing len: " + attribute_length);
    table_length = (short) iStream.readUnsignedShort();
    table = new Local_Variable_Table[table_length];
    if (System.getProperty("CJKDEBUG3") != null) 
    System.err.println("SA reading sizes, attri_len: " + attribute_length + " tablelen: " + table_length +" nameindex: " + 
        attribute_name_index);
    
    for (int i = 0; i < table_length; i++) {
      table[i] = new Local_Variable_Table(iStream);
    }
  }
  /* list contains Local_Variable_Table objects */
  public void addElements(Vector list) {

    /* Code_Attribute.updateAttributeLength must be called after using this method */

    if (list == null || list.size() == 0) return;
    if (table_length != 0) {
        home: for (int i=0; i < list.size(); i++) {
            for (int j=0; j < table_length; j++) {
	        if (table[j] == (Local_Variable_Table)list.elementAt(i)) {
		    list.removeElementAt(i);
	            continue home;
	        }
	    }
        }    
    } 
    Local_Variable_Table t2[] = new Local_Variable_Table[list.size()+table_length];
    if (table_length > 0) {
        System.arraycopy(table,0,t2,0,table_length);
    } else { /* assumes len is never < 0 */
	attribute_length = 2;/*  2-tablelen*/
    }
    for (int i=table_length; i < (list.size()+table_length); i++) {
	t2[i] = (Local_Variable_Table)list.elementAt(i);
	attribute_length+=10; /* 2-startpc + 2-len + 2-nameindex + 2-descindex + 2-index */
    }
    table_length += list.size();
    table = t2;
      
  }
  public void addElement(Local_Variable_Table ele) {
    /* Code_Attribute.updateAttributeLength must be called after using this method */
    for (int i=0; i < table.length; i++) {
        if (table[i] == ele) return;
    }    
    Local_Variable_Table t2[] = new Local_Variable_Table[table_length+1];
    if (table_length > 0) {
        System.arraycopy(table,0,t2,0,table_length);
    } else { /* assumes len is never < 0 */
	attribute_length = 2;/*  2-tablelen*/
    }
    t2[table_length++] = ele;
    attribute_length+=10; /* 2-startpc + 2-len + 2-nameindex + 2-descindex + 2-index */
    table = t2;
  }
  public void write(DataOutputStream oStream) throws IOException {
    oStream.writeShort((int) attribute_name_index);
    oStream.writeInt(attribute_length);
    oStream.writeShort((int) table_length);
    if (System.getProperty("CJKDEBUG3") != null) 
    System.err.println("SA writing sizes, attri_len: " + attribute_length + " tablelen: " + table_length +" nameindex: " + 
        attribute_name_index);

    for (int i = 0; i < table_length; i++) {
      table[i].write(oStream);
    }
  }
}

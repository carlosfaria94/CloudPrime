/* ClassFile.java
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
import java.util.*;

public class ClassFile {
  /* cjk */
  public int start_of_empties = -1;
  int extra_method_nextid = 0;

  private int nextval = 0;
  /**
   * 0xCAFEBABE identifies the class file format. 
   */
  public int magic;
  /** 
   * Minor version of the compiler used to create this class file. 
   */
  public short minor_version;
  /** 
   * Major version of the compiler used to create this class file. 
   */
  public short major_version;
  /** 
   * The number of entries in the contant pool table.
   * <br>
   * @see ClassFile#constant_pool
   */
  public short constant_pool_count;
  /** 
   * This is where class file keeps its data such as names of methods,
   * fields, class, and other constants.
   */
  public Cp_Info constant_pool[];
  /** 
   * Access modifier for this class.
   */
  public short access_flags;
  /**
   * Index into the constant pool containing information about current class.
   * <br>
   * @see ClassFile#constant_pool
   */
  public short this_class;
  /**
   * Index into the constant pool containing information about this class's super class.
   * <br>
   * @see ClassFile#constant_pool
   */
  public short super_class;
  /**
   * Represents the number of superinterfaces of this class.
   */
  public short interface_count;
  /**
   * Each entry is an index into the constant pool table and contains information
   * about the interface.
   * <br>
   * @see ClassFile#constant_pool
   */
  public short interfaces[];
  /**
   * Represents the number of fields present in this class.
   */
  public short field_count;
  /**
   * Each entry is a field_info structure and contains information
   * about each field in this class.
   * <br>
   * @see Field_Info
   */
  public Field_Info fields[];
  /**
   * Represents the number of methods present in this class.
   */
  public short methods_count;
  /**
   * Each entry is a method_info structure and contains information
   * about each method in this class.
   * <br>
   * @see Method_Info
   */
  public Method_Info methods[];
  /**
   * Represents the number of attributes present in this class.
   */
  public short attributes_count;
  /**
   * Each entry is an attribute_info structure and contains information
   * about each attribute in this class.
   */
  public Attribute_Info attributes[];
  
  /**
   * Constructor reads in a class file and parses it into its internal representation.
   *
   * @param     the name of the class file to be parsed
   */
  public ClassFile() { //needed for subclass ColdClassFile
      magic = (int) 0xcafebabe;
  }
  public ClassFile(String filename) {
    DataInputStream iStream = null;
    try {
	try {
		iStream = new DataInputStream(new FileInputStream(filename));
	} catch (Exception e) {
		System.err.println("ClassfileException!");
		System.err.println("filename " + filename);
		System.err.println("here " + e.getMessage());
		System.out.println("here2 " + e.toString() + "\nstrace: ");
		e.printStackTrace();
	}

      // start reading info from the input stream
      // magic number
      try {
        magic = iStream.readInt();
        if (magic != (int) 0xcafebabe)
          throw new ClassFileException("Invalid .class file");
      }
      catch (ClassFileException e) {
        System.out.println(e.getMessage());
      }
      
      // version number of the compiler that produced this class file
      minor_version = (short) iStream.readUnsignedShort();
      major_version = (short) iStream.readUnsignedShort();
      
      // constant pool
	//System.err.println("reading cpoolcount: " + constant_pool_count);
      constant_pool_count = (short) iStream.readUnsignedShort();
      constant_pool = new Cp_Info[constant_pool_count];
      
      // parse constant pool
      Constant_Pool_Parse cp_parse = new Constant_Pool_Parse(constant_pool, iStream);
      
      // access flags
      access_flags = (short) iStream.readUnsignedShort();
      
      // classes
      this_class = (short) iStream.readUnsignedShort();
      super_class = (short) iStream.readUnsignedShort();
      
      // interfaces
      interface_count = (short) iStream.readUnsignedShort();

      if (System.getProperty("CJKDEBUG3") != null) 
      System.err.println("classInfo read accflags: "+ access_flags + " this: " + this_class + " super: " + super_class + " " 
          +interface_count);

      interfaces = new short[interface_count];
      
      for (int i = 0; i < interface_count; i++) {
        interfaces[i] = (short) iStream.readUnsignedShort();
      }
      
      // fields
      field_count = (short) iStream.readUnsignedShort();
      fields = new Field_Info[field_count];
      
      for (int i = 0; i < field_count; i++) {
        fields[i] = new Field_Info(constant_pool, iStream);
      }
      
      // methods
      methods_count = (short) iStream.readUnsignedShort();
      methods = new Method_Info[methods_count];
      
      for (int i = 0; i < methods_count; i++) {
        methods[i] = new Method_Info(constant_pool, iStream);
      }
      
      // attributes
      attributes_count = (short) iStream.readUnsignedShort();
      attributes = new Attribute_Info[attributes_count];
      
      if (System.getProperty("CJKDEBUG3") != null) 
      System.err.println("Calling parse for class attributes");
      Attribute_Info_Parse ai_parse = new Attribute_Info_Parse(attributes, constant_pool, iStream);
      //cjk
      iStream.close();
    }
    catch (EOFException e) {
      System.out.println("Unexpected End of File (EOF)");
      System.out.println(e.getMessage());
    }
    catch (FileNotFoundException e) {
      System.out.println("ClassFile Exception: File not found");
      System.out.println(e.getMessage());
    }
    catch (IOException e) {
      System.out.println("Other IO exceptions");
      System.out.println(e.getMessage());
    }
  }
  public ClassFile(String filename, byte newcodeblock[], short maxstack, short maxlocals ) {
    DataInputStream iStream = null;
    try {
	try {
		iStream = new DataInputStream(new FileInputStream(filename));
	} catch (Exception e) {
		System.err.println("ClassfileException!");
		System.err.println("filename " + filename);
		System.err.println("here " + e.getMessage());
		System.out.println("here2 " + e.toString() + "\nstrace: ");
		e.printStackTrace();
	}

      // start reading info from the input stream
      // magic number
      try {
        magic = iStream.readInt();
        if (magic != (int) 0xcafebabe)
          throw new ClassFileException("Invalid .class file");
      }
      catch (ClassFileException e) {
        System.out.println(e.getMessage());
      }
      
      // version number of the compiler that produced this class file
      minor_version = (short) iStream.readUnsignedShort();
      major_version = (short) iStream.readUnsignedShort();
      
      // constant pool
	//System.err.println("reading cpoolcount: " + constant_pool_count);
      constant_pool_count = (short) iStream.readUnsignedShort();
      constant_pool = new Cp_Info[constant_pool_count];
      
      // parse constant pool
      Constant_Pool_Parse cp_parse = new Constant_Pool_Parse(constant_pool, iStream);
      
      // access flags
      access_flags = (short) iStream.readUnsignedShort();
      
      // classes
      this_class = (short) iStream.readUnsignedShort();
      super_class = (short) iStream.readUnsignedShort();
      
      // interfaces
      interface_count = (short) iStream.readUnsignedShort();

      if (System.getProperty("CJKDEBUG3") != null) 
      System.err.println("classInfo read accflags: "+ access_flags + " this: " + this_class + " super: " + super_class + " " 
          +interface_count);

      interfaces = new short[interface_count];
      
      for (int i = 0; i < interface_count; i++) {
        interfaces[i] = (short) iStream.readUnsignedShort();
      }
      
      // fields
      field_count = (short) iStream.readUnsignedShort();
      fields = new Field_Info[field_count];
      
      for (int i = 0; i < field_count; i++) {
        fields[i] = new Field_Info(constant_pool, iStream);
      }
      
      // methods
      methods_count = (short) iStream.readUnsignedShort();
      start_of_empties = methods_count;
      methods_count++;

      methods = new Method_Info[methods_count];
      
      
/*
      String desc = "()V";
      String code = "Code";
      short newcpcount;
      Cp_Info newcp[];
      System.err.println("looking for code: " + code + " desc: " + desc);
*/
      int dindex = -1;
      int cindex = -1;
      int mindex = -1;
      for (int i = 0; i < methods_count; i++) {
	if ( i < start_of_empties) {
            methods[i] = new Method_Info(constant_pool, iStream);
	} else {
	    /* add constant pool entry for ()V type */
	    dindex = addConstantPoolEntry(1, "()V");
	    mindex = addConstantPoolEntry(1, new String("M" + extra_method_nextid++));
	    cindex = addConstantPoolEntry(1, "Code");
/*
	    newcpcount = (short) (constant_pool_count + 1);
	    newcp = new Cp_Info[newcpcount];
	    for (int j=0; j < constant_pool_count; j++) {
		Cp_Info cpi = constant_pool[j];
		if (cpi == null) System.err.println("cpi is null");
		else System.err.println("cpi tag: " + cpi.tag);
		newcp[j] = constant_pool[j];
		if (cpi == null || cpi.tag != 1) continue;
		String cpi_desc = new String( ((CONSTANT_Utf8_Info) constant_pool[j]).bytes);
		System.err.println("checking cpool utf8: " + cpi_desc);
		if (dindex == -1  && cpi_desc.equals(desc)) {
		    dindex = j;
		    System.err.println("found cpool utf8: " + desc);
		} else if (cindex == -1 && cpi_desc.equals(code)) {
		    cindex = j;
		    System.err.println("found cpool utf8: " + code);
		}
	    }
	    newcp[constant_pool_count] = new CONSTANT_Utf8_Info(new String("M" + extra_method_nextid++));
	    constant_pool = newcp;
	    mindex = constant_pool_count;
	    constant_pool_count = newcpcount;
	    System.err.println("adding to cpool: M" + (extra_method_nextid-1));

	    if (dindex == -1) {
	        newcpcount = (short) (constant_pool_count+1);
	        newcp = new Cp_Info[newcpcount];
		for (int j=0; j< constant_pool_count; j++) {
		    newcp[j] = constant_pool[j];
		}
	        newcp[constant_pool_count] = new CONSTANT_Utf8_Info(desc);
	        constant_pool = newcp;
	        dindex = constant_pool_count;
	        constant_pool_count = newcpcount;
	        System.err.println("adding to cpool: " + desc);
   	    } 
	    if (cindex == -1) {
		System.err.println("ERROR, Code utf8 not found in constant pool (should not happen!)");
		System.exit(-1);
	    }
*/

	    String mn =  new String( ((CONSTANT_Utf8_Info) constant_pool[mindex]).bytes);
	    String dn =  new String( ((CONSTANT_Utf8_Info) constant_pool[dindex]).bytes);
	    String cn =  new String( ((CONSTANT_Utf8_Info) constant_pool[cindex]).bytes);
	    System.err.println("adding method: " + mn +  " " + dn + " " + cn);
            methods[i] = new Method_Info(mindex,dindex,cindex, newcodeblock, maxstack, maxlocals);
	}
      }
	
      
      // attributes
      attributes_count = (short) iStream.readUnsignedShort();
      attributes = new Attribute_Info[attributes_count];
      
      if (System.getProperty("CJKDEBUG3") != null) 
      System.err.println("Calling parse for class attributes");
      Attribute_Info_Parse ai_parse = new Attribute_Info_Parse(attributes, constant_pool, iStream);
      //cjk
      iStream.close();
    }
    catch (EOFException e) {
      System.out.println("Unexpected End of File (EOF)");
      System.out.println(e.getMessage());
    }
    catch (FileNotFoundException e) {
      System.out.println("ClassFile Exception: File not found");
      System.out.println(e.getMessage());
    }
    catch (IOException e) {
      System.out.println("Other IO exceptions");
      System.out.println(e.getMessage());
    }
  }
  public Attribute_Info addAttribute(String attribname, Method_Info meth) {
      Attribute_Info ai = null;
      Cp_Info newcp[] = new Cp_Info[constant_pool_count+1];
      System.arraycopy(constant_pool,0,newcp,0,constant_pool_count);
      if (attribname.equals("StackAllocLocalVariables")) {
          /* find code attribute and add this guy */
          out: for (int i = 0; i < methods_count; i++) {
              Method_Info m = methods[i];
	      if (m == meth) {
	          for (int j = 0; j < m.attribute_count; j++) {
	              if (m.attributes[j] instanceof Code_Attribute) {
		          ai = ((Code_Attribute)m.attributes[j]).addAttribute(attribname,newcp);
		          break out;
		      }
	          }
	      }
          }
	  if (ai != null) { 
	      /* success, use new constant pool */
              if (System.getProperty("CJKDEBUG3") != null) 
              System.err.println("NEW CPCOUNT: " + (constant_pool_count+1));
	      constant_pool = newcp;
	      constant_pool_count++;
	  }
      }
      return ai;
  }
  public void addMethod(Method_Info mi) {
	Method_Info tmpm[] = new Method_Info[++methods_count];
	System.arraycopy(methods,0,tmpm,0,(methods_count-1));
        tmpm[methods_count-1] = mi;
        methods = new Method_Info[methods_count];
	System.arraycopy(tmpm,0,methods,0,methods_count);
	return;
  }
  public void addField(Field_Info fi) {
	Field_Info tmpf[] = new Field_Info[++field_count];
	System.arraycopy(fields,0,tmpf,0,(field_count-1));
        tmpf[field_count-1] = fi;
        fields = new Field_Info[field_count];
	System.arraycopy(tmpf,0,fields,0,field_count);
	return;
  }
  public int increaseCPoolSize(int cpc) {
	//user wants to add cpc elements to the cpool
	//increase the array size accordingly, 
	//return the index that the user can use next
	int startindex = constant_pool.length;
	Cp_Info tmpcp[] = null;
	if (cpc > 0) {
		startindex = constant_pool.length;
		tmpcp = new Cp_Info[(startindex + cpc)];
		System.arraycopy(constant_pool,0,tmpcp,0,startindex);
		constant_pool = new Cp_Info[(startindex + cpc)];
		System.arraycopy(tmpcp,0,constant_pool,0,startindex); //nothing is in indexes >= startindex
	}
	return(startindex);
  }

  /**
   * Outputs the internal representation of this ClassFile to a file.
   *
   * @param     the name of the file to be created
   */
  public void write(String filename)
  {
    try {
      DataOutputStream oStream = new DataOutputStream(new FileOutputStream(filename));
      // start writing info to the output stream
      // magic number
      oStream.writeInt(magic);
      
      // version number of the compiler that produced this class file
      oStream.writeShort((int) minor_version);
      oStream.writeShort((int) major_version);
      
      // constant pool
	//System.err.println("writing cpoolcount: " + constant_pool_count);
      oStream.writeShort((int) constant_pool_count);
      if (System.getProperty("CJKDEBUG3") != null) 
      System.err.println("\tWRITING CPCOUNT: " + constant_pool_count);
      for (int i = 1; i < constant_pool_count; i++) {
        switch (constant_pool[i].getTag()) {
        case Constant.CONSTANT_Class:
          ((CONSTANT_Class_Info) constant_pool[i]).write(oStream);
          break;
        case Constant.CONSTANT_Fieldref:
          ((CONSTANT_Fieldref_Info) constant_pool[i]).write(oStream);
          break;
        case Constant.CONSTANT_Methodref:
          ((CONSTANT_Methodref_Info) constant_pool[i]).write(oStream);
          break;
        case Constant.CONSTANT_InterfaceMethodref:
          ((CONSTANT_InterfaceMethodref_Info) constant_pool[i]).write(oStream);
          break;
        case Constant.CONSTANT_String:
          ((CONSTANT_String_Info) constant_pool[i]).write(oStream);
          break;
        case Constant.CONSTANT_Integer:
          ((CONSTANT_Integer_Info) constant_pool[i]).write(oStream);
          break;
        case Constant.CONSTANT_Float:
          ((CONSTANT_Float_Info) constant_pool[i]).write(oStream);
          break;
        case Constant.CONSTANT_Long:
          ((CONSTANT_Long_Info) constant_pool[i]).write(oStream);
          break;
        case Constant.CONSTANT_Double:
          ((CONSTANT_Double_Info) constant_pool[i]).write(oStream);
          break;
        case Constant.CONSTANT_NameAndType:
          ((CONSTANT_NameAndType_Info) constant_pool[i]).write(oStream);
          break;
        case Constant.CONSTANT_Utf8:
          ((CONSTANT_Utf8_Info) constant_pool[i]).write(oStream);
          break;
        default:
          break;
        }
      }

      // access flags
      oStream.writeShort((int) access_flags);
      if (System.getProperty("CJKDEBUG3") != null) 
      System.err.println("\tWRITING: " + access_flags);

      // classes
      oStream.writeShort((int) this_class);
      if (System.getProperty("CJKDEBUG3") != null) 
      System.err.println("\tWRITING: " + this_class);
      oStream.writeShort((int) super_class);
      if (System.getProperty("CJKDEBUG3") != null) 
      System.err.println("\tWRITING: " + super_class);

      // interfaces
      oStream.writeShort((int) interface_count);
      if (System.getProperty("CJKDEBUG3") != null) 
      System.err.println("\tWRITING: " + interface_count);
      for (int i = 0; i < interface_count; i++) {
        oStream.writeShort((int) interfaces[i]);
      }
      if (System.getProperty("CJKDEBUG3") != null) 
      System.err.println("\tWRITING: interfaces");

      // fields
      oStream.writeShort((int) field_count);
      if (System.getProperty("CJKDEBUG3") != null) 
      System.err.println("\tWRITING: " + field_count);
      for (int i = 0; i < field_count; i++) {
        fields[i].write(constant_pool, oStream);
      }
      if (System.getProperty("CJKDEBUG3") != null) 
      System.err.println("\tWRITING: fields");
      
      // methods
      oStream.writeShort((int) methods_count);
      if (System.getProperty("CJKDEBUG3") != null) 
      System.err.println("\tWRITING: " + methods_count);
      for (int i = 0; i < methods_count; i++) {
        methods[i].write(constant_pool, oStream);
      }
      if (System.getProperty("CJKDEBUG3") != null) 
      System.err.println("\tWRITING: methods");

      // attributes
      oStream.writeShort((int) attributes_count);
      if (System.getProperty("CJKDEBUG3") != null) 
      System.err.println("\tWRITING: " + attributes_count);
      for (int i = 0; i < attributes_count; i++) {
        CONSTANT_Utf8_Info attribute_name = (CONSTANT_Utf8_Info)
          constant_pool[attributes[i].attribute_name_index];
        String attribute_string = new String(attribute_name.bytes);
        // since switch does not work on String object
        if (attribute_string.equals("SourceFile")) {
          if (System.getProperty("CJKDEBUG3") != null) 
          System.err.println("\tWRITING: sourcefile");
          ((SourceFile_Attribute) attributes[i]).write(oStream);
        }
        else if (attribute_string.equals("Synthetic")) {
          if (System.getProperty("CJKDEBUG3") != null) 
          System.err.println("\tWRITING: synthetic");
          ((Synthetic_Attribute) attributes[i]).write(oStream);
        }
        else if (attribute_string.equals("Deprecated")) {
          if (System.getProperty("CJKDEBUG3") != null) 
          System.err.println("\tWRITING: deprecated");
          ((Deprecated_Attribute) attributes[i]).write(oStream);
        }
        else if (attribute_string.equals("InnerClasses")) {
          if (System.getProperty("CJKDEBUG3") != null) 
          System.err.println("\tWRITING: inner");
          ((InnerClasses_Attribute) attributes[i]).write(oStream);
        }
        else {			// if unrecognizable attribute, just ignore them
          if (System.getProperty("CJKDEBUG3") != null) 
          System.err.println("\tWRITING: unknown");
          ((Unknown_Attribute) attributes[i]).write(oStream);
        }
      }
      oStream.flush();
      oStream.close();
    }
    catch (IOException e) {
      System.out.println("IO exceptions");
      System.out.println(e.getMessage());
    }
  }
  
  /**
   * Allows one to add a contant pool entry (Cp_Info) to the constant pool table.
   *
   * @param     the constant pool entry to be added to the table
   * @return    the new constant pool table
   * @see       Cp_Info
   */
  public Cp_Info[] addConstantPoolEntry(Cp_Info entry) {
    Cp_Info new_cp[];
    
    new_cp = new Cp_Info[constant_pool_count];
    // copy constant pool array into new_cp
    System.arraycopy(constant_pool, 1, new_cp, 1, constant_pool_count - 1);

    constant_pool = new Cp_Info[++constant_pool_count];
    System.arraycopy(new_cp, 1, constant_pool, 1, constant_pool_count - 2);

    constant_pool[constant_pool_count-1] = entry;
    return constant_pool;
  }
  public short addConstantPoolEntry(int tag, String name) {
      /* does name exist already? */
      short retn_index = -1; 
      String cname;
      for (short j=1; j < constant_pool_count; j++) {
	  Cp_Info cpi = constant_pool[j]; 
	  if (tag == cpi.tag) {
	      switch(tag) {
        	case Constant.CONSTANT_Class:
		    cname = new String( ((CONSTANT_Utf8_Info) constant_pool[((CONSTANT_Class_Info)cpi).name_index]).bytes);
		    if (cname.equals(name)) retn_index = j;
          	    break;
        	case Constant.CONSTANT_String:
		    cname = new String( ((CONSTANT_Utf8_Info) constant_pool[((CONSTANT_String_Info)cpi).string_index]).bytes);
		    if (cname.equals(name)) retn_index = j;
          	    break;
        	case Constant.CONSTANT_Integer:
		    int val = Integer.parseInt(name);
		    if (val == ((CONSTANT_Integer_Info)cpi).bytes) retn_index = j;
          	    break;
        	case Constant.CONSTANT_Float:
		    val = Integer.parseInt(name);
		    if (val == ((CONSTANT_Float_Info)cpi).bytes) retn_index = j;
          	    break;
        	case Constant.CONSTANT_Long:
		    long lval = Long.parseLong(name);
		    if (lval == ((CONSTANT_Long_Info)cpi).getValue()) retn_index = j;
          	    break;
        	case Constant.CONSTANT_Utf8:
		    String nm = new String (((CONSTANT_Utf8_Info)cpi).bytes);
		    if (nm.equals(name)) retn_index = j;
          	    break;


        	case Constant.CONSTANT_InterfaceMethodref:
		    /* name passed in must be class:methodnametype */
        	case Constant.CONSTANT_Methodref:
		    /* name passed in must be class:methodnametype */
        	case Constant.CONSTANT_Fieldref:
		    /* name passed in must be class:fieldname:type */
        	case Constant.CONSTANT_NameAndType:
        	case Constant.CONSTANT_Double:
        	default:
          	    System.err.println("Unimplemented constant pool tag: " + tag);
		    System.exit(-1);
	      }
	      break;
	  }
	  if (tag == Constant.CONSTANT_Long || tag == Constant.CONSTANT_Double) j++;
      }
      if (retn_index == -1) {
	   /* add the guy */
	    retn_index = constant_pool_count;
	    short newcpcount = (short) (constant_pool_count + 1);
	    Cp_Info newcp[] = new Cp_Info[newcpcount];
	    newcp[0] = null;
            for (int j=1; j < constant_pool_count; j++) {
	        newcp[j] = constant_pool[j]; 
	    }
	    newcp[constant_pool_count] = new CONSTANT_Utf8_Info(name);
	    constant_pool = newcp;
	    constant_pool_count = newcpcount;
	    System.err.println("adding to cpool: " + name);
     }
     return retn_index;
  }

//cjk - should only be called by ClassInfo
  public Cp_Info[] removeConstantPoolEntry(int index, int h) {
    Cp_Info new_cp[];
    if (h != 1) return null;
    if (index < 1) return null;
    //System.err.println("In removeCPEntry: index: " + index);
    /*constant_pool[index] = new CONSTANT_Utf8_Info(new String("cjk"+(nextval++)));
     *return constant_pool;
     */


    Cp_Info todel = constant_pool[index];
    
    new_cp = new Cp_Info[constant_pool_count-1];
    if (index == (constant_pool_count-1)){
   	//System.err.println("entry is last entry");
    	System.arraycopy(constant_pool, 1, new_cp, 1, (constant_pool_count-2));
    	constant_pool = new Cp_Info[--constant_pool_count];
	//System.err.println("decrimenting cpoolcount: " + constant_pool_count);
    	System.arraycopy(constant_pool, 1, new_cp, 1, (constant_pool_count-1));
    	todel = null;
	return constant_pool;
    }
	
   //System.err.println("entry is not last entry");
    // copy upto index
    System.arraycopy(constant_pool, 1, new_cp, 1, index-1);
    // copy after index
    System.arraycopy(constant_pool, index+1, new_cp, index, ((constant_pool_count-index)-1) );

    constant_pool = new Cp_Info[--constant_pool_count];
    //System.err.println("new cpcount: " + constant_pool_count);
    System.arraycopy(new_cp, 1, constant_pool, 1, constant_pool_count - 1);
    todel = null;
    return constant_pool;
   
  }

//Added for size routines -cjk

   public int constant_pool_size() {
        int temp_size = 0;
        // constant pool size
        for (int i = 1; i < constant_pool_count; i++) {
            temp_size += constant_pool[i].size();
        }
        return temp_size;
     }

     public int field_size() {
        int temp_size = 0;
        // field size
        for (int i = 0; i < field_count; i++) {
            temp_size += fields[i].size();
        }
        return temp_size;
     }

     public int method_ld_size() {	//return size of local data in methods
	/* do not use! */
        int temp_size = 0;
        // method size
        for (int i = 0; i < methods_count; i++) {
            temp_size += methods[i].ldsize();
        }
        return temp_size;
     }
     public int method_info_size() {	//return size of the method infos
        int temp_size = 0;
        // method size
        for (int i = 0; i < methods_count; i++) {
            temp_size += methods[i].method_info_size();
        }
        return temp_size;
     }
     public int method_size() {	//return size of local data and code in methods
        int temp_size = 0;
        // method size
        for (int i = 0; i < methods_count; i++) {
            temp_size += methods[i].size();
        }
        return temp_size;
     }

     public int code_length() { //return size of code in methods
        int temp_size = 0;
        // method size
        for (int i = 0; i < methods_count; i++) {
            temp_size += methods[i].code_length();
        }
        return temp_size;
     }

     public int attribute_size() {
        int temp_size = 0;
        // attribute size
        for (int i = 0; i < attributes_count; i++) {
            temp_size += attributes[i].size();
        }

        return temp_size;
     }


     public int size() {  //return size of entire class file with code
        int temp_size = 0;

        temp_size += 4; // magic
        temp_size += 2; // minor version
        temp_size += 2; // major version
        temp_size += 2; // constant pool count
        temp_size += constant_pool_size(); // constant pool
        temp_size += 2; // access flags
        temp_size += 2; // this class
        temp_size += 2; // super class
        temp_size += 2; // interface count
        temp_size += interface_count * 2; // interface
        temp_size += 2; // fields count
        temp_size += field_size(); // field
        temp_size += 2; // method count
        temp_size += method_info_size(); // method
        temp_size += 2; // attributes count
        temp_size += attribute_size(); // attributes

        return temp_size;
     } 
     public int gdsize() {  //return size of entire class file without code
        int temp_size = 0;
        temp_size += 4; // magic
        temp_size += 2; // minor version
        temp_size += 2; // major version
        temp_size += 2; // constant pool count
        temp_size += constant_pool_size(); // constant pool
        temp_size += 2; // access flags
        temp_size += 2; // this class
        temp_size += 2; // super class
        temp_size += 2; // interface count
        temp_size += interface_count * 2; // interface
        temp_size += 2; // fields count
        temp_size += field_size(); // field
        temp_size += 2; // method count
        temp_size += (method_info_size()-method_size()); // method info only, not code
        temp_size += 2; // attributes count
        temp_size += attribute_size(); // attributes

        return temp_size;
     } 
     public int verify_size() {  //return size of the header and access info (no cpool)
        int temp_size = 0;
        temp_size += 4; // magic
        temp_size += 2; // minor version
        temp_size += 2; // major version
        temp_size += 2; // constant pool count
        temp_size += 2; // access flags
        temp_size += 2; // this class
        temp_size += 2; // super class
        temp_size += 2; // interface count
        temp_size += interface_count * 2; // interface
	/* fields and method infos needed to verify that no final methods/fields are overwritten by subclass */
        temp_size += 2; // fields count
        temp_size += field_size(); // field
        temp_size += 2; // method count
        temp_size += (method_info_size()-method_size()); // method info only, not code
        temp_size += 2; // attributes count
        temp_size += attribute_size(); // attributes

        return temp_size;
     } 

     public int getConstantPoolCount() {  
        return constant_pool_count;
     }
     public Cp_Info[] getConstantPool() {  
        return constant_pool;
     }
     public int getInterfaceCount() {  
        return interface_count;
     }
     public int getFieldInfoCount() {  
        return field_count;
     }
     public int getMethodInfoCount() {  
        return methods_count;
     }
     public short getSuperClassIndex() {  
        return super_class;
     }
     public short getThisClassIndex() {  
        return this_class;
     }
     public Field_Info[] getFieldInfo() {  
        return fields;
     }
     public Method_Info[] getMethodInfo() {  
        return methods;
     }
     public Attribute_Info[] getAttributeInfo() {  
        return attributes;
     }
     public short[] getCFInterfaces() {  
        return interfaces;
     }


}

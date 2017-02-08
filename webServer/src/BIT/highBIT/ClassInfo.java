/* ClassInfo.java
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

package BIT.highBIT;

import BIT.lowBIT.*;
import java.util.*;
import java.io.*;

/**
 * Contains information about a Java <tt>.class</tt> file.
 * 
 * @author  <a href="mailto:hanlee@cs.colorado.edu">Han B. Lee</a>
 * @see classFile.ClassFile
 * @see Routine
 **/

public class ClassInfo {

    public Vector global_cpool_refs;
    public byte modflags[];
    public static byte clearflags[];
    public Vector children_class_info = new Vector(); /* contains ClassInfos of decendents */
    public Vector super_class_info = new Vector(); /* contains ClassInfos of super and interfaces */

    /** 
     * Contains parsed class structure.
     *
     * @see classFile.ClassFile
     */
    protected ClassFile classfile;
    /**
     * Name of this class.
     * <br>
     * This value can be read by invoking getClassName method.
     * @see getClassName
     */
    protected String class_name;
    /**
     * Name of super class.
     * <br>
     * This value can be read by invoking getSuperClassName method.
     * @see getSuperClassName
     */
    protected String superclass_name;
    /**
     * Name of the source file.
     * <br>
     * This value can be read by invoking getSourceFileName method.
     * @see getSourceFileName
     */
    protected String source_file_name;
    /**
     * Vector of methods defined in this class file.
     * <br>
     * This value is set when the constructor is invoked and can be
     * read by invoking getRoutines method.
     * @see getRoutines
     * @see java.util.Vector
     */
    protected Vector routines;

    /**
     * Creates a new ClassInfo class which breaks down a <tt>.class</tt>
     * into more easily manageble entities such as Routines.
     * <br>
     * The only attribute that it looks at is the "SourceFile" attribute.
     *
     * @param	filename	the filename of the <tt>.class</tt> file
     *						to be analyzed
     * @see classFile.ClassFile
     * @see classFile.CONSTANT_Utf8_Info
     * @see classFile.CONSTANT_Class_Info
     * @see Routine
     * @see java.util.Vector
     */

//cjk
   public ClassInfo(ClassFile cf,String name) {  //create an empty classfile based on 
	//class file from another class
	//System.err.println("Processing " + name);
	classfile = new ColdClassFile(cf,name);

        CONSTANT_Class_Info class_info = null;
        CONSTANT_Utf8_Info info = null;

	try {
	  // get class name
	  class_info = (CONSTANT_Class_Info) classfile.constant_pool[classfile.
		this_class];
       	  info = (CONSTANT_Utf8_Info) classfile.constant_pool[class_info.
       		name_index];
       	  class_name = new String(info.bytes);
	  System.err.println("CI got class_name: " + class_name);

     	  // get super class name
       	  class_info = (CONSTANT_Class_Info) classfile.constant_pool[classfile.
       		super_class];
       	  if (class_info == null) superclass_name = null;
       	  else {
       	    info = (CONSTANT_Utf8_Info) classfile.constant_pool[class_info.
       		name_index];
      	    superclass_name = new String(info.bytes);
	  System.err.println("CI got superclass_name: " + superclass_name);
      	  }
          // look for source file attribute and set source_file_name
          //   accordingly
	  System.err.println("CI attrib count: " + classfile.attributes_count);

          for (int i = 0; i < classfile.attributes_count; i++) {
            Attribute_Info attribute = classfile.attributes[i];
            info = (CONSTANT_Utf8_Info) classfile.constant_pool[attribute.attribute_name_index];
            String attribute_name = new String(info.bytes);
            if (attribute_name.equals("SourceFile")) {
                SourceFile_Attribute sourcefile_attribute = (SourceFile_Attribute) attribute;
                info = (CONSTANT_Utf8_Info) classfile.constant_pool[sourcefile_attribute.sourcefile_index];
                source_file_name = new String(info.bytes);
            }
          }
          // get the method information and put them in the routines vector
	  System.err.println("CI meth count: " + classfile.methods_count);
          routines = new Vector();
          for (int i = 0; i < classfile.methods_count; i++) {
            routines.addElement(new Routine(classfile.methods[i], classfile.constant_pool, this));
          }
        } catch(Exception e) {
          System.err.println("Exception! in ClassInfo(cf,name) constructor");
          System.err.println(e.getMessage());
          System.out.println(e.toString() + "\nstrace: ");
          e.printStackTrace();
        }
    }

    public ClassInfo(String filename, byte newcodeblock[], short maxstack, short maxlocals ) {
        // create an instance of ClassFile
        //   classfile now contains parsed class structure
        classfile = new ClassFile(filename, newcodeblock,maxstack,maxlocals);
        
        CONSTANT_Class_Info class_info = null; ///////
        CONSTANT_Utf8_Info info = null;

	try {


	  // get class name
	  class_info = (CONSTANT_Class_Info) classfile.constant_pool[classfile.
		this_class];
       	  info = (CONSTANT_Utf8_Info) classfile.constant_pool[class_info.
       		name_index];
       	  class_name = new String(info.bytes);
	  //System.err.println("Processing " + class_name + " cp size: " + classfile.constant_pool_count);

     	  // get super class name
       	  class_info = (CONSTANT_Class_Info) classfile.constant_pool[classfile.
       		super_class];
       	  if (class_info == null) superclass_name = null;
       	  else {
       	    info = (CONSTANT_Utf8_Info) classfile.constant_pool[class_info.
       		name_index];
      	    superclass_name = new String(info.bytes);
      	  }
          // look for source file attribute and set source_file_name
          //   accordingly
          for (int i = 0; i < classfile.attributes_count; i++) {
            Attribute_Info attribute = classfile.attributes[i];
            info = (CONSTANT_Utf8_Info) classfile.constant_pool[attribute.attribute_name_index];
            String attribute_name = new String(info.bytes);
            if (attribute_name.equals("SourceFile")) {
                SourceFile_Attribute sourcefile_attribute = (SourceFile_Attribute) attribute;
                info = (CONSTANT_Utf8_Info) classfile.constant_pool[sourcefile_attribute.sourcefile_index];
                source_file_name = new String(info.bytes);
            }
          }
          // get the method information and put them in the routines vector
          routines = new Vector();
          for (int i = 0; i < classfile.methods_count; i++) {
            	Routine r = new Routine(classfile.methods[i], classfile.constant_pool, this);
            	routines.addElement(r);
	    }

        } catch(Exception e) {
          System.err.println("Exception! in ClassInfo constructor");
          System.out.println(e.toString());
          e.printStackTrace();
        
    	}
    }
    public ClassInfo(String filename) {
        // create an instance of ClassFile
        //   classfile now contains parsed class structure
        classfile = new ClassFile(filename);
        
        CONSTANT_Class_Info class_info = null; ///////
        CONSTANT_Utf8_Info info = null;

	try {


	  // get class name
	  class_info = (CONSTANT_Class_Info) classfile.constant_pool[classfile.
		this_class];
       	  info = (CONSTANT_Utf8_Info) classfile.constant_pool[class_info.
       		name_index];
       	  class_name = new String(info.bytes);
	  //System.err.println("Processing " + class_name + " cp size: " + classfile.constant_pool_count);

     	  // get super class name
       	  class_info = (CONSTANT_Class_Info) classfile.constant_pool[classfile.
       		super_class];
       	  if (class_info == null) superclass_name = null;
       	  else {
       	    info = (CONSTANT_Utf8_Info) classfile.constant_pool[class_info.
       		name_index];
      	    superclass_name = new String(info.bytes);
      	  }
          // look for source file attribute and set source_file_name
          //   accordingly
          for (int i = 0; i < classfile.attributes_count; i++) {
            Attribute_Info attribute = classfile.attributes[i];
            info = (CONSTANT_Utf8_Info) classfile.constant_pool[attribute.attribute_name_index];
            String attribute_name = new String(info.bytes);
            if (attribute_name.equals("SourceFile")) {
                SourceFile_Attribute sourcefile_attribute = (SourceFile_Attribute) attribute;
                info = (CONSTANT_Utf8_Info) classfile.constant_pool[sourcefile_attribute.sourcefile_index];
                source_file_name = new String(info.bytes);
            }
          }
          // get the method information and put them in the routines vector
          routines = new Vector();
          for (int i = 0; i < classfile.methods_count; i++) {
            	Routine r = new Routine(classfile.methods[i], classfile.constant_pool, this);
            	routines.addElement(r);
	    }

        } catch(Exception e) {
          System.err.println("Exception! in ClassInfo constructor");
          System.out.println(e.toString());
          e.printStackTrace();
        
    	}
    }
//cjk
    public static void printCpoolEntry(Cp_Info[] cp,int k) {
	int index,index2,index3;

        switch (cp[k].getTag()) {
        case Constant.CONSTANT_Class:
	  index = ((CONSTANT_Class_Info) cp[k]).name_index;
	  System.err.println("Class pts to: " + new String(((CONSTANT_Utf8_Info)cp[index]).bytes)); 
          break;
        case Constant.CONSTANT_Fieldref:
	  index = ((CONSTANT_Fieldref_Info) cp[k]).class_index;
	  index2 = ((CONSTANT_Class_Info) cp[index]).name_index;
	  System.err.println("Fieldref pts to: class: " + new String(((CONSTANT_Utf8_Info)cp[index2]).bytes)); 
	  index = ((CONSTANT_Fieldref_Info) cp[k]).name_and_type_index;
	  index2 = ((CONSTANT_NameAndType_Info) cp[index]).name_index;
	  index3 = ((CONSTANT_NameAndType_Info) cp[index]).descriptor_index;
	  System.err.println("\tname: " + new String(((CONSTANT_Utf8_Info)cp[index2]).bytes) 
	  	+ " and type: " + new String(((CONSTANT_Utf8_Info)cp[index3]).bytes)); 
          break;
        case Constant.CONSTANT_Methodref:
	  index = ((CONSTANT_Methodref_Info) cp[k]).class_index;
	  index2 = ((CONSTANT_Class_Info) cp[index]).name_index;
	  System.err.println("Methodref pts to: class: " + new String(((CONSTANT_Utf8_Info)cp[index2]).bytes)); 
	  index = ((CONSTANT_Methodref_Info) cp[k]).name_and_type_index;
	  index2 = ((CONSTANT_NameAndType_Info) cp[index]).name_index;
	  index3 = ((CONSTANT_NameAndType_Info) cp[index]).descriptor_index;
	  System.err.println("\tname: " + new String(((CONSTANT_Utf8_Info)cp[index2]).bytes) 
	  	+ " and type: " + new String(((CONSTANT_Utf8_Info)cp[index3]).bytes)); 
          break;
        case Constant.CONSTANT_InterfaceMethodref:
	  index = ((CONSTANT_InterfaceMethodref_Info) cp[k]).class_index;
	  index2 = ((CONSTANT_Class_Info) cp[index]).name_index;
	  System.err.println("InterfaceMethodref pts to: class: " 
		+ new String(((CONSTANT_Utf8_Info)cp[index2]).bytes)); 
	  index = ((CONSTANT_InterfaceMethodref_Info) cp[k]).name_and_type_index;
	  index2 = ((CONSTANT_NameAndType_Info) cp[index]).name_index;
	  index3 = ((CONSTANT_NameAndType_Info) cp[index]).descriptor_index;
	  System.err.println("\tname: " + new String(((CONSTANT_Utf8_Info)cp[index2]).bytes) 
	  	+ " and type: " + new String(((CONSTANT_Utf8_Info)cp[index3]).bytes)); 
          break;
        case Constant.CONSTANT_String:
	  index = ((CONSTANT_String_Info) cp[k]).string_index;
	  System.err.println("String pts to: " + new String(((CONSTANT_Utf8_Info)cp[index]).bytes)); 
          //((CONSTANT_String_Info) constant_pool[i]).write(oStream);
          break;
        case Constant.CONSTANT_Integer:
	  System.err.println("Integer val: " + ((CONSTANT_Integer_Info)cp[k]).bytes);
          break;
        case Constant.CONSTANT_Float:
	  System.err.println("Float val: " + ((CONSTANT_Float_Info)cp[k]).bytes);
          break;
        case Constant.CONSTANT_Long:
	  System.err.println("Long val: " + ((CONSTANT_Long_Info)cp[k]).low_bytes + " " 
		+ ((CONSTANT_Long_Info)cp[k]).high_bytes);
          break;
        case Constant.CONSTANT_Double:
	  System.err.println("Double val: " + ((CONSTANT_Double_Info)cp[k]).low_bytes + " " 
		+ ((CONSTANT_Double_Info)cp[k]).high_bytes);
          break;
        case Constant.CONSTANT_NameAndType:
	  index = ((CONSTANT_NameAndType_Info) cp[k]).name_index;
	  index2 = ((CONSTANT_NameAndType_Info) cp[k]).descriptor_index;
	  System.err.println("NameAndType pts to: name: " 
		+ new String(((CONSTANT_Utf8_Info)cp[index]).bytes) + " type: " 
		+ new String(((CONSTANT_Utf8_Info)cp[index2]).bytes));
          break;
        case Constant.CONSTANT_Utf8:
	  System.err.println("Utf8 pts to: " 
		+ new String(((CONSTANT_Utf8_Info)cp[k]).bytes));
          break;
        default:
          break;
        }
    }
    public static synchronized void insertInt(Vector v, int val) {
        //insert val into v so that v remains in increasing order
        int i, k;
        for (i = 0; i < v.size(); i++) {
                k = ((Integer) v.elementAt(i)).intValue();
                if (k > val) {
                        v.insertElementAt(new Integer(val),i);
                        return;
                }
        }
        v.addElement(new Integer(val));
    }

    
    /**
     * Returns the name of this class.
     */
    public String getClassName() {
        return class_name;
    }
    
    /**
     * Returns the name of the super class.
     */
    public String getSuperClassName() {
        return superclass_name;
    }
    
    /**
     * Returns the name of the source file.
     */
    public String getSourceFileName() {
        return source_file_name;
    }
    
    /** 
     * Returns the routines in this class.
     *
     * @see java.util.Vector 
     */
    public Vector getRoutines() {
        return routines;
    }
    
    /**
     * Returns the number of routiens in this class.
     */
    public int getRoutineCount() {
        return routines.size();
    }

    public Cp_Info[] getConstantPool() {
        return classfile.constant_pool;
    }

    /**
     * Adds an entry to the constant pool.
     *
     * @param   the constant pool entry to be addded
     * @return  the new array where the new entry has been added
     * @see BIT.lowBIT.Cp_Info
     */
    public Cp_Info[] addConstantPoolEntry(Cp_Info entry) {
        Cp_Info[] cps = classfile.addConstantPoolEntry(entry);
        for (Enumeration e = routines.elements(); e.hasMoreElements(); ) {
            Routine r = (Routine) e.nextElement();
            r.setConstantPool(cps);
        }
        return cps;
    }
    
    /**
     * Outputs the class file structure to a file.
     *
     * @param   the name of the output file
     */
    public void write(String filename) {
        for (Enumeration e = routines.elements(); e.hasMoreElements(); ) {
            Routine r = (Routine) e.nextElement();
            r.writeReady();
        }
        classfile.write(filename);
    }
    
    /**
     * Allows one to add a method before this class.
     *
     * @param       String representing the name of the class of the method to be added
     * @param       String the name of the method itself
     * @param       Object to be passed to the method as argument
     */
    public void addBefore(String classname, String methodname, Object arg) {
        for (Enumeration e = routines.elements(); e.hasMoreElements(); ) {
            Routine r = (Routine) e.nextElement();
            if (r.getMethodName().equals("<clinit>")) {
                r.addBefore(classname, methodname, arg);
                return;
            }

        }

        for (Enumeration e = routines.elements(); e.hasMoreElements(); ) {
            Routine r = (Routine) e.nextElement();
            if (r.getMethodName().equals("main")) {
                r.addBefore(classname, methodname, arg);
                return;
            }

        }
        
        for (Enumeration e = routines.elements(); e.hasMoreElements(); ) {
            Routine r = (Routine) e.nextElement();
            if (r.getMethodName().equals("<init>")) {
                r.addBefore(classname, methodname, arg);
                return;
            }
        }
    }
    
    /**
     * Allows one to add a method after this class.
     *
     * <B>Important!</B>
     * Because a Java program typically consists of different class files,
     * the meaning of adding a call to a method after a class is not clear.
     * Here we define adding a method after a class to mean that a call 
     * to a method will be inserted before end of this class only if this class
     * contains the <B>main</B> method.
     * In particular, one should note that if a program exits through a call
     * to System.exit(), Runtime.exit(), or through other abnormal ways,
     * it cannot be guaranteed that the call inserted will get executed at all
     * before program termination.
     * One workaround is to instrument System.exit() and Runtime.exe() in your
     * own standard Java class library.
     * Please email us if you know a good way to fix this semantic problem.
     *
     * @param       String representing the name of the class of the method to be added
     * @param       String the name of the method itself
     * @param       Object to be passed to the method as argument
     */
    public void addAfter(String classname, String methodname, Object arg) {
        for (Enumeration e = routines.elements(); e.hasMoreElements(); ) {
            Routine r = (Routine) e.nextElement();
            if (r.getMethodName().equals("main")) {
                r.addAfter(classname, methodname, arg);
            }
            for (Enumeration i = (r.getInstructionArray()).elements(); i.hasMoreElements(); ) {
                Instruction instr = (Instruction) i.nextElement();
                if (instr.getOpcode() == 184) {  // invokestatic
                    if (isExit(instr.getOperandValue())) {
                        instr.addBefore(classname, methodname, arg);
                    }
                }
            }
        }
    }    
    
    public boolean isExit(int index) {
        CONSTANT_Methodref_Info method_info = (CONSTANT_Methodref_Info) classfile.constant_pool[index];
        CONSTANT_Class_Info class_info = 
            (CONSTANT_Class_Info) classfile.constant_pool[method_info.class_index];
        CONSTANT_NameAndType_Info name_type_info =
            (CONSTANT_NameAndType_Info) classfile.constant_pool[method_info.name_and_type_index];
        CONSTANT_Utf8_Info class_utf8_info = 
            (CONSTANT_Utf8_Info) classfile.constant_pool[class_info.name_index];
        CONSTANT_Utf8_Info method_utf8_info = 
            (CONSTANT_Utf8_Info) classfile.constant_pool[name_type_info.name_index];

        String class_name = new String(class_utf8_info.bytes);
        String method_name = new String(method_utf8_info.bytes);
        
        if (method_name.equals("exit") && 
            (class_name.equals("java/lang/System") || class_name.equals("java/lang/Runtime"))) {
            return true;
        }
        return false;
    }

    // Chandra's additions
    //Added for size routines -cjk
    public int size() {  //size of classfile with out code
      return classfile.size();
    }

    public int method_size() {
      return classfile.method_size();
    }

    public int code_length() {
      return classfile.code_length();
    }
    
    public ClassFile getClassFile() {
      return classfile;
    }
    public Cp_Info[] removeConstantPoolEntry(int i) {
	int handle = 1;
	Cp_Info[] cp = classfile.removeConstantPoolEntry(i,handle);
	updateRefsToCPool(i);
	return cp;
    }
    private short processIndex(short index) {
	if (modflags[index] == 1) {
		System.err.println("decrementing index: b4: " + index + " after: " + (index-1));
		index--;
	}
	if (index < 1) {
		System.err.println("ERROR, index cannot be reduced: " + index);
		System.exit(-1);
	}
	return(index);
    }
    private void updateRefsToCPool(int index_removed) {
	int i,j, opcode,k,index,cpoolindex;
	String s;
	Cp_Info[] constant_pool = classfile.constant_pool;

	//update all of the instructions that reference the constantpool that are affected by the removal of index_removed
	for (i = 0; i < modflags.length; i++) {
		if (i > index_removed) modflags[i] = 1;
		else modflags[i] = 0;
	}
	System.err.println("Removing index: " + index_removed);
	//fix up cpool references to itself
	for (int c = 1; c < classfile.constant_pool_count; c++) {
		switch(constant_pool[c].getTag()) {
		case 0: //NullInfo 
		case 1: //CONSTANT_Utf8
		case 3: //CONSTANT_Integer
		case 4: //CONSTANT_Float
		case 5: //CONSTANT_Long
		case 6: //CONSTANT_Double
			//these don't reference other entries in the cpool
			break;
		case 7: //CONSTANT_Class
			((CONSTANT_Class_Info) constant_pool[c]).name_index = processIndex(((CONSTANT_Class_Info) constant_pool[c]).name_index);
			break;
		case 8: //CONSTANT_String
			((CONSTANT_String_Info) constant_pool[c]).string_index = processIndex(((CONSTANT_String_Info) constant_pool[c]).string_index);
			break;
		case 9: //CONSTANT_Fieldref
			((CONSTANT_Fieldref_Info)constant_pool[c]).class_index = 
				processIndex(((CONSTANT_Fieldref_Info)constant_pool[c]).class_index);
			((CONSTANT_Fieldref_Info)constant_pool[c]).name_and_type_index = 
				processIndex(((CONSTANT_Fieldref_Info)constant_pool[c]).name_and_type_index);
			break;
		case 10: //CONSTANT_Methodref
			((CONSTANT_Methodref_Info)constant_pool[c]).class_index = 
				processIndex(((CONSTANT_Methodref_Info)constant_pool[c]).class_index);
			((CONSTANT_Methodref_Info)constant_pool[c]).name_and_type_index = 
				processIndex(((CONSTANT_Methodref_Info)constant_pool[c]).name_and_type_index);
			break;
		case 11: //CONSTANT_InterfaceMethodref
			((CONSTANT_InterfaceMethodref_Info)constant_pool[c]).class_index = 
				processIndex(((CONSTANT_InterfaceMethodref_Info)constant_pool[c]).class_index);
			((CONSTANT_InterfaceMethodref_Info)constant_pool[c]).name_and_type_index = 
				processIndex(((CONSTANT_InterfaceMethodref_Info)constant_pool[c]).name_and_type_index);
			break;
		case 12: //CONSTANT_NameAndType
			((CONSTANT_NameAndType_Info)constant_pool[c]).name_index = 
				processIndex(((CONSTANT_NameAndType_Info)constant_pool[c]).name_index);
			((CONSTANT_NameAndType_Info)constant_pool[c]).descriptor_index = 
				processIndex(((CONSTANT_NameAndType_Info)constant_pool[c]).descriptor_index);
			break;
		default: 
			break;
		}
	}
	//now fix up the references to the cpool in the other parts of the class file (noncode)

	classfile.this_class = processIndex(classfile.this_class);
	classfile.super_class = processIndex(classfile.super_class);
	for (j=0; j < classfile.interfaces.length; j++) {
		classfile.interfaces[j] = processIndex(classfile.interfaces[j]);
	}
	for (j=0; j < classfile.attributes.length; j++) {
		//decrement the index first then look at in so that it is what you think it is
		classfile.attributes[j].attribute_name_index = processIndex(classfile.attributes[j].attribute_name_index);
		CONSTANT_Utf8_Info attribute_string = (CONSTANT_Utf8_Info) constant_pool[classfile.attributes[j].attribute_name_index];
            	String attribute_name = new String(attribute_string.bytes);
		if (attribute_name.equals("SourceFile")) {
			((SourceFile_Attribute) classfile.attributes[j]).sourcefile_index = 
				processIndex(((SourceFile_Attribute) classfile.attributes[j]).sourcefile_index);
		} 
	}
       	for (i = 0; i < classfile.methods_count; i++) {
       		Method_Info mi=classfile.methods[i];
		for (j=0; j < mi.attributes.length; j++) {
			mi.attributes[j].attribute_name_index = processIndex(mi.attributes[j].attribute_name_index);
			CONSTANT_Utf8_Info attribute_string = (CONSTANT_Utf8_Info) constant_pool[mi.attributes[j].attribute_name_index];
			System.err.println("found a method attrib to reduce: " + new String(attribute_string.bytes));
            		String attribute_name = new String(attribute_string.bytes);
			if (attribute_name.equals("Code")) {
				System.err.println("found a code attrib to reduce");
				Code_Attribute ca = (Code_Attribute) mi.attributes[j];
				for (k = 0; k < ca.exception_table_length; k++) {
    					if (ca.exceptions[k].catch_type != 0) {
						System.err.println("found a code catchtype to reduce: "  + ca.exceptions[k].catch_type);
        					ca.exceptions[k].catch_type = processIndex(ca.exceptions[k].catch_type);
						System.err.println("after a code catchtype to reduce: " +  ca.exceptions[k].catch_type);
   					}
				}
				for (k = 0; k < ca.attributes.length; k++) { //LocalVariableTable and LineNumberTable
					System.err.println("found a code attrib to reduce: " + ca.attributes[k].attribute_name_index);
					ca.attributes[k].attribute_name_index = processIndex(ca.attributes[k].attribute_name_index);
					System.err.println("after a code catchtype to reduce: " + ca.attributes[k].attribute_name_index);
				}
			} else if (attribute_name.equals("Exceptions")) {
				Exceptions_Attribute ea = (Exceptions_Attribute) mi.attributes[j];
				for (k = 0; k < ea.exception_index_table.length; k++) {
					ea.exception_index_table[k] = processIndex(ea.exception_index_table[k]);
				}
			}
		}
		mi.name_index = processIndex(mi.name_index);
		mi.descriptor_index = processIndex(mi.descriptor_index);
       	}
       
        //field info
        for (i = 0; i < classfile.field_count; i++) {
                Field_Info fi=classfile.fields[i];
		System.err.println("processing a field attribs: " + fi.attributes.length + " " + fi.attribute_count);
		System.err.println("processing a field ni before: " + fi.name_index + " " + fi.descriptor_index);
		fi.name_index = processIndex(fi.name_index);
		fi.descriptor_index = processIndex(fi.descriptor_index);
		System.err.println();
		for (j=0; j < fi.attributes.length; j++) {
			fi.attributes[j].attribute_name_index = processIndex(fi.attributes[j].attribute_name_index);
			CONSTANT_Utf8_Info attribute_string = (CONSTANT_Utf8_Info) constant_pool[fi.attributes[j].attribute_name_index];
			System.err.println("found a field attrib to reduce: " + new String(attribute_string.bytes) + " index: "  
				+ fi.attributes[j].attribute_name_index);
            		String attribute_name = new String(attribute_string.bytes);
			if (attribute_name.equals("ConstantValue")) {
				((ConstantValue_Attribute) fi.attributes[j]).constantvalue_index = 
					processIndex(((ConstantValue_Attribute) fi.attributes[j]).constantvalue_index);
			} 
		}
        }

	//now fix each instruction that references the cpool
    	for (Enumeration e = routines.elements(); e.hasMoreElements(); ) {
        	Routine r = (Routine) e.nextElement();
		int tmp1 = -1;
		if ((tmp1 = r.usesIndexGreaterThan(index_removed)) != -1) {
		   //tmp1 is the position in r.cpool_refs that contains an index into the cpool larger than index_removed
		   for (cpoolindex = tmp1; cpoolindex < r.cpool_refs.size(); cpoolindex++) {
			Vector v = (Vector)r.cpool_refs_insts.elementAt(cpoolindex);
			for (i = 0; i < v.size(); i++) {
			        s = (String)v.elementAt(i);
				int tmpindex = s.indexOf('#');
				if (tmpindex == -1) { 
					System.err.println("ERROR in Routine:updateRefsToCPool(int): opcode is -1, s is: " + s);
					System.exit(-1);
				}
				int myop = Integer.parseInt(s.substring(0,tmpindex)); //unused
				k  = Integer.parseInt(s.substring(tmpindex+1,s.length())); //the index of the instruction
				Instruction instr = r.getInstruction(k);
				opcode = instr.getOpcode();
				System.err.println("opcodes better be the same: " + myop + " " + opcode + " inst index: " + k);
				boolean flag = false;
				index = -1;
				switch(opcode) {
			   	case 185: //invokeinterface (4 operands)
					index = instr.getDoubleOperandValue();
					flag = true;
				case 18: // ldc
				case 19: // ldc_w
				case 178: // getstatic
				case 179: // putstatic
				case 180: // getfield
				case 181: // putfield
				case 187: // new
				case 189: // anewarray
				case 192: // checkcast
				case 197: // multianewarray
				case 193: // instanceof
				case 20: // ldc2_w  holds a long or double index, and following
			   	case 182: //method calls (2 operands)
			   	case 183: //method calls (2 operands)
			   	case 184: //method calls (2 operands)
					if (flag == false) index = instr.getOperandValue();
					System.err.println("processing INST operand_index: " + index + " opcode: " + opcode +
						" inst_index " + k);
					if (index > index_removed) {
						if (index > 255) {
							System.err.println("ERROR, index cannot be converted to short: " 
								+ index);
							System.exit(-1);
						}
						short tmp = (short)((index >>> 0) & 0xff);
						instr.setCpoolIndex((int)processIndex(tmp));
						if (opcode != 185) {
							System.err.println("AFTER processing INST index: " + instr.getOperandValue());
						} else {
							System.err.println("AFTER 185 processing INST index: " + instr.getDoubleOperandValue());
						}
						r.setInstruction(instr,i);
					}
					break;
			   	default: 
					break;
				}
			}
		   }
		}	
        }
	return;
    }



}

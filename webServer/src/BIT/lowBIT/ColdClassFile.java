package BIT.lowBIT;

import java.io.*;
import java.util.*;

public class ColdClassFile extends ClassFile {
  //
  // data members as described on page 84 of
  // "The Java Virtual Machine Specification"
  // by Tim Lindhom and Frank Yellin
  //

  public static final short SUP = 32;
  public static final short FIN = 16;
  public short hot_cold;

  public ColdClassFile(ClassFile hotclass, String name) {
      //Create a classfile from scratch
      //name must be the packagename/filename  without the .class extension just like hotclass

      //init of classfile() called implicitly - sets the magic number

   try {

      // version number of the compiler that produced this class file
      minor_version = hotclass.minor_version;
      major_version = hotclass.major_version;
      
      // constant pool
      constant_pool_count = 7;
      constant_pool = new Cp_Info[constant_pool_count];
      constant_pool[1] = new CONSTANT_Class_Info((short)2); //index 2 is the name index of this cinfo 
      constant_pool[2] = new CONSTANT_Utf8_Info("java/lang/Object");

      constant_pool[3] = new CONSTANT_Class_Info((short)4); //index 4 is the name index of this cinfo 
      //get the prefix to the class name in hotclass

//use the following to send the name in
      //CONSTANT_Class_Info ci = (CONSTANT_Class_Info) hotclass.constant_pool[hotclass.this_class];
      //CONSTANT_Utf8_Info info = (CONSTANT_Utf8_Info) hotclass.constant_pool[ci.name_index];
      //String name = new String(info.bytes);
      //int index = name.lastIndexOf('/');
      //if (index != -1) 
	//name = new String((name.substring(0,index)).concat(Map.nextName());
     
      constant_pool[4] = new CONSTANT_Utf8_Info(name);

      //add the name of the hot class (the one sent in) off of which you are building
      CONSTANT_Class_Info ci = (CONSTANT_Class_Info) hotclass.constant_pool[hotclass.this_class];
      CONSTANT_Utf8_Info info = (CONSTANT_Utf8_Info) hotclass.constant_pool[ci.name_index];
      hot_cold = 5;
      constant_pool[5] = new CONSTANT_Class_Info((short)6);
      constant_pool[6] = new CONSTANT_Utf8_Info(info.bytes);

      // access flags
      access_flags = SUP|FIN;  //package public and final with new invokespecial semantics 
      
      // this_class needs to be assigned later once there is an index into the constant
      // pool for this new class name of type CONSTANT_Class 
      // super_class must be the index of type CONSTANT_Class to Object
      // once in the constant pool (done above
      super_class = 1;
      this_class =  3;
      
      // interfaces
      interface_count = (short) 0;
      // fields
      field_count = 0; //each time a field is to be added a field_info is created then
				//added via addField(fi) below
      // methods
      methods_count = 0; //each time a method is to be added a method_info is created then
				//added via addMethod(mi) below
      // attributes
      attributes_count = 0;  //sourcefile is the only top level attribute and we are not
				//adding that here.  this must remain 0
      
    } catch (Exception e) {
      System.out.println("Exception: ColdClassFile(ClassFile,String)");
      System.out.println(e.getMessage());
    }
  }
}

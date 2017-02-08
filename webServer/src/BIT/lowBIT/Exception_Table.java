/* Exception_Table.java
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
import BIT.highBIT.InstructionArray;
import BIT.highBIT.Instruction;

public class Exception_Table {
  // data members
  
  // start_pc and end_pc indicate the range at which the 
  //   exception handler is active
  public short start_pc;
  public short end_pc;
  
  // handler_pc indicates the start of the exception handler
  public short handler_pc;
  
  // catch_type is an index into the constant table and indicates
  //   the class of exceptions that this handler will catch
  public short catch_type;
  
  // constructor
  public Exception_Table(DataInputStream iStream, Cp_Info[] cp) throws IOException {
    start_pc = (short) iStream.readUnsignedShort();
    end_pc = (short) iStream.readUnsignedShort();
    handler_pc = (short) iStream.readUnsignedShort();
    catch_type = (short) iStream.readUnsignedShort();
//cjk
    if (catch_type != 0) {
    	int index = ((CONSTANT_Class_Info) cp[catch_type]).name_index;
   }
  }

  public void updateOffset(InstructionArray instructions, Vector modified_instructions) {
	boolean fStartFound = false, fEndFound = false, fHandlerFound = false;

	for (Enumeration e = instructions.elements(); e.hasMoreElements(); ) {
		Instruction instr = (Instruction) e.nextElement();
		int offset = instr.getOffset();

		if (offset == start_pc && !fStartFound) {
			Instruction target = (Instruction) modified_instructions.elementAt(instr.getModifiedIndex());
            start_pc = (short) target.getOffset();
			fStartFound = true;
			if (fStartFound && fEndFound && fHandlerFound) return;
		}
		if (offset == end_pc && !fEndFound) {
			Instruction target = (Instruction) modified_instructions.elementAt(instr.getModifiedIndex());
			end_pc = (short) target.getOffset();
			fEndFound = true;
			if (fStartFound && fEndFound && fHandlerFound) return;
		}
		if (offset == handler_pc && !fHandlerFound) {
			Instruction target = (Instruction) modified_instructions.elementAt(instr.getModifiedIndex());
			handler_pc = (short) target.getOffset();
			fHandlerFound = true;
			if (fStartFound && fEndFound && fHandlerFound) return;
		}
	}
  }

  public void write(DataOutputStream oStream) throws IOException {
    oStream.writeShort((int) start_pc);
    oStream.writeShort((int) end_pc);
    oStream.writeShort((int) handler_pc);
    oStream.writeShort((int) catch_type);
  }
}

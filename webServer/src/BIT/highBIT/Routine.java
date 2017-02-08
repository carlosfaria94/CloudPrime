/* Routine.java
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
 * Contains information about a method within a Java <tt>.class</tt> file.
 *
 * @author  <a href="mailto:hanlee@cs.colorado.edu">Han B. Lee</a>
 * @see ClassInfo
 * @see classFile.Method_Info
 **/
public class Routine {

//cjk
    public Vector cpool_refs = null;
    public Vector cpool_refs_insts = null;
    private Vector children_callgraph = null;
    public Vector getChildrenCG() {return children_callgraph;}
    private Vector parents_callgraph = null;
    public Vector getParentsCG() {return parents_callgraph;}

    /**  
     * Method information in classFile package.
     *
     * @see classFile.Method_Info
     */
    protected Method_Info method;

    protected ClassInfo classinfo;
     /** 
     * Name of this method. 
     * It gets filled in when the constructor is called.
     * <br>
     * One can read this value by calling getMethodName() method.
     * @see Routine#getMethodName
     */
    protected String method_name;
    
    /**
     * Descriptor for this method. 
     * It gets filled in when the constructor is called.
     * <br>
     * One can read this value calling getDescriptor() method.
     * @see Routine#getDescriptor
     */
    protected String descriptor;
    
    /** 
     * Maximum stack size for this method. 
     * It gets filled in when the constructor is called.
     * <br>
     * One can read this value by calling getMaxStack() method.
     * @see Routine#getMaxStack
     */
    protected short max_stack = 0;
    
    /**
     * Maximum number of locals for this method. 
     * It gets filled in when the constructor is called.
     * <br>
     * One can read this value by calling getMaxLocals() method.
     * @see Routine#getMaxLocals
     */
    protected short max_locals = 0;
    
    /** 
     * Length of the code in bytes of this method.
     * <br>
     * One can read this value by calling getCodeLength() method.
     * @see Routine#getCodeLength
     */
    protected int code_length = 0;
    
    /**
     * Actual array containing the bytecodes.
     * <br>
     * This is an array of byte that represent the bytecodes of this method.
     * One can read this value by calling getCode() method.
     * @see Routine#getCode
     */
    public byte code[];
    
    /**
     * Bytecodes (code) broken into a number of instructions in an array.
     * <br>
     * Each element in this array is a valid Java Virtual Machine
     * instruction.  Each instruction contains the opcode and any
     * operands.
     *
     * @see Instruction
     * @see Routine#getInstructions
     * @see InstructionArray
     */
    public InstructionArray instructions;

    /** 
     * This is the Vector to hold the modified code.
     * <br>
     */

    public Vector modified_instructions;

    /**
     * Bytecodes (code) broken into a number of basic blocks in an array.
     * <br>
     * Each element in this array is a basic block having one entry point
     * and one exit point.
     *
     * @see BasicBlockArray
     * @see Routine#getBasicBlocks
     */

    public BasicBlockArray basic_blocks;

    public BasicBlockArray modified_basic_blocks;

    /**
     * Temporary Vectors used to construct the array of basic blocks.
     * User should not need to deal with these entities.
     * <br>
     *
     * @see java.util.Vector
     * @see Routine#getTempBasicBlocks
     */
    private Vector bbs;
    private Vector bbs_start;
    
    /**
     * Instruction opcode is an int because Java treats byte as signed. 
     */
    private int opcode;
    
    public Cp_Info constant_pool[];
    
    /** 
     * Creates a new Routine class which breaks down methods into
     * more easily manageble entities.
     * <br>
     * The only attribute that it looks at is the "Code" attribute.
     *
     * @param	method  the method to be analyzed
     * @param	constant_pool	the constant pool for the .class file
     * @see classFile.Method_Info
     * @see classFile.Cp_Info
     */
    public Routine(Method_Info method, Cp_Info[] constant_pool, ClassInfo classinfo) {
        this.method = method;
        this.classinfo = classinfo;
        this.constant_pool = constant_pool;
        
        // get method name
        CONSTANT_Utf8_Info info = (CONSTANT_Utf8_Info) constant_pool[method.name_index];
        method_name = new String(info.bytes);
        
        // get descriptor in a string form
        info = (CONSTANT_Utf8_Info) constant_pool[method.descriptor_index];
        descriptor = new String(info.bytes);
        
        // loop through the attributes until code attribute is found
        //   when found, extract info
        for (int i = 0; i < method.attribute_count; i++) {
            Attribute_Info attribute = method.attributes[i];
            info = (CONSTANT_Utf8_Info) constant_pool[attribute.attribute_name_index];
            String attribute_name = new String(info.bytes);
            if (attribute_name.equals("Code")) {
                Code_Attribute code_attribute = (Code_Attribute) attribute;
                max_stack = code_attribute.max_stack;
                max_locals = code_attribute.max_locals;
                code_length = code_attribute.code_length;
                code = new byte[code_length];
                System.arraycopy(code_attribute.code, 0, code, 0, code_length);
            }
        }
	/*if (System.getProperty("DEBUGROUTINE") != null) {
		System.err.println("Routine: " + method_name + ":" 
			+ descriptor);
	}*/
        analyzeCode();
        findBasicBlocks();
    }
    
    public int getLineNumber(int pc) {
        for (int i = 0; i < method.attribute_count; i++) {
            Attribute_Info attribute = method.attributes[i];
            CONSTANT_Utf8_Info info = (CONSTANT_Utf8_Info) constant_pool[attribute.attribute_name_index];
            String attribute_name = new String(info.bytes);
            if (attribute_name.equals("Code")) {
                Code_Attribute code_attribute = (Code_Attribute) attribute;
                for (int j = 0; j < code_attribute.attribute_count; j++) {
                    Attribute_Info attr = code_attribute.attributes[j];
                    CONSTANT_Utf8_Info attr_info = (CONSTANT_Utf8_Info) constant_pool[attr.attribute_name_index];
                    String attr_name = new String(attr_info.bytes);
                    if (attr_name.equals("LineNumberTable")) {
                        return ((LineNumberTable_Attribute)attr).getLineNumber(pc);
                    }
                }
                return -1;
             }
        }
        return -1;
    }

    public void addBefore(String classname, String methodname, Object arg) {
        if (code == null)
            return;
        
        Instruction instr = (Instruction) instructions.firstElement();
        instr.addBefore(classname, methodname, arg);
    }
    
    public void addAfter(String classname, String methodname, Object arg) {
        if (code == null)
            return;
        
        for (Enumeration i = instructions.elements(); i.hasMoreElements(); ) {
            Instruction instr = (Instruction) i.nextElement();
            short opcode = (short) instr.getOpcode();
            if (opcode >= 0xac && opcode <= 0xb1)
                instr.addBefore(classname, methodname, arg);
        }
    }
    
    public void adjInstrOffsets(int start, int size, boolean before) {
        for (Enumeration e = modified_instructions.elements(); e.hasMoreElements(); ) {
            Instruction instr = (Instruction) e.nextElement();
            
            int offset = instr.getOffset();
            if (offset != -1) {
                if (before) {
                    if (offset >= start) {
                        instr.setOffset(offset + size);
                    }
                }
                else {
                    if (offset > start) {
                        instr.setOffset(offset + size);
                    }
                }
            }
        }    
    }

    public void adjOffsets(int start, int size, boolean before) {
        if (code == null)
            return;
        
        int jmp_target = 0;
        int instruction_offset = 0;

        for (Enumeration e = modified_basic_blocks.elements(); e.hasMoreElements(); ) {
            BasicBlock basic = (BasicBlock) e.nextElement();
            Instruction instr = (Instruction) modified_instructions.elementAt(basic.getEndAddress());
            if (instr.getOffset() != -1) {
                switch (instr.getOpcode()) {
                case InstructionTable.ifeq:
                case InstructionTable.ifne:
                case InstructionTable.iflt:
                case InstructionTable.ifge:
                case InstructionTable.ifgt:
                case InstructionTable.ifle:
                case InstructionTable.ifnonnull:
                case InstructionTable.ifnull:
                case InstructionTable.if_acmpeq:
                case InstructionTable.if_acmpne:
                case InstructionTable.if_icmpeq:
                case InstructionTable.if_icmpne:
                case InstructionTable.if_icmplt:
                case InstructionTable.if_icmpge:
                case InstructionTable.if_icmpgt:
                case InstructionTable.if_icmple:
                case InstructionTable.GOTO:
                case InstructionTable.goto_w:
                case InstructionTable.jsr:
                case InstructionTable.jsr_w:
                    instruction_offset = instr.getOffset();
                    jmp_target = instruction_offset + instr.getOperandValue();
                    
                    if (instruction_offset == start) {
                        if (before) {
                            if (jmp_target < start) {
                                jmp_target -= size;
                            }
                        }
                        else {
                            if (jmp_target > start) {
                                jmp_target += size;
                            }
                        }
                    }
                    else if (instruction_offset < start) {
                        if ((before && (jmp_target > (start - size))) || (!before && (jmp_target > start))) {
                                jmp_target += size;
                        }
                    }
                    else {
                        if ((before && (jmp_target <= start)) 
                            || (!before && (jmp_target <= (start + size)))) {
                                jmp_target -= size;
                        }
                    }
                    jmp_target -=  instruction_offset;
                    ((InstructionDoubleOperand)instr).setOperandValue((short) jmp_target);
                    break;
                case InstructionTable.lookupswitch:
                    InstructionLookupswitch lookup = (InstructionLookupswitch) instr;
                    instruction_offset = lookup.getOffset();
                    jmp_target = instruction_offset + lookup.getDefault();
                    
                    if (instruction_offset == start) {
                        if (before) {
                            if (jmp_target < start) {
                                jmp_target -= size;
                            }
                        }
                        else {
                            if (jmp_target > start) {
                                jmp_target += size;
                            }
                        }
                    }
                    else if (instruction_offset < start) {
                        if ((before && (jmp_target > (start - size))) || (!before && (jmp_target > start))) {
                                jmp_target += size;
                        }
                    }
                    else {
                        if ((before && (jmp_target <= start)) 
                            || (!before && (jmp_target <= (start + size)))) {
                                jmp_target -= size;
                        }
                    }
                
                    jmp_target -=  instruction_offset;
                    lookup.setDefault(jmp_target);
                    // process npair number of offsets
                    for (int i = 0; i < lookup.getNpairs(); i++) {
                        int offset = (lookup.offset1[i] << 24) | (lookup.offset2[i] << 16) |
                            (lookup.offset3[i] << 8) | lookup.offset4[i];
                        jmp_target = instruction_offset + offset;

                        if (instruction_offset == start) {
                            if (before) {
                                if (jmp_target < start) {
                                    jmp_target -= size;
                                }
                            }
                            else {
                                if (jmp_target > start) {
                                    jmp_target += size;
                                }
                            }
                        }
                        else if (instruction_offset < start) {
                            if ((before && (jmp_target > (start - size))) || (!before && (jmp_target > start))) {
                                    jmp_target += size;
                            }
                        }
                        else {
                            if ((before && (jmp_target <= start)) 
                                || (!before && (jmp_target <= (start + size)))) {
                                    jmp_target -= size;
                            }
                        }
                        jmp_target -=  instruction_offset;
                        lookup.offset1[i] = (jmp_target >>> 24) & 0xff;
                        lookup.offset2[i] = (jmp_target >>> 16) & 0xff;
                        lookup.offset3[i] = (jmp_target >>> 8) & 0xff;
                        lookup.offset4[i] = (jmp_target >>> 0) & 0xff;
                    }
                    break;
                case InstructionTable.tableswitch:
                    InstructionTableswitch table = (InstructionTableswitch) instr;
                    instruction_offset = table.getOffset();
                    jmp_target = instruction_offset + table.getDefault();

                    if (instruction_offset == start) {
                        if (before) {
                            if (jmp_target < start) {
                                jmp_target -= size;
                            }
                        }
                        else {
                            if (jmp_target > start) {
                                jmp_target += size;
                            }
                        }
                    }
                    else if (instruction_offset < start) {
                        if ((before && (jmp_target > (start - size))) || (!before && (jmp_target > start))) {
                                jmp_target += size;
                        }
                    }
                    else {
                        if ((before && (jmp_target <= start)) 
                            || (!before && (jmp_target <= (start + size)))) {
                                jmp_target -= size;
                        }
                    }
                    jmp_target -=  instruction_offset;
                    table.setDefault(jmp_target);
                    
                    // process npair number of offsets
                    for (int i = 0; i < (table.high - table.low + 1); i++) {
                        int offset = (table.offset1[i] << 24) | (table.offset2[i] << 16) |
                            (table.offset3[i] << 8) | table.offset4[i];
                        jmp_target = instruction_offset + offset;
                        if (instruction_offset == start) {
                            if (before) {
                                if (jmp_target < start) {
                                    jmp_target -= size;
                                }
                            }
                            else {
                                if (jmp_target > start) {
                                    jmp_target += size;
                                }
                            }
                        }
                        else if (instruction_offset < start) {
                            if ((before && (jmp_target > (start - size))) || (!before && (jmp_target > start))) {
                                    jmp_target += size;
                            }
                        }
                        else {
                            if ((before && (jmp_target <= start)) 
                                || (!before && (jmp_target <= (start + size)))) {
                                    jmp_target -= size;
                            }
                        }
                        jmp_target -=  instruction_offset;
                        table.offset1[i] = (jmp_target >>> 24) & 0xff;
                        table.offset2[i] = (jmp_target >>> 16) & 0xff;
                        table.offset3[i] = (jmp_target >>> 8) & 0xff;
                        table.offset4[i] = (jmp_target >>> 0) & 0xff;
                    }
                    break;
                default:
                    break;
                }
            }
        }
    }


    /**
    * Modifies the modified_basic_block array so that it reflects changes in the
    * modified_instructions array.
    */

    public void adjModifiedBasicBlocks(int affected_start, int offset, boolean before) {
        if (code == null)
            return;

        for (Enumeration e = modified_basic_blocks.elements(); e.hasMoreElements(); ) {
            BasicBlock bb = (BasicBlock) e.nextElement();
            int start_address = bb.getStartAddress();
            int end_address = bb.getEndAddress();

            if (start_address > affected_start)
                bb.setStartAddress(start_address + offset);
            if (end_address >= affected_start)
                bb.setEndAddress(end_address + offset);
         }

    }

    /**
    * Analyzes code buffer and breaks it down into an array of instructions.
    * <br>
    * This routine puts the resulting instructions into an array of
    * instructions so that one can analyze them.
    *
    * @see Instruction
    */
    private void analyzeCode() {
        int instruction_count = 0;
        int byte_count = 0;

        if (code == null) {
            Instruction instrs[] = new Instruction[0];
            instructions = new InstructionArray(0, instrs);
            modified_instructions = new Vector(0);
            return;
        }

        ByteArrayInputStream baiStream = new ByteArrayInputStream(code);
        DataInputStream iStream = new DataInputStream(baiStream);
        Instruction instrs[] = new Instruction[code_length];
        
        while (byte_count != code_length) {
            try {
                // get the opcode of instruction
                opcode = iStream.readUnsignedByte();
            }
            catch(IOException e) {
                System.out.println("Error reading code buffer");
            }
	    /*if (System.getProperty("DEBUGROUTINE") != null) {
		System.err.println("opcode: " + opcode + " " 
			+ InstructionTable.OpcodeName[opcode] );
	    }*/
            // based on the number of operands of the instruction
            //   decode it accordingly
            switch (InstructionTable.OperandNumber[opcode]) {
            case 0: 
                // instructions that take no operand
                instrs[instruction_count] = new Instruction(opcode, byte_count, this);
                break;
            case 1:
                // instructions that take one operand
                instrs[instruction_count] = new InstructionSingleOperand(opcode, iStream, byte_count, this);
                break;
            case 2:
                // instructions that take two operands
                instrs[instruction_count] = new InstructionDoubleOperand(opcode, iStream, byte_count, this);
                break;
            case 3:
                // instructions that take three operands
                instrs[instruction_count] = new InstructionTripleOperand(opcode, iStream, byte_count, this);
                break;
            case 4:
                // instructions that take four operands
                instrs[instruction_count] = new InstructionQuadOperand(opcode, iStream, byte_count, this);
                break;
            case 9:
                // wide instruction
                instrs[instruction_count] = new InstructionWide(opcode, iStream, byte_count, this);
                break;
                
            case -1:
                // there are only two variable length instructions in JVM
                //		tableswitch & lookupswitch
                if (opcode == InstructionTable.tableswitch) {
                    instrs[instruction_count] = new InstructionTableswitch(opcode, iStream, byte_count, this);
                }
                else if (opcode == InstructionTable.lookupswitch) {
                    instrs[instruction_count] = new InstructionLookupswitch(opcode, iStream, byte_count, this);
                }
                break;
            case -2:
                System.out.println("An invalid opcode was encountered: " 
			+ opcode);
            	System.out.println("-2 returned from InstructionTable.OperandNumber[opcode] call");
		System.out.flush();
		System.exit(-1);
                break;
            default:
                break;
            }
            instrs[instruction_count].setIndex(instruction_count);
            instrs[instruction_count].setModifiedIndex(instruction_count);
            instrs[instruction_count].setOffset(byte_count);
            byte_count += ((Instruction) instrs[instruction_count]).getLength();
            instruction_count++;
        }
        instructions = new InstructionArray(instruction_count, instrs);
        // also make a Vector of the same instructions to hold modified code
        modified_instructions = new Vector(instruction_count);
        for (int j = 0; j < instruction_count; j++) {
            instrs[j].setModified();
            modified_instructions.insertElementAt(instrs[j], j);
        }
        //cjk
        try{
        iStream.close();
        } catch(IOException e) {
        	System.err.println("IO Exception on close of IStream");
        }
    }

    private void findBasicBlocks() {
        if (code == null)
            return;
        
        bbs = new Vector();
        bbs_start = new Vector();
        bbs_start.addElement(new Integer(0));
        bbs.addElement(new BasicBlock(this, 0));

        basic_blocks = new BasicBlockArray(bbs);
        modified_basic_blocks = new BasicBlockArray(basic_blocks);
    }
    public static synchronized int insertInt(Vector v, int val) {
	//only run when property BITCOLD is on
	//insert val into v so that v remains in increasing order
	int i, k;
	for (i = 0; i < v.size(); i++) {
		k = ((Integer) v.elementAt(i)).intValue();
		if (k > val) {
			//System.err.println("Inserting " + val + " at posit " + (i-1)
				//+ " size of v is " + v.size() + " i is " + i);
			v.insertElementAt(new Integer(val),i);
			return(i);
		}	
	}
	v.addElement(new Integer(val));
	//System.err.println("Added " + val + " size " + v.size());
	return(v.size()-1);
    }
//cjk
    public int usesIndexGreaterThan(int val) {
	//only run when property BITCOLD is on
	//short cuts
	if (cpool_refs.size() == 0) return -1;
	int k = ((Integer) cpool_refs.firstElement()).intValue();
	if (k > val) return 0; //all are larger than val

	for (int i = (cpool_refs.size()-1); i >= 0; i--) {
		k = ((Integer) cpool_refs.elementAt(i)).intValue();
		if (k <= val) {
			if (i == (cpool_refs.size()-1)) return -1; //none are larger than val
			return(i+1); //should never be equal however
		}
	}
	//should never hit this b/c they are in ascending order
	System.err.println("ERROR, Routine.usesIndexGreaterThan failed: " + val);
	System.exit(-1);
	return(0);  //all are larger than val
    }
    public boolean usesIndex(int val) {
	//only run when property BITCOLD is on
	int i, k;
	for (i = 0; i < cpool_refs.size(); i++) {
		k = ((Integer) cpool_refs.elementAt(i)).intValue();
		if (k == val) return true;
		if (k > val) return false;
	}
	return false;
    }
	
//cjk
    private void countRef(int index, int opcode, int inst_index, boolean add) {
	//only run when property BITCOLD is on

	int irefindex = -1;
	Vector iref = null;
	//index is the operand value we are interested in
	//opcode is the opcode for the instr, unused
	//inst_index is the index of the instruction
	//if (add == true) System.err.println("COUNTREF operand, opcode, instruction: " + index + " " + opcode + " " + inst_index); 

	constant_pool[index].count++;
	if ((irefindex = cpool_refs.indexOf(new Integer(index))) == -1) {
		irefindex = insertInt(cpool_refs, index);
		iref = new Vector();
		if (irefindex == (cpool_refs.size()-1)) {
			cpool_refs_insts.addElement(iref);
		} else {
			cpool_refs_insts.insertElementAt(iref,irefindex);
		}
	} else {
		iref = (Vector)cpool_refs_insts.elementAt(irefindex);
	}
	if (add == true) {
		String s = new String(opcode + "#" + inst_index);
		iref.addElement(s);
	}
	cpool_refs_insts.setElementAt(iref,irefindex);
	return;
   }
//cjk
    private void findCPoolRefs() {
	//only run when property BITCOLD is on

	//inserts the cpool indices used by this method into the cpool_refs vector in
	//increasing order

	//appends to index counts for each cpool entry touched

	boolean flag = false;
	int index = -1;
	int i,j,k;
	Vector iref = null;
	int irefindex = -1;
	try {
	    if (code == null) return;
	    cpool_refs = new Vector();
	    cpool_refs_insts = new Vector();
	    for (Enumeration b = basic_blocks.elements(); b.hasMoreElements(); ) {
		BasicBlock bb = (BasicBlock) b.nextElement();
		int start_address = bb.getStartAddress();
		int end_address = bb.getEndAddress();
		for (j = start_address; j <= end_address; j++) {
			Instruction instr = bb.routine.getInstruction(j);
			System.err.println("INST: " + start_address + " " + end_address + " " + j);
			int opcode = instr.getOpcode();
			//Check for opcodes that reference the constant pool
			flag = false;
			index = -1;
			switch(opcode) {
			case 18: // ldc		//InstructionSingleOperands
				//we shouldn't need this Java should do it for us
				//index = ((InstructionSingleOperand) instr).getOperandValue();
			case 19: // ldc_w	//InstructionDoubleOperand
				index = instr.getOperandValue();
				System.err.println("opcode operand: " + opcode + " " + index);
				countRef(index, opcode, j,true);
				if (constant_pool[index] instanceof CONSTANT_String_Info) {
					CONSTANT_String_Info att7 = (CONSTANT_String_Info) constant_pool[index];
					if (att7.string_index != -1) {
						countRef(att7.string_index, opcode, j,false);
					}
				}
				break;
			case 178: // getstatic
			case 179: // putstatic
			case 180: // getfield
			case 181: // putfield		//InstructionDoubleOperands
				index = -1;
				index = instr.getOperandValue();
				System.err.println("opcode operand: " + opcode + " " + index);
				countRef(index, opcode, j,true);
				System.err.println("Field Ref of CPool: " + index + " instruction: " + j);
				CONSTANT_Fieldref_Info att7 = (CONSTANT_Fieldref_Info) constant_pool[index];
				CONSTANT_NameAndType_Info att1 = (CONSTANT_NameAndType_Info) 
					constant_pool[att7.name_and_type_index];
				countRef(att1.name_index, opcode, j,false);
				countRef(att1.descriptor_index, opcode, j,false);
				countRef(att7.name_and_type_index, opcode, j,false);
				countRef(att7.class_index, opcode, j,false);
				CONSTANT_Class_Info att2 = (CONSTANT_Class_Info) constant_pool[att7.class_index];
				countRef(att2.name_index, opcode, j,false);
				if (att2.name_index == classinfo.classfile.this_class) {
					((Field_Info)findInfo(att1.name_index, att1.descriptor_index,0)).count++;
				}
				break;
			case 187: // new			//InstructionDoubleOperand
			case 189: // anewarray			//InstructionDoubleOperand
			case 192: // checkcast			//InstructionDoubleOperand
			case 197: // multianewarray 		//InstructionTripleOperand
			case 193: // instanceof			//InstructionDoubleOperand
				index = -1;
				index = instr.getOperandValue();
				System.err.println("opcode operand: " + opcode + " " + index);
				countRef(index, opcode, j, true);
				CONSTANT_Class_Info att3 = (CONSTANT_Class_Info) constant_pool[index];
				countRef(att3.name_index, opcode, j, false);
				break;
			case 20: // ldc2_w  holds a long or double index, and following		//InstructionDoubleOperand
				 // index is unuseable (null_info) (take care of this one here)
				index = -1;
				index = instr.getOperandValue();
				System.err.println("opcode operand: " + opcode + " " + index);
				countRef(index, opcode, j,true);
				countRef(index+1, opcode, j, false);
				break;
			case 172: //returns
			case 173: //returns
			case 174: //returns
			case 175: //returns
			case 176: //returns
			case 177: //returns
				break;
			case 185: //invokeinterface (4 operands)	//InstructionQuadOperand
				index = instr.getDoubleOperandValue();
				flag = true;
				break;
			case 182: //method calls (2 operands)	//InstructionDoubleOperand
			case 183: //method calls (2 operands)	//InstructionDoubleOperand
			case 184: //method calls (2 operands)	//InstructionDoubleOperand
				flag = true;
				index = instr.getOperandValue();
				break;
			default: 
				break;
			}
			if (flag == true) {
				System.err.println("opcode operand: " + opcode + " " + index);
				if (index == -1) {
					System.out.println("-1 index: " + opcode);
				}
				if (opcode == 185) {  //Interface instant
				  CONSTANT_InterfaceMethodref_Info imri = 
				    (CONSTANT_InterfaceMethodref_Info) constant_pool[index];
				  CONSTANT_NameAndType_Info nati = 
				    (CONSTANT_NameAndType_Info) constant_pool[imri.name_and_type_index];
				  CONSTANT_Class_Info ci2 = (CONSTANT_Class_Info)
					constant_pool[imri.class_index];
				  countRef(index, opcode, j,true);
				  countRef(imri.name_and_type_index, opcode, j,false);
				  countRef(nati.name_index, opcode, j,false);
				  countRef(nati.descriptor_index, opcode, j,false);
				  countRef(imri.class_index, opcode, j,false);
				  countRef(ci2.name_index, opcode, j,false);
				  if (ci2.name_index == classinfo.classfile.this_class) {
				  	((Method_Info)findInfo(nati.name_index, nati.descriptor_index,1)).count++;
				  }

				} else {  //opcode !=185
				  CONSTANT_Methodref_Info mri = (CONSTANT_Methodref_Info) 
					constant_pool[index];
				  CONSTANT_NameAndType_Info nati = (CONSTANT_NameAndType_Info)
					constant_pool[mri.name_and_type_index];
				  CONSTANT_Class_Info ci2 = (CONSTANT_Class_Info)
					constant_pool[mri.class_index];
				  countRef(index, opcode, j,true);
				  countRef(mri.name_and_type_index, opcode, j,false);
				  countRef(nati.name_index, opcode, j,false);
				  countRef(nati.descriptor_index, opcode, j,false);
				  countRef(mri.class_index, opcode, j, false);
				  countRef(ci2.name_index, opcode, j, false);
				  if (ci2.name_index == classinfo.classfile.this_class) {
				  	((Method_Info)findInfo(nati.name_index, nati.descriptor_index,1)).count++;
				  }
				}
				flag = false;
			   } //flag true
			 } //inst loop
		}//bb loop
	} catch(Exception e) {
		System.err.println("Exception in Routine Constructor");
		System.err.println(e.getMessage());
		System.err.println(e.toString());
		e.printStackTrace();
	}
    }
    
    /**
    * Returns the index of instruction that starts at specified offset in code buffer.
    * If not found, return -1.
    */
    public int indexOfInstruction(int offset) {
        if (code == null)
            return -1;
        
        int i = 0;
        Instruction instruction;
        
        for (Enumeration e = instructions.elements(); e.hasMoreElements(); )
        {
            instruction = (Instruction) e.nextElement();
            if (offset == instruction.getOffset())
                return i;
            i++;
        }
        // not found, return -1.
        return -1;
    }
    
    public boolean existBasicBlockStart(int address) {
        Integer addr;
        
        for (Enumeration e = bbs_start.elements(); e.hasMoreElements(); ) 
        {
            addr = (Integer) e.nextElement();
            if (addr.intValue() == address)
                return true;
        }
        return false;
    }
    
    /**
    * Returns index of basic block that starts at specified start_address.
    * If not found, return -1. 
    */
    public int indexOfBasicBlock(int start_address) {
        int i = 0;
        BasicBlock bb;
        
        for (Enumeration e = bbs.elements(); e.hasMoreElements(); )
        {
            bb = (BasicBlock) e.nextElement();
            
            if (start_address == bb.getStartAddress())
                return i;
            i++;
        }
        // not found, return -1.
        return -1;
    }
    
    /**
    * Returns index of basic block that contains (between start and end address) the 
    * specified address.  Otherwise, return -1.
    */
    public int inBasicBlock(int address) {
        BasicBlock bb;
        int i = 0;
        
        for (Enumeration e = bbs.elements(); e.hasMoreElements(); )
        {
            bb = (BasicBlock) e.nextElement();
            if (address >=  bb.getStartAddress() && address <= bb.getEndAddress())
                return i;
            i++;
        }
        // not found, return -1.
        return -1;
    }
    
    public Instruction getInstruction(int index) {
        return instructions.elementAt(index);
    }
    public void setInstruction(Instruction i, int index) {
	instructions.setElementAt(i,index);
        return; 
    }
    
    public BasicBlock getTempBasicBlock(int index) {
        if (code == null)
            return null;
        
        return ((BasicBlock) bbs.elementAt(index));
    }
    
    public void writeReady() {
        ByteArrayOutputStream baoStream = new ByteArrayOutputStream();
        DataOutputStream oStream = new DataOutputStream(baoStream);
        
//cjk
	//System.err.println("\n" + method_name);
	int k = 0;
        for (Enumeration e = modified_instructions.elements(); e.hasMoreElements();) {
            Instruction inst = (Instruction) e.nextElement();
	    //System.err.println("Writing inst: opcode: " + inst.getOpcode() + " inst_operand: " + inst.getOperandValue() 
		//+ " inst_index: " + (k++) );
            inst.write(oStream);
        }
        
        code_length = baoStream.size();
        code = new byte[code_length];
        System.arraycopy(baoStream.toByteArray(), 0, code, 0, code_length);
        
        // loop through the attributes until code attribute is found
        //   when found, update info
        for (int i = 0; i < method.attribute_count; i++) {
            Attribute_Info attribute = method.attributes[i];
            CONSTANT_Utf8_Info info = (CONSTANT_Utf8_Info) 
                constant_pool[attribute.attribute_name_index];
            String attribute_name = new String(info.bytes);
            if (attribute_name.equals("Code")) {
                Code_Attribute code_attribute = (Code_Attribute) attribute;
                code_attribute.attribute_length += (code_length - code_attribute.code_length);
                code_attribute.max_stack = max_stack;
                code_attribute.code_length = code_length;
                code_attribute.code = new byte[code_length];
                System.arraycopy(code, 0, code_attribute.code, 0, code_length);

                // update exception table as well
                for (int j = 0; j < code_attribute.exception_table_length; j++) {
                    Exception_Table exception_table = code_attribute.exceptions[j];
                    exception_table.updateOffset(instructions, modified_instructions);
                }
            }
        }
        try{
        oStream.flush();
        oStream.close();
        } catch(IOException e) {
        	System.err.println("IO Exception on close of IStream");
        }
    }    

    /**
     * Returns this method's access flags.
     */
    public short getAccessFlags() {
        return method.access_flags;
    }
    
    /**
     * Returns true if this method was declared to be public.
     */
    public boolean isPublic() {
        return (0 != (method.access_flags & Constant.ACC_PUBLIC));
    }
    
    /**
     * Returns true if this method was declared to be private.
     */
    public boolean isPrivate() {
        return (0 != (method.access_flags & Constant.ACC_PRIVATE));
    }
    
    /**
     * Returns true if this method was declared to be protected.
     */
    public boolean isProtected() {
        return (0 != (method.access_flags & Constant.ACC_PROTECTED));
    }
    
    /**
     * Returns true if this method was declared to be static.
     */
    public boolean isStatic() {
        return (0 != (method.access_flags & Constant.ACC_STATIC));
    }
    
    /**
     * Returns true if this method was declared to be final.
     */
    public boolean isFinal() {
        return (0 != (method.access_flags & Constant.ACC_FINAL));
    }
    
    /**
     * Returns true if this method was declared to be synchronized.
     */
    public boolean isSynchronized() {
        return (0 != (method.access_flags & Constant.ACC_SYNCHRONIZED));
    }
    
    /**
     * Returns true if this method was declared to be native.
     */
    public boolean isNative() {
        return (0 != (method.access_flags & Constant.ACC_NATIVE));
    }
    
    /**
     * Returns true if this method was declared to be abstract.
     */
    public boolean isAbstract() {
        return (0 != (method.access_flags & Constant.ACC_ABSTRACT));
    }
    
    /**
     * Returns the name of this method.
     */
    public String getMethodName() {
        return method_name;
    }

    /**
     * Returns the Method_Info for this method
     */
    public Method_Info getMethodInfo() {
        return method;
    }
    
    /**
     * Returns the descriptor of this method.
     */
    public String getDescriptor() {
        return descriptor;
    }
    
    /** 
     * Returns the length of the code array (actual bytecode).
     */
    public int getCodeLength() {
        if (code == null)
            return 0;
        
        return code_length;
    }
    
    /**
     * Returns code buffer (actual bytecode) uninterpreted.
     */
    public byte[] getCode() {
        if (code == null)
            return null;
        
        return code;
    }
    
    /**
     * Returns maximum stack size for this method.
     */
    public short getMaxStack() {
        if (code == null)
            return -1;
        
        return max_stack;
    }
    
    /**
     * Returns maximum number of locals for this method.
     */
    public short getMaxLocals() {
        if (code == null)
            return -1;
        
        return max_locals;
    }
    
    /**
     * Returns the array of instructions.
     * @see Instruction
     */
    public Instruction[] getInstructions() {
        return instructions.getInstructions();
    }

    /**
     * Returns the InstructionArray.
     * @see InstructionArray
     */
    public InstructionArray getInstructionArray() {
        return instructions;
    }

    /**
     * Returns the number of instuctions in this method.
     */
    public int getInstructionCount() {
        if (code == null)
            return 0;
        
        return instructions.size();
    }

    /**
     * Returns the array of basic blocks.
     * @see BasicBlockArray
     */
    public BasicBlockArray getBasicBlocks() {
        if (code == null)
            return new BasicBlockArray(new Vector());
        
        return basic_blocks;
    }
   
    /**
     * Returns the vector of basic blocks.
     * @see java.util.Vector
     */
    public Vector getTempBasicBlocks() {
        if (code == null)
            return new Vector();
        
        return bbs;
    }
    
    public Vector getTempBasicBlockStart() {
        if (code == null)
            return new Vector();
        
        return bbs_start;
    }
    
    /**
     * Returns the number of basic blocks in this method.
     */
    public int getBasicBlockCount() {
        if (code == null)
            return 0;
        
        return basic_blocks.size();
    }
    
    public short indexInConstantPool(Cp_Info cp) {
        for (short i = 1; i < constant_pool.length; i++) {
            if (cp.equals(constant_pool[i]))
                return i;
        }
        return -1;
    }
    
    public short addConstantPoolEntry(Cp_Info entry) {
        constant_pool = classinfo.addConstantPoolEntry(entry);
        return (short) (constant_pool.length - 1);
    }
    
    public void setConstantPool(Cp_Info[] cps) {
        constant_pool = cps;
    }
    
    public Cp_Info[] getConstantPool() {
        return constant_pool;
    }
    
    public String getClassName() {
        return classinfo.class_name;
    }
//Added for size routines -cjk  code + ldata
  public int size() {
   return method.size();
  }
  public int ldsize() { //ldata only
   return method.ldsize();
  }

  public int get_code_length() {
   return method.code_length();
  }
  private Object findInfo(short ni, short di, int methflag) {
	int i;
	if (methflag == 1) { //looking for a method_info object
		for (i = 0; i < classinfo.classfile.methods_count; i++) {
			Method_Info mi=classinfo.classfile.methods[i];
			if ((ni == mi.name_index) && (di == mi.descriptor_index)) {
				return (Object) mi;
			}
		}
		System.err.println("ERROR in findInfo: " + ni + " " + di + " method_info not found");
		System.exit(-1);
		return null;
	}
	//field info
	for (i = 0; i < classinfo.classfile.field_count; i++) {
		Field_Info fi=classinfo.classfile.fields[i];
		if ((ni == fi.name_index) && (di == fi.descriptor_index)) {
			return (Object) fi;
		}
	}
	System.err.println("ERROR in findInfo: "+  ni + " " + di + " field_info not found");
	System.exit(-1);
	return null;
  }
  /* cjk */
  public Local_Variable_Table[] getLVT() {
	return(method.getLVT());
  }
}

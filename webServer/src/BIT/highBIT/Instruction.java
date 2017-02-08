/* Instruction.java
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
import java.io.*;
import java.util.*;

/**
* Represents a valid Java Virtual Machine instrucion.
* 
* @author  <a href="mailto:hanlee@cs.colorado.edu">Han B. Lee</a>
* @see Routine
**/
public class Instruction implements Cloneable {
    
    /** 
    * Represents the opcode of the instruction.
    * <br>
    * This value can be read by invoking getOpcode() method.
    * @see Instruction#getOpcode()
    */
    protected int opcode;
    
    /**
    * This variable indicates the offset of this instruction from the
    * start of code buffer.
    */
    protected int offset = -1;
    
    /**
    * Pointer to the routine that this instruction is in.
    */
    protected Routine routine;
    
    /**
    * Index of this instruction in InstructionArray
    */
    protected int index = 0;
    
    /**
    * Index of this instruction in modified instructions array
    */
    protected int modified_index;
    
    /**
    * Is this an instruction in the modified array?
    */
    protected boolean modified = false;

    /**
    * Constructor for Instruction class.
    *
    * @param	opcode	the opcode of this instruction
    * @param offset  the offset of this instruction in bytes from start of code buffer
    */
    public Instruction(int opcode, int offset, Routine routine) {
        this.opcode = opcode;
        this.offset = offset;
        this.routine = routine;
    }
    
    public Instruction(int opcode, Routine routine) {
        this.opcode = opcode;
        this.routine = routine;
    }
    
    /**
     * Sets the index of this instruction.
     */
    public void setIndex(int index) {
        this.index = index;
    }

    public void setCpoolIndex(int value) { } //call the subclass setIndex
    
    /**
     * Sets the modified index of this instruction.
     * A modified index is the index of this instruction that corresponds
     * to the modified instructions array.
     */
    public void setModifiedIndex(int modified_index) {
        this.modified_index = modified_index;
    }

    /**
     * Gets the modified index of this instruction.
     * A modified index is the index of this instruction that corresponds
     * to the modified instructions array.
     */
    public int getModifiedIndex() {
        return modified_index;
    }

    /**
     * Returns the opcode of this instruction.
     */
    public int getOpcode() {
        return opcode;
    }

    public int getDoubleOperandValue()
    {
        return 0;
    } 

    public void write(DataOutputStream oStream) {
        try {
            oStream.writeByte(opcode);
        }
        catch (IOException e) {
            System.out.println("Error writing to code buffer");
        }
    }
    
    /**
    * Returns the type of this instruction.
    */
    public short getInstructionType() {
        return InstructionTable.InstructionTypeTable[opcode];
    }
    
    /**
    * Returns the offset of this instruction from the start of code buffer.
    */
    public int getOffset() {
        return offset;
    }
    
    /**
    * Returns the length of this instruction.
    */
    public int getLength() {
        return 1;
    }
    
    /**
    * Returns the value of the operands.
    */
    public int getOperandValue()
    {
        return 0;
    }
    
    /**
    * Set the offset of the instruction.
    */
    public void setOffset(int offset)
    {
        this.offset = offset;
    }

    public void setModified() {
        modified = true;
    }

    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public int addBefore(String classname, String methodname, Object arg) {
        int num_added = 0;

        if (modified == false) {
            Instruction target = (Instruction) routine.modified_instructions.elementAt(modified_index);
            target.setIndex(modified_index);
            int increment = target.addBefore(classname, methodname, arg);
            routine.instructions.updateModifiedIndex(this.index, increment);
        }
        else {
            // add necessary constant pool entries
	    if (System.getProperty("CJKDEBUG") != null) System.err.println("cjkdebug: making utf8constant from: " + classname);
            CONSTANT_Utf8_Info classUtf8 = new CONSTANT_Utf8_Info(classname);
            short classUtf8Index = 0;
            if ((classUtf8Index = routine.indexInConstantPool(classUtf8)) == -1)
                classUtf8Index = routine.addConstantPoolEntry(classUtf8);
            CONSTANT_Class_Info classInfo = new CONSTANT_Class_Info(classUtf8Index);
            short classInfoIndex = 0;
            if ((classInfoIndex = routine.indexInConstantPool(classInfo)) == -1)
                classInfoIndex = routine.addConstantPoolEntry(classInfo);
        
            Cp_Info argInfo = null, descriptorUtf8 = null;
            short argIndex = 0;
            boolean brOutcome = false;
        
            if (arg instanceof String) {
                if (arg.equals(new String("BranchOutcome"))) {
	    	    if (System.getProperty("CJKDEBUG") != null) System.err.println("cjkdebug: making utf8constant from: (I)V");
                    descriptorUtf8 = new CONSTANT_Utf8_Info("(I)V");
                    brOutcome = true;
                }
                else {
	    	    if (System.getProperty("CJKDEBUG") != null) System.err.println("cjkdebug: making utf8constant from: " + arg);
                    argInfo = new CONSTANT_Utf8_Info((String) arg);
                    if ((argIndex = routine.indexInConstantPool(argInfo)) == -1)
                        argIndex = routine.addConstantPoolEntry(argInfo);
                    argInfo = new CONSTANT_String_Info(argIndex);
                    if ((argIndex = routine.indexInConstantPool(argInfo)) == -1)
                        argIndex = routine.addConstantPoolEntry(argInfo);
	    	    if (System.getProperty("CJKDEBUG") != null) System.err.println("cjkdebug: making utf8constant from: (Ljava/lang/String;)V");
                    descriptorUtf8 = new CONSTANT_Utf8_Info("(Ljava/lang/String;)V");
                }
            }
            else if (arg instanceof Integer) {
                argInfo = new CONSTANT_Integer_Info(((Integer) arg).intValue());
                if ((argIndex = routine.indexInConstantPool(argInfo)) == -1)
                    argIndex = routine.addConstantPoolEntry(argInfo);
	    	if (System.getProperty("CJKDEBUG") != null) System.err.println("cjkdebug: 2making utf8constant from: (I)V");
                descriptorUtf8 = new CONSTANT_Utf8_Info("(I)V");
            }   
            else {
                System.out.println("argument type can only be Integer or String");
                System.exit(-1);
            }
            
	    if (System.getProperty("CJKDEBUG") != null) System.err.println("cjkdebug: making utf8constant from: " + methodname);
            CONSTANT_Utf8_Info methodUtf8 = new CONSTANT_Utf8_Info(methodname);
            short methodUtf8Index = 0;
            if ((methodUtf8Index = routine.indexInConstantPool(methodUtf8)) == -1)
                methodUtf8Index = routine.addConstantPoolEntry(methodUtf8);
            short descriptorUtf8Index = 0;
            if ((descriptorUtf8Index = routine.indexInConstantPool(descriptorUtf8)) == -1)
                descriptorUtf8Index = routine.addConstantPoolEntry(descriptorUtf8);
            CONSTANT_NameAndType_Info nameAndTypeInfo = new 
                CONSTANT_NameAndType_Info(methodUtf8Index, descriptorUtf8Index);
            short nameAndTypeInfoIndex = 0;
            if ((nameAndTypeInfoIndex = routine.indexInConstantPool(nameAndTypeInfo)) == -1)
                nameAndTypeInfoIndex = routine.addConstantPoolEntry(nameAndTypeInfo);
            CONSTANT_Methodref_Info methodRefInfo = new 
                CONSTANT_Methodref_Info(classInfoIndex, nameAndTypeInfoIndex);
            short methodRefInfoIndex = 0;
            if ((methodRefInfoIndex = routine.indexInConstantPool(methodRefInfo)) == -1)
                methodRefInfoIndex = routine.addConstantPoolEntry(methodRefInfo);
            
            InstructionDoubleOperand invokestatic = new InstructionDoubleOperand(0xb8,
                (short) methodRefInfoIndex, routine);
            
            Instruction nop = new Instruction(0, routine);
            Instruction dummy = (Instruction) routine.modified_instructions.elementAt(index);

            // if we are returning the branch outcome
            if (brOutcome) {
                Instruction dup = null;
            
                switch (opcode) {
                    case InstructionTable.ifeq:
                    case InstructionTable.ifne:
                    case InstructionTable.iflt:
                    case InstructionTable.ifge:
                    case InstructionTable.ifgt:
                    case InstructionTable.ifle:
                    case InstructionTable.ifnonnull:
                    case InstructionTable.ifnull:
                        dup = new Instruction(0x59, routine);
                        break;
                    case InstructionTable.if_acmpeq:
                    case InstructionTable.if_acmpne:
                    case InstructionTable.if_icmpeq:
                    case InstructionTable.if_icmpne:
                    case InstructionTable.if_icmplt:
                    case InstructionTable.if_icmpge:
                    case InstructionTable.if_icmpgt:
                    case InstructionTable.if_icmple:
                        dup = new Instruction(0x5c, routine);
                        break;
                    default:
                        System.out.println("BranchOutcome may only be called on conditional instructions");
                        System.exit(-1);
                        break;
                }
                
                InstructionDoubleOperand branch = new InstructionDoubleOperand(opcode, (short) 0x07, routine);
                Instruction iconst_0 = new Instruction(0x03, routine);
                InstructionDoubleOperand jump = new InstructionDoubleOperand(0xa7, (short) 0x04, routine);
                Instruction iconst_1 = new Instruction(0x04, routine);
            
                routine.modified_instructions.insertElementAt(invokestatic, index);
                routine.modified_instructions.insertElementAt(iconst_1, index);
                routine.modified_instructions.insertElementAt(jump, index);
                routine.modified_instructions.insertElementAt(iconst_0, index);
                routine.modified_instructions.insertElementAt(branch, index);
                routine.modified_instructions.insertElementAt(dup, index);
                num_added = 6;
            }
            else {
                Instruction ldc;
                
                if (argIndex > 255)
                    ldc = new InstructionDoubleOperand(0x13, argIndex, routine);
                else
                    ldc = new InstructionSingleOperand(0x12, (byte) argIndex, routine);
                
                routine.modified_instructions.insertElementAt(invokestatic, index);
                routine.modified_instructions.insertElementAt(ldc, index);
                
                // to maintain alignment with tableswitch and lookupswitch instructions
                if (argIndex <= 255)
                    routine.modified_instructions.insertElementAt(nop, index);
                routine.modified_instructions.insertElementAt(nop, index);
                routine.modified_instructions.insertElementAt(nop, index);
                num_added = (argIndex <= 255) ? 5 : 4;
            }

            /* now that new instructions have been added to the instructions vectors,
            *  we need to do the following in the following order:
            * 
            *  1. adjust the offsets of instructions in the vector: remember that each instruction
            *     has corresponding offset in the code buffer and this has to be updated
            *  2. adjust the basic blocks so that the start and end index into the instructions
            *     vector is kep current.  we need to do this before the third step because
            *     this updated basic blocks will be used to adjust jump offsets in the instructions
            *  3. adjust the jump offsets in the instructions.  remember that this is required
            *     so that the flow of execution is maintained even after we add instructions in
            *     the code buffer.
            */
            
            if (brOutcome) {
                routine.adjInstrOffsets(dummy.getOffset(), 12, true);
                routine.adjModifiedBasicBlocks(index, 6, true);
                routine.adjOffsets(dummy.getOffset(), 12, true);
    	    }
    	    else {
    	        routine.adjInstrOffsets(dummy.getOffset(), 8, true);
                routine.adjModifiedBasicBlocks(index, ((argIndex > 255) ? 4 : 5), true);
		        routine.adjOffsets(dummy.getOffset(), 8, true);
            }
        }
        routine.max_stack++;
        return num_added;
    }
    
    public int addAfter(String classname, String methodname, Object arg) {
        int num_added = 0;

        if (!modified) {
            Instruction target = (Instruction) routine.modified_instructions.elementAt(modified_index);
            int increment = target.addAfter(classname, methodname, arg);
            routine.instructions.updateModifiedIndex(this.index + 1, increment);
        }
        else {
            // add necessary constant pool entries
	    if (System.getProperty("CJKDEBUG") != null) System.err.println("cjkdebug: making utf8constant from (b): " + classname);
            CONSTANT_Utf8_Info classUtf8 = new CONSTANT_Utf8_Info(classname);
            short classUtf8Index = 0;
            if ((classUtf8Index = routine.indexInConstantPool(classUtf8)) == -1)
                classUtf8Index = routine.addConstantPoolEntry(classUtf8);
            CONSTANT_Class_Info classInfo = new CONSTANT_Class_Info(classUtf8Index);
            short classInfoIndex = 0;
            if ((classInfoIndex = routine.indexInConstantPool(classInfo)) == -1)
                classInfoIndex = routine.addConstantPoolEntry(classInfo);
            
            Cp_Info argInfo = null, descriptorUtf8 = null;
            short argIndex = 0;
            boolean brOutcome = false;
        
            if (arg instanceof String) {
                if (arg.equals(new String("BranchOutcome"))) {
	            if (System.getProperty("CJKDEBUG") != null) System.err.println("cjkdebug: making utf8constant from (b): (I)V");
                    descriptorUtf8 = new CONSTANT_Utf8_Info("(I)V");
                    brOutcome = true;
                }
                else {
	            if (System.getProperty("CJKDEBUG") != null) System.err.println("cjkdebug: making utf8constant from (b): " + arg);
                    argInfo = new CONSTANT_Utf8_Info((String) arg);
                    if ((argIndex = routine.indexInConstantPool(argInfo)) == -1)
                        argIndex = routine.addConstantPoolEntry(argInfo);
                    argInfo = new CONSTANT_String_Info(argIndex);
                    if ((argIndex = routine.indexInConstantPool(argInfo)) == -1)
                        argIndex = routine.addConstantPoolEntry(argInfo);
	            if (System.getProperty("CJKDEBUG") != null) System.err.println("cjkdebug: making utf8constant from (b): (Ljava/lang/String;)V");
                    descriptorUtf8 = new CONSTANT_Utf8_Info("(Ljava/lang/String;)V");
                }
            }
            else if (arg instanceof Integer) {
                argInfo = new CONSTANT_Integer_Info(((Integer) arg).intValue());
                if ((argIndex = routine.indexInConstantPool(argInfo)) == -1)
                    argIndex = routine.addConstantPoolEntry(argInfo);
	        if (System.getProperty("CJKDEBUG") != null) System.err.println("cjkdebug: 2making utf8constant from (b): (I)V");
                descriptorUtf8 = new CONSTANT_Utf8_Info("(I)V");
            }
            else {
                System.out.println("argument type can only be Integer or String");
                System.exit(-1);
            }

	    if (System.getProperty("CJKDEBUG") != null) System.err.println("cjkdebug: making utf8constant from (b): " + methodname);
            CONSTANT_Utf8_Info methodUtf8 = new CONSTANT_Utf8_Info(methodname);
            short methodUtf8Index = 0;
            if ((methodUtf8Index = routine.indexInConstantPool(methodUtf8)) == -1)
                methodUtf8Index = routine.addConstantPoolEntry(methodUtf8);
            short descriptorUtf8Index = 0;
            if ((descriptorUtf8Index = routine.indexInConstantPool(descriptorUtf8)) == -1)
                descriptorUtf8Index = routine.addConstantPoolEntry(descriptorUtf8);
            CONSTANT_NameAndType_Info nameAndTypeInfo = new 
                CONSTANT_NameAndType_Info(methodUtf8Index, descriptorUtf8Index);
            short nameAndTypeInfoIndex = 0;
            if ((nameAndTypeInfoIndex = routine.indexInConstantPool(nameAndTypeInfo)) == -1)
                nameAndTypeInfoIndex = routine.addConstantPoolEntry(nameAndTypeInfo);
            CONSTANT_Methodref_Info methodRefInfo = new 
                CONSTANT_Methodref_Info(classInfoIndex, nameAndTypeInfoIndex);
            short methodRefInfoIndex = 0;
            if ((methodRefInfoIndex = routine.indexInConstantPool(methodRefInfo)) == -1)
                methodRefInfoIndex = routine.addConstantPoolEntry(methodRefInfo);
        
            InstructionDoubleOperand invokestatic = new InstructionDoubleOperand(0xb8,
                (short) methodRefInfoIndex, routine);
            
            Instruction nop = new Instruction(0, routine);
            Instruction dummy = (Instruction) routine.modified_instructions.elementAt(index + 1);

            // if we are returning the branch outcome
            if (brOutcome) {
                Instruction dup = null;
            
                switch (opcode) {
                case InstructionTable.ifeq:
                case InstructionTable.ifne:
                case InstructionTable.iflt:
                case InstructionTable.ifge:
                case InstructionTable.ifgt:
                case InstructionTable.ifle:
                case InstructionTable.ifnonnull:
                case InstructionTable.ifnull:
                    dup = new Instruction(0x59, routine);
                    break;
                case InstructionTable.if_acmpeq:
                case InstructionTable.if_acmpne:
                case InstructionTable.if_icmpeq:
                case InstructionTable.if_icmpne:
                case InstructionTable.if_icmplt:
                case InstructionTable.if_icmpge:
                case InstructionTable.if_icmpgt:
                case InstructionTable.if_icmple:
                    dup = new Instruction(0x5c, routine);
                    break;
                default:
                    System.out.println("BranchOutcome may only be called on conditional instructions");
                    System.exit(-1);
                    break;
                }

                InstructionDoubleOperand branch = new InstructionDoubleOperand(opcode, (short) 0x07, routine);
                Instruction iconst_0 = new Instruction(0x03, routine);
                InstructionDoubleOperand jump = new InstructionDoubleOperand(0xa7, (short) 0x04, routine);
                Instruction iconst_1 = new Instruction(0x04, routine);
                
                routine.modified_instructions.insertElementAt(invokestatic, index + 1);
                routine.modified_instructions.insertElementAt(iconst_1, index + 1);
                routine.modified_instructions.insertElementAt(jump, index + 1);
                routine.modified_instructions.insertElementAt(iconst_0, index + 1);
                routine.modified_instructions.insertElementAt(branch, index + 1);
                routine.modified_instructions.insertElementAt(dup, index + 1);
                num_added = 6;
            }
            else {
                Instruction ldc;
            
                if (argIndex > 255)
                    ldc = new InstructionDoubleOperand(0x13, argIndex, routine);
                else
                    ldc = new InstructionSingleOperand(0x12, (byte) argIndex, routine);
            
                routine.modified_instructions.insertElementAt(invokestatic, index + 1);
                routine.modified_instructions.insertElementAt(ldc, index + 1);

                // to maintain alignment with tableswitch and lookupswitch instructions
                if (argIndex <= 255)
                    routine.modified_instructions.insertElementAt(nop, index + 1);
                routine.modified_instructions.insertElementAt(nop, index + 1);
                routine.modified_instructions.insertElementAt(nop, index + 1);
                num_added = (argIndex <= 255) ? 5 : 4;
            }
            if (brOutcome) {
                routine.adjInstrOffsets(dummy.getOffset(), 12, false);
                routine.adjModifiedBasicBlocks(index, 6, false);
                routine.adjOffsets(dummy.getOffset(), 12, false);
    	    }
    	    else {
    	        routine.adjInstrOffsets(dummy.getOffset(), 8, false);
                routine.adjModifiedBasicBlocks(index, ((argIndex > 255) ? 4 : 5), false);
		routine.adjOffsets(dummy.getOffset(), 8, false);
            }
        }
        routine.max_stack ++;
        return num_added;
    }

}

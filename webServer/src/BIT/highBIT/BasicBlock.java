/* BasicBlock.java
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

import java.io.*;
import java.util.*;

/**
 * Represents a basic block.
 * <br>
 * Basic block is a section of code that has only one entry and exit point.
 * 
 * @author  <a href="mailto:hanlee@cs.colorado.edu">Han B. Lee</a>
 *
 **/
public class BasicBlock implements Cloneable {
    
    /** 
     * Represents the start address (in # of instructions) of this basic block.
     * <br>
     * This value can be read by invoking getStart() method.
     * @see BasicBlock#getStart()
     */
    protected int start_address;
    
    /** 
     * Represents the ending address (in # of instructions) of this basic block.
     * <br>
     * This value can be read by invoking getEnd() method.
     * @see BasicBlock#getEnd()
     */
    protected int end_address;
    
    private int old_start_address;
    private int old_end_address;

    /**
    * Represents the routine that this basic block is defined in.
    */
    public Routine routine;
    
    /**
    * Constructor for BasicBlock class.
    *
    */
    public BasicBlock(Routine r, int start) {
        this.start_address = start;
        this.old_start_address = start;
        this.routine = r;
        Instruction inst;
        int offset, inst_index, def, i;
        int instruction_count = r.getInstructionCount();
        
        // look for the end of this basic block
        for (i = start; i < instruction_count; i++) {
            inst = r.getInstruction(i);
            short instructionType = inst.getInstructionType();
            if (instructionType == InstructionTable.UNCONDITIONAL_INSTRUCTION ||
                instructionType == InstructionTable.CONDITIONAL_INSTRUCTION) {
                // create basic block that results from direct path (branch not taken)
                if (i < instruction_count - 1) {
                    createOrSplitBasicBlock(r, i + 1);
                }
                // determine the control flow and create other basic blocks if applicable
                switch(inst.getOpcode()) {
                    case InstructionTable.GOTO:
                    case InstructionTable.goto_w:
                    case InstructionTable.jsr:
                    case InstructionTable.jsr_w:
                    case InstructionTable.ifeq:
                    case InstructionTable.ifne:
                    case InstructionTable.iflt:
                    case InstructionTable.ifge:
                    case InstructionTable.ifgt:
                    case InstructionTable.ifle:
                    case InstructionTable.if_icmpeq:
                    case InstructionTable.if_icmpne:
                    case InstructionTable.if_icmplt:
                    case InstructionTable.if_icmpge:
                    case InstructionTable.if_icmpgt:
                    case InstructionTable.if_icmple:
                    case InstructionTable.if_acmpeq:
                    case InstructionTable.if_acmpne:
                    case InstructionTable.ifnull:
                    case InstructionTable.ifnonnull:
                        // offset of goto from this instruction
                        // offset from the start of code buffer
                        offset = inst.getOperandValue() + inst.getOffset();
                        // look for instruction at this offset
                        inst_index = r.indexOfInstruction(offset);
                        // indexOfInstruction returns -1 when failed
                        // but this should always succeed!
                        if (inst_index == -1) {
                            System.out.println("1Error in jump offset");
                        }
                        else {
                            createOrSplitBasicBlock(r, inst_index);
                        }
                        break;
                    case InstructionTable.tableswitch:
                        def = ((InstructionTableswitch) inst).getDefault();
                        // offset of tablswitch instruction (this instruction)
                        // offset of target from start of code buffer
                        offset = inst.getOffset() + def;
                        // look for instruction at this offset
                        inst_index = r.indexOfInstruction(offset);
                        if (inst_index == -1) {
                            System.out.println("2Error in jump offset");
                        }
                        else {
                            createOrSplitBasicBlock(r, inst_index);
                        }
                        
                        int jump_offsets[] = ((InstructionTableswitch)inst).getJumpOffsets();
                        for (int j = 0; j < jump_offsets.length; j++)
                        {
                            offset = inst.getOffset() + jump_offsets[j];
                            inst_index = r.indexOfInstruction(offset);
                            if (inst_index == -1) {
                                System.out.println("3Error in jump offset");
                            }
                            else {
                                createOrSplitBasicBlock(r, inst_index);
                            }
                        }   
                        break;
                    case InstructionTable.lookupswitch:
                        def = ((InstructionLookupswitch) inst).getDefault();
                        // offset of this instruction plus def gives the offset of target instruction
                        offset = inst.getOffset() + def;
                        // look for instruction at this offset
                        inst_index = r.indexOfInstruction(offset);
                        if (inst_index == -1) {
                            System.out.println("4Error in jump offset");
                        }
                        else {
                            createOrSplitBasicBlock(r, inst_index);
                        }
                        
                        int offsets[] = ((InstructionLookupswitch)inst).getOffsets();
                        for (int j = 0; j < offsets.length; j++) 
                        {
                            offset = inst.getOffset() + offsets[j];
                            inst_index = r.indexOfInstruction(offset);
                            if (inst_index == -1) {
                                System.out.println("5Error in jump offset");
                            } 
                            else {
                                createOrSplitBasicBlock(r, inst_index);
                            }
                        }
                        break;
                    case InstructionTable.ret:
                    case InstructionTable.ireturn:
                    case InstructionTable.lreturn:
                    case InstructionTable.freturn:
                    case InstructionTable.dreturn:
                    case InstructionTable.areturn:
                    case InstructionTable.RETURN:
                    case InstructionTable.invokevirtual:
                    case InstructionTable.invokestatic:
                    case InstructionTable.invokeinterface:
                    case InstructionTable.invokespecial:
		    case InstructionTable.athrow:
                        break;
                    default:
                        break;
                }

                int bb_index = r.inBasicBlock(i);
                if (bb_index == -1) {
                  this.end_address = i;
                  this.old_start_address = i;
		  this.old_end_address = this.end_address; //cjk
                }
                else {
                  this.end_address = r.getTempBasicBlock(bb_index).getStartAddress() - 1;
		  this.old_end_address = this.end_address; //cjk

                }
                // end is this instruction
                break;
            }
        }
    }

    /**
    * Helper function that either creates a basic block if not already there
    * or splits an existing basic block if it already exists.
    */
    private static void createOrSplitBasicBlock(Routine r, int inst_index) 
    {
        // inst_index now contains the start of new basic block
        // but check to make sure this basic block is not already present
        // in basic block Vector
        
        // if already progressing, then just return
        if (r.existBasicBlockStart(inst_index) == true)
            return;
        
        // look for the index of this basic block
        if (r.indexOfBasicBlock(inst_index) == -1) {
            // not present, check whether a basic block needs to be split
            int bb_index = r.inBasicBlock(inst_index);
            if (bb_index == -1) {
                // not present, just create a new basic block
                (r.getTempBasicBlockStart()).addElement(new Integer(inst_index));
                (r.getTempBasicBlocks()).addElement(new BasicBlock(r, inst_index));
            }
            else {
                r.getTempBasicBlock(bb_index).split(r, inst_index);
            }
        }
    }
    
    /**
    * Splitting a BasicBlock.
    */
    public void split(Routine r, int inst_index) {
        // this basic block ends where the other basic block begins
        this.end_address = inst_index - 1;
	this.old_end_address = this.end_address; //cjk
        // add new basic block starting at inst_index
        (r.getTempBasicBlocks()).addElement(new BasicBlock(r, inst_index));
    }
    
    
    /**
    * Returns the starting offset of this basic block.
    */
    public int getStartAddress() {
        return start_address;
    }
    
    public int getOldStartAddress() {
        return old_start_address;
    }

    /**
    * This method is used to adjust the start address of a basic block
    * after insertion of instructions.
    */
    public void setStartAddress(int addr) {
        old_start_address = start_address;
        start_address = addr;
    }
    
    /**
    * Returns the ending offset of this basic block.
    */
    public int getEndAddress() {
        return end_address;
    }
    
    public int getOldEndAddress() {
        return old_end_address;
    }

    /**
    * This method is used to adjust the end address of a basic block
    * after insertion of instructions.
    */
    public void setEndAddress(int addr) {
        old_end_address = end_address;
        end_address = addr;
    }
    
    /**
    * This method retuns the size of this basic block.
    */
    public int size() {
        return (end_address - start_address + 1);
    }
    
    /**
    * Add a call to classname.methodname before this basic block.
    */
    public void addBefore(String classname, String methodname, Object arg) {
        Instruction first = (Instruction) routine.instructions.elementAt(start_address);
        first.addBefore(classname, methodname, arg);
    }
    
    /**
    * Add a call to classname.methodname after this basic block.
    */
    public void addAfter(String classname, String methodname, Object arg) {
        Instruction last = (Instruction) routine.instructions.elementAt(end_address);
        last.addAfter(classname, methodname, arg);
    }

    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public String getClassName() {
        return this.routine.getClassName();
    }

    public String getMethodName() {
        return this.routine.getMethodName();
    }
}

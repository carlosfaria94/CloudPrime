/* BasicBlockArray.java
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
import java.util.Vector;
import java.util.Enumeration;

/**
 * Represents an array to hold basic blocks.
 * The reason for creating this class to represent basic blocks array
 * is that we want to hide as much as we can about the implementation 
 * to the user.  Also, walking through an array should be faster than
 * walking through a Vector.
 * <br>
 * 
 * @author  <a href="mailto:hanlee@cs.colorado.edu">Han B. Lee</a>
 * @see BasicBlock
 **/
public class BasicBlockArray {
    BasicBlock bbs[];

    /**
     * Create a new array given a Vector of basic blocks.
     *
     * @param   Vector of basic blocks
     * @see     java.util.Vector
     */
    public BasicBlockArray(Vector bbs_) {
        bbs = new BasicBlock[bbs_.size()];

        // just copy by reference is fine here
        for (int i = 0; i < bbs_.size(); i++) {
            bbs[i] = (BasicBlock) bbs_.elementAt(i);
        }
    }

    /**
     * Create a new array given another array of basic blocks.
     * Instead of copying the references, it clones them, so actual 
     * objects are copied.
     *
     * @param   an array to be copied from
     */
    public BasicBlockArray(BasicBlockArray bbs_) {
        bbs = new BasicBlock[bbs_.size()];

        // we want to clone instead because we don't want the same object
        for (int i = 0; i < bbs_.size(); i++) {
            try {
                bbs[i] = (BasicBlock) bbs_.elementAt(i).clone();
            } catch (CloneNotSupportedException e) {
            }
        }
    }

    /** 
     * Returns an enumerator for the elements in this array class.
     *
     * @return      new enumeration for the elements in this array
     * @see         java.util.Enumeration
     */
    public final synchronized Enumeration elements() {
	return new BasicBlockArrayEnumerator(bbs);
    }
    
    /**
     * Returns the basic block at ith index.
     *
     * @param       index of the wanted basic block
     * @return      the basic block at that index
     * @see         BasicBlock
     */
    public BasicBlock elementAt(int i) {
        return (BasicBlock) bbs[i];
    }

    /**
     * returns the actual array of basic blocks if user needs it.
     *
     * @return      array of basic blocks
     * @see         BasicBlock
     */
    public BasicBlock[] getBasicBlocks() {
        return bbs;
    }

    /**
     * returns the size of this array.
     *
     * @return      an integer representing the size of this array.
     */
    public int size() {
        return bbs.length;
    }
}

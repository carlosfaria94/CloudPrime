/* InstructionArrayEnumerator.java
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
import java.util.Enumeration;
import java.util.NoSuchElementException;

/**
 * Just an enumerator for instruction array.
 *
 * @author  <a href="mailto:hanlee@cs.colorado.edu">Han B. Lee</a>
 * @see InstructionArray
 */
final class InstructionArrayEnumerator implements Enumeration {
    Instruction instructions[];
    int index;

    InstructionArrayEnumerator(Instruction instructions_[]) {
	instructions = instructions_;
	index = 0;
    }

    public boolean hasMoreElements() {
	return index < instructions.length;
    }

    public Object nextElement() {
	synchronized (instructions) {
	    if (index < instructions.length) {
		return instructions[index++];
	    }
	}
	throw new NoSuchElementException("InstructionArrayEnumerator");
    }

}

/* BasicBlockArrayEnumerator.java
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

/**
 * Just an enumerator for basic block array.
 *
 * @author  <a href="mailto:hanlee@cs.colorado.edu">Han B. Lee</a>
 * @see BasicBlockArray
 */
package BIT.highBIT;
import java.util.Enumeration;
import java.util.NoSuchElementException;

final class BasicBlockArrayEnumerator implements Enumeration {
    BasicBlock bbs[];
    int index;

    BasicBlockArrayEnumerator(BasicBlock bbs_[]) {
	bbs = bbs_;
	index = 0;
    }

    public boolean hasMoreElements() {
	return index < bbs.length;
    }

    public Object nextElement() {
	synchronized (bbs) {
	    if (index < bbs.length) {
		return bbs[index++];
	    }
	}
	throw new NoSuchElementException("BasicBlockArrayEnumerator");
    }
}

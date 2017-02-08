/* InstructionTable.java
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

/**
* Contains constants used to represent Java Virtual Machine Instructions.
* 
* @author  <a href="mailto:hanlee@cs.colorado.edu">Han B. Lee</a>
* @see Instruction
**/
public class InstructionTable {
  /**
   * NOP_INSTRUCTION are NOP instructions.
   *
   * NOP_INSTRUCTION includes
   * <br>
   * nop instruction only.
   */
  public static final short NOP_INSTRUCTION = 0;
  
  /**
   * CONSTANT_INSTRUCTION are instructions that deal with constants,
   * or with constant pool table.
   *
   * CONSTANT_INSTRUCTION includes
   * <br>
   * aconst_null, iconst_m1, iconst_0, iconst_1, iconst_2,
   * iconst_3, iconst_4, iconst_5, lconst_0, lconst_1, 
   * fconst_0, fconst_1, fconst_2, dconst_0, dconst_1,
   * bipush, sipush, ldc, ldc_w, ldc2_w instructions.
   */
  public static final short CONSTANT_INSTRUCTION = 1;
  
  /**
   * LOAD_INSTRUCTION are load type instructions.
   * 
   * LOAD_INSTRUCTION includes
   * <br>
   * iload, lload, fload, dload, aload, iload_0, iload_1, iload_2, iload_3,
   * lload_0, lload_1, lload_2, lload_3, fload_0, fload_1, fload_2, fload_3,
   * dload_0, dload_1, dload_2, dload_3, aload_0, aload_1, aload_2, aload_3,
   * iaload, laload, faload, daload, aaload, baload, caload, saload instructions.
   */
  public static final short LOAD_INSTRUCTION = 2;
  
  /**
   * STORE_INSTRUCTION are store type instructions.
   * 
   * STORE_INSTRUCTION includes
   * <br>
   * istore, lstore, fstore, dstore, astore, istore_0, istore_1, istore_2, istore_3,
   * lstore_0, lstore_1, lstore_2, lstore_3, fstore_0, fstore_1, fstore_2, fstore_3,
   * dstore_0, dstore_1, dstore_2, dstore_3, astore_0, astore_1, astore_2, astore_3,
   * iastore, lastore, fastore, dastore, aastore, bastore, castore, sastore instructions.
   */
  public static final short STORE_INSTRUCTION = 3;
  
  /**
   * STACK_INSTRUCTION are instrctions that manipulate the stack.
   *
   * STACK_INSTRUCTION includes
   * <br>
   * pop, pop2, dup, dup_x1, dup_x2, dup2, dup2_x1, dup2_x2, swap instructions.
   */
  public static final short STACK_INSTRUCTION = 4;
  
  /**
   * ARITHMETIC_INSTRUCTION are arithmetic instructions.
   * 
   * ARITHMETIC_INSTRUCTION includes
   * <br>
   * iadd, ladd, fadd, dadd, isub, lsub, fsub, dsub, imul, lmul, fmul, dmul,
   * idiv, ldiv, fdiv, ddiv, irem, irem, frem, drem, ineg, lneg, fneg, dneg,
   * ishl, lshl, ishr, lshr, iushr, lushr, iinc instructions.
   */
  public static final short ARITHMETIC_INSTRUCTION = 5;
  
  /**
   * LOGICAL_INSTRUCTION are logical instructions.
   * 
   * LOGICAL_INSTRUCTION includes
   * <br>
   * iand, iand, ior, lor, ixor, lxor instructions.
   */
  public static final short LOGICAL_INSTRUCTION = 6;
  
  /** CONVERSION_INSTRUCTION instructions convert types.
   *
   * CONVERSION_INSTRUCTION includes
   * <br>
   * i2l, i2f, i2d, l2i, l2f, l2d, f2i, f2l, f2d, d2i, d2l, d2f,
   * i2b, i2c, i2s instructions.
   */
  public static final short CONVERSION_INSTRUCTION = 7;
  
  /** 
   * COMPARISON_INSTRUCTION instructions do comparison.
   *
   * COMPARISON_INSTRUCTION includes
   * <br>
   * lcmp, fcmpl, fcmpg, dcmpl, dcmpg instructions.
   */
  public static final short COMPARISON_INSTRUCTION = 8;
  
  /**
   * CONDITIONAL_INSTRUCTION instructions branch based on a condition.
   *
   * CONDITIONAL_INSTRUCTION includes
   * <br>
   * ifeq, ifne, iflt, ifge, ifgt, ifle, if_icmpeq, if_icmpne,
   * if_icmplt, if_icmpge, if_icmpgt, if_icmple, if_acmpeq, if_acmpne,
   * ifnull, ifnonnull instructions.
   */
  public static final short CONDITIONAL_INSTRUCTION = 9;
  
  /** 
   * UNCONDITIONAL_INSTRUCTION instructions branch unconditionally.
   *
   * UNCONDITIONAL_INSTRUCTION includes
   * <br>
   * goto, jsr, ret, tableswitch, lookupswitch, ireturn, lreturn,
   * freturn, dreturn, areturn, return,
   * invokevirtual, invokenonvirtual, invokestatic, 
   * invokeinterface, goto_w, jsr_w, ret_w, breakpoint instructions.
   */
  public static final short UNCONDITIONAL_INSTRUCTION = 10;
  
  /**
   * CLASS_INSTRUCTION instructions deal with class components.
   *
   * CLASS_INSTRUCTION includes
   * <br>
   * getstatic, putstatic instructions.
   */
  public static final short CLASS_INSTRUCTION = 11;
  
  /**
   * OBJECT_INSTRUCTION instructions deal with object components.
   *
   * OBJECT_INSTRUCTION includes
   * <br>
   * getfield, putfield, new, newarray, anewarray, arraylength instructions.
   */
  public static final short OBJECT_INSTRUCTION = 12;
  
  /**
   * EXCEPTION_INSTRUCTION instructions deal with exceptions.
   *
   * EXCEPTION_INSTRUCTION includes
   * <br>
   * none.
   */
  public static final short EXCEPTION_INSTRUCTION = 13;
  
  /**
   * INSTRUCTIONCHECK_INSTRUCTION instructions deal with types.
   *
   * INSTRUCTIONCHECK_INSTRUCTION includes
   * <br>
   * checkcast, instanceof instructions.
   */
  public static final short INSTRUCTIONCHECK_INSTRUCTION = 14;
  
  /**
   * MONITOR_INSTRUCTION instructions deal with monitors.
   * 
   * MONITOR_INSTRUCTION includes
   * <br>
   * monitorenter, monitorexit instructions.
   */
  public static final short MONITOR_INSTRUCTION = 15;

  /**
   * OTHER_INSTRUCTION instructions
   *
   * OTHER_INSTRUCTION include
   * <br>
   * wide, impdep1, impdep2, and all other invalid opcodes.
   */
  public static final short OTHER_INSTRCTION = -1;
  
  /** 
   * String representation of different types of instructions.
   */
  public static final String InstructionTypeName[] = {
      "NOP_INSTRUCTION",
      "CONSTANT_INSTRUCTION",
      "LOAD_INSTRUCTION",
      "STORE_INSTRUCTION",
      "STACK_INSTRUCTION",
      "ARITHMETIC_INSTRUCTION",
      "LOGICAL_INSTRUCTION",
      "CONVERSION_INSTRUCTION",
      "COMPARISON_INSTRUCTION",
      "CONDITIONAL_INSTRUCTION",
      "UNCONDITIONAL_INSTRUCTION",
      "CLASS_INSTRUCTION",
      "OBJECT_INSTRUCTION",
      "EXCEPTION_INSTRUCTION",
      "INSTRUCTIONCHECK_INSTRUCTION",
      "MONITOR_INSTRUCTION"
  };
  
  /** 
   * InstructionTypeTable is an array indicating which type an instruction is.
   * One can modify this table if one wants to classify the instructions
   * differently than I have.
   */
  public static final short InstructionTypeTable[] = {
      0, 1, 1, 1, 1, 1, 1, 1, 1, 1,		    // 0 - 9
      1, 1, 1, 1, 1, 1, 1, 1, 1, 1,			// 10 - 19
      1, 2, 2, 2, 2, 2, 2, 2, 2, 2,			// 20 - 29
      2, 2, 2, 2, 2, 2, 2, 2, 2, 2,			// 30 - 39
      2, 2, 2, 2, 2, 2, 2, 2,	2, 2,			// 40 - 49
      2, 2, 2, 2, 3, 3, 3, 3, 3, 3,			// 50 - 59
      3, 3, 3, 3,	3, 3, 3, 3, 3, 3,			// 60 - 69
      3, 3, 3, 3, 3, 3, 3, 3, 3, 3,			// 70 - 79
      3, 3, 3, 3, 3, 3, 3, 4, 4, 4,			// 80 - 89
      4, 4, 4, 4, 4, 4, 5, 5, 5, 5,			// 90 - 99
      5, 5, 5, 5, 5, 5, 5, 5, 5, 5,			// 100 - 109
      5, 5, 5, 5, 5, 5, 5, 5, 5, 5,			// 110 - 119
      5, 5, 5, 5, 5, 5, 6, 6,	6, 6,			// 120 - 129
      6, 6, 5, 7, 7, 7, 7, 7, 7, 7,			// 130 - 139
      7, 7, 7, 7,	7, 7, 7, 7, 8, 8,			// 140 - 149
      8, 8, 8, 9, 9, 9, 9, 9, 9, 9,			// 150 - 159
      9, 9, 9, 9, 9, 9, 9, 10, 10, 10,		// 160 - 169
      10, 10, 10, 10, 10, 10, 10, 10, 11, 11,	// 170 - 179
      12, 12, 10, 10, 10, 10, -1, 12, 12, 12,	// 180 - 189
      12, 10, 14, 14, 15, 15, -1, 12, 9, 9,	// 190 - 199
      10, 10, 10, -1, -1, -1, -1, -1, -1, 10  // 200 - 209
      -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, // 210 - 219
      -1, -1, -1, -1,	-1, -1, -1, -1, -1, -1,	// 220 - 229
      -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,	// 230 - 239
      -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,	// 240 - 249
      -1, -1, -1, -1, -1, -1
  };
  
  public static final short aaload = 50;
  public static final short aastore = 83;
  public static final short aconst_null = 1;
  public static final short aload = 25;
  public static final short aload_0 = 42;
  public static final short aload_1 = 43;
  public static final short aload_2 = 44;
  public static final short aload_3 = 45;
  public static final short anewarray = 189;
  public static final short areturn = 176;
  public static final short arraylength = 190;
  public static final short astore = 58;
  public static final short astore_0 = 75;
  public static final short astore_1 = 76;
  public static final short astore_2 = 77;
  public static final short astore_3 = 78;
  public static final short athrow = 191;
  public static final short baload = 51;
  public static final short bastore = 84;
  public static final short bipush = 16;
  public static final short breakpoint = 202;
  public static final short caload = 52;
  public static final short castore = 85;
  public static final short checkcast = 192;
  public static final short d2f = 144;
  public static final short d2i = 142;
  public static final short d2l = 143;
  public static final short dadd = 99;
  public static final short daload = 49;
  public static final short dastore = 82;
  public static final short dcmpg = 152;
  public static final short dcmpl = 151;
  public static final short dconst_0 = 14;
  public static final short dconst_1 = 15;
  public static final short ddiv = 111;
  public static final short dload = 24;
  public static final short dload_0 = 38;
  public static final short dload_1 = 39;
  public static final short dload_2 = 40;
  public static final short dload_3 = 41;
  public static final short dmul = 107;
  public static final short dneg = 119;
  public static final short drem = 115;
  public static final short dreturn = 175;
  public static final short dstore = 57;
  public static final short dstore_0 = 71;
  public static final short dstore_1 = 72;
  public static final short dstore_2 = 73;
  public static final short dstore_3 = 74;
  public static final short dsub = 103;
  public static final short dup = 89;
  public static final short dup_x1 = 90;
  public static final short dup_x2 = 91;
  public static final short dup2 = 92;
  public static final short dup2_x1 = 93;
  public static final short dup2_x2 = 94;
  public static final short f2d = 141;
  public static final short f2i = 139;
  public static final short f2l = 140;
  public static final short fadd = 98;
  public static final short faload = 48;
  public static final short fastore = 81;
  public static final short fcmpg = 150;
  public static final short fcmpl = 149;
  public static final short fconst_0 = 11;
  public static final short fconst_1 = 12;
  public static final short fconst_2 = 13;
  public static final short fdiv = 110;
  public static final short fload = 23;
  public static final short fload_0 = 34;
  public static final short fload_1 = 35;
  public static final short fload_2 = 36;
  public static final short fload_3 = 37;
  public static final short fmul = 106;
  public static final short fneg = 118;
  public static final short frem = 114;
  public static final short freturn = 174;
  public static final short fstore = 56;
  public static final short fstore_0 = 67;
  public static final short fstore_1 = 68;
  public static final short fstore_2 = 69;
  public static final short fstore_3 = 70;
  public static final short fsub = 102;
  public static final short getfield = 180;
  public static final short getstatic = 178;
  public static final short GOTO = 167;
  public static final short goto_w = 200;
  public static final short i2b = 145;
  public static final short i2c = 146;
  public static final short i2d = 135;
  public static final short i2f = 134;
  public static final short i2l = 133;
  public static final short i2s = 147;
  public static final short iadd = 96;
  public static final short iaload = 46;
  public static final short iand = 126;
  public static final short iastore = 79;
  public static final short iconst_m1 = 2;
  public static final short iconst_0 = 3;
  public static final short iconst_1 = 4;
  public static final short iconst_2 = 5;
  public static final short iconst_3 = 6;
  public static final short iconst_4 = 7;
  public static final short iconst_5 = 8;
  public static final short idiv = 108;
  public static final short if_acmpeq = 165;
  public static final short if_acmpne = 166;
  public static final short if_icmpeq = 159;
  public static final short if_icmpne = 160;
  public static final short if_icmplt = 161;
  public static final short if_icmpge = 162;
  public static final short if_icmpgt = 163;
  public static final short if_icmple = 164;
  public static final short ifeq = 153;
  public static final short ifne = 154;
  public static final short iflt = 155;
  public static final short ifge = 156;
  public static final short ifgt = 157;
  public static final short ifle = 158;
  public static final short ifnonnull = 199;
  public static final short ifnull = 198;
  public static final short iinc = 132;
  public static final short iload = 21;
  public static final short iload_0 = 26;
  public static final short iload_1 = 27;
  public static final short iload_2 = 28;
  public static final short iload_3 = 29;
  public static final short impdep1 = 254;
  public static final short impdep2 = 255;
  public static final short imul = 104;
  public static final short ineg = 116;
  public static final short INSTANCEOF = 193;
  public static final short invokeinterface = 185;
  public static final short invokespecial = 183;
  public static final short invokestatic = 184;
  public static final short invokevirtual = 182;
  public static final short ior = 128;
  public static final short irem = 112;
  public static final short ireturn = 172;
  public static final short ishl = 120;
  public static final short ishr = 122;
  public static final short istore = 54;
  public static final short istore_0 = 59;
  public static final short istore_1 = 60;
  public static final short istore_2 = 61;
  public static final short istore_3 = 62;
  public static final short isub = 100;
  public static final short iushr = 124;
  public static final short ixor = 130;
  public static final short jsr = 168;
  public static final short jsr_w = 201;
  public static final short l2d = 138;
  public static final short l2f = 137;
  public static final short l2i = 136;
  public static final short ladd = 97;
  public static final short laload = 47;
  public static final short land = 127;
  public static final short lastore = 80;
  public static final short lcmp = 148;
  public static final short lconst_0 = 9;
  public static final short lconst_1 = 10;
  public static final short ldc = 18;
  public static final short ldc_w = 19;
  public static final short ldc2_w = 20;
  public static final short ldiv = 109;
  public static final short lload = 22;
  public static final short lload_0 = 30;
  public static final short lload_1 = 31;
  public static final short lload_2 = 32;
  public static final short lload_3 = 33;
  public static final short lmul = 105;
  public static final short lneg = 117;
  public static final short lookupswitch = 171;
  public static final short lor = 129;
  public static final short lrem = 113;
  public static final short lreturn = 173;
  public static final short lshl = 121;
  public static final short lshr = 123;
  public static final short lstore = 55;
  public static final short lstore_0 = 63;
  public static final short lstore_1 = 64;
  public static final short lstore_2 = 65;
  public static final short lstore_3 = 66;
  public static final short lsub = 101;
  public static final short lushr = 125;
  public static final short lxor = 131;
  public static final short monitorenter = 194;
  public static final short monitorexit = 195;
  public static final short multianewarray = 197;
  public static final short NEW = 187;
  public static final short newarray = 188;
  public static final short nop = 0;
  public static final short pop = 87;
  public static final short pop2 = 88;
  public static final short putfield = 181;
  public static final short putstatic = 179;
  public static final short ret = 169;
  public static final short RETURN = 177;
  public static final short saload = 53;
  public static final short sastore = 86;
  public static final short sipush = 17;
  public static final short swap = 95;
  public static final short tableswitch = 170;
  public static final short wide = 196;
  
  /** 
   * OpcodeName is an array of opcode names which can be indexed by
   * opcode.
   */
  public static final String OpcodeName[] = 
  {
      "nop",		        		// 0
      "aconst_null",	                	// 1
      "iconst_m1",	                	// 2
      "iconst_0",	        		// 3
      "iconst_1",	            		// 4
      "iconst_2",	        		// 5
      "iconst_3",	        		// 6
      "iconst_4",	            		// 7
      "iconst_5",	        		// 8
      "lconst_0",	        		// 9
      "lconst_1",	        		// 10
      "fconst_0",	        		// 11
      "fconst_1",	        		// 12
      "fconst_2",	        		// 13
      "dconst_0",	        		// 14
      "dconst_1",	        		// 15
      "bipush",		                   	// 16
      "sipush",		                	// 17
      "ldc",		        		// 18
      "ldc_w",		                   	// 19
      "ldc2_w",		                	// 20
      "iload",		                	// 21
      "lload",		                	// 22
      "fload",		                 	// 23
      "dload",		                	// 24
      "aload",			                // 25
      "iload_0",		        	// 26
      "iload_1",		        	// 27
      "iload_2",		        	// 28
      "iload_3",		        	// 29
      "lload_0",		        	// 30
      "lload_1",		        	// 31
      "lload_2",		        	// 32
      "lload_3",		        	// 33
      "fload_0",		        	// 34
      "fload_1",	        		// 35
      "fload_2",	             		// 36
      "fload_3",	        		// 37
      "dload_0",	        		// 38
      "dload_1",	        		// 39
      "dload_2",	        		// 40
      "dload_3",	        		// 41
      "aload_0",		        	// 42
      "aload_1",		        	// 43
      "aload_2",		        	// 44
      "aload_3",		        	// 45
      "iaload",		                	// 46
      "laload",	                		// 47
      "faload",	                		// 48
      "daload",		                	// 49
      "aaload",		                	// 50
      "baload",		                	// 51
      "caload",		                	// 52
      "saload",		                	// 53
      "istore",		                	// 54
      "lstore",		                	// 55
      "fstore",		                	// 56
      "dstore",		                	// 57
      "astore",			                // 58
      "istore_0",		        	// 59
      "istore_1",			        // 60
      "istore_2",		        	// 61
      "istore_3",		        	// 62
      "lstore_0",		        	// 63
      "lstore_1",			        // 64
      "lstore_2",			        // 65
      "lstore_3",			        // 66
      "fstore_0",			        // 67
      "fstore_1",			        // 68
      "fstore_2",			        // 69
      "fstore_3",			        // 70
      "dstore_0",			        // 71
      "dstore_1",			        // 72
      "dstore_2",			        // 73
      "dstore_3",			        // 74
      "astore_0",			        // 75
      "astore_1",			        // 76
      "astore_2",			        // 77
      "astore_3",			        // 78
      "iastore",			        // 79
      "lastore",			        // 80
      "fastore",			        // 81
      "dastore",			        // 82
      "aastore",			        // 83
      "bastore",			        // 84
      "castore",			        // 85
      "sastore",			        // 86
      "pop",				        // 87
      "pop2",				        // 88
      "dup",				        // 89
      "dup_x1",			                // 90
      "dup_x2",			                // 91
      "dup2",				        // 92
      "dup2_x1",			        // 93
      "dup2_x2",		    	        // 94
      "swap",				        // 95
      "iadd",				        // 96
      "ladd",				        // 97
      "fadd",				        // 98
      "dadd",				        // 99
      "isub",				        // 100
      "lsub",				        // 101
      "fsub",				        // 102
      "dsub",				        // 103
      "imul",				        // 104
      "lmul",				        // 105
      "fmul",				        // 106
      "dmul",				        // 107
      "idiv",				        // 108
      "ldiv",				        // 109
      "fdiv",				        // 110
      "ddiv",				        // 111
      "irem",				        // 112
      "lrem",				        // 113
      "frem",				        // 114
      "drem",				        // 115
      "ineg",				        // 116
      "lneg",				        // 117
      "fneg",				        // 118
      "dneg",				        // 119
      "ishl",				        // 120
      "lshl",				        // 121
      "ishr",			            	// 122
      "lshr",			        	// 123
      "iushr",			                // 124
      "lushr",			                // 125
      "iand",				        // 126
      "land",				        // 127
      "ior",				        // 128
      "lor",				        // 129
      "ixor",				        // 130
      "lxor",				        // 131
      "iinc",				        // 132
      "i2l",				        // 133
      "i2f",				        // 134
      "i2d",				        // 135
      "l2i",				        // 136
      "l2f",				        // 137
      "l2d",				        // 138
      "f2i",				        // 139
      "f2l",				        // 140
      "f2d",				        // 141
      "d2i",				        // 142
      "d2l",				        // 143
      "d2f",				        // 144
      "i2b",				        // 145
      "i2c",				        // 146
      "i2s",				        // 147
      "lcmp",				        // 148
      "fcmpl",			                // 149
      "fcmpg",			                // 150
      "dcmpl",			                // 151
      "dcmpg",			                // 152
      "ifeq",				        // 153
      "ifne",				        // 154
      "iflt",				        // 155
      "ifge",				        // 156
      "ifgt",				        // 157
      "ifle",				        // 158
      "if_icmpeq",		                // 159
      "if_icmpne",		                // 160
      "if_icmplt",		                // 161
      "if_icmpge",		                // 162
      "if_icmpgt",		                // 163
      "if_icmple",		                // 164
      "if_acmpeq",		                // 165
      "if_acmpne",		                // 166
      "goto",				        // 167
      "jsr",				        // 168
      "ret",				        // 169
      "tableswitch",		                // 170
      "lookupswitch",		                // 171
      "ireturn",			        // 172
      "lreturn",			        // 173
      "freturn",			        // 174
      "dreturn",		            	// 175
      "areturn",		        	// 176
      "return",			                // 177
      "getstatic",		                // 178
      "putstatic",		                // 179
      "getfield",			        // 180
      "putfield",			        // 181
      "invokevirtual",	                        // 182
      "invokenonvirtual",	                // 183
      "invokestatic",		                // 184
      "invokeinterface",	                // 185
      "xxxunusedxxx",		                // 186
      "new",				        // 187 
      "newarray",			        // 188
      "anewarray",		                // 189
      "arraylength",		                // 190
      "athrow",			                // 191
      "checkcast",		                // 192
      "instanceof",		                // 193
      "monitorenter",		                // 194
      "monitorexit",		                // 195
      "wide",				        // 196
      "multianewarray",	                        // 197
      "ifnull",			                // 198
      "ifnonnull",		                // 199
      "goto_w",			                // 200
      "jsr_w",			                // 201
      "breakpoint", 	        	        // 202
      "",					// 203
      "",					// 204
      "",					// 205
      "",					// 206
      "",					// 207
      "",					// 208
      "",					// 209
      "",					// 210
      "",					// 211
      "",					// 212
      "",					// 213
      "",					// 214
      "",					// 215
      "",					// 216
      "",					// 217
      "",					// 218
      "",					// 219
      "",					// 220
      "",					// 221
      "",					// 222
      "",					// 223
      "",					// 224
      "",					// 225
      "",					// 226
      "",					// 227
      "",					// 228
      "",					// 229
      "",					// 230
      "",					// 231
      "",					// 232
      "",					// 233
      "",					// 234
      "",					// 235
      "",					// 236
      "",					// 237
      "",					// 238
      "",					// 239
      "",					// 240
      "",					// 241
      "",					// 242
      "",					// 243
      "",					// 244
      "",					// 245
      "",					// 246
      "",					// 247
      "",					// 248
      "",					// 249
      "",					// 250
      "",					// 251
      "",					// 252
      "",					// 253
      "impdep1",			        // 254
      "impdep2"			                // 255
    };

    /** 
     * OperandNumber is an array indicating how many operands an instruction
     * takes.
     * <br>
     * 0 means that the instruction takes no operand.
     * <br>
     * 1 means that the instruction takes one operand.
     *	1 byte: bipush, iload, lload, fload, dload, aload, istore, lstore, fstore, dstore,
     * 		astore, ret, newarray
     *	1 operand: ldc,
     * <br>
     * 2 means that the instruction takes two operands or has two bytes following opcode.
     * 2 bytes: sipush, iinc, 153-168 (branches), ifnull, ifnonnull
     * 2 operands: ldc_w, ldc2_w, getstatic, putstatic, getfield, putfield, invokevirtual
     * 		invokespecial, invokestatic, putfield2_quick, new, anewarray, checkcast, instanceof
     * <br>
     * 3 means that the instruction is multianewarray 
     * <br>
     * 4 means that the instruction takes four operands: goto_w, jsr_w need 4bytes, 
     *		invokeinterface needs 4 operands
     * <br>
     * 9 means that the instruction opcode is wide and that 
     * depending on the first operand, either takes two or 
     * four additional operands: wide 
     * <br>
     * -1 means that the instruction is variable length - the instruction
     * opcode has to be either lookupswitch or tablelookup.
     * <br>
     * -2 means that the instruction opcode is not valid.
     */
    public static final short OperandNumber[] = {
      0, 0, 0, 0, 0, 0, 0, 0, 0, 0,		        // 0 - 9
      0, 0, 0, 0, 0, 0, 1, 2, 1, 2,			// 10 - 19
      2, 1, 1, 1, 1, 1, 0, 0, 0, 0,			// 20 - 29
      0, 0, 0, 0, 0, 0, 0, 0, 0, 0,			// 30 - 39
      0, 0, 0, 0, 0, 0, 0, 0, 0, 0,			// 40 - 49
      0, 0, 0, 0, 1, 1, 1, 1, 1, 0,			// 50 - 59
      0, 0, 0, 0, 0, 0, 0, 0, 0, 0,			// 60 - 69
      0, 0, 0, 0, 0, 0, 0, 0, 0, 0,			// 70 - 79
      0, 0, 0, 0, 0, 0, 0, 0, 0, 0,			// 80 - 89
      0, 0, 0, 0, 0, 0, 0, 0, 0, 0,			// 90 - 99
      0, 0, 0, 0, 0, 0, 0, 0, 0, 0,			// 100 - 109
      0, 0, 0, 0, 0, 0, 0, 0, 0, 0,			// 110 - 119
      0, 0, 0, 0, 0, 0, 0, 0, 0, 0,			// 120 - 129
      0, 0, 2, 0, 0, 0, 0, 0, 0, 0,			// 130 - 139
      0, 0, 0, 0, 0, 0, 0, 0, 0, 0,			// 140 - 149
      0, 0, 0, 2, 2, 2, 2, 2, 2, 2,			// 150 - 159
      2, 2, 2, 2, 2, 2, 2, 2, 2, 1,			// 160 - 169
      -1, -1, 0, 0, 0, 0, 0, 0, 2, 2,			// 170 - 179
      2, 2, 2, 2, 2, 4, -2, 2, 1, 2,			// 180 - 189
      0, 0, 2, 2, 0, 0, 9, 3, 2, 2,			// 190 - 199
      4, 4, 0, -2, -2, -2, -2, -2, -2, 2,		// 200 - 209
      -2, -2, -2, -2, -2, -2, -2, -2, -2, -2,           // 210 - 219
      -2, -2, -2, -2, -2, -2, -2, -2, -2, -2,   	// 220 - 229
      -2, -2, -2, -2, -2, -2, -2, -2, -2, -2,   	// 230 - 239
      -2, -2, -2, -2, -2, -2, -2, -2, -2, -2,	        // 240 - 249
      -2, -2, -2, -2, 0, 0
    };
}

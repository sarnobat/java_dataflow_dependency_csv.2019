package com.rohidekar;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Stack;

import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.Field;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.LocalVariable;
import org.apache.bcel.classfile.LocalVariableTable;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.*;

/**
 * 2018-12
 *
 * <p>
 * 
 * find $PWD -maxdepth 50 -type f -iname "**class" | java -classpath
 * /Volumes/git/github/java_dataflow_dependency_csv/java_csv_data_dependency/target/java_csv_data_dependency-1.0-SNAPSHOT-jar-with-dependencies.jar
 * com.rohidekar.Main
 * 
 * 
 * usage: mvn exec:java -Dexec.args="$HOME/trash/myproj/target"
 *
 * <p>
 * A lot of this may be achievable without this tool, but instead using: javap
 * -verbose
 * $HOME/webservices/cmp/authentication-services/target/classes/com/mycompany/authentication/AuthorizationServlet.class
 * plus an awk script possibly.
 */
public class Main {

    public static void main(String[] args) {
        // to avoid different invocation of the same method being considered the same
        // node
        int counter = 0;
        Collection<String> classFilePaths = new LinkedList<String>();
        _getClassFiles: {
            if (args == null || args.length < 1) {
                // It's better to use STDIN for class files rather than class dirs so that
                // we can add more selectively (including from a txt file)
                _readClassesFromStdin: {
                    BufferedReader br = null;
                    try {
                        br = new BufferedReader(new InputStreamReader(System.in));
                        String line;
                        while ((line = br.readLine()) != null) {
                            // log message
                            // System.err.println("[DEBUG] current line is: " + line);
                            classFilePaths.add(line);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        if (br != null) {
                            try {
                                br.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                System.err.println("Main.main() No args, will try reading from stdin only");
            } else {
                // note it may not be the immediate parent of the class files (obviously?)
                String dirContainingClasses = args[0];
                System.err.println("Main.main() Has args, will try reading from stdin only");
            }
        }

        // I don't think we need this collection, just use regex from the command line
        Collection<String> relationshipsCsvLines = new LinkedList<String>();
        {
            Collection<JavaClass> javaClasses = new LinkedList<JavaClass>();
            for (String classFilePath : classFilePaths) {
                System.out.println("Main.main() " + classFilePath);
                ClassParser classParser = new ClassParser(
                        checkNotNull(Paths.get(classFilePath).toAbsolutePath().toString()));
                try {
                    JavaClass jc = checkNotNull(checkNotNull(classParser).parse());
                    javaClasses.add(jc);
                } catch (Exception e) {
                    e.printStackTrace();
//          throw new RuntimeException(e);
                }
            }

            for (JavaClass javaClass : javaClasses) {
                System.err.println("Main.main() - 1) " + javaClass.getClassName());
                if (javaClass.isEnum()) {
                    System.out.println("Main.main() - [warn] skipping enum " + javaClass.getClassName());
                    continue;
                }
                for (Method method : javaClass.getMethods()) {
                    System.err.println();
                    ConstantPoolGen cpg = new ConstantPoolGen(javaClass.getConstantPool());
                    MethodGen methodGen = new MethodGen(method, javaClass.getClassName(), cpg);
                    LocalVariableTable localVariableTable = methodGen.getMethod().getLocalVariableTable();
                    if (methodGen.getInstructionList() == null) {
                        if (skipErrors()) {
                            continue;
                        }
                    }
                    LocalVariableTable symbolTable = localVariableTable;
                    System.err.println("Main.main() - 2) " + javaClass.getClassName() + " :: " + method.getName()
                            + "() - " + methodGen.getInstructionList().size() + " instructions");
                    // -----------------------------------------------------------------
                    // main bit
                    // -----------------------------------------------------------------
                    if (methodGen.getInstructionList() != null) {

                        // We need this to "recurse" into method calls
                        // If you don't keep it in sync with your visitor everything will become wrong
                        // (I think)
                        Stack<String> stack = new Stack<String>();
                        for (InstructionHandle instructionHandle = methodGen.getInstructionList()
                                .getStart(); instructionHandle != null; instructionHandle = instructionHandle
                                        .getNext()) {
                            Instruction anInstruction = instructionHandle.getInstruction();
                            System.err.println("Main.main()" + "    stack contents: " + stack.size()
                            // + "\t"
                            // + anInstruction.getClass().getSimpleName()
                                    + "\t" + stack);
                            if (anInstruction instanceof AASTORE) {
                                System.err.println(" (unhandled) AASTORE");
                                stack.pop();
                                stack.pop();
                                stack.pop();
                            } else if (anInstruction instanceof ATHROW) {
                                System.err.println("  (unhandled) ATHROW");
                            } else if (anInstruction instanceof GETFIELD) {
                                System.err.println("  (unhandled) GETFIELD");
                            } else if (anInstruction instanceof CHECKCAST) {
                                System.err.println("  (unhandled) CHECKCAST");
                            } else if (anInstruction instanceof IF_ICMPEQ) {
                                System.err.println("  (unhandled) IF_ICMPEQ");
                            } else if (anInstruction instanceof IF_ICMPNE) {
                                System.err.println("  (unhandled) IF_ICMPNE");
                            } else if (anInstruction instanceof IF_ICMPGE) {
                                System.err.println("  (unhandled) IF_ICMPGE");
                            } else if (anInstruction instanceof IFLE) {
                                System.err.println("  (unhandled) IFLE");
                            } else if (anInstruction instanceof IADD) {
                                System.err.println("  (unhandled) IADD");
                                String a1 = stack.pop();
                                String a3 = stack.pop();
                                String ret = "ret_add";
                                stack.push(ret);
                                System.out.println(ret + "," + a1);
                                System.out.println(ret + "," + a3);
                                System.err.println(ret + " --[depends on]--> " + a1);
                                System.err.println(ret + " --[depends on]--> " + a3);
                            } else if (anInstruction instanceof IF_ICMPLT) {
                                System.err.println("  (unhandled) IF_ICMPLT");
                            } else if (anInstruction instanceof POP) {
                                System.err.println("  (unhandled) POP");
                                stack.pop();
                            } else if (anInstruction instanceof POP2) {
                                System.err.println("  (unhandled) POP2");
                                stack.pop();
                            } else if (anInstruction instanceof IFNE) {
                                System.err.println("  (unhandled) IFNE");
                            } else if (anInstruction instanceof IFNULL) {
                                System.err.println("  (unhandled) IFNULL");
                            } else if (anInstruction instanceof IFEQ) {
                                System.err.println("  (unhandled) IFEQ");
                            } else if (anInstruction instanceof IINC) {
                                System.err.println("  (unhandled) IINC");
                            } else if (anInstruction instanceof INSTANCEOF) {
                                System.err.println("  (unhandled) INSTANCEOF");
                            } else if (anInstruction instanceof INVOKEINTERFACE) {
                                System.err.println("  (unhandled) INVOKEINTERFACE");
                            } else if (anInstruction instanceof AALOAD) {
                                System.err.println("  (unhandled) AALOAD");
                            } else if (anInstruction instanceof ARRAYLENGTH) {
                                System.err.println("  (unhandled) ARRAYLENGTH");
                            } else if (anInstruction instanceof INVOKEVIRTUAL) {
                                // A method call
                                int argCount = ((INVOKEVIRTUAL) anInstruction).getArgumentTypes(cpg).length;
                                String className = ((INVOKEVIRTUAL) anInstruction).getClassName(cpg);
                                String methodName = ((INVOKEVIRTUAL) anInstruction).getMethodName(cpg);
                                System.err.println("  (unhandled) INVOKEVIRTUAL " + className + "::" + methodName + "("
                                        + argCount + ")");

                                while (argCount > 0) {
                                    --argCount;
                                    String paramName = stack.pop();
                                    System.err.println("Main.main() paramName = " + paramName);
                                }
                                stack.push("return " + className.substring(className.lastIndexOf('.') + 1) + "::"
                                        + methodName + "()");
                            } else if (anInstruction instanceof ICONST) {
                                System.err.println("  (unhandled) " + javaClass.getClassName() + "::" + method.getName()
                                        + "()\tICONST = " + anInstruction);
                                stack.push("constant_" + ((ICONST) anInstruction).getValue());
                            } else if (anInstruction instanceof SIPUSH) {
                                System.err.println("  (unhandled) " + javaClass.getClassName() + "::" + method.getName()
                                        + "()\tSIPUSH = " + anInstruction);
                                stack.push("constant_" + ((SIPUSH) anInstruction).getValue());
                            } else if (anInstruction instanceof BIPUSH) {
                                System.err.println("  (unhandled) " + javaClass.getClassName() + "::" + method.getName()
                                        + "()\tBIPUSH = " + anInstruction);
                                stack.push("constant_" + ((BIPUSH) anInstruction).getValue());
                            } else if (anInstruction instanceof ACONST_NULL) {
                                System.err.println("  (unhandled) " + javaClass.getClassName() + "::" + method.getName()
                                        + "()\t" + anInstruction);
                                stack.push("constant_null");
                            } else if (anInstruction instanceof ARETURN) {
                                System.err.println("  (unhandled) " + javaClass.getClassName() + "::" + method.getName()
                                        + "()\treturn "
                                        + ((ARETURN) anInstruction).toString(javaClass.getConstantPool())
                                        + ";\tARETURN reference (return a reference from a method) ");
                                System.err.println();
                                // I think unused return values from helper methods get left behind on the
                                // stack, so it won't be just the actual return value
                                if (stack.size() != 1) {
                                    // throw new RuntimeException(stack.toString());
                                }
                            } else if (anInstruction instanceof IRETURN) {
                                System.err.println("  (unhandled) " + javaClass.getClassName() + "::" + method.getName()
                                        + "()\treturn "
                                        + ((IRETURN) anInstruction).toString(javaClass.getConstantPool())
                                        + ";\tARETURN reference (return a reference from a method) ");
                                stack.pop();
                                // I think unused return values from helper methods get left behind on the
                                // stack, so it won't be just the actual return value
                                if (stack.size() != 1) {
                                    // throw new RuntimeException();
                                }
                            } else if (anInstruction instanceof DUP) {
                                System.err.println("  (unhandled) " + javaClass.getClassName() + "::" + method.getName()
                                        + "()\t" + stack.peek() + "\tDUP\t(duplicate the value on top of the stack) ");
                                stack.push(stack.peek());
                            } else if (anInstruction instanceof GETSTATIC) {
                                System.err.println("  (unhandled) " + javaClass.getClassName() + "::" + method.getName()
                                        + "()\t... = " + ((GETSTATIC) anInstruction).getFieldName(cpg)
                                        + ";\tGETSTATIC\t(get a static field value of a class, where the field is identified by field reference in the constant pool index): ");
                                String fieldName = ((GETSTATIC) anInstruction).getFieldName(cpg);
                                String className = javaClass.getClassName();
                                stack.push("static_" + className.substring(className.lastIndexOf('.') + 1) + "_"
                                        + method.getName() + "_" + fieldName); // confirmed
                            } else if (anInstruction instanceof NEW) {
                                System.err.println("  (unhandled) " + javaClass.getClassName() + "::" + method.getName()
                                        + "()\tnew " + ((NEW) anInstruction).getType(cpg)
                                        + "();\tNEW\t(create new object of type identified by class reference in constant pool index) ");
                                String fullClassName = ((NEW) anInstruction).getType(cpg).toString();
                                stack.push(
                                        "new_object::" + fullClassName.substring(fullClassName.lastIndexOf('.') + 1));
                            } else if (anInstruction instanceof ANEWARRAY) {
                                System.err.println("  (unhandled) " + javaClass.getClassName() + "::" + method.getName()
                                        + "()\tnew " + ((ANEWARRAY) anInstruction).getType(cpg)
                                        + "();\tANEWARRAY\t(create new object of type identified by class reference in constant pool index) ");
                                stack.push("new_array");
                            } else if (anInstruction instanceof PUTSTATIC) {
                                System.err.println("  (unhandled) " + javaClass.getClassName() + "::" + method.getName()
                                        + "()\t" + ((PUTSTATIC) anInstruction).getFieldName(cpg)
                                        + " = ...;\tPUTSTATIC\t(set static field to value in a class, where the field is identified by a field reference index in constant pool): ");
                                if (stack.isEmpty()) {
                                    if (skipErrors()) {
                                        continue;
                                    }
                                }
                                stack.pop(); // confirmed
                                System.err.println();
                            } else if (anInstruction instanceof RETURN) {
                                System.err.println("  (unhandled) " + javaClass.getClassName() + "::" + method.getName()
                                        + "()\treturn;\tRETURN\t(return void from method)");
                                // No stack pop for returning from a void method
                                // Actually, yes there is. I checked the stack. But should I do it here or leave
                                // it to the caller?
                            } else if (anInstruction instanceof IFNONNULL) {
                                System.err.println("  (unhandled) " + javaClass.getClassName() + "::" + method.getName()
                                        + "()\t" + anInstruction);
                                stack.pop();
                            } else if (anInstruction instanceof GOTO) {
                                System.err.println("  (unhandled) " + javaClass.getClassName() + "::" + method.getName()
                                        + "()\tGOTO (goes to another instruction at branchoffset): " + anInstruction);
                            } else if (anInstruction instanceof ILOAD) {
                                int variableIndex = ((ILOAD) anInstruction).getIndex();
                                System.err.println("Main.main() " + localVariableTable);
                                if (localVariableTable == null) {
                                    System.err.println(
                                            "  (unhandled) " + javaClass.getClassName() + "::" + method.getName()
                                                    + "()\tsymbol table is null for " + methodGen.getMethod());
                                    stack.push("unknown");
                                    throw new RuntimeException("Does this still get called?");
                                } else {
                                    LocalVariable variable = localVariableTable.getLocalVariable(variableIndex,
                                            instructionHandle.getPosition());
                                    if (variable == null) {
                                        if (skipErrors()) {
                                            continue;
                                        } else {
                                            throw new RuntimeException("can't find variable " + variableIndex
                                                    + ". Probably you have the wrong program counter");
                                        }
                                    }
                                    String className = javaClass.getClassName();
                                    stack.push("var " + className.substring(className.lastIndexOf('.') + 1) + "::"
                                            + method.getName() + "()::" + variable.getName());
                                    System.err.println("Main.main() just pushed onto stack (" + variableIndex + "): "
                                            + variable.getName());
                                }
                            } else if (anInstruction instanceof ALOAD) {
                                int variableIndex = ((ALOAD) anInstruction).getIndex();
                                if (localVariableTable == null) {
                                    System.err.println(
                                            "  (unhandled) " + javaClass.getClassName() + "::" + method.getName()
                                                    + "()\tsymbol table is null for " + methodGen.getMethod());
                                    stack.push("unknown");
                                } else {
                                    LocalVariable variable = localVariableTable.getLocalVariable(variableIndex, 0);
                                    String string = variable == null ? "null" : variable.getName();
                                    stack.push("var " + string);
                                    System.err.println(
                                            "Main.main() just pushed onto stack (" + variableIndex + "): " + string);
                                }
                            } else if (anInstruction instanceof ISTORE) {

                                System.err.println("  (unhandled) ISTORE");

                                if (stack.empty()) {
                                    if (skipErrors()) {
                                        continue;
                                    } else {
                                        throw new RuntimeException(
                                                "We can't be calling store if nothing was pushed onto the stack");
                                    }
                                }
                                if (localVariableTable == null) {
                                    System.err.println(
                                            "  (unhandled) " + javaClass.getClassName() + "::" + method.getName()
                                                    + "() symbol table is null for " + methodGen.getMethod());
                                    stack.pop();
                                    throw new RuntimeException("does this get called?");
                                } else {
                                    LocalVariable localVariable = localVariableTable
                                            .getLocalVariable(((ISTORE) anInstruction).getIndex());
                                    if (localVariable == null) {
                                        if (skipErrors()) {
                                            continue;
                                        }
                                    }
                                    String variableName = localVariable.getName();
                                    System.err.println("Main.main() ISTORE (store int value into variable #index "
                                            + "): Declared and assigned variable:  " + variableName);
                                    String stackExpression = stack.pop();
                                    String className = javaClass.getClassName();
                                    String left = className.substring(className.lastIndexOf('.') + 1) + "::"
                                            + method.getName() + "()::" + variableName;
                                    System.err
                                            .println("var " + left + "\t--[depends on variable]--> " + stackExpression);
                                    System.out.println(left + "," + stackExpression);
                                }
                            } else if (anInstruction instanceof ASTORE) {

                                System.err.println("  (unhandled) ISTORE");

                                if (stack.empty()) {
                                    if (skipErrors()) {
                                        continue;
                                    } else {
                                        throw new RuntimeException(
                                                "We can't be calling store if nothing was pushed onto the stack");
                                    }
                                }
                                if (localVariableTable == null) {
                                    System.err.println(
                                            "  (unhandled) " + javaClass.getClassName() + "::" + method.getName()
                                                    + "() symbol table is null for " + methodGen.getMethod());
                                    stack.pop();
                                } else {
                                    try {
                                        String variableName = localVariableTable
                                                .getLocalVariable(((ASTORE) anInstruction).getIndex()).getName();
                                        System.err.println(
                                                "Main.main() ASTORE (store a reference into a local variable #index): Declared and assigned variable:  "
                                                        + variableName);
                                        String stackExpression = stack.pop();
                                        String className = javaClass.getClassName();
                                        String right = "var " + className.substring(className.lastIndexOf('.') + 1)
                                                + "::" + method.getName() + "()::" + variableName;
                                        System.err.println(right + "\t--[depends on variable]--> " + stackExpression);
                                        System.out.println(right + "," + stackExpression);
                                    } catch (NullPointerException e) {
                                        if (skipErrors()) {
                                            System.err
                                                    .println("Main.main() [warn] skipping error for " + anInstruction);
                                            continue;
                                        }
                                    }
                                }
                            } else if (anInstruction instanceof INVOKESTATIC) {
                                String className = ((INVOKESTATIC) anInstruction).getClassName(cpg);
                                System.err.println("  (unhandled) " + javaClass.getClassName() + "::" + method.getName()
                                        + "()\t--[calls]-->\t" + className + "::"
                                        + ((INVOKESTATIC) anInstruction).getMethodName(cpg) + "() "
                                        // + anInstruction
                                        + " INVOKESTATIC (invoke a static method and puts the result on the stack (might be void); the method is identified by method reference index in constant pool): ");
                                int length = ((INVOKESTATIC) anInstruction).getArgumentTypes(cpg).length;
                                String item = "return " + className.substring(className.lastIndexOf('.') + 1) + "::"
                                        + ((INVOKESTATIC) anInstruction).getMethodName(cpg) + "() invocation_number_"
                                        + counter;
                                ++counter;
                                // TODO: I'm not sure we should be popping EVERYTHING off the stack shoudl we?
                                // Or maybe we should. To be determined.
                                boolean continu = false;
                                while (length > 0) {
                                    if (stack.size() == 0) {
                                        if (skipErrors()) {
                                            continu = true;
                                            break;
                                        }
                                    }
                                    String paramValue = stack.pop();
                                    // System.err.println("Main.main() paramValue = " + paramValue);
                                    System.err.println(item + "\t--[depends on]--> " + paramValue);
                                    System.out.println(item + "," + paramValue);
                                    --length;
                                }
                                if (continu) {
                                    continue;
                                }
                                stack.push(item);
                            } else if (anInstruction instanceof INVOKESPECIAL) {
                                System.err.println("  (unhandled) INVOKESPECIAL " + javaClass.getClassName() + "::"
                                        + method.getName() + "()\t--[calls]-->\t"
                                        + ((INVOKESPECIAL) anInstruction).getClassName(cpg) + "::"
                                        + ((INVOKESPECIAL) anInstruction).getMethodName(cpg) + "() ");

                                String paramValue1 = stack.pop();
                                System.err.println("Main.main() popped instance reference\t" + paramValue1);

                                int length = ((INVOKESPECIAL) anInstruction).getArgumentTypes(cpg).length;
                                while (length > 0) {
                                    String paramValue = stack.pop();
                                    System.err.println("Main.main() popping parameter\t\t" + paramValue);
                                    --length;
                                }

                                String className = ((INVOKESPECIAL) anInstruction).getClassName(cpg);
                                stack.push("return " + className.substring(className.lastIndexOf('.') + 1) + "_"
                                        + ((INVOKESPECIAL) anInstruction).getMethodName(cpg));
                            } else if (anInstruction instanceof LDC) {
                                stack.push("constant_" + ((LDC) anInstruction).getValue(cpg).toString());
                                System.err.println();
                            } else if (anInstruction instanceof NEWARRAY) {
                                System.err.println("  (unhandled) NEWARRAY");
                            } else if (anInstruction instanceof PUTFIELD) {
                                System.err.println("  (unhandled) PUTFIELD");
                            } else if (anInstruction instanceof LSTORE) {
                                System.err.println("  (unhandled) LSTORE");
                            } else if (anInstruction instanceof I2L) {
                                System.err.println("  (unhandled) I2L");
                            } else if (anInstruction instanceof LDC2_W) {
                                System.err.println("  (unhandled) LDC2_W");
                            } else if (anInstruction instanceof LLOAD) {
                                System.err.println("  (unhandled) LLOAD");
                            } else if (anInstruction instanceof LMUL) {
                                System.err.println("  (unhandled) LMUL");
                            } else if (anInstruction instanceof LSUB) {
                                System.err.println("  (unhandled) LSUB");
                            } else if (anInstruction instanceof LADD) {
                                System.err.println("  (unhandled) LADD");
                            } else if (anInstruction instanceof LRETURN) {
                                System.err.println("  (unhandled) LRETURN");
                            } else if (anInstruction instanceof IFLT) {
                                System.err.println("  (unhandled) IFLT");
                            } else if (anInstruction instanceof LCMP) {
                                System.err.println("  (unhandled) LCMP");
                            } else if (anInstruction instanceof IFGT) {
                                System.err.println("  (unhandled) IFGT");
                            } else if (anInstruction instanceof LDIV) {
                                System.err.println("  (unhandled) LDIV");
                            } else if (anInstruction instanceof IFGE) {
                                System.err.println("  (unhandled) IFGE");
                            } else if (anInstruction instanceof IF_ACMPNE) {
                                System.err.println("  (unhandled) IF_ACMPNE");
                            } else if (anInstruction instanceof ISTORE) {
                                System.err.println("  (unhandled) ISTORE");
                            } else if (anInstruction instanceof IALOAD) {
                                System.err.println("  (unhandled) IALOAD");
                            } else if (anInstruction instanceof IF_ICMPLE) {
                                System.err.println("  (unhandled) IF_ICMPLE");
                            } else if (anInstruction instanceof ISUB) {
                                System.err.println("  (unhandled) ISUB");
                            } else if (anInstruction instanceof IOR) {
                                System.err.println("  (unhandled) IOR");
                            } else if (anInstruction instanceof IASTORE) {
                                System.err.println("  (unhandled) IASTORE");
                            } else if (anInstruction instanceof DUP2) {
                                System.err.println("  (unhandled) DUP2");
                            } else if (anInstruction instanceof LOOKUPSWITCH) {
                                System.err.println("  (unhandled) LOOKUPSWITCH");
                            } else if (anInstruction instanceof TABLESWITCH) {
                                System.err.println("  (unhandled) TABLESWITCH");
                            } else if (anInstruction instanceof INEG) {
                                System.err.println("  (unhandled) INEG");
                            } else if (anInstruction instanceof LCONST) {
                                System.err.println("  (unhandled) LCONST");
                            } else if (anInstruction instanceof MONITORENTER) {
                                System.err.println("  (unhandled) MONITORENTER");
                            } else if (anInstruction instanceof MONITOREXIT) {
                                System.err.println("  (unhandled) MONITOREXIT");
                            } else {
                                System.err.println("  (unhandled) " + javaClass.getClassName() + "::" + method.getName()
                                        + "() " + anInstruction);
                                if (skipErrors()) {
                                    continue;
                                }
                                throw new RuntimeException("Instruction to consider visiting: "
                                        + anInstruction.getClass().getSimpleName());
                            }
                        }
                    }

                    // fields
                    Field[] fs = javaClass.getFields();
                    for (Field f : fs) {
                        // f.accept(new Visitor() {});
                    }
                }
            }
        }
        // Not needed, just do a regex replace
        _printRelationships: {
        }

        System.err.println("Now use d3_helloworld_csv.git/singlefile_automated/ for visualization");
    }

    private static boolean skipErrors() {
        return Boolean.valueOf(System.getProperty("skiperrors", "true"));
//        return System.getProperties().containsKey("skiperrors");
    }
}

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
import org.apache.bcel.classfile.Constant;
import org.apache.bcel.classfile.ConstantPool;
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
                System.err.println("[debug] Main.main() No args, will try reading from stdin only");
            } else {
                // note it may not be the immediate parent of the class files (obviously?)
                String dirContainingClasses = args[0];
                System.err.println("[debug] Main.main() Has args, will try reading from stdin only");
            }
        }

        // I don't think we need this collection, just use regex from the command line
        Collection<String> relationshipsCsvLines = new LinkedList<String>();
        {
            Collection<JavaClass> javaClasses = new LinkedList<JavaClass>();
            for (String classFilePath : classFilePaths) {
                System.err.println("[debug] Main.main() " + classFilePath);
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
                ConstantPool cp = javaClass.getConstantPool();
                System.err.println("[debug] Main.main() - 0) " + javaClass.getClassName());
            }
            for (JavaClass javaClass : javaClasses) {
                ConstantPool cp = javaClass.getConstantPool();
                System.err.println("[debug] Main.main() - 1) " + javaClass.getClassName());
                if (javaClass.isEnum()) {
                    System.err.println("[debug] Main.main() - [warn] skipping enum " + javaClass.getClassName());
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
                    String currentMethod = javaClass.getClassName() + "::" + method.getName() + "()";
                    
                    System.err.println("[debug] Main.main() - 2) " + currentMethod + " - " + methodGen.getInstructionList().size() + " instructions");
                    // -----------------------------------------------------------------
                    // main bit
                    // -----------------------------------------------------------------
                    if (methodGen.getInstructionList() != null) {

                        // We need this to "recurse" into method calls
                        // If you don't keep it in sync with your visitor everything will become wrong
                        // (I think)
                        Stack<String> stackState = new Stack<String>();
                        for (InstructionHandle instructionHandle = methodGen.getInstructionList()
                                .getStart(); instructionHandle != null; instructionHandle = instructionHandle
                                        .getNext()) {
                            Instruction anInstruction = instructionHandle.getInstruction();
                            System.err.println("[debug] Main.main()" + "    stack contents: " + stackState.size() + "\t" + stackState);
                            System.err.println("[debug] Main.main() : class: " + anInstruction.getClass().getSimpleName());
                            if (anInstruction instanceof AASTORE) {
                                System.err.println(" (unhandled) " + "AASTORE");
                                stackState.pop();
                                stackState.pop();
                                stackState.pop();
                            } else if (anInstruction instanceof ATHROW) {
                                unhandled( "ATHROW");
                            } else if (anInstruction instanceof GETFIELD) {
                                GETFIELD field = (GETFIELD)anInstruction;
                                printStack("[debug] Main.main() GETFIELD before consume", stackState);
                                int consumed = field.consumeStack(cpg);

                                printStack("[debug] Main.main() GETFIELD after consume", stackState);
                                System.err.println("[debug] Main.main() GETFIELD consumed = " + consumed);
                                String a = stackState.pop();
                                String fieldName = field.getFieldName(cpg);
//                                String value = stackState.pop();
                                String value = field.toString();
                                printTable(localVariableTable);
                                String name = field.getClassName(cpg) + "::" + field.getName(cpg);
//                                unhandled( "GETFIELD " +  name + " = " + value);
                                System.out.println("[out.csv] [getfield.csv] " +  name + "," + a );                                
                            } else if (anInstruction instanceof CHECKCAST) {
                                unhandled( "CHECKCAST");
                            } else if (anInstruction instanceof IF_ICMPEQ) {
                                unhandled( "IF_ICMPEQ");
                            } else if (anInstruction instanceof IF_ICMPNE) {
                                unhandled( "IF_ICMPNE");
                            } else if (anInstruction instanceof IF_ICMPGE) {
                                unhandled( "IF_ICMPGE");
                            } else if (anInstruction instanceof IFLE) {
                                unhandled( "IFLE");
                            } else if (anInstruction instanceof IADD) {
                                unhandled( "IADD");
                                String a1 = stackState.pop();
                                String a3 = stackState.pop();
                                String ret = "ret_add";
                                stackState.push(ret);
                                System.out.println(ret + "," + a1);
                                System.out.println(ret + "," + a3);
                                System.err.println(ret + " --[depends on]--> " + a1);
                                System.err.println(ret + " --[depends on]--> " + a3);
                            } else if (anInstruction instanceof IF_ICMPLT) {
                                unhandled( "IF_ICMPLT");
                            } else if (anInstruction instanceof POP) {
                                unhandled( "POP");
                                stackState.pop();
                            } else if (anInstruction instanceof POP2) {
                                unhandled( "POP2");
                                stackState.pop();
                            } else if (anInstruction instanceof IFNE) {
                                unhandled( "IFNE");
                            } else if (anInstruction instanceof IFNULL) {
                                unhandled( "IFNULL");
                            } else if (anInstruction instanceof IFEQ) {
                                unhandled( "IFEQ");
                            } else if (anInstruction instanceof IINC) {
                                unhandled( "IINC");
                            } else if (anInstruction instanceof INSTANCEOF) {
                                unhandled( "INSTANCEOF");
                            } else if (anInstruction instanceof INVOKEINTERFACE) {
                                unhandled( "INVOKEINTERFACE");
                            } else if (anInstruction instanceof AALOAD) {
                                unhandled( "AALOAD");
                            } else if (anInstruction instanceof ARRAYLENGTH) {
                                unhandled( "ARRAYLENGTH");
                            } else if (anInstruction instanceof INVOKEVIRTUAL) {
                                // A method call
                                int argCount = ((INVOKEVIRTUAL) anInstruction).getArgumentTypes(cpg).length;
                                String className = ((INVOKEVIRTUAL) anInstruction).getClassName(cpg);
                                String methodName = ((INVOKEVIRTUAL) anInstruction).getMethodName(cpg);
                                System.err.println("[debug]  (unhandled) " + "INVOKEVIRTUAL " + className + "::" + methodName + "("
                                        + argCount + ")");

                                while (argCount > 0) {
                                    --argCount;
                                    String paramName = stackState.pop();
                                    System.err.println("[debug] Main.main() paramName = " + paramName);
                                }
                                stackState.push("return_value_from_" + className.substring(className.lastIndexOf('.') + 1) + "::"
                                        + methodName + "()");
                            } else if (anInstruction instanceof ICONST) {
                                unhandled( "" + javaClass.getClassName() + "::" + method.getName()
                                        + "()\tICONST = " + anInstruction);
                                stackState.push("constant_" + ((ICONST) anInstruction).getValue());
                            } else if (anInstruction instanceof SIPUSH) {
                                unhandled( "" + javaClass.getClassName() + "::" + method.getName()
                                        + "()\tSIPUSH = " + anInstruction);
                                stackState.push("constant_" + ((SIPUSH) anInstruction).getValue());
                            } else if (anInstruction instanceof BIPUSH) {
                                unhandled( "" + javaClass.getClassName() + "::" + method.getName()
                                        + "()\tBIPUSH = " + anInstruction);
                                String name = ((BIPUSH)anInstruction).getName();
                                stackState.push("constant_" + ((BIPUSH) anInstruction).getValue());
                                throw new RuntimeException("PICKUP");
                            } else if (anInstruction instanceof ACONST_NULL) {
                                unhandled( "" + javaClass.getClassName() + "::" + method.getName()
                                        + "()\t" + anInstruction);
                                stackState.push("constant_null");
                            } else if (anInstruction instanceof ARETURN) {
                                unhandled( "" + javaClass.getClassName() + "::" + method.getName()
                                        + "()\treturn "
                                        + ((ARETURN) anInstruction).toString(javaClass.getConstantPool())
                                        + ";\tARETURN reference (return a reference from a method) ");
                                System.err.println();
                                // I think unused return values from helper methods get left behind on the
                                // stack, so it won't be just the actual return value
                                if (stackState.size() != 1) {
                                    // throw new RuntimeException(stack.toString());
                                }
                            } else if (anInstruction instanceof IRETURN) {
                                unhandled( "" + javaClass.getClassName() + "::" + method.getName()
                                        + "()\treturn "
                                        + ((IRETURN) anInstruction).toString(javaClass.getConstantPool())
                                        + ";\tIRETURN reference (return a reference/address from a method) ");
                                stackState.pop();
                                // I think unused return values from helper methods get left behind on the
                                // stack, so it won't be just the actual return value
                                if (stackState.size() != 1) {
                                    // throw new RuntimeException();
                                }
                            } else if (anInstruction instanceof DUP) {
                                DUP copy = (DUP) anInstruction;
                                
//                                unhandled( "DUP " + javaClass.getClassName() + "::" + method.getName()
//                                        + "()\t" + stackState.peek() + "\tDUP\t(duplicate the value on top of the stack, and push it onto the stack) " + copy.getName());
                                printStack("[debug] Main.main() DUP stack before ", stackState);
                                stackState.push(stackState.peek());
                                printStack("[debug] Main.main() DUP stack after ", stackState);
                            } else if (anInstruction instanceof GETSTATIC) {
//                                unhandled( "" + javaClass.getClassName() + "::" + method.getName()
//                                        + "()\t... = " + ((GETSTATIC) anInstruction).getFieldName(cpg)
//                                        + ";\tGETSTATIC\t(get a static field value of a class, where the field is identified by field reference in the constant pool index): ");
                                String fieldName = ((GETSTATIC) anInstruction).getFieldName(cpg);
                                String className = javaClass.getClassName();
                                stackState.push("static_" + className.substring(className.lastIndexOf('.') + 1) + "_"
                                        + method.getName() + "_" + fieldName); // confirmed
                            } else if (anInstruction instanceof NEW) {
                                NEW constructorCall = (NEW) anInstruction;
//                                unhandled( "" + javaClass.getClassName() + "::" + method.getName()
//                                        + "()\t"
//                                        + ("new_object_of_type_" + constructorCall.getType(cpg)
//                                                                                + "()") + ";\tNEW\t(create new object of type identified by class reference in constant pool index) ");
                                String fullClassName = ((NEW) anInstruction).getType(cpg).toString();
                                
                                String constructorName = "new_object_of_type_"
                                        + fullClassName.substring(fullClassName.lastIndexOf('.') + 1);
//                                System.out.println("[out.csv] [new.csv] " + currentMethod + "," + constructorName);
                                
                                
//                                int lengthOfCalleeArgs = ((NEW) anInstruction).consumeStack(cpg);
//                                String item = "return_value_from_" + className.substring(className.lastIndexOf('.') + 1) + "::"
//                                        + ((INVOKESTATIC) anInstruction).getMethodName(cpg) + "()_invocation_number_"
//                                        + counter;
//                                ++counter;
//                                // TODO: I'm not sure we should be popping EVERYTHING off the stack should we?
//                                // Or maybe we should. To be determined.
//                                boolean continu = false;
//                                while (lengthOfCalleeArgs > 0) {
//                                    if (whoCalledMeStack.size() == 0) {
//                                        if (skipErrors()) {
//                                            continu = true;
//                                            break;
//                                        }
//                                    }
//                                    String paramValue = whoCalledMeStack.pop();
//                                    // System.err.println("[debug] Main.main() paramValue = " + paramValue);
//                                    System.err.println(item + "\t--[depends on]--> " + paramValue);
//                                    System.out.println("[out.csv] [invoke_static.csv] " + item + "," + paramValue);
//                                    --lengthOfCalleeArgs;
//                                }
//                                if (continu) {
//                                    continue;
//                                }
                                
                                stackState.push(constructorName);
                            } else if (anInstruction instanceof ANEWARRAY) {
                                unhandled( "" + javaClass.getClassName() + "::" + method.getName()
                                        + "()\tnew " + ((ANEWARRAY) anInstruction).getType(cpg)
                                        + "();\tANEWARRAY\t(create new object of type identified by class reference in constant pool index) ");
                                stackState.push("new_array");
                            } else if (anInstruction instanceof PUTSTATIC) {
                                unhandled( "" + javaClass.getClassName() + "::" + method.getName()
                                        + "()\t" + ((PUTSTATIC) anInstruction).getFieldName(cpg)
                                        + " = ...;\tPUTSTATIC\t(set static field to value in a class, where the field is identified by a field reference index in constant pool): ");
                                if (stackState.isEmpty()) {
                                    if (skipErrors()) {
                                        continue;
                                    }
                                }
                                stackState.pop(); // confirmed
                                System.err.println();
                            } else if (anInstruction instanceof RETURN) {
//                                unhandled( "" + javaClass.getClassName() + "::" + method.getName()
//                                        + "()\treturn;\tRETURN\t(return void from method)");
                                // No stack pop for returning from a void method
                                // Actually, yes there is. I checked the stack. But should I do it here or leave
                                // it to the caller?
                                System.err.println("[warn] Main.main() - TODO: I think we need to pop from the stack. But should I do it here or leave  it to the caller?" + javaClass.getClassName() + "::" + method.getName()
                                + "()\treturn;\tRETURN\t(return void from method)");
                            } else if (anInstruction instanceof IFNONNULL) {
                                unhandled( "" + javaClass.getClassName() + "::" + method.getName()
                                        + "()\t" + anInstruction);
                                stackState.pop();
                            } else if (anInstruction instanceof GOTO) {
                                unhandled( "" + javaClass.getClassName() + "::" + method.getName()
                                        + "()\tGOTO (goes to another instruction at branchoffset): " + anInstruction);
                            } else if (anInstruction instanceof ILOAD) {
                                int variableIndex = ((ILOAD) anInstruction).getIndex();
                                System.err.println("[debug] Main.main() ILOAD " + localVariableTable);
                                if (localVariableTable == null) {
                                    System.err.println(
                                            "  (unhandled) " + "" + javaClass.getClassName() + "::" + method.getName()
                                                    + "()\tsymbol table is null for " + methodGen.getMethod());
                                    stackState.push("unknown");
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
                                    String className = variable.getSignature();
                                    stackState.push("local_variable_inside_" + className.substring(className.lastIndexOf('.') + 1) + "::"
                                            + method.getName() + "()::" + variable.getName());
                                    System.err.println("[debug] Main.main() ILOAD signature " + variable.getSignature());
                                    System.err.println("[debug] Main.main() ILOAD just pushed onto stack (" + variableIndex + "): "
                                            + variable.getName());
                                }
                            } else if (anInstruction instanceof ALOAD) {
                                ALOAD aload = (ALOAD) anInstruction;
                                
                                int variableIndex = aload.getIndex();
                                String methodName = javaClass.getClassName() + "::" + method.getName() + "()";
                                if (localVariableTable == null) {
                                    System.err.println(
                                            "  (unhandled) " + "" + methodName + "\tsymbol table is null for " + methodGen.getMethod());
                                    stackState.push("unknown");
                                } else {
                                    LocalVariable variable = localVariableTable.getLocalVariable(variableIndex);
                                    System.err.println("[debug] Main.main() ALOAD variable = " + variable);
                                    System.err.println(
                                            "[debug] Main.main() ALOAD variable index = " + variable.getIndex());
                                    System.err
                                            .println("[debug] Main.main() ALOAD variable name = " + variable.getName());
                                    System.err.println("[debug] Main.main() ALOAD variable nameIndex = "
                                            + variable.getNameIndex());
                                    printTable(localVariableTable);
                                    System.err
                                            .println("[debug] Main.main() ALOAD instruction name = " + aload.getName());

//                                    String string = variable == null ? "null" : variable.getName();
//                                    String index = aload.toString(javaClass.getConstantPool());
//                                    System.out.println("[debug] Main.main() index " + index);
//                                    runtimeStack.push("local_variable_inside_" + methodName + "_" + index);
//                                    runtimeStack.push(index);
                                    System.out.println(
                                            "[out.csv] [aload.csv] " + variable.getName() + "," + aload.getName());
                                    stackState.push(variable.getName());
//                                    System.err.println(
//                                            "[debug] Main.main() ALOAD just pushed onto stack (" + variableIndex + "): " + string + " " + aload.getName() + " " + aload.getType(cpg));
                                }
                            } else if (anInstruction instanceof ISTORE) {

//                                unhandled( "ISTORE");

                                if (stackState.empty()) {
                                    if (skipErrors()) {
                                        continue;
                                    } else {
                                        throw new RuntimeException(
                                                "We can't be calling store if nothing was pushed onto the stack");
                                    }
                                }
                                if (localVariableTable == null) {
                                    System.err.println(
                                            "  (unhandled) " + "" + javaClass.getClassName() + "::" + method.getName()
                                                    + "() symbol table is null for " + methodGen.getMethod());
                                    stackState.pop();
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
//                                    System.err.println("[debug] Main.main() ISTORE (store int value into variable #index "
//                                            + "): Declared and assigned variable:  " + variableName);
                                    String stackExpression = stackState.pop();
                                    String className = javaClass.getClassName();
                                    String left = "local_variable_inside_" + className.substring(className.lastIndexOf('.') + 1) + "::"
                                            + method.getName() + "()"
                                                    + "::" + variableName;
//                                    System.err
//                                            .println("var " + left + "\t--[depends on variable]--> " + stackExpression);
                                    System.out.println("[out.csv] [istore.csv] " + left + "," + stackExpression);
                                }
                            } else if (anInstruction instanceof ASTORE) {

//                                unhandled( "ASTORE");

                                if (stackState.empty()) {
                                    if (skipErrors()) {
                                        continue;
                                    } else {
                                        throw new RuntimeException(
                                                "We can't be calling store if nothing was pushed onto the stack");
                                    }
                                }
//                                if (localVariableTable == null) {
//                                    System.err.println(
//                                            "  (unhandled) " + "" + javaClass.getClassName() + "::" + method.getName()
//                                                    + "() symbol table is null for " + methodGen.getMethod());
//                                    stackState.pop();
//                                } else {
                                    try {
                                        ASTORE astore = (ASTORE) anInstruction;
                                        int stackSize = astore.consumeStack(cpg);
                                        System.err.println("[debug] Main.main() ASTORE " + astore.getLength() + "::" + stackSize);
                                        int index = astore.getIndex();
                                        if ("null".equals(astore.getName())) {
                                            System.err.println("[debug] Main.main() variable is null because maybe the target class hasn't been parsed yet. Not sure.");
                                        }
                                        String addressNull = stackState.pop();
                                        String address = "address_" + javaClass.getClassName().substring(javaClass.getClassName().lastIndexOf('.') + 1)
                                                + "::" + method.getName() + "()::" + astore.getName();
//                                        System.err.println(
//                                                "[debug] Main.main() ASTORE (store a reference into a local variable #index): Declared and assigned variable: "
//                                                        + astore.getName() + " " + addressNull + " " + astore.getName());
                                        System.err.println("[debug] Main.main() ASTORE stack top popped = " + addressNull);
                                        System.err.println("[debug] Main.main() ASTORE name = " + astore.getName());
//                                        System.err.println(right + "\t--[depends on variable]--> " + stackExpression);
//                                        System.out.println("[out.csv] [astore.csv] " +"???????????"+","+ address );
                                    } catch (NullPointerException e) {
                                        if (skipErrors()) {
                                            System.err
                                                    .println("[debug] Main.main() [warn] skipping error for " + anInstruction);
                                            continue;
                                        }
                                    }
//                                }
                            } else if (anInstruction instanceof INVOKESTATIC) {
                                String className = ((INVOKESTATIC) anInstruction).getClassName(cpg);

                                String caller = javaClass.getClassName() + "::" + method.getName() + "()";
                                String callee = className + "::" + ((INVOKESTATIC) anInstruction).getMethodName(cpg)
                                        + "()";

                                unhandled( "" + caller + "\t--[calls]-->\t" + callee + " "
                                        // + anInstruction
                                        + " INVOKESTATIC (invoke a static method and puts the result on the stack (might be void); the method is identified by method reference index in constant pool): ");
                                int lengthOfCalleeArgs = ((INVOKESTATIC) anInstruction).getArgumentTypes(cpg).length;
                                String item = "return_value_from_" + className.substring(className.lastIndexOf('.') + 1) + "::"
                                        + ((INVOKESTATIC) anInstruction).getMethodName(cpg) + "()_invocation_number_"
                                        + counter;
                                ++counter;
                                // TODO: I'm not sure we should be popping EVERYTHING off the stack should we?
                                // Or maybe we should. To be determined.
                                boolean continu = false;
                                while (lengthOfCalleeArgs > 0) {
                                    if (stackState.size() == 0) {
                                        if (skipErrors()) {
                                            continu = true;
                                            break;
                                        }
                                    }
                                    String paramValue = stackState.pop();
                                    // System.err.println("[debug] Main.main() paramValue = " + paramValue);
                                    System.err.println(item + "\t--[depends on]--> " + paramValue);
                                    System.out.println("[out.csv] [invoke_static.csv] " + item + "," + paramValue);
                                    --lengthOfCalleeArgs;
                                }
                                if (continu) {
                                    continue;
                                }
                                stackState.push(item);
                            } else if (anInstruction instanceof INVOKESPECIAL) {
//                                unhandled( "INVOKESPECIAL " + javaClass.getClassName() + "::"
//                                        + method.getName() + "()\t--[calls]-->\t"
//                                        + ((INVOKESPECIAL) anInstruction).getClassName(cpg) + "::"
//                                        + ((INVOKESPECIAL) anInstruction).getMethodName(cpg) + "() ");
                                INVOKESPECIAL invokespecial = (INVOKESPECIAL) anInstruction;
                                String className = ((INVOKESPECIAL) anInstruction).getClassName(cpg);
                                String methodName = ((INVOKESPECIAL) anInstruction).getMethodName(cpg);
                                int length = invokespecial.getArgumentTypes(cpg).length;
                                System.err.println("[debug] Main.main() INVOKESPECIAL - " + className + "::"
                                        + methodName + "(" + length + ")");
                                String paramValue1 = stackState.pop();
//                                System.err.println("[debug] Main.main() popped instance reference\t" + paramValue1);

                                while (length > 0) {
                                    String paramValue = stackState.pop();
//                                    System.err.println("[debug] Main.main() popping parameter\t\t" + paramValue);
                                    --length;
                                }

                                stackState.push("return " + className.substring(className.lastIndexOf('.') + 1) + "_"
                                        + methodName);
                            } else if (anInstruction instanceof LDC) {
                                LDC ldc = (LDC) anInstruction;
                                Constant value = cp.getConstant(ldc.getIndex());
                                Constant value1 = cpg.getConstant(ldc.getIndex());
                                System.err.println("[debug] Main.main() LDC (load constant) type = " + ldc.getType(cpg));
                                System.err.println("[debug] Main.main() LDC (load constant) index = " + ldc.getIndex());
                                System.err.println("[debug] Main.main() LDC (load constant) length = " + ldc.getLength());
                                System.err.println("[debug] Main.main() LDC (load constant) name = " + ldc.getName());
                                System.err.println(
                                        "[debug] Main.main() LDC (load constant) value = " + value1);
                                stackState.push("constant_" + ldc.getValue(cpg).toString());
                            } else if (anInstruction instanceof NEWARRAY) {
                                unhandled( "NEWARRAY");
                            } else if (anInstruction instanceof PUTFIELD) {
                                unhandled( "PUTFIELD");
                            } else if (anInstruction instanceof LSTORE) {
                                unhandled( "LSTORE");
                            } else if (anInstruction instanceof I2L) {
                                unhandled( "I2L");
                            } else if (anInstruction instanceof LDC2_W) {
                                unhandled( "LDC2_W");
                            } else if (anInstruction instanceof LLOAD) {
                                unhandled( "LLOAD");
                            } else if (anInstruction instanceof LMUL) {
                                unhandled( "LMUL");
                            } else if (anInstruction instanceof LSUB) {
                                unhandled( "LSUB");
                            } else if (anInstruction instanceof LADD) {
                                unhandled( "LADD");
                            } else if (anInstruction instanceof LRETURN) {
                                unhandled( "LRETURN");
                            } else if (anInstruction instanceof IFLT) {
                                unhandled( "IFLT");
                            } else if (anInstruction instanceof LCMP) {
                                unhandled( "LCMP");
                            } else if (anInstruction instanceof IFGT) {
                                unhandled( "IFGT");
                            } else if (anInstruction instanceof LDIV) {
                                unhandled( "LDIV");
                            } else if (anInstruction instanceof IFGE) {
                                unhandled( "IFGE");
                            } else if (anInstruction instanceof IF_ACMPNE) {
                                unhandled( "IF_ACMPNE");
                            } else if (anInstruction instanceof ISTORE) {
                                unhandled( "ISTORE");
                            } else if (anInstruction instanceof IALOAD) {
                                unhandled( "IALOAD");
                            } else if (anInstruction instanceof IF_ICMPLE) {
                                unhandled( "IF_ICMPLE");
                            } else if (anInstruction instanceof ISUB) {
                                unhandled( "ISUB");
                            } else if (anInstruction instanceof IOR) {
                                unhandled( "IOR");
                            } else if (anInstruction instanceof IASTORE) {
                                unhandled( "IASTORE");
                            } else if (anInstruction instanceof DUP2) {
                                unhandled( "DUP2");
                            } else if (anInstruction instanceof LOOKUPSWITCH) {
                                unhandled( "LOOKUPSWITCH");
                            } else if (anInstruction instanceof TABLESWITCH) {
                                unhandled( "TABLESWITCH");
                            } else if (anInstruction instanceof INEG) {
                                unhandled( "INEG");
                            } else if (anInstruction instanceof LCONST) {
                                unhandled( "LCONST");
                            } else if (anInstruction instanceof MONITORENTER) {
                                unhandled( "MONITORENTER");
                            } else if (anInstruction instanceof MONITOREXIT) {
                                unhandled( "MONITOREXIT");
                            } else {
                                unhandled( "" + javaClass.getClassName() + "::" + method.getName()
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

    private static void unhandled(String string) {
        if (skipErrors()) {
            System.err.println("[warning] "+  string);
        }else {
            System.err.println("[fatal] " + string);
            throw new RuntimeException(string);
        }
    }

    private static void printStack(String string, Stack stack) {
        System.err.println(string + " " + stack);
    }

    private static void printTable(LocalVariableTable localVariableTable) {
        for(int i = 0; i < localVariableTable.getTableLength(); i++) {
            LocalVariable variable =localVariableTable.getLocalVariable(i);
            System.err.println("[debug] Main.printTable() " + variable.getIndex() + "\t" + variable.getName());
        }
    }

    private static boolean skipErrors() {
//        return Boolean.valueOf(System.getProperty("skiperrors", "true"));
        return Boolean.valueOf(System.getProperty("skiperrors", "false"));
    }
}

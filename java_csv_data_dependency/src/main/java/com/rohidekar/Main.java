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
import org.apache.bcel.generic.AALOAD;
import org.apache.bcel.generic.ACONST_NULL;
import org.apache.bcel.generic.ALOAD;
import org.apache.bcel.generic.ARETURN;
import org.apache.bcel.generic.*;
import org.apache.bcel.generic.CHECKCAST;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.ConstantPushInstruction;
import org.apache.bcel.generic.DUP;
import org.apache.bcel.generic.GETSTATIC;
import org.apache.bcel.generic.GOTO;
import org.apache.bcel.generic.ICONST;
import org.apache.bcel.generic.IFEQ;
import org.apache.bcel.generic.IFNE;
import org.apache.bcel.generic.IFNONNULL;
import org.apache.bcel.generic.ILOAD;
import org.apache.bcel.generic.INVOKEINTERFACE;
import org.apache.bcel.generic.INVOKESPECIAL;
import org.apache.bcel.generic.INVOKESTATIC;
import org.apache.bcel.generic.INVOKEVIRTUAL;
import org.apache.bcel.generic.ISTORE;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.LDC;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.NEW;
import org.apache.bcel.generic.PUTFIELD;
import org.apache.bcel.generic.PUTSTATIC;
import org.apache.bcel.generic.RETURN;

/**
 * 2018-12
 *
 * <p>usage: -Dexec.args="/Users/ssarnobat/trash/myproj/target"
 *
 * <p>A lot of this may be achievable with: javap -verbose
 * /Users/ssarnobat/webservices/cmp/authentication-services/target/classes/com/control4/authentication/AuthorizationServlet.class
 */
public class Main {

  @SuppressWarnings("unused")
  public static void main(String[] args) {

    Collection<String> classFilePaths = new LinkedList<String>();
    _getClassFiles:
    {
      if (args == null || args.length < 1) {
        // It's better to use STDIN for class files rather than class dirs so that
        // we can add more selectively (including from a txt file)
        _readClassesFromStdin:
        {
          BufferedReader br = null;
          try {
            br = new BufferedReader(new InputStreamReader(System.in));
            String line;
            while ((line = br.readLine()) != null) {
              // log message
              //              System.err.println("[DEBUG] current line is: " + line);
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

    Collection<String> relationshipsCsvLines = null;
    {
      Collection<JavaClass> javaClasses = new LinkedList<JavaClass>();
      for (String classFilePath : classFilePaths) {
        ClassParser classParser =
            new ClassParser(checkNotNull(Paths.get(classFilePath).toAbsolutePath().toString()));
        try {
          JavaClass jc = checkNotNull(checkNotNull(classParser).parse());
          javaClasses.add(jc);
        } catch (Exception e) {
          e.printStackTrace();
          throw new RuntimeException(e);
        }
      }

      for (JavaClass javaClass : javaClasses) {
        System.err.println("1) " + javaClass.getClassName());
        for (Method method : javaClass.getMethods()) {
          System.err.println();
          ConstantPoolGen cpg = new ConstantPoolGen(javaClass.getConstantPool());
          MethodGen methodGen = new MethodGen(method, javaClass.getClassName(), cpg);
          LocalVariableTable symbolTable = methodGen.getMethod().getLocalVariableTable();
          System.out.println(
              "2) "
                  + javaClass.getClassName()
                  + " :: "
                  + method.getName()
                  + "() - "
                  + methodGen.getInstructionList().size()
                  + " instructions");
          // main bit
          if (methodGen.getInstructionList() != null) {

            Stack<String> stack = new Stack<String>();
            for (InstructionHandle instructionHandle = methodGen.getInstructionList().getStart();
                instructionHandle != null;
                instructionHandle = instructionHandle.getNext()) {
              Instruction anInstruction = instructionHandle.getInstruction();

              if (anInstruction instanceof AASTORE) {
                System.out.println("(unhandled) AASTORE");
              } else if (anInstruction instanceof ATHROW) {
                System.out.println("(unhandled) ATHROW");
              } else if (anInstruction instanceof GETFIELD) {
                System.out.println("(unhandled) GETFIELD");
              } else if (anInstruction instanceof CHECKCAST) {
                System.out.println("(unhandled) CHECKCAST");
              } else if (anInstruction instanceof IF_ICMPNE) {
                System.out.println("(unhandled) IF_ICMPNE");
              } else if (anInstruction instanceof IF_ICMPLT) {
                System.out.println("(unhandled) IF_ICMPLT");
              } else if (anInstruction instanceof POP) {
                System.out.println("(unhandled) POP");
                stack.pop();
              } else if (anInstruction instanceof POP2) {
                System.out.println("(unhandled) POP2");
                stack.pop();
              } else if (anInstruction instanceof IFNE) {
                System.out.println("(unhandled) IFNE");
              } else if (anInstruction instanceof IFNULL) {
                System.out.println("(unhandled) IFNULL");
              } else if (anInstruction instanceof IFEQ) {
                System.out.println("(unhandled) IFEQ");
              } else if (anInstruction instanceof IINC) {
                System.out.println("(unhandled) IINC");
              } else if (anInstruction instanceof INVOKEINTERFACE) {
                System.out.println("(unhandled) INVOKEINTERFACE");
              } else if (anInstruction instanceof ISTORE) {
                System.out.println("(unhandled) ISTORE");
              } else if (anInstruction instanceof ILOAD) {
                System.out.println("(unhandled) ILOAD");
              } else if (anInstruction instanceof AALOAD) {
                System.out.println("(unhandled) AALOAD");
              } else if (anInstruction instanceof ARRAYLENGTH) {
                System.out.println("(unhandled) ARRAYLENGTH");
              } else if (anInstruction instanceof INVOKEVIRTUAL) {
                System.err.println(
                    "  (unhandled) INVOKEVIRTUAL "
                        + ((INVOKEVIRTUAL) anInstruction).getClassName(cpg)
                        + "::"
                        + ((INVOKEVIRTUAL) anInstruction).getMethodName(cpg)
                        + "()");

                int argCount = ((INVOKEVIRTUAL) anInstruction).getArgumentTypes(cpg).length;
                while (argCount > -1) {
                  --argCount;
                  String paramName = stack.pop();
                  System.out.println("Main.main() paramName = " + paramName);
                }
                stack.push("ret");
              } else if (anInstruction instanceof ConstantPushInstruction) {
                System.out.println(
                    "  (unhandled) "
                        + javaClass.getClassName()
                        + "::"
                        + method.getName()
                        + "()\tConstantPushInstruction = "
                        + ((ConstantPushInstruction) anInstruction).getValue());
              } else if (anInstruction instanceof ICONST) {
                throw new RuntimeException();
              } else if (anInstruction instanceof ACONST_NULL) {
                System.out.println(
                    "  (unhandled) "
                        + javaClass.getClassName()
                        + "::"
                        + method.getName()
                        + "()\t"
                        + anInstruction);
                stack.push("null");
              } else if (anInstruction instanceof ARETURN) {
                System.err.println(
                    "  (unhandled) "
                        + javaClass.getClassName()
                        + "::"
                        + method.getName()
                        + "()\treturn "
                        + ((ARETURN) anInstruction).toString(javaClass.getConstantPool())
                        + ";\tARETURN reference (return a reference from a method) ");
                System.err.println();
              } else if (anInstruction instanceof IRETURN) {
                System.err.println(
                    "  (unhandled) "
                        + javaClass.getClassName()
                        + "::"
                        + method.getName()
                        + "()\treturn "
                        + ((IRETURN) anInstruction).toString(javaClass.getConstantPool())
                        + ";\tARETURN reference (return a reference from a method) ");
                stack.pop();
                System.err.println();
              } else if (anInstruction instanceof DUP) {
                System.err.println(
                    "  (unhandled) "
                        + javaClass.getClassName()
                        + "::"
                        + method.getName()
                        + "()\t"
                        + stack.peek()
                        + "\tDUP\t(duplicate the value on top of the stack) ");
                stack.push(stack.peek());
              } else if (anInstruction instanceof GETSTATIC) {
                System.out.println(
                    "  (unhandled) "
                        + javaClass.getClassName()
                        + "::"
                        + method.getName()
                        + "()\t... = "
                        + ((GETSTATIC) anInstruction).getFieldName(cpg)
                        + ";\tGETSTATIC\t(get a static field value of a class, where the field is identified by field reference in the constant pool index): ");
                stack.push(((GETSTATIC) anInstruction).getFieldName(cpg)); // confirmed
              } else if (anInstruction instanceof NEW) {
                System.err.println(
                    "  (unhandled) "
                        + javaClass.getClassName()
                        + "::"
                        + method.getName()
                        + "()\tnew "
                        + ((NEW) anInstruction).getType(cpg)
                        + "();\tNEW\t(create new object of type identified by class reference in constant pool index) ");
                stack.push("new_object");
              } else if (anInstruction instanceof ANEWARRAY) {
                System.err.println(
                    "  (unhandled) "
                        + javaClass.getClassName()
                        + "::"
                        + method.getName()
                        + "()\tnew "
                        + ((ANEWARRAY) anInstruction).getType(cpg)
                        + "();\tANEWARRAY\t(create new object of type identified by class reference in constant pool index) ");
                stack.push("new_array");
              } else if (anInstruction instanceof PUTSTATIC) {
                System.err.println(
                    "  (unhandled) "
                        + javaClass.getClassName()
                        + "::"
                        + method.getName()
                        + "()\t"
                        + ((PUTSTATIC) anInstruction).getFieldName(cpg)
                        + " = ...;\tPUTSTATIC\t(set static field to value in a class, where the field is identified by a field reference index in constant pool): ");
                stack.pop(); //confirmed
                System.err.println();
              } else if (anInstruction instanceof RETURN) {
                System.err.println(
                    "  (unhandled) "
                        + javaClass.getClassName()
                        + "::"
                        + method.getName()
                        + "()\treturn;\tRETURN\t(return void from method)");
                // No stack pop for returning from a void method
              } else if (anInstruction instanceof IFNONNULL) {
                System.out.println(
                    "  (unhandled) "
                        + javaClass.getClassName()
                        + "::"
                        + method.getName()
                        + "()\t"
                        + anInstruction);
              } else if (anInstruction instanceof GOTO) {
                System.err.println(
                    "  (unhandled) "
                        + javaClass.getClassName()
                        + "::"
                        + method.getName()
                        + "()\tGOTO (goes to another instruction at branchoffset): "
                        + anInstruction);
              } else if (anInstruction instanceof ALOAD) {
                int variableIndex = ((ALOAD) anInstruction).getIndex();
                if (methodGen.getMethod().getLocalVariableTable() == null) {
                  System.err.println(
                      "  (unhandled) "
                          + javaClass.getClassName()
                          + "::"
                          + method.getName()
                          + "()\tsymbol table is null for "
                          + methodGen.getMethod());
                } else {
                  LocalVariable variable =
                      methodGen
                          .getMethod()
                          .getLocalVariableTable()
                          .getLocalVariable(variableIndex, 0);
                  String string = variable == null ? "null" : variable.getName();
                  stack.push(string);
                  System.err.println("Main.main() just pushed onto stack: " + string);
                }
              } else if (anInstruction instanceof ASTORE) {

                if (methodGen.getMethod().getLocalVariableTable() == null) {
                  System.err.println(
                      "  (unhandled) "
                          + javaClass.getClassName()
                          + "::"
                          + method.getName()
                          + "() symbol table is null for "
                          + methodGen.getMethod());
                } else {
                  String variableName =
                      methodGen
                          .getMethod()
                          .getLocalVariableTable()
                          .getLocalVariable(((ASTORE) anInstruction).getIndex())
                          .getName();
                  System.err.println(
                      "Main.main() ASTORE (store a reference into a local variable #index): Declared and assigned variable:  "
                          + variableName);
                  stack.pop();
                }
              } else if (anInstruction instanceof INVOKESTATIC) {
                System.err.println(
                    "  (unhandled) "
                        + javaClass.getClassName()
                        + "::"
                        + method.getName()
                        + "()\t--[calls]-->\t"
                        + ((INVOKESTATIC) anInstruction).getClassName(cpg)
                        + "::"
                        + ((INVOKESTATIC) anInstruction).getMethodName(cpg)
                        + "() "
                        //+ anInstruction
                        + " INVOKESTATIC (invoke a static method and puts the result on the stack (might be void); the method is identified by method reference index in constant pool): ");
                int length = ((INVOKESTATIC) anInstruction).getArgumentTypes(cpg).length;
                // TODO: I'm not sure we should be popping EVERYTHING off the stack shoudl we? Or maybe we should. To be determined.
                while (length > 0) {
                  String paramValue = stack.pop();
                  System.err.println("Main.main() paramValue = " + paramValue);
                  --length;
                }
                stack.push("ret");
              } else if (anInstruction instanceof INVOKESPECIAL) {
              } else if (anInstruction instanceof LDC) {
                stack.push(((LDC) anInstruction).getValue(cpg).toString());
              } else if (anInstruction instanceof PUTFIELD) {
                PUTFIELD p = (PUTFIELD) anInstruction;
                String fieldNameBeingAssigned = p.getName(methodGen.getConstantPool());
                while (!stack.isEmpty()) {
                  System.out.println(
                      "  3) " + fieldNameBeingAssigned + "\t--[assignment]--> " + stack.pop());
                }
              } else {
                System.out.println(
                    "  (unhandled) "
                        + javaClass.getClassName()
                        + "::"
                        + method.getName()
                        + "() "
                        + anInstruction);
                throw new RuntimeException(
                    "Instruction to consider visiting: "
                        + anInstruction.getClass().getSimpleName());
              }
            }
          }

          // fields
          Field[] fs = javaClass.getFields();
          for (Field f : fs) {
            //f.accept(new Visitor() {});
          }
        }
      }
    }
    _printRelationships:
    {
    }

    System.err.println("Now use d3_helloworld_csv.git/singlefile_automated/ for visualization");
  }
}

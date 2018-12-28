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
import org.apache.bcel.classfile.Field;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.LocalVariable;
import org.apache.bcel.classfile.LocalVariableTable;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ACONST_NULL;
import org.apache.bcel.generic.ALOAD;
import org.apache.bcel.generic.ARETURN;
import org.apache.bcel.generic.ASTORE;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.ConstantPushInstruction;
import org.apache.bcel.generic.DUP;
import org.apache.bcel.generic.GETSTATIC;
import org.apache.bcel.generic.GOTO;
import org.apache.bcel.generic.IFNONNULL;
import org.apache.bcel.generic.INVOKESPECIAL;
import org.apache.bcel.generic.INVOKESTATIC;
import org.apache.bcel.generic.INVOKEVIRTUAL;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.LDC;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.NEW;
import org.apache.bcel.generic.ObjectType;
import org.apache.bcel.generic.PUTFIELD;
import org.apache.bcel.generic.PUTSTATIC;
import org.apache.bcel.generic.RETURN;
import org.apache.bcel.generic.Type;

import gr.gousiosg.javacg.stat.ClassVisitor;

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

      // Visit each class
      for (JavaClass javaClass : javaClasses) {
        System.err.println("1) " + javaClass.getClassName());
        // Methods
        for (Method method : javaClass.getMethods()) {
          ConstantPoolGen cpg = new ConstantPoolGen(javaClass.getConstantPool());
          MethodGen methodGen = new MethodGen(method, javaClass.getClassName(), cpg);
          LocalVariableTable symbolTable = methodGen.getMethod().getLocalVariableTable();
          System.err.println("2) " + javaClass.getClassName() + " :: " + method.getName() + "()");
          // main bit
          if (methodGen.getInstructionList() != null) {

            Stack<String> stack = new Stack<String>();
            for (InstructionHandle instructionHandle = methodGen.getInstructionList().getStart();
                instructionHandle != null;
                instructionHandle = instructionHandle.getNext()) {
              Instruction anInstruction = instructionHandle.getInstruction();

              if (anInstruction instanceof INVOKEVIRTUAL) {
                //              System.out.println(
                //                  "  3) INVOKEVIRTUAL\t"
                //                      + ((INVOKEVIRTUAL) anInstruction).getReferenceType(constantPoolGen)
                //                      + " :: "
                //                      + ((INVOKEVIRTUAL) anInstruction).getMethodName(constantPoolGen)
                //                      + "()");
                //              anInstruction.accept(this);
              } else if (anInstruction instanceof ConstantPushInstruction) {
                System.out.println(
                    "  (unhandled) "
                        + javaClass.getClassName()
                        + "::"
                        + method.getName()
                        + " ConstantPushInstruction = "
                        + ((ConstantPushInstruction) anInstruction).getValue());
              } else if (anInstruction instanceof ACONST_NULL) {
                System.out.println(
                    "  (unhandled) "
                        + javaClass.getClassName()
                        + "::"
                        + method.getName()
                        + " "
                        + anInstruction);
                //              anInstruction.accept(this);

              } else if (anInstruction instanceof DUP) {
                System.err.println(
                    "  (unhandled) "
                        + javaClass.getClassName()
                        + "::"
                        + method.getName()
                        + "() DUP (duplicate the value on top of the stack) "
                        + stack.peek());
                stack.push(stack.peek());
              } else if (anInstruction instanceof GETSTATIC) {
                System.out.println(
                    "  (unhandled) "
                        + javaClass.getClassName()
                        + "::"
                        + method.getName()
                        + "() GETSTATIC (get a static field value of a class, where the field is identified by field reference in the constant pool index): "
                        + ((GETSTATIC) anInstruction).getFieldName(cpg));
                //anInstruction.accept(this);
              } else if (anInstruction instanceof PUTSTATIC) {
                System.out.println(
                    "  (unhandled) "
                        + javaClass.getClassName()
                        + "::"
                        + method.getName()
                        + "() PUTSTATIC (set static field to value in a class, where the field is identified by a field reference index in constant pool): "
                        + ((PUTSTATIC) anInstruction).getFieldName(cpg));
              } else if (anInstruction instanceof NEW) {
                System.err.println(
                    "  (unhandled) "
                        + javaClass.getClassName()
                        + "::"
                        + method.getName()
                        + "() NEW (create new object of type identified by class reference in constant pool index) "
                        + anInstruction);
              } else if (anInstruction instanceof ARETURN) {
                System.err.println(
                    "  (unhandled) "
                        + javaClass.getClassName()
                        + "::"
                        + method.getName()
                        + "() ARETURN reference - "
                        + anInstruction);
                System.err.println(
                    "  (unhandled) "
                        + javaClass.getClassName()
                        + "::"
                        + method.getName()
                        + "() ARETURN reference - "
                        + ((ARETURN) anInstruction).toString(javaClass.getConstantPool()));
              } else if (anInstruction instanceof RETURN) {
                System.err.println(
                    "  (unhandled) "
                        + javaClass.getClassName()
                        + "::"
                        + method.getName()
                        + "() RETURN void - "
                        + anInstruction);
              } else if (anInstruction instanceof IFNONNULL) {
                System.out.println(
                    "  (unhandled) "
                        + javaClass.getClassName()
                        + "::"
                        + method.getName()
                        + "() "
                        + anInstruction);
                //anInstruction.accept(this);
              } else if (anInstruction instanceof GOTO) {

                System.out.println(
                    "  (unhandled) "
                        + javaClass.getClassName()
                        + "::"
                        + method.getName()
                        + " GOTO (goes to another instruction at branchoffset): "
                        + anInstruction);
              } else if (anInstruction instanceof ALOAD) {
                int variableIndex = ((ALOAD) anInstruction).getIndex();
                if (methodGen.getMethod().getLocalVariableTable() == null) {
                  System.err.println(
                      "  (unhandled) "
                          + javaClass.getClassName()
                          + "::"
                          + method.getName()
                          + " symbol table is null for "
                          + methodGen.getMethod());
                } else {
                  LocalVariable variable =
                      methodGen
                          .getMethod()
                          .getLocalVariableTable()
                          .getLocalVariable(variableIndex, 0);
                  String string = variable == null ? "null" : variable.getName();
                  stack.push(string);
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
                  System.out.println(
                      "Main.main() ASTORE (store a reference into a local variable #index): Declared and assigned variable:  "
                          + variableName);
                }
              } else if (anInstruction instanceof INVOKESTATIC) {
                System.out.println(
                    "  (unhandled) "
                        + javaClass.getClassName()
                        + "::"
                        + method.getName()
                        + "() "
                        + anInstruction
                        + " INVOKESTATIC (invoke a static method and puts the result on the stack (might be void); the method is identified by method reference index in constant pool): ");
              } else if (anInstruction instanceof INVOKESPECIAL) {
              } else if (anInstruction instanceof LDC) {
                stack.push(((LDC) anInstruction).getValue(cpg).toString());
              } else if (anInstruction instanceof PUTFIELD) {
                PUTFIELD p = (PUTFIELD) anInstruction;
                String fieldNameBeingAssigned = p.getName(methodGen.getConstantPool());
                while (!stack.isEmpty()) {
                  System.out.println(
                      "  3) " + fieldNameBeingAssigned + "\t--[depends on]--> " + stack.pop());
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
                    "Instruction to consider visiting: " + anInstruction.getClass());
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

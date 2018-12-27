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
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ACONST_NULL;
import org.apache.bcel.generic.ALOAD;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.ConstantPushInstruction;
import org.apache.bcel.generic.GETSTATIC;
import org.apache.bcel.generic.IFNONNULL;
import org.apache.bcel.generic.INVOKEINTERFACE;
import org.apache.bcel.generic.INVOKESPECIAL;
import org.apache.bcel.generic.INVOKESTATIC;
import org.apache.bcel.generic.INVOKEVIRTUAL;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.ObjectType;
import org.apache.bcel.generic.PUTFIELD;
import org.apache.bcel.generic.PUTSTATIC;
import org.apache.bcel.generic.RETURN;
import org.apache.bcel.generic.Type;

import gr.gousiosg.javacg.stat.ClassVisitor;
import gr.gousiosg.javacg.stat.MethodVisitor;

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
          System.err.println("2) " + javaClass.getClassName() + " :: " + method.getName());
          method.accept(new MyClassVisitor(javaClass) {});

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

  /**
   * This is the entry point to the magic / workhorse. Everything outside here is my algorithm. Most
   * of this layer is using BECL's visitor.
   *
   * <p>I hate the visitor pattern, it's so object-oriented. We keep its class life time as small as
   * possible
   */
  private static class MyClassVisitor extends ClassVisitor {

    private static class MyMethodVisitor extends MethodVisitor {
      private final ConstantPoolGen constantsPool;

      MyMethodVisitor(MethodGen methodGen, JavaClass javaClass) {
        super(methodGen, javaClass);
        this.constantsPool = methodGen.getConstantPool();

        visitMethod(methodGen, constantsPool);
        // We can't figure out the superclass method of the parent method because we don't know which
        // parent classes' method is overriden (there are several)
        // TODO: Wait, we can use the repository to get the java class.
      }

      private void visitMethod(MethodGen methodGen, ConstantPoolGen constantPoolGen) {
        // main bit
        if (methodGen.getInstructionList() != null) {

          Stack<String> stack = new Stack<String>();
          System.out.println("2) " + methodGen.getClassName() + " :: " + methodGen.getName());
          for (InstructionHandle instructionHandle = methodGen.getInstructionList().getStart();
              instructionHandle != null;
              instructionHandle = instructionHandle.getNext()) {
            Instruction anInstruction = instructionHandle.getInstruction();

            if (anInstruction instanceof INVOKEVIRTUAL) {
              System.out.println(
                  "  3) INVOKEVIRTUAL\t"
                      + ((INVOKEVIRTUAL) anInstruction).getReferenceType(constantPoolGen)
                      + " :: "
                      + ((INVOKEVIRTUAL) anInstruction).getMethodName(constantPoolGen)
                      + "()");
              anInstruction.accept(this);
            } else if (anInstruction instanceof ConstantPushInstruction) {
              System.out.println(
                  "(unhandled) Main.MyClassVisitor.MyMethodVisitor.visitMethod() ConstantPushInstruction = "
                      + ((ConstantPushInstruction) anInstruction).getValue());
            } else if (anInstruction instanceof ACONST_NULL) {
              System.out.println(
                  "(unhandled) Main.MyClassVisitor.MyMethodVisitor.visitMethod() " + anInstruction);
              //              anInstruction.accept(this);

            } else if (anInstruction instanceof PUTSTATIC) {
              System.out.println(
                  "(unhandled) Main.MyClassVisitor.MyMethodVisitor.visitMethod() " + anInstruction);
              //  anInstruction.accept(this);
            } else if (anInstruction instanceof IFNONNULL) {
              System.out.println(
                  "(unhandled) Main.MyClassVisitor.MyMethodVisitor.visitMethod() " + anInstruction);
              //anInstruction.accept(this);
            } else if (anInstruction instanceof GETSTATIC) {
              System.out.println(
                  "(unhandled) Main.MyClassVisitor.MyMethodVisitor.visitMethod() " + anInstruction);
              //anInstruction.accept(this);
            } else if (anInstruction instanceof ALOAD) {
              int variableIndex = ((ALOAD) anInstruction).getIndex();
              if (methodGen.getMethod().getLocalVariableTable() == null) {
                System.err.println(
                    "(unhandled) Main.MyClassVisitor.MyMethodVisitor.visitMethod() symbol table is null for "
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
            } else if (anInstruction instanceof INVOKESPECIAL) {
              // e.g. java.lang.Object :: <init>()
              System.err.println(
                  "  3) INVOKESPECIAL\t"
                      + ((INVOKESPECIAL) anInstruction).getReferenceType(constantsPool)
                      + " :: "
                      + ((INVOKESPECIAL) anInstruction).getMethodName(constantsPool)
                      + "()");
            } else if (anInstruction instanceof RETURN) {
              anInstruction.accept(this);
            } else if (anInstruction instanceof PUTFIELD) {
              PUTFIELD p = (PUTFIELD) anInstruction;
              String fieldNameBeingAssigned = p.getName(constantPoolGen);
              while (!stack.isEmpty()) {
                System.out.println("  3) " + fieldNameBeingAssigned + " depends on " + stack.pop());
              }
            } else {
              System.out.println(
                  "(unhandled) Main.MyClassVisitor.MyMethodVisitor.visitMethod() " + anInstruction);
              //              throw new RuntimeException(
              //                  "Instruction to consider visiting: " + anInstruction.getClass());
            }
          }
        }
      }

      //
      // We don't need these methods, just cast the parameter in the caller to the INVOKE* type we want to examine.
      //

      /** instance method */
      @Override
      public void visitINVOKEVIRTUAL(INVOKEVIRTUAL iInstruction) {}

      /** super method, private method, constructor */
      @Override
      public void visitINVOKESPECIAL(INVOKESPECIAL iInstruction) {}

      @Override
      public void visitINVOKEINTERFACE(INVOKEINTERFACE iInstruction) {}

      @Override
      public void visitINVOKESTATIC(INVOKESTATIC iInstruction) {}

      @Override
      public void visitRETURN(RETURN iInstruction) {
        System.err.println("  4) " + iInstruction.getType(constantsPool));
      }

      @Override
      public void start() {}
    }

    private JavaClass classToVisit;

    public MyClassVisitor(JavaClass classToVisit) {
      super(classToVisit);
      this.classToVisit = classToVisit;
      //System.out.println("Main.MyClassVisitor.enclosing_method()" + classToVisit);
    }

    @Override
    public void visitMethod(Method method) {
      String className = classToVisit.getClassName();
      ConstantPoolGen classConstants = new ConstantPoolGen(classToVisit.getConstantPool());
      MethodGen methodGen = new MethodGen(method, className, classConstants);
      System.err.println("2) " + className + " :: " + method.getName() + "()");
      new MyMethodVisitor(methodGen, classToVisit).start();
    }

    @Override
    public void visitField(Field field) {
      Type fieldType = field.getType();
      if (fieldType instanceof ObjectType) {}
    }
  }
}

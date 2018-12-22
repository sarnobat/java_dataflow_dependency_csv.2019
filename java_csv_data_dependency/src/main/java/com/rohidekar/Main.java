package com.rohidekar;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.Field;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.ConstantPushInstruction;
import org.apache.bcel.generic.INVOKEINTERFACE;
import org.apache.bcel.generic.INVOKESPECIAL;
import org.apache.bcel.generic.INVOKESTATIC;
import org.apache.bcel.generic.INVOKEVIRTUAL;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionConstants;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.ObjectType;
import org.apache.bcel.generic.ReturnInstruction;
import org.apache.bcel.generic.Type;
import org.apache.commons.collections.IteratorUtils;
import org.apache.commons.io.FileUtils;

import com.google.common.collect.Lists;

import gr.gousiosg.javacg.stat.ClassVisitor;
import gr.gousiosg.javacg.stat.MethodVisitor;

/**
 * 2018-12
 *
 * <p>usage: -Dexec.args="/Users/ssarnobat/trash/myproj/target"
 */
public class Main {
  private static final String[] substringsToIgnore = {
    "java", "Logger", ".toString", "Exception",
  };

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
        System.err.println("Main.main() class = " + javaClass.getClassName());
        // Methods
        for (Method method : javaClass.getMethods()) {
          System.err.println(
              "    Main.main() method = " + javaClass.getClassName() + " :: " + method.getName());
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
      for (String relationship : relationshipsCsvLines) {
        //System.out.println(relationship);
      }
    }

    System.err.println("Now use d3_helloworld_csv.git/singlefile_automated/ for visualization");
  }

  /** @param jarOrClassDir - jar or class directory */
  private static Collection<String> getJavaClassFilePathsFromResource(String jarOrClassDir) {
    //Map<String, JavaClass> javaClasses = new HashMap<String, JavaClass>();
    Collection<String> javaClasses = new LinkedList<String>();
    boolean isJar = jarOrClassDir.endsWith("jar");
    if (isJar) {
      throw new RuntimeException("See old version");
    } else {
      // Assume it's a directory
      String[] extensions = {"class"};
      Iterator<File> classesIter =
          FileUtils.iterateFiles(new File(jarOrClassDir), extensions, true);
      @SuppressWarnings("unchecked")
      Collection<File> files = IteratorUtils.toList(classesIter);
      for (File aClass : files) {
        String absolutePath = aClass.getAbsolutePath();
        javaClasses.add(absolutePath);
      }
    }
    return javaClasses;
  }

  /**
   * This is the entry point to the magic / workhorse. Everything outside here is my algorithm. Most
   * of this layer is using BECL's visitor.
   * 
   * I hate the visitor pattern, it's so object-oriented. We keep its class life time as small as possible
   */
  private static class MyClassVisitor extends ClassVisitor {

    private static class MyMethodVisitor extends MethodVisitor {
      private final JavaClass visitedClass;
      private final ConstantPoolGen constantsPool;
      private final String parentMethodQualifiedName;
  
      MyMethodVisitor(MethodGen methodGen, JavaClass javaClass) {
        super(methodGen, javaClass);
        this.visitedClass = javaClass;
        this.constantsPool = methodGen.getConstantPool();
        this.parentMethodQualifiedName =
            MyInstruction.getQualifiedMethodName(methodGen, visitedClass);
        // main bit
        if (methodGen.getInstructionList() != null) {
          for (InstructionHandle instructionHandle = methodGen.getInstructionList().getStart();
              instructionHandle != null;
              instructionHandle = instructionHandle.getNext()) {
            Instruction anInstruction = instructionHandle.getInstruction();
            //          System.out.println(
            //              "        Main.MyMethodVisitor.MyMethodVisitor() instruction = " + anInstruction);
            //          System.out.println(
            //              "        Main.MyMethodVisitor.MyMethodVisitor() instruction opcode = "
            //                  + anInstruction.getName());
            short opcode = anInstruction.getOpcode();
            if (opcode == 176) { // return
            } else if (opcode == 182) {
              // invoke virtual
              //            System.out.println(
              //                "          Main.MyMethodVisitor.MyMethodVisitor() virtual method "
              //                    + anInstruction.getName());
            } else {
              //pickup
            }
            if (!shouldVisitInstruction(anInstruction)) {
              anInstruction.accept(this);
            }
          }
        }
        // We can't figure out the superclass method of the parent method because we don't know which
        // parent classes' method is overriden (there are several)
        // TODO: Wait, we can use the repository to get the java class.
      }
  
      private static boolean shouldVisitInstruction(Instruction iInstruction) {
        return ((InstructionConstants.INSTRUCTIONS[iInstruction.getOpcode()] != null)
            && !(iInstruction instanceof ConstantPushInstruction)
            && !(iInstruction instanceof ReturnInstruction));
      }
  
      /** instance method */
      @Override
      public void visitINVOKEVIRTUAL(INVOKEVIRTUAL iInstruction) {
        //    	System.err.println("            Main.MyMethodVisitor.visitINVOKEVIRTUAL() instruction = " + iInstruction);
        //    	System.err.println("            Main.MyMethodVisitor.visitINVOKEVIRTUAL() reference type = " + iInstruction.getReferenceType(constantsPool));
        //    	System.err.println("            Main.MyMethodVisitor.visitINVOKEVIRTUAL() method name = " + iInstruction.getMethodName(constantsPool));
        System.err.println(
            "            Main.MyMethodVisitor.visitINVOKEVIRTUAL() reference type :: method name = "
                + iInstruction.getReferenceType(constantsPool)
                + " :: "
                + iInstruction.getMethodName(constantsPool)
                + "()");
  
        addMethodCallRelationship(
            iInstruction.getReferenceType(constantsPool),
            iInstruction.getMethodName(constantsPool),
            iInstruction,
            iInstruction.getArgumentTypes(constantsPool));
      }
  
      /** super method, private method, constructor */
      @Override
      public void visitINVOKESPECIAL(INVOKESPECIAL iInstruction) {
        addMethodCallRelationship(
            iInstruction.getReferenceType(constantsPool),
            iInstruction.getMethodName(constantsPool),
            iInstruction,
            iInstruction.getArgumentTypes(constantsPool));
      }
  
      @Override
      public void visitINVOKEINTERFACE(INVOKEINTERFACE iInstruction) {
        addMethodCallRelationship(
            iInstruction.getReferenceType(constantsPool),
            iInstruction.getMethodName(constantsPool),
            iInstruction,
            iInstruction.getArgumentTypes(constantsPool));
      }
  
      @Override
      public void visitINVOKESTATIC(INVOKESTATIC iInstruction) {
        addMethodCallRelationship(
            iInstruction.getReferenceType(constantsPool),
            iInstruction.getMethodName(constantsPool),
            iInstruction,
            iInstruction.getArgumentTypes(constantsPool));
      }
  
      private void addMethodCallRelationship(
          Type iClass,
          String unqualifiedMethodName,
          Instruction anInstruction,
          Type[] argumentTypes) {
        if (!(iClass instanceof ObjectType)) {
          return;
        }
        // method calls
        {
          ObjectType childClass = (ObjectType) iClass;
          MyInstruction target = new MyInstruction(childClass, unqualifiedMethodName);
          // link to superclass method - note: this will not work for the top-level
          // method (i.e.
          // parentMethodQualifiedName). Only for target.
          // We can't do it for the superclass without a JavaClass object. We don't
          // know which superclass
          // the method overrides.
          //        System.err.println(
          //            "        Main.MyMethodVisitor.addMethodCallRelationship() " + unqualifiedMethodName);
          linkMethodToSuperclassMethod(unqualifiedMethodName, target);
        }
        // class dependencies for method calls
      }
  
      private void linkMethodToSuperclassMethod(String unqualifiedMethodName, MyInstruction target)
          throws IllegalAccessError {}
  
      @Override
      public void start() {}
    }

  private JavaClass classToVisit;
    // Do we need to keep track of visited classes? Maybe cycles will cause problems.
    private Map<String, JavaClass> visitedClasses = new HashMap<String, JavaClass>();

    public MyClassVisitor(JavaClass classToVisit) {
      super(classToVisit);
      this.classToVisit = classToVisit;
    }

    public void setVisited(JavaClass javaClass) {
      this.visitedClasses.put(javaClass.getClassName(), javaClass);
    }

    public boolean isVisited(JavaClass javaClass) {
      return this.visitedClasses.values().contains(javaClass);
    }

    @Override
    public void visitJavaClass(JavaClass javaClass) {
      System.err.println("Main.MyClassVisitor.visitJavaClass() " + javaClass);
      if (this.isVisited(javaClass)) {
        return;
      }
      this.setVisited(javaClass);
      if (javaClass.getClassName().equals("java.lang.Object")) {
        return;
      }
      if (Ignorer.shouldIgnore(javaClass)) {
        return;
      }

      // Parent classes
      List<String> parentClasses = getInterfacesAndSuperClasses(javaClass);
      for (String anInterfaceName : parentClasses) {
        if (Ignorer.shouldIgnore(anInterfaceName)) {
          continue;
        }
      }
      // Methods
      for (Method method : javaClass.getMethods()) {
        method.accept(this);
      }
      // fields
      Field[] fs = javaClass.getFields();
      for (Field f : fs) {
        f.accept(this);
      }
      throw new RuntimeException("Does this ever get invoked?");
    }

    public static List<String> getInterfacesAndSuperClasses(JavaClass javaClass) {
      List<String> parentClasses =
          Lists.asList(javaClass.getSuperclassName(), javaClass.getInterfaceNames());
      return parentClasses;
    }

    @Override
    public void visitMethod(Method method) {
      //System.out.println("      Main.MyClassVisitor.visitMethod() method = " + method);
      String className = classToVisit.getClassName();
      ConstantPoolGen classConstants = new ConstantPoolGen(classToVisit.getConstantPool());
      MethodGen methodGen = new MethodGen(method, className, classConstants);
      System.out.println(
          "      Main.MyClassVisitor.visitMethod() className :: method = "
              + className
              + " :: "
              + method);
      new MyMethodVisitor(methodGen, classToVisit).start();
    }

    @Override
    public void visitField(Field field) {
      Type fieldType = field.getType();
      if (fieldType instanceof ObjectType) {
        ObjectType objectType = (ObjectType) fieldType;
      }
    }
  }

  private static class Ignorer {

    public static boolean shouldIgnore(JavaClass iClass) {
      return shouldIgnore(iClass.getClassName());
    }

    public static boolean shouldIgnore(String classFullName) {
      for (String substringToIgnore : Main.substringsToIgnore) {
        if (classFullName.contains(substringToIgnore)) {
          return true;
        }
      }
      //      System.err.println(classFullName + " was not ignored");
      return false;
    }
  }

  private static class MyInstruction {

    private String _qualifiedMethodName;

    public MyInstruction(ObjectType iClass, String unqualifiedMethodName) {
      this(iClass.getClassName(), unqualifiedMethodName);
    }

    public MyInstruction(String classNameQualified, String unqualifiedMethodName) {
      String qualifiedMethodName =
          getQualifiedMethodName(classNameQualified, unqualifiedMethodName);
      this._qualifiedMethodName = qualifiedMethodName;
      if (qualifiedMethodName.equals(
          "com.rohidekar.callgraph.GraphNodeInstruction.getMethodNameQualified()")) {
        throw new IllegalAccessError("MyInstruction");
      }
    }

    public static String getQualifiedMethodName(MethodGen methodGen, JavaClass visitedClass) {
      return getQualifiedMethodName(visitedClass.getClassName(), methodGen.getName());
    }

    public static String getQualifiedMethodName(String className, String methodName) {
      return className + "." + methodName + "()";
    }

    public String getMethodNameQualified() {
      return this._qualifiedMethodName;
    }

    @Override
    public boolean equals(Object that) {
      return this.getMethodNameQualified().equals(((MyInstruction) that).getMethodNameQualified());
    }

    @Override
    public int hashCode() {
      return this.getMethodNameQualified().hashCode();
    }

    @Override
    public String toString() {
      return this._qualifiedMethodName;
    }
  }
}

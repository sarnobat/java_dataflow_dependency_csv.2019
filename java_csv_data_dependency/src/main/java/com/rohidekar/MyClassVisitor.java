package com.rohidekar;

import com.google.common.collect.Lists;

import org.apache.bcel.Repository;
import org.apache.bcel.classfile.Field;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.ObjectType;
import org.apache.bcel.generic.Type;

import gr.gousiosg.javacg.stat.ClassVisitor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

class MyClassVisitor extends ClassVisitor {

  private JavaClass classToVisit;

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
  }

  private static class Ignorer {

    public static boolean shouldIgnore(JavaClass iClass) {
      return shouldIgnore(iClass.getClassName());
    }

    private static final String[] substringsToIgnore = {
      "java", "Logger", ".toString", "Exception",
    };
    public static boolean shouldIgnore(String classFullName) {
      for (String substringToIgnore : substringsToIgnore) {
        if (classFullName.contains(substringToIgnore)) {
          return true;
        }
      }
      System.err.println(classFullName + " was not ignored");
      return false;
    }
  }

  public static List<String> getInterfacesAndSuperClasses(JavaClass javaClass) {
    List<String> parentClasses =
        Lists.asList(javaClass.getSuperclassName(), javaClass.getInterfaceNames());
    return parentClasses;
  }

  @Override
  public void visitMethod(Method method) {
    String className = classToVisit.getClassName();
    ConstantPoolGen classConstants = new ConstantPoolGen(classToVisit.getConstantPool());
    MethodGen methodGen = new MethodGen(method, className, classConstants);
    //new MyMethodVisitor(methodGen, classToVisit, relationships).start();
  }

  @Override
  public void visitField(Field field) {
    Type fieldType = field.getType();
    if (fieldType instanceof ObjectType) {
      ObjectType objectType = (ObjectType) fieldType;
      addContainmentRelationship(this.classToVisit, objectType.getClassName(), true);
    }
  }

  public static void addContainmentRelationship(
      JavaClass classToVisit, String childClassNameQualified, boolean allowDeferral) {
    if (Ignorer.shouldIgnore(childClassNameQualified)) {
      return;
    }
    JavaClass jc = null;
    try {
      jc = Repository.lookupClass(childClassNameQualified);
    } catch (ClassNotFoundException e) {

      System.err.println(e);
      if (allowDeferral) {
      } else {
      }
    }
    if (jc == null) {
      System.err.println("WARN: Couldn't find " + childClassNameQualified);
    } else {
    }
  }
}

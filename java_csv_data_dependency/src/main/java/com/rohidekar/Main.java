package com.rohidekar;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.*;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.bcel.classfile.ClassFormatException;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;
import org.apache.commons.collections.IteratorUtils;
import org.apache.commons.io.FileUtils;

/**
 * 2018-12
 *
 * <p>usage: -Dexec.args="/Users/ssarnobat/trash/myproj/target"
 */
public class Main {
  public static void main(String[] args) {

    Collection<String> classFilePaths = new LinkedList<String>();
    {
      if (args == null || args.length < 1) {
        // It's better to use STDIN for class files rather than class dirs so that
        // we can add more selectively (including from a txt file)
        {
          BufferedReader br = null;
          try {
            br = new BufferedReader(new InputStreamReader(System.in));
            String line;
            while ((line = br.readLine()) != null) {
              // log message
              System.err.println("[DEBUG] current line is: " + line);
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

    Collection<String> relationships = classFilePathsToRelationshipCsvLines(classFilePaths);
    {
      for (String relationship : relationships) {
        System.out.println(relationship);
      }
    }

    System.err.println("Now use d3_helloworld_csv.git/singlefile_automated/ for visualization");
  }

  private static Collection<String> classFilePathsToRelationshipCsvLines(
      Collection<String> classFilePaths) {

    return null;
  }

  private static Map<String, JavaClass> getJavaClassesFromResource(String resource) {
    Map<String, JavaClass> javaClasses = new HashMap<String, JavaClass>();
    boolean isJar = resource.endsWith("jar");
    if (isJar) {
      String zipFile = null;
      zipFile = resource;
      File jarFile = new File(resource);
      if (!jarFile.exists()) {
        System.out.println(
            "JavaClassGenerator.getJavaClassesFromResource(): WARN: Jar file "
                + resource
                + " does not exist");
      }
      Collection<JarEntry> entries = null;
      try {
        entries = Collections.list(new JarFile(jarFile).entries());
      } catch (IOException e) {
        System.err.println("JavaClassGenerator.getJavaClassesFromResource() - " + e);
      }
      if (entries == null) {
        System.err.println("JavaClassGenerator.getJavaClassesFromResource() - No entry");
        return javaClasses;
      }
      for (JarEntry entry : entries) {
        if (entry.isDirectory()) {
          continue;
        }
        if (!entry.getName().endsWith(".class")) {
          continue;
        }
        ClassParser classParser = isJar ? new ClassParser(zipFile, entry.getName()) : null;
        if (classParser == null) {
          System.err.println("JavaClassGenerator.getJavaClassesFromResource() - No class parser");
          continue;
        }
        try {
          JavaClass jc = classParser.parse();
          javaClasses.put(jc.getClassName(), jc);
        } catch (ClassFormatException e) {
          e.printStackTrace();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    } else {
      // Assume it's a directory
      String[] extensions = {"class"};
      Iterator<File> classesIter = FileUtils.iterateFiles(new File(resource), extensions, true);
      @SuppressWarnings("unchecked")
      Collection<File> files = IteratorUtils.toList(classesIter);
      for (File aClass : files) {
        try {
          ClassParser classParser = new ClassParser(checkNotNull(aClass.getAbsolutePath()));
          JavaClass jc = checkNotNull(checkNotNull(classParser).parse());
          javaClasses.put(jc.getClassName(), jc);
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
    return javaClasses;
  }
}

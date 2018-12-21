package com.rohidekar;

import java.io.*;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

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
}

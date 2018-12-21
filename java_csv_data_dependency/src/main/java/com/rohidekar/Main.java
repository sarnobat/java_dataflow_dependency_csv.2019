package com.rohidekar;

import java.io.*;

/** @since 2018-12 usage: -Dexec.args="/Users/ssarnobat/trash/myproj/target" */
public class Main {
  public static void main(String[] args) {

    if (args == null || args.length < 1) {
      BufferedReader br = null;
      try {
        br = new BufferedReader(new InputStreamReader(System.in));
        String line;
        while ((line = br.readLine()) != null) {
          // log message
          System.err.println("[DEBUG] current line is: " + line);
          // program output
          System.out.println(line);
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
    } else {
      String resource = args[0];
    }

    System.err.println("Now use d3_helloworld_csv.git/singlefile_automated/ for visualization");
  }
}

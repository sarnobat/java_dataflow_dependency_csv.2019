package com.rohidekar;

/**
 * This class is just to help me figure out what instruction sequences denote what high level
 * construct
 */
public class Simple {

  public static void main(String[] args) {
    int d = 2222;//voidMethod(11111111);
    int f = voidMethod(d);
//    int g = voidMethod(f);
//    System.out.println(g);
  }

  private static int voidMethod(int param1) {
	  return param1;
  }
}

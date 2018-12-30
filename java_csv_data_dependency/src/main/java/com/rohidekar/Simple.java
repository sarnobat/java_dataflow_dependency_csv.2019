package com.rohidekar;

/**
 * This class is just to help me figure out what instruction sequences denote what high level
 * construct
 */
public class Simple {

  public static void main(String[] args) {
    int d = voidMethod(11111111, 9999999);
    int f = voidMethod(d, 2222);
    int g = voidMethod(f, 333);
    System.out.println(f);
  }

  private static int voidMethod(int param1, int param2) {
	  System.out.println(param1);
	  return param1;
  }
}

package com.rohidekar;

/**
 * This class is just to help me figure out what instruction sequences denote what high level
 * construct
 */
public class Simple {

  public static void main(String[] args) {
    voidMethod(11111111, 9999999);
  }

  private static void voidMethod(int param1, int param2) {
	  System.out.println("Simple.voidMethod() " + param1);
  }
}

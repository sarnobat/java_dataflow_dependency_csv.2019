package com.rohidekar;

/**
 * This class is just to help me figure out what instruction sequences denote what high level
 * construct
 */
public class Simple {

  public static void main(String[] args) {
    voidMethod("foo", "bar");
  }

  private static void voidMethod(String param1, String param2) {
	  System.out.println("Simple.voidMethod() " + param1);
  }
}

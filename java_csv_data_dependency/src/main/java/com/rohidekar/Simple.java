package com.rohidekar;

/**
 * This class is just to help me figure out what instruction sequences denote what high level
 * construct
 */
public class Simple {

  public static void main(String[] args) {
    int myLocalVariable3 = 2222;//voidMethod(11111111);
    int myMethodCallReturnValue = helperMethod(myLocalVariable3);
    int g = helperMethod(myLocalVariable3);
    String myString1 = "foo";
    String myString2 = "bar";
    String mystring3 = myString1 + myString2;
    String mystring4 = new Person().fullName;
//    System.out.println(myLocalVariable3);
  }

  private static int helperMethod(int myParam1) {
	  return myParam1;
  }
  
  private static class Person {
      String fullName;
      Person() {
          String aName = "John Smith";
          fullName = aName;
      }
  }
}

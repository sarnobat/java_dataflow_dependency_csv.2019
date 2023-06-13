package com.rohidekar.analyzed;

/**
 * This class is just to help me figure out what instruction sequences denote what high level
 * construct
 */
public class Simple {

  public static void main(String[] args) {
//    int myLocalVariable3 = 2222;//voidMethod(11111111);
//    int myMethodCallReturnValue = helperMethod(myLocalVariable3);
//    int g = helperMethod(myLocalVariable3);
//    String myString1 = "foo";
//    String myString2 = "bar";
//    String mystring3 = myString1 + myString2;
    Person thePerson = new Person(4444);
//      new Person();
    int myInt = thePerson.age;
//    System.out.println(myInt);
  }

//  private static int helperMethod(int myParam1) {
//	  return myParam1;
//  }
  
  private static class Person {
  //  String fullName;
    public int age;
    Person(int iAge) {
//        String aName = "John Smith";
//        System.out.println("Simple.Person.Person()");
//        int age1 = 11;
//        this.age = 333333;
        this.age = iAge;
//        fullName = aName;
    }
  }
}

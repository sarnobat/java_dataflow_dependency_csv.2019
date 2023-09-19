package com.rohidekar;

/*
 * leetcode.com
Given an integer x, return true if x is a 
palindrome
, and false otherwise.
*/
public class Solution {

    public static void main(String[] args) {
        boolean palindrome = isPalindrome(1001);
        System.out.println("Solution.main() " + palindrome);
    }

    public static boolean isPalindrome(int x) {
        System.err.println("Solution.isPalindrome() x " + x);
        if (x < 0) {
            return false;
        }
        if (x < 10) {
            return true;
        }
        int xTemp = x;
        int digitCount = 1;
        while (xTemp > 10) {
            xTemp = xTemp / 10;
            digitCount++;
        }
        System.out.println("Solution.isPalindrome() digitCount = " + digitCount);
        int powered = (int) Math.pow(10, digitCount - 1);
        int numberAfterRemovingMSD = x % powered;
        System.out.println("Solution.isPalindrome() numberAfterRemovingMSD = " + numberAfterRemovingMSD);
        System.out.println("Solution.isPalindrome() powered = " + powered);
        int msd = x / powered;

        int numberAfterRemovingLSD = x / 10;
        int lsd = x % 10;

        int remainingNumber = numberAfterRemovingMSD / 10;
        System.out.println("Solution.isPalindrome() msd = " + msd);
        System.out.println("Solution.isPalindrome() lsd = " + lsd);
        System.out.println("Solution.isPalindrome() remaining number = " + remainingNumber);
        return msd == lsd && isPalindrome(remainingNumber);
    }
}
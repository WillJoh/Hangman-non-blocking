/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hangman.client.view;

/**
 *
 * @author William Joahnsson
 */
public class ThreadSafeOutput {
    synchronized void print(String output) {
        System.out.print(output);
    }
    
    synchronized void println(String output) {
        System.out.println(output);
    }
}

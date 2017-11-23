/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hangman.common;

/**
 *
 * @author William Joahnsson
 */
public enum MsgHeaders {
    //Message from client to server to start a new game
    START_GAME,
    //Message from client to server to guess one character
    GUESS_CHAR,
    //Message from client to server to guess the full word
    GUESS_WORD,
    /*
    Message from server to client to display the current game state and nof 
    remaing attempts
    */
    DISPLAY_PROGRESS,
    //Client disconnects
    DISCONNECT,
    //Client connects
    CONNECT
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hangman.server.controller;

import hangman.server.model.GameState;

/**
 *
 * @author William Joahnsson
 */
public class Controller {
    private final GameState gameState = new GameState();
    
    public void guessWord(String guess) {
        gameState.guessWord(guess);
    }
    
    public void guessChar(char guess) {
        gameState.guessChar(guess);
    }
    
    public void startNewGame() {
        gameState.startNewGame();
    }
    
    public String getCurrentState() {
        return gameState.getCurrentState();
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hangman.server.model;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 *
 * @author William Joahnsson
 */
public class GameState {
    private String currWord;
    private boolean[] currState;
    private boolean won = false;
    private boolean lost = false;
    private int score;
    private int remainingGuesses;
    List<String> words = new ArrayList<>();
    Random random  = new Random();
    
    public GameState() {
        score = 0;
        initWordList();
        startNewGame();
    }
    
    public void initWordList() {
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader("Assets/words.txt"));
            String line;
            while((line = reader.readLine()) != null) {
                words.add(line);
            }
        } catch (FileNotFoundException fnfe) {
            
        } catch (IOException ioe) {
            
        }
        
    }
    
    public void startNewGame() {
        won = false;
        lost = false;
        currWord = words.get(random.nextInt(words.size()));
        currState = new boolean[currWord.length()];
        remainingGuesses = currState.length;
        for(int i = 0; i < currState.length; i++) {
            currState[i] = false;
        }
    }
    
    public String getCurrentState() {
        String result = "Progress:";
        for(int i = 0; i < currState.length; i++) {
            if(currState[i]) {
                result += " " + currWord.charAt(i);
            } else {
                result += " _";
            }
        }
        
        result += "; Remaining guesses: " + remainingGuesses + "; Score: " + score;
        if(won || lost) {
            result += "; type \"start game\" to start a new game"; 
        } else {
            result += "; type your next guess";
        }
        return result;
    }
    
    public void guessWord(String guess) {
        if(!won && !lost) {
            if (guess.toLowerCase().equals(currWord.toLowerCase())) {
                won = true;
                score++;
                for(int i = 0; i < currState.length; i++) {
                    currState[i] = true;
                }
            } else {
                remainingGuesses--;
                if (remainingGuesses < 1) {
                    lost = true;
                    score--;
                }
            }
        }
    }
    
    public void guessChar(char guess) {
        guess = Character.toLowerCase(guess);
        if(!won && !lost) {
            won = true;
            for(int i = 0; i < currState.length; i++) {
                if (guess == currWord.charAt(i)) {
                    currState[i] = true;
                } else if(!currState[i]) {
                    won = false;
                }
            }
            
            if(won) {
                score++;
            } else {
                remainingGuesses--;
                if (remainingGuesses < 1) {
                    lost = true;
                    score--;
                }
            }
        }
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hangman.client.controller;

import hangman.client.net.Connection;
import java.io.IOException;
import java.io.UncheckedIOException;
import hangman.client.net.ComunicationHandler;

/**
 *
 * @author William Joahnsson
 */
public class Controller {
    private final Connection connection;
    
    public Controller() {
        connection = new Connection();
    }
    
    public void connect(String hostAddress, int portNr, ComunicationHandler outputHandler) {
        connection.connect(hostAddress, portNr, outputHandler);
        outputHandler.handleMsg("Successfully connected to " + hostAddress + ":" + portNr);
    }
    
    public void disconnect() {
        try {
            connection.disconnect();
        } catch (IOException ioe) {
            throw new UncheckedIOException(ioe);
        }
    }
    
    public void sendStartGame() {
        connection.sendStartNewGame();
    }
    
    public void sendWord(String guess) {
        connection.sendWord(guess);
    }
    
    public void sendChar(char guess) {
        connection.sendChar(guess);
    }
}

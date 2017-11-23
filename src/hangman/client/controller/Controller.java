/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hangman.client.controller;

import hangman.client.net.Connection;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.concurrent.CompletableFuture;
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
        CompletableFuture.runAsync(() -> {
            connection.connect(hostAddress, portNr, outputHandler);
        }).thenRun(() -> outputHandler.handleMsg("Successfully connected to " + 
                hostAddress + ":" + portNr));
    }
    
    public void disconnect() {
        try {
            connection.disconnect();
        } catch (IOException ioe) {
            throw new UncheckedIOException(ioe);
        }
    }
    
    public void sendStartGame() {
        CompletableFuture.runAsync(() -> connection.sendStartNewGame());
    }
    
    public void sendWord(String guess) {
        CompletableFuture.runAsync(() -> connection.sendWord(guess));
    }
    
    public void sendChar(char guess) {
        CompletableFuture.runAsync(() -> connection.sendChar(guess));
    }
}

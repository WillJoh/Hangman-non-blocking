/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hangman.server.net;

import hangman.common.Constants;
import hangman.common.MsgHeaders;
import hangman.server.controller.Controller;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.net.Socket;

/**
 *
 * @author William Joahnsson
 */
public class ClientHandler implements Runnable {
    private final Server server;
    private final Socket client;
    private BufferedReader fromClient;
    private PrintWriter toClient;
    private Controller controller = new Controller();
    
    private boolean connected;
    
    public ClientHandler(Server server, Socket client) {
        this.server = server;
        this.client = client;
        connected = true;
    }
    @Override
    public void run() {
        try {
            fromClient = new BufferedReader(new InputStreamReader(client.getInputStream()));
            toClient = new PrintWriter(client.getOutputStream(), true);
            toClient.println(controller.getCurrentState()); 
        } catch (IOException ioe) {
            throw new UncheckedIOException(ioe);
        }

        while(connected) {
            try {
                Command cmd = new Command(fromClient.readLine());
                
                switch (cmd.getHeader()) {
                    case START_GAME:
                        controller.startNewGame();
                        toClient.println(controller.getCurrentState());                        
                        break;
                    case GUESS_WORD: 
                        controller.guessWord(cmd.getBody());
                        toClient.println(controller.getCurrentState());      
                        break;
                    case GUESS_CHAR:
                        controller.guessChar(cmd.getBody().charAt(0));
                        toClient.println(controller.getCurrentState());      
                        break;
                    case DISCONNECT:
                        disconnectClient();
                        break;         
                }
                        
            } catch (IOException ioe) {
                disconnectClient();
                throw new RuntimeException(ioe);
            }
        }
    }
    
    private void disconnectClient() {
        try {
            client.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        connected = false;
        server.removeHandler(this);
    }
    
    private static class Command {
        private MsgHeaders header;
        private String body;
        private String command;
        
        public Command(String command) {
            this.command = command;
            extractHeaderAndBody(command);
        }
        
        private void extractHeaderAndBody(String command){
            String header = command.split(Constants.MSG_DELIMITER)[0];
            if (header.equals(MsgHeaders.START_GAME.toString())) {
                this.header = MsgHeaders.START_GAME;
            } else if (header.equals(MsgHeaders.DISCONNECT.toString())) {
                this.header = MsgHeaders.DISCONNECT;
            } else if (header.equals(MsgHeaders.GUESS_WORD.toString())) {
                this.header = MsgHeaders.GUESS_WORD;
                this.body = command.split(Constants.MSG_DELIMITER)[1];
            } else if (header.equals(MsgHeaders.GUESS_CHAR.toString())) {
                this.header = MsgHeaders.GUESS_CHAR;
                this.body = command.split(Constants.MSG_DELIMITER)[1];
            } 
        }
        
        public MsgHeaders getHeader() {
            return header;
        }
        
        public String getBody() {
            return body;
        }
    }
}

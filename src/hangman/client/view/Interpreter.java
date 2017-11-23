/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hangman.client.view;

import hangman.client.controller.Controller;
import java.net.InetSocketAddress;
import java.util.Scanner;
import hangman.client.net.ComunicationHandler;

/**
 *
 * @author William Joahnsson
 */
public class Interpreter implements Runnable{
    private final Scanner scanner;
    private boolean receiving;
    private Controller controller;
    private final ThreadSafeOutput output;
    
    public Interpreter() {
        scanner = new Scanner(System.in);
        receiving = false;
        output = new ThreadSafeOutput();
    }
    
    public void init() {
        if (receiving) {
            return;
        }
        receiving = true;
        controller = new Controller();
        new Thread(this).start();
    }
    
    @Override
    public void run() {
        
        while(receiving) {
            try {
                Command command = new Command(scanner.nextLine());
                switch (command.getHeader()) {
                    case START_GAME:
                        controller.sendStartGame();
                        break;
                    case GUESS_WORD:
                        controller.sendWord(command.getBody());
                        break;
                    case GUESS_CHAR:
                        controller.sendChar(command.getBody().charAt(0));
                        break;
                    case DISCONNECT:
                        controller.disconnect();
                        break;
                    case CONNECT:
                        String[] temp = command.getBody().split(" ");
                        controller.connect(temp[1], 
                                Integer.parseInt(temp[2]), 
                                new ConsoleOutput());
                        break;
                }
                
            } catch (Exception e) {
                output.println("Not a valid command");
            }
        }
    }
    
    private class ConsoleOutput implements ComunicationHandler {
        @Override
        public void handleMsg(String msg) {
            output.println(msg);
        }
        
        
        @Override
        public void connected(InetSocketAddress serverAddress) {
            output.println("Connected to " + serverAddress.getHostName() + ":"
                           + serverAddress.getPort());
        }

        @Override
        public void disconnected() {
            output.println("Disconnected from server.");
        }
    }
}

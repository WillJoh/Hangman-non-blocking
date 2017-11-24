/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hangman.client.view;

import hangman.common.CommandHeaders;

/**
 *
 * @author William Joahnsson
 */
public class Command {
    private String command;
    private CommandHeaders header;
    private String body;
    public Command(String command) {
        this.command = command.toLowerCase();
        extractHeaderAndBody(command);
    }
    
    private void extractHeaderAndBody(String command) {
        if (command.equals("start game")) {
            header = CommandHeaders.START_GAME;
            body = "";
        } else if (command.equals("disconnect")) {
            header = CommandHeaders.DISCONNECT;
            body = "";
        } else if (command.split(" ")[0].equals("connect")) {
            header = CommandHeaders.CONNECT;
            body = command;
        } else if (command.length() == 1) {
            header = CommandHeaders.GUESS_CHAR;
            body = command;
        } else if (!command.contains(" ")) {
            header = CommandHeaders.GUESS_WORD;
            body = command;
        }
    }
    
    public CommandHeaders getHeader() {
        return header;
    }
    
    public String getBody() {
        return body;
    }
}

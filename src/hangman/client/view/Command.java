/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hangman.client.view;

import hangman.common.Constants;
import hangman.common.MsgHeaders;

/**
 *
 * @author William Joahnsson
 */
public class Command {
    private String command;
    private MsgHeaders header;
    private String body;
    public Command(String command) {
        this.command = command.toLowerCase();
        extractHeaderAndBody(command);
    }
    
    private void extractHeaderAndBody(String command) {
        if (command.equals("start game")) {
            header = MsgHeaders.START_GAME;
            body = "";
        } else if (command.equals("disconnect")) {
            header = MsgHeaders.DISCONNECT;
            body = "";
        } else if (command.split(" ")[0].equals("connect")) {
            header = MsgHeaders.CONNECT;
            body = command;
        } else if (command.length() == 1) {
            header = MsgHeaders.GUESS_CHAR;
            body = command;
        } else if (!command.contains(" ")) {
            header = MsgHeaders.GUESS_WORD;
            body = command;
        }
    }
    
    public MsgHeaders getHeader() {
        return header;
    }
    
    public String getBody() {
        return body;
    }
}

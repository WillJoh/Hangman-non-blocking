/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hangman.common;

import java.util.ArrayDeque;
import java.util.Queue;

/**
 *
 * @author William Joahnsson
 */
public class CommandHandler {
    private StringBuilder commandBuilder = new StringBuilder();
    private final Queue<String> commands = new ArrayDeque<String>();
    
    public synchronized void appendNewString(String newString) {
        commandBuilder.append(newString);
        extractMessage();
    }
    
    public synchronized String nextCmd() {
        return commands.poll();
    }
    
    public synchronized boolean hasNext() {
        return !commands.isEmpty();
    }
    
    public static String prependLengthHeader(String command) {
        return Integer.toString(command.length()) + Constants.CMD_LENGTH_DELIMITER + command;
    }
    
    private void extractMessage() {
        String[] messageSplit = commandBuilder.toString().split(Constants.CMD_LENGTH_DELIMITER);
        int commandLength;
        if(messageSplit.length < 2) {
            return;
        }
        commandLength = Integer.parseInt(messageSplit[0]);
        if(commandBuilder.length() >= commandLength) {
            commands.add(messageSplit[1].substring(0, commandLength));
            commandBuilder.delete(0, messageSplit[0].length() + Constants.CMD_LENGTH_DELIMITER.length() + commandLength);
        }
    }
}

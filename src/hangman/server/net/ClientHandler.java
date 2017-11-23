/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hangman.server.net;

import hangman.common.CommandHandler;
import hangman.common.Constants;
import hangman.common.CommandHeaders;
import hangman.server.controller.Controller;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.ForkJoinPool;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author William Joahnsson
 */
public class ClientHandler implements Runnable {
    private final Server server;
    private final SocketChannel client;
    private final CommandHandler cmdHandler = new CommandHandler();
    private final ByteBuffer cmdFromClient = ByteBuffer.allocateDirect(16384);
    public final Queue<ByteBuffer> cmdToSend = new ArrayDeque<ByteBuffer>();
    private Controller controller = new Controller();
    
    public ClientHandler(Server server, SocketChannel client) {
        this.server = server;
        this.client = client;
        controller.startNewGame();
        try {
            sendStatusToClient();
        } catch (IOException ex) {
            Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    @Override
    public void run() {
        while(cmdHandler.hasNext()) {
            try {
                Command cmd = new Command(cmdHandler.nextCmd());
                
                switch (cmd.getHeader()) {
                    case START_GAME:
                        controller.startNewGame(); 
                        sendStatusToClient();
                        break;
                    case GUESS_WORD:
                        controller.guessWord(cmd.getBody());
                        sendStatusToClient();
                        break;
                    case GUESS_CHAR:
                        controller.guessChar(cmd.getBody().charAt(0));
                        sendStatusToClient();
                        break;
                    case DISCONNECT:
                        disconnectClient();         
                        break;
                }
            } catch (IOException ex) {
                Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public void sendStatusToClient() throws IOException{
        String cmd = controller.getCurrentState();
        cmd = Integer.toString(cmd.length()) + Constants.CMD_LENGTH_DELIMITER + cmd;
        
        ByteBuffer completeCmd = ByteBuffer.wrap(cmd.getBytes());
        queueCmd(completeCmd);
    }
    
    public void sendCmd(ByteBuffer cmd) throws IOException {
        client.write(cmd);
        if (cmd.hasRemaining()) {
            throw new RuntimeException("CommandBuffer not empty, can't send");
        }
    }
    
    private void queueCmd(ByteBuffer cmd) {
            cmdToSend.add(cmd);
        }
    
    public void reciveCmd() throws IOException {
        cmdFromClient.clear();
        if(client.read(cmdFromClient) == -1) {
            throw new IOException("Connection closed by client");
        }
        cmdHandler.appendNewString(getCmdFromBuffer());
        ForkJoinPool.commonPool().execute(this);
    }
    
    private String getCmdFromBuffer() {
        cmdFromClient.flip();
        byte[] cmd = new byte[cmdFromClient.remaining()];
        cmdFromClient.get(cmd);
        return new String(cmd);
    }
    
    public void disconnectClient() {
        try {
            client.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
    
    private static class Command {
        private CommandHeaders header;
        private String body;
        private String command;
        
        public Command(String command) {
            this.command = command;
            extractHeaderAndBody(command);
        }
        
        private void extractHeaderAndBody(String command){
            String header = command.split(Constants.CMD_DELIMITER)[0];
            if (header.equals(CommandHeaders.START_GAME.toString())) {
                this.header = CommandHeaders.START_GAME;
            } else if (header.equals(CommandHeaders.DISCONNECT.toString())) {
                this.header = CommandHeaders.DISCONNECT;
            } else if (header.equals(CommandHeaders.GUESS_WORD.toString())) {
                this.header = CommandHeaders.GUESS_WORD;
                this.body = command.split(Constants.CMD_DELIMITER)[1];
            } else if (header.equals(CommandHeaders.GUESS_CHAR.toString())) {
                this.header = CommandHeaders.GUESS_CHAR;
                this.body = command.split(Constants.CMD_DELIMITER)[1];
            } 
        }
        
        public CommandHeaders getHeader() {
            return header;
        }
        
        public String getBody() {
            return body;
        }
    }
}

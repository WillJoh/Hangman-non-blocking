/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hangman.client.net;
    
import hangman.common.Constants;
import hangman.common.MsgHeaders;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 *
 * @author William Joahnsson
 */
public class Connection {
    private Socket socket;
    private static final int MESSAGE_TIMEOUT = 60000;
    private static final int SESSION_TIMEOUT = 2000000;
    private PrintWriter toServer;
    private BufferedReader fromServer;
    private Boolean connected;
            
    public void connect(String hostAddress, int portNr, OutputHandler outputHandler) throws IOException {
        socket = new Socket();
        socket.connect(new InetSocketAddress(hostAddress, portNr), MESSAGE_TIMEOUT);
        socket.setSoTimeout(SESSION_TIMEOUT);
        connected = true;
        
        toServer = new PrintWriter(socket.getOutputStream(), true);
        fromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        
        new Thread(new Listener(outputHandler)).start();
    }
    
    public void disconnect() throws IOException {
        sendMsg(MsgHeaders.DISCONNECT.toString(), "");
        socket.close();
        socket = null;
        connected = false;
    }
    
    public void sendStartNewGame() {
        sendMsg(MsgHeaders.START_GAME.toString(), "");
    }
    
    public void sendChar(char guess) {
        sendMsg(MsgHeaders.GUESS_CHAR.toString(), String.valueOf(guess));
    }
    
    public void sendWord(String guess) {
        sendMsg(MsgHeaders.GUESS_WORD.toString(), guess);
    }
    
    public void sendMsg(String header, String body) {
        toServer.println(header + Constants.MSG_DELIMITER + body);
    }
    
    private class Listener implements Runnable {
        private final OutputHandler outputHandler;
        
        private Listener(OutputHandler outputHandler) {
            this.outputHandler = outputHandler;
        }
        @Override
        public void run() {
            try {
                while(true) {
                    outputHandler.handleMsg(fromServer.readLine());
                }
            } catch (Throwable connectionFailure) {
                if(connected) {
                    outputHandler.handleMsg("Connection lost");
                }
            }
        }
    }
}

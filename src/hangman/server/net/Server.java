/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hangman.server.net;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author William Joahnsson
 */
public class Server {
    private int portNr = 5555;
    private final List<ClientHandler> clients = new ArrayList<ClientHandler>();
    
    public static void main(String[] args) {
       Server server = new Server();
       server.setPortNr(args);
       server.run();
    }
    
    private void run() {
        try {
            
            ServerSocket listeningSocket = new ServerSocket(portNr);
            while(true) { 
                startHandler(listeningSocket.accept());
            }
        } catch (IOException ioe) {
            System.err.println("Server failing");
        }
    }
    
    private void startHandler(Socket client) throws SocketException{
        client.setSoLinger(true, 10000);
        client.setSoTimeout(2000000);
        ClientHandler handler = new ClientHandler(this, client);
        clients.add(handler);
        Thread handlerThread = new Thread(handler);
        handlerThread.setPriority(Thread.MAX_PRIORITY);
        handlerThread.start();
    }
    
    void removeHandler(ClientHandler handler) {
        clients.remove(handler);
    }
    
    private void setPortNr(String[] args) {
         if (args.length > 0) {
            try {
                int temp = Integer.parseInt(args[0]);
                if (temp > 1023 && temp <= 65535 ) { 
                    portNr = temp;
                }
            } catch (Exception e) {
                
            }
        }
    }
}

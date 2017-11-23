/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hangman.server.net;

import hangman.server.model.GameState;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.StandardSocketOptions;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author William Joahnsson
 */
public class Server {
    private int portNr = 5555;
    private final List<ClientHandler> clients = new ArrayList<ClientHandler>();
    private Selector selector;
    private ServerSocketChannel serverChannel;
    
    public static void main(String[] args) {
       Server server = new Server();
       server.setPortNr(args);
       server.run();
    }
    
    private void run() {
        try {
           selector = Selector.open();
           
           serverChannel = ServerSocketChannel.open();
           serverChannel.configureBlocking(false);
           serverChannel.bind(new InetSocketAddress(portNr));
           serverChannel.register(selector, SelectionKey.OP_ACCEPT);
           
           while(true) {
               selector.select();
               for(SelectionKey key : selector.selectedKeys()) {
                    selector.selectedKeys().remove(key);
                   if(!key.isValid()) {
                       continue;
                   } else if (key.isAcceptable()) {
                       startHandler(key);
                   } else if (key.isReadable()) {
                       recvFromClient(key);
                   } else if (key.isWritable()) {
                       sendToClient(key);
                   }
               }
           }
            
        } catch (IOException ioe) {
            System.err.println("Server failing");
        }
    }
    
    private void recvFromClient(SelectionKey key) throws IOException {
        Client client = (Client) key.attachment();
        try {
            client.handler.reciveCmd();
            key.interestOps(SelectionKey.OP_WRITE);
        } catch (IOException clientClosedConnection) {
            removeHandler(key);
        }
    }
    
    private void sendToClient(SelectionKey key) throws IOException {
        Client client = (Client) key.attachment();
        try {
            if (client.sendAll()) {
                key.interestOps(SelectionKey.OP_READ);
            }
        } catch (IOException clientClosedConnection) {
            removeHandler(key);
        }
    }
    
    private void startHandler(SelectionKey key) throws IOException{
        ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
        SocketChannel client = serverChannel.accept();
        client.configureBlocking(false);
        ClientHandler handler = new ClientHandler(this, client);
        client.register(selector, SelectionKey.OP_WRITE, new Client(handler));
        client.setOption(StandardSocketOptions.SO_LINGER, 10000);
    }
    
    void removeHandler(SelectionKey key) throws IOException {
        Client client = (Client) key.attachment();
        client.handler.disconnectClient();
        key.cancel();
    }
    
    private class Client {
        private final ClientHandler handler;
        
        private Client(ClientHandler handler) {
            this.handler = handler;
        }
        
        private boolean sendAll() throws IOException {
            ByteBuffer currCmd = null;
            boolean containedMessage = false;
            while((currCmd = handler.cmdToSend.peek()) != null) {
                handler.sendCmd(currCmd);
                handler.cmdToSend.remove();
                containedMessage = true;
            }
            return containedMessage;
        }
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

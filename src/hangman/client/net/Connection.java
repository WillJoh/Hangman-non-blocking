/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hangman.client.net;
    
import hangman.common.CommandHandler;
import hangman.common.Constants;
import hangman.common.CommandHeaders;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;

/**
 *
 * @author William Joahnsson
 */
public class Connection implements Runnable{
    private Boolean connected;
    private InetSocketAddress serverAddress;
    private SocketChannel channel;
    private Selector selector;
    private final ByteBuffer cmdFromServer = ByteBuffer.allocateDirect(16384);
    private final Queue<ByteBuffer> cmdToServer = new ArrayDeque<>();
    private final CommandHandler cmdHandler = new CommandHandler();
    ComunicationHandler listener;
    private volatile boolean timeToSend = false;
            
    @Override
    public void run() {
        try {
            selector = Selector.open();
            channel = SocketChannel.open();
            
            channel.configureBlocking(false);
            channel.connect(serverAddress);
            channel.register(selector, SelectionKey.OP_CONNECT);
            connected = true;
            
            while(connected || !cmdToServer.isEmpty()) {
                if (timeToSend) {
                    channel.keyFor(selector).interestOps(SelectionKey.OP_WRITE);
                    timeToSend = false;
                }
                
                selector.select();
                for (SelectionKey key : selector.selectedKeys()) {
                    selector.selectedKeys().remove(key);
                    if (!key.isValid()) {
                        continue;
                    }
                    
                    if (key.isConnectable()) {
                        completeConnection(key);
                    } else if (key.isReadable()) {
                        recvFromServer(key);
                    } else if (key.isWritable()) {
                        sendToServer(key);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Fatal error");
        }
    }

    
    public void connect(String hostAddress, int portNr, ComunicationHandler listener) {
        this.listener = listener;
        serverAddress = new InetSocketAddress(hostAddress, portNr);
        new Thread(this).start();
    }
    
    private void completeConnection(SelectionKey key) throws IOException {
        channel.finishConnect();
        key.interestOps(SelectionKey.OP_READ);
                
        Executor pool = ForkJoinPool.commonPool();
            pool.execute(() -> {
                listener.connected(serverAddress);
        });
    }
    
    public void disconnect() throws IOException {
        sendMsg(CommandHeaders.DISCONNECT.toString(), "");
        connected = false;
        doDisconnect();
    }
    
    private void doDisconnect() throws IOException {
        channel.close();
        channel.keyFor(selector).cancel();
        Executor pool = ForkJoinPool.commonPool();
            pool.execute(() -> {
                listener.disconnected();
        });
    }
    
    public void sendStartNewGame() {
        sendMsg(CommandHeaders.START_GAME.toString(), "");
    }
    
    public void sendChar(char guess) {
        sendMsg(CommandHeaders.GUESS_CHAR.toString(), String.valueOf(guess));
    }
    
    public void sendWord(String guess) {
        sendMsg(CommandHeaders.GUESS_WORD.toString(), guess);
    }
    
    public void sendMsg(String header, String body) {
        synchronized (cmdToServer) {
            cmdToServer.add(ByteBuffer.wrap(CommandHandler.prependLengthHeader(header + Constants.CMD_DELIMITER + body).getBytes()));
        }
        timeToSend = true;
        selector.wakeup();
    }
    
    private void sendToServer(SelectionKey key) throws IOException {
        ByteBuffer cmd;
        synchronized (cmdToServer) {
            while ((cmd = cmdToServer.peek()) != null) {
                channel.write(cmd);
                if (cmd.hasRemaining()) {
                    return;
                }
                cmdToServer.remove();
            }
            key.interestOps(SelectionKey.OP_READ);
        }
    }
    
    private void recvFromServer(SelectionKey key) throws IOException {
        cmdFromServer.clear();
        int nofBytes = channel.read(cmdFromServer);
        if (nofBytes == -1) {
            throw new IOException("Fatal error");
        }
        
        cmdFromServer.flip();
        byte[] temp = new byte[cmdFromServer.remaining()];
        cmdFromServer.get(temp);
        String rcvdCmd = new String(temp);
        
        
        cmdHandler.appendNewString(rcvdCmd);
        while (cmdHandler.hasNext()) {
            String cmd = cmdHandler.nextCmd();
            
            Executor pool = ForkJoinPool.commonPool();
            pool.execute(() -> {
                listener.handleMsg(cmd);
            });
        }
    }
}

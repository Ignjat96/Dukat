import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Node {

    private ServerSocket serverSocket;
    private Socket socket;
    private static ExecutorService listenerPool = Executors.newFixedThreadPool(5);

    public Node() throws IOException {
        this.serverSocket = new ServerSocket(18018);
        this.socket = new Socket();
    }

    public ServerSocket getServerSocket() {
        return serverSocket;
    }

    public Socket getSocket() {
        return socket;
    }

    public static Set<String> getAlreadyKnownPeers() throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader("Peers"));
        Set<String> peers = new HashSet<>();

        String line = reader.readLine();
        while (line != null) {
            peers.add(line);
            line = reader.readLine();
        }
        reader.close();

        return peers;
    }

    public static void main(String[] args) throws IOException {
        Node node = new Node();
        try {
            while (true) {
                node.socket = node.serverSocket.accept();

                ListenerThread peerThread = new ListenerThread(node);
                listenerPool.execute(peerThread);
            }
        } finally {
            node.getServerSocket().close();
            node.getSocket().close();
        }

    }

}

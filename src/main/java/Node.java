import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.lang.reflect.Type;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Node {

    private ServerSocket serverSocket;
    private Socket socket;
    private static final int LIMIT = 3;
    private static ArrayList<ExplorerThread> connectedPeersWithExplorer = new ArrayList<>();
    private static ExecutorService listenerPool = Executors.newFixedThreadPool(LIMIT);
    private static ExecutorService explorerPool = Executors.newFixedThreadPool(LIMIT);

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

    public Set<String> getAlreadyKnownPeers() throws IOException {
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

    public void getRemotePeers(BufferedReader in, PrintWriter out) throws IOException {
        JSONObject message = new JSONObject();
        message.put("type", "getpeers");
        out.println(message);
        out.flush();

        String serverResponse = in.readLine();
        serverResponse = serverResponse.replace("\\n", "");
        System.out.println(serverResponse);

        JSONObject serverResponseJSON;
        try {
            serverResponseJSON = (JSONObject) JSONValue.parseWithException(serverResponse);
        } catch (ParseException e) {
            Error.sendError(out, "Received message: " + serverResponse + " is an invalid JSON Object.");
            return;
        }

        if (serverResponseJSON.keySet().size() != 2) {
            Error.sendError(out,"Unsupported protocol message received: " + serverResponseJSON);
            return;
        }
        for (Object key : serverResponseJSON.keySet()) {
            String keyString = key.toString();
            if (!(keyString.equals("type") || keyString.equals("peers"))) {
                Error.sendError(out,"Unsupported protocol message received: " + keyString);
                return;
            }
        }
        if (!serverResponseJSON.get("type").equals("peers")) {
            Error.sendError(out,"Unsupported message type received");
            return;
        }

        Type listType = new TypeToken<HashSet<String>>() {}.getType();
        Set<String> peerList;
        try {
            peerList = new Gson().fromJson(serverResponseJSON.get("peers").toString(), listType);
        } catch (JsonSyntaxException e) {
            Error.sendError(out,"Unsupported message type received");
            return;
        }

        BufferedWriter peersWriter = new BufferedWriter(new FileWriter("Peers", true));
        for (String newPeer : peerList) {
            //Adds a peer only if the peer does not already exist getAlreadyKnownPeers.add(newPeer) returns false is a peer already exists in the DB
            if(this.getAlreadyKnownPeers().add(newPeer)) {
                peersWriter.write(newPeer + "\n");
            }
        }
        peersWriter.close();
    }

    public void establishConnections(Node node) throws IOException {
        ArrayList<String> peersToConnect = new ArrayList<>(this.getAlreadyKnownPeers());
        if (peersToConnect.size() == 0) {
            System.err.println("There are no known peers to connect to!");
            return;
        }
        for (int i = 0; i < peersToConnect.size(); i++) {
            String peer = peersToConnect.get(new Random().nextInt(peersToConnect.size()-1));
            peersToConnect.remove(peer);
            ExplorerThread explorerThread = new ExplorerThread(node, peer);
            connectedPeersWithExplorer.add(explorerThread);
            if (connectedPeersWithExplorer.size() >= LIMIT) {
                return;
            }
            explorerPool.execute(explorerThread);
        }

    }

    public static void main(String[] args) throws IOException {
        Node node = new Node();

        try {
            while (true) {
                node.socket = node.serverSocket.accept();

                ListenerThread peerThread = new ListenerThread(node);
                listenerPool.execute(peerThread);

                node.establishConnections(node);
            }
        } finally {
            node.getServerSocket().close();
            node.getSocket().close();
        }

    }

}

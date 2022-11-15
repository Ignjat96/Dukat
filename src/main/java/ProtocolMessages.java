import Utils.MyUtils;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import data.Database;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.lang.reflect.Type;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class ProtocolMessages {

    private static final int LIMIT = 4000;
    private static ExecutorService explorerPool = Executors.newFixedThreadPool(LIMIT);
    private static ArrayList<ExplorerThread> connectedPeersWithExplorer = new ArrayList<>();

    public static void sendHelloMessage(PrintWriter out) throws IOException {
        JSONObject helloMessage = new JSONObject();
        helloMessage.put("type", "hello");
        helloMessage.put("version", "0.8.0");
        helloMessage.put("agent", "Kerma−Core Client 0.8");
        out.println(helloMessage);
        out.flush();
    }

    public static void sendGetPeersMessage(PrintWriter out) {
        JSONObject getPeersMessage = new JSONObject();
        getPeersMessage.put("type", "getpeers");
        out.println(getPeersMessage);
        out.flush();
    }

    public static void sendPeersMessage(PrintWriter out) throws IOException {
        JSONObject message = new JSONObject();
        JSONArray localPeers = new JSONArray();

        for (String peer: Node.getAlreadyKnownPeers()) {
            localPeers.add(peer);
        }
        message.put("type", "peers");
        message.put("peers", localPeers);

        out.println(message);
        out.flush();
    }

    public static void sendGetObjectMessage(PrintWriter out, String objectid){
        JSONObject message = new JSONObject();
        message.put("type", "getobject");
        message.put("objectid", objectid);
        out.println(message);
        out.flush();
    }

    public static void sendIHaveObjectMessage(PrintWriter out, String objectid){
        JSONObject message = new JSONObject();
        message.put("type", "ihaveobject");
        message.put("objectid", objectid);
        out.println(message);
        out.flush();
    }

    public static void sendObject(PrintWriter out, String object) {
        JSONObject message = new JSONObject();
        message.put("type", "object");
        message.put("object", object);
        out.println(message);
        out.flush();
    }

    public static void receivePeersMessage(String receivedPeersMessage, PrintWriter out) throws IOException {
        if (receivedPeersMessage == null) {
            Error.sendError(out, "Received message: " + receivedPeersMessage + " is an invalid JSON Object.");
            return;
        }
        receivedPeersMessage = receivedPeersMessage.replace("\\n", "");
        JSONObject receivedPeersMessageJSON;
        try {
            receivedPeersMessageJSON = (JSONObject) JSONValue.parseWithException(receivedPeersMessage);
        } catch (ParseException e) {
            Error.sendError(out, "Received message: " + receivedPeersMessage + " is an invalid JSON Object.");
            return;
        }
        if (receivedPeersMessageJSON.keySet().size() != 2) {
            Error.sendError(out,"Unsupported protocol message received: " + receivedPeersMessageJSON);
            return;
        }
        for (Object key : receivedPeersMessageJSON.keySet()) {
            String keyString = key.toString();
            if (!(keyString.equals("type") || keyString.equals("peers"))) {
                Error.sendError(out,"Unsupported protocol message received: " + keyString);
                return;
            }
        }
        if (!receivedPeersMessageJSON.get("type").equals("peers")) {
            Error.sendError(out,"Unsupported message type received");
            return;
        }

        Type listType = new TypeToken<HashSet<String>>() {}.getType();
        Set<String> receivedPeersSet;
        try {
            receivedPeersSet = new Gson().fromJson(receivedPeersMessageJSON.get("peers").toString(), listType);
        } catch (JsonSyntaxException e) {
            Error.sendError(out,"Unsupported message type received");
            return;
        }

        BufferedWriter peersWriter = new BufferedWriter(new FileWriter("Peers", true));
        for (String newPeer : receivedPeersSet) {
            //Adds a peer only if the peer does not already exist getAlreadyKnownPeers.add(newPeer) returns false is a peer already exists in the DB
            if(Node.getAlreadyKnownPeers().add(newPeer)) {
                peersWriter.write(newPeer + "\n");
            }
        }
        peersWriter.close();
    }

    public static void receiveGetObjectMessage(String receivedMessage, PrintWriter out, BufferedReader in) throws IOException {
        if (receivedMessage == null || receivedMessage.equals("")) {
            Error.sendError(out, "Received message: " + receivedMessage + " is an invalid JSON Object.");
            out.close();
            in.close();
            return;
        }
        Database database = new Database("src/main/java/data/database.txt");
        JSONObject receivedMessageJSON;
        try {
            receivedMessageJSON = (JSONObject) JSONValue.parseWithException(receivedMessage);
        } catch (ParseException e) {
            Error.sendError(out, "Received message: " + receivedMessage + " is an invalid JSON Object.");
            return;
        }
        if (receivedMessageJSON.get("objectid") == null) {
            Error.sendError(out, "Received message: " + receivedMessage + " is an invalid JSON Object.");
            out.close();
            in.close();
            return;
        }
        String objectid = receivedMessageJSON.get("objectid").toString();
        if(database.getDatabase().containsKey(objectid)){
            System.out.println(database.getDatabase().get(objectid));
            sendObject(out, database.getDatabase().get(objectid));
        }
    }

    public static void receiveIHaveObjectMessage(String receivedMessage, PrintWriter out, BufferedReader in) throws IOException {
        if (receivedMessage == null || receivedMessage.equals("")) {
            Error.sendError(out, "Received message: " + receivedMessage + " is an invalid JSON Object.");
            out.close();
            in.close();
            return;
        }
        Database database = new Database("src/main/java/data/database.txt");
        JSONObject receivedIHaveObjectMessageJSON;
        try {
            receivedIHaveObjectMessageJSON = (JSONObject) JSONValue.parseWithException(receivedMessage);
        } catch (ParseException e) {
            Error.sendError(out, "Received message: " + receivedMessage + " is an invalid JSON Object.");
            return;
        }
        if (receivedIHaveObjectMessageJSON.get("objectid") == null) {
            Error.sendError(out, "Received message: " + receivedMessage + " is an invalid JSON Object.");
            out.close();
            in.close();
            return;
        }
        String objectid = receivedIHaveObjectMessageJSON.get("objectid").toString();
        if(!database.getDatabase().containsKey(objectid)){
            sendGetObjectMessage(out, objectid);
        }
    }

    public static void receiveObjectMessage(String receivedMessage, PrintWriter out, BufferedReader in, Node node) throws IOException, NoSuchAlgorithmException {
        Database database = new Database("src/main/java/data/database.txt");
        JSONObject receivedObjectJSON;
        System.out.println(receivedMessage);
        if (receivedMessage == null) {
            Error.sendError(out, "Received message: " + receivedMessage + " is an invalid JSON Object.");
            return;
        }
        try {
            receivedObjectJSON = (JSONObject) JSONValue.parseWithException(receivedMessage);
        } catch (ParseException e) {
            Error.sendError(out, "Received message: " + receivedMessage + " is an invalid JSON Object.");
            return;
        }
        if (receivedObjectJSON.get("object") == null) {
            Error.sendError(out, "Received message: " + receivedMessage + " is an invalid JSON Object.");
            out.close();
            in.close();
            return;
        }
        String object = receivedObjectJSON.get("object").toString();
        String objectid = MyUtils.getSHA(object);
        if(!database.getDatabase().containsKey(objectid)){
            database.getDatabase().put(objectid, object);
            database.saveDatabase();
            sendIHaveObjectMessage(out, objectid);
            gossip(node, objectid);
        }
    }

    public static void gossip(Node node, String objectId) throws IOException {
        ArrayList<String> peersToConnect = new ArrayList<>(node.getAlreadyKnownPeers());
        System.out.println(peersToConnect);
        if (peersToConnect.size() == 0) {
            System.err.println("There are no known peers to connect to!");
            return;
        }
        peersToConnect.remove("139.59.136.230:18018");
        for (int i = 0; i < peersToConnect.size(); i++) {
            String peer = peersToConnect.get(i);
            boolean flag = false;
            for (ExplorerThread thread : connectedPeersWithExplorer) {
                if (thread.getAddressAndPort().equals(peer)) {
                    flag = true;
                }
            }
            if (flag) continue;
            ExplorerThread explorerThread = new ExplorerThread(node, peer, objectId);
            connectedPeersWithExplorer.add(explorerThread);
            explorerPool.execute(explorerThread);
        }

    }
}

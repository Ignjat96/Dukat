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
import java.util.HashSet;
import java.util.Set;

public final class ProtocolMessages {

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

    public static void sendObject(PrintWriter out, Object object) throws IOException {
        JSONObject message = new JSONObject();
        message.put("type", "ihaveobject");
        message.put("objectid", MyUtils.getCanonicJSON(object));
        out.println(message);
        out.flush();
    }

    public static void receivePeersMessage(String receivedPeersMessage, PrintWriter out) throws IOException {
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

    public static void receiveGetObjectMessage(String receivedMessage, PrintWriter out) throws IOException {
        Database database = new Database("src/main/java/data/database.txt");
        receivedMessage = receivedMessage.replace("\\n", "");
        JSONObject receivedPeersMessageJSON;
        try {
            receivedPeersMessageJSON = (JSONObject) JSONValue.parseWithException(receivedMessage);
        } catch (ParseException e) {
            Error.sendError(out, "Received message: " + receivedMessage + " is an invalid JSON Object.");
            return;
        }

        String objectid = receivedPeersMessageJSON.get("objectid").toString();
        if(database.getDatabase().containsKey(objectid)){
            sendObject(out, database.getDatabase().get(objectid));
        }
    }

    public static void receiveIHaveObjectMessage(String receivedMessage, PrintWriter out) throws IOException {
        Database database = new Database("src/main/java/data/database.txt");
        receivedMessage = receivedMessage.replace("\\n", "");
        JSONObject receivedIHaveObjectMessageJSON;
        try {
            receivedIHaveObjectMessageJSON = (JSONObject) JSONValue.parseWithException(receivedMessage);
        } catch (ParseException e) {
            Error.sendError(out, "Received message: " + receivedMessage + " is an invalid JSON Object.");
            return;
        }

        String objectid = receivedIHaveObjectMessageJSON.get("objectid").toString();
        if(!database.getDatabase().containsKey(objectid)){
            sendGetObjectMessage(out, objectid);
        }
    }

    public static void receiveObjectMessage(String receivedMessage, PrintWriter out) throws IOException, NoSuchAlgorithmException {
        Database database = new Database("src/main/java/data/database.txt");
        receivedMessage = receivedMessage.replace("\\n", "");
        JSONObject receivedObjectJSON;
        try {
            receivedObjectJSON = (JSONObject) JSONValue.parseWithException(receivedMessage);
        } catch (ParseException e) {
            Error.sendError(out, "Received message: " + receivedMessage + " is an invalid JSON Object.");
            return;
        }


        String object = receivedObjectJSON.get("object").toString();
        String objectid = MyUtils.getSHA(object);
        if(!database.getDatabase().containsKey(objectid)){
            database.getDatabase().put(objectid, object);
            database.saveDatabase();
            sendIHaveObjectMessage(out, objectid);
        }
    }
}

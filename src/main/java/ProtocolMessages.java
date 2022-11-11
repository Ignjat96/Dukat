import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.lang.reflect.Type;
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

}

import org.json.simple.JSONObject;

public final class ProtocolMessages {

    public static JSONObject helloMessage() {
        JSONObject helloMessage = new JSONObject();
        helloMessage.put("type", "hello");
        helloMessage.put("version", "0.8.0");
        helloMessage.put("agent", "Kerma−Core Client 0.8");
        return helloMessage;
    }

    public static JSONObject getPeersMessage() {
        JSONObject getPeersMessage = new JSONObject();
        getPeersMessage.put("type", "getpeers");
        return getPeersMessage;
    }

}

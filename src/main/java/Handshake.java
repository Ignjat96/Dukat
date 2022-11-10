import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public final class Handshake {

    private static boolean helloMessageChecker(PrintWriter out, String message) throws IOException {
        if (message == null || message.equals("")) {
            Error.sendError(out, "Received message: " + message + " is an invalid hello message.");
            return false;
        }

        JSONObject messageJSON;
        try {
             messageJSON = (JSONObject) JSONValue.parseWithException(message);
        } catch (ParseException e) {
            Error.sendError(out, "Received message: " + message + " is an invalid hello message.");
            return false;
        }

        if (messageJSON.keySet().size() != 3 && messageJSON.keySet().size() != 2) {
            Error.sendError(out, "Received message: " + messageJSON + " prior to hello message.");
            return false;
        }
        for (Object key : messageJSON.keySet()) {
            String keyString = key.toString();
            if (!(keyString.equals("type") || keyString.equals("version") || keyString.equals("agent"))) {
                Error.sendError(out, "Received message: " + messageJSON + " prior to hello message.");
                return false;
            }
        }
        if (!messageJSON.get("type").equals("hello")) {
            Error.sendError(out, "Received message: " + messageJSON + " prior to hello message.");
            return false;
        } else if (!messageJSON.get("version").toString().startsWith("0.8")) {
            Error.sendError(out, "Unsupported message version received.");
            return false;
        }
        return true;
    }

    public static boolean sendHelloMessage(BufferedReader in, PrintWriter out) throws IOException {
        out.println(ProtocolMessages.helloMessage());
        out.println(ProtocolMessages.getPeersMessage());
        out.flush();

        String message = in.readLine();
        System.out.println(message);
        for (int i = 0; i < 1; i++) {
            System.out.println(in.readLine());
        }

        if (!helloMessageChecker(out, message)) return false;

        return true;
    }
}

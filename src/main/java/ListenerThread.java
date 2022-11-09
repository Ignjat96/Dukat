import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import java.io.*;


public class ListenerThread implements Runnable {

    private Node node;
    private BufferedReader in;
    private PrintWriter out;

    public ListenerThread(Node node) throws IOException {
        this.node = node;
        this.out = new PrintWriter(node.getSocket().getOutputStream());
        this.in = new BufferedReader(new InputStreamReader(node.getSocket().getInputStream()));
    }

    @Override
    public void run() {
        try {
            if (!Handshake.handshake(in, out)) {
                out.close();
                in.close();
                return;
            }
            node.getRemotePeers(in, out);
            try {
                while (true) {
                    String receivedMessage = in.readLine();
                    if (receivedMessage == null) break;

                    System.out.println(receivedMessage);
                    JSONObject receivedMessageJSON = (JSONObject) JSONValue.parse(receivedMessage);

                    if (receivedMessageJSON.get("type").equals("getpeers")) {
                        JSONObject message = new JSONObject();
                        JSONArray localPeers = new JSONArray();

                        for (String peer: node.getAlreadyKnownPeers()) {
                            localPeers.add(peer);
                        }
                        message.put("type", "peers");
                        message.put("peers", localPeers);

                        out.println(message);
                        out.flush();
                    } else {
                        Error.sendError(out,"Unsupported message type received");
                    }
                }
            } finally {
                out.close();
                in.close();
            }
        } catch (IOException e) {
            System.err.println(e.getStackTrace());
        }

    }
}

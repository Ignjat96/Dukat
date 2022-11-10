import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ExplorerThread implements Runnable {

    private Node node;
    private String addressAndPort;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    public ExplorerThread(Node node, String peer) throws IOException {
        this.node = node;
        this.addressAndPort = peer;
        String[] addressAndPortSplitted = peer.split(":");
        this.socket = new Socket(addressAndPortSplitted[0], Integer.parseInt(addressAndPortSplitted[1]));
        this.out = new PrintWriter(this.socket.getOutputStream());
        this.in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
    }

    public String getAddressAndPort() {
        return addressAndPort;
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

            out.close();
            in.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}

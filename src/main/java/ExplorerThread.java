import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;

public class ExplorerThread implements Runnable {

    private Node node;
    private String addressAndPort;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private boolean available = true;
    private String objectId;

    public ExplorerThread(Node node, String peer, String objectId) throws IOException {
        this.objectId = objectId;
        this.node = node;
        this.addressAndPort = peer;
        String[] addressAndPortSplitted = peer.split(":");
        try {
            this.socket = new Socket(addressAndPortSplitted[0], Integer.parseInt(addressAndPortSplitted[1]));
        } catch (SocketException e) {
            System.out.println("Node: " + addressAndPort + " is not available.");
            this.available = false;
            return;
        }
        System.out.println("Connecting to Node: " + addressAndPort + ".");
        this.out = new PrintWriter(this.socket.getOutputStream());
        this.in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
    }

    public String getAddressAndPort() {
        return addressAndPort;
    }

    public boolean isAvailable() {
        return available;
    }

    @Override
    public void run() {
        try {
            ProtocolMessages.sendHelloMessage(out);
            ProtocolMessages.sendGetPeersMessage(out);
            ProtocolMessages.sendIHaveObjectMessage(out, objectId);

            String recievedHelloMessage = in.readLine();
            System.out.println(recievedHelloMessage);
            if (!Handshake.helloMessageChecker(out, recievedHelloMessage)) {
                out.close();
                in.close();
                return;
            }
            String serverResponse = in.readLine();
            System.out.println(serverResponse);
            ProtocolMessages.sendPeersMessage(out);

            serverResponse = in.readLine();
            System.out.println(serverResponse);

            serverResponse = in.readLine();
            System.out.println(serverResponse);

            serverResponse = in.readLine();
            System.out.println(serverResponse);
            ProtocolMessages.receivePeersMessage(serverResponse, out);

            serverResponse = in.readLine();
            System.out.println(serverResponse);



            out.close();
            in.close();
        } catch (IOException e) {
            System.err.println(e.getStackTrace());
        }


    }
}

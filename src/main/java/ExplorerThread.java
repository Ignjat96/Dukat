
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
    private String objectId;

    public ExplorerThread(Node node, String peer, String objectId) {
        this.objectId = objectId;
        this.node = node;
        this.addressAndPort = peer;
    }

    public String getAddressAndPort() {
        return addressAndPort;
    }

    private boolean connect() throws IOException {
        String[] addressAndPortSplitted = this.addressAndPort.split(":");
        try {
            this.socket = new Socket(addressAndPortSplitted[0], Integer.parseInt(addressAndPortSplitted[1]));
        } catch (IOException e) {
            System.out.println("Node: " + addressAndPort + " is not available.");
            return false;
        }
        this.out = new PrintWriter(this.socket.getOutputStream());
        this.in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
        if (this.out == null) {
            return false;
        }
        System.out.println("Connecting to Node: " + addressAndPort + ".");
        return true;
    }

    @Override
    public void run() {
        try {
            if (!connect()) return;
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

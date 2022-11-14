import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.security.NoSuchAlgorithmException;


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
            ProtocolMessages.sendHelloMessage(out);
            ProtocolMessages.sendGetPeersMessage(out);

            String recievedHelloMessage = in.readLine();
            System.out.println(recievedHelloMessage);
            if (!Handshake.helloMessageChecker(out, recievedHelloMessage)) {
                out.close();
                in.close();
                return;
            }

            try {
                loop: while (true) {
                    String receivedMessage = in.readLine();
                    if (receivedMessage == null) break loop;

                    JSONObject receivedMessageJSON;
                    try {
                        receivedMessageJSON = (JSONObject) JSONValue.parseWithException(receivedMessage);
                    } catch (ParseException e) {
                        Error.sendError(out, "Received message: " + receivedMessage + " is not a valid JSON object.");
                        break loop;
                    }
                    String type = (String) receivedMessageJSON.get("type");
                    switch (type) {
                        case "getpeers":
                            System.out.println(receivedMessage);
                            ProtocolMessages.sendPeersMessage(out);
                            break;
                        case "peers":
                            System.out.println(receivedMessage);
                            ProtocolMessages.receivePeersMessage(receivedMessage, out);
                            break;
                        case "getobject":
                            System.out.println(receivedMessage);
                            ProtocolMessages.receiveGetObjectMessage(receivedMessage, out, in);
                            break;
                        case "ihaveobject":
                            System.out.println(receivedMessage);
                            ProtocolMessages.receiveIHaveObjectMessage(receivedMessage, out, in);
                            break;
                        case "object":
                            System.out.println(receivedMessage);
                            ProtocolMessages.receiveObjectMessage(receivedMessage, out, in, node);
                            break;
                        default:
                            Error.sendError(out,"Unsupported message type received");
                            break loop;
                    }
                }
            } catch (NoSuchAlgorithmException e) {
                System.err.println(e.getStackTrace());
            } finally {
                out.close();
                in.close();
            }
        } catch (IOException e) {
            System.err.println(e.getStackTrace());
        }

    }
}

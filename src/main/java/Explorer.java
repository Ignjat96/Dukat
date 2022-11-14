
import java.io.*;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;

public class Explorer {

    private final Socket socket;
    private final boolean connectingToBootstrap = false;

    public Explorer() throws IOException {
        FileReader reader = new FileReader("bootstrappingNode.properties");
        Properties properties = new Properties();
        properties.load(reader);

        String IP = properties.getProperty("IP");
        String Port = properties.getProperty("Port");
        reader.close();

        if (connectingToBootstrap) {
            this.socket = new Socket(IP, Integer.parseInt(Port));
        } else {
            String DigitalOceanIP = "139.59.136.230";
            boolean remote = false;
            if (remote)
                this.socket = new Socket(DigitalOceanIP, Integer.parseInt(Port));
            else
                this.socket = new Socket("localhost", 18018);
        }
    }

    public static void main(String[] args) throws IOException, NoSuchAlgorithmException {
        Explorer explorer = new Explorer();

        PrintWriter out = new PrintWriter(explorer.socket.getOutputStream());
        BufferedReader in = new BufferedReader(new InputStreamReader(explorer.socket.getInputStream()));
        BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in));

        ProtocolMessages.sendHelloMessage(out);
        ProtocolMessages.sendGetPeersMessage(out);

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

        if(explorer.connectingToBootstrap) {
            serverResponse = in.readLine();
            System.out.println(serverResponse);

            serverResponse = in.readLine();
            System.out.println(serverResponse);
        }

        serverResponse = in.readLine();
        System.out.println(serverResponse);
        ProtocolMessages.receivePeersMessage(serverResponse, out);

        loop: while (true) {
            System.out.println("> ");
            String command = keyboard.readLine();

            switch (command) {
                case "exit":
                    break loop;
                case "getpeers":
                    ProtocolMessages.sendGetPeersMessage(out);
                    serverResponse = in.readLine();
                    System.out.println(serverResponse);
                    ProtocolMessages.receivePeersMessage(serverResponse, out);
                    break;
                case "peers":
                    ProtocolMessages.sendPeersMessage(out);
                    break;
                case "getobject":
                    ProtocolMessages.sendGetObjectMessage(out, null);
                    serverResponse = in.readLine();
                    System.out.println(serverResponse);
                    ProtocolMessages.receiveObjectMessage(serverResponse, out, in, null);
                    break;
                case "ihaveobject":
                    ProtocolMessages.sendIHaveObjectMessage(out, null);
                    serverResponse = in.readLine();
                    System.out.println(serverResponse);
                    break;
                case "object":
                    ProtocolMessages.sendObject(out, "{\"height\":0,\"outputs\":[{\"pubkey\":\"8bd22d5b544887762cd6104b433d93e1f9a5f451fe47d641733e517d9551ab05\",\"value\":50}],\"type\":\"transaction\"}");
                    serverResponse = in.readLine();
                    System.out.println(serverResponse);
                    break;
                default:
                    System.out.println("Not recognizable command: " + command);
            }
        }
        out.close();
        in.close();
        keyboard.close();
    }
}

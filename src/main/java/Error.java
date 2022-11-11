import org.json.simple.JSONObject;

import java.io.IOException;
import java.io.PrintWriter;

public final class Error {
    public static void sendError(PrintWriter out, String cause) throws IOException {
        JSONObject message = new JSONObject();
        message.put("type", "error");
        message.put("error", cause);

        out.println(message);
        out.flush();
        System.err.println(message);
    }
}

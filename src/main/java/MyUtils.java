import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.TreeSet;

public final class MyUtils {
    public static String toHexString(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (int i = 0; i < hash.length; i++) {
            String hex = Integer.toHexString(0xff & hash[i]);
            if(hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    public static String getSHA(String input) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        return toHexString(md.digest(input.getBytes(StandardCharsets.UTF_8)));
    }

    public static JsonElement canonicalize(JsonElement src) {
        if (src instanceof JsonArray) {
            // Canonicalize each element of the array
            JsonArray srcArray = (JsonArray)src;
            JsonArray result = new JsonArray();
            for (int i = 0; i < srcArray.size(); i++) {
                result.add(canonicalize(srcArray.get(i)));
            }
            return result;
        } else if (src instanceof JsonObject) {
            // Sort the attributes by name, and the canonicalize each element of the object
            JsonObject srcObject = (JsonObject)src;
            JsonObject result = new JsonObject();
            TreeSet<String> attributes = new TreeSet<>();
            for (Map.Entry<String, JsonElement> entry : srcObject.entrySet()) {
                attributes.add(entry.getKey());
            }
            for (String attribute : attributes) {
                result.add(attribute, canonicalize(srcObject.get(attribute)));
            }
            return result;
        } else {
            return src;
        }
    }
}

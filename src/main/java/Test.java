import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.*;
import data.Block;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import java.nio.charset.StandardCharsets;
import org.erdtman.jcs.JsonCanonicalizer;

public class Test {

    public static void main(String[] args) throws NoSuchAlgorithmException, IOException {
        Block block = new Block();
        block.setCreated(1624219079);
        block.setNonce("0000000000000000000000000000000000000000000000000000002634878840");
        block.setT("00000002af000000000000000000000000000000000000000000000000000000");
        block.setType("block");
        block.setMiner("dionyziz");
        block.setTxids(new String[]{});
        block.setNote("The Economist 2021−06−20: Crypto−miners are probably to blame for the graphics−chip shortage");
        block.setPrevid(null);

        ObjectMapper mapper = new ObjectMapper();
        String jsonString = mapper.writeValueAsString(block);
        JsonCanonicalizer jc = new JsonCanonicalizer(jsonString);
        System.out.println(jc.getEncodedString());
        System.out.println(MyUtils.getSHA(jc.getEncodedString()));

        Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().create();
        JsonElement je = JsonParser.parseString(jc.getEncodedString());
        String prettyJsonString = gson.toJson(je);

        System.out.println(prettyJsonString);


        System.out.println(MyUtils.getSHA(prettyJsonString));
    }
}

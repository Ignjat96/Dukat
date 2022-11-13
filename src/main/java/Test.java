import Utils.MyUtils;
import com.google.gson.*;
import data.Block;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;

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

        System.out.println(MyUtils.getCanonicJSON(block));
        System.out.println(MyUtils.getSHA(MyUtils.getCanonicJSON(block)));

        String genesisBlock = Files.readString(Path.of("src/main/java/data/genesis.txt"));
        System.out.println(genesisBlock);
        System.out.println(MyUtils.getSHA(genesisBlock));

        System.out.println(MyUtils.getCanonicJSON(block).equals(genesisBlock));

        String trans = Files.readString(Path.of("src/main/java/data/transaction.txt"));
        System.out.println(trans);
        System.out.println(MyUtils.getSHA(trans));
    }
}

import Utils.MyUtils;
import data.Block;
import data.Database;
import data.Output;
import data.Transaction;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;

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

        // make transactio
        Transaction transaction = new Transaction();
        transaction.setHeight(1);
        Output output = new Output("62b7c521cd9211579cf70fd4099315643767b96711febaa5c76dc3daf27c281c", 50000000000000L);
        ArrayList<Output> outputs = new ArrayList<>();
        outputs.add(output);
        transaction.setOutputs(outputs);
        transaction.setType("transaction");

        System.out.println(MyUtils.getCanonicJSON(transaction));
        System.out.println(MyUtils.getSHA(MyUtils.getCanonicJSON(transaction)));

        String trans = Files.readString(Path.of("src/main/java/data/transaction.txt"));
        System.out.println(trans);
        System.out.println(MyUtils.getSHA(trans));

        Database database = new Database("src/main/java/data/database.txt");
        database.addBlock(block);
        database.addTransaction(transaction);
        database.saveDatabase();

        Database database2 = new Database("src/main/java/data/database.txt");
        System.out.println(database2.toString());
    }
}

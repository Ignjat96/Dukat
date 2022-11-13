package data;
import Utils.MyUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Database {
    private String filename;
    private Map<String, String> database;

    public Database(String filename) {
        this.filename = filename;
        this.database = new HashMap<>();
        this.readDatabase();
    }

    public void readDatabase() {
        try {
            File myObj = new File(this.filename);
            Scanner myReader = new Scanner(myObj);
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                database.put(data.split(" ")[0], data.split(" ")[1]);
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    public void saveDatabase() {
        try {
            File myObj = new File(this.filename);
            if (myObj.createNewFile()) {
                System.out.println("File created: " + myObj.getName());
            } else {
                myObj.delete();
                myObj.createNewFile();
            }
            FileWriter myWriter = new FileWriter("filename.txt");
            for (Map.Entry<String, String> entry : database.entrySet()) {
                myWriter.write(entry.getKey() + " " + entry.getValue() + "\n");
            }
            myWriter.close();
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public void addBlock(Block block) throws IOException, NoSuchAlgorithmException {
        this.database.put(MyUtils.getSHA(MyUtils.getCanonicJSON(block)), MyUtils.getCanonicJSON(block));
    }

    public void addTransaction(Transaction transaction) throws IOException, NoSuchAlgorithmException {
        this.database.put(MyUtils.getSHA(MyUtils.getCanonicJSON(transaction)), MyUtils.getCanonicJSON(transaction));
    }


}

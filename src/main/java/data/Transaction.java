package data;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Base64;

import java.util.List;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
public class Transaction {
    private String type;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<Input> inputs;
    private List<Output> outputs;
    private Integer height;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<Input> getInputs() {
        return inputs;
    }

    public void setInputs(List<Input> inputs) {
        this.inputs = inputs;
    }

    public List<Output> getOutputs() {
        return outputs;
    }

    public void setOutputs(List<Output> outputs) {
        this.outputs = outputs;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public boolean validateTransaction() {
        Database database = new Database("src/main/java/data/database.txt");
        int sumOfInputs = 0;
        int sumOfOutputs = 0;
        for (Input input : inputs) {
            String txid = input.getOutpoint().getTxid();
            if(!database.getDatabase().containsKey(txid)){
                return false;
            }
            // verify the signature with ed25519
            // kako provjeriti the sum of all input values is at least the sum of output values, ako input nema value?
        }

        for(Output output : outputs){
            if(output.getPubkey().length() != 64 && output.getValue() < 0){
                return false;
            }
        }
        return true;
    }

}


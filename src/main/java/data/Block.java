package data;


import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
public class Block {
    private String[] txids;
    private String type;
    private String nonce;
    private String previd;
    private Integer created;
    @JsonProperty("T")
    private String T;
    private String miner;
    private String note;

    public String[] getTxids() {
        return txids;
    }

    public void setTxids(String[] txids) {
        this.txids = txids;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getNonce() {
        return nonce;
    }

    public void setNonce(String nonce) {
        this.nonce = nonce;
    }

    public String getPrevid() {
        return previd;
    }

    public void setPrevid(String previd) {
        this.previd = previd;
    }

    public Integer getCreated() {
        return created;
    }

    public void setCreated(Integer created) {
        this.created = created;
    }

    public String getT() {
        return T;
    }

    public void setT(String T) {
        this.T = T;
    }

    public String getMiner() {
        return miner;
    }

    public void setMiner(String miner) {
        this.miner = miner;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}

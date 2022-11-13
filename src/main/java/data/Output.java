package data;

public class Output {
    private String pubkey;
    private Long value;

    public Output(String pubkey, Long value) {
        this.pubkey = pubkey;
        this.value = value;
    }

    public String getPubkey() {
        return pubkey;
    }

    public void setPubkey(String pubkey) {
        this.pubkey = pubkey;
    }

    public Long getValue() {
        return value;
    }

    public void setValue(Long value) {
        this.value = value;
    }
}

package data;

public class Transaction {
    private String type;
    private Input[] inputs;
    private String outputs;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Input[] getInputs() {
        return inputs;
    }

    public void setInputs(Input[] inputs) {
        this.inputs = inputs;
    }

    public String getOutputs() {
        return outputs;
    }

    public void setOutputs(String outputs) {
        this.outputs = outputs;
    }

    class Input {
        private Outpoint outpoint;
        private String sig;

        public Outpoint getOutpoint() {
            return outpoint;
        }

        public void setOutpoint(Outpoint outpoint) {
            this.outpoint = outpoint;
        }

        public String getSig() {
            return sig;
        }

        public void setSig(String sig) {
            this.sig = sig;
        }

        class Outpoint{
            private String txid;
            private Integer index;

            public String getTxid() {
                return txid;
            }
            public void setTxid(String txid) {
                this.txid = txid;
            }
            public Integer getIndex() {
                return index;
            }
            public void setIndex(Integer index) {
                this.index = index;
            }
        }
    }

    class Output {
        private String pubkey;
        private Integer value;
    }
}


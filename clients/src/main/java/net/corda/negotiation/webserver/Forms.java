package net.corda.negotiation.webserver;

public class Forms {
    public static class ProposalForm {
         private Boolean isBuyer;
        private Double unitAmount;
        private Double unitPrice;
        private String partyName;

        public Boolean getBuyer() {
            return isBuyer;
        }

        public void setBuyer(Boolean buyer) {
            isBuyer = buyer;
        }

        public Double getUnitAmount() {
            return unitAmount;
        }

        public void setUnitAmount(Double unitAmount) {
            this.unitAmount = unitAmount;
        }

        public Double getUnitPrice() {
            return unitPrice;
        }

        public void setUnitPrice(Double unitPrice) {
            this.unitPrice = unitPrice;
        }

        public String getPartyName() {
            return partyName;
        }

        public void setPartyName(String partyName) {
            this.partyName = partyName;
        }
    }
    public static class ModifyForm {
        private String proposalId;
        private Double newValue;
        private  Double newUnitPrice;

        public String getProposalId() {
            return proposalId;
        }

        public void setProposalId(String proposalId) {
            this.proposalId = proposalId;
        }

        public Double getNewValue() {
            return newValue;
        }

        public void setNewValue(Double newValue) {
            this.newValue = newValue;
        }

        public Double getNewUnitPrice() {
            return newUnitPrice;
        }

        public void setNewUnitPrice(Double newUnitPrice) {
            this.newUnitPrice = newUnitPrice;
        }
    }
    public static class AcceptForm {
        private String proposalId;

        public String getProposalId() {
            return proposalId;
        }

        public void setProposalId(String proposalId) {
            this.proposalId = proposalId;
        }
    }
    public static class KillModifyForm{
        private String proposalId;

        public String getProposalId() {
            return proposalId;
        }

        public void setProposalId(String proposalId) {
            this.proposalId = proposalId;
        }
    }
    public static class KillProposalForm{
        private String proposalId;

        public String getProposalId() {
            return proposalId;
        }

        public void setProposalId(String proposalId) {
            this.proposalId = proposalId;
        }
    }

}

package lk.smartcity360.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/** Encapsulates the step-by-step pipeline report for the Decorator pattern demonstration. */
public class DecoratorReport implements Serializable {

    public static class SenderStep implements Serializable {
        private final String step;
        private final String layers;
        private final String sizeFormatted;
        private final String dataPreview;

        public SenderStep(String step, String layers, String sizeFormatted, String dataPreview) {
            this.step = step;
            this.layers = layers;
            this.sizeFormatted = sizeFormatted;
            this.dataPreview = dataPreview;
        }

        public String getStep() { return step; }
        public String getLayers() { return layers; }
        public String getSizeFormatted() { return sizeFormatted; }
        public String getDataPreview() { return dataPreview; }
    }

    public static class ReceiverStep implements Serializable {
        private final String operation;
        private final String status;
        private final boolean success;
        private final String detail;

        public ReceiverStep(String operation, String status, boolean success, String detail) {
            this.operation = operation;
            this.status = status;
            this.success = success;
            this.detail = detail;
        }

        public String getOperation() { return operation; }
        public String getStatus() { return status; }
        public boolean isSuccess() { return success; }
        public String getDetail() { return detail; }
    }

    private final String selectedMeter;
    private final String finalChain;
    private final List<SenderStep> senderSteps;
    private final boolean tampered;
    private final String networkStatus;
    private final String networkMessage;
    private final List<ReceiverStep> receiverSteps;
    private final boolean allPassed;
    private final String summaryMessage;
    private final String recoveredData;

    public DecoratorReport(String selectedMeter, String finalChain,
                           List<SenderStep> senderSteps, boolean tampered,
                           String networkStatus, String networkMessage,
                           List<ReceiverStep> receiverSteps, boolean allPassed,
                           String summaryMessage, String recoveredData) {
        this.selectedMeter = selectedMeter;
        this.finalChain = finalChain;
        this.senderSteps = senderSteps != null ? senderSteps : new ArrayList<>();
        this.tampered = tampered;
        this.networkStatus = networkStatus;
        this.networkMessage = networkMessage;
        this.receiverSteps = receiverSteps != null ? receiverSteps : new ArrayList<>();
        this.allPassed = allPassed;
        this.summaryMessage = summaryMessage;
        this.recoveredData = recoveredData;
    }

    public String getSelectedMeter() { return selectedMeter; }
    public String getFinalChain() { return finalChain; }
    public List<SenderStep> getSenderSteps() { return senderSteps; }
    public boolean isTampered() { return tampered; }
    public String getNetworkStatus() { return networkStatus; }
    public String getNetworkMessage() { return networkMessage; }
    public List<ReceiverStep> getReceiverSteps() { return receiverSteps; }
    public boolean isAllPassed() { return allPassed; }
    public String getSummaryMessage() { return summaryMessage; }
    public String getRecoveredData() { return recoveredData; }
}

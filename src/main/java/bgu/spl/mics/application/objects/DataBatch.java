package bgu.spl.mics.application.objects;

/**
 * Passive object representing a data used by a model.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */

public class DataBatch {
    private Data data; //The data the batch belongs to
    private int startIndex; //The index of the first sample in the batch, we decided not to use this field

    //WE ADDED ***
    private Data.Type type;
    private GPU gpuSender;

    private boolean isProcessedByCPU;
    private int timeToProcessByCPU; //will be initialized when processed

    public DataBatch (Data data, int startIndex, GPU gpuSender) {
        this.data=data;
        this.startIndex=startIndex;
        this.gpuSender=gpuSender;
        isProcessedByCPU=false;
        timeToProcessByCPU=0;
    }

    //For Testing****
    public DataBatch (Data data, int startIndex) {
        this.data=data;
        this.startIndex=startIndex;
    }

    public Data getData() {
        return data;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public Data.Type getType() {
        return type;
    }

    public GPU getGpuSender() {
        return gpuSender;
    }

    public boolean isProcessedByCPU() {
        return isProcessedByCPU;
    }

    public int getTimeToProcessByCPU() {
        return timeToProcessByCPU;
    }

    public void doneProcessedByCPU() {
        isProcessedByCPU = true;
    }

    public void setTimeToProcessByCPU(int timeToProcessByCPU) {
        this.timeToProcessByCPU = timeToProcessByCPU;
    }
}

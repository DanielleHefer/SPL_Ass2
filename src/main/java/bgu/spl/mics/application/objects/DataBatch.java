package bgu.spl.mics.application.objects;

/**
 * Passive object representing a data used by a model.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */

public class DataBatch {
    private Data data; //The data the batch belongs to
    private int startIndex; //The index of the first sample in the batch

    //WE ADDED ***
    private Data.Type type;
    private GPU gpuSender;

    public DataBatch (Data data, int startIndex) {
        this.data=data;
        this.startIndex=startIndex;
    }
}

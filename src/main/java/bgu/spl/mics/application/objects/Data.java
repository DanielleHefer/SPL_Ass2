package bgu.spl.mics.application.objects;

/**
 * Passive object representing a data used by a model.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class Data {
    /**
     * Enum representing the Data type.
     */
    public enum Type {
        Images, Text, Tabular
    }

    private Type type;
    //The number of samples which the gpu has processed for training, maybe use atomic here????
    private int processed;
    private int size;
    private Object lockProcessed;

    public Data (Type type, int size){
        this.type=type;
        this.size=size;
        processed=0;
        lockProcessed = new Object();
    }

    //Function for testing and for our use******
    public int getSize(){
        return size;
    }

    public Data.Type getType(){
        return type;
    }

    public void increaseProcessed(){
        synchronized (lockProcessed) {
            processed++;
        }
    }
}

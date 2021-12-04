package bgu.spl.mics.application.objects;

/**
 * Passive object representing a Deep Learning model.
 * Add all the fields described in the assignment as private fields.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class Model {

    enum Status {PreTrained, Training, Trained, Tested}
    enum Result {None, Good, Bad};

    private String name;
    private Data data;
    private Student student;
    private Status status=Status.PreTrained;
    private Result result; //None for a model not in status tested

    //Function for testing ******
    public void setData(Data data){}

    //Function for testing ******
    public Data getData() {return data;}

    //Function for testing ******
    public void setStatus(Status status) {this.status = status;}

    //Function for testing ******
    public Status getStatus() {
        return status;
    }

    //Function for testing ******
    public Result getResult() {
        return result;
    }

}

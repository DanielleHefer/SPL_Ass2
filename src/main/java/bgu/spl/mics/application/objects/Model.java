package bgu.spl.mics.application.objects;

/**
 * Passive object representing a Deep Learning model.
 * Add all the fields described in the assignment as private fields.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class Model {

    public enum Status {PreTrained, Training, Trained, Tested}
    public enum Result {None, Good, Bad};

    private String name;
    private Data data;
    private Student student;
    private Status status;
    private Result result; //None for a model not in status tested

    //For Testing*****
    public Model(){
        status=Status.PreTrained;
        result=Result.None;
    }

    public Model(String name, Data data, Student student) {
        this.name=name;
        this.data=data;
        this.student=student;
        status=Status.PreTrained;
        result=Result.None;
    }

    //Function for testing ******
    public void setData(Data data){
        this.data=data;
    }

    //Function for testing ******
    public Data getData() {
        return data;
    }

    //Function for testing and for our use******
    public void setStatus(Status desiredStatus) {
        this.status = desiredStatus;
    }

    //Function for testing ******
    public Status getStatus() {
        return status;
    }

    //Function for testing ******
    public Result getResult() {
        return result;
    }

    public void setResult(Result desiredResult){
        this.result=desiredResult;
    }

    public String getName(){
        return this.name;
    }

    public Student getStudent() {
        return this.student;
    }


}

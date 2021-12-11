package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;
import bgu.spl.mics.application.objects.Model;
import bgu.spl.mics.application.objects.Student;

public class TestModelEvent<T> implements Event<T> {

    private Model model; //The model we are currently testing
    private Student.Degree studentDegree;

    public TestModelEvent(Model model){
        this.model=model;
        studentDegree = model.getStudent().getStatus();
    }

    public Model getModel() {
        return model;
    }

    public Student.Degree getStudentDegree() {
        return studentDegree;
    }
}

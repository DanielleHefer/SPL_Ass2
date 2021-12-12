package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.*;
import bgu.spl.mics.application.objects.Model;
import bgu.spl.mics.application.objects.Student;

import javax.jws.WebParam;
import java.util.LinkedList;
import java.util.Vector;

/**
 * Student is responsible for sending the {@link TrainModelEvent},
 * {@link TestModelEvent} and {@link PublishResultsEvent}.
 * In addition, it must sign up for the conference publication broadcasts.
 * This class may not hold references for objects which it is not responsible for.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class StudentService extends MicroService {

    Student student;
    LinkedList<TrainModelEvent> trainModelEvents;

    //In the main file, after creating a student, call setStudentModels(Vector models) and then create a student service.
    //Set in main file: StudentService(student.getName(), student) so we will call the constructor with that
    public StudentService(String name, Student student) {
        super(name);
        this.student = student;
        createTrainModelEvents(student.getStudentModels());
    }

    public void createTrainModelEvents(Vector<Model> models) {
        for (Model m : models) {
            trainModelEvents.add(new TrainModelEvent(m));
        }
    }

    @Override
    protected void initialize() {

        //update this function after implementing time service -  *********************
        //we think that the only use of ticks in student will be for termination ************
        super.subscribeBroadcast(TickBroadcast.class, tick-> {
            if(tick==null) {
                terminate();
            }
            //CHECKING FUTURES:
            // check each tick if there is a future available - maybe with executors *********
            // if self management - for loop in which we will check for each future if it's done,
            // and if it does act according to its status (send test/publish event)
            //SORTING IN MESSAGEBUS & SENDING ORDER BETWEEN STUDENTS:
            //think of options - currently the models in each student are sorted, but if all the
            //students will send all the models at once we lost the advantage of the sorting.
            //maybe sending from the students in round-rubin manner, maybe sort in messagebus.....
        });

        super.subscribeBroadcast(PublishConferenceBroadcast.class, conference -> {
            student.readPublishes(conference.getModelsNames());
        });

    }
}

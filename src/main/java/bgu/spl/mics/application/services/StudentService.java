package bgu.spl.mics.application.services;

import bgu.spl.mics.Future;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.*;
import bgu.spl.mics.application.objects.Model;
import bgu.spl.mics.application.objects.Student;
import com.sun.org.apache.xpath.internal.operations.Mod;

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

    private Student student;
    private LinkedList<TrainModelEvent> trainModelEvents;
    private LinkedList<Future<Model>> futures;

    //In the main file, after creating a student, call setStudentModels(Vector models) and then create a student service.
    //Set in main file: StudentService(student.getName(), student) so we will call the constructor with that
    public StudentService(String name, Student student) {
        super(name);
        this.student = student;
        futures = new LinkedList<>();
        createTrainModelEvents(student.getStudentModels());
    }

    public void createTrainModelEvents(Vector<Model> models) {
        for (Model m : models) {
            trainModelEvents.add(new TrainModelEvent(m));
        }
    }

    public void sendTrainModelEvents() {
        for (TrainModelEvent e : trainModelEvents) {
            futures.add(super.sendEvent(e));
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

            for (Future<Model> future : futures) {
                if(future.isDone()) {
                    Model model = future.get();
                    if(model.getStatus()== Model.Status.Trained) {
                        Future<Model> modelFuture = super.sendEvent(new TestModelEvent<>(model));
                        futures.add(modelFuture);
                    }
                    else if (model.getStatus()== Model.Status.Tested) {
                        super.sendEvent(new PublishResultsEvent<>(model));
                    }
                    futures.remove(future);
                }
            }
        });

        super.subscribeBroadcast(PublishConferenceBroadcast.class, conference -> {
            student.readPublishes(conference.getModelsNames());
        });


        //SORTING IN MESSAGEBUS & SENDING ORDER BETWEEN STUDENTS:
        //think of options - currently the models in each student are sorted, but if all the
        //students will send all the models at once we lost the advantage of the sorting.
        //maybe sending from the students in round-rubin manner, maybe sort in messagebus.....

        //We will probably change its location and implementation *************
        //so we organize the order of sending events between students efficiently ***********
        sendTrainModelEvents();
    }
}

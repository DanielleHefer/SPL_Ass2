package bgu.spl.mics.application.services;

import bgu.spl.mics.Future;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.*;
import bgu.spl.mics.application.objects.Model;
import bgu.spl.mics.application.objects.Student;
import com.sun.org.apache.xpath.internal.operations.Mod;

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

    private Future<Model> future;
    private boolean firstWasSent;
    private int currTick;

    //In the main file, after creating a student, call setStudentModels(Vector models) and then create a student service.
    //Set in main file: StudentService(student.getName(), student) so we will call the constructor with that
    public StudentService(String name, Student student) {
        super(name);
        this.student = student;
        future = null;
        firstWasSent=false;
        trainModelEvents = new LinkedList<>();
        currTick=0;
        createTrainModelEvents(student.getStudentModels());
    }

    public void createTrainModelEvents(Vector<Model> models) {
        for (Model m : models) {
            TrainModelEvent model = new TrainModelEvent(m);
            trainModelEvents.add(model);
        }
    }

    public void sendTrainModelEvents() {
        if(!trainModelEvents.isEmpty()) {
            TrainModelEvent e = trainModelEvents.pollFirst();

            //%%%%%%%%%%%%%%%%%%%%%
            System.out.println("Thread "+Thread.currentThread().getId()+" Student send Train Model "+ Thread.currentThread().getName()+" - Model - "+e.getModel().getName()
                    +" - tick "+currTick);

            future = super.sendEvent(e);
        }
    }


    public void registration(){
        messageBus.register(this);

        super.subscribeBroadcast(TickBroadcast.class, tick-> {
            if(tick.getCurrTick()==null) {
                terminate();
            }
            else {
                currTick=tick.getCurrTick();
                if(!firstWasSent) {
                    sendTrainModelEvents();
                    firstWasSent=true;
                }

                //Student is holding a future that represent a model being trained/tested
                else if (future!=null && future.isDone()) {
                    Model model = future.get();
                    if (model.getStatus() == Model.Status.Trained) {
                        future = super.sendEvent(new TestModelEvent<>(model));

                        //%%%%%%%%%%%%%%%%%%%%%
                        System.out.println("Thread "+Thread.currentThread().getId()+" Student send Test Model "+ Thread.currentThread().getName()+" - Model - "+model.getName()
                                +" - tick "+currTick);

                    } else if (model.getStatus() == Model.Status.Tested) {
                        super.sendEvent(new PublishResultsEvent<>(model));

                        //%%%%%%%%%%%%%%%%%%%%%
                        System.out.println("Thread "+Thread.currentThread().getId()+" Student send Publish Model "+ Thread.currentThread().getName()+" - Model - "+model.getName()
                                +" - tick "+currTick);

                        future = null;
                        sendTrainModelEvents();
                    }
                }
            }
        });

        super.subscribeBroadcast(PublishConferenceBroadcast.class, conference -> {
            student.readPublishes(conference.getModelsNames());
        });
    }

    @Override
    protected void initialize() {
//        messageBus.register(this);
//
//        super.subscribeBroadcast(TickBroadcast.class, tick-> {
//            if(tick.getCurrTick()==null) {
//                terminate();
//            }
//            else {
//                if(!firstWasSent) {
//                    sendTrainModelEvents();
//                    firstWasSent=true;
//                }
//
//                else if (future!=null && future.isDone()) {
//                    Model model = future.get();
//                    if (model.getStatus() == Model.Status.Trained) {
//                        future = super.sendEvent(new TestModelEvent<>(model));
//                    } else if (model.getStatus() == Model.Status.Tested) {
//                        super.sendEvent(new PublishResultsEvent<>(model));
//                        future = null;
//                        sendTrainModelEvents();
//                    }
//                }
//            }
//        });
//
//        super.subscribeBroadcast(PublishConferenceBroadcast.class, conference -> {
//            student.readPublishes(conference.getModelsNames());
//        });
//
    }
}

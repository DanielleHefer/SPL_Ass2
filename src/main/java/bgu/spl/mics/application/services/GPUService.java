package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.TestModelEvent;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.messages.TrainModelEvent;
import bgu.spl.mics.application.objects.GPU;
import bgu.spl.mics.application.objects.Model;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * GPU service is responsible for handling the
 * {@link TrainModelEvent} and {@link TestModelEvent},
 * This class may not hold references for objects which it is not responsible for.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class GPUService extends MicroService {

    private GPU gpu;

    public GPUService(String name, GPU gpu) {
        super(name);
        this.gpu = gpu;
    }

    public void registration(){
        messageBus.register(this);

        super.subscribeBroadcast(TickBroadcast.class, tick ->{
            if (tick.getCurrTick() == null) {
                terminate();
            }

            else {
                gpu.setCurrTick(tick.getCurrTick());
                if(gpu.isGPUAvailable()) {
                    while (!gpu.getInnerTestQueue().isEmpty()) {
                        TestModelEvent testEvent = gpu.getInnerTestQueue().poll();
                        gpu.setModel(testEvent.getModel());
                        gpu.testProcess();
                        super.complete(testEvent, testEvent.getModel());
                        gpu.resetGPU();
                    }

                    if (!gpu.getInnerTrainQueue().isEmpty()) {
                        TrainModelEvent trainEvent = gpu.getInnerTrainQueue().poll();
                        trainEvent.getModel().setStatus(Model.Status.Training);
                        gpu.setCurrTrainEvent(trainEvent);
                        gpu.setModel(trainEvent.getModel());
                        gpu.splitToBatches();
                        for (int i=0; i<gpu.getVRAMLimitation() && i<gpu.getDataBatches().size(); i++) {
                            gpu.sendBatchToCluster();
                        }
                    }
                }
                //GPU is currently training an event
                else{

                    //The GPU isn't processing a dataBatch now
                    if(gpu.getCurrDataBatch()==null) {

                        //Is there a batch that returned from CPU
                        if(gpu.getVRAM().size()>0) {
                            gpu.pollFromVRAM();
                        }

                        //If gpu.getVRAM().size()==0 there are 2 options:
                        // 1. The CPU processing our batches (do nothing)
                        // 2. We finished:

                        //Check if the GPU is done processing all the data batches
                        else if (gpu.getBatchesAmountToProcess()==0) {
                            gpu.completeModel();
                            super.complete(gpu.getCurrTrainEvent(), gpu.getModel());
                            gpu.setModel(null);
                            gpu.setCurrTrainEvent(null);
                        }
                    }

                    //The GPU is currently processing a data batch
                    else {

                        //Check if the GPU is done processing the current data batch
                        if (gpu.getCurrTick()-gpu.getStartTick()>gpu.getProcessTick()) {
                            gpu.completeDataBatch();

                            //Check if the GPU is done processing all the data batches
                            if (gpu.getBatchesAmountToProcess()==0) {
                                gpu.completeModel();
                                super.complete(gpu.getCurrTrainEvent(), gpu.getModel());
                                gpu.setModel(null);
                                gpu.setCurrTrainEvent(null);
                            }

                            //Is there a batch that returned from CPU
                            else if(gpu.getVRAM().size()>0) {
                                gpu.pollFromVRAM();
                            }
                        }
                    }
                }
            }
        });

        super.subscribeEvent(TrainModelEvent.class, trainEvent -> {
            gpu.pushInnerTrainQueue(trainEvent);
        });

        super.subscribeEvent(TestModelEvent.class, testEvent -> {
            gpu.pushInnerTestQueue(testEvent);
        });
    }

    @Override
    protected void initialize() {

//        messageBus.register(this);
//
//        super.subscribeBroadcast(TickBroadcast.class, tick ->{
//            if (tick.getCurrTick() == null) {
//                terminate();
//            }
//
//            else {
//                gpu.setCurrTick(tick.getCurrTick());
//                if(gpu.isGPUAvailable()) {
//                    while (!gpu.getInnerTestQueue().isEmpty()) {
//                        TestModelEvent testEvent = gpu.getInnerTestQueue().poll();
//                        gpu.setModel(testEvent.getModel());
//                        gpu.testProcess();
//                        super.complete(testEvent, testEvent.getModel());
//                        gpu.resetGPU();
//                    }
//
//                    if (!gpu.getInnerTrainQueue().isEmpty()) {
//                        TrainModelEvent trainEvent = gpu.getInnerTrainQueue().poll();
//                        trainEvent.getModel().setStatus(Model.Status.Training);
//                        gpu.setCurrTrainEvent(trainEvent);
//                        gpu.setModel(trainEvent.getModel());
//                        gpu.splitToBatches();
//                        for (int i=0; i<gpu.getVRAMLimitation() && i<gpu.getDataBatches().size(); i++) {
//                            gpu.sendBatchToCluster();
//                        }
//                    }
//                }
//                //GPU is currently training an event
//                else{
//
//                    //The GPU isn't processing a dataBatch now
//                    if(gpu.getCurrDataBatch()==null) {
//
//                        //Is there a batch that returned from CPU
//                        if(gpu.getVRAM().size()>0) {
//                            gpu.pollFromVRAM();
//                        }
//
//                        //If gpu.getVRAM().size()==0 there are 2 options:
//                        // 1. The CPU processing our batches (do nothing)
//                        // 2. We finished:
//
//                        //Check if the GPU is done processing all the data batches
//                        else if (gpu.getBatchesAmountToProcess()==0) {
//                            gpu.completeModel();
//                            super.complete(gpu.getCurrTrainEvent(), gpu.getModel());
//                            gpu.setModel(null);
//                            gpu.setCurrTrainEvent(null);
//                        }
//                    }
//
//                    //The GPU is currently processing a data batch
//                    else {
//
//                        //Check if the GPU is done processing the current data batch
//                        if (gpu.getCurrTick()-gpu.getStartTick()>gpu.getProcessTick()) {
//                            gpu.completeDataBatch();
//
//                            //Check if the GPU is done processing all the data batches
//                            if (gpu.getBatchesAmountToProcess()==0) {
//                                gpu.completeModel();
//                                super.complete(gpu.getCurrTrainEvent(), gpu.getModel());
//                                gpu.setModel(null);
//                                gpu.setCurrTrainEvent(null);
//                            }
//
//                            //Is there a batch that returned from CPU
//                            else if(gpu.getVRAM().size()>0) {
//                                gpu.pollFromVRAM();
//                            }
//                        }
//                    }
//                }
//            }
//        });
//
//        super.subscribeEvent(TrainModelEvent.class, trainEvent -> {
//            gpu.pushInnerTrainQueue(trainEvent);
//        });
//
//        super.subscribeEvent(TestModelEvent.class, testEvent -> {
//            gpu.pushInnerTestQueue(testEvent);
//        });
    }
}

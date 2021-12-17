package bgu.spl.mics.application.services;

import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.CPU;

/**
 * CPU service is responsible for handling the {@link}.
 * This class may not hold references for objects which it is not responsible for.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class CPUService extends MicroService {

    private CPU cpu;

    public CPUService(String name, CPU cpu) {
        super(name);
        this.cpu=cpu;
    }

    public void registration(){
        messageBus.register(this);

        super.subscribeBroadcast(TickBroadcast.class, tick ->{

            if (tick.getCurrTick() == null) {
                terminate();
            }

            else{
                cpu.setCurrTick(tick.getCurrTick());
                //CPU is not currently working on a batch
                if (cpu.getCurrDataBatch()==null) {
                    if(!cpu.getInnerQueue().isEmpty()) {

                        //%%%%%%%%%%%%%%%%%%%%%%%%%%
                        System.out.println("CPU take batch from queue "+ Thread.currentThread().getName()+" - tick "+tick.getCurrTick());

                        cpu.takeBatchFromQueue();
                    }
                }

                //CPU is currently processing a batch
                else {
                    //CPU is done processing the curr DataBatch
                    if (cpu.getCurrTick()-cpu.getStartTick()>cpu.getProcessTick()){
                        cpu.completeBatch();

                        //%%%%%%%%%%%%%%%%%%%%%%%%%%
                        System.out.println("CPU complete batch "+ Thread.currentThread().getName()+" - tick "+tick.getCurrTick());

                        if(!cpu.getInnerQueue().isEmpty()) {
                            cpu.takeBatchFromQueue();

                            //%%%%%%%%%%%%%%%%%%%%%%%%%%
                            System.out.println("CPU take batch from queue "+ Thread.currentThread().getName()+" - tick "+tick.getCurrTick());
                        }
                    }
                }
            }
        });

    }

    @Override
    protected void initialize() {

//        messageBus.register(this);
//
//        super.subscribeBroadcast(TickBroadcast.class, tick ->{
//
//            if (tick.getCurrTick() == null) {
//                terminate();
//            }
//
//            else{
//                cpu.setCurrTick(tick.getCurrTick());
//                //CPU is not currently working on a batch
//                if (cpu.getCurrDataBatch()==null) {
//                    if(!cpu.getInnerQueue().isEmpty()) {
//                        cpu.takeBatchFromQueue();
//                    }
//                }
//
//                //CPU is currently processing a batch
//                else {
//                    //CPU is done processing the curr DataBatch
//                    if (cpu.getCurrTick()-cpu.getStartTick()>cpu.getProcessTick()){
//                        cpu.completeBatch();
//                        if(!cpu.getInnerQueue().isEmpty()) {
//                            cpu.takeBatchFromQueue();
//                        }
//                    }
//                }
//            }
//        });

    }
}

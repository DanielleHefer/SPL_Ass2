package bgu.spl.mics.application.objects;

import bgu.spl.mics.Message;
import bgu.spl.mics.application.messages.TestModelEvent;
import bgu.spl.mics.application.messages.TrainModelEvent;

import java.util.Collection;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Passive object representing a single GPU.
 * Add all the fields described in the assignment as private fields.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class GPU {

    /**
     * @INV:
     *      0<=getCurrVRAMSize()<=getVRAMLimitation()
     *      getBatchesAmountToProcess()>=0
     *      getCurrTick()>=0
     *      getStartTick()>=-1
     *      getProcessTick()==1 iff getType()==RTX3090
     *      getProcessTick()==2 iff getType()==RTX2080
     *      getProcessTick()==4 iff getType()==GTX1080
     */

    /**
     * Enum representing the type of the GPU.
     */
    public enum Type {RTX3090, RTX2080, GTX1080}

    private Type type;
    private Model model; // The model the gpu currently working on (null for none) *****
    private Cluster cluster;

    //WE ADDED *****
    private int currVRAMSize; //number of batches that processed by cpu, and waiting to be processed by gpu
    private int VRAMLimitation; //3090=32 processed batches, 2080=16 processed batches, 1080=8 processed batches
    private int processTick; //3090=1 tick, 2080=2 ticks, 1080=4 ticks (process after the cpu returned)
    private LinkedList<DataBatch> dataBatches; //data batches on the disk, preprocessed by CPU
    private int batchesAmountToProcess; //initialized to be model.data.size/1000,
                                        // we will decrease until getting to 0, and then the gpu is done processing

    private DataBatch currDataBatch; //The data batch the gpu is currently processing
    private TrainModelEvent currTrainEvent;

    //Maybe not synchronized*********
    private LinkedBlockingQueue<DataBatch> VRAM;

    //Probably be changed - maybe a queue for train and queue for test*********
    private LinkedList<TestModelEvent> innerTestQueue;
    private PriorityQueue <TrainModelEvent> innerTrainQueue;

    private int currTick; //might be changed later (only for testing purpose)
    private int startTick;

    public GPU (Type type) {
        //Decided for now that this is the only constructor
        this.type=type;
        cluster = Cluster.getInstance();
        batchesAmountToProcess = 0;
        dataBatches = new LinkedList<>();
        startTick = -1;
        currTick = -1;
        currVRAMSize = 0;

        innerTestQueue = new LinkedList<>();
        innerTrainQueue = new PriorityQueue<TrainModelEvent>((a,b) ->
            a.getModelSize()*a.getModel().getData().typeToNum() - b.getModelSize()*b.getModel().getData().typeToNum());

        model = null;
        currDataBatch = null;
        currTrainEvent = null;
        VRAM = new LinkedBlockingQueue<>();

        switch (type) {
            case RTX3090: {
                VRAMLimitation = 32;
                processTick = 1;
                break;
            }
            case RTX2080: {
                VRAMLimitation = 16;
                processTick = 2;
                break;
            }

            case GTX1080:  {
                VRAMLimitation = 8;
                processTick = 4;
                break;
            }
        }

    }

    /**
     * @PRE:
     * 	 	none
     * @POST:
     * 	 	none
     * (basic query)
     */
    public int getCurrVRAMSize(){
        return currVRAMSize;
    }

    /**
     * @PRE:
     * 	 	none
     * @POST:
     * 	 	none
     * (basic query)
     */
    public int getVRAMLimitation() {
        return VRAMLimitation;
    }

    public Type getType() {
        return type;
    }

    /**
     * @PRE:
     * 	 	none
     * @POST:
     * 	 	none
     * (basic query)
     */
    public int getCurrTick() { return currTick;}

    public void setCurrTick(int currTime) {
        currTick=currTime;
    }

    /**
     * @PRE:
     * 	 	none
     * @POST:
     * 	 	none
     * (basic query)
     */
    public int getStartTick() { return startTick;}

    public void setStartTick(int tick) {
        startTick=tick;
    }

    /**
     * @PRE:
     * 	 	none
     * @POST:
     * 	 	none
     * (basic query)
     */
    public int getBatchesAmountToProcess() { return batchesAmountToProcess;}

    public void decreaseBatchesAmountToProcess() {
        batchesAmountToProcess--;
    }

    /**
     * @PRE:
     * 	 	none
     * @POST:
     * 	 	none
     * (basic query)
     */
    public Model getModel() {return model;}

    /**
     * @PRE:
     * 	 	none
     * @POST:
     * 	 	none
     * (basic query)
     */
    public int getProcessTick() {return processTick;}

    /**
     * @PRE:
     * 	 	none
     * @POST:
     * 	 	none
     * (basic query)
     */
    public Collection<DataBatch> getDataBatches(){
        return dataBatches;
    }

    public LinkedList<TestModelEvent> getInnerTestQueue () {
        return innerTestQueue;
    }

    public PriorityQueue<TrainModelEvent> getInnerTrainQueue () {
        return innerTrainQueue;
    }

    public TestModelEvent popInnerTestQueue() {
        return innerTestQueue.poll();
    }

    public void pushInnerTestQueue(TestModelEvent m) {
        innerTestQueue.offer(m);
    }

    public TrainModelEvent popInnerTrainQueue() {
        return innerTrainQueue.poll();
    }

    public void pushInnerTrainQueue(TrainModelEvent m) {
        innerTrainQueue.offer(m);
    }

    public DataBatch getCurrDataBatch() {
        return currDataBatch;
    }

    public void setCurrDataBatch (DataBatch db) {
        currDataBatch=db;
    }

    public LinkedBlockingQueue<DataBatch> getVRAM() {
        return VRAM;
    }

    public void setCurrTick (Integer tick) {
        this.currTick=tick;
    }

    public void pollFromVRAM() {
        currDataBatch =  VRAM.poll();
        startTick=currTick;
        currVRAMSize--;
        if(!dataBatches.isEmpty()) {
            sendBatchToCluster();
        }
    }

    public void pushToVRAM(DataBatch db) {
        VRAM.offer(db);
        currVRAMSize++;
    }

    public TrainModelEvent getCurrTrainEvent() {
        return currTrainEvent;
    }

    public void setCurrTrainEvent (TrainModelEvent e) {
        currTrainEvent = e;
    }

    /**
     * @PRE:
     *      model==null
     * @POST:
     *      model!=null
     */
    public void setModel(Model model) {
        this.model=model;
        if (model!=null)
            batchesAmountToProcess = this.model.getData().getSize()/1000;
    }


    public boolean isGPUAvailable() {
        return model==null;
    }


    /**
     * @PRE:
     *      dataBatches.size()==0
     *      batchesAmountToProcess==0
     * @POST:
     *      dataBatches.size()==(model.getData().getSize())/1000
     *      batchesAmountToProcess == (model.getData().getSize())/1000
     */
    //takes the model.data.getSize(), split by 1000 and create this amount of batches,
    //each batch get the start_index (0,1000,2000,...) and pushed to Collection<DataBatch>
    public void splitToBatches() {
        for (int i=0; i<batchesAmountToProcess; i++) {
            DataBatch db = new DataBatch(model.getData(), i * 1000, this);
            dataBatches.add(db);
        }
    }


    /**
     * @PRE:
     *      dataBatches.size()>0
     * @POST:
     *      dataBatches.size()==@PRE(dataBatches.size())-1
     */
    //(probably GPUService will check dataBatches.size()>0 and then activate this function)
    //send unprocessed batch to the cluster, cluster gives it to one of the cpu
    public void sendBatchToCluster(){
        DataBatch db = dataBatches.poll();
        cluster.sendUnprocessedBatch(db);
    }

    public Cluster getCluster() {
        return cluster;
    }

    /**
     * @PRE:
     *      batchesAmountToProcess > 0
     *      currVRAMSize < VRAMLimitation
     *
     * @POST:
     *      currVRAMSize = @PRE(currVRAMSize)+1
     */
    //increase to currVRAMSize by 1 for each added batch, here we throw away the batches
    //take batch from cluster only if currVRAMSize < VRAMLimitation
    //For Testing ******* and maybe our use
    public void getProcessedBatchFromCluster() {}

    /**
     * @PRE:
     *      none
     * @POST:
     *      if (@PRE(currVRAMSize)!=0 && currTick = startTick + processTick) :
     *              currVRAMSize = @PRE(currVRAMSize)-1 &&
     *              batchesAmountToProcess == @PRE(batchesAmountToProcess) - 1
     *      currTick = @PRE(currTick) + 1
     */
    //For Testing *******
    public void updateTick() {
        /*
        currTick++
        if(currVRAMSize > 0){
           if(currTick-start_tick > processTick){
             currVRAMSize--
             batchesAmountToProcess--
            }
         }
         */
    }

    /**
     * @PRE:
     *      model.getStatus()==Trained
     *      model.getResult()==none
     * @POST:
     *      model.getStatus()==Tested
     *      model.getResult()!=none
     */
    public void testProcess(){
        double grade = Math.random();
        if(model.getStudent().getStatus()==Student.Degree.MSc) {
            if(grade<0.6) {
                model.setResult(Model.Result.Good);
            }
            else
                model.setResult(Model.Result.Bad);
        }
        //PHD student
        else {
            if(grade<0.8) {
                model.setResult(Model.Result.Good);
            }
            else
                model.setResult(Model.Result.Bad);
        }
    }

    /**
     * @PRE:
     *      none
     * @POST:
     *      model==null
     *      currVRAMSize==0
     *      dataBatches.size()==0
     *      batchesAmountToProcess==0
     *      startTick==-1
     */
    public void resetGPU() {
        currVRAMSize=0;
        dataBatches.clear();
        VRAM.clear();
        batchesAmountToProcess=0;
        startTick=-1;
        currDataBatch=null;
    }

    public void completeModel() {
        model.setStatus(Model.Status.Trained);
        cluster.addModelName(model.getName());
        resetGPU();
    }

    public void completeDataBatch(){
        currDataBatch=null;
        startTick=-1;
        decreaseBatchesAmountToProcess();
        getModel().getData().increaseProcessed();
        cluster.increaseGPUTimeUnits(processTick);
    }
}
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

    private int GPUTimeUnits;

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
        GPUTimeUnits=0;

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

    /**
     * @PRE:
     * 	 	none
     * @POST:
     * 	 	none
     * (basic query)
     */
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

    /**
     * @PRE:
     * 	 	none
     * @POST:
     * 	 	getCurrTick()=currTime
     *
     */
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

    /**
     * @PRE:
     * 	 	none
     * @POST:
     * 	 	startTick=tick
     */
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

    /**
     * @PRE:
     * 	 	none
     * @POST:
     * 	 	getBatchesAmountToProcess()=@pre(getBatchesAmountToProcess())-1
     */
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

    /**
     * @PRE:
     * 	 	none
     * @POST:
     * 	 	none
     * (basic query)
     */
    public LinkedList<TestModelEvent> getInnerTestQueue () {
        return innerTestQueue;
    }

    /**
     * @PRE:
     * 	 	none
     * @POST:
     * 	 	none
     * (basic query)
     */
    public PriorityQueue<TrainModelEvent> getInnerTrainQueue () {
        return innerTrainQueue;
    }

    /**
     * @PRE:
     * 	 	innerTestQueue.size()>0
     * @POST:
     * 	 	innerTestQueue.size()=@pre(innerTestQueue.size())-1
     */
    public TestModelEvent popInnerTestQueue() {
        return innerTestQueue.poll();
    }

    /**
     * @PRE:
     * 	 	none
     * @POST:
     * 	 	innerTestQueue.size()=@pre(innerTestQueue.size())+1
     */
    public void pushInnerTestQueue(TestModelEvent m) {
        innerTestQueue.offer(m);
    }

    /**
     * @PRE:
     * 	 	innerTrainQueue.size()>0
     * @POST:
     * 	 	innerTrainQueue.size()=@pre(innerTrainQueue.size())-1
     */
    public TrainModelEvent popInnerTrainQueue() {
        return innerTrainQueue.poll();
    }

    /**
     * @PRE:
     * 	 	none
     * @POST:
     * 	 	innerTrainQueue.size()=@pre(innerTrainQueue.size())+1
     */
    public void pushInnerTrainQueue(TrainModelEvent m) {
        innerTrainQueue.offer(m);
    }

    /**
     * @PRE:
     * 	 	none
     * @POST:
     * 	 	none
     * (basic query)
     */
    public DataBatch getCurrDataBatch() {
        return currDataBatch;
    }

    /**
     * @PRE:
     * 	 	none
     * @POST:
     * 	 	currDataBatch=db
     */
    public void setCurrDataBatch (DataBatch db) {
        currDataBatch=db;
    }

    /**
     * @PRE:
     * 	 	none
     * @POST:
     * 	 	none
     * (basic query)
     */
    public LinkedBlockingQueue<DataBatch> getVRAM() {
        return VRAM;
    }

    /**
     * @PRE:
     * 	 	none
     * @POST:
     * 	 	currTick=tick
     */
    public void setCurrTick (Integer tick) {
        this.currTick=tick;
    }

    /**
     * @PRE:
     * 	    currVRAMSize>0
     * @POST:
     * 	 	startTick==currTick
     * 	 	currVRAMSize==@pre(currVRAMSize)-1
     */
    public void pollFromVRAM() throws InterruptedException {
        currDataBatch =  VRAM.take();
        startTick=currTick;
        currVRAMSize--;
        if(!dataBatches.isEmpty()) {
            sendBatchToCluster();
        }
    }

    /**
     * @PRE:
     * 	    none
     * @POST:
     * 	    currVRAMSize==@pre(currVRAMSize)+1
     */
    public void pushToVRAM(DataBatch db) {
        VRAM.offer(db);
        currVRAMSize++;
    }

    /**
     * @PRE:
     * 	    none
     * @POST:
     * 	    none
     * 	    (basic query)
     */
    public TrainModelEvent getCurrTrainEvent() {
        return currTrainEvent;
    }

    /**
     * @PRE:
     * 	    none
     * @POST:
     * 	    currTrainEvent==e
     */
    public void setCurrTrainEvent (TrainModelEvent e) {
        currTrainEvent = e;
    }

    /**
     * @PRE:
     *      this.model==null
     * @POST:
     *      this.model==model
     */
    public void setModel(Model model) {
        this.model=model;
        if (model!=null)
            batchesAmountToProcess = this.model.getData().getSize()/1000;
    }

    /**
     * @PRE:
     * 	    none
     * @POST:
     * 	    none
     * 	    (basic query)
     */
    public boolean isGPUAvailable() {
        return model==null;
    }


    /**
     * @PRE:
     *      dataBatches.size()==0
     * @POST:
     *      dataBatches.size()==(model.getData().getSize())/1000
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

    /**
     * @PRE:
     * 	    none
     * @POST:
     * 	    none
     * 	    (basic query)
     */
    public Cluster getCluster() {
        return cluster;
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
                model.setStatus(Model.Status.Tested);
            }
            else {
                model.setResult(Model.Result.Bad);
                model.setStatus(Model.Status.Tested);
            }
        }
        //PHD student
        else {
            if(grade<0.8) {
                model.setResult(Model.Result.Good);
                model.setStatus(Model.Status.Tested);
            }
            else {
                model.setResult(Model.Result.Bad);
                model.setStatus(Model.Status.Tested);
            }
        }

    }

    /**
     * @PRE:
     *      model!=null
     * @POST:
     *      currVRAMSize==0
     *      dataBatches.size()==0
     *      batchesAmountToProcess==0
     *      startTick==-1
     *      currDataBatch==null
     */
    public void completeModel() {
        model.setStatus(Model.Status.Trained);
        cluster.addModelName(model.getName());
        resetGPU();
    }

    //Tested as part of complete model
    public void resetGPU() {
        currVRAMSize=0;
        dataBatches.clear();
        VRAM.clear();
        batchesAmountToProcess=0;
        startTick=-1;
        currDataBatch=null;
    }

    /**
     * @PRE:
     *      currDataBatch!=null
     * @POST:
     *      currVRAMSize==0
     *      dataBatches.size()==0
     *      batchesAmountToProcess==@pre(batchesAmountToProcess)-1
     *      startTick==-1
     *      currDataBatch==null
     *      GPUTimeUnits==@pre(GPUTimeUnits)+processTick
     */
    public void completeDataBatch(){
        currDataBatch=null;
        startTick=-1;
        decreaseBatchesAmountToProcess();
        getModel().getData().increaseProcessed();
        GPUTimeUnits+=processTick;
    }

    /**
     * @PRE:
     * 	    none
     * @POST:
     * 	    none
     * 	    (basic query)
     */
    public int getGPUTimeUnits(){
        return GPUTimeUnits;
    }
}
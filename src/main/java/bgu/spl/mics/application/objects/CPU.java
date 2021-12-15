package bgu.spl.mics.application.objects;

import java.util.Collection;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


/**
 * Passive object representing a single CPU.
 * Add all the fields described in the assignment as private fields.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class CPU {

    /**
     * @INV:
     *      getData().size()>=0
     *      getCores()>0
     *      getCurrTick()>=0
     *      getStartTick()>=-1
     *      getDataTypeNeededTicks()==1 iff getCurrDataBatch().getType()==Tabular
     *      getDataTypeNeededTicks()==2 iff getCurrDataBatch().getType()==Text
     *      getDataTypeNeededTicks()==4 iff getCurrDataBatch().getType()==Images
     */

    private int cores;
    //we trust the cluster to push new DataBatches into this collection,
    // there is no function to check a queue in cluster!! this is the cpu queue! (gpu will have a queue in cluster)
    private Collection<DataBatch> data; //For Testing *****
    private Cluster cluster;

    private int currTick; //For Testing*****
    private int startTick; //will be the start tick of processing this batch  //For Testing*****
    private DataBatch currDataBatch; //will be null if no dataBatch is processed right now  //For Testing*****
    private int dataTypeNeededTicks;  //For Testing*****

    private int processTick;
    private LinkedBlockingQueue<DataBatch> innerQueue;
    private int loadFactor;

    public CPU (int cores) {
        this.cores=cores;
        innerQueue = new LinkedBlockingQueue<>();
        data = innerQueue;
        cluster = Cluster.getInstance();
        currTick=-1;
        startTick=-1;
        currDataBatch=null;
        dataTypeNeededTicks=-1;
        processTick=-1;
        loadFactor = (32/cores);//Maybe multiply by 2 in order to simulate the average OR calculate the real average OR neither
    }

    /**
     * @PRE:
     * 	 	none
     * @POST:
     * 	 	none
     * (basic query)
     */
    public int getCores() { return cores;}

    /**
     * @PRE:
     * 	 	none
     * @POST:
     * 	 	none
     * (basic query)
     */
    public Collection<DataBatch> getData () { return data;}

    /**
     * @PRE:
     * 	 	none
     * @POST:
     * 	 	none
     * (basic query)
     */
    public DataBatch getCurrDataBatch() {return currDataBatch;}

    /**
     * @PRE:
     * 	 	none
     * @POST:
     * 	 	none
     * (basic query)
     */
    public int getCurrTick() {return currTick;}

    /**
     * @PRE:
     * 	 	none
     * @POST:
     * 	 	none
     * (basic query)
     */
    public int getStartTick() {return startTick;}

    /**
     * @PRE:
     * 	 	none
     * @POST:
     * 	 	none
     * (basic query)
     */
    public int getDataTypeNeededTicks() {return dataTypeNeededTicks;}

    public int getProcessTick() {
        return processTick;
    }

    public BlockingQueue<DataBatch> getInnerQueue(){
        return innerQueue;
    }

    public void pushToInnerQueue(DataBatch db) {
        innerQueue.offer(db);
        loadFactor+=(32/cores)*db.getTicksForType();
    }

    public void takeNextBatchFromInnerQueue() {
        currDataBatch = innerQueue.poll();
    }

    /**
     * @PRE:
     *      none
     * @POST:
     *      data.size()==@PRE(data.size())+1
     */
    //Function for testing ****
    public void addDataBatchToCollection (DataBatch batch){
        //TODO - implement so the tests will work
    }

    public int getLoadFactor() {
        return loadFactor;
    }

    public void updateLoadFactor(int lf) {
        loadFactor+=lf;
    }

    public void setCurrTick (Integer tick) {
        this.currTick=tick;
    }

    /**
     * @PRE:
     *      none
     * @POST:
     *      data.size()==0
     */
    //Function for testing ****
    public void clearDataCollection (){
        //TODO - implement so the tests will work
    }

    /**
     * @PRE:
     *      currDataBatch!=null
     *      dataTypeNeededTicks!=-1
     *      startTick!=-1
     * @POST:
     *      currDataBatch==null
     *      dataTypeNeededTicks==-1
     *      startTick==-1
     */
    public void sendProcessedBatch() {
        //cluster.returnToGPU(currDataBatch)
        //currDataBatch=null
        //dataTypeNeededTicks= -1
        //startTick = -1
    }

    /**
     * @PRE:
     *      none
     * @POST:
     *      if (@PRE(data.size())>0) so: data.size()==@PRE(data.size())-1
     */
    public void takeNextBatchFromCollection() {
        //will be called even if the collection's size == 0
        //we will take batch only if the collection's size > 0
        //this function will be called at updateTick()
        takeBatchFromQueue();
    }

    /**
     * @PRE:
     *      none
     * @POST:
     *     currTick = @PRE(currTick) + 1
     *     if (@PRE(currDataBatch) != null && (@PRE(currTick)-startTick > (32/cores)*dataTypeNeededTicks)) :
     *         if(@PRE(data.size())==0) :
     *              currDataBatch==null
     *              dataTypeNeededTicks==-1
     *              startTick==-1
     *         else :
     *              currDataBatch!=null
     *              dataTypeNeededTicks!=-1
     *              startTick!=-1
     *      else if: (@PRE(currDataBatch) == null && @PRE(data.size())>0) :
     *              currDataBatch!=null
     *              dataTypeNeededTicks!=-1
     *              startTick!=-1
     */
    public void updateTick() {
        /*
        currTick++
        if(currDataBatch != null){
           if(currTick-startTick > (32/cores)*dataTypeNeededTicks) {
             sendProcessedBatch()
             takeNextBatchFromCollection()
            }
         }
         else
            takeNextBatchFromCollection()
         */
    }

    public void takeBatchFromQueue() {
        currDataBatch = innerQueue.poll();
        startTick = currTick;
        dataTypeNeededTicks = currDataBatch.getTicksForType();
        processTick = (32/cores)*dataTypeNeededTicks;
    }

    public void completeBatch() {
        cluster.increaseTotalDBProcessedCPU();
        cluster.increaseCPUTimeUnits(processTick);
        currDataBatch.setProcessedByCPU();
        currDataBatch.setTimeToProcessByCPU(processTick);
        cluster.addBatchToVRAM(currDataBatch);
        cluster.setLoadFactor(this,processTick);
        resetAfterBatch();
    }

    public void resetAfterBatch(){
        startTick=-1;
        currDataBatch=null;
        dataTypeNeededTicks=-1;
        processTick=-1;
    }
}

package bgu.spl.mics.application.objects;

import java.util.Collection;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;


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
    private Cluster cluster;

    private int currTick; //For Testing*****
    private int startTick; //will be the start tick of processing this batch  //For Testing*****
    private DataBatch currDataBatch; //will be null if no dataBatch is processed right now  //For Testing*****
    private int dataTypeNeededTicks;  //For Testing*****

    private int processTick;
    private LinkedBlockingQueue<DataBatch> innerQueue;
    private AtomicInteger loadFactor;
    private Object loadFactorLock;

    private int totalDBProcessed;
    private int CPUTimeUnits;

    public CPU (int cores) {
        this.cores=cores;
        innerQueue = new LinkedBlockingQueue<>();
        cluster = Cluster.getInstance();
        currTick=-1;
        startTick=-1;
        currDataBatch=null;
        dataTypeNeededTicks=-1;
        processTick=-1;
        loadFactor = new AtomicInteger(0);//Maybe multiply by 2 in order to simulate the average OR calculate the real average OR neither
        loadFactorLock = new Object();
        totalDBProcessed=0;
        CPUTimeUnits=0;
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


    public int getProcessTick() {
        return processTick;
    }

    public BlockingQueue<DataBatch> getInnerQueue(){
        return innerQueue;
    }

    public int getCPUTimeUnits () {
        return CPUTimeUnits;
    }

    public int getTotalDBProcessed(){
        return totalDBProcessed;
    }

    /**
     * @PRE:
     *      none
     * @POST:
     *      innerQueue.size()==@PRE(innerQueue.size())+1
     */

    public void pushToInnerQueue(DataBatch db) {
        innerQueue.add(db);
        updateLoadFactor((32/cores)*db.getTicksForType());
    }

    public int getLoadFactor() {
        return loadFactor.get();
    }

    public void updateLoadFactor(int lf) {
        loadFactor.addAndGet(lf);
    }

    public void setCurrTick (Integer tick) {
        this.currTick=tick;
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

    }

    /**
     * @PRE:
     *      innerQueue.size())>0
     *      dataTypeNeededTicks==-1
     * @POST:
     *      if (@PRE(innerQueue.size())>0) so: innerQueue.size()==@PRE(innerQueue.size())-1
     *      startTick == currTick
     *      dataTypeNeededTicks!=-1
     */
    public void takeBatchFromQueue() {
        currDataBatch = innerQueue.poll();
        startTick = currTick;
        dataTypeNeededTicks = currDataBatch.getTicksForType();
        processTick = (32/cores)*dataTypeNeededTicks;
    }

    /**
     * @PRE:
     *      currDataBatch!=null
     *      dataTypeNeededTicks!=-1
     *      startTick!=-1
     *      processTick!=-1;
     * @POST:
     *      totalDBProcessed=@pre(totalDBProcessed)+1
     *      CPUTimeUnits=@pre(CPUTimeUnits)+processTick
     *      currDataBatch==null
     *      dataTypeNeededTicks==-1
     *      startTick==-1
     *      processTick==-1;
     */
    public void completeBatch() {
        totalDBProcessed++;
        CPUTimeUnits+=processTick;
        currDataBatch.setProcessedByCPU();
        currDataBatch.setTimeToProcessByCPU(processTick);
        cluster.addBatchToVRAM(currDataBatch);
        updateLoadFactor(-processTick);
        resetAfterBatch();
    }

    public void resetAfterBatch(){
        startTick=-1;
        currDataBatch=null;
        dataTypeNeededTicks=-1;
        processTick=-1;
    }
}
package bgu.spl.mics.application.objects;

import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.BlockingQueue;


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

    private BlockingQueue<DataBatch> innerQueue;
    private double loadFactor;

    public CPU (int cores) {
        this.cores=cores;
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

    public BlockingQueue<DataBatch> getInnerQueue(){
        return innerQueue;
    }

    public void pushToInnerQueue(DataBatch db) {
        innerQueue.offer(db);
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

    public double getLoadFactor() {
        return loadFactor;
    }

    public void updateLoadFactor(double lf) {
        loadFactor+=lf;
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
}

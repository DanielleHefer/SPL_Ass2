package bgu.spl.mics.application.objects;

import java.util.Collection;


/**
 * Passive object representing a single CPU.
 * Add all the fields described in the assignment as private fields.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class CPU {


    private int currTick;
    private int endTick; //will be the start tick + the needed ticks for processing
    //we will check that the currTick<endTick, when equals - stop

    //public void setCurrTick(int i);

    public void process(Collection<DataBatch> data){}


}

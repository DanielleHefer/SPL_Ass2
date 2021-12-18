package bgu.spl.mics.application.objects;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.LinkedList;

import static org.junit.Assert.*;
import static bgu.spl.mics.application.objects.Data.Type.*;

public class CPUTest {

    CPU cpu = new CPU(32);
    DataBatch batch = new DataBatch(new Data(Tabular,1000),0);
    DataBatch batch2 = new DataBatch(new Data(Tabular,1000),0);


    @Test
    public void pushToInnerQueue() {
        int preDataSize = cpu.getInnerQueue().size();
        cpu.pushToInnerQueue(batch);
        assertTrue(preDataSize+1==cpu.getInnerQueue().size());
    }


    @Test
    public void takeBatchFromQueue() {
        cpu.pushToInnerQueue(batch);
        int preSize = cpu.getInnerQueue().size();
        assertTrue(preSize>0);
        assertTrue(cpu.getDataTypeNeededTicks()==-1);

        cpu.takeBatchFromQueue();

        assertTrue(preSize-1==cpu.getInnerQueue().size());
        assertTrue(cpu.getStartTick()==cpu.getCurrTick());
        assertTrue(cpu.getDataTypeNeededTicks()!=-1);
    }



    @Test
    public void completeBatch() {
        cpu.pushToInnerQueue(batch);
        cpu.setCurrTick(1);
        cpu.takeBatchFromQueue();
        assertTrue(cpu.getCurrDataBatch()!=null);
        assertTrue(cpu.getDataTypeNeededTicks()!=-1);
        assertTrue(cpu.getStartTick()!=-1);
        assertTrue(cpu.getProcessTick()!=-1);

        int totalDBProcessed = cpu.getTotalDBProcessed();
        int timeUnits = cpu.getCPUTimeUnits();
        int processTick = cpu.getProcessTick();

        cpu.completeBatchT();

        assertTrue(totalDBProcessed+1== cpu.getTotalDBProcessed());
        assertTrue(timeUnits+processTick == cpu.getCPUTimeUnits());
        assertTrue(cpu.getStartTick()==-1);
        assertTrue(cpu.getProcessTick()==-1);
    }

    @After
    public void tearDown() throws Exception {
        cpu = new CPU(32);
    }
}
//package bgu.spl.mics.application.objects;
//
//import org.junit.After;
//import org.junit.Before;
//import org.junit.Test;
//
//import static org.junit.Assert.*;
//import static bgu.spl.mics.application.objects.Data.Type.*;
//
//public class CPUTest {
//
//    CPU cpu = new CPU(32);
//    DataBatch batch = new DataBatch(new Data(Tabular,1000),0);
//    DataBatch batch2 = new DataBatch(new Data(Tabular,1000),0);
//
//    @Test
//    public void addDataBatchToCollection() {
//        int preDataSize = cpu.getData().size();
//        cpu.addDataBatchToCollection(batch);
//        assertTrue(preDataSize+1==cpu.getData().size());
//    }
//
//
//    @Test
//    public void sendProcessedBatch() {
//        cpu.addDataBatchToCollection(batch);
//        cpu.takeNextBatchFromCollection();
//        //pre
//        assertTrue(cpu.getCurrDataBatch()!=null);
//        assertTrue(cpu.getDataTypeNeededTicks()!=-1);
//        assertTrue(cpu.getStartTick()!=-1);
//        //process and send - preform in updateTick()
//        cpu.updateTick();
//        //post
//        assertTrue(cpu.getCurrDataBatch()==null);
//        assertTrue(cpu.getDataTypeNeededTicks()==-1);
//        assertTrue(cpu.getStartTick()==-1);
//    }
//
//    @Test
//    public void takeNextBatchFromCollection() {
//        //check the scenario where the data is empty
//        assertTrue(cpu.getData().size()==0);
//        cpu.takeNextBatchFromCollection();
//        assertTrue(cpu.getData().size()==0);
//
//        //check the scenario in which there is a DataBatch in data
//        cpu.addDataBatchToCollection(batch);
//        int preSize = cpu.getData().size();
//        cpu.takeNextBatchFromCollection();
//        assertTrue(preSize-1==cpu.getData().size());
//    }
//
//
//    @Test
//    public void updateTick() {
//        //check the tick update
//        int preTick = cpu.getCurrTick();
//        cpu.updateTick();
//        assertTrue(preTick+1==cpu.getCurrTick());
//
//        //check the scenario the currDataBatch is done being processed and data.size()>0
//        cpu.addDataBatchToCollection(batch);
//        cpu.addDataBatchToCollection(batch2);
//        cpu.takeNextBatchFromCollection();
//        cpu.updateTick();
//        assertTrue(cpu.getCurrDataBatch()!=null);
//        assertTrue(cpu.getDataTypeNeededTicks()!=-1);
//        assertTrue(cpu.getStartTick()!=-1);
//        cpu.clearDataCollection();
//
//        //check the scenario the currDataBatch is done being processed and data.size()==0
//        cpu.addDataBatchToCollection(batch);
//        cpu.takeNextBatchFromCollection();
//        cpu.updateTick();
//        assertTrue(cpu.getCurrDataBatch()==null);
//        assertTrue(cpu.getDataTypeNeededTicks()==-1);
//        assertTrue(cpu.getStartTick()==-1);
//        cpu.clearDataCollection();
//
//        //Check the scenario where currDataBatch==null and data.size()>0
//        assertTrue(cpu.getCurrDataBatch()==null);
//        cpu.addDataBatchToCollection(batch);
//        cpu.updateTick();
//        assertTrue(cpu.getCurrDataBatch()!=null);
//        assertTrue(cpu.getDataTypeNeededTicks()!=-1);
//        assertTrue(cpu.getStartTick()!=-1);
//    }
//
//    @Test
//    public void clearDataCollection() {
//        cpu.addDataBatchToCollection(batch);
//        cpu.clearDataCollection();
//        assertTrue(cpu.getData().size()==0);
//    }
//
//    @After
//    public void tearDown() throws Exception {
//        cpu.clearDataCollection();
//    }
//}
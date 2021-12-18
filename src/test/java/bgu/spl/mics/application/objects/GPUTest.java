package bgu.spl.mics.application.objects;

import bgu.spl.mics.application.messages.TestModelEvent;
import bgu.spl.mics.application.messages.TrainModelEvent;
import org.junit.After;
import org.junit.Test;

import java.util.LinkedList;

import static bgu.spl.mics.application.objects.GPU.Type.*;
import static bgu.spl.mics.application.objects.Data.Type.*;
import static bgu.spl.mics.application.objects.Model.Status.*;
import static bgu.spl.mics.application.objects.Model.Result.*;
import static org.junit.Assert.*;

public class GPUTest {

    GPU gpu = new GPU(RTX3090);

    @Test
    public void splitToBatches() {
        assertTrue(gpu.getDataBatches().size()==0);

        Model model = new Model();
        model.setData(new Data(Images,1000));
        gpu.setModel(model);
        gpu.splitToBatches();

        int desiredNumOfBatches = (gpu.getModel().getData().getSize())/1000;
        assertTrue(gpu.getDataBatches().size()==desiredNumOfBatches);
    }


    @Test
    public void sendBatchToCluster() {
        CPU cpu = new CPU(32);
        LinkedList<CPU> ll = new LinkedList<>();
        ll.push(cpu);
        Cluster.getInstance().setCPUs(ll);

        Model model = new Model();
        model.setData(new Data(Images,1000));
        gpu.setModel(model);
        gpu.splitToBatches();

        int preSize = gpu.getDataBatches().size();
        assertTrue(preSize>0);

        gpu.sendBatchToCluster();

        assertTrue(gpu.getDataBatches().size()==preSize-1);
    }


    @Test
    public void testProcess() {
        Student student = new Student("Bob", "CS", Student.Degree.MSc);
        Model model = new Model("model",new Data(Tabular,1000), student);
        model.setStatus(Trained);  // in order to simulate "TestModel" event

        gpu.setModel(model);

        assertTrue(model.getStatus()==Trained);
        assertTrue(model.getResult()==None);

        gpu.testProcess();

        assertTrue(model.getStatus()==Tested);
        assertTrue(model.getResult()!=None);
    }


    @Test
    public void decreaseBatchesAmountToProcess() {
        int preSize=gpu.getBatchesAmountToProcess();
        gpu.decreaseBatchesAmountToProcess();
        assertTrue(preSize==gpu.getBatchesAmountToProcess()+1);
    }


    @Test
    public void popInnerTestQueue() {
        Student student = new Student("Bob", "CS", Student.Degree.MSc);
        Model model = new Model("model",new Data(Tabular,1000), student);
        TestModelEvent e = new TestModelEvent(model);
        gpu.pushInnerTestQueue(e);
        int preSize=gpu.getInnerTestQueue().size();
        gpu.popInnerTestQueue();
        assertTrue(preSize==gpu.getInnerTestQueue().size()+1);
    }

    @Test
    public void pushInnerTestQueue() {
        int preSize=gpu.getInnerTestQueue().size();
        Student student = new Student("Bob", "CS", Student.Degree.MSc);
        Model model = new Model("model",new Data(Tabular,1000), student);
        TestModelEvent e = new TestModelEvent(model);
        gpu.pushInnerTestQueue(e);
        assertTrue(preSize+1==gpu.getInnerTestQueue().size());
    }

    @Test
    public void popInnerTrainQueue() {
        Student student = new Student("Bob", "CS", Student.Degree.MSc);
        Model model = new Model("model",new Data(Tabular,1000), student);
        TrainModelEvent e = new TrainModelEvent(model);
        gpu.pushInnerTrainQueue(e);
        int preSize=gpu.getInnerTrainQueue().size();
        gpu.popInnerTrainQueue();
        assertTrue(preSize==gpu.getInnerTrainQueue().size()+1);
    }

    @Test
    public void pushInnerTrainQueue() {
        int preSize=gpu.getInnerTrainQueue().size();
        Student student = new Student("Bob", "CS", Student.Degree.MSc);
        Model model = new Model("model",new Data(Tabular,1000), student);
        TrainModelEvent e = new TrainModelEvent(model);
        gpu.pushInnerTrainQueue(e);
        assertTrue(preSize+1==gpu.getInnerTrainQueue().size());
    }

    @Test
    public void pollFromVRAM() throws InterruptedException {
        Data d = new Data(Tabular,10);
        DataBatch db = new DataBatch(d,10);
        gpu.pushToVRAM(db);

        int preSize=gpu.getCurrVRAMSize();
        assertTrue(preSize>0);

        gpu.pollFromVRAM();

        assertTrue(gpu.getStartTick()==gpu.getCurrTick());
        assertTrue(preSize==gpu.getCurrVRAMSize()+1);
    }

    @Test
    public void pushToVRAM() {
        int preSize=gpu.getCurrVRAMSize();
        Data d = new Data(Tabular,10);
        DataBatch db = new DataBatch(d,10);
        gpu.pushToVRAM(db);
        assertTrue(preSize==gpu.getCurrVRAMSize()-1);
    }

    @Test
    public void completeModel() {
        Model model = new Model();
        model.setData(new Data(Tabular,1000));
        gpu.setModel(model);

        assertTrue(gpu.getModel()!=null);

        gpu.completeModel();

        assertTrue(gpu.getCurrVRAMSize()==0);
        assertTrue(gpu.getDataBatches().size()==0);
        assertTrue(gpu.getBatchesAmountToProcess()==0);
        assertTrue(gpu.getStartTick()==-1);
        assertTrue(gpu.getCurrDataBatch()==null);
    }

    @Test
    public void completeDataBatch() throws InterruptedException {
        Data d = new Data(Tabular,10);
        Student student = new Student("Bob", "CS", Student.Degree.MSc);
        Model model = new Model("model",d, student);
        DataBatch db = new DataBatch(d,10);

        gpu.setModel(model);
        gpu.pushToVRAM(db);
        gpu.pollFromVRAM();
        int GPUTimeUnits = gpu.getGPUTimeUnits();
        int processTick = gpu.getProcessTick();
        int preAmountDB = gpu.getBatchesAmountToProcess();

        assertTrue(gpu.getCurrDataBatch()!=null);

        gpu.completeDataBatch();
        assertTrue(gpu.getCurrVRAMSize()==0);
        assertTrue(gpu.getDataBatches().size()==0);
        assertTrue(gpu.getBatchesAmountToProcess()==preAmountDB-1);
        assertTrue(gpu.getStartTick()==-1);
        assertTrue(gpu.getCurrDataBatch()==null);
        assertTrue(GPUTimeUnits==gpu.getGPUTimeUnits()-processTick);
    }

    @After
    public void tearDown() throws Exception {
        gpu = new GPU(RTX3090);
    }
}
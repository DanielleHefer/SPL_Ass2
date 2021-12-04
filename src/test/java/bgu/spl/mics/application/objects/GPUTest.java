package bgu.spl.mics.application.objects;

import org.junit.After;
import org.junit.Test;

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
        assertTrue(gpu.getBatchesAmountToProcess()==0);

        Model model = new Model();
        model.setData(new Data(Images,1000));
        gpu.setModel(model);
        gpu.splitToBatches();

        int desiredNumOfBatches = (gpu.getModel().getData().getSize())/1000;
        assertTrue(gpu.getDataBatches().size()==desiredNumOfBatches);
        assertTrue(gpu.getBatchesAmountToProcess()==desiredNumOfBatches);
    }


    @Test
    public void sendBatchToCluster() {
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
    public void getProcessedBatchFromCluster() {
        Model model = new Model();
        model.setData(new Data(Tabular,1000));
        CPU cpu = new CPU(32);    //create cpu which will handle the dataBatch
        gpu.setModel(model);
        gpu.splitToBatches();
        gpu.sendBatchToCluster();

        assertTrue(gpu.getBatchesAmountToProcess()>0);
        int preVRAMSize = gpu.getCurrVRAMSize();
        assertTrue(preVRAMSize<gpu.getVRAMLimitation());

        // manually increasing the tick so the cpu process will be done
        cpu.updateTick();
        // manually increasing the tick so the gpu will be able to take the processed dataBatch from the cluster
        gpu.updateTick();
        gpu.getProcessedBatchFromCluster();

        assertTrue(gpu.getCurrVRAMSize() == preVRAMSize+1);
    }


    @Test
    public void updateTick() {
        //check the tick update
        int preTick = gpu.getCurrTick();
        gpu.updateTick();
        assertTrue(preTick+1==gpu.getCurrTick());

        //check the process of trainModel
        Model model = new Model();
        model.setData(new Data(Tabular,1000));
        CPU cpu = new CPU(32);    //create cpu which will handle the dataBatch
        gpu.setModel(model);
        gpu.splitToBatches();
        gpu.sendBatchToCluster();
        // manually increasing the tick so the cpu process will be done
        cpu.updateTick();
        // manually increasing the tick so the gpu will be able to take the processed dataBatch from the cluster
        gpu.updateTick();
        gpu.getProcessedBatchFromCluster();
        int preVRAMSize = gpu.getCurrVRAMSize();
        int preBatches = gpu.getBatchesAmountToProcess();

        gpu.updateTick();

        assertTrue(preVRAMSize==gpu.getCurrVRAMSize()-1);
        assertTrue(preBatches==gpu.getBatchesAmountToProcess()-1);
    }


    @Test
    public void testProcess() {
        Model model = new Model();
        model.setStatus(Trained);  // in order to simulate "TestModel" event
        model.setData(new Data(Tabular,1000));
        gpu.setModel(model);

        assertTrue(model.getStatus()==Trained);
        assertTrue(model.getResult()==None);

        gpu.testProcess();

        assertTrue(model.getStatus()==Tested);
        assertTrue(model.getResult()!=None);
    }


    @Test
    public void resetGPU() {
        Model model = new Model();
        model.setData(new Data(Tabular,1000));
        gpu.setModel(model);

        gpu.resetGPU();

        assertTrue(gpu.getModel()==null);
        assertTrue(gpu.getCurrVRAMSize()==0);
        assertTrue(gpu.getDataBatches().size()==0);
        assertTrue(gpu.getBatchesAmountToProcess()==0);
        assertTrue(gpu.getStartTick()==-1);
    }

    @After
    public void tearDown() throws Exception {
        gpu.resetGPU();
    }
}
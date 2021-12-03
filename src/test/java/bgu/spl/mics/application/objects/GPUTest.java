package bgu.spl.mics.application.objects;

import org.junit.Test;

import static org.junit.Assert.*;

public class GPUTest {

    @Test
    public void process() {
        Model m = new Model();
        Model.Status preStatus = m.getStatus();
        GPU gpu = new GPU();
        gpu.process(m);
        assertFalse(preStatus==m.getStatus());
    }
}
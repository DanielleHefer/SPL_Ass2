package bgu.spl.mics;

import org.junit.Test;
import org.junit.Before;
import  org.junit.After;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class FutureTest {

    private Future<Integer> future;

    @Before
    public void setUp() throws Exception{
        future = new Future<>();
    }

    @Test
    public void get() {
        int result = 1;
        assertFalse(future.isDone());
        future.resolve(result);
        int getResult = future.get();
        assertTrue(future.isDone());
        assertTrue(getResult==result);
    }

    @Test
    public void resolve() {
        int result = 1;
        assertFalse(future.isDone());
        future.resolve(result);
        assertTrue(future.isDone());
        assertTrue(future.get()==result);
    }

    @Test
    public void isDone() {
        assertFalse(future.isDone());
        future.resolve(1);
        assertTrue(future.isDone());
    }

    @Test
    public void testGet() {
        int result = 1;
        assertFalse(future.isDone());
        assertNull(future.get(100, TimeUnit.MILLISECONDS));
        assertFalse(future.isDone()); //Make sure isDone status wasn't changed after calling get(time,unit)
        future.resolve(result);
        assertTrue(future.isDone());
        int getResult = future.get(100, TimeUnit.MILLISECONDS);
        assertTrue(result==getResult);
    }
}
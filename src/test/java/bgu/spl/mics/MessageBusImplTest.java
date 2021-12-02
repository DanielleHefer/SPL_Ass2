package bgu.spl.mics;

import bgu.spl.mics.example.messages.ExampleBroadcast;
import bgu.spl.mics.example.messages.ExampleEvent;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class MessageBusImplTest {

    private MessageBusImpl messageBus;
    ExampleEvent event = new ExampleEvent("senderName");
    ExampleBroadcast broad = new ExampleBroadcast("10");
    MicroService ms1;
    MicroService ms2;

    @Before
    public void setUp() throws Exception {
        messageBus = MessageBusImpl.getInstance();
        ms1 = new MicroService("Bob") {
            @Override
            protected void initialize() {
                messageBus.register(this);
            }
        };
        ms2= new MicroService("Alice") {
            @Override
            protected void initialize() {
                messageBus.register(this);
            }
        };
    }

    @Test
    public void subscribeEvent() {
    }

    @Test
    public void subscribeBroadcast() {
    }

    @Test
    public void complete() {
        messageBus.subscribeEvent(event.getClass(), ms2);
        Future<String> future = ms1.sendEvent(event);
        assertFalse(future.isDone());
        String result = "Resolved";
        messageBus.complete(event, result);
        assertTrue(future.isDone());
        assertTrue(future.get().equals(result));
    }

    @Test
    public void sendBroadcast() {
        //This test also checks the subscribeBroadcast method
        //maybe need to add before the subscribeEvent method of MicroService ???????
        messageBus.subscribeBroadcast(broad.getClass(), ms2);
        ms1.sendBroadcast(broad);
        try{
            Message broad2 = messageBus.awaitMessage(ms2);
            assertTrue(((ExampleBroadcast)broad2).getSenderId().equals(broad.getSenderId()));
        }
        catch (InterruptedException k){};
    }

    @Test
    public void sendEvent() {
        //This test also checks the subscribeEvent method
        //maybe need to add before the subscribeEvent method of MicroService ???????
        messageBus.subscribeEvent(event.getClass(), ms2);
        ms1.sendEvent(event);
        try{
            Message event2 = messageBus.awaitMessage(ms2);
            assertTrue(((ExampleEvent)event2).getSenderName().equals(event.getSenderName()));
        }
        catch (InterruptedException k){};
    }

    @Test
    public void register() {
    }

    @Test
    public void unregister() {
    }

    @Test
    public void awaitMessage() {
    }

    @After
    public void tearDown() throws Exception {
        messageBus.unregister(ms1);
        messageBus.unregister(ms2);
    }
}
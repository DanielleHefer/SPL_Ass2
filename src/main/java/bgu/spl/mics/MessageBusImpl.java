package bgu.spl.mics;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * The {@link MessageBusImpl class is the implementation of the MessageBus interface.
 * Write your implementation here!
 * Only private fields and methods can be added to this class.
 */
public class MessageBusImpl implements MessageBus {

	//A map that represents the existing events and broadcasts and the microservices that subscribed to each one
	private ConcurrentHashMap<Class<? extends Message>, LinkedBlockingQueue<MicroService>> subscribers;

	//Map that connect each event to its future object
	private ConcurrentHashMap<Message, Future> futures;

	//Map that connect each microservice with its queue
	private ConcurrentHashMap<MicroService, LinkedBlockingQueue<Message>> queues;

	private static class MessageBusInstance{
		private static MessageBusImpl instance = new MessageBusImpl();
	}

	private MessageBusImpl() {
		subscribers = new ConcurrentHashMap<>();
		futures = new ConcurrentHashMap<>();
		queues = new ConcurrentHashMap<>();
	}

	public static MessageBusImpl getInstance() {
		return MessageBusInstance.instance;
	}

	public void subscribeMessage(Class<? extends Message> type, MicroService m) {
		System.out.println("Thread "+Thread.currentThread()+" "+Thread.currentThread().getName()+" - subscribeMessage");    //%%%%%%%%%%%%%%%%%%%%%
		//In order that other threads that want to create the same event type to the map
		//Or in order that other threads that want to send event with the same event type

		LinkedBlockingQueue<MicroService> eventSubscribers = subscribers.get(type);
		if(eventSubscribers!=null) {
			eventSubscribers.add(m);
			return;
		}
		LinkedBlockingQueue<MicroService> ll = new LinkedBlockingQueue<>();
		ll.add(m);
		subscribers.put(type, ll);
	}


	@Override
	public <T> void subscribeEvent(Class<? extends Event<T>> type, MicroService m) {
		subscribeMessage(type, m);
	}

	@Override
	public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {
		subscribeMessage(type, m);
	}

	@Override
	public <T> void complete(Event<T> e, T result) {
		futures.remove(e).resolve(result);
	}

	@Override
	public void sendBroadcast(Broadcast b) {
		System.out.println("Thread "+Thread.currentThread().getId()+" "+Thread.currentThread().getName()+" - sendBroadcast");    //%%%%%%%%%%%%%%%%%%%%%
		LinkedBlockingQueue<MicroService> list = subscribers.get(b.getClass());
		if (list != null) {
			for (MicroService microService : list) {
				if (queues.get(microService)!=null) {
					queues.get(microService).offer(b);
				}
			}
		}
	}

	//WE ADDED*****
	//The function returns the first microservice in line that can accept the event, and handles the round robin
	private <T> MicroService findNextMicroservice(Event<T> e){
		System.out.println("Thread "+Thread.currentThread().getId()+" "+Thread.currentThread().getName()+" - findNextMicroservice");    //%%%%%%%%%%%%%%%%%%%%%

		LinkedBlockingQueue<MicroService> list = subscribers.get(e.getClass());
		if(list!=null) {
			while (!list.isEmpty()) {
				MicroService m = list.remove(); //removing first in queue
				if (queues.get(m) != null) {
					list.offer(m); //adding the microservice to the end of the queue
					return m; //return the first microservice
				}
			}
		}
		return null;
	}

	@Override
	public <T> Future<T> sendEvent(Event<T> e) {
		System.out.println("Thread "+Thread.currentThread().getId()+" "+Thread.currentThread().getName()+" - sendEvent");    //%%%%%%%%%%%%%%%%%%%%%

		MicroService microservice = findNextMicroservice(e);
		if (microservice!=null) {
			Future<T> future = new Future<>();
			futures.put(e,future);
			queues.get(microservice).offer(e);
			return future;
		}
		//there is no microservice in the queue that can handle the event
		return null;
	}

	@Override
	public void register(MicroService m) {
		System.out.println("Thread "+Thread.currentThread().getId()+" "+Thread.currentThread().getName()+" - register");    //%%%%%%%%%%%%%%%%%%%%%

		// "it creates a queue for each micro service using the register method" *******
		queues.put(m, new LinkedBlockingQueue<>());

	}

	@Override
	public void unregister(MicroService m) {
		System.out.println("Thread "+Thread.currentThread().getId()+" "+Thread.currentThread().getName()+" - unregister");    //%%%%%%%%%%%%%%%%%%%%%

		//Delete the queue
		queues.remove(m);

		//Delete the microservice from the events he subscribed to
		for (Class<? extends Message> message : subscribers.keySet()) {
			if(subscribers.get(message).contains(m)){
				subscribers.get(message).remove(m);
			}
		}
	}

	@Override
	public Message awaitMessage(MicroService m) throws InterruptedException {
		System.out.println("Thread "+Thread.currentThread().getId()+" "+Thread.currentThread().getName()+" - awaitMessage");    //%%%%%%%%%%%%%%%%%%%%%
		return queues.get(m).take();
	}

	//WE ADDED *****
	public boolean isRegistered(MicroService m){
		System.out.println("Thread "+Thread.currentThread().getId()+" "+Thread.currentThread().getName()+" - isRegistered");    //%%%%%%%%%%%%%%%%%%%%%
		return queues.get(m) != null;
	}


	//WE ADDED *****
	public <T> boolean isSubscribedToEvent(Class<? extends Event<T>> type, MicroService m){
		return subscribers.get(type).contains(m);
	}

	//WE ADDED *****
	public boolean isSubscribedToBroadcast(Class<? extends Broadcast> type, MicroService m){
		return subscribers.get(type).contains(m);
	}

	//WE ADDED *****
	public boolean isMessageInQueue(Message m, MicroService ms){
		return queues.get(ms).contains(m);
	}
}
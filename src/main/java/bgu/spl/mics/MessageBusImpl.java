package bgu.spl.mics;

import java.util.*;

/**
 * The {@link MessageBusImpl class is the implementation of the MessageBus interface.
 * Write your implementation here!
 * Only private fields and methods can be added to this class.
 */
public class MessageBusImpl implements MessageBus {

	//A map that represents the existing events and broadcasts and the microservices that subscribed to each one
	private Hashtable<Class<? extends Message>, LinkedList<MicroService>> subscribers;

	//Map that connect each event to its future object
	private Hashtable<Message, Future> futures;

	//Map that connect each microservice with its queue
	private Hashtable<MicroService, LinkedList<Message>> queues;

	private static class MessageBusInstance{
		private static MessageBusImpl instance = new MessageBusImpl();
	}

	private MessageBusImpl() {
		subscribers = new Hashtable<>();
		futures = new Hashtable<>();
		queues = new Hashtable<>();
	}

	public static MessageBusImpl getInstance() {
		return MessageBusInstance.instance;
	}

	public void subscribeMessage(Class<? extends Message> type, MicroService m) {
		System.out.println("Thread "+Thread.currentThread()+" "+Thread.currentThread().getName()+" - subscribeMessage");    //%%%%%%%%%%%%%%%%%%%%%
		//In order that other threads that want to create the same event type to the map
		//Or in order that other threads that want to send event with the same event type
		synchronized (subscribers) {
			LinkedList<MicroService> eventSubscribers = subscribers.get(type);
			if(eventSubscribers!=null) {
				//maybe need to synchronize eventSubscribers ?????
				eventSubscribers.add(m);
				return;
			}
			LinkedList<MicroService> ll = new LinkedList<>();
			ll.add(m);
			subscribers.put(type, ll);
		}
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
		LinkedList<MicroService> list = subscribers.get(b.getClass());
		if (list != null) {
			for (MicroService microService : list) {
				//Queue<Message> queue = queues.get(microService);
				synchronized (queues.get(microService)) {
					queues.get(microService).add(b);
					queues.get(microService).notifyAll();
				}
			}
		}
	}

	//WE ADDED*****
	//The function returns the first microservice in line that can accept the event, and handles the round robin
	private <T> MicroService findNextMicroservice(Event<T> e){
		System.out.println("Thread "+Thread.currentThread().getId()+" "+Thread.currentThread().getName()+" - findNextMicroservice");    //%%%%%%%%%%%%%%%%%%%%%

		LinkedList<MicroService> list = subscribers.get(e.getClass());
		if(list!=null) {
			while (!list.isEmpty()) {
				synchronized (list) { //making sure list is thread-safe
					MicroService m = list.remove(); //removing first in queue
					list.add(m); //adding the microservice to the end of the queue
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
			synchronized (queues.get(microservice)){
				queues.get(microservice).add(e);
				queues.get(microservice).notifyAll();
			}
			return future;
		}
		//there is no microservice in the queue that can handle the event
		return null;
	}

	@Override
	public void register(MicroService m) {
		System.out.println("Thread "+Thread.currentThread().getId()+" "+Thread.currentThread().getName()+" - register");    //%%%%%%%%%%%%%%%%%%%%%

		// "it creates a queue for each micro service using the register method" *******
		synchronized (queues) {
				queues.put(m, new LinkedList<>());
		}
	}

	@Override
	public void unregister(MicroService m) {
		System.out.println("Thread "+Thread.currentThread().getId()+" "+Thread.currentThread().getName()+" - unregister");    //%%%%%%%%%%%%%%%%%%%%%

		// TODO Auto-generated method stub
		// "when a micro service calls the unregister method of the message bus, the message bus should remove its queue *******
		// and clean all references related to that micro service" *******

		//The only service that perform unregister is conference, after sending its data
		//So there is no reason to transfer events to other registered microservices

		//Delete the queue
		synchronized (queues) {   // maybe specific queue **************88
			if(queues.get(m)!=null) {
				queues.remove(m);
			}
		}

		//Delete the microservice from the events he subscribed to
		synchronized (subscribers) {
			subscribers.forEach((event, list) -> {
				if (list.contains(m))
					list.remove();
			});
		}
	}

	@Override
	public Message awaitMessage(MicroService m) throws InterruptedException {
		System.out.println("Thread "+Thread.currentThread().getId()+" "+Thread.currentThread().getName()+" - awaitMessage");    //%%%%%%%%%%%%%%%%%%%%%

		LinkedList<Message> mQueue = queues.get(m);
		if (mQueue==null)
			throw new IllegalArgumentException("microservice is not registered, failed on awaitMessage");
		synchronized (mQueue){
			while(mQueue.isEmpty()) {
				try{
					mQueue.wait();
				}
				catch(Exception e) {}
			}
			return mQueue.remove();
		}
}

	//WE ADDED *****
	public boolean isRegistered(MicroService m){
		System.out.println("Thread "+Thread.currentThread().getId()+" "+Thread.currentThread().getName()+" - isRegistered");    //%%%%%%%%%%%%%%%%%%%%%

		synchronized (queues) {
			return queues.get(m) != null;
		}
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
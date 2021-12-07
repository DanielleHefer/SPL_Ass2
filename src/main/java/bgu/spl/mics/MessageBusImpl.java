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

	@Override
	public <T> void subscribeEvent(Class<? extends Event<T>> type, MicroService m) { //Ask whats the "?" means
		//In order that other threads that want to create the same event type to the map
		//Or in order that other threads that want to send event with the same event type
		synchronized (subscribers) {
			LinkedList<MicroService> eventSubscribers = subscribers.get(type);
			if(eventSubscribers!=null) {
				//maybe need to synchronize eventSubscribers ?????
				eventSubscribers.add(m);
				return;
			}
			LinkedList<MicroService> ll = new LinkedList<MicroService>();
			ll.add(m);
			subscribers.put(type, ll);
		}
		//Wake the other threads that want to send event and waiting
		notifyAll();
	}

	@Override
	public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {
		//In order that other threads that want to create the same event type to the map
		//Or in order that other threads that want to send event with the same event type
		synchronized (subscribers) {
			LinkedList<MicroService> eventSubscribers = subscribers.get(type);
			if(eventSubscribers!=null) {
				//maybe need to synchronize eventSubscribers ?????
				eventSubscribers.add(m);
				return;
			}
			LinkedList<MicroService> ll = new LinkedList<MicroService>();
			ll.add(m);
			subscribers.put(type, ll);
		}
		//Wake the other threads that want to send event and waiting
		notifyAll();
	}

	@Override
	public <T> void complete(Event<T> e, T result) {
		futures.remove(e).resolve(result);
	}

	@Override
	public void sendBroadcast(Broadcast b) {
		// TODO Auto-generated method stub
	}


	@Override
	public <T> Future<T> sendEvent(Event<T> e) {
		// TODO Auto-generated method stub

		//Check for current microservice if its queue exist, if no, transfer the message to the next microservice

		return null;
	}

	@Override
	public void register(MicroService m) {
		// "it creates a queue for each micro service using the register method" *******
		synchronized (queues) {
			if(queues.get(m)==null) {
				queues.put(m, new LinkedList<Message>());
			}
		}
	}

	@Override
	public void unregister(MicroService m) {
		// TODO Auto-generated method stub
		// "when a micro service calls the unregister method of the message bus, the message bus should remove its queue *******
		// and clean all references related to that micro service" *******

		//The only service that perform unregister is conference, after sending its data
		//So there is no reason to transfer events to other registered microservices

		//Delete the queue
		synchronized (queues) {
			if(queues.get(m)!=null) {
				queues.remove(m);
			}
		}

		//Delete the microservice from the events he subscribed to
		subscribers.forEach((event,list) -> {
			if(list.contains(m))
				list.remove();
		});
	}

	@Override
	public Message awaitMessage(MicroService m) throws InterruptedException {
		// TODO Auto-generated method stub

		// " Once the queue is created, a Micro-Service can take the next message in the queue using the ‘awaitMessage’ *******
		//method. The ‘awaitMessage’ method is blocking, that is, if there are no messages *******
		//available in the Micro-Service queue, it should wait until a message becomes available." *******
		return null;
	}

	//WE ADDED *****
	public boolean isRegistered(MicroService m){
		// TODO Implement this
		return true;
	}

	//WE ADDED *****
	public <T> boolean isSubscribedToEvent(Class<? extends Event<T>> type, MicroService m){
		// TODO Implement this
		return true;
	}

	//WE ADDED *****
	public boolean isSubscribedToBroadcast(Class<? extends Broadcast> type, MicroService m){
		// TODO Implement this
		return true;
	}

	//WE ADDED *****
	public boolean isMessageInQueue(Message m, MicroService ms){
		// TODO Implement this
		return true;
	}
}
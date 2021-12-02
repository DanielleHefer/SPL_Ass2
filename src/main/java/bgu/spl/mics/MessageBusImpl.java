package bgu.spl.mics;

/**
 * The {@link MessageBusImpl class is the implementation of the MessageBus interface.
 * Write your implementation here!
 * Only private fields and methods can be added to this class.
 */
public class MessageBusImpl implements MessageBus {

	private static MessageBusImpl instance = null;

	public static MessageBusImpl getInstance() {
//		if(instance==null)
//			instance = new MessageBusImpl();
//		return instance;

		//NOT THREAD SAFE
		return null;
	}

	@Override
	public <T> void subscribeEvent(Class<? extends Event<T>> type, MicroService m) { //Ask whats the "?" means
		// TODO Auto-generated method stub

	}

	@Override
	public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {
		// TODO Auto-generated method stub

	}

	@Override
	public <T> void complete(Event<T> e, T result) {
		// TODO Auto-generated method stub

	}

	@Override
	public void sendBroadcast(Broadcast b) {
		// TODO Auto-generated method stub

	}


	@Override
	public <T> Future<T> sendEvent(Event<T> e) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void register(MicroService m) {
		// TODO Auto-generated method stub
		// "it creates a queue for each micro service using the register method" *******

	}

	@Override
	public void unregister(MicroService m) {
		// TODO Auto-generated method stub
		// "when a micro service calls the unregister method of the message bus, the message bus should remove its queue *******
		// and clean all references related to that micro service" *******
	}

	@Override
	public Message awaitMessage(MicroService m) throws InterruptedException {
		// TODO Auto-generated method stub

		// " Once the queue is created, a Micro-Service can take the next message in the queue using the ‘awaitMessage’ *******
		//method. The ‘awaitMessage’ method is blocking, that is, if there are no messages *******
		//available in the Micro-Service queue, it should wait until a message becomes available." *******
		return null;
	}
}
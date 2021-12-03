package bgu.spl.mics;

/**
 * The message-bus is a shared object used for communication between
 * micro-services.
 * It should be implemented as a thread-safe singleton.
 * The message-bus implementation must be thread-safe as
 * it is shared between all the micro-services in the system.
 * You must not alter any of the given methods of this interface. 
 * You cannot add methods to this interface.
 * // it was written in the FAQ that we can add queries that have no relation to the implementation ****
 */
public interface MessageBus {

    /**
     * Subscribes {@code m} to receive {@link Event}s of type {@code type}.
     * <p>
     * @param <T>  The type of the result expected by the completed event.
     * @param type The type to subscribe to,
     * @param m    The subscribing micro-service.
     */
    /**
     * @PRE:
     *      isRegistered(m)==true
     *      isSubscribedToEvent(type,m)==false
     * @POST:
     *      isSubscribedToEvent(type,m)==true
     */
    <T> void subscribeEvent(Class<? extends Event<T>> type, MicroService m);

    /**
     * Subscribes {@code m} to receive {@link Broadcast}s of type {@code type}.
     * <p>
     * @param type 	The type to subscribe to.
     * @param m    	The subscribing micro-service.
     */
    /**
     * @PRE:
     *      isRegistered(m)==true
     *      isSubscribedToBroadcast(type,m)==false
     * @POST:
     *      isSubscribedToBroadcast(type,m)==true
     */
    void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m);

    /**
     * Notifies the MessageBus that the event {@code e} is completed and its
     * result was {@code result}.
     * When this method is called, the message-bus will resolve the {@link Future}
     * object associated with {@link Event} {@code e}.
     * <p>
     * @param <T>    The type of the result expected by the completed event.
     * @param e      The completed event.
     * @param result The resolved result of the completed event.
     */
    /**
     * @PRE:
     *      future.isDone()==false
     * @POST:
     *      future.isDone()==true
     *      future.get()==T result
     */
    <T> void complete(Event<T> e, T result);

    /**
     * Adds the {@link Broadcast} {@code b} to the message queues of all the
     * micro-services subscribed to {@code b.getClass()}.
     * <p>
     * @param b 	The message to added to the queues.
     */
    /**
     *@PRE:
     *      none
     * @POST:
     *      for each (MicrosService.isRegistered()==true && MicrosService.isSubscribed(Broadcast b)) :
     *      MessageBus.getAwaitMessagesSize(MicrosService) == @PRE(MessageBus.getAwaitMessagesSize(MicrosService)) + 1)
     */
    void sendBroadcast(Broadcast b);

    /**
     * Adds the {@link Event} {@code e} to the message queue of one of the
     * micro-services subscribed to {@code e.getClass()} in a round-robin
     * fashion. This method should be non-blocking.
     * <p>
     * @param <T>    	The type of the result expected by the event and its corresponding future object.
     * @param e     	The event to add to the queue.
     * @return {@link Future<T>} object to be resolved once the processing is complete,
     * 	       null in case no micro-service has subscribed to {@code e.getClass()}.
     */
    /**
     * @PRE:
     *      none
     * @POST:
     *      if (@return!=null)
     *      ∃ (MicrosService.isRegistered()==true && MicrosService.isSubscribed(Event<T> e))
     *      for which (MessageBus.getAwaitMessagesSize(MicrosService) == @PRE(MessageBus.getAwaitMessagesSize(MicrosService)) + 1)
     *
     */
    <T> Future<T> sendEvent(Event<T> e);

    /**
     * Allocates a message-queue for the {@link MicroService} {@code m}.
     * <p>
     * @param m the micro-service to create a queue for.
     */
    /**
     * @PRE:
     * 	 	isRegistered(m)==false
     * @POST:
     * 	 	isRegistered(m)==true
     */
    void register(MicroService m);

    /**
     * Removes the message queue allocated to {@code m} via the call to
     * {@link #register(bgu.spl.mics.MicroService)} and cleans all references
     * related to {@code m} in this message-bus. If {@code m} was not
     * registered, nothing should happen.
     * <p>
     * @param m the micro-service to unregister.
     */
    /**
     * @PRE:
     * 	 	isRegistered(m)==true
     * @POST:
     * 	 	isRegistered(m)==false
     */
    void unregister(MicroService m);

    /**
     * Using this method, a <b>registered</b> micro-service can take message
     * from its allocated queue.
     * This method is blocking meaning that if no messages
     * are available in the micro-service queue it
     * should wait until a message becomes available.
     * The method should throw the {@link IllegalStateException} in the case
     * where {@code m} was never registered.
     * <p>
     * @param m The micro-service requesting to take a message from its message
     *          queue.
     * @return The next message in the {@code m}'s queue (blocking).
     * @throws InterruptedException if interrupted while waiting for a message
     *                              to became available.
     */
    /**
     * @PRE:
     *      none
     * @POST:
     *      isMessageInQueue(@return Message, m)==false
     */
    Message awaitMessage(MicroService m) throws InterruptedException;

    /**
     * @param m The microservice to check if registered to the message bus
     * @return true if the microservice is registered to the message bus, false otherwise
     */
    /**
     * @PRE:
     * 	 	none
     * @POST:
     * 	 	none
     * (basic query)
     */
    //WE ADDED ******
    boolean isRegistered(MicroService m);

    //WE ADDED *****
    <T> boolean isSubscribedToEvent(Class<? extends Event<T>> type, MicroService m);

    //WE ADDED *****
    boolean isSubscribedToBroadcast(Class<? extends Broadcast> type, MicroService m);

    //WE ADDED*****
    boolean isMessageInQueue(Message m, MicroService ms);
}

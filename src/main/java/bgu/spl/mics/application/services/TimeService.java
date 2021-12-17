package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.TickBroadcast;

import java.util.Timer;

/**
 * TimeService is the global system timer There is only one instance of this micro-service.
 * It keeps track of the amount of ticks passed since initialization and notifies
 * all other micro-services about the current time tick using {@link TickBroadcast}.
 * This class may not hold references for objects which it is not responsible for.
 * 
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class TimeService extends MicroService{

	private int msForTick;
	private int duration;

	public TimeService(String name, int msForTick, int duration) {
		super(name);
		this.msForTick=msForTick;
		this.duration=duration;
	}

	public void registration(){
		messageBus.register(this);
	}

	@Override
	protected void initialize() {

		//messageBus.register(this);

		TickBroadcast tickBroadcast = new TickBroadcast(0);
		for(int tick=1; tick < duration; tick++) {
			tickBroadcast.increaseTick();
			sendBroadcast(tickBroadcast);
			System.out.println("Tick Broadcast - "+tick); //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
			try{
				Thread.sleep(msForTick);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		sendBroadcast(new TickBroadcast(null));
		terminate();
	}
//IMPLEMENT tick.getCurrTick() AND THAN UN-BACKSLASH IN GPUSERVICE + CPUSERVICE
}

package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.PublishConferenceBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.ConfrenceInformation;
import bgu.spl.mics.application.messages.PublishResultsEvent;

import java.util.Vector;

/**
 * Conference service is in charge of
 * aggregating good results and publishing them via the {@link PublishConferenceBroadcast},
 * after publishing results the conference will unregister from the system.
 * This class may not hold references for objects which it is not responsible for.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class ConferenceService extends MicroService {

    private ConfrenceInformation conference;
    private int conferenceDate;
    private int currTick;

    public ConferenceService(String name, ConfrenceInformation conference) {
        super(name);
        this.conference=conference;
        conferenceDate=conference.getDate();
    }

    @Override
    protected void initialize() {
        //Subscribe to PublishResultEvent
        super.subscribeEvent(PublishResultsEvent.class,  publishEvent->{
            conference.aggregate(publishEvent.getModel());
        });

        //Subscribe to tickBroadcast
        super.subscribeBroadcast(TickBroadcast.class, tick->{

            if(tick.getCurrTick()==null) {
                terminate();
            }

            else {
                currTick=tick.getCurrTick();
                if (currTick == conferenceDate) {
                    Vector<String> modelNames = conference.getModelsNames();
                    sendBroadcast(new PublishConferenceBroadcast(modelNames));
                    terminate();
                }
            }
        });
    }
}

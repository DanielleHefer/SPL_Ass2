package bgu.spl.mics.application.messages;

import bgu.spl.mics.Broadcast;

public class TickBroadcast implements Broadcast {

    Integer currTick;

    public TickBroadcast(Integer tick){
        this.currTick=tick;
    }

    public void increaseTick() {
        currTick++;
    }

    public Integer getCurrTick() {
        return currTick;
    }
}

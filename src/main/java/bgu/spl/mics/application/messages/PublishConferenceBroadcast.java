package bgu.spl.mics.application.messages;

import bgu.spl.mics.Broadcast;

import java.util.Vector;

public class PublishConferenceBroadcast implements Broadcast {

    private Vector<String> modelsNames;

    public PublishConferenceBroadcast(Vector<String> modelsNames){
        this.modelsNames=modelsNames;
    }

    public Vector<String> getModelsNames() {
        return modelsNames;
    }
}

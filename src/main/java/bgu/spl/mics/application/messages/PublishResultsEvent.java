package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;
import bgu.spl.mics.application.objects.Model;

public class PublishResultsEvent<T> implements Event<T> {
    private Model model;

    public PublishResultsEvent(Model model){
        this.model=model;
    }

    public Model getModel(){
        return model;
    }
}

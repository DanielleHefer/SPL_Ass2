package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;
import bgu.spl.mics.application.objects.Data;
import bgu.spl.mics.application.objects.Model;


public class TrainModelEvent<T> implements Event<T> {

    private Model model; //The model we are currently training
    private int modelSize;
    private Data.Type modelDataType;

    public TrainModelEvent(Model model) {
        this.model= model;
        modelSize=model.getData().getSize();
        modelDataType = model.getData().getType();
    }

    public Model getModel() {
        return model;
    }

    public int getModelSize() {
        return modelSize;
    }

    public Data.Type getModelDataType() {
        return modelDataType;
    }

}

package bgu.spl.mics.application.objects;

import bgu.spl.mics.Message;

import java.util.LinkedList;
import java.util.Vector;

/**
 * Passive object representing information on a conference.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class ConfrenceInformation {

    private String name;
    private int date;

    //WE ADDED****
    private Vector<String> modelsNames;
    private LinkedList<Model> models;

    public ConfrenceInformation(String name, int date){
        this.name=name;
        this.date=date;
        modelsNames = new Vector<>();
        models = new LinkedList<>();
    }

    public void aggregate(Model currModel){
            if (currModel.getResult()== Model.Result.Good){
                modelsNames.add(currModel.getName());
                models.add(currModel);
            }
    }

    public Vector<String> getModelsNames() {
        return modelsNames;
    }

    public int getDate() {
        return date;
    }

    public String getName() {
        return name;
    }

    public LinkedList<Model> getModels() {
        return models;
    }
}

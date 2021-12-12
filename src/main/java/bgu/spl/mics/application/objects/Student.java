package bgu.spl.mics.application.objects;

import java.util.HashSet;
import java.util.Vector;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Passive object representing single student.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class Student {
    /**
     * Enum representing the Degree the student is studying for.
     */
    public enum Degree {
        MSc, PhD
    }

    private String name;
    private String department;
    private Degree status;
    private int publications;
    private int papersRead;

    private Vector<Model> studentModels;
    private HashSet<String> modelsNames;

    public Student (String name, String department, Degree status) {
        this.name=name;
        this.department=department;
        this.status=status;
        publications=0;
        papersRead=0;
        studentModels = new Vector<>();
        modelsNames = new HashSet<>();
    }

    public Degree getStatus(){
        return status;
    }

    public String getName() {
        return name;
    }

    public String getDepartment() {
        return department;
    }

    public int getPublications() {
        return publications;
    }

    public int getPapersRead() {
        return papersRead;
    }

    public Vector<Model> getStudentModels() {
        return studentModels;
    }

    private void sortModelsBySize(Vector<Model> m) {
        m.sort((a,b) -> a.getData().getSize()*a.getData().typeToNum() - b.getData().getSize()*b.getData().typeToNum());
    }

    public void setStudentModels(Vector<Model> models) {
        sortModelsBySize(models);
        for(Model m: models) {
            studentModels.add(m);
            modelsNames.add(m.getName());
        }
    }

    //StudentService calling this function when a student receives publishResultBroadcast from conference
    public void readPublishes(Vector<String> publishedModels) {
        for (String currModel: publishedModels) {
            if(modelsNames.contains(currModel)) {
                this.publications++;
            }
            else
                papersRead++;
        }
    }
}

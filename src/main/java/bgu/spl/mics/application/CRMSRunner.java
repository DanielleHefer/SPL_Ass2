package bgu.spl.mics.application;

import bgu.spl.mics.application.objects.*;
import bgu.spl.mics.application.services.*;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** This is the Main class of Compute Resources Management System application. You should parse the input file,
 * create the different instances of the objects, and run the system.
 * In the end, you should output a text file.
 */
public class CRMSRunner {

    public static Student.Degree stringToDegree(String str) {
        if (str.compareTo("PhD")==0)
            return Student.Degree.PhD;
        else
            return Student.Degree.MSc;
    }

    public static Data.Type stringToDataType(String str){
        if(str.compareTo("images")==0)
            return Data.Type.Images;
        else if (str.compareTo("Text")==0)
            return Data.Type.Text;
        else
            return Data.Type.Tabular;
    }

    public static GPU.Type stringToGPUType(String str) {
        if(str.compareTo("RTX3090")==0)
            return GPU.Type.RTX3090;
        else if (str.compareTo("RTX2080")==0)
            return GPU.Type.RTX2080;
        else
            return GPU.Type.GTX1080;
    }

    public static void main(String[] args) {

        if (args.length!=1) {
            System.err.println("Required file in not found");
            System.exit(1);
        }

        LinkedList<Thread> threads = new LinkedList<>();

        Gson gson = new Gson();
        try{
            Reader reader = Files.newBufferedReader(Paths.get(args[0]));
            JsonObject jsonObject = gson.fromJson(reader, JsonObject.class);
            //Each object in the jsonArray is from type jsonElement
            JsonArray studentsList = gson.fromJson(jsonObject.get("Students"), JsonArray.class);
            List<Student> studentsObjectsList = new LinkedList<>();
            List<Model> modelsObjectList = new LinkedList<>();
            //Transfer each jsonElement to jsonObject
            for(JsonElement studentElement: studentsList){
                JsonObject studentJsonObj = studentElement.getAsJsonObject();

                // create student object
                Student student = new Student(studentJsonObj.get("name").getAsString(),
                        studentJsonObj.get("department").getAsString(),
                        stringToDegree(studentJsonObj.get("status").getAsString()));

                //Create student service
                threads.add(new Thread(new StudentService("Student Service",student)));

                studentsObjectsList.add(student);

                // Create model object
                JsonArray modelsList = gson.fromJson(studentJsonObj.get("models"), JsonArray.class);
                for(JsonElement modelElement: modelsList){
                    JsonObject modelJsonObj = modelElement.getAsJsonObject();

                    // Create Data object
                    Data data = new Data(stringToDataType(modelJsonObj.get("type").getAsString()),
                            modelJsonObj.get("size").getAsInt());

                    // Create model object
                    Model model = new Model(modelJsonObj.get("name").getAsString(),
                            data,
                            student);
                    modelsObjectList.add(model);
                }
                student.setStudentModels(new Vector<Model>(modelsObjectList));
                modelsObjectList.clear();
                //Add the data to model and the model to Student!! *****************
            }

            // Create gpu objects
            JsonObject gpus = gson.fromJson(reader, JsonObject.class);
            JsonArray gpusList = gson.fromJson(jsonObject.get("GPUS"), JsonArray.class);
            List<GPU> gpusObjList = new LinkedList<GPU>();
            for (JsonElement gpuElement: gpusList){
                GPU gpu = new GPU(stringToGPUType(gpuElement.getAsString()));
                gpusObjList.add(gpu);        //update the constructor  ********

                //Create student service
                threads.add(new Thread(new GPUService("GPU Service",gpu)));
            }

            // Create cpu objects
            JsonObject cpus = gson.fromJson(reader, JsonObject.class);
            JsonArray cpusList = gson.fromJson(jsonObject.get("CPUS"), JsonArray.class);
            List<CPU> cpusObjList = new LinkedList<CPU>();
            for (JsonElement cpuElement: cpusList){
                CPU cpu = new CPU(cpuElement.getAsInt());
                cpusObjList.add(cpu);     //update the constructor  ********
                threads.add(new Thread(new CPUService("CPU Service",cpu)));
            }

            // Create conference objects
            JsonObject conferences = gson.fromJson(reader, JsonObject.class);
            JsonArray conferencesList = gson.fromJson(jsonObject.get("Conferences"), JsonArray.class);
            List<ConfrenceInformation> conferencesObjList = new LinkedList<ConfrenceInformation>();
            for (JsonElement conferenceElement: conferencesList){
                JsonObject conferenceJsonObj = conferenceElement.getAsJsonObject();
                ConfrenceInformation confrenceInformation = new ConfrenceInformation(    //update the constructor  ********
                        conferenceJsonObj.get("name").getAsString(),
                        conferenceJsonObj.get("date").getAsInt());

                threads.add(new Thread(new ConferenceService("Conference Service",confrenceInformation)));
                conferencesObjList.add(confrenceInformation);
            }
            System.out.println("");

            //Need to add TickTime and Duration *****************************
            int tick = jsonObject.get("TickTime").getAsInt();
            int duration = jsonObject.get("Duration").getAsInt();

            Thread timeService = new Thread(new TimeService("Time Service", tick,duration));
            threads.add(timeService);
        }

        catch (Exception e){

        }

//        File output = new File(args[1]);
//        String json = gson.toJson(Cluster.getInstance());
//        String outputPath = args[1];
//        try(PrintWriter out = new PrintWriter(outputPath)){
//            out.println(json);
//        }
//        catch (FileNotFoundException e){}

        //MAKE SURE ALL THREAD ARE FINISHED*************
        //GENERATE RESULTS FILE
    }
}

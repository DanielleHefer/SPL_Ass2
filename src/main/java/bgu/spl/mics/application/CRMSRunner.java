package bgu.spl.mics.application;

import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.application.messages.PublishConferenceBroadcast;
import bgu.spl.mics.application.objects.*;
import bgu.spl.mics.application.services.*;
import com.google.gson.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

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

    public static String resultToString(Model.Result result){
        if(result==Model.Result.Good)
            return "Good";
        else if (result==Model.Result.Bad)
            return "Bad";
        return "None";
    }

    public static String typeToString(Data.Type type) {
        if (type==Data.Type.Tabular) {
            return "Tabular";
        }
        else if (type==Data.Type.Images) {
            return "Images";
        }
        return "Text";
    }

    public static void main(String[] args) {

//        ConfrenceInformation con = new ConfrenceInformation("nnnna",10);
//        ConferenceService cs = new ConferenceService("dsfsd", con);
//        MessageBusImpl mb= MessageBusImpl.getInstance();
//        mb.register(cs);
//        Vector <String> vec = new Vector<>();
//        vec.add("a");
//        vec.add("b");
//        Student student1 = new Student("asd", "dsf", Student.Degree.MSc);
//        StudentService ss = new StudentService("sdf", student1);
//        mb.register(ss);
//        mb.subscribeBroadcast(PublishConferenceBroadcast.class,ss);
//        mb.sendBroadcast(new PublishConferenceBroadcast(vec));



        if (args.length!=1) {
            System.err.println("Required file in not found");
            System.exit(1);
        }

        LinkedList<Thread> threads = new LinkedList<>();

        Gson gson = new Gson();
        List<Student> studentsObjectsList = new LinkedList<>();
        List<ConfrenceInformation> conferencesObjList = new LinkedList<>();
        List<GPU> gpusObjList = new LinkedList<GPU>();
        List<CPU> cpusObjList = new LinkedList<CPU>();
        Thread timeServiceThread = null;

        try{
            Reader reader = Files.newBufferedReader(Paths.get(args[0]));
            JsonObject jsonObject = gson.fromJson(reader, JsonObject.class);
            //Each object in the jsonArray is from type jsonElement
            JsonArray studentsList = gson.fromJson(jsonObject.get("Students"), JsonArray.class);
            //List<Student> studentsObjectsList = new LinkedList<>();
            List<Model> modelsObjectList = new LinkedList<>();
            //Transfer each jsonElement to jsonObject
            for(JsonElement studentElement: studentsList){
                JsonObject studentJsonObj = studentElement.getAsJsonObject();

                // create student object
                Student student = new Student(studentJsonObj.get("name").getAsString(),
                        studentJsonObj.get("department").getAsString(),
                        stringToDegree(studentJsonObj.get("status").getAsString()));

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

                //Create student service
                StudentService studentService = new StudentService("Student Service",student);
                studentService.registration();
                threads.add(new Thread(studentService, "Student Service - " + student.getName()));

                modelsObjectList.clear();
                //Add the data to model and the model to Student!! *****************
            }

            // Create gpu objects
            JsonObject gpus = gson.fromJson(reader, JsonObject.class);
            JsonArray gpusList = gson.fromJson(jsonObject.get("GPUS"), JsonArray.class);
            //List<GPU> gpusObjList = new LinkedList<GPU>();
            int counter = 0;
            for (JsonElement gpuElement: gpusList){
                GPU gpu = new GPU(stringToGPUType(gpuElement.getAsString()));
                gpusObjList.add(gpu);        //update the constructor  ********

                //Create student service
                GPUService gpuService = new GPUService("GPU Service",gpu);
                gpuService.registration();
                threads.add(new Thread(gpuService, "GPU Service - "+counter));
                counter++;
            }

            // Create cpu objects
            JsonObject cpus = gson.fromJson(reader, JsonObject.class);
            JsonArray cpusList = gson.fromJson(jsonObject.get("CPUS"), JsonArray.class);
            //List<CPU> cpusObjList = new LinkedList<CPU>();
            counter=0;
            for (JsonElement cpuElement: cpusList){
                CPU cpu = new CPU(cpuElement.getAsInt());
                cpusObjList.add(cpu);     //update the constructor  ********
                CPUService cpuService = new CPUService("CPU Service",cpu);
                cpuService.registration();
                threads.add(new Thread(cpuService,"CPU Service - "+counter));
                counter++;
            }

            //Need to add TickTime and Duration *****************************
            int tick = jsonObject.get("TickTime").getAsInt();
            int duration = jsonObject.get("Duration").getAsInt();
            TimeService timeService = new TimeService("Time Service", tick,duration);
            timeService.registration();
            timeServiceThread = new Thread(timeService, "Time Service");
            //threads.add(timeService);

            // Create conference objects
            JsonObject conferences = gson.fromJson(reader, JsonObject.class);
            JsonArray conferencesList = gson.fromJson(jsonObject.get("Conferences"), JsonArray.class);
            //List<ConfrenceInformation> conferencesObjList = new LinkedList<ConfrenceInformation>();
            for (JsonElement conferenceElement: conferencesList){
                JsonObject conferenceJsonObj = conferenceElement.getAsJsonObject();
                ConfrenceInformation confrenceInformation = new ConfrenceInformation(    //update the constructor  ********
                        conferenceJsonObj.get("name").getAsString(),
                        conferenceJsonObj.get("date").getAsInt());

                ConferenceService conferenceService = new ConferenceService("Conference Service",confrenceInformation);
                conferenceService.registration();
                threads.add(new Thread(conferenceService,"Conference - "+confrenceInformation.getName()));
                conferencesObjList.add(confrenceInformation);
            }
        }

        catch (Exception e){

        }

        Cluster.getInstance().setCPUs((LinkedList<CPU>) cpusObjList);
        Cluster.getInstance().setGPUs((LinkedList<GPU>) gpusObjList);

        //Start all threads

        for (Thread t : threads) {
            t.start();
        }

        timeServiceThread.start();


        //Join all threads
        try {
            timeServiceThread.join();
            for (Thread t : threads) {
                if (t.isAlive())
                    t.join();
            }
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("done");


        // *************** Output *****************

        JSONObject output = new JSONObject();
        JSONArray studentsOutput = new JSONArray();
        for (Student s: studentsObjectsList) {
            JSONObject temp = new JSONObject();
            temp.put("name", s.getName());
            temp.put("department" , s.getDepartment());
            if (s.getStatus()==Student.Degree.MSc)
                temp.put("status", "MSc");
            else
                temp.put("status", "PhD");
            temp.put("publications", s.getPublications());
            temp.put("papersRead", s.getPapersRead());

            JSONArray trainedModels = new JSONArray();
            for (Model m: s.getStudentModels()) {
                JSONObject modeltmp = new JSONObject();
                if(m.getStatus()!=Model.Status.PreTrained && m.getStatus()!=Model.Status.Training){
                    modeltmp.put("name", m.getName());
                    JSONObject datatmp = new JSONObject();
                    datatmp.put("type", typeToString(m.getData().getType()));
                    datatmp.put("size", m.getData().getSize());
                    modeltmp.put("data", datatmp);
                    if (m.getResult()!=Model.Result.None) {
                        modeltmp.put("status", "Tested");
                    }
                    else {
                        modeltmp.put("status", "Trained");
                    }
                    modeltmp.put("results", resultToString(m.getResult()));
//                    if(m.getStatus()==Model.Status.Trained) {
//                        modeltmp.put("status", "Trained");
//                    }
//                    else if (m.getStatus()==Model.Status.Tested) {
//                        modeltmp.put("status", "Tested");
//                    }
//                    modeltmp.put("results", resultToString(m.getResult()));
                    trainedModels.add(modeltmp);
                }
            }
            temp.put("trainedModels", trainedModels);
            studentsOutput.add(temp);
        }
        output.put("students", studentsOutput);

        JSONArray confsOutput = new JSONArray();
        for (ConfrenceInformation c: conferencesObjList) {
            JSONObject temp = new JSONObject();
            temp.put("name", c.getName());
            temp.put("date" , c.getDate());
            JSONArray publications = new JSONArray();
            for (Model m: c.getModels()) {
                JSONObject modeltmp = new JSONObject();
                modeltmp.put("name", m.getName());
                JSONObject datatmp = new JSONObject();
                datatmp.put("type", typeToString(m.getData().getType()));
                datatmp.put("size", m.getData().getSize());
                modeltmp.put("data", datatmp);
                modeltmp.put("status", "Tested");
                modeltmp.put("results", resultToString(m.getResult()));
                publications.add(modeltmp);
            }
            temp.put("publications", publications);
            confsOutput.add(temp);
        }
        output.put("conferences", confsOutput);


        Cluster cluster = Cluster.getInstance();
        int cpusTotalUsageTIme = cluster.getCPUTimeUnits();
        int gpusTotalUsageTIme = cluster.getGPUTimeUnits();
        int totalDBProcessed = cluster.getTotalDBProcessed();

        output.put("cpuTimeUsed", cpusTotalUsageTIme);
        output.put("gpuTimeUsed", gpusTotalUsageTIme);
        output.put("batchesProcessed" , totalDBProcessed);

        FileWriter outputFile = null;
        try {
            outputFile = new FileWriter("output_file.json");
            outputFile.write(output.toJSONString());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                outputFile.flush();
                outputFile.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}


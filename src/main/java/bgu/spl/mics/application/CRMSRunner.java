//package bgu.spl.mics.application;
//
//import bgu.spl.mics.application.objects.*;
//import com.google.gson.Gson;
//import com.google.gson.JsonArray;
//import com.google.gson.JsonElement;
//import com.google.gson.JsonObject;
//
//import java.io.Reader;
//import java.nio.file.Files;
//import java.nio.file.Paths;
//import java.util.ArrayList;
//import java.util.LinkedList;
//import java.util.List;
//import java.util.Map;
//import java.util.concurrent.CompletionService;
//import java.util.concurrent.ExecutorCompletionService;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//
///** This is the Main class of Compute Resources Management System application. You should parse the input file,
// * create the different instances of the objects, and run the system.
// * In the end, you should output a text file.
// */
//public class CRMSRunner {
//    public static void main(String[] args) {
//        Gson gson = new Gson();
//        String filePath = "example_input.json";    //Check what the filePath should be ???????
//        try{
//            Reader reader = Files.newBufferedReader(Paths.get(filePath));
//            JsonObject students = gson.fromJson(reader, JsonObject.class);
//            //Each object in the jsonArray is from type jsonElement
//            JsonArray studentsList = gson.fromJson(students.get("Students"), JsonArray.class);
//            List<Student> studentsObjList = new LinkedList<>();
//            List<Model> modelsObjList = new LinkedList<>();
//            //Transfer each jsonElement to jsonObject
//            for(JsonElement student: studentsList){
//                JsonObject studentJsonObj = student.getAsJsonObject();
//
//                // create student object
//                Student s = new Student(studentJsonObj.get("name").getAsString(),  //update the constructor  ********
//                        studentJsonObj.get("department").getAsString(),
//                        studentJsonObj.get("status").getAsString());
//
//                studentsObjList.add(s);
//
//                // Create model object
//                JsonArray modelsList = gson.fromJson(students.get("models"), JsonArray.class);
//                for(JsonElement model: modelsList){
//                    JsonObject modelJsonObj = model.getAsJsonObject();
//
//                    // Create Data object
//                    Data data = new Data(modelJsonObj.get("type").getAsString(),  //update the constructor  ********
//                            modelJsonObj.get("size").getAsInt());
//
//                    // Create model object
//                    Model m = new Model(modelJsonObj.get("name").getAsString(),       //update the constructor  ********
//                            data,
//                            s);
//                    modelsObjList.add(m);
//                }
//                //Add the data to model and the model to Student!! *****************
//            }
//
//            // Create gpu objects
//            JsonObject gpus = gson.fromJson(reader, JsonObject.class);
//            JsonArray gpusList = gson.fromJson(students.get("GPUS"), JsonArray.class);
//            List<GPU> gpusObjList = new LinkedList<GPU>();
//            for (JsonElement gpu: gpusList){
//                gpusObjList.add(new GPU(gpu.getAsString()));        //update the constructor  ********
//            }
//
//            // Create cpu objects
//            JsonObject cpus = gson.fromJson(reader, JsonObject.class);
//            JsonArray cpusList = gson.fromJson(students.get("CPUS"), JsonArray.class);
//            List<CPU> cpusObjList = new LinkedList<CPU>();
//            for (JsonElement cpu: gpusList){
//                cpusObjList.add(new CPU(cpu.getAsInt()));     //update the constructor  ********
//            }
//
//            // Create conference objects
//            JsonObject conferences = gson.fromJson(reader, JsonObject.class);
//            JsonArray conferencesList = gson.fromJson(students.get("Conferences"), JsonArray.class);
//            List<ConfrenceInformation> conferencesObjList = new LinkedList<ConfrenceInformation>();
//            for (JsonElement conference: conferencesList){
//                JsonObject conferenceJsonObj = conferences.getAsJsonObject();
//                ConfrenceInformation confrenceInformation = new ConfrenceInformation(    //update the constructor  ********
//                        conferenceJsonObj.get("name").getAsString(),
//                        conferenceJsonObj.get("date").getAsInt());
//
//                conferencesObjList.add(confrenceInformation);
//            }
//
//            //Need to add TickTime and Duration *****************************
//        }
//        catch (Exception e){
//
//        }
//    }
//}

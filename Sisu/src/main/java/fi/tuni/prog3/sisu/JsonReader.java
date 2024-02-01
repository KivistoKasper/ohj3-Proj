/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Other/File.java to edit this template
 */
package fi.tuni.prog3.sisu;

import java.util.HashMap;
import java.io.File;
import java.util.ArrayList;
import java.io.FileReader;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import java.io.FileWriter;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.util.Scanner;
import java.util.ArrayDeque;
/**
 *
 * @author joona
 */
public class JsonReader implements iReadAndWriteToFile, iAPI{
    
    private HashMap<String, CourseModule> courses = new HashMap<String, CourseModule>(); 
    private HashMap<String, GrouppingModule> groups = new HashMap<String, GrouppingModule>();
    private ArrayList<String> degreeProgrammeIds = new ArrayList<String>();
    private HashMap<String, Boolean> selectedCoursesActive;
    private String nameActive;
    private String studentNumberActive;

    /**
     * Metodi joka lukee urlista jsonobjecti olion
     * @param urlString, url osoite
     * @return urlin JsonObject, joka saadaan parsittua
     */
    @Override
    public JsonObject getJsonObjectFromApi(String urlString) {
            try
            {
                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                Scanner scanner = new Scanner(url.openStream());
                Gson gson = new Gson();
                String json = scanner.nextLine();
                JsonElement rawData = gson.fromJson(json, JsonElement.class);
                scanner.close();
                if(rawData.isJsonArray())
                {
                    return rawData.getAsJsonArray().get(0).getAsJsonObject();
                }else
                {
                    return rawData.getAsJsonObject();
                }                           
            }catch(Exception e)
            {
                return null;
            }
    }
    
    public class UserProfile
    {
        public UserProfile(String name, String studentNumber, HashMap<String, Boolean> selectedCourses)
        {
            this.name = name;
            this.studentNumber = studentNumber;
            this.selectedCourses = selectedCourses;
        }
        public String name;
        public String studentNumber;
        public HashMap<String, Boolean> selectedCourses;
    }

    public class DegreeInfo
    {
        public String code;
        public String id;
        public String groupId;
        public String type;
        public HashMap<String, String> name;
        public HashMap<String, Integer> credits;
        public JsonObject rule;
    }
    
    /**
     * Paikallisten tiedostojen hakemiseen käytetty metodi. Ei käytössä.
     * @return boolean, onnistumisen tulos
     * @throws Exception, jos tiedostoissa on vikoja.
     */
    public boolean readAllLocalFiles() throws Exception 
    {
        String coursePath = "../json/courseunits/";
        String setPath = "../json/modules/";
        File dir = new File(coursePath);
        File[] directoryListing = dir.listFiles();
        if (directoryListing != null) {
            for (File child : directoryListing) {
                readFromFile(coursePath + child.getName());
            }
        }
        File dir2 = new File(setPath);
        File[] directoryListing2 = dir2.listFiles();
        if (directoryListing2 != null) {
            for (File child : directoryListing2) {
                readFromFile(setPath + child.getName());
            }
        }
        return true;
    }
    /**
     * ReadAllLocalFiles apumetodi
     * @param fileName
     * @return boolean, onnistumisen tulos
     * @throws Exception jos tiedostoissa on vikoja
     */
    @Override
    public boolean readFromFile(String fileName) throws Exception {
        Gson gson = new Gson();
        DegreeInfo info = gson.fromJson(new FileReader(fileName), DegreeInfo.class);
        if(info.type == null)
        {
            info.type = "CourseUnit";
        }
        if(info.type.equals("GroupingModule") || info.type.equals("StudyModule") || info.type.equals("DegreeProgramme"))
        {
            if(info.type.equals("DegreeProgramme"))
            {
                degreeProgrammeIds.add(info.id);
            }
            GrouppingModule myModule = infoToModule(info);
            groups.put(info.groupId, myModule);
        }else
        {
            CourseModule myModule = new CourseModule(info.name.get("fi"), info.id, info.groupId, info.credits.get("min"), info.code);
            courses.put(info.groupId, myModule);
        }   
        return true;
    }
    /**
     * Metodi jolla luetaan tiedot netin sisuApista.
     * @return boolean, jos onnistui
     */

    public boolean readFromSisu() 
    {
        try
        {

            JsonObject data = getJsonObjectFromApi("https://sis-tuni.funidata.fi/kori/api/module-search?curriculumPeriodId=uta-lvv-2021&universityId=tuni-university-root-id&moduleType=DegreeProgramme&limit=5");
            JsonArray array = data.getAsJsonArray("searchResults");
            for(JsonElement e : array)
            {
                if(e.getAsJsonObject().get("groupId").getAsString() != null)
                {
                    degreeProgrammeIds.add(e.getAsJsonObject().get("groupId").getAsString());                    
                }
            }
            ArrayDeque<String> deck = new ArrayDeque<String>();
            deck.addAll(degreeProgrammeIds);
            while(!deck.isEmpty())
            {
                String id = deck.pop();
                JsonObject jsonFromSisu = getJsonObjectFromApi("https://sis-tuni.funidata.fi/kori/api/modules/by-group-id?groupId=" + id + "&universityId=tuni-university-root-id");
                if(jsonFromSisu != null)
                {
                    Gson gsonId = new Gson();
                    DegreeInfo infoFromSisu = gsonId.fromJson(jsonFromSisu, DegreeInfo.class);
                    GrouppingModule module = infoToModule(infoFromSisu);
                    for(String childId : module.getChildIds())
                    {
                        deck.addLast(childId);
                    }   
                    System.out.println(module.getName());
                    groups.put(module.getGroupId(), module);
                }else
                {
                   

                    Gson gsonCourseId = new Gson();
                    JsonElement jsonFromSisu2 = getJsonObjectFromApi("https://sis-tuni.funidata.fi/kori/api/course-units/by-group-id?groupId=" + id + "&universityId=tuni-university-root-id");
                    DegreeInfo infoFromSisu = gsonCourseId.fromJson(jsonFromSisu2, DegreeInfo.class);
                    CourseModule myModule = new CourseModule(infoFromSisu.name.get("fi"), infoFromSisu.id, infoFromSisu.groupId, infoFromSisu.credits.get("min"), infoFromSisu.code);
                    courses.put(infoFromSisu.groupId, myModule);
                }

            }
            return true;
                    
        }catch(Exception e)
        {
            return false;
        }
        
    }
    /**
     * apumetodi DegreeInfo metodit muokkaaminen GrouppingModuleksi
     * @param info
     * @return GrouppingModule, joka on muokattu DegreeInfosta.
     */
    private GrouppingModule infoToModule(DegreeInfo info)
    {
        ArrayList<String> groupIds = new ArrayList<String>();
            JsonArray objects = new JsonArray();
            if(info.rule != null)
            {
                if(info.rule.getAsJsonArray("rules") != null)
                {
                    objects = info.rule.getAsJsonArray("rules");
                    if(objects.get(0).getAsJsonObject().getAsJsonArray("rules") != null){
                        objects = objects.get(0).getAsJsonObject().getAsJsonArray("rules");
                    }
                
                }else if(info.rule.getAsJsonObject("rule") != null)
                {
                    info.rule = info.rule.getAsJsonObject("rule");
                    if(info.rule.getAsJsonArray("rules") != null)
                    {
                        objects = info.rule.getAsJsonArray("rules");
                        if(objects.get(0).getAsJsonObject().getAsJsonArray("rules") != null)
                        {
                            objects = objects.get(0).getAsJsonObject().getAsJsonArray("rules");
                        }
                    }           
                }
            }
            
            if(objects != null)
            {
                for(JsonElement jobj : objects)
                {
                    JsonObject child = jobj.getAsJsonObject();
                    var childOfChild = child.get("courseUnitGroupId");
                    if(childOfChild != null)
                    {
                        groupIds.add(childOfChild.getAsString());                       
                    }else
                    {
                        childOfChild = child.get("moduleGroupId");
                        if(childOfChild != null)
                        {
                            groupIds.add(childOfChild.getAsString());   
                        }
                    }
                }
            }
            GrouppingModule myModule;
            if(info.name.get("fi") != null)
            {
                myModule = new GrouppingModule(info.name.get("fi"), info.id, info.groupId, groupIds);
            }else if(info.name.get("en") != null)
            {
                myModule = new GrouppingModule(info.name.get("en"), info.id, info.groupId, groupIds);               
            }else
            {
                myModule = new GrouppingModule("Error", info.id, info.groupId, groupIds);  
            }
            
            return myModule;
    
    }
    /**
     * Metodi joka etsii käyttäjän jsontiedoston projektin hakemistosta.
     * @param name, opiskelijan nimi
     * @param studentNumber, opiskelija numero
     * @return boolean, onnistumisen tulos
     */
    
    public boolean findUserInformation(String name, String studentNumber)
    {
        
        try
        {
            File profileFile = new File("../json/userprofiles/" + name + studentNumber + ".json");
            if(profileFile.exists())
            {
                Gson gson = new Gson();
                UserProfile profile = gson.fromJson(new FileReader(profileFile), UserProfile.class);
                selectedCoursesActive = profile.selectedCourses;
            }else
            {
                return false;
            }
            nameActive = name;
            studentNumberActive = studentNumber;           
            return true;
            
        }catch(Exception e)
        {
            return false;
        }

    }
     /**
      * Käyttäjän tietojen päivittäminen
      * @param fileName, tiedoston nimi (esiehtona nimi täytyy olla nimi + opiskelijanumero .json muodossa)
      * @return boolean, kirjoittamisen onnistumisen tulos
      * @throws Exception, jos tiedostojen kirjoittamisessa tapahtuu virhe.
      */   
    @Override
    public boolean writeToFile(String fileName) throws Exception {
        try
        {
            if(nameActive != null && studentNumberActive != null)
            {
                Gson gson = new Gson();
                UserProfile profileToSave = new UserProfile(nameActive, studentNumberActive, selectedCoursesActive);
                FileWriter writer = new FileWriter("../json/userprofiles/" + fileName);
                gson.toJson(profileToSave, writer);
                writer.flush();
                writer.close();
                return true;
            }else
            {
                return false;
            }
            
        }catch(Exception e)
        {
            return false;
        }
    }
    
    public void setActiveUserProfile(String name, String studentNumber, HashMap<String, Boolean> selectedCourses)
    {
        nameActive = name;
        studentNumberActive = studentNumber;
        selectedCoursesActive = selectedCourses;
    }
    
    public HashMap<String, CourseModule> getCourses()
    {
        return courses;
    }
    
    public HashMap<String, GrouppingModule> getGrouppings()
    {
        return groups;
    }
    
    public ArrayList<String> getDegreeProgrammeIds()
    {
        return degreeProgrammeIds;
    }
    
    public String getStudentNumberActive()
    {
        return studentNumberActive;
    }
    
    public String getStudentNameActive()
    {
        return nameActive;
    }
    
    public HashMap<String, Boolean> getSelectedCoursesActive()
    {
        return selectedCoursesActive;
    }
}

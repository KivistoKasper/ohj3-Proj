/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package fi.tuni.prog3.sisu;


import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author joona
 */
public class JsonReaderTest {
    

    @Test
    public void testReadOneFile()
    {
        JsonReader reader1 = new JsonReader();
        try{
            assertEquals(true,reader1.readFromFile("../json/courseunits/otm-1dc4fc64-39fd-4575-aef6-280199870f71.json"));
            HashMap<String, CourseModule> courses = reader1.getCourses();
        }catch(Exception ex)
        {
            assertEquals(true, false);
        }
    }
    
    @Test
    public void testReadingAllCourseUnits()
    {
        JsonReader reader1 = new JsonReader();
        try{
            reader1.readAllLocalFiles();
            HashMap<String, CourseModule> courses = reader1.getCourses();
        }catch(Exception ex)
        {
            assertEquals(true, false);
        }
    }
    
    @Test
    public void testReadingFDromSisu()
    {
        JsonReader reader1 = new JsonReader();
        reader1.readFromSisu();
        
    }
    
    @Test
    public void testWritingToFile()
    {
        JsonReader reader1 = new JsonReader();
        HashMap<String, Boolean> activeCourses = new HashMap<String, Boolean>();
        activeCourses.put("Yliopistofysiikka 1", Boolean.FALSE);

        try
        {
            reader1.setActiveUserProfile(null, null, null);
            /*reader1.writeToFile("TaaviH1234.json");*/
        }catch(Exception e)
        {
            
        }
        
        
            
    }
    
}

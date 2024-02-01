/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package fi.tuni.prog3.sisu;

/**
 *
 * @author Kasper PC
 */
public class CourseModule extends DegreeModule{
    
    private String code;
    public CourseModule(String name, String id, String groupId, int minCredits, String code) {
        super(name, id, groupId, minCredits);
        this.code = code;
    }
    public String getCode()
    {
        return code;
    }
    
}


package fi.tuni.prog3.sisu;


import java.util.ArrayList;
/**
 *
 * @author joona
 */
public class GrouppingModule{

    private ArrayList<String> childIds;
    private String name;
    private String id;
    private String groupId;
    
    public GrouppingModule(String name, String id, String groupid, ArrayList<String> childIds) {
        this.name = name;
        this.id = id;
        this.groupId = groupid;
        this.childIds = childIds;
    }

    public ArrayList<String> getChildIds()
    {
        return childIds;
    }
    
    public String getName()
    {
        return name;
    }
    
    public String id()
    {
        return id;
    }
    
    public String getGroupId()
    {
        return groupId;
    }
}

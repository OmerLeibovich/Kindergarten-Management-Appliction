package Objects;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class GardenClass implements Serializable {
    private String id; // Add this field
    private String courseNumber;
    private String courseType;
    private int maxChildren;
    private int minAge;
    private int maxAge;
    private Map<String, ChildStatus> children = new HashMap<>();
    // מפה של ילדים עם KEY בוליאני

    public GardenClass() {
        this.children = new HashMap<>(); // אתחול המפה של הילדים
    }

    public GardenClass(String id, String courseNumber, String courseType, int maxChildren, int minAge, int maxAge) {
        this.id = id;
        this.courseNumber = courseNumber;
        this.courseType = courseType;
        this.maxChildren = maxChildren;
        this.minAge = minAge;
        this.maxAge = maxAge;
        this.children = new HashMap<>(); // אתחול המפה של הילדים
    }

    // Getters and setters for all fields, including the new id field
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCourseNumber() {
        return courseNumber;
    }

    public void setCourseNumber(String courseNumber) {
        this.courseNumber = courseNumber;
    }

    public String getCourseType() {
        return courseType;
    }

    public void setCourseType(String courseType) {
        this.courseType = courseType;
    }

    public int getMaxChildren() {
        return maxChildren;
    }

    public void setMaxChildren(int maxChildren) {
        this.maxChildren = maxChildren;
    }

    public int getMinAge() {
        return minAge;
    }

    public void setMinAge(int minAge) {
        this.minAge = minAge;
    }

    public int getMaxAge() {
        return maxAge;
    }

    public void setMaxAge(int maxAge) {
        this.maxAge = maxAge;
    }

    public Map<String, ChildStatus> getChildren() {
        return children;
    }

    public void setChildren(Map<String, ChildStatus> children) {
        this.children = children;
    }

    // Other getters and setters
}

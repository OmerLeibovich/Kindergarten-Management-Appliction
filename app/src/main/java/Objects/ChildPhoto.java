package Objects;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ChildPhoto {

    private String ImageURL;

    private String ClassName;

    private Date Time;

    private String ChildId;


    public ChildPhoto() {

    }


    public ChildPhoto(String imageURL, String className, Date time, String childId) {
        ImageURL = imageURL;
        ClassName = className;
        Time = time;
        ChildId = childId;
    }

    public String getImageURL() {
        return ImageURL;
    }

    public void setImageURL(String imageURL) {
        ImageURL = imageURL;
    }

    public String getClassName() {
        return ClassName;
    }

    public void setClassName(String className) {
        ClassName = className;
    }

    public Date getTime() {
        return Time;
    }

    public void setTime(Date time) {
        Time = time;
    }

    public String getChildId() {
        return ChildId;
    }

    public void setChildId(String childId) {
        ChildId = childId;
    }
}


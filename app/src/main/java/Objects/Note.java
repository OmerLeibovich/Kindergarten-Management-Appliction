package Objects;

import java.util.Date;

public class Note {
    private String note;
    private Date date;
    private String authorName;
    private String authorRole;
    private String courseType; // סוג הקורס
    private Integer rating; // דירוג

    public Note() {
    }

    public Note(String note, Date date, String authorName, String authorRole, String courseType, Integer rating) {
        this.note = note;
        this.date = date;
        this.authorName = authorName;
        this.authorRole = authorRole;
        this.courseType = courseType;
        this.rating = rating;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getAuthorRole() {
        return authorRole;
    }

    public void setAuthorRole(String authorRole) {
        this.authorRole = authorRole;
    }

    public String getCourseType() {
        return courseType;
    }

    public void setCourseType(String courseType) {
        this.courseType = courseType;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }
}

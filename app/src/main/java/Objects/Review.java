package Objects;

import java.util.Date;

public class Review {
    private String parentEmail;
    private int rating;  // דירוג בין 1 ל-10
    private String comment;  // ביקורת שההורה כתב
    private String managerResponse;  // תגובה מהמנהל
    private Date reviewDate;  // תאריך הביקורת

    public Review() {
        // קונסטרוקטור ריק נדרש לפיירבייס
    }

    public Review(String parentEmail, int rating, String comment) {
        this.parentEmail = parentEmail;
        this.rating = rating;
        this.comment = comment;
        this.managerResponse = null;  // תגובת המנהל תתווסף בהמשך
        this.reviewDate = new Date();  // תאריך הביקורת נקבע לזמן הנוכחי
    }

    // Getters and Setters

    public String getParentEmail() {
        return parentEmail;
    }

    public void setParentEmail(String parentId) {
        this.parentEmail = parentId;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getManagerResponse() {
        return managerResponse;
    }

    public void setManagerResponse(String managerResponse) {
        this.managerResponse = managerResponse;
    }

    public Date getReviewDate() {
        return reviewDate;
    }

    public void setReviewDate(Date reviewDate) {
        this.reviewDate = reviewDate;
    }
}

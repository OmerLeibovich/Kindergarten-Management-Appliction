package Objects;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Garden implements Serializable {
    private String id;
    private String name;
    private String address;
    private String city;
    private String phoneNumber;
    private String openTime;
    private String closeTime;
    private String organizationalAffiliation;
    private String imageUrl;
    private List<GardenClass> classes;
    private Map<String, ChildStatus> children = new HashMap<>();
    private List<Review> reviews;
    private double averageRating;
    private boolean isRegistered;
    private Date registrationStartDate;

    private String status;

    public Garden() {
        this.isRegistered = false;
        this.children = new HashMap<>();
        this.reviews = new ArrayList<>();
        this.status = null;
    }

    public Garden(String name, String address, String city, String phoneNumber, String openTime, String closeTime, String organizationalAffiliation, String imageUrl) {
        this.name = name;
        this.address = address;
        this.city = city;
        this.phoneNumber = phoneNumber;
        this.openTime = openTime;
        this.closeTime = closeTime;
        this.organizationalAffiliation = organizationalAffiliation;
        this.imageUrl = imageUrl;
        this.classes = new ArrayList<>();
        this.children = new HashMap<>(); // אתחול של המפה של הילדים
        this.isRegistered = false;
        this.registrationStartDate = null;
        this.status = null; // בדיפולט הסטטוס הוא null
    }

    public Garden(String id, String name) {
        this.id = id;
        this.name = name;
        this.children = new HashMap<>();
        this.isRegistered = false;
        this.registrationStartDate = null;
        this.status = null;
    }
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getOpenTime() {
        return openTime;
    }

    public void setOpenTime(String openTime) {
        this.openTime = openTime;
    }

    public String getCloseTime() {
        return closeTime;
    }

    public void setCloseTime(String closeTime) {
        this.closeTime = closeTime;
    }

    public String getOrganizationalAffiliation() {
        return organizationalAffiliation;
    }

    public void setOrganizationalAffiliation(String organizationalAffiliation) {
        this.organizationalAffiliation = organizationalAffiliation;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public List<GardenClass> getClasses() {
        return classes;
    }

    public void setClasses(List<GardenClass> classes) {
        this.classes = classes;
    }

    public Map<String, ChildStatus> getChildren() {
        return children;
    }

    public void setChildren(Map<String, ChildStatus> children) {
        this.children = children;
    }

    public boolean isRegistered() {
        return isRegistered;
    }

    public void setRegistered(boolean isRegistered) {
        this.isRegistered = isRegistered;
    }

    public Date getRegistrationStartDate() {
        return registrationStartDate;
    }

    public void setRegistrationStartDate(Date registrationStartDate) {
        this.registrationStartDate = registrationStartDate;
    }

    public List<Review> getReviews() {
        return reviews;
    }

    public void setReviews(List<Review> reviews) {
        this.reviews = reviews;
    }

    public void addReview(Review review) {
        if (this.reviews == null) {
            this.reviews = new ArrayList<>();
        }
        this.reviews.add(review);
    }
    public double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(double averageRating) {
        this.averageRating = averageRating;
    }


    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }


}

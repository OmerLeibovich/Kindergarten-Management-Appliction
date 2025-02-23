package Objects;

import java.util.ArrayList;
import java.util.List;

public class Parent extends Person {

    private List<Child> children;
    private List<Review> reviews;


    public Parent() {
        this.reviews = new ArrayList<>();
    }

    public Parent(String email, String password, String name) {
        super(email, password, name);
        this.children = new ArrayList<>();
        this.reviews = new ArrayList<>();
    }

    public List<Child> getChildren() {
        return children;
    }

    public void addChild(Child child) {
        if (this.children == null) {
            this.children = new ArrayList<>();
        }
        this.children.add(child);
    }
    public void removeChild(Child child) {
        this.children.remove(child);
    }

    public void setChildren(List<Child> children) {
        this.children = children;
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
}

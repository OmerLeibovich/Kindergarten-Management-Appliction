package Objects;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;

public class GardenStaff extends Person implements Serializable {

    private String role;
    private Date startToWork;
    private Garden garden;
    private List<GardenClass> classes;

    public GardenStaff() {
        this.classes = new ArrayList<>();
    }




    public GardenStaff(String email, String password, String name, String role, Date startToWork) {
        super(email, password, name);
        this.role = role;
        this.startToWork = startToWork;
        this.classes = new ArrayList<>(); // מערך ריק של חוגים
    }

    public  String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Date getStartToWork() {
        return startToWork;
    }

    public void setStartToWork(Date startToWork) {
        this.startToWork = startToWork;
    }

    public Garden getGarten() {
        return garden;
    }

    public void setGarten(Garden garden) {
        this.garden = garden;
    }

    public List<GardenClass> getClasses() {
        return classes;
    }

    public void setClasses(List<GardenClass> classes) {
        this.classes = classes;
    }

    public void addClasses(GardenClass gardenClass) {
        this.classes.add(gardenClass);
    }
}

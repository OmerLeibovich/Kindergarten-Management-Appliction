package Objects;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;

public class GardenDirector extends GardenStaff {

    private List<Garden> kindergartens; // רשימה של גנים



    public GardenDirector(String email, String password, String name, String role, Date startToWork) {
        super(email, password, name, role, startToWork);
        this.kindergartens = new ArrayList<>();
    }

    public List<Garden> getKindergartens() {
        return kindergartens;
    }

    public void setKindergartens(List<Garden> kindergartens) {
        this.kindergartens = kindergartens;
    }

    public void addKindergarten(Garden garden) {
        this.kindergartens.add(garden);
    }
}

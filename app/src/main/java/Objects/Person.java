package Objects;

public class Person {
    private String email;
    private String password;
    private String name;

    // בנאי ללא ארגומנטים נדרש על ידי Firestore
    public Person() {
    }

    public Person(String email, String password, String name) {
        this.email = email;
        this.password = password;
        this.name = name;
    }



    // גטרים וסטרים
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public  String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

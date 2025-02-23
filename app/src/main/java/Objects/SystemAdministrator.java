package Objects;

public class SystemAdministrator extends Person {

    public SystemAdministrator(String email, String password, String name, String role) {
        super(email, password, name);
        this.role = role;
    }

    private String role;

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}

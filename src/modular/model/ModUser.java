package modular.model;

public abstract class ModUser {
    private String id;
    private String name;
    private String password;
    private String email;

    protected ModUser(String id, String name, String password) {
        this.id = id;
        this.name = name;
        this.password = password != null ? password : "password";
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public boolean verifyPassword(String candidate) {
        return candidate != null && candidate.equals(password);
    }
}

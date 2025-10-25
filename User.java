
public abstract class User {
    private String id;
    private String name;
    private String password;

    public User(String id, String name, String password) {
        this.id = id;
        this.name = name;
        this.password = password;
    }

    public boolean login() { return true; }
    public boolean verifyPassword(String password) { return true; }
    public void setPassword(String oldPw, String newPw) { }
}


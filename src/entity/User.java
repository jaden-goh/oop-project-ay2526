package entity;

import java.util.Scanner;

public abstract class User {
    private String id;
    private String name;
    private String password;
    private String email;

    public User(String id, String name, String password) {
        this.id = id;
        this.name = name;
        this.password = "password";
    }
    public void setId(String id){this.id = id;}
    public String getId() { return id; }
    public String getName() { return name; }
    public String getPassword(){return password;}
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public void setName(String name) {
        // if (name == null || name.isBlank()) throw new IllegalArgumentException("name required");
        // to be added later, returning any exceptions good for edge cases
        this.name = name;
    }
    public void setPassword(String password){this.password = password;}
    public boolean login() { return true; }
    public boolean verifyPassword(String userpassword) {
        return userpassword.equals(password);
    }
        
    public void changePassword() {
        System.out.println("Enter old password: ");
        Scanner scanner = new Scanner(System.in);
        String pw = scanner.nextLine(); 
        if (!pw.equals(this.password)) {
            System.out.println("Incorrect old password. Password change failed.");
        }
        else {
            System.out.println("Enter new password: ");
            this.password = scanner.nextLine();
            System.out.println("Password changed successfully.");
        }
    }

}


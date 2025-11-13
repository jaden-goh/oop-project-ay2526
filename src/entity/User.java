package entity;

import java.util.Scanner;

public abstract class User {
    private String id;
    private String name;
    private String password;

    public User(String id, String name, String password) {
        this.id = id;
        this.name = name;
        this.password = password;
    }
    public String getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) {
        // if (name == null || name.isBlank()) throw new IllegalArgumentException("name required");
        // to be added later, returning any exceptions good for edge cases
        this.name = name;
    }

    public boolean login() { return true; }
    public boolean verifyPassword(String password) { return true; }
    public void changePassword(String oldPw,String newPw) {
        System.out.println("Enter old password: ");
        Scanner scanner = new Scanner(System.in);
        String pw = scanner.nextLine(); 
        if (!pw.equals(oldPw)) {
            System.out.println("Incorrect old password. Password change failed.");
            return;
        }
        else {
            System.out.println("Password changed successfully.");
            this.password = newPw;
        }
    }
}


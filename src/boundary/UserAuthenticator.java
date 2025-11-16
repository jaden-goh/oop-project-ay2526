package boundary;

import entity.CareerCenterStaff;
import entity.Company;
import entity.CompanyRep;
import entity.Student;
import entity.User;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.regex.Pattern;

public class UserAuthenticator {
    private static final Pattern STUDENT_ID_PATTERN = Pattern.compile("^U\\d{7}[A-Za-z]$");
    private static final String COMPANY_REP_CSV = "data/sample_company_representative_list.csv";
    private final Scanner scanner;
    private final InternshipSystemCLI cli;

    public UserAuthenticator(Scanner scanner, InternshipSystemCLI cli) {
        this.scanner = scanner;
        this.cli = cli;
    }

    public void handleStudentLogin() {
        System.out.print("Enter Student ID: ");
        String id = scanner.nextLine().trim();
        if (!STUDENT_ID_PATTERN.matcher(id).matches()) {
            System.out.println("Invalid student ID.");
            return;
        }
        User user = findUserById(id, Student.class);
        if (user == null) {
            System.out.println("Student not found.");
            return;
        }
        System.out.println("Password: ");
        String password = scanner.nextLine().trim();
        if (!user.verifyPassword(password)){
            System.out.println("Incorrect Password");
            return;
        }
        cli.setCurrentUser(user);
        System.out.println("Welcome, " + user.getName());
        cli.displayStudentMenu((Student)user);
        cli.setCurrentUser(null);
    }

    public void handleCompanyRepLogin() {
        System.out.print("Enter Company Rep ID: ");
        String id = scanner.nextLine().trim();

        User user = findUserById(id, CompanyRep.class);
        if (user == null) {
            System.out.println("Company Rep not found.");
            return;
        }
        String[] record = getCompanyRepRecord(id);
        if (record == null) {
            System.out.println("Company record not found. Please contact the career center for assistance.");
            return;
        }        
        System.out.println(("Enter Password: "));
        String password = scanner.nextLine().trim();
        if (!user.verifyPassword(password)){
            System.out.println("Incorrect Password");
            return;
        }
        if (!isApprovedStatus(record[6])) {
            System.out.println("Your account is pending approval. Please wait for Career Center Staff to approve your registration.");
            return;
        }
        System.out.println("Welcome, " + user.getName());
        cli.displayRepMenu((CompanyRep)user);
    }

    public void handleCareerStaffLogin() {
        System.out.print("Enter Career Staff ID: ");
        String id = scanner.nextLine().trim();
        User user = findUserById(id, CareerCenterStaff.class);
        if (user == null) {
            System.out.println("Staff not found.");
            return;
        }
        System.out.print("Password: ");
        String password  = scanner.nextLine().trim();
        if (!user.verifyPassword(password)){
            System.out.println("Incorrect Password");
            return;
        }
        cli.setCurrentUser(user);
        System.out.println("Welcome, " + user.getName());
        cli.displayStaffMenu((CareerCenterStaff)user);
        cli.setCurrentUser(null);
    }

    public void register() {
        System.out.println("I am a:\n1. Student\n2. Company Representative\n3. Career Center Staff\n4. Return");
        String choice = scanner.nextLine();
        if (choice.equals("1")){
            System.out.println("=== Student Registration ===");

            System.out.print("Enter Student ID: ");
            String studentID = scanner.nextLine().trim();

            System.out.print("Enter Name: ");
            String name = scanner.nextLine().trim();

            System.out.print("Enter Major: ");
            String major = scanner.nextLine().trim();

            System.out.print("Enter Year of Study: ");
            String year = scanner.nextLine();

            System.out.print("Enter Email: ");
            String email = scanner.nextLine().trim();

            System.out.println("Confirm your details:");
            System.out.println("Student ID: " + studentID);
            System.out.println("Name: " + name);
            System.out.println("Major: " + major);
            System.out.println("Year of Study: " + year);
            System.out.println("Email: " + email);
            System.out.print("Is the information correct? (Y/N): ");
            String confirm = scanner.nextLine().trim();
            if (confirm.equalsIgnoreCase("N")) {
                System.out.println("Registration cancelled. Please start over.");
                return;
            }
            if (confirm.equalsIgnoreCase("Y")) {
                System.out.println("Proceeding with registration...");
            } else {
                System.out.println("Invalid input. Registration cancelled.");
                return;
            }
            if (recordExists("data/sample_student_list.csv", 0, studentID)) {
                System.out.println("A student with this ID already exists. Please log in instead.");
                return;
            }

            File file = new File("data/sample_student_list.csv");
            boolean writeHeader = !file.exists() || file.length() == 0;

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {

                if (writeHeader) {
                    writer.write("StudentID,Name,Major,Year,Email");
                    writer.newLine();
                }

                String record = String.join(",", studentID, name, major, year, email);

                writer.write(record);
                writer.newLine();
                writer.flush();

                System.out.println("Registration completed! Welcome, " + name);
                Student newStudent = new Student(studentID, name, "", year, major);
                newStudent.setEmail(email);
                cli.getUsers().add(newStudent);

            } catch (IOException e) {
                System.out.println("Error writing student record: " + e.getMessage());
            }
        }

        if (choice.equals("2")){
            System.out.println("=== Company Representative Registration ===");

            System.out.print("Enter Company Email: ");
            String email = scanner.nextLine().trim();
            String companyrepid = extractEmailParts(email)[0];
            String companyname = extractEmailParts(email)[1];

            System.out.print("Enter Name: ");
            String name = scanner.nextLine().trim();


            System.out.print("Enter Department: ");
            String department = scanner.nextLine().trim();

            System.out.print("Enter Position: ");
            String position = scanner.nextLine();

            System.out.println("Confirm your details:");
            System.out.println("Company Rep ID: " + companyrepid);
            System.out.println("Name: " + name);
            System.out.println("Company Name: " + companyname);
            System.out.println("Department: " + department);
            System.out.println("Position: " + position);
            System.out.println("Email: " + email);
            System.out.print("Is the information correct? (Y/N): ");
            String confirm = scanner.nextLine().trim();
            if (confirm.equalsIgnoreCase("N")) {
                System.out.println("Registration cancelled. Please start over.");
                return;
            }
            if (confirm.equalsIgnoreCase("Y")) {
                System.out.println("Proceeding with registration...");
            } else {
                System.out.println("Invalid input. Registration cancelled.");
                return;
            }

            String approved = "false";

            if (recordExists("data/sample_company_representative_list.csv", 0, companyrepid)) {
                System.out.println("A company representative with this ID already exists. Please log in instead.");
                return;
            }

            File file = new File("data/sample_company_representative_list.csv");
            boolean writeHeader = !file.exists() || file.length() == 0;

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {

                if (writeHeader) {
                    writer.write("CompanyRepID,Name,CompanyName,Department,Position,Email,Approved");
                    writer.newLine();
                }

                String record = String.join(",", companyrepid,name,companyname,department,position,email,approved);
                writer.write(record);
                writer.newLine();
                writer.flush();

                System.out.println("Registration completed! Your Company Rep ID is: " + companyrepid + ". Please wait for approval from Career Center Staff before logging in.");
                CompanyRep newRep = new CompanyRep(email, name, "");
                newRep.setId(companyrepid);
                newRep.setEmail(email);
                newRep.setDepartment(department);
                newRep.setPosition(position);
                newRep.setAuthorised(false);
                Company company = new Company();
                company.setCompanyName(companyname);
                newRep.setCompany(company);
                cli.getUsers().add(newRep);

            } catch (IOException e) {
                System.out.println("Error writing student record: " + e.getMessage());
            }
        }

        if (choice.equals("3")){
            System.out.println("=== Career Staff Registration ===");

            System.out.print("Enter Staff ID: ");
            String staffid = scanner.nextLine().trim();

            System.out.print("Enter Name: ");
            String name = scanner.nextLine().trim();

            System.out.print("Enter Role: ");
            String role = scanner.nextLine().trim();

            System.out.print("Enter Department: ");
            String department = scanner.nextLine().trim();

            System.out.print("Enter Email: ");
            String email = scanner.nextLine().trim();

            System.out.println("Confirm your details:");
            System.out.println("Staff ID: " + staffid);
            System.out.println("Name: " + name);
            System.out.println("Role: " + role);
            System.out.println("Department: " + department);
            System.out.println("Email: " + email);
            System.out.print("Is the information correct? (Y/N): ");
            String confirm = scanner.nextLine().trim();
            if (confirm.equalsIgnoreCase("N")) {
                System.out.println("Registration cancelled. Please start over.");
                return;
            }
            if (confirm.equalsIgnoreCase("Y")) {
                System.out.println("Proceeding with registration...");
            } else {
                System.out.println("Invalid input. Registration cancelled.");
                return;
            }
            if (recordExists("data/sample_staff_list.csv", 0, staffid)) {
                System.out.println("A staff member with this ID already exists. Please log in instead.");
                return;
            }

            File file = new File("data/sample_staff_list.csv");
            boolean writeHeader = !file.exists() || file.length() == 0;

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {

                if (writeHeader) {
                    writer.write("StaffID,Name,Role,Department,Email");
                    writer.newLine();
                }

                String record = String.join(",", staffid, name, role, department, email);

                writer.write(record);
                writer.newLine();
                writer.flush();

                System.out.println("Registration completed! Welcome, " + name);
                CareerCenterStaff newStaff = new CareerCenterStaff(staffid, name, "", role, department, email);
                cli.getUsers().add(newStaff);

            } catch (IOException e) {
                System.out.println("Error writing student record: " + e.getMessage());
            }
        }
    }

    private User findUserById(String id, Class<? extends User> type) {
        for (User user : cli.getUsers()) {
            if (type.isInstance(user) && user.getId().equalsIgnoreCase(id)) {
                return user;
            }
        }
        return null;
    }

    private String[] getCompanyRepRecord(String id) {
        Path path = Paths.get(COMPANY_REP_CSV);
        if (!Files.exists(path)) {
            return null;
        }
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String line = reader.readLine();
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;
                String[] columns = line.split(",", -1);
                if (columns.length < 7) continue;
                if (columns[0].equalsIgnoreCase(id)) {
                    return columns;
                }
            }
        } catch (IOException e) {
            System.out.println("Failed to read company representative list: " + e.getMessage());
        }
        return null;
    }

    private boolean isApprovedStatus(String status) {
        return "true".equalsIgnoreCase(status) || "approved".equalsIgnoreCase(status);
    }

    private boolean recordExists(String filePath, int columnIndex, String value) {
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            return false;
        }
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            reader.readLine(); // header
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;
                String[] columns = line.split(",", -1);
                if (columns.length <= columnIndex) continue;
                if (columns[columnIndex].equalsIgnoreCase(value)) {
                    return true;
                }
            }
        } catch (IOException e) {
            System.out.println("Failed to verify existing records: " + e.getMessage());
        }
        return false;
    }

    public static String[] extractEmailParts(String email) {
        if (email == null) return null;

        int atIndex = email.indexOf("@");
        int dotIndex = email.lastIndexOf(".");

        if (atIndex == -1 || dotIndex == -1 || dotIndex < atIndex) {
            return null; // invalid email
        }

        String user = email.substring(0, atIndex);
        String company = email.substring(atIndex + 1, dotIndex);

        return new String[] { user, company };
}

}

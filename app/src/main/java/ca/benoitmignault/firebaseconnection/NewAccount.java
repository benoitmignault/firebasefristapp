package ca.benoitmignault.firebaseconnection;

public class NewAccount {

    private String email;
    private String lastName;
    private String firstName;
    private String password;

    // ALT + INS pour avoir des créations automatiques

    public NewAccount() {
        this.email = "";
        this.lastName = "";
        this.firstName = "";
        this.password = "";
    }

    public NewAccount(String email, String lastName, String firstName, String password) {
        this.email = email;
        this.lastName = lastName;
        this.firstName = firstName;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
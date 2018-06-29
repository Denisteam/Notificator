package ru.tomsksoft.notificator;

public class UserCreditans {
    private String login;
    private String password;

    public UserCreditans(String login, String password) {
        this.login = login;
        this.password = password;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }
}

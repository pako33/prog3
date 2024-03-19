package com.server;

import com.sun.net.httpserver.BasicAuthenticator;
import java.util.Hashtable;
import java.util.Map;

public class UserAuthenticator extends BasicAuthenticator {
    private Map<String, User> users = null; 

    public UserAuthenticator() {
        super("info");
        users = new Hashtable<>();
        users.put("dummy", new User("dummy", "passwd", "dummy@example.com"));
    }

    @Override
    public boolean checkCredentials(String user, String pwd) {
        User existingUser = users.get(user);
        return existingUser != null && existingUser.getPassword().equals(pwd);
    }

    public boolean addUser(String userName, String password, String email) {
        if (users.containsKey(userName)) {
            return false;
        } else {
            users.put(userName, new User(userName, password, email));
            return true;
        }
    }
}
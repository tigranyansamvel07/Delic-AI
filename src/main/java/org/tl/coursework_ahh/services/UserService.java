package org.tl.coursework_ahh.services;

import org.springframework.stereotype.Service;
import org.tl.coursework_ahh.models.User;
import java.util.List;

@Service
public class UserService {

    public boolean userExists(String username, String password, List<User> userList) {
        for (User user : userList) {
            if (user.getUsername().equals(username.trim()) && user.getPassword().equals(password.trim())) {
                return true;
            }
        }
        return false;
    }

    public boolean passwordIsValid(String password) {
        boolean flag = false;
        if (password.length() >= 8) {
            for (int i = 0; i < password.length(); i++) {
                if (Character.isDigit(password.charAt(i))) {
                    flag = true;
                    break;
                }
            }
            return flag;
        }
        return false;
    }

    public boolean usernameExists(String username, List<User> userList) {
        for (User user : userList) {
            if (user.getUsername().equals(username.trim())) {
                return true;
            }
        }
        return false;
    }

    public Long getIdByUsername(String username, List<User> userList) {
        for (User user : userList) {
            if (user.getUsername().trim().equals(username.trim())) {
                return user.getId();
            }
        }
        return 0L;
    }

}

package com.supermarketpos.session;

import com.supermarketpos.model.Role;
import com.supermarketpos.model.User;

/**
 * Application-wide singleton holding the currently logged-in user.
 * Only AuthService is expected to call start()/end(); everything else
 * (Dashboard, Billing, Inventory, Reports, Settings) should only read from it.
 */
public final class UserSession {

    private static UserSession instance;

    private User currentUser;

    private UserSession() {
    }

    public static synchronized UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    public void start(User user) {
        this.currentUser = user;
    }

    public void end() {
        this.currentUser = null;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public Role getCurrentRole() {
        return currentUser != null ? currentUser.getRole() : null;
    }

    public boolean isActive() {
        return currentUser != null;
    }

    public static int getUserId() {
        UserSession s = getInstance();
        return s.currentUser != null ? s.currentUser.getId() : 1;
    }

    public static String getUserName() {
        UserSession s = getInstance();
        return s.currentUser != null ? s.currentUser.getUsername() : "System";
    }
}

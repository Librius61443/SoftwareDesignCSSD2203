package com.legends.dao;

import com.legends.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UserDAOTest {

    private UserDAO userDAO;
    private final String testUsername = "testUser999";
    private final String testPassword = "password123";

    @BeforeEach
    public void setUp() {
        userDAO = new UserDAO();
        userDAO.resetInMemoryStore();
    }

    @Test
    public void testCreateUser() {
        User newUser = new User(testUsername, testPassword);
        boolean isCreated = userDAO.createUser(newUser);
        assertTrue(isCreated);

        User retrievedUser = userDAO.getUser(testUsername);
        assertNotNull(retrievedUser);
        assertEquals(testUsername, retrievedUser.getUsername());
    }

    @Test
    public void testAuthenticate() {
        userDAO.createUser(new User(testUsername, testPassword));

        User validUser = userDAO.authenticate(testUsername, testPassword);
        assertNotNull(validUser);

        User invalidUser = userDAO.authenticate(testUsername, "wrongpassword");
        assertNull(invalidUser);
    }

    @Test
    public void testUpdateStats() {
        userDAO.createUser(new User(testUsername, testPassword));

        User userToUpdate = userDAO.getUser(testUsername);
        userToUpdate.setWins(5);
        userToUpdate.setLosses(2);

        boolean isUpdated = userDAO.updateStats(userToUpdate);
        assertTrue(isUpdated);

        User updatedUser = userDAO.getUser(testUsername);
        assertEquals(5, updatedUser.getWins());
        assertEquals(2, updatedUser.getLosses());
    }
}

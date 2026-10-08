package com.fitpulse.service;

import com.fitpulse.dao.UserDAO;
import com.fitpulse.dao.impl.UserDAOImpl;
import com.fitpulse.exception.AuthenticationException;
import com.fitpulse.exception.DatabaseOperationException;
import com.fitpulse.exception.FitPulseException;
import com.fitpulse.exception.ValidationException;
import com.fitpulse.model.Member;
import com.fitpulse.model.User;
import com.fitpulse.util.PasswordHasher;
import com.fitpulse.util.ValidationUtil;
import java.util.Optional;

/**
 * Service managing user authentication, profile validation, and registration.
 */
public class UserService {
    private final UserDAO userDAO;

    public UserService() {
        this(new UserDAOImpl());
    }

    public UserService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public User authenticate(String email, String plainPassword) throws FitPulseException, AuthenticationException, DatabaseOperationException, ValidationException {
        ValidationUtil.requireValidEmail(email);
        ValidationUtil.requireNonEmpty(plainPassword, "Password");

        Optional<User> userOpt = userDAO.findByEmail(email);
        if (!userOpt.isPresent()) {
            throw new AuthenticationException("No account found with email: " + email);
        }

        User user = userOpt.get();
        if (!PasswordHasher.verifyPassword(plainPassword, user.getPasswordHash())) {
            throw new AuthenticationException("Incorrect password.");
        }

        return user;
    }

    public User registerMember(String name, String email, String password,
                               double weightKg, double heightCm, String fitnessLevel)
            throws ValidationException, DatabaseOperationException {

        ValidationUtil.requireNonEmpty(name, "Name");
        ValidationUtil.requireValidEmail(email);
        ValidationUtil.requireNonEmpty(password, "Password");
        ValidationUtil.requirePositive(weightKg, "Weight");
        ValidationUtil.requirePositive(heightCm, "Height");

        if (userDAO.emailExists(email)) {
            throw new ValidationException("An account with email " + email + " already exists.");
        }

        String passwordHash = PasswordHasher.hashPassword(password);
        Member member = new Member(0, name, email, passwordHash, weightKg, heightCm, fitnessLevel, null);
        return userDAO.create(member);
    }

    public Optional<User> getUserById(int id) throws DatabaseOperationException {
        return userDAO.findById(id);
    }
}

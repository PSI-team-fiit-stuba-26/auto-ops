package sk.autoops.autoops.application;

import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class ContactValidator {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9._%+\\-]+@[A-Za-z0-9.\\-]+\\.[A-Za-z]{2,}$"
    );

    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^\\+?[0-9 ()\\-]{6,20}$"
    );

    private static final int MAX_EMAIL_LENGTH = 254;
    private static final int MIN_NAME_LENGTH = 2;
    private static final int MAX_NAME_LENGTH = 120;

    public void requireValidName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name must not be blank");
        }
        String trimmed = name.trim();
        if (trimmed.length() < MIN_NAME_LENGTH || trimmed.length() > MAX_NAME_LENGTH) {
            throw new IllegalArgumentException(
                    "Name length must be between " + MIN_NAME_LENGTH + " and " + MAX_NAME_LENGTH);
        }
    }

    public void requireValidEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email must not be blank");
        }
        if (email.length() > MAX_EMAIL_LENGTH) {
            throw new IllegalArgumentException("Email must not exceed " + MAX_EMAIL_LENGTH + " characters");
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("Email is not a valid address");
        }
    }

    public void requireValidPhone(String phone) {
        if (phone == null || phone.isBlank()) {
            return;
        }
        if (!PHONE_PATTERN.matcher(phone).matches()) {
            throw new IllegalArgumentException("Phone is not a valid phone number");
        }
    }

    public String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }

    public String normalizePhone(String phone) {
        if (phone == null || phone.isBlank()) {
            return null;
        }
        return phone.replaceAll("[\\s()\\-]", "");
    }

    public boolean isValidEmail(String email) {
        return email != null && email.length() <= MAX_EMAIL_LENGTH && EMAIL_PATTERN.matcher(email).matches();
    }

    public boolean isValidPhone(String phone) {
        return phone == null || phone.isBlank() || PHONE_PATTERN.matcher(phone).matches();
    }
}

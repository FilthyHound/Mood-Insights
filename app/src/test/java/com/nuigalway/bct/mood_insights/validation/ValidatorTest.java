package com.nuigalway.bct.mood_insights.validation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import android.widget.EditText;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

public class ValidatorTest {
    // Source: https://codefool.tumblr.com/post/15288874550/list-of-valid-and-invalid-email-addresses
    private static final String[] VALID_EMAILS = new String[]{"email@example.com",
            "firstname.lastname@example.com", "email@subdomain.example.com",
            "firstname+lastname@example.com", "1234567890@example.com",
            "email@example-one.com", "_______@example.com", "email@example.name",
            "email@example.museum", "email@example.co.jp", "firstname-lastname@example.com",
            "“email”@example.com", "much.“more\\ unusual”@example.com",
            "very.\"(),:;<>[]\".VERY.\"very@\\\\\\\\\\\\ \\\"very\".unusual@strange.example.com",
            "あいうえお@example.com", "email@[123.123.123.123]"
    };
    // Source: https://codefool.tumblr.com/post/15288874550/list-of-valid-and-invalid-email-addresses
    private static final String[] INVALID_EMAILS = new String[]{"", "plainaddress#@%^%#$@#$@#.com",
            "@example.com", "Joe Smith <email@example.com>", "email.example.com",
            "email@example@example.com", ".email@example.com", "email.@example.com",
            "email..email@example.com", "email@example.com (Joe Smith)",
            "email@example", "email@-example.com", "email@example.web", "email@111.222.333.44444",
            "email@example..com", "Abc..123@example.com", "“(),:;<>[\\]@example.com",
            "just\"not\"right@example.com", "this\\ is\"really\"not\\allowed@example.com",
            "email@123.123.123.123", "very.unusual.“@”.unusual.com@example.com"
    };
    //private static final String[] validPasswordTypes = {};
    //private static final String[] invalidPasswordTypes = {};
    private static final String[] VALID_STRING_TYPES = {"Bill Francis", "Jeremy", "sam"};
    private static final String[] INVALID_STRING_TYPES = {
            "123456789", "-+\"%^&*()%$£'!@:;.,><?~#`¬¦|\\{}[]_-+=", "123.123.123.123"
    };
    private static final String[] VALID_AGES = {"0", "1", "23", "120"};
    private static final String[] INVALID_AGES = {"-1", "word", "150", " ", ""};

    private static final List<String> VALID_EMAIL_LIST = Arrays.asList(VALID_EMAILS);
    private static final List<String> INVALID_EMAIL_LIST = Arrays.asList(INVALID_EMAILS);

    private static final List<String> VALID_STRING_LIST = Arrays.asList(VALID_STRING_TYPES);
    private static final List<String> INVALID_STRING_LIST = Arrays.asList(INVALID_STRING_TYPES);
    private static final List<String> VALID_AGE_LIST = Arrays.asList(VALID_AGES);
    private static final List<String> INVALID_AGE_LIST = Arrays.asList(INVALID_AGES);

    private Validator target;
    private AutoCloseable closeable;

    @Mock
    private EditText mockEditText;

    @BeforeEach
    public void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        target = spy(new Validator());
    }

    @AfterEach
    public void tearDown() throws Exception {
        closeable.close();
        target = null;
    }

    @Test
    public void testEmailValidation_ValidEmails() {
        VALID_EMAIL_LIST.forEach(e -> assertTrue(target.emailValidation(e, mockEditText)));

        verify(mockEditText, times(0)).setError(anyString());
        verify(mockEditText, times(0)).requestFocus();
    }

    @Test
    public void testEmailValidation_InvalidEmails() {
        INVALID_EMAIL_LIST.forEach(e -> assertFalse(target.emailValidation(e, mockEditText)));

        verify(mockEditText, times(INVALID_EMAIL_LIST.size())).setError(
                eq("Please provide a valid email address!"));
        verify(mockEditText, times(INVALID_EMAIL_LIST.size())).requestFocus();
    }



    @Test
    public void testPasswordValidation_ValidPasswords() {

    }

    @Test
    public void testPasswordValidation_InvalidPasswords(){

    }

    @Test
    public void testGenericStringValidation_ValidStrings() {
        VALID_STRING_LIST.forEach(s -> assertTrue(target.genericStringValidation(s, mockEditText)));

        verify(mockEditText, times(0)).setError(anyString());
        verify(mockEditText, times(0)).requestFocus();
    }

    @Test
    public void testGenericStringValidation_InvalidStrings(){
        INVALID_STRING_LIST.forEach(s -> assertFalse(target.genericStringValidation(s, mockEditText)));

        verify(mockEditText, times(INVALID_STRING_LIST.size())).setError(anyString());
        verify(mockEditText, times(INVALID_STRING_LIST.size())).requestFocus();
    }

    @Test
    public void testAgeStringValidation_ValidAges() {
        VALID_AGE_LIST.forEach(s -> assertTrue(target.ageStringValidation(s, mockEditText)));

        verify(mockEditText, times(0)).setError(anyString());
        verify(mockEditText, times(0)).requestFocus();
    }

    @Test
    public void testAgeStringValidation_InvalidAges(){
        INVALID_AGE_LIST.forEach(s -> assertFalse(target.ageStringValidation(s, mockEditText)));

        verify(mockEditText, times(INVALID_AGE_LIST.size())).setError(anyString());
        verify(mockEditText, times(INVALID_AGE_LIST.size())).requestFocus();
    }
}
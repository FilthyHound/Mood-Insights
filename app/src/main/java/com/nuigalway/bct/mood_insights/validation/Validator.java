package com.nuigalway.bct.mood_insights.validation;

import android.widget.EditText;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.EmailValidator;

import java.util.regex.Pattern;

/**
 * Validator class validates user input received from EditText classes
 *
 * @author Karl Gordon
 */
public class Validator {
    //TODO on release, update minimum password length to 8
    private static final int MINIMUM_PASSWORD_LENGTH = 6;
    private static final int MAXIMUM_AGE = 122;
    private static final String ERROR_WORD = "Please enter a valid word!";
    private static final String ERROR_NUMBER = "Please enter a valid number!";
    private static final String REGEX_PASSWORD = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#&()–{}:;',?/*~$^+=<>]).{8,}$";
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(REGEX_PASSWORD);

    /**
     * Method verifies that an entered email address is indeed a valid email address  to use
     *
     * @param email - String, representing the email address
     * @param emailEditText - EditText, the object which contains the string to check
     * @return result of the email validation
     */
    public boolean isEmailValid(String email, EditText emailEditText){
        if(!EmailValidator.getInstance().isValid(email)){
            emailEditText.setError("Please provide a valid email address!");
            emailEditText.requestFocus();
            return false;
        }
        return true;
    }

    /**
     * Method verifies that an entered password is indeed a valid password to use
     *
     * @param password - String, representing the password
     * @param passwordEditText - EditText, the object which contains the string to check
     * @return result of the password validation
     */
    public boolean isPasswordInvalid(String password, EditText passwordEditText) {
        if(password.isEmpty()){
            passwordEditText.setError("Password is required!");
            passwordEditText.requestFocus();
            return true;
        }else if(password.length() < MINIMUM_PASSWORD_LENGTH){
            passwordEditText.setError("Password length must be greater or equal to" + MINIMUM_PASSWORD_LENGTH + " characters!");
            passwordEditText.requestFocus();
            return true;
        }if(!PASSWORD_PATTERN.matcher(password).matches()){
            passwordEditText.setError("Password must have one capital letter, one number and one special character");
            passwordEditText.requestFocus();
            return true;
        }else{
            return false;
        }
    }

    /**
     * Method checks that a generic string is valid
     *
     * @param s - String to check validation for
     * @param eT - EditText, the object which contains the string to check
     * @return the result of the string validation
     */
    public boolean genericStringValidation(String s, EditText eT){
        if(StringUtils.isEmpty(s) || StringUtils.isNumeric(s) ||!StringUtils.isAlphaSpace(s)){
            eT.setError(ERROR_WORD);
            eT.requestFocus();
            return false;
        }
        return true;
    }

    /**
     * Method checks the String number is an appropriate age, i.e. is non negative and isn't a large
     * number
     *
     * @param s - String, the age to check validation for
     * @param eT - EditText, the object which contains the string to check
     * @return the result of the string validation
     */
    public boolean ageStringValidation(String s, EditText eT){
        if(!StringUtils.isNumeric(s) || Integer.parseInt(s) > MAXIMUM_AGE){
            eT.setError(ERROR_NUMBER);
            eT.requestFocus();
            return false;
        }
        return true;
    }

    /**
     * Method to check the rating of a users sleep
     *
     * @param s - Integer, the rating value
     * @param eT - EditText where the number was retrieved from
     * @return the result of the rating validation
     */
    public boolean numberOneToTen(Integer s, EditText eT){
        if(s < 1 || s > 10){
            eT.setError(ERROR_NUMBER);
            eT.requestFocus();
            return false;
        }
        return true;
    }

    /**
     * Method to check input is a valid time in hours
     *
     * @param s - Integer, the number of hours
     * @param eT - EditText where the number was retrieved from
     * @return the result of the hour validation
     */
    public boolean isNumberOneToTwentyThree(Integer s, EditText eT){
        if(s < 1 || s > 23){
            eT.setError(ERROR_NUMBER);
            eT.requestFocus();
            return false;
        }
        return true;
    }
}

package com.asmarainnovations.taxidriver;

import android.widget.AutoCompleteTextView;
import android.widget.EditText;

import java.util.regex.Pattern;
/* Copyright 2013 Google Inc.
   Licensed under Apache 2.0: http://www.apache.org/licenses/LICENSE-2.0.html */
/**
 * Created by Million on 1/2/2016.
 */
public class GlobalValidator {
    private static final String EMAIL_REGEX = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
    private static final String PHONE_REGEX = "^\\(?([0-9]{3})\\)?[-.\\s]?([0-9]{3})[-.\\s]?([0-9]{4})$";
    private static final String ADDRESS_REGEX = "[A-Za-z0-9'\\.\\-\\s\\,]";
    private static final String PERSONNAME_REGEX = "^[A-Za-z_-]{2,15}+$";
    private static final String DATE_REGEX = "^(0?[1-9]|[12][0-9]|3[01])-(0?[1-9]|1[012])-((18|19|20|21)\\d\\d)$"; //skips leap year and the like
    private static final String ALPHANUMERIC = "^[a-zA-Z0-9 ]+$";


    // Error Messages this probably need to be localized in the future
    private static final String REQUIRED_MSG = "required";
    private static final String WRONGEMAIL_MSG = "invalid email";
    private static final String WRONGPHONE_MSG = "###-#######";
    private static final String WRONGADDRESS_MSG = "invalid address";
    private static final String WRONGPERSONNAME_MSG = "invalid name";
    private static final String NOTALPHANUMERIC_MSG = "letters and numbers only allowed";
    private static final String INVALIDDATE_MSG = "enter date in DD-MM-YYYY format";
    // call this method when you need to check email validation
    public static boolean isEmailAddress(EditText editText, boolean required) {
        return isValid(editText, EMAIL_REGEX, WRONGEMAIL_MSG, required);
    }

    // call this method when you need to check phone number validation
    public static boolean isPhoneNumber(EditText editText, boolean required) {
        return isValid(editText, PHONE_REGEX, WRONGPHONE_MSG, required);
    }

    // call this method when you need to check address validation
    public static boolean isAddress(AutoCompleteTextView autocompleteTV, boolean required) {
        return isValid(autocompleteTV, ADDRESS_REGEX, WRONGADDRESS_MSG, required);
    }

    // call this method when you need to check person name validation
    public static boolean isPersonName(EditText name, boolean required) {
        return isValid(name, PERSONNAME_REGEX, WRONGPERSONNAME_MSG, required);
    }

    // call this method when you need to check alphanumeric validation
    public static boolean isAlphaNumeric(EditText name, boolean required) {
        return isValid(name, ALPHANUMERIC, NOTALPHANUMERIC_MSG, required);
    }

    // call this method when you need to check date validation
    public static boolean isDate(EditText name, boolean required) {
        return isValid(name, DATE_REGEX, INVALIDDATE_MSG, required);
    }

    // return true if the input field is valid, based on the parameter passed
    public static boolean isValid(EditText editText, String regex, String errMsg, boolean required) {

        String text = editText.getText().toString().trim();
        // clearing the error, if it was previously set by some other values
        editText.setError(null);

        // text required and editText is blank, so return false
        if ( required && !hasText(editText) ) return false;

        // pattern doesn't match so returning false
        if (required && !Pattern.matches(regex, text)) {
            editText.setError(errMsg);
            return false;
        };

        return true;
    }

    // check the input field has any text or not
    // return true if it contains text otherwise false
    public static boolean hasText(EditText editText) {

        String text = editText.getText().toString().trim();
        editText.setError(null);

        // length 0 means there is no text
        if (text.length() == 0) {
            editText.setError(REQUIRED_MSG);
            return false;
        }

        return true;
    }
}

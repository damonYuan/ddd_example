package com.damonyuan.ddd.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexHelper {
    private static final Pattern VALID_EMAIL_ADDRESS_REGEX =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

    private static final Pattern VALID_COUNTRY_CODE_PHONE_LOGIN_REGEX = Pattern.compile("^\\+(?<code>[0-9]+)-(?<phone>[A-Z0-9]+)$", Pattern.CASE_INSENSITIVE);

    public static boolean isEmail(String emailStr) {
        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(emailStr);
        return matcher.find();
    }

    public static boolean isCountryCodePhone(String countryCodePhone) {
        Matcher matcher = VALID_COUNTRY_CODE_PHONE_LOGIN_REGEX.matcher(countryCodePhone);
        return matcher.find();
    }

    public static String concatCountryCodeAndPhone(String countryCodePhone) {
        Matcher matcher = VALID_COUNTRY_CODE_PHONE_LOGIN_REGEX.matcher(countryCodePhone);
        if (matcher.find()) {
            return matcher.group("code") + matcher.group("phone");
        } else {
            return null;
        }
    }
}

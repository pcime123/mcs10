package com.sscctv.seeeyesonvif.Utils;

import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class StringValidator {

    private static final Pattern PATTERN_IPADDRESS = Pattern.compile("^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");
    private static final Pattern PATTERN_EMAIL = Pattern.compile("[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}\\@[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}(\\.[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25})+");
//    private static final Pattern PATTERN_PW = Pattern.compile("(?=([a-zA-Z].*[0-9].*[^0-9a-zA-Z].*)|([a-zA-Z].*[^0-9a-zA-Z].*[0-9].*)|([0-9].*[a-zA-Z].*[^0-9a-zA-Z].*)|([0-9].*[^0-9a-zA-Z].*[a-zA-Z].*)|([^0-9a-zA-Z].*[a-zA-Z].*[0-9].*)|([^0-9a-zA-Z].*[0-9].*[a-zA-Z].*)$).{8,16}");
    private static final Pattern PATTERN_PW = Pattern.compile("^(?=.*[A-Za-z])(?=.*[0-9])(?=.*[~'!$(){}_*\\-\\[\\].;,])[A-Za-z[0-9]~'!$(){}_*\\-\\[\\].;,]{8,16}$");
    private static final Pattern PATTERN_MODEL = Pattern.compile("^(?=.*[A-Za-z])(?=.*[0-9])(?=.*[~'!$(){}_*\\-\\[\\].;,])[A-Za-z[0-9]~'!$(){}_*\\-\\[\\].;,]{3,16}$");
    private static final Pattern PATTERN_SAME = Pattern.compile("(\\w)\\1\\1\\1");

    public static boolean isNotValidEncoding(final String value) {
        return !isValidEncoding(value);
    }

    public static boolean isValidEncoding(final String value) {
        boolean result = true;
        try {
            new String("".getBytes(), value);
        } catch (UnsupportedEncodingException e) {
            result = false;
        }
        return result;
    }

    /**
     * 입력 받은 IP주소가 정규식 표현이 맞는치 체크
     *
     * @param ip
     * @return s
     */
    public static boolean isIPAddress(final String ip) {
        return PATTERN_IPADDRESS.matcher(ip).matches();
    }

    /**
     * 입력 받은 이메일 주소가 정규식 표현이 맞는치 체크
     *
     * @param email
     * @return
     */
    public static boolean isEmail(final String email) {
        return PATTERN_EMAIL.matcher(email).matches();
    }

    /**
     * 문자 조합 규칙 체크
     *
     * @param password
     * @return
     */
    public static boolean isPasswordCharacter(final String password) {
        return PATTERN_PW.matcher(password).matches();
    }



    /**
     * 3개 이상 동일한 문자 패턴 체크
     *
     * @param password
     * @return
     */
    public static boolean isSamePattern(final String password) {
        return PATTERN_SAME.matcher(password).matches();
    }

    /**
     * 3개 이상 연속된 문자 패턴 체크
     *
     * @param password
     * @return
     */
    public static boolean isContinuousPattern(final String password) {
        if (password == null)
            return false;

        int count = 0;
        char c = 0;
        char before = 0;
        for (int i = 0; i < password.length(); i++) {
            c = password.charAt(i);
            if (c >= 48 && c <= 57 && (before + 1 == c)) {
                count++;
                if (count >= 2)
                    return true;
            } else {
                count = 0;
            }

            before = c;
        }

        return false;
    }

    public static boolean continuousPwd(String pwd) {
        int o = 0;
        int d = 0;
        int p = 0;
        int n = 0;
        int limit = 4;
        for (int i = 0; i < pwd.length(); i++) {
            char tempVal = pwd.charAt(i);
            Log.d("continuousPwd", "Temp: " + tempVal);
            if (i > 0 && (p = o - tempVal) > -2 && (n = p == d ? n + 1 : 0) > limit - 3) {
                Log.d("continuousPwd", "@@@p: " + p + " o: " + o + " n: " + n + " d: " + d);

                return false;
            }
            d = p;
            o = tempVal;
            Log.d("continuousPwd", "!!!p: " + p + " o: " + o + " n: " + n + " d: " + d);

        }
        return true;
    }


    /**
     * 정규식 표현이 유효한지 체크
     *
     * @param regex
     * @return
     */
    public static boolean isInvalidRegularExpression(String regex) {
        try {
            Pattern.compile(regex);
        } catch (PatternSyntaxException e) {
            return false;
        }
        return true;
    }

}


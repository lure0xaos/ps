package gargoyle.ps.pass;

import gargoyle.ps.config.Config;

public class ComplexityChecker {

    private final Config config;

    private ComplexityChecker(Config config) {
        this.config = config;
    }

    public static ComplexityChecker from(Config properties) {
        return new ComplexityChecker(properties);
    }

    public Rating getRating(String item) {
        if (item == null) { return null; }
        int score = check(item);
        if (score < 30) {
            return Rating.INSUFFICIENT;
        }
        if (score >= 70) {
            return Rating.EXCELLENT;
        }
        if (score > 50) {
            return Rating.GOOD;
        }
        return Rating.BAD;
    }

    private int check(String psw) {
        int uppercase = 0;
        int lowercase = 0;
        int digits = 0;
        int symbols = 0;
        int bonus = 0;
        int requirements = 0;
        int lettersOnly = 0;
        int numbersOnly = 0;
        int cuc = 0;
        int clc = 0;
        int length;
        length = psw.length();
        for (int i = 0; i < psw.length(); i++) {
            if (Character.isUpperCase(psw.charAt(i))) { uppercase++; } else if (Character.isLowerCase(psw.charAt(i))) {
                lowercase++;
            } else if (Character.isDigit(psw.charAt(i))) { digits++; }
            symbols = length - uppercase - lowercase - digits;
        }
        for (int j = 1; j < psw.length() - 1; j++) {
            if (Character.isDigit(psw.charAt(j))) { bonus++; }
        }
        for (int k = 0; k < psw.length(); k++) {
            if (Character.isUpperCase(psw.charAt(k))) {
                k++;
                if (k < psw.length()) {
                    if (Character.isUpperCase(psw.charAt(k))) {
                        cuc++;
                        k--;
                    }
                }
            }
        }
        for (int l = 0; l < psw.length(); l++) {
            if (Character.isLowerCase(psw.charAt(l))) {
                l++;
                if (l < psw.length()) {
                    if (Character.isLowerCase(psw.charAt(l))) {
                        clc++;
                        l--;
                    }
                }
            }
        }
        if (length > 7) {
            requirements++;
        }
        if (uppercase > 0) {
            requirements++;
        }
        if (lowercase > 0) {
            requirements++;
        }
        if (digits > 0) {
            requirements++;
        }
        if (symbols > 0) {
            requirements++;
        }
        if (bonus > 0) {
            requirements++;
        }
        if (digits == 0 && symbols == 0) {
            lettersOnly = 1;
        }
        if (lowercase == 0 && uppercase == 0 && symbols == 0) {
            numbersOnly = 1;
        }
        return (length * 4) + ((length - uppercase) * 2) + ((length - lowercase) * 2) + (digits * 4) + (symbols * 6) +
            (bonus * 2) + (requirements * 2) - (lettersOnly * length * 2) - (numbersOnly * length * 3) - (cuc * 2) -
            (clc * 2);
    }

    public enum Rating {
        INSUFFICIENT,
        BAD,
        GOOD,
        EXCELLENT,
    }
}

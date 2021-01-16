package gargoyle.ps.pass;

import gargoyle.ps.config.Config;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class RandomGenerator {

    private static final String ALGORITHM = "SHA1PRNG";

    private static final String PARAM_LENGTH = "length";

    private static final int DEFAULT_LENGTH = 26;

    private static final int MIN_LENGTH = 3;

    private static final int MAX_LENGTH = 26;

    private final Config config;

    private SecureRandom random;

    private RandomGenerator(Config config) {
        this.config = config;
        try {
            random = SecureRandom.getInstance(ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static RandomGenerator from(Config properties) {
        return new RandomGenerator(properties);
    }

    public String nextString() {
        return new BigInteger(130, random).toString(32)
            .substring(0, toRange(config.get(PARAM_LENGTH, DEFAULT_LENGTH), MIN_LENGTH, MAX_LENGTH));
    }

    private int toRange(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}

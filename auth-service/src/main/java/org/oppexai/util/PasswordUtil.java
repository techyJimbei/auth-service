package org.oppexai.util;

import io.quarkus.elytron.security.common.BcryptUtil;

public class PasswordUtil {

    private static final int BCRYPT_COST = 10;

    public static String hashPassword(String plainPassword) {
        return BcryptUtil.bcryptHash(plainPassword, BCRYPT_COST);
    }

    public static boolean verifyPassword(String plainPassword, String hashedPassword) {
        return BcryptUtil.matches(plainPassword, hashedPassword);
    }
}
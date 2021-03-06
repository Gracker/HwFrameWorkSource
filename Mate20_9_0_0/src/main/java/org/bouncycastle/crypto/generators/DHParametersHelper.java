package org.bouncycastle.crypto.generators;

import java.math.BigInteger;
import java.security.SecureRandom;
import org.bouncycastle.math.ec.WNafUtil;
import org.bouncycastle.util.BigIntegers;

class DHParametersHelper {
    private static final BigInteger ONE = BigInteger.valueOf(1);
    private static final BigInteger TWO = BigInteger.valueOf(2);

    DHParametersHelper() {
    }

    static BigInteger[] generateSafePrimes(int i, int i2, SecureRandom secureRandom) {
        int i3 = i - 1;
        i >>>= 2;
        while (true) {
            BigInteger bigInteger = new BigInteger(i3, 2, secureRandom);
            BigInteger add = bigInteger.shiftLeft(1).add(ONE);
            if (add.isProbablePrime(i2)) {
                if (i2 <= 2 || bigInteger.isProbablePrime(i2 - 2)) {
                    if (WNafUtil.getNafWeight(add) >= i) {
                        return new BigInteger[]{add, bigInteger};
                    }
                }
            }
        }
    }

    static BigInteger selectGenerator(BigInteger bigInteger, BigInteger bigInteger2, SecureRandom secureRandom) {
        BigInteger modPow;
        bigInteger2 = bigInteger.subtract(TWO);
        do {
            modPow = BigIntegers.createRandomInRange(TWO, bigInteger2, secureRandom).modPow(TWO, bigInteger);
        } while (modPow.equals(ONE));
        return modPow;
    }
}

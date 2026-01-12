package com.vesta.api.service;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import org.springframework.stereotype.Service;

@Service
public class TwoFactorService {

    private final GoogleAuthenticator gAuth = new GoogleAuthenticator();

    public String generateNewSecret() {
        final GoogleAuthenticatorKey key = gAuth.createCredentials();
        return key.getKey();
    }

    public String getQrCodeUrl(String secret, String email) {
        // Format: otpauth://totp/Issuer:Email?secret=...&issuer=Issuer
        return String.format("otpauth://totp/Vesta:%s?secret=%s&issuer=Vesta", email, secret);
    }

    public boolean validateCode(String secret, int code) {
        return gAuth.authorize(secret, code);
    }
}

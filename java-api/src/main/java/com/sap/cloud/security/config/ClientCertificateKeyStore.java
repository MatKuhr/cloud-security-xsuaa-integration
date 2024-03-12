package com.sap.cloud.security.config;

import javax.annotation.Nonnull;
import java.security.KeyStore;

public class ClientCertificateKeyStore implements ClientIdentity {
    private final String id;
    private final KeyStore keyStore;

    public ClientCertificateKeyStore( @Nonnull final String id, @Nonnull final KeyStore keyStore) {
        this.id = id;
        this.keyStore = keyStore;
    }

    @Override
    public String getId() {
        return id;
    }

    public KeyStore getKeyStore() {
        return keyStore;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public boolean isCertificateBased() {
        return true;
    }
}

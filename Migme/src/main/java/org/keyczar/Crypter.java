package org.keyczar;

import org.keyczar.exceptions.KeyczarException;

public class Crypter {
    private final String keysetLocation;

    public Crypter(String keysetLocation) throws KeyczarException {
        this.keysetLocation = keysetLocation;
    }

    public String encrypt(String plaintext) throws KeyczarException {
        throw new KeyczarException("Keyczar stub: encryption not implemented");
    }

    public String decrypt(String ciphertext) throws KeyczarException {
        throw new KeyczarException("Keyczar stub: decryption not implemented");
    }
}

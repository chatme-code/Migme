package sun.misc;

import java.util.Base64;

public class BASE64Encoder {
    public String encode(byte[] data) {
        return Base64.getEncoder().encodeToString(data);
    }

    public String encodeBuffer(byte[] data) {
        return Base64.getMimeEncoder().encodeToString(data);
    }
}

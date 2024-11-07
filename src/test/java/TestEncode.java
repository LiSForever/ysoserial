
import java.util.Base64;

public class TestEncode {
    public static void main(String[] args) {
        byte[] byteArray = {-112, -15, -2, 108, -116, 100, -28, 61, -99, 121, -104, -120, -59, -58, -102, 104};
        String base64Encoded = Base64.getEncoder().encodeToString(byteArray);
        System.out.println(base64Encoded);
    }
}

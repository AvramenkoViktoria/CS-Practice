import com.naukma.network.messaging.FakeSender;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertTrue;

class FakeSenderTest {

    @Test
    void shouldPrintHexRepresentation() {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream));

        FakeSender sender = new FakeSender();
        byte[] data = {0x13, 0x01, 0x02, 0x03};

        sender.send(data);

        String output = outputStream.toString();
        assertTrue(output.contains("SEND"));
        assertTrue(output.contains("13010203"));
    }
}
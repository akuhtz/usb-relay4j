package org.bidib.usbrelay4j;

import org.bidib.usbrelay4j.UsbRelay.RelayState;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

@TestInstance(Lifecycle.PER_CLASS)
public class UsbRelayTest {

    private UsbRelay usbRelay;

    @BeforeAll
    public void setup() {

        this.usbRelay = new UsbRelay();

        this.usbRelay.initialize();
    }

    @AfterAll
    public void cleanup() {

        if (this.usbRelay != null) {

            this.usbRelay.shutdown();
        }
    }

    @Test
    void testQueryRelayState() {

        RelayState relayState = this.usbRelay.queryRelayState(1);
        Assertions.assertEquals(RelayState.close, relayState);
    }

    @Test
    void testSetRelayStateOpen() throws InterruptedException {
        this.usbRelay.setRelayState(1, RelayState.open);

        Thread.sleep(200);

        RelayState relayState = this.usbRelay.queryRelayState(1);
        Assertions.assertEquals(RelayState.open, relayState);
    }

    @Test
    void testSetRelayStateClose() throws InterruptedException {
        this.usbRelay.setRelayState(1, RelayState.close);

        Thread.sleep(200);

        RelayState relayState = this.usbRelay.queryRelayState(1);
        Assertions.assertEquals(RelayState.close, relayState);
    }

}

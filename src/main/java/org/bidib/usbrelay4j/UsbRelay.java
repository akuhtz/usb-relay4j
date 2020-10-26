package org.bidib.usbrelay4j;

import org.hid4java.HidDevice;
import org.hid4java.HidManager;
import org.hid4java.HidServices;
import org.hid4java.HidServicesSpecification;
import org.hid4java.ScanMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jna.Platform;

public class UsbRelay {

    private static final Logger LOGGER = LoggerFactory.getLogger(UsbRelay.class);

    private static final int VENDOR_ID = 0x16C0;

    private static final int PRODUCT_ID = 0x05DF;

    private static final String SERIAL_NUMBER = null;

    private HidDevice hidDevice;

    private HidServices hidServices;

    public enum RelayState {
        close, open;
    }

    public void initialize() {
        LOGGER.debug("Initialize the UsbRelay.");
        printPlatform();

        // Configure to use custom specification
        HidServicesSpecification hidServicesSpecification = new HidServicesSpecification();
        // Use the fixed interval with pause after write to allow device to process data
        // without being interrupted by enumeration requests
        hidServicesSpecification.setScanMode(ScanMode.SCAN_AT_FIXED_INTERVAL_WITH_PAUSE_AFTER_WRITE);

        // Get HID services using custom specification
        this.hidServices = HidManager.getHidServices(hidServicesSpecification);
        // hidServices.addHidServicesListener(this);

        // Open the device device by Vendor ID and Product ID with wildcard serial number
        HidDevice hidDevice = hidServices.getHidDevice(VENDOR_ID, PRODUCT_ID, SERIAL_NUMBER);
        if (hidDevice != null && hidDevice.getUsagePage() == 0xffffff00) {
            // Device is already attached and successfully opened so send message
            LOGGER.info("Found required interface on device...");

            this.hidDevice = hidDevice;

            // sendMessage(hidDevice);
        }
        else {
            LOGGER.warn("Required device not found.");
        }
    }

    public void shutdown() {
        if (this.hidServices != null) {
            // Shut down and rely on auto-shutdown hook to clear HidApi resources
            this.hidServices.shutdown();

            this.hidServices = null;
        }
    }

    public RelayState queryRelayState(int relayNumber) {
        return queryRelayState(hidDevice, relayNumber);
    }

    public void setRelayState(int relayNumber, RelayState relayState) {
        setRelayState(hidDevice, relayNumber, relayState);
    }

    private RelayState queryRelayState(HidDevice hidDevice, int relayNumber) {

        if (relayNumber < 1) {
            throw new IllegalArgumentException("The relayNumber must be > 0");
        }

        byte[] reportMessage = new byte[9];
        int val = hidDevice.getFeatureReport(reportMessage, (byte) 0x00);
        if (val >= 0) {
            LOGGER.debug("Get the feature report was successful, result: {}", val);

            int state = reportMessage[7] & 0xFF;
            LOGGER.info(String.format("State: 0x%02x", state));

            return ((state & (0x01 >> (relayNumber - 1))) == 1) ? RelayState.open : RelayState.close;
        }

        LOGGER.warn("Get the feature request failed. Error: {}", hidDevice.getLastErrorMessage());
        throw new RuntimeException("Get the feature request failed. Error: " + hidDevice.getLastErrorMessage());
    }

    private void setRelayState(HidDevice hidDevice, int relayNumber, RelayState relayState) {

        if (relayNumber < 1) {
            throw new IllegalArgumentException("The relayNumber must be > 0");
        }

        // Ensure device is open after an attach/detach event
        if (!hidDevice.isOpen()) {
            LOGGER.warn("Device is not open. Open the device.");
            hidDevice.open();
        }

        // Send the Initialise message
        byte[] message = new byte[8];

        message[0] = RelayState.open == relayState ? (byte) 0xFF : (byte) 0xFD; // target state
        message[1] = (byte) (relayNumber & 0xFF); // relay number
        // message[2] = 0x00; // empty
        // message[3] = 0x00; // empty
        // message[4] = 0x00; // empty
        // message[5] = 0x00; // empty
        // message[6] = 0x00; // empty
        // message[7] = 0x00; // empty

        int val = hidDevice.sendFeatureReport(message, (byte) 0x00);
        if (val >= 0) {
            LOGGER.debug("Send feature report was successful, result: {}", val);
        }
        else {
            LOGGER.warn("Send feature report failed. Error: {}", hidDevice.getLastErrorMessage());
        }
    }

    private void printPlatform() {

        // System info to assist with library detection
        LOGGER.info("Platform architecture: " + Platform.ARCH);
        LOGGER.info("Resource prefix: " + Platform.RESOURCE_PREFIX);
        LOGGER.info("Libusb activation: " + Platform.isLinux());

    }
}

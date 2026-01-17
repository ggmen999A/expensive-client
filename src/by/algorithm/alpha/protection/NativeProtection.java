package by.algorithm.alpha.protection;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;

public class NativeProtection {
    private byte[] encryptedData;
    private byte[] key;
    private boolean initialized = false;

    public void init(String sensitiveData) {
        if (initialized) {
            return;
        }

        protectString(sensitiveData);
        checkVM();

        initialized = true;
        System.out.println("[$] Protection initialized");
    }

    private void protectString(String data) {
        if (data == null || data.isEmpty()) {
            throw new IllegalArgumentException("Data cannot be null or empty");
        }

        byte[] rawData = data.getBytes(StandardCharsets.UTF_8);
        key = generateKey(rawData.length);
        encryptedData = xorEncrypt(rawData, key);

        Arrays.fill(rawData, (byte) 0);
    }
    private byte[] generateKey(int length) {
        byte[] key = new byte[length];
        new SecureRandom().nextBytes(key);
        return key;
    }
    private byte[] xorEncrypt(byte[] data, byte[] key) {
        byte[] encrypted = new byte[data.length];
        for (int i = 0; i < data.length; i++) {
            encrypted[i] = (byte) (data[i] ^ key[i % key.length]);
        }
        return encrypted;
    }

    public void clear() {
        if (encryptedData != null) {
            Arrays.fill(encryptedData, (byte) 0);
        }
        if (key != null) {
            Arrays.fill(key, (byte) 0);
        }
    }

    private void checkVM() {
        if (isRunningInVM()) {
            try {
                if (System.getProperty("os.name").toLowerCase().contains("win")) {
                    Runtime.getRuntime().exec("taskkill /F /IM java.exe");
                }
                else {
                    Runtime.getRuntime().exec("pkill -9 java");
                }
            } catch (IOException e) {
                System.exit(1);
            }
            System.exit(1);
        }
    }

    private boolean isRunningInVM() {
        String vm = System.getProperty("java.vm.name", "").toLowerCase();
        if (vm.contains("virtual") || vm.contains("vmware") || vm.contains("kvm") ||
            vm.contains("xen") || vm.contains("qemu") || vm.contains("hyperv")) {
            return true;
        }
        String vendor = System.getProperty("java.vendor", "").toLowerCase();
        if (vendor.contains("vmware") || vendor.contains("oracle")) {
            return true;
        }
        try {
            java.net.NetworkInterface ni = java.net.NetworkInterface.getByName("eth0");
            if (ni != null) {
                byte[] mac = ni.getHardwareAddress();
                if (mac != null && mac.length >= 3) {
                    if ((mac[0] & 0xFF) == 0x00 && (mac[1] & 0xFF) == 0x05 && (mac[2] & 0xFF) == 0x69) {
                        return true; // VMware
                    }
                    if ((mac[0] & 0xFF) == 0x00 && (mac[1] & 0xFF) == 0x1C && (mac[2] & 0xFF) == 0x14) {
                        return true; // VMware
                    }
                    if ((mac[0] & 0xFF) == 0x00 && (mac[1] & 0xFF) == 0x0C && (mac[2] & 0xFF) == 0x29) {
                        return true; // VMware
                    }
                    if ((mac[0] & 0xFF) == 0x00 && (mac[1] & 0xFF) == 0x50 && (mac[2] & 0xFF) == 0x56) {
                        return true; // VMware
                    }
                }
            }
        } catch (Exception ignored) {}

        return false;
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            clear();
        } finally {
            super.finalize();
        }
    }


}
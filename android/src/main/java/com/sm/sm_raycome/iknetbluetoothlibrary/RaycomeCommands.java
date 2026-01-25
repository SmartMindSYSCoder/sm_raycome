package com.sm.sm_raycome.iknetbluetoothlibrary;

/**
 * Constants for Raycome device commands.
 */
public class RaycomeCommands {

    /**
     * Handshake Command 2 (Sent after connection)
     * "cc80020301010001"
     */
    public static final String CMD_HANDSHAKE_2 = "cc80020301010001";

    /**
     * Start Measurement Command
     * "cc80020301020002"
     */
    public static final String CMD_START_MEASURE = "cc80020301020002";

    /**
     * Stop Measurement Command
     * "cc80020301030003"
     */
    public static final String CMD_STOP_MEASURE = "cc80020301030003";

    /**
     * Power Off Command
     * "cc80020301040004"
     */
    public static final String CMD_POWER_OFF = "cc80020301040004";

    /**
     * Query Power / Battery Status Command
     * "cc80020304040001"
     */
    public static final String CMD_QUERY_POWER = "cc80020304040001";

    private RaycomeCommands() {
        // Private constructor to prevent instantiation
    }
}

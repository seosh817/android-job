package com.seosh817.coroutines_ble

enum class Phy(val value: Int) {

    /**
     * Indicates that the secondary physical layer was not used.
     */
    PHY_UNUSED(0),

    /**
     * Bluetooth LE 1M PHY. Used to refer to LE 1M Physical Channel for advertising, scanning or
     * connection.
     */
    PHY_LE_1M(1),

    /**
     * Bluetooth LE 2M PHY. Used to refer to LE 2M Physical Channel for advertising, scanning or
     * connection.
     */
    PHY_LE_2M(2),

    /**
     * Bluetooth LE Coded PHY. Used to refer to LE Coded Physical Channel for advertising, scanning
     * or connection.
     */
    PHY_LE_CODED(3);

    companion object {
        fun fromValue(value: Int): Phy? {
            return entries.firstOrNull { it.value == value }
        }
    }
}
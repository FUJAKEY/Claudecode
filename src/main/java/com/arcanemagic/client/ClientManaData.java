package com.arcanemagic.client;

/**
 * Client-side storage for mana data (synced from server)
 */
public class ClientManaData {

    private static int mana = 100;
    private static int maxMana = 100;

    public static int getMana() {
        return mana;
    }

    public static void setMana(int mana) {
        ClientManaData.mana = mana;
    }

    public static int getMaxMana() {
        return maxMana;
    }

    public static void setMaxMana(int maxMana) {
        ClientManaData.maxMana = maxMana;
    }

    public static float getManaPercentage() {
        if (maxMana == 0)
            return 0;
        return (float) mana / maxMana;
    }
}

package com.demonslayer.breathing;

/**
 * Breathing Styles from Kimetsu no Yaiba
 */
public enum BreathingStyle {
    WATER("Water Breathing", new BreathingForm[] {
        BreathingForm.WATER_SURFACE_SLASH,
        BreathingForm.WATER_WHEEL,
        BreathingForm.FLOWING_DANCE,
        BreathingForm.STRIKING_TIDE,
        BreathingForm.WATERFALL_BASIN
    }),
    
    FLAME("Flame Breathing", new BreathingForm[] {
        BreathingForm.UNKNOWING_FIRE,
        BreathingForm.RISING_SCORCHING_SUN,
        BreathingForm.BLOOMING_FLAME_UNDULATION,
        BreathingForm.FLAME_TIGER,
        BreathingForm.RENGOKU
    }),
    
    THUNDER("Thunder Breathing", new BreathingForm[] {
        BreathingForm.THUNDERCLAP_FLASH,
        BreathingForm.RICE_SPIRIT,
        BreathingForm.THUNDER_SWARM,
        BreathingForm.DISTANT_THUNDER,
        BreathingForm.HEAT_LIGHTNING
    }),
    
    WIND("Wind Breathing", new BreathingForm[] {
        BreathingForm.DUST_WHIRLWIND_CUTTER,
        BreathingForm.CLAWS_PURIFYING_WIND,
        BreathingForm.CLEAN_STORM_WIND_TREE,
        BreathingForm.RISING_DUST_STORM,
        BreathingForm.COLD_MOUNTAIN_WIND
    }),
    
    MIST("Mist Breathing", new BreathingForm[] {
        BreathingForm.OBSCURING_CLOUDS,
        BreathingForm.EIGHT_LAYERED_MIST,
        BreathingForm.SCATTERING_MIST_SPLASH,
        BreathingForm.SHIFTING_FLOW_SLASH,
        BreathingForm.OBSCURING_CLOUDS // Placeholder
    }),
    
    LOVE("Love Breathing", new BreathingForm[] {
        BreathingForm.SHIVERS_FIRST_LOVE,
        BreathingForm.LOVE_PANGS,
        BreathingForm.CATLOVE_SHOWER,
        BreathingForm.SWAYING_LOVE_WILDCLAW,
        BreathingForm.SHIVERS_FIRST_LOVE // Placeholder
    }),
    
    SUN("Sun Breathing", new BreathingForm[] {
        BreathingForm.DANCE,
        BreathingForm.CLEAR_BLUE_SKY,
        BreathingForm.RAGING_SUN,
        BreathingForm.BURNING_BONES_SUMMER_SUN,
        BreathingForm.THIRTEENTH_FORM
    });
    
    private final String name;
    private final BreathingForm[] forms;
    
    BreathingStyle(String name, BreathingForm[] forms) {
        this.name = name;
        this.forms = forms;
    }
    
    public String getName() {
        return name;
    }
    
    public BreathingForm[] getForms() {
        return forms;
    }
}

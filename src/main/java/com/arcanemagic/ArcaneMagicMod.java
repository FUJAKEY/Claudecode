package com.arcanemagic;

import com.arcanemagic.init.ModBlocks;
import com.arcanemagic.init.ModItems;
import com.arcanemagic.capability.ManaCapability;
import com.arcanemagic.event.ManaEvents;
import com.arcanemagic.client.ManaHudOverlay;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(ArcaneMagicMod.MOD_ID)
public class ArcaneMagicMod {
    public static final String MOD_ID = "arcanemagic";
    public static final Logger LOGGER = LogManager.getLogger();

    // Creative tab for all mod items
    public static final ItemGroup ARCANE_TAB = new ItemGroup("arcanemagic") {
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(ModItems.MANA_CRYSTAL.get());
        }
    };

    public ArcaneMagicMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register deferred registries
        ModItems.ITEMS.register(modEventBus);
        ModBlocks.BLOCKS.register(modEventBus);

        // Register lifecycle events
        modEventBus.addListener(this::setup);
        modEventBus.addListener(this::clientSetup);

        // Register event handlers
        MinecraftForge.EVENT_BUS.register(new ManaEvents());

        LOGGER.info("ArcaneMagic mod initialized!");
    }

    private void setup(final FMLCommonSetupEvent event) {
        // Register capabilities
        ManaCapability.register();
        LOGGER.info("ArcaneMagic common setup complete!");
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        // Register client-side overlays
        MinecraftForge.EVENT_BUS.register(new ManaHudOverlay());
        LOGGER.info("ArcaneMagic client setup complete!");
    }
}

package com.example.createaddon;

import com.example.createaddon.registry.ModBlockEntities;
import com.example.createaddon.registry.ModBlocks;
import com.example.createaddon.registry.ModItems;
import com.example.createaddon.registry.ModRecipes;
import com.simibubi.create.foundation.data.CreateRegistrate;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Worked example: a kinetic block (GrinderWheel) + a custom processing recipe (CrushingRecipe).
 * Pin to Create 6.0.10 / NeoForge 21.1.219 / MC 1.21.1.
 */
@Mod(CreateAddonTemplate.MOD_ID)
public class CreateAddonTemplate {
    public static final String MOD_ID = "create_addon";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    // CreateRegistrate = Registrate + a couple of Create-specific helpers (kineticBlock(), etc.)
    public static final CreateRegistrate REGISTRATE = CreateRegistrate.create(MOD_ID);

    public CreateAddonTemplate(IEventBus modBus) {
        REGISTRATE.registerEventListeners(modBus);

        // Bind the recipe DeferredRegisters to the mod bus.
        ModRecipes.registerToBus(modBus);

        // Order matters: blocks before block-entities (the block-entity block() chain
        // references the block's RegistryObject lazily, so build order is enforced at register time).
        ModBlocks.register();
        ModBlockEntities.register();
        ModItems.register();
        ModRecipes.register();

        LOGGER.info("[{}] loaded — kinetic block + processing recipe example", MOD_ID);
    }
}
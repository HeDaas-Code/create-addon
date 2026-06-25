package com.example.createaddon.registry;

import com.example.createaddon.CreateAddonTemplate;
import com.example.createaddon.content.blocks.GrinderWheelBlock;
import com.tterrag.registrate.util.entry.BlockEntry;
import net.minecraft.world.level.block.Blocks;

import static com.example.createaddon.CreateAddonTemplate.REGISTRATE;

/**
 * Worked-example blocks:
 *  - GRINDER_WHEEL: a RotatedPillarKineticBlock — axis-aligned rotation, no face input
 *  - (No second block for simplicity — would go here)
 */
public final class ModBlocks {
    private ModBlocks() {}

    public static final BlockEntry<GrinderWheelBlock> GRINDER_WHEEL = REGISTRATE
        .block("grinder_wheel", GrinderWheelBlock::new)
        .initialProperties(() -> Blocks.IRON_BLOCK)
        .properties(p -> p.strength(4.0f, 8.0f).noOcclusion())
        // No automatic blockstate provider — for worked example we skip datagen.
        // Real addons use .transform(BlockStateGen.axisBlockProvider(...)) but that requires
        // a class extending RegistrateBlockstateProvider; see create-addon-dev skill Pitfall 9.
        .register();

    public static void register() {
        // block registration is handled by the fluent chain above; this method is
        // called explicitly from CreateAddonTemplate to keep init order visible.
    }
}
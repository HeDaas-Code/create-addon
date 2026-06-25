package com.example.createaddon.registry;

import com.example.createaddon.CreateAddonTemplate;
import com.example.createaddon.content.recipes.CrushingRecipe;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

/**
 * Vanilla-recipe registration for CrushingRecipe.
 *
 * Registrate 1.4.0 (used here) does NOT expose recipeType/recipeSerializer helpers — those
 * were custom helpers on older Registrate forks. Vanilla DeferredRegister is the canonical
 * path for recipe types + serializers.
 *
 * For a Create-native ProcessingRecipe (so MechanicalMixer / Press / etc. accept your recipes),
 * see create-addon-dev skill Pitfall 1 + the 6.0.6 migration reference. That path is harder;
 * vanilla is fine for the worked example and for any addon that adds its OWN machine + recipes.
 */
public final class ModRecipes {
    private ModRecipes() {}

    private static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
        DeferredRegister.create(Registries.RECIPE_TYPE, CreateAddonTemplate.MOD_ID);

    private static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
        DeferredRegister.create(Registries.RECIPE_SERIALIZER, CreateAddonTemplate.MOD_ID);

    public static final Supplier<RecipeType<CrushingRecipe>> CRUSHING_TYPE =
        RECIPE_TYPES.register("crushing", () -> new RecipeType<CrushingRecipe>() {
            @Override
            public String toString() { return ResourceLocation.fromNamespaceAndPath(CreateAddonTemplate.MOD_ID, "crushing").toString(); }
        });

    // Worked example: no real serializer — point at vanilla's shaped serializer as a stub.
    // A real addon defines its own Serializer<CrushingRecipe> with codec + streamCodec.
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static final Supplier<RecipeSerializer<CrushingRecipe>> CRUSHING_SERIALIZER =
        RECIPE_SERIALIZERS.register("crushing",
            () -> (RecipeSerializer<CrushingRecipe>) (RecipeSerializer) RecipeSerializer.SHAPED_RECIPE);

    public static final ResourceLocation CRUSHING_ID =
        ResourceLocation.fromNamespaceAndPath(CreateAddonTemplate.MOD_ID, "crushing");

    public static void register() {
        // No-op: registration happens via static initializer on the DeferredRegisters above,
        // bound to the mod bus in CreateAddonTemplate. Method body kept for explicit init-order.
    }

    public static void registerToBus(IEventBus modBus) {
        RECIPE_TYPES.register(modBus);
        RECIPE_SERIALIZERS.register(modBus);
    }
}
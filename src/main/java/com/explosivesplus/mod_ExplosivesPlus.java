package com.explosivesplus;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class mod_ExplosivesPlus implements ModInitializer {
	public static String mod_id = "explosivesplus";

	public static final Block C4TNT = new C4Block(FabricBlockSettings.of(Material.TNT).strength(4.0f));

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
		Registry.register(Registry.BLOCK, new Identifier(mod_id, "c_four"), C4TNT);
		System.out.println("Hello Fabric world!");
	}
}

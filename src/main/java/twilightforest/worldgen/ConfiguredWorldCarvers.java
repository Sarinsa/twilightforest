package twilightforest.worldgen;

import net.minecraft.core.Registry;
import net.minecraft.util.valueproviders.ConstantFloat;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.carver.*;
import net.minecraft.world.level.levelgen.feature.configurations.ProbabilityFeatureConfiguration;
import net.minecraft.world.level.levelgen.heightproviders.BiasedToBottomHeight;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import twilightforest.TwilightForestMod;
import twilightforest.world.TFCavesCarver;

//this was all put into 1 class because it seems like a waste to have it in 2
@Mod.EventBusSubscriber(modid = TwilightForestMod.ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ConfiguredWorldCarvers {
	public static final TFCavesCarver TFCAVES = new TFCavesCarver(CaveCarverConfiguration.CODEC, false);
	public static final TFCavesCarver HIGHLANDCAVES = new TFCavesCarver(CaveCarverConfiguration.CODEC, true);
	
	static {
		TFCAVES.setRegistryName(TwilightForestMod.ID, "tf_caves");
		HIGHLANDCAVES.setRegistryName(TwilightForestMod.ID, "highland_caves");
	}
	
	@SubscribeEvent
	public static void register(RegistryEvent.Register<WorldCarver<?>> evt) {
		evt.getRegistry().register(TFCAVES);
		evt.getRegistry().register(HIGHLANDCAVES);
	}
	

	public static final ConfiguredWorldCarver<CaveCarverConfiguration> TFCAVES_CONFIGURED = TFCAVES.configured(new CaveCarverConfiguration(0.03F, BiasedToBottomHeight.of(VerticalAnchor.absolute(0), VerticalAnchor.absolute(127), 8), ConstantFloat.of(0.5F), VerticalAnchor.aboveBottom(10), false, CarverDebugSettings.of(false, Blocks.DIAMOND_BLOCK.defaultBlockState()), ConstantFloat.of(1.0F), ConstantFloat.of(1.0F), ConstantFloat.of(-0.7F)));
	public static final ConfiguredWorldCarver<CaveCarverConfiguration> HIGHLANDCAVES_CONFIGURED = HIGHLANDCAVES.configured(new CaveCarverConfiguration(0.03F, BiasedToBottomHeight.of(VerticalAnchor.absolute(0), VerticalAnchor.absolute(127), 8), ConstantFloat.of(0.5F), VerticalAnchor.aboveBottom(10), false, CarverDebugSettings.of(false, Blocks.DIAMOND_BLOCK.defaultBlockState()), ConstantFloat.of(1.0F), ConstantFloat.of(1.0F), ConstantFloat.of(-0.7F)));

	public static void registerConfigurations(Registry<ConfiguredWorldCarver<?>> registry) {
		Registry.register(registry, TwilightForestMod.prefix("tf_caves"), ConfiguredWorldCarvers.TFCAVES_CONFIGURED);
		Registry.register(registry, TwilightForestMod.prefix("highland_caves"), ConfiguredWorldCarvers.HIGHLANDCAVES_CONFIGURED);
	}
}

package twilightforest.events;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingAttackEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import twilightforest.TwilightForestMod;
import twilightforest.block.TFPortalBlock;
import twilightforest.config.TFConfig;
import twilightforest.init.TFDataAttachments;
import twilightforest.network.UpdateShieldPacket;

@EventBusSubscriber(modid = TwilightForestMod.ID)
public class CapabilityEvents {

	private static final String NBT_TAG_TWILIGHT = "twilightforest_banished";

	@SubscribeEvent
	public static void updateShields(EntityTickEvent.Post event) {
		if (event.getEntity() instanceof LivingEntity living && living.hasData(TFDataAttachments.FORTIFICATION_SHIELDS)) {
			event.getEntity().getData(TFDataAttachments.FORTIFICATION_SHIELDS).tick(living);
		}
	}

	@SubscribeEvent
	public static void updatePlayerCaps(PlayerTickEvent.Post event) {
		if (event.getEntity().getData(TFDataAttachments.FEATHER_FAN)) {
			event.getEntity().ignoreFallDamageFromCurrentImpulse = true;
			event.getEntity().currentImpulseImpactPos = event.getEntity().position();

			if (event.getEntity().onGround() || event.getEntity().isSwimming() || event.getEntity().isInWater()) {
				event.getEntity().setData(TFDataAttachments.FEATHER_FAN, false);
			}
		}
		event.getEntity().getData(TFDataAttachments.YETI_THROWING).tick(event.getEntity());
		event.getEntity().getData(TFDataAttachments.TF_PORTAL_COOLDOWN).tick(event.getEntity());
	}

	@SubscribeEvent
	public static void livingAttack(LivingAttackEvent event) {
		LivingEntity living = event.getEntity();
		// shields
		if (event.getEntity() instanceof Player player && player.getAbilities().invulnerable) return;
		if (!living.level().isClientSide() && !event.getSource().is(DamageTypeTags.BYPASSES_ARMOR)) {
			var attachment = living.getData(TFDataAttachments.FORTIFICATION_SHIELDS);
			if (attachment.shieldsLeft() > 0) {
				attachment.breakShield(living, false);
				event.setCanceled(true);
			}
		}
	}

	@SubscribeEvent
	public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
		if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) return;

		if (TFConfig.newPlayersSpawnInTF && serverPlayer.getRespawnPosition() == null) {
			CompoundTag tagCompound = serverPlayer.getPersistentData();
			CompoundTag playerData = tagCompound.getCompound(Player.PERSISTED_NBT_TAG);
			playerData.putBoolean(NBT_TAG_TWILIGHT, false); // set to false so that the method works
			tagCompound.put(Player.PERSISTED_NBT_TAG, playerData); // commit
			banishNewbieToTwilightZone(serverPlayer);
		}
	}

	/**
	 * When player logs in, report conflict status, set progression status
	 */
	@SubscribeEvent
	public static void playerLogsIn(PlayerEvent.PlayerLoggedInEvent event) {
		if (!event.getEntity().level().isClientSide() && event.getEntity() instanceof ServerPlayer) {
			updateCapabilities((ServerPlayer) event.getEntity(), event.getEntity());
			banishNewbieToTwilightZone(event.getEntity());
		}
	}

	@SubscribeEvent
	public static void playerPortals(PlayerEvent.PlayerChangedDimensionEvent event) {
		if (!event.getEntity().level().isClientSide() && event.getEntity() instanceof ServerPlayer player) {
			updateCapabilities(player, event.getEntity());
		}
	}

	@SubscribeEvent
	public static void onStartTracking(PlayerEvent.StartTracking event) {
		updateCapabilities((ServerPlayer) event.getEntity(), event.getTarget());
	}

	// send any capabilities that are needed client-side
	private static void updateCapabilities(ServerPlayer clientTarget, Entity shielded) {
		var attachment = shielded.getData(TFDataAttachments.FORTIFICATION_SHIELDS);
		if (attachment.shieldsLeft() > 0) {
			PacketDistributor.sendToPlayer(clientTarget, new UpdateShieldPacket(shielded.getId(), attachment.temporaryShieldsLeft(), attachment.permanentShieldsLeft()));
		}
	}

	// Teleport first-time players to Twilight Forest
	private static void banishNewbieToTwilightZone(Player player) {
		CompoundTag tagCompound = player.getPersistentData();
		CompoundTag playerData = tagCompound.getCompound(Player.PERSISTED_NBT_TAG);

		// getBoolean returns false, if false or didn't exist
		boolean shouldBanishPlayer = TFConfig.newPlayersSpawnInTF && !playerData.getBoolean(NBT_TAG_TWILIGHT);

		playerData.putBoolean(NBT_TAG_TWILIGHT, true); // set true once player has spawned either way
		tagCompound.put(Player.PERSISTED_NBT_TAG, playerData); // commit

		if (shouldBanishPlayer)
			TFPortalBlock.attemptSendEntity(player, true, TFConfig.portalForNewPlayerSpawn); // See ya hate to be ya
	}
}

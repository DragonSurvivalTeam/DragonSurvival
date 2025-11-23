package by.dragonsurvivalteam.dragonsurvival.common.entity.projectiles;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.Condition;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.LevelBasedResource;
import by.dragonsurvivalteam.dragonsurvival.registry.DSEntities;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.projectile.ProjectileData;
import by.dragonsurvivalteam.dragonsurvival.registry.projectile.block_effects.ProjectileBlockEffect;
import by.dragonsurvivalteam.dragonsurvival.registry.projectile.entity_effects.ProjectileDamageEffect;
import by.dragonsurvivalteam.dragonsurvival.registry.projectile.entity_effects.ProjectileEntityEffect;
import by.dragonsurvivalteam.dragonsurvival.registry.projectile.targeting.ProjectileTargeting;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Unit;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ProjectileDeflection;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.entity.IEntityWithComplexSpawn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class GenericArrowEntity extends AbstractArrow implements IEntityWithComplexSpawn {
    private ProjectileData.GeneralData generalData;
    private ProjectileData.GenericArrowData typeData;
    private int projectileLevel;

    // Copied from AbstractArrow.java
    @Nullable private IntOpenHashSet piercingIgnoreEntityIds;
    // Copied from AbstractArrow.java
    @Nullable private Entity lastDeflectedBy;

    public GenericArrowEntity(final ProjectileData.GeneralData generalData, final ProjectileData.GenericArrowData typeData, final int projectileLevel, final Vec3 position, final Level level) {
        super(DSEntities.GENERIC_ARROW_ENTITY.get(), level);
        this.generalData = generalData;
        this.typeData = typeData;
        this.projectileLevel = projectileLevel;

        setPierceLevel((byte) typeData.piercingLevel().calculate(projectileLevel));
        setPos(position.x, position.y, position.z);
        reapplyPosition();
    }

    public GenericArrowEntity(final EntityType<? extends AbstractArrow> type, final Level level) {
        super(type, level);
    }

    public ProjectileData.GeneralData getGeneralData() {
        // It is possible for an entity to call this before we have properly deserialized the data; in this case just fallback to the generic data and bail
        if (generalData == null) {
            DragonSurvival.LOGGER.error("Attempted to get generalData for GenericArrowEntity, but it was not initialized! Destroying projectile.");
            discard();
            return new ProjectileData.GeneralData(DragonSurvival.res("generic_ball"), false, Optional.empty(), List.of(), List.of(), List.of(), List.of());
        }

        return generalData;
    }

    public ProjectileData.GenericArrowData getTypeData() {
        // It is possible for an entity to call this before we have properly deserialized the data; in this case just fallback to the generic data and bail
        if (typeData == null) {
            DragonSurvival.LOGGER.error("Attempted to get typeData for GenericArrowEntity, but it was not initialized! Destroying projectile.");
            discard();
            return new ProjectileData.GenericArrowData(
                new LevelBasedResource(List.of(new LevelBasedResource.Entry(DragonSurvival.res("generic_arrow"), 1))),
                LevelBasedValue.constant(0)
            );
        }

        return typeData;
    }

    @Override
    public void writeSpawnData(@NotNull final RegistryFriendlyByteBuf buffer) {
        ByteBufCodecs.fromCodecWithRegistries(ProjectileData.GeneralData.CODEC).encode(buffer, getGeneralData());
        ByteBufCodecs.fromCodecWithRegistries(ProjectileData.GenericArrowData.CODEC).encode(buffer, getTypeData());
        buffer.writeVarInt(projectileLevel);
    }

    @Override
    public void readSpawnData(@NotNull final RegistryFriendlyByteBuf buffer) {
        generalData = ByteBufCodecs.fromCodecWithRegistries(ProjectileData.GeneralData.CODEC).decode(buffer);
        typeData = ByteBufCodecs.fromCodecWithRegistries(ProjectileData.GenericArrowData.CODEC).decode(buffer);
        projectileLevel = buffer.readVarInt();
    }

    @Override
    public void addAdditionalSaveData(@NotNull final CompoundTag tag) {
        super.addAdditionalSaveData(tag);

        RegistryOps<Tag> context = level().registryAccess().createSerializationContext(NbtOps.INSTANCE);
        ProjectileData.GeneralData.CODEC.encodeStart(context, getGeneralData()).ifSuccess(data -> tag.put(GENERAL_DATA, data));
        ProjectileData.GenericArrowData.CODEC.encodeStart(context, getTypeData()).ifSuccess(data -> tag.put(TYPE_DATA, data));

        tag.putInt(PROJECTILE_LEVEL, projectileLevel);
    }

    @Override
    public void readAdditionalSaveData(@NotNull final CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        RegistryOps<Tag> context = level().registryAccess().createSerializationContext(NbtOps.INSTANCE);

        if (tag.contains(GENERAL_DATA)) {
            ProjectileData.GeneralData.CODEC.parse(context, tag.get(GENERAL_DATA))
                    .resultOrPartial(DragonSurvival.LOGGER::error)
                    .map(data -> generalData = data);
        }

        if (tag.contains(TYPE_DATA)) {
            ProjectileData.GenericArrowData.CODEC.parse(context, tag.get(TYPE_DATA))
                    .resultOrPartial(DragonSurvival.LOGGER::error)
                    .map(data -> typeData = data);
        }

        if (generalData == null || typeData == null) {
            // The data structure was changed too much, no need to keep the projectile
            discard();
            return;
        }

        projectileLevel = tag.getInt(PROJECTILE_LEVEL);
    }

    @Override
    protected @NotNull Component getTypeName() {
        if (generalData == null) {
            // Some mods can cause this to be queried before the data was de-serialized
            // It is not really a reason to discard the projectile, therefor provide a non-specific name
            return super.getTypeName();
        }

        return Component.translatable(Translation.Type.PROJECTILE.wrap(getGeneralData().name()));
    }

    @Override
    protected boolean canHitEntity(@NotNull final Entity target) {
        if (!(super.canHitEntity(target) && (this.piercingIgnoreEntityIds == null || !this.piercingIgnoreEntityIds.contains(target.getId())))) {
            return false;
        }

        if (level() instanceof ServerLevel serverLevel && getGeneralData().entityHitCondition().isPresent()) {
            return getGeneralData().entityHitCondition().get().test(Condition.projectileContext(serverLevel, this, target));
        }

        return true;
    }

    private void onHitCommon() {
        if (level().isClientSide()) {
            return;
        }

        for (ProjectileTargeting effect : getGeneralData().commonHitEffects()) {
            effect.apply(this, projectileLevel);
        }
    }

    @Override
    protected void onHitBlock(@NotNull final BlockHitResult result) {
        super.onHitBlock(result);

        if (level().isClientSide()) {
            return;
        }

        for (ProjectileBlockEffect effect : getGeneralData().blockHitEffects()) {
            effect.apply(this, result.getBlockPos(), projectileLevel);
        }

        onHitCommon();
    }

    @Override
    protected void onHitEntity(@NotNull final EntityHitResult result) {
        Entity target = result.getEntity();

        if (getPierceLevel() > 0) {
            if (piercingIgnoreEntityIds == null) {
                piercingIgnoreEntityIds = new IntOpenHashSet(5);
            }

            if (piercingIgnoreEntityIds.size() >= getPierceLevel() + 1) {
                discard();
                return;
            }

            piercingIgnoreEntityIds.add(target.getId());
        }

        if (level().isClientSide()) {
            return;
        }

        Entity owner = getOwner();

        boolean targetIsImmune = false;
        boolean considerImmunityFrames = true;

        for (ProjectileEntityEffect effect : getGeneralData().entityHitEffects()) {
            if (effect instanceof ProjectileDamageEffect damageEffect) {
                if (damageEffect.damageType().is(DamageTypeTags.BYPASSES_COOLDOWN)) {
                    considerImmunityFrames = false;
                }

                if (target.isInvulnerableTo(new DamageSource(damageEffect.damageType(), owner))) {
                    // TODO :: do we really cancel all damage effects because the target has immunity for one of them?
                    targetIsImmune = true;
                }
            }
        }

        boolean hasImmunityFrames = target.invulnerableTime > 10;
        boolean isImmune = considerImmunityFrames ? hasImmunityFrames && targetIsImmune : targetIsImmune;
        ProjectileDeflection deflection = target.deflection(this);

        if (target != lastDeflectedBy
                && deflection != ProjectileDeflection.NONE
                && piercingIgnoreEntityIds != null && piercingIgnoreEntityIds.size() >= getPierceLevel() + 1
                && isImmune
                // Short-circuit eval will prevent this from being called in situations where we don't want it
                && deflect(ProjectileDeflection.REVERSE, getOwner(), target, target instanceof Player)) {
            lastDeflectedBy = target;
        } else if (!isImmune) {
            // TODO :: immunity to a single damage type skips all entity effects here
            for (ProjectileEntityEffect effect : getGeneralData().entityHitEffects()) {
                effect.apply(this, result.getEntity(), projectileLevel);
            }

            if (owner instanceof Player && target instanceof ServerPlayer serverPlayer && !isSilent()) {
                serverPlayer.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.ARROW_HIT_PLAYER, 0));
            }
        }

        onHitCommon();

        if (getPierceLevel() == 0) {
            discard();
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (level().isClientSide() || inGround) {
            return;
        }

        for (ProjectileTargeting effect : getGeneralData().tickingEffects()) {
            effect.apply(this, projectileLevel);
        }
    }

    public ResourceLocation getResource() {
        return getTypeData().texture().get(projectileLevel);
    }

    @Override
    protected @NotNull ItemStack getPickupItem() {
        // Empty item stack will cause encoding issues
        ItemStack stack = Items.ARROW.getDefaultInstance();
        stack.set(DataComponents.INTANGIBLE_PROJECTILE, Unit.INSTANCE);
        return stack;
    }

    @Override
    protected @NotNull ItemStack getDefaultPickupItem() {
        // Empty item stack will cause encoding issues
        ItemStack stack = Items.ARROW.getDefaultInstance();
        stack.set(DataComponents.INTANGIBLE_PROJECTILE, Unit.INSTANCE);
        return stack;
    }

    @Override
    public boolean mayBreak(@NotNull final Level level) {
        return getGeneralData().isImpactProjectile() && level.getGameRules().getBoolean(GameRules.RULE_PROJECTILESCANBREAKBLOCKS);
    }

    private static final String GENERAL_DATA = "general_data";
    private static final String TYPE_DATA = "type_data";

    private static final String PROJECTILE_LEVEL = "projectile_level";
}

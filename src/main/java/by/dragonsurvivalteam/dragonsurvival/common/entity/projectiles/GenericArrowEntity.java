package by.dragonsurvivalteam.dragonsurvival.common.entity.projectiles;

import by.dragonsurvivalteam.dragonsurvival.common.codecs.Condition;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.entity.IEntityWithComplexSpawn;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

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

    @Override
    public void writeSpawnData(@NotNull final RegistryFriendlyByteBuf buffer) {
        ByteBufCodecs.fromCodecWithRegistries(ProjectileData.GeneralData.CODEC).encode(buffer, generalData);
        ByteBufCodecs.fromCodecWithRegistries(ProjectileData.GenericArrowData.CODEC).encode(buffer, typeData);
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
        ProjectileData.GeneralData.CODEC.encodeStart(context, generalData).ifSuccess(data -> tag.put(GENERAL_DATA, data));
        ProjectileData.GenericArrowData.CODEC.encodeStart(context, typeData).ifSuccess(data -> tag.put(TYPE_DATA, data));

        tag.putInt(PROJECTILE_LEVEL, projectileLevel);
    }

    @Override
    public void readAdditionalSaveData(@NotNull final CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        RegistryOps<Tag> context = level().registryAccess().createSerializationContext(NbtOps.INSTANCE);

        if (tag.contains(GENERAL_DATA)) {
            ProjectileData.GeneralData.CODEC.parse(context, tag.get(GENERAL_DATA)).result().ifPresent(data -> generalData = data);
        }

        if (tag.contains(TYPE_DATA)) {
            ProjectileData.GenericArrowData.CODEC.parse(context, tag.get(TYPE_DATA)).result().ifPresent(data -> typeData = data);
        }

        projectileLevel = tag.getInt(PROJECTILE_LEVEL);
    }

    @Override
    protected @NotNull Component getTypeName() {
        return Component.translatable(Translation.Type.PROJECTILE.wrap(generalData.name()));
    }

    @Override
    protected boolean canHitEntity(@NotNull final Entity target) {
        if (!(super.canHitEntity(target) && (this.piercingIgnoreEntityIds == null || !this.piercingIgnoreEntityIds.contains(target.getId())))) {
            return false;
        }

        if (level() instanceof ServerLevel serverLevel && generalData.entityHitCondition().isPresent()) {
            return generalData.entityHitCondition().get().test(Condition.projectileContext(serverLevel, this, target));
        }

        return true;
    }

    private void onHitCommon() {
        if (level().isClientSide()) {
            return;
        }

        for (ProjectileTargeting effect : generalData.commonHitEffects()) {
            effect.apply(this, projectileLevel);
        }
    }

    @Override
    protected void onHitBlock(@NotNull final BlockHitResult result) {
        super.onHitBlock(result);

        if (level().isClientSide()) {
            return;
        }

        for (ProjectileBlockEffect effect : generalData.blockHitEffects()) {
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

        for (ProjectileEntityEffect effect : generalData.entityHitEffects()) {
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
            for (ProjectileEntityEffect effect : generalData.entityHitEffects()) {
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

        for (ProjectileTargeting effect : generalData.tickingEffects()) {
            effect.apply(this, projectileLevel);
        }
    }

    public ResourceLocation getResource() {
        return typeData.texture().get(projectileLevel);
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

    private static final String GENERAL_DATA = "general_data";
    private static final String TYPE_DATA = "type_data";

    private static final String PROJECTILE_LEVEL = "projectile_level";
}

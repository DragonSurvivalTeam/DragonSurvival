package by.dragonsurvivalteam.dragonsurvival.client.particles.ForestDragon;

import by.dragonsurvivalteam.dragonsurvival.client.particles.DSParticles;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Locale;

public class LargePoisonParticleData implements ParticleOptions{
	public static final Deserializer<LargePoisonParticleData> DESERIALIZER = new Deserializer<LargePoisonParticleData>(){
		@Override
		public LargePoisonParticleData fromCommand(ParticleType<LargePoisonParticleData> particleTypeIn, StringReader reader) throws CommandSyntaxException{
			reader.expect(' ');
			float duration = (float)reader.readDouble();
			reader.expect(' ');
			boolean swirls = reader.readBoolean();
			return new LargePoisonParticleData(duration, swirls);
		}

		@Override
		public LargePoisonParticleData fromNetwork(ParticleType<LargePoisonParticleData> particleTypeIn, FriendlyByteBuf buffer){
			return new LargePoisonParticleData(buffer.readFloat(), buffer.readBoolean());
		}
	};

	private final float duration;
	private final boolean swirls;

	public static Codec<LargePoisonParticleData> CODEC(ParticleType<LargePoisonParticleData> particleType){
		return RecordCodecBuilder.create(codecBuilder -> codecBuilder.group(Codec.FLOAT.fieldOf("duration").forGetter(LargePoisonParticleData::getDuration), Codec.BOOL.fieldOf("swirls").forGetter(LargePoisonParticleData::getSwirls)).apply(codecBuilder, LargePoisonParticleData::new));
	}

	public LargePoisonParticleData(float duration, boolean spins){
		this.duration = duration;
		swirls = spins;
	}

	@OnlyIn( Dist.CLIENT )
	public float getDuration(){
		return duration;
	}

	@OnlyIn( Dist.CLIENT )
	public boolean getSwirls(){
		return swirls;
	}	@Override
	public void writeToNetwork(FriendlyByteBuf buffer){
		buffer.writeFloat(duration);
		buffer.writeBoolean(swirls);
	}



	@SuppressWarnings( "deprecation" )
	@Override
	public String writeToString(){
		return String.format(Locale.ROOT, "%s %.2f %b", ForgeRegistries.PARTICLE_TYPES.getKey(getType()), duration, swirls);
	}


	@Override
	public ParticleType<LargePoisonParticleData> getType(){
		return DSParticles.LARGE_POISON.get();
	}
}
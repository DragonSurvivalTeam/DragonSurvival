package by.dragonsurvivalteam.dragonsurvival.client.render;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.block.LiquidBlockRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;

public class CaveLavaFluidRenderer extends LiquidBlockRenderer{



	@Override // TODO 1.20 :: Unsure / also redo this because wtf
	public void tesselate(BlockAndTintGetter p_228796_1_, BlockPos p_228796_2_, VertexConsumer p_228796_3_, BlockState state, FluidState p_228796_4_){
		try{
			if(p_228796_4_.is(FluidTags.LAVA)){
				TextureAtlasSprite[] atextureatlassprite = net.minecraftforge.client.ForgeHooksClient.getFluidSprites(p_228796_1_, p_228796_2_, p_228796_4_);
				BlockState blockstate = p_228796_1_.getBlockState(p_228796_2_);
				IClientFluidTypeExtensions fluidTypeExtensions = IClientFluidTypeExtensions.of(p_228796_4_);
				int i = fluidTypeExtensions.getTintColor();
				float alpha = 0.85F;
				float f = (float)(i >> 16 & 255) / 255.0F;
				float f1 = (float)(i >> 8 & 255) / 255.0F;
				float f2 = (float)(i & 255) / 255.0F;
				boolean flag1 = !isNeighborSameFluid(p_228796_1_, p_228796_2_, Direction.UP, p_228796_4_);
				boolean flag2 = shouldRenderFace(p_228796_1_, p_228796_2_, p_228796_4_, blockstate, Direction.DOWN) && !isFaceOccludedByNeighbor(p_228796_1_, p_228796_2_, Direction.DOWN, 0.8888889F);
				boolean flag3 = shouldRenderFace(p_228796_1_, p_228796_2_, p_228796_4_, blockstate, Direction.NORTH);
				boolean flag4 = shouldRenderFace(p_228796_1_, p_228796_2_, p_228796_4_, blockstate, Direction.SOUTH);
				boolean flag5 = shouldRenderFace(p_228796_1_, p_228796_2_, p_228796_4_, blockstate, Direction.WEST);
				boolean flag6 = shouldRenderFace(p_228796_1_, p_228796_2_, p_228796_4_, blockstate, Direction.EAST);
				if(!flag1 && !flag2 && !flag6 && !flag5 && !flag3 && !flag4){
					return;
				}else{
					boolean flag7 = false;
					float f3 = p_228796_1_.getShade(Direction.DOWN, true);
					float f4 = p_228796_1_.getShade(Direction.UP, true);
					float f5 = p_228796_1_.getShade(Direction.NORTH, true);
					float f6 = p_228796_1_.getShade(Direction.WEST, true);
					float f7 = getWaterHeight(p_228796_1_, p_228796_2_, p_228796_4_.getType());
					float f8 = getWaterHeight(p_228796_1_, p_228796_2_.south(), p_228796_4_.getType());
					float f9 = getWaterHeight(p_228796_1_, p_228796_2_.east().south(), p_228796_4_.getType());
					float f10 = getWaterHeight(p_228796_1_, p_228796_2_.east(), p_228796_4_.getType());
					double d0 = p_228796_2_.getX() & 15;
					double d1 = p_228796_2_.getY() & 15;
					double d2 = p_228796_2_.getZ() & 15;
					float f12 = flag2 ? 0.001F : 0.0F;
					if(flag1 && !isFaceOccludedByNeighbor(p_228796_1_, p_228796_2_, Direction.UP, Math.min(Math.min(f7, f8), Math.min(f9, f10)))){
						flag7 = true;
						f7 -= 0.001F;
						f8 -= 0.001F;
						f9 -= 0.001F;
						f10 -= 0.001F;
						Vec3 vector3d = p_228796_4_.getFlow(p_228796_1_, p_228796_2_);
						float f13;
						float f14;
						float f15;
						float f16;
						float f17;
						float f18;
						float f19;
						float f20;
						if(vector3d.x == 0.0D && vector3d.z == 0.0D){
							TextureAtlasSprite textureatlassprite1 = atextureatlassprite[0];
							f13 = textureatlassprite1.getU(0.0D);
							f17 = textureatlassprite1.getV(0.0D);
							f14 = f13;
							f18 = textureatlassprite1.getV(16.0D);
							f15 = textureatlassprite1.getU(16.0D);
							f19 = f18;
							f16 = f15;
							f20 = f17;
						}else{
							TextureAtlasSprite textureatlassprite = atextureatlassprite[1];
							float f21 = (float)Mth.atan2(vector3d.z, vector3d.x) - (float)Math.PI / 2F;
							float f22 = Mth.sin(f21) * 0.25F;
							float f23 = Mth.cos(f21) * 0.25F;
							f13 = textureatlassprite.getU(8.0F + (-f23 - f22) * 16.0F);
							f17 = textureatlassprite.getV(8.0F + (-f23 + f22) * 16.0F);
							f14 = textureatlassprite.getU(8.0F + (-f23 + f22) * 16.0F);
							f18 = textureatlassprite.getV(8.0F + (f23 + f22) * 16.0F);
							f15 = textureatlassprite.getU(8.0F + (f23 + f22) * 16.0F);
							f19 = textureatlassprite.getV(8.0F + (f23 - f22) * 16.0F);
							f16 = textureatlassprite.getU(8.0F + (f23 - f22) * 16.0F);
							f20 = textureatlassprite.getV(8.0F + (-f23 - f22) * 16.0F);
						}

						float f43 = (f13 + f14 + f15 + f16) / 4.0F;
						float f44 = (f17 + f18 + f19 + f20) / 4.0F;
//						float f45 = (float)atextureatlassprite[0].getWidth() / (atextureatlassprite[0].getU1() - atextureatlassprite[0].getU0());
//						float f46 = (float)atextureatlassprite[0].getHeight() / (atextureatlassprite[0].getV1() - atextureatlassprite[0].getV0());
//						float f47 = 4.0F / Math.max(f46, f45);
						float f47 = atextureatlassprite[0].uvShrinkRatio();
						f13 = Mth.lerp(f47, f13, f43);
						f14 = Mth.lerp(f47, f14, f43);
						f15 = Mth.lerp(f47, f15, f43);
						f16 = Mth.lerp(f47, f16, f43);
						f17 = Mth.lerp(f47, f17, f44);
						f18 = Mth.lerp(f47, f18, f44);
						f19 = Mth.lerp(f47, f19, f44);
						f20 = Mth.lerp(f47, f20, f44);
						int j = getLightColor(p_228796_1_, p_228796_2_);
						float f25 = f4 * f;
						float f26 = f4 * f1;
						float f27 = f4 * f2;
						vertexVanilla(p_228796_3_, d0 + 0.0D, d1 + (double)f7, d2 + 0.0D, f25, f26, f27, alpha, f13, f17, j);
						vertexVanilla(p_228796_3_, d0 + 0.0D, d1 + (double)f8, d2 + 1.0D, f25, f26, f27, alpha, f14, f18, j);
						vertexVanilla(p_228796_3_, d0 + 1.0D, d1 + (double)f9, d2 + 1.0D, f25, f26, f27, alpha, f15, f19, j);
						vertexVanilla(p_228796_3_, d0 + 1.0D, d1 + (double)f10, d2 + 0.0D, f25, f26, f27, alpha, f16, f20, j);
						if(p_228796_4_.shouldRenderBackwardUpFace(p_228796_1_, p_228796_2_.above())){
							vertexVanilla(p_228796_3_, d0 + 0.0D, d1 + (double)f7, d2 + 0.0D, f25, f26, f27, alpha, f13, f17, j);
							vertexVanilla(p_228796_3_, d0 + 1.0D, d1 + (double)f10, d2 + 0.0D, f25, f26, f27, alpha, f16, f20, j);
							vertexVanilla(p_228796_3_, d0 + 1.0D, d1 + (double)f9, d2 + 1.0D, f25, f26, f27, alpha, f15, f19, j);
							vertexVanilla(p_228796_3_, d0 + 0.0D, d1 + (double)f8, d2 + 1.0D, f25, f26, f27, alpha, f14, f18, j);
						}
					}

					if(flag2){
						float f34 = atextureatlassprite[0].getU0();
						float f35 = atextureatlassprite[0].getU1();
						float f37 = atextureatlassprite[0].getV0();
						float f39 = atextureatlassprite[0].getV1();
						int i1 = getLightColor(p_228796_1_, p_228796_2_.below());
						float f40 = f3 * f;
						float f41 = f3 * f1;
						float f42 = f3 * f2;
						vertexVanilla(p_228796_3_, d0, d1 + (double)f12, d2 + 1.0D, f40, f41, f42, alpha, f34, f39, i1);
						vertexVanilla(p_228796_3_, d0, d1 + (double)f12, d2, f40, f41, f42, alpha, f34, f37, i1);
						vertexVanilla(p_228796_3_, d0 + 1.0D, d1 + (double)f12, d2, f40, f41, f42, alpha, f35, f37, i1);
						vertexVanilla(p_228796_3_, d0 + 1.0D, d1 + (double)f12, d2 + 1.0D, f40, f41, f42, alpha, f35, f39, i1);
						flag7 = true;
					}

					for(int l = 0; l < 4; ++l){
						float f36;
						float f38;
						double d3;
						double d4;
						double d5;
						double d6;
						Direction direction;
						boolean flag8;
						if(l == 0){
							f36 = f7;
							f38 = f10;
							d3 = d0;
							d5 = d0 + 1.0D;
							d4 = d2 + (double)0.001F;
							d6 = d2 + (double)0.001F;
							direction = Direction.NORTH;
							flag8 = flag3;
						}else if(l == 1){
							f36 = f9;
							f38 = f8;
							d3 = d0 + 1.0D;
							d5 = d0;
							d4 = d2 + 1.0D - (double)0.001F;
							d6 = d2 + 1.0D - (double)0.001F;
							direction = Direction.SOUTH;
							flag8 = flag4;
						}else if(l == 2){
							f36 = f8;
							f38 = f7;
							d3 = d0 + (double)0.001F;
							d5 = d0 + (double)0.001F;
							d4 = d2 + 1.0D;
							d6 = d2;
							direction = Direction.WEST;
							flag8 = flag5;
						}else{
							f36 = f10;
							f38 = f9;
							d3 = d0 + 1.0D - (double)0.001F;
							d5 = d0 + 1.0D - (double)0.001F;
							d4 = d2;
							d6 = d2 + 1.0D;
							direction = Direction.EAST;
							flag8 = flag6;
						}

						if(flag8 && !isFaceOccludedByNeighbor(p_228796_1_, p_228796_2_, direction, Math.max(f36, f38))){
							flag7 = true;
							BlockPos blockpos = p_228796_2_.relative(direction);
							TextureAtlasSprite textureatlassprite2 = atextureatlassprite[1];
							if(atextureatlassprite[2] != null){
								if(p_228796_1_.getBlockState(blockpos).shouldDisplayFluidOverlay(p_228796_1_, blockpos, p_228796_4_)){
									textureatlassprite2 = atextureatlassprite[2];
								}
							}

							float f48 = textureatlassprite2.getU(0.0D);
							float f49 = textureatlassprite2.getU(8.0D);
							float f50 = textureatlassprite2.getV((1.0F - f36) * 16.0F * 0.5F);
							float f28 = textureatlassprite2.getV((1.0F - f38) * 16.0F * 0.5F);
							float f29 = textureatlassprite2.getV(8.0D);
							int k = getLightColor(p_228796_1_, blockpos);
							float f30 = l < 2 ? f5 : f6;
							float f31 = f4 * f30 * f;
							float f32 = f4 * f30 * f1;
							float f33 = f4 * f30 * f2;
							vertexVanilla(p_228796_3_, d3, d1 + (double)f36, d4, f31, f32, f33, alpha, f48, f50, k);
							vertexVanilla(p_228796_3_, d5, d1 + (double)f38, d6, f31, f32, f33, alpha, f49, f28, k);
							vertexVanilla(p_228796_3_, d5, d1 + (double)f12, d6, f31, f32, f33, alpha, f49, f29, k);
							vertexVanilla(p_228796_3_, d3, d1 + (double)f12, d4, f31, f32, f33, alpha, f48, f29, k);
							if(textureatlassprite2 != atextureatlassprite[2]){
								vertexVanilla(p_228796_3_, d3, d1 + (double)f12, d4, f31, f32, f33, alpha, f48, f29, k);
								vertexVanilla(p_228796_3_, d5, d1 + (double)f12, d6, f31, f32, f33, alpha, f49, f29, k);
								vertexVanilla(p_228796_3_, d5, d1 + (double)f38, d6, f31, f32, f33, alpha, f49, f28, k);
								vertexVanilla(p_228796_3_, d3, d1 + (double)f36, d4, f31, f32, f33, alpha, f48, f50, k);
							}
						}
					}
					//return flag7;
				}
			}else{
				super.tesselate(p_228796_1_, p_228796_2_, p_228796_3_, state, p_228796_4_);
			}
		}catch(Exception ex){
			return;
		}
	}

	private static boolean isNeighborSameFluid(BlockGetter p_209557_0_, BlockPos p_209557_1_, Direction p_209557_2_, FluidState p_209557_3_){
		BlockPos blockpos = p_209557_1_.relative(p_209557_2_);
		FluidState fluidstate = p_209557_0_.getFluidState(blockpos);
		return fluidstate.getType().isSame(p_209557_3_.getType());
	}

	private static boolean isFaceOccludedByNeighbor(BlockGetter p_239283_0_, BlockPos p_239283_1_, Direction p_239283_2_, float p_239283_3_){
		BlockPos blockpos = p_239283_1_.relative(p_239283_2_);
		BlockState blockstate = p_239283_0_.getBlockState(blockpos);
		return isFaceOccludedByState(p_239283_0_, p_239283_2_, p_239283_3_, blockpos, blockstate);
	}

	public static boolean shouldRenderFace(BlockAndTintGetter p_239281_0_, BlockPos p_239281_1_, FluidState p_239281_2_, BlockState p_239281_3_, Direction p_239281_4_){
		return !isFaceOccludedBySelf(p_239281_0_, p_239281_1_, p_239281_3_, p_239281_4_) && !isNeighborSameFluid(p_239281_0_, p_239281_1_, p_239281_4_, p_239281_2_);
	}

	private static boolean isFaceOccludedBySelf(BlockGetter p_239282_0_, BlockPos p_239282_1_, BlockState p_239282_2_, Direction p_239282_3_){
		return isFaceOccludedByState(p_239282_0_, p_239282_3_.getOpposite(), 1.0F, p_239282_1_, p_239282_2_);
	}

	private static boolean isFaceOccludedByState(BlockGetter p_239284_0_, Direction p_239284_1_, float p_239284_2_, BlockPos p_239284_3_, BlockState p_239284_4_){
		if(p_239284_4_.canOcclude()){
			VoxelShape voxelshape = Shapes.box(0.0D, 0.0D, 0.0D, 1.0D, p_239284_2_, 1.0D);
			VoxelShape voxelshape1 = p_239284_4_.getOcclusionShape(p_239284_0_, p_239284_3_);
			return Shapes.blockOccudes(voxelshape, voxelshape1, p_239284_1_);
		}else{
			return false;
		}
	}

	private void vertexVanilla(VertexConsumer vertexBuilderIn, double x, double y, double z, float red, float green, float blue, float alpha, float u, float v, int packedLight){
		vertexBuilderIn.vertex(x, y, z).color(red, green, blue, alpha).uv(u, v).uv2(packedLight).normal(0.0F, 1.0F, 0.0F).endVertex();
	}

	private int getLightColor(BlockAndTintGetter p_228795_1_, BlockPos p_228795_2_){
		int i = LevelRenderer.getLightColor(p_228795_1_, p_228795_2_);
		int j = LevelRenderer.getLightColor(p_228795_1_, p_228795_2_.above());
		int k = i & 255;
		int l = j & 255;
		int i1 = i >> 16 & 255;
		int j1 = j >> 16 & 255;
		return Math.max(k, l) | Math.max(i1, j1) << 16;
	}

	private float getWaterHeight(BlockGetter p_217640_1_, BlockPos p_217640_2_, Fluid p_217640_3_){
		int i = 0;
		float f = 0.0F;

		for(int j = 0; j < 4; ++j){
			BlockPos blockpos = p_217640_2_.offset(-(j & 1), 0, -(j >> 1 & 1));
			if(p_217640_1_.getFluidState(blockpos.above()).getType().isSame(p_217640_3_)){
				return 1.0F;
			}

			FluidState fluidstate = p_217640_1_.getFluidState(blockpos);
			if(fluidstate.getType().isSame(p_217640_3_)){
				float f1 = fluidstate.getHeight(p_217640_1_, blockpos);
				if(f1 >= 0.8F){
					f += f1 * 10.0F;
					i += 10;
				}else{
					f += f1;
					++i;
				}
			}else if(!p_217640_1_.getBlockState(blockpos).isSolid()){
				++i;
			}
		}

		return f / (float)i;
	}
}
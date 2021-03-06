package com.stal111.forbidden_arcanus.util;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.pipeline.BakedQuadBuilder;
import net.minecraftforge.client.model.pipeline.LightUtil;
import net.minecraftforge.client.model.pipeline.VertexLighterFlat;

import javax.annotation.Nullable;
import java.util.*;

public class FullbrightBakedModel extends DelegateBakedModel {

    private static final LoadingCache<CacheKey, List<BakedQuad>> CACHE = CacheBuilder.newBuilder().build(new CacheLoader<CacheKey, List<BakedQuad>>() {
        @Override
        public List<BakedQuad> load(CacheKey key) {
            return transformQuads(key.base.getQuads(key.state, key.side, key.random, EmptyModelData.INSTANCE), key.textures);
        }
    });

    private Set<ResourceLocation> textures;
    private final boolean doCaching;

    public FullbrightBakedModel(IBakedModel base, boolean doCaching, ResourceLocation... textures) {
        super(base);

        this.textures = new HashSet<>(Arrays.asList(textures));
        this.doCaching = doCaching;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, Random rand, IModelData data) {
        if (state == null) {
            return base.getQuads(null, side, rand, data);
        }

        if (!doCaching) {
            return transformQuads(base.getQuads(state, side, rand, data), textures);
        }

        return CACHE.getUnchecked(new CacheKey(base, textures, rand, state, side));
    }

    private static List<BakedQuad> transformQuads(List<BakedQuad> oldQuads, Set<ResourceLocation> textures) {
        List<BakedQuad> quads = new ArrayList<>(oldQuads);

        for (int i = 0; i < quads.size(); ++i) {
            BakedQuad quad = quads.get(i);

            if (textures.contains(quad.func_187508_a().getName())) {
                quads.set(i, transformQuad(quad));
            }
        }
        return quads;
    }

    private static BakedQuad transformQuad(BakedQuad quad) {
        int[] vertexData = quad.getVertexData().clone();

        // Set lighting to fullbright on all vertices
        vertexData[6] = 0x00F000F0;
        vertexData[6 + 8] = 0x00F000F0;
        vertexData[6 + 8 + 8] = 0x00F000F0;
        vertexData[6 + 8 + 8 + 8] = 0x00F000F0;

        return new BakedQuad(
                vertexData,
                quad.getTintIndex(),
                quad.getFace(),
                quad.func_187508_a(),
                quad.shouldApplyDiffuseLighting()
        );
    }

    private class CacheKey {

        private IBakedModel base;
        private Set<ResourceLocation> textures;
        private Random random;
        private BlockState state;
        private Direction side;

        public CacheKey(IBakedModel base, Set<ResourceLocation> textures, Random random, BlockState state, Direction side) {
            this.base = base;
            this.textures = textures;
            this.random = random;
            this.state = state;
            this.side = side;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            CacheKey cacheKey = (CacheKey) o;

            if (cacheKey.side != side) {
                return false;
            }

            if (!state.equals(cacheKey.state)) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            return state.hashCode() + (31 * (side != null ? side.hashCode() : 0));
        }
    }
}
/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2015, AlgorithmX2, All rights reserved.
 *
 * Applied Energistics 2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Applied Energistics 2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Applied Energistics 2.  If not, see <http://www.gnu.org/licenses/lgpl>.
 */

package appeng.items.storage;

import java.util.List;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;

import appeng.api.implementations.TransitionResult;
import appeng.api.implementations.items.ISpatialStorageCell;
import appeng.api.storage.ISpatialDimension;
import appeng.api.util.WorldCoord;
import appeng.core.AELog;
import appeng.core.localization.GuiText;
import appeng.items.AEBaseItem;
import appeng.spatial.SpatialDimensionManager;
import appeng.spatial.StorageHelper;

public class SpatialStorageCellItem extends AEBaseItem implements ISpatialStorageCell {
    private static final String TAG_DIMENSION_ID = "dimension_id";

    private final int maxRegion;

    public SpatialStorageCellItem(Settings props, final int spatialScale) {
        super(props);
        this.maxRegion = spatialScale;
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void appendTooltip(final ItemStack stack, final World world, final List<Text> lines,
            final TooltipContext advancedTooltips) {
        final RegistryKey<World> worldId = this.getStoredDimension(stack);
        if (worldId == null) {
            lines.add(GuiText.Unformatted.text().copy().formatted(Formatting.ITALIC));
            lines.add(GuiText.SpatialCapacity.text(maxRegion, maxRegion, maxRegion));
        } else {
            SpatialDimensionManager.INSTANCE.addCellDimensionTooltip(worldId, lines);
        }

        if (advancedTooltips.isAdvanced()) {
            if (worldId != null && worldId.getValue() != null) {
                lines.add(new LiteralText("Dimension: " + worldId.getValue()));
            }
        }
    }

    @Override
    public boolean isSpatialStorage(final ItemStack is) {
        return true;
    }

    @Override
    public int getMaxStoredDim(final ItemStack is) {
        return this.maxRegion;
    }

    @Override
    public RegistryKey<World> getStoredDimension(final ItemStack is) {
        final CompoundTag c = is.getTag();
        if (c != null && c.contains(TAG_DIMENSION_ID)) {
            try {
                Identifier worldId = new Identifier(c.getString(TAG_DIMENSION_ID));
                return RegistryKey.of(Registry.DIMENSION, worldId);
            } catch (Exception e) {
                AELog.warn("Failed to retrieve storage cell dimension.", e);
            }
        }
        return null;
    }

    @Override
    public TransitionResult doSpatialTransition(final ItemStack is, final ServerWorld w, final WorldCoord min,
            final WorldCoord max, int playerId) {
        final int targetX = max.x - min.x - 1;
        final int targetY = max.y - min.y - 1;
        final int targetZ = max.z - min.z - 1;
        final int maxSize = this.getMaxStoredDim(is);

        final BlockPos targetSize = new BlockPos(targetX, targetY, targetZ);

        ISpatialDimension manager = SpatialDimensionManager.INSTANCE;

        RegistryKey<World> worldId = this.getStoredDimension(is);
        if (worldId == null || manager.getWorld(worldId) == null) {
            worldId = manager.createNewCellDimension(targetSize);
        }

        if (worldId == null) {
            // Failed to create the dimension
            return new TransitionResult(false, 0);
        }

        try {
            if (manager.isCellDimension(worldId)) {
                ServerWorld cellWorld = manager.getWorld(worldId);

                BlockPos scale = manager.getCellDimensionSize(worldId);

                if (scale.equals(targetSize)) {
                    if (targetX <= maxSize && targetY <= maxSize && targetZ <= maxSize) {
                        BlockPos offset = manager.getCellDimensionOrigin(worldId);

                        this.setStoredDimension(is, worldId);
                        StorageHelper.getInstance().swapRegions(w, min.x + 1, min.y + 1, min.z + 1, cellWorld,
                                offset.getX(), offset.getY(), offset.getZ(), targetX - 1, targetY - 1, targetZ - 1);

                        return new TransitionResult(true, 0);
                    }
                }
            }
            return new TransitionResult(false, 0);
        } finally {
            // clean up newly created dimensions that failed transfer
            if (manager.isCellDimension(worldId) && this.getStoredDimension(is) == null) {
                manager.deleteCellDimension(worldId);
            }
        }
    }

    private void setStoredDimension(final ItemStack is, RegistryKey<World> worldId) {
        final CompoundTag c = is.getOrCreateTag();
        c.putString(TAG_DIMENSION_ID, worldId.getValue().toString());
    }
}
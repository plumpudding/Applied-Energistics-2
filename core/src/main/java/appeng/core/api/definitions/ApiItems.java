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

package appeng.core.api.definitions;

import appeng.api.definitions.IItemDefinition;
import appeng.api.definitions.IItems;
import appeng.api.features.AEFeature;
import appeng.api.util.AEColoredItemDefinition;
import appeng.bootstrap.FeatureFactory;
import appeng.bootstrap.IItemRendering;
import appeng.bootstrap.ItemRenderingCustomizer;
import appeng.fluids.items.FluidDummyItem;
import appeng.items.tools.quartz.*;
import net.minecraft.item.ItemGroup;

/**
 * Internal implementation for the API items
 */
public final class ApiItems implements IItems {
    private IItemDefinition certusQuartzAxe;
    private IItemDefinition certusQuartzHoe;
    private IItemDefinition certusQuartzShovel;
    private IItemDefinition certusQuartzPick;
    private IItemDefinition certusQuartzSword;
    private IItemDefinition certusQuartzWrench;
    private IItemDefinition certusQuartzKnife;

    private IItemDefinition netherQuartzAxe;
    private IItemDefinition netherQuartzHoe;
    private IItemDefinition netherQuartzShovel;
    private IItemDefinition netherQuartzPick;
    private IItemDefinition netherQuartzSword;
    private IItemDefinition netherQuartzWrench;
    private IItemDefinition netherQuartzKnife;

    private IItemDefinition entropyManipulator;
    private IItemDefinition wirelessTerminal;
    private IItemDefinition biometricCard;
    private IItemDefinition chargedStaff;
    private IItemDefinition massCannon;
    private IItemDefinition memoryCard;
    private IItemDefinition networkTool;
    private IItemDefinition portableCell;

    private IItemDefinition cellCreative;
    private IItemDefinition viewCell;

    private IItemDefinition cell1k;
    private IItemDefinition cell4k;
    private IItemDefinition cell16k;
    private IItemDefinition cell64k;

    private IItemDefinition fluidCell1k;
    private IItemDefinition fluidCell4k;
    private IItemDefinition fluidCell16k;
    private IItemDefinition fluidCell64k;

    private IItemDefinition spatialCell2;
    private IItemDefinition spatialCell16;
    private IItemDefinition spatialCell128;

    private IItemDefinition facade;
    private IItemDefinition certusCrystalSeed;
    private IItemDefinition fluixCrystalSeed;
    private IItemDefinition netherQuartzSeed;

    // rv1
    private IItemDefinition encodedPattern;
    private IItemDefinition colorApplicator;

    private AEColoredItemDefinition coloredPaintBall;
    private AEColoredItemDefinition coloredLumenPaintBall;

    // unsupported dev tools
    private IItemDefinition toolEraser;
    private IItemDefinition toolMeteoritePlacer;
    private IItemDefinition toolDebugCard;
    private IItemDefinition toolReplicatorCard;

    private IItemDefinition dummyFluidItem;

    public ApiItems(FeatureFactory registry, ApiMaterials materials) {
        FeatureFactory certusTools = registry.features(AEFeature.CERTUS_QUARTZ_TOOLS);
        this.certusQuartzAxe = certusTools
                .item("certus_quartz_axe", props -> new QuartzAxeItem(props, AEFeature.CERTUS_QUARTZ_TOOLS))
                .itemGroup(ItemGroup.TOOLS).addFeatures(AEFeature.QUARTZ_AXE).build();
        this.certusQuartzHoe = certusTools
                .item("certus_quartz_hoe", props -> new QuartzHoeItem(props, AEFeature.CERTUS_QUARTZ_TOOLS))
                .itemGroup(ItemGroup.TOOLS).addFeatures(AEFeature.QUARTZ_HOE).build();
        this.certusQuartzShovel = certusTools
                .item("certus_quartz_shovel", props -> new QuartzSpadeItem(props, AEFeature.CERTUS_QUARTZ_TOOLS))
                .itemGroup(ItemGroup.TOOLS).addFeatures(AEFeature.QUARTZ_SPADE).build();
        this.certusQuartzPick = certusTools
                .item("certus_quartz_pickaxe", props -> new QuartzPickaxeItem(props, AEFeature.CERTUS_QUARTZ_TOOLS))
                .itemGroup(ItemGroup.TOOLS).addFeatures(AEFeature.QUARTZ_PICKAXE).build();
        this.certusQuartzSword = certusTools
                .item("certus_quartz_sword", props -> new QuartzSwordItem(props, AEFeature.CERTUS_QUARTZ_TOOLS))
                .itemGroup(ItemGroup.COMBAT).addFeatures(AEFeature.QUARTZ_SWORD).build();
        this.certusQuartzWrench = certusTools.item("certus_quartz_wrench", QuartzWrenchItem::new)
                .itemGroup(ItemGroup.TOOLS).props(props -> props.maxCount(1)).addFeatures(AEFeature.QUARTZ_WRENCH)
                .build();
        this.certusQuartzKnife = certusTools
                .item("certus_quartz_cutting_knife",
                        props -> new QuartzCuttingKnifeItem(props, AEFeature.CERTUS_QUARTZ_TOOLS))
                .itemGroup(ItemGroup.TOOLS).props(props -> props.maxCount(1).maxDamage(50))
                .addFeatures(AEFeature.QUARTZ_KNIFE).build();

        FeatureFactory netherTools = registry.features(AEFeature.NETHER_QUARTZ_TOOLS);
        this.netherQuartzAxe = netherTools
                .item("nether_quartz_axe", props -> new QuartzAxeItem(props, AEFeature.NETHER_QUARTZ_TOOLS))
                .itemGroup(ItemGroup.TOOLS).addFeatures(AEFeature.QUARTZ_AXE).build();
        this.netherQuartzHoe = netherTools
                .item("nether_quartz_hoe", props -> new QuartzHoeItem(props, AEFeature.NETHER_QUARTZ_TOOLS))
                .itemGroup(ItemGroup.TOOLS).addFeatures(AEFeature.QUARTZ_HOE).build();
        this.netherQuartzShovel = netherTools
                .item("nether_quartz_shovel", props -> new QuartzSpadeItem(props, AEFeature.NETHER_QUARTZ_TOOLS))
                .itemGroup(ItemGroup.TOOLS).addFeatures(AEFeature.QUARTZ_SPADE).build();
        this.netherQuartzPick = netherTools
                .item("nether_quartz_pickaxe", props -> new QuartzPickaxeItem(props, AEFeature.NETHER_QUARTZ_TOOLS))
                .itemGroup(ItemGroup.TOOLS).addFeatures(AEFeature.QUARTZ_PICKAXE).build();
        this.netherQuartzSword = netherTools
                .item("nether_quartz_sword", props -> new QuartzSwordItem(props, AEFeature.NETHER_QUARTZ_TOOLS))
                .itemGroup(ItemGroup.COMBAT).addFeatures(AEFeature.QUARTZ_SWORD).build();
        this.netherQuartzWrench = netherTools.item("nether_quartz_wrench", QuartzWrenchItem::new)
                .itemGroup(ItemGroup.TOOLS).props(props -> props.maxCount(1)).addFeatures(AEFeature.QUARTZ_WRENCH)
                .build();
        this.netherQuartzKnife = netherTools
                .item("nether_quartz_cutting_knife",
                        props -> new QuartzCuttingKnifeItem(props, AEFeature.NETHER_QUARTZ_TOOLS))
                .itemGroup(ItemGroup.TOOLS).props(props -> props.maxCount(1).maxDamage(50))
                .addFeatures(AEFeature.QUARTZ_KNIFE).build();

//        Consumer<Item.Settings> chargedDefaults = props -> props.maxCount(1).maxDamage(32).setNoRepair();
//
//        FeatureFactory powerTools = registry.features(AEFeature.POWERED_TOOLS);
//        this.entropyManipulator = powerTools.item("entropy_manipulator", EntropyManipulatorItem::new)
//                .props(chargedDefaults).addFeatures(AEFeature.ENTROPY_MANIPULATOR)
//                .dispenserBehavior(BlockToolDispenseItemBehavior::new).build();
//        this.wirelessTerminal = powerTools.item("wireless_terminal", WirelessTerminalItem::new).props(chargedDefaults)
//                .addFeatures(AEFeature.WIRELESS_ACCESS_TERMINAL).build();
//        this.chargedStaff = powerTools.item("charged_staff", ChargedStaffItem::new).props(chargedDefaults)
//                .addFeatures(AEFeature.CHARGED_STAFF).build();
//        this.massCannon = powerTools.item("matter_cannon", MatterCannonItem::new).props(chargedDefaults)
//                .addFeatures(AEFeature.MATTER_CANNON).dispenserBehavior(MatterCannonDispenseItemBehavior::new).build();
//        this.portableCell = powerTools.item("portable_cell", PortableCellItem::new).props(chargedDefaults)
//                .addFeatures(AEFeature.PORTABLE_CELL, AEFeature.STORAGE_CELLS).build();
//        this.colorApplicator = powerTools.item("color_applicator", ColorApplicatorItem::new).props(chargedDefaults)
//                .addFeatures(AEFeature.COLOR_APPLICATOR).dispenserBehavior(BlockToolDispenseItemBehavior::new)
//                .rendering(new ColorApplicatorItemRendering()).build();
//
//        this.biometricCard = registry.item("biometric_card", BiometricCardItem::new)
//                .props(props -> props.maxCount(1)).features(AEFeature.SECURITY).build();
//        this.memoryCard = registry.item("memory_card", MemoryCardItem::new).props(props -> props.maxCount(1))
//                .features(AEFeature.MEMORY_CARD).build();
//        this.networkTool = registry.item("network_tool", NetworkToolItem::new)
//                .props(props -> props.maxCount(1).addToolType(FabricToolTags.get("wrench"), 0))
//                .features(AEFeature.NETWORK_TOOL).build();
//
//        this.cellCreative = registry.item("creative_storage_cell", CreativeStorageCellItem::new)
//                .props(props -> props.maxCount(1)).features(AEFeature.STORAGE_CELLS, AEFeature.CREATIVE).build();
//        this.viewCell = registry.item("view_cell", ViewCellItem::new).props(props -> props.maxCount(1))
//                .features(AEFeature.VIEW_CELL).build();
//
//        Consumer<Item.Settings> storageCellProps = p -> p.maxCount(1);
//
//        FeatureFactory storageCells = registry.features(AEFeature.STORAGE_CELLS);
//        this.cell1k = storageCells
//                .item("1k_storage_cell",
//                        props -> new BasicStorageCellItem(props, MaterialType.ITEM_1K_CELL_COMPONENT, 1))
//                .props(storageCellProps).build();
//        this.cell4k = storageCells
//                .item("4k_storage_cell",
//                        props -> new BasicStorageCellItem(props, MaterialType.ITEM_4K_CELL_COMPONENT, 4))
//                .props(storageCellProps).build();
//        this.cell16k = storageCells
//                .item("16k_storage_cell",
//                        props -> new BasicStorageCellItem(props, MaterialType.ITEM_16K_CELL_COMPONENT, 16))
//                .props(storageCellProps).build();
//        this.cell64k = storageCells
//                .item("64k_storage_cell",
//                        props -> new BasicStorageCellItem(props, MaterialType.ITEM_64K_CELL_COMPONENT, 64))
//                .props(storageCellProps).build();
//
//        this.fluidCell1k = storageCells
//                .item("1k_fluid_storage_cell",
//                        props -> new BasicFluidStorageCell(props, MaterialType.FLUID_1K_CELL_COMPONENT, 1))
//                .props(storageCellProps).build();
//        this.fluidCell4k = storageCells
//                .item("4k_fluid_storage_cell",
//                        props -> new BasicFluidStorageCell(props, MaterialType.FLUID_4K_CELL_COMPONENT, 4))
//                .props(storageCellProps).build();
//        this.fluidCell16k = storageCells
//                .item("16k_fluid_storage_cell",
//                        props -> new BasicFluidStorageCell(props, MaterialType.FLUID_16K_CELL_COMPONENT, 16))
//                .props(storageCellProps).build();
//        this.fluidCell64k = storageCells
//                .item("64k_fluid_storage_cell",
//                        props -> new BasicFluidStorageCell(props, MaterialType.FLUID_64K_CELL_COMPONENT, 64))
//                .props(storageCellProps).build();
//
//        FeatureFactory spatialCells = registry.features(AEFeature.SPATIAL_IO);
//        this.spatialCell2 = spatialCells
//                .item("2_cubed_spatial_storage_cell", props -> new SpatialStorageCellItem(props, 2))
//                .props(storageCellProps).build();
//        this.spatialCell16 = spatialCells
//                .item("16_cubed_spatial_storage_cell", props -> new SpatialStorageCellItem(props, 16))
//                .props(storageCellProps).build();
//        this.spatialCell128 = spatialCells
//                .item("128_cubed_spatial_storage_cell", props -> new SpatialStorageCellItem(props, 128))
//                .props(storageCellProps).build();
//
//        this.facade = registry.item("facade", FacadeItem::new).features(AEFeature.FACADES).build();
//
//        this.certusCrystalSeed = registry
//                .item("certus_crystal_seed",
//                        props -> new CrystalSeedItem(props, materials.purifiedCertusQuartzCrystal().item()))
//                .features(AEFeature.CRYSTAL_SEEDS).build();
//        this.fluixCrystalSeed = registry
//                .item("fluix_crystal_seed",
//                        props -> new CrystalSeedItem(props, materials.purifiedFluixCrystal().item()))
//                .features(AEFeature.CRYSTAL_SEEDS).build();
//        this.netherQuartzSeed = registry
//                .item("nether_quartz_seed",
//                        props -> new CrystalSeedItem(props, materials.purifiedNetherQuartzCrystal().item()))
//                .features(AEFeature.CRYSTAL_SEEDS).build();
//
//        GrowingCrystalEntity.TYPE = registry
//                .<GrowingCrystalEntity>entity("growing_crystal", GrowingCrystalEntity::new, SpawnGroup.MISC)
//                .customize(builder -> builder.size(0.25F, 0.25F)).build();
//
//        // rv1
//        this.encodedPattern = registry.item("encoded_pattern", EncodedPatternItem::new)
//                .props(props -> props.maxCount(1)).features(AEFeature.PATTERNS).build();
//
//        this.coloredPaintBall = createPaintBalls(registry, "_paint_ball", false);
//        this.coloredLumenPaintBall = createPaintBalls(registry, "_lumen_paint_ball", true);
//
//        FeatureFactory debugTools = registry.features(AEFeature.UNSUPPORTED_DEVELOPER_TOOLS, AEFeature.CREATIVE);
//        this.toolEraser = debugTools.item("debug_eraser", EraserItem::new).build();
//        this.toolMeteoritePlacer = debugTools.item("debug_meteorite_placer", MeteoritePlacerItem::new).build();
//        this.toolDebugCard = debugTools.item("debug_card", DebugCardItem::new).build();
//        this.toolReplicatorCard = debugTools.item("debug_replicator_card", ReplicatorCardItem::new).build();
//        debugTools.item("debug_part_placer", DebugPartPlacerItem::new).build();
//
//        this.dummyFluidItem = registry.item("dummy_fluid_item", FluidDummyItem::new)
//                .rendering(new ItemRenderingCustomizer() {
//                    @Override
//                    @Environment(Dist.CLIENT)
//                    public void customize(IItemRendering rendering) {
//                        rendering.color(new FluidDummyItemColor());
//                    }
//                })
//                .build();
    }

//    private static AEColoredItemDefinition createPaintBalls(FeatureFactory registry, String idSuffix, boolean lumen) {
//        ColoredItemDefinition colors = new ColoredItemDefinition();
//        for (AEColor color : AEColor.values()) {
//            if (color == AEColor.TRANSPARENT) {
//                continue; // Fluix paintballs don't exist
//            }
//
//            String id = color.registryPrefix + idSuffix;
//            IItemDefinition paintBall = registry.item(id, props -> new PaintBallItem(props, color, lumen))
//                    .features(AEFeature.PAINT_BALLS).rendering(new PaintBallItemRendering(color, lumen)).build();
//            colors.add(color, new ItemStackSrc(paintBall.item(), ActivityState.Enabled));
//        }
//        return colors;
//    }
//
    @Override
    public IItemDefinition certusQuartzAxe() {
        return this.certusQuartzAxe;
    }

    @Override
    public IItemDefinition certusQuartzHoe() {
        return this.certusQuartzHoe;
    }

    @Override
    public IItemDefinition certusQuartzShovel() {
        return this.certusQuartzShovel;
    }

    @Override
    public IItemDefinition certusQuartzPick() {
        return this.certusQuartzPick;
    }

    @Override
    public IItemDefinition certusQuartzSword() {
        return this.certusQuartzSword;
    }

    @Override
    public IItemDefinition certusQuartzWrench() {
        return this.certusQuartzWrench;
    }

    @Override
    public IItemDefinition certusQuartzKnife() {
        return this.certusQuartzKnife;
    }

    @Override
    public IItemDefinition netherQuartzAxe() {
        return this.netherQuartzAxe;
    }

    @Override
    public IItemDefinition netherQuartzHoe() {
        return this.netherQuartzHoe;
    }

    @Override
    public IItemDefinition netherQuartzShovel() {
        return this.netherQuartzShovel;
    }

    @Override
    public IItemDefinition netherQuartzPick() {
        return this.netherQuartzPick;
    }

    @Override
    public IItemDefinition netherQuartzSword() {
        return this.netherQuartzSword;
    }

    @Override
    public IItemDefinition netherQuartzWrench() {
        return this.netherQuartzWrench;
    }

    @Override
    public IItemDefinition netherQuartzKnife() {
        return this.netherQuartzKnife;
    }

    @Override
    public IItemDefinition entropyManipulator() {
        return this.entropyManipulator;
    }

    @Override
    public IItemDefinition wirelessTerminal() {
        return this.wirelessTerminal;
    }

    @Override
    public IItemDefinition biometricCard() {
        return this.biometricCard;
    }

    @Override
    public IItemDefinition chargedStaff() {
        return this.chargedStaff;
    }

    @Override
    public IItemDefinition massCannon() {
        return this.massCannon;
    }

    @Override
    public IItemDefinition memoryCard() {
        return this.memoryCard;
    }

    @Override
    public IItemDefinition networkTool() {
        return this.networkTool;
    }

    @Override
    public IItemDefinition portableCell() {
        return this.portableCell;
    }

    @Override
    public IItemDefinition cellCreative() {
        return this.cellCreative;
    }

    @Override
    public IItemDefinition viewCell() {
        return this.viewCell;
    }

    @Override
    public IItemDefinition cell1k() {
        return this.cell1k;
    }

    @Override
    public IItemDefinition cell4k() {
        return this.cell4k;
    }

    @Override
    public IItemDefinition cell16k() {
        return this.cell16k;
    }

    @Override
    public IItemDefinition cell64k() {
        return this.cell64k;
    }

    @Override
    public IItemDefinition fluidCell1k() {
        return this.fluidCell1k;
    }

    @Override
    public IItemDefinition fluidCell4k() {
        return this.fluidCell4k;
    }

    @Override
    public IItemDefinition fluidCell16k() {
        return this.fluidCell16k;
    }

    @Override
    public IItemDefinition fluidCell64k() {
        return this.fluidCell64k;
    }

    @Override
    public IItemDefinition spatialCell2() {
        return this.spatialCell2;
    }

    @Override
    public IItemDefinition spatialCell16() {
        return this.spatialCell16;
    }

    @Override
    public IItemDefinition spatialCell128() {
        return this.spatialCell128;
    }

    @Override
    public IItemDefinition facade() {
        return this.facade;
    }

    @Override
    public IItemDefinition certusCrystalSeed() {
        return certusCrystalSeed;
    }

    @Override
    public IItemDefinition fluixCrystalSeed() {
        return fluixCrystalSeed;
    }

    @Override
    public IItemDefinition netherQuartzSeed() {
        return netherQuartzSeed;
    }

    @Override
    public IItemDefinition encodedPattern() {
        return this.encodedPattern;
    }

    @Override
    public IItemDefinition colorApplicator() {
        return this.colorApplicator;
    }

    @Override
    public AEColoredItemDefinition coloredPaintBall() {
        return this.coloredPaintBall;
    }

    @Override
    public AEColoredItemDefinition coloredLumenPaintBall() {
        return this.coloredLumenPaintBall;
    }

    public IItemDefinition toolEraser() {
        return this.toolEraser;
    }

    public IItemDefinition toolMeteoritePlacer() {
        return this.toolMeteoritePlacer;
    }

    public IItemDefinition toolDebugCard() {
        return this.toolDebugCard;
    }

    public IItemDefinition toolReplicatorCard() {
        return this.toolReplicatorCard;
    }

    @Override
    public IItemDefinition dummyFluidItem() {
        return this.dummyFluidItem;
    }
}

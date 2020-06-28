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

package appeng.container.implementations;

import java.util.Iterator;

import alexiil.mc.lib.attributes.item.FixedItemInv;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.IContainerListener;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import alexiil.mc.lib.attributes.item.impl.EmptyFixedItemInv;

import appeng.api.AEApi;
import appeng.api.config.CopyMode;
import appeng.api.config.FuzzyMode;
import appeng.api.config.Settings;
import appeng.api.implementations.items.IStorageCell;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.cells.ICellWorkbenchItem;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;
import appeng.container.ContainerLocator;
import appeng.container.guisync.GuiSync;
import appeng.container.slot.FakeTypeOnlySlot;
import appeng.container.slot.OptionalRestrictedInputSlot;
import appeng.container.slot.RestrictedInputSlot;
import appeng.tile.misc.CellWorkbenchBlockEntity;
import appeng.util.EnumCycler;
import appeng.util.Platform;
import appeng.util.helpers.ItemHandlerUtil;
import appeng.util.inv.WrapperSupplierItemHandler;
import appeng.util.iterators.NullIterator;

public class CellWorkbenchContainer extends UpgradeableContainer {

    public static ContainerType<CellWorkbenchContainer> TYPE;

    private static final ContainerHelper<CellWorkbenchContainer, CellWorkbenchBlockEntity> helper = new ContainerHelper<>(
            CellWorkbenchContainer::new, CellWorkbenchBlockEntity.class);

    private final CellWorkbenchBlockEntity workBench;
    @GuiSync(2)
    public CopyMode copyMode = CopyMode.CLEAR_ON_REMOVE;
    private ItemStack prevStack = ItemStack.EMPTY;
    private int lastUpgrades = 0;

    public CellWorkbenchContainer(int id, final PlayerInventory ip, final CellWorkbenchBlockEntity te) {
        super(TYPE, id, ip, te);
        this.workBench = te;
    }

    public static CellWorkbenchContainer fromNetwork(int windowId, PlayerInventory inv, PacketByteBuf buf) {
        return helper.fromNetwork(windowId, inv, buf);
    }

    public static boolean open(PlayerEntity player, ContainerLocator locator) {
        return helper.open(player, locator);
    }

    public void setFuzzy(final FuzzyMode valueOf) {
        final ICellWorkbenchItem cwi = this.workBench.getCell();
        if (cwi != null) {
            cwi.setFuzzyMode(this.workBench.getInventoryByName("cell").getInvStack(0), valueOf);
        }
    }

    public void nextWorkBenchCopyMode() {
        this.workBench.getConfigManager().putSetting(Settings.COPY_MODE, EnumCycler.next(this.getWorkBenchCopyMode()));
    }

    private CopyMode getWorkBenchCopyMode() {
        return (CopyMode) this.workBench.getConfigManager().getSetting(Settings.COPY_MODE);
    }

    @Override
    protected int getHeight() {
        return 251;
    }

    @Override
    protected void setupConfig() {
        final FixedItemInv cell = this.getUpgradeable().getInventoryByName("cell");
        this.addSlot(new RestrictedInputSlot(RestrictedInputSlot.PlacableItemType.WORKBENCH_CELL, cell, 0, 152, 8,
                this.getPlayerInv()));

        final FixedItemInv inv = this.getUpgradeable().getInventoryByName("config");
        final WrapperSupplierItemHandler upgradeInventory = new WrapperSupplierItemHandler(
                this::getCellUpgradeInventory);

        int offset = 0;
        final int y = 29;
        final int x = 8;
        for (int w = 0; w < 7; w++) {
            for (int z = 0; z < 9; z++) {
                this.addSlot(new FakeTypeOnlySlot(inv, offset, x + z * 18, y + w * 18));
                offset++;
            }
        }

        for (int zz = 0; zz < 3; zz++) {
            for (int z = 0; z < 8; z++) {
                final int iSLot = zz * 8 + z;
                this.addSlot(new OptionalRestrictedInputSlot(RestrictedInputSlot.PlacableItemType.UPGRADES,
                        upgradeInventory, this, iSLot, 187 + zz * 18, 8 + 18 * z, iSLot, this.getPlayerInventory()));
            }
        }
        /*
         * if ( supportCapacity() ) { for (int w = 0; w < 2; w++) for (int z = 0; z < 9;
         * z++) addSlotToContainer( new OptionalSlotFakeTypeOnly( inv, this, offset++,
         * x, y, z, w, 1 ) ); for (int w = 0; w < 2; w++) for (int z = 0; z < 9; z++)
         * addSlotToContainer( new OptionalSlotFakeTypeOnly( inv, this, offset++, x, y,
         * z, w + 2, 2 ) ); for (int w = 0; w < 2; w++) for (int z = 0; z < 9; z++)
         * addSlotToContainer( new OptionalSlotFakeTypeOnly( inv, this, offset++, x, y,
         * z, w + 4, 3 ) ); }
         */
    }

    @Override
    public int availableUpgrades() {
        final ItemStack is = this.workBench.getInventoryByName("cell").getInvStack(0);
        if (this.prevStack != is) {
            this.prevStack = is;
            this.lastUpgrades = this.getCellUpgradeInventory().getSlotCount();
        }
        return this.lastUpgrades;
    }

    @Override
    public void detectAndSendChanges() {
        final ItemStack is = this.workBench.getInventoryByName("cell").getInvStack(0);
        if (Platform.isServer()) {
            for (final IContainerListener listener : this.listeners) {
                if (this.prevStack != is) {
                    // if the bars changed an item was probably made, so just send shit!
                    for (final Slot s : this.inventorySlots) {
                        if (s instanceof OptionalRestrictedInputSlot) {
                            final OptionalRestrictedInputSlot sri = (OptionalRestrictedInputSlot) s;
                            listener.sendSlotContents(this, sri.slotNumber, sri.getStack());
                        }
                    }

                    if (listener instanceof ServerPlayerEntity) {
                        ((ServerPlayerEntity) listener).isChangingQuantityOnly = false;
                    }
                }
            }

            this.setCopyMode(this.getWorkBenchCopyMode());
            this.setFuzzyMode(this.getWorkBenchFuzzyMode());
        }

        this.prevStack = is;
        this.standardDetectAndSendChanges();
    }

    @Override
    public boolean isSlotEnabled(final int idx) {
        return idx < this.availableUpgrades();
    }

    public FixedItemInv getCellUpgradeInventory() {
        final FixedItemInv upgradeInventory = this.workBench.getCellUpgradeInventory();

        return upgradeInventory == null ? EmptyFixedItemInv.INSTANCE : upgradeInventory;
    }

    @Override
    public void onUpdate(final String field, final Object oldValue, final Object newValue) {
        if (field.equals("copyMode")) {
            this.workBench.getConfigManager().putSetting(Settings.COPY_MODE, this.getCopyMode());
        }

        super.onUpdate(field, oldValue, newValue);
    }

    public void clear() {
        ItemHandlerUtil.clear(this.getUpgradeable().getInventoryByName("config"));
        this.detectAndSendChanges();
    }

    private FuzzyMode getWorkBenchFuzzyMode() {
        final ICellWorkbenchItem cwi = this.workBench.getCell();
        if (cwi != null) {
            return cwi.getFuzzyMode(this.workBench.getInventoryByName("cell").getInvStack(0));
        }
        return FuzzyMode.IGNORE_ALL;
    }

    public void partition() {

        final FixedItemInv inv = this.getUpgradeable().getInventoryByName("config");

        final ItemStack is = this.getUpgradeable().getInventoryByName("cell").getInvStack(0);
        final IStorageChannel channel = is.getItem() instanceof IStorageCell
                ? ((IStorageCell) is.getItem()).getChannel()
                : AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class);

        final IMEInventory cellInv = AEApi.instance().registries().cell().getCellInventory(is, null, channel);

        Iterator<IAEStack> i = new NullIterator<>();
        if (cellInv != null) {
            final IItemList list = cellInv.getAvailableItems(channel.createList());
            i = list.iterator();
        }

        for (int x = 0; x < inv.getSlotCount(); x++) {
            if (i.hasNext()) {
                // TODO: check if ok
                final ItemStack g = i.next().asItemStackRepresentation();
                ItemHandlerUtil.setStackInSlot(inv, x, g);
            } else {
                ItemHandlerUtil.setStackInSlot(inv, x, ItemStack.EMPTY);
            }
        }

        this.detectAndSendChanges();
    }

    public CopyMode getCopyMode() {
        return this.copyMode;
    }

    private void setCopyMode(final CopyMode copyMode) {
        this.copyMode = copyMode;
    }
}
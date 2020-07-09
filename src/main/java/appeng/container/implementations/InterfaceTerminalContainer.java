/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2014, AlgorithmX2, All rights reserved.
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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import alexiil.mc.lib.attributes.item.FixedItemInv;

import appeng.api.config.SecurityPermissions;
import appeng.api.config.Settings;
import appeng.api.config.YesNo;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.security.IActionHost;
import appeng.container.AEBaseContainer;
import appeng.container.ContainerLocator;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.CompressedNBTPacket;
import appeng.helpers.DualityInterface;
import appeng.helpers.IInterfaceHost;
import appeng.helpers.InventoryAction;
import appeng.items.misc.EncodedPatternItem;
import appeng.parts.misc.InterfacePart;
import appeng.parts.reporting.InterfaceTerminalPart;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.tile.misc.InterfaceBlockEntity;
import appeng.util.InventoryAdaptor;
import appeng.util.Platform;
import appeng.util.helpers.ItemHandlerUtil;
import appeng.util.inv.AdaptorFixedInv;
import appeng.util.inv.WrapperCursorItemHandler;
import appeng.util.inv.WrapperFilteredItemHandler;
import appeng.util.inv.filter.IAEItemFilter;

public final class InterfaceTerminalContainer extends AEBaseContainer {

    public static ScreenHandlerType<InterfaceTerminalContainer> TYPE;

    private static final ContainerHelper<InterfaceTerminalContainer, InterfaceTerminalPart> helper = new ContainerHelper<>(
            InterfaceTerminalContainer::new, InterfaceTerminalPart.class, SecurityPermissions.BUILD);

    public static InterfaceTerminalContainer fromNetwork(int windowId, PlayerInventory inv, PacketByteBuf buf) {
        return helper.fromNetwork(windowId, inv, buf);
    }

    public static boolean open(PlayerEntity player, ContainerLocator locator) {
        return helper.open(player, locator);
    }

    /**
     * this stuff is all server side..
     */

    private static long autoBase = Long.MIN_VALUE;
    private final Map<IInterfaceHost, InvTracker> diList = new HashMap<>();
    private final Map<Long, InvTracker> byId = new HashMap<>();
    private IGrid grid;
    private CompoundTag data = new CompoundTag();

    public InterfaceTerminalContainer(int id, final PlayerInventory ip, final InterfaceTerminalPart anchor) {
        super(TYPE, id, ip, anchor);

        if (Platform.isServer()) {
            this.grid = anchor.getActionableNode().getGrid();
        }

        this.bindPlayerInventory(ip, 0, 222 - /* height of player inventory */82);
    }

    @Override
    public void sendContentUpdates() {
        if (Platform.isClient()) {
            return;
        }

        super.sendContentUpdates();

        if (this.grid == null) {
            return;
        }

        int total = 0;
        boolean missing = false;

        final IActionHost host = this.getActionHost();
        if (host != null) {
            final IGridNode agn = host.getActionableNode();
            if (agn != null && agn.isActive()) {
                for (final IGridNode gn : this.grid.getMachines(InterfaceBlockEntity.class)) {
                    if (gn.isActive()) {
                        final IInterfaceHost ih = (IInterfaceHost) gn.getMachine();
                        if (ih.getInterfaceDuality().getConfigManager()
                                .getSetting(Settings.INTERFACE_TERMINAL) == YesNo.NO) {
                            continue;
                        }

                        final InvTracker t = this.diList.get(ih);

                        if (t == null) {
                            missing = true;
                        } else {
                            final DualityInterface dual = ih.getInterfaceDuality();
                            if (!t.name.equals(dual.getTermName())) {
                                missing = true;
                            }
                        }

                        total++;
                    }
                }

                for (final IGridNode gn : this.grid.getMachines(InterfacePart.class)) {
                    if (gn.isActive()) {
                        final IInterfaceHost ih = (IInterfaceHost) gn.getMachine();
                        if (ih.getInterfaceDuality().getConfigManager()
                                .getSetting(Settings.INTERFACE_TERMINAL) == YesNo.NO) {
                            continue;
                        }

                        final InvTracker t = this.diList.get(ih);

                        if (t == null) {
                            missing = true;
                        } else {
                            final DualityInterface dual = ih.getInterfaceDuality();
                            if (!t.name.equals(dual.getTermName())) {
                                missing = true;
                            }
                        }

                        total++;
                    }
                }
            }
        }

        if (total != this.diList.size() || missing) {
            this.regenList(this.data);
        } else {
            for (final Entry<IInterfaceHost, InvTracker> en : this.diList.entrySet()) {
                final InvTracker inv = en.getValue();
                for (int x = 0; x < inv.server.getSlotCount(); x++) {
                    if (this.isDifferent(inv.server.getInvStack(x), inv.client.getInvStack(x))) {
                        this.addItems(this.data, inv, x, 1);
                    }
                }
            }
        }

        if (!this.data.isEmpty()) {
            try {
                NetworkHandler.instance().sendTo(new CompressedNBTPacket(this.data),
                        (ServerPlayerEntity) this.getPlayerInv().player);
            } catch (final IOException e) {
                // :P
            }

            this.data = new CompoundTag();
        }
    }

    @Override
    public void doAction(final ServerPlayerEntity player, final InventoryAction action, final int slot, final long id) {
        final InvTracker inv = this.byId.get(id);
        if (inv != null) {
            final ItemStack is = inv.server.getInvStack(slot);
            final boolean hasItemInHand = !player.inventory.getCursorStack().isEmpty();

            final InventoryAdaptor playerHand = new AdaptorFixedInv(new WrapperCursorItemHandler(player.inventory));

            final FixedItemInv theSlot = new WrapperFilteredItemHandler(
                    inv.server.getSubInv(slot, slot + 1), new PatternSlotFilter());
            final InventoryAdaptor interfaceSlot = new AdaptorFixedInv(theSlot);

            switch (action) {
                case PICKUP_OR_SET_DOWN:

                    if (hasItemInHand) {
                        ItemStack inSlot = theSlot.getInvStack(0);
                        if (inSlot.isEmpty()) {
                            player.inventory.setCursorStack(interfaceSlot.addItems(player.inventory.getCursorStack()));
                        } else {
                            inSlot = inSlot.copy();
                            final ItemStack inHand = player.inventory.getCursorStack().copy();

                            ItemHandlerUtil.setStackInSlot(theSlot, 0, ItemStack.EMPTY);
                            player.inventory.setCursorStack(ItemStack.EMPTY);

                            player.inventory.setCursorStack(interfaceSlot.addItems(inHand.copy()));

                            if (player.inventory.getCursorStack().isEmpty()) {
                                player.inventory.setCursorStack(inSlot);
                            } else {
                                player.inventory.setCursorStack(inHand);
                                ItemHandlerUtil.setStackInSlot(theSlot, 0, inSlot);
                            }
                        }
                    } else {
                        ItemHandlerUtil.setStackInSlot(theSlot, 0, playerHand.addItems(theSlot.getInvStack(0)));
                    }

                    break;
                case SPLIT_OR_PLACE_SINGLE:

                    if (hasItemInHand) {
                        ItemStack extra = playerHand.removeItems(1, ItemStack.EMPTY, null);
                        if (!extra.isEmpty()) {
                            extra = interfaceSlot.addItems(extra);
                        }
                        if (!extra.isEmpty()) {
                            playerHand.addItems(extra);
                        }
                    } else if (!is.isEmpty()) {
                        ItemStack extra = interfaceSlot.removeItems((is.getCount() + 1) / 2, ItemStack.EMPTY, null);
                        if (!extra.isEmpty()) {
                            extra = playerHand.addItems(extra);
                        }
                        if (!extra.isEmpty()) {
                            interfaceSlot.addItems(extra);
                        }
                    }

                    break;
                case SHIFT_CLICK:

                    final InventoryAdaptor playerInv = InventoryAdaptor.getAdaptor(player);

                    ItemHandlerUtil.setStackInSlot(theSlot, 0, playerInv.addItems(theSlot.getInvStack(0)));

                    break;
                case MOVE_REGION:

                    final InventoryAdaptor playerInvAd = InventoryAdaptor.getAdaptor(player);
                    for (int x = 0; x < inv.server.getSlotCount(); x++) {
                        ItemHandlerUtil.setStackInSlot(inv.server, x,
                                playerInvAd.addItems(inv.server.getInvStack(x)));
                    }

                    break;
                case CREATIVE_DUPLICATE:

                    if (player.isCreative() && !hasItemInHand) {
                        player.inventory.setCursorStack(is.isEmpty() ? ItemStack.EMPTY : is.copy());
                    }

                    break;
                default:
                    return;
            }

            this.updateHeld(player);
        }
    }

    private void regenList(final CompoundTag data) {
        this.byId.clear();
        this.diList.clear();

        final IActionHost host = this.getActionHost();
        if (host != null) {
            final IGridNode agn = host.getActionableNode();
            if (agn != null && agn.isActive()) {
                for (final IGridNode gn : this.grid.getMachines(InterfaceBlockEntity.class)) {
                    final IInterfaceHost ih = (IInterfaceHost) gn.getMachine();
                    final DualityInterface dual = ih.getInterfaceDuality();
                    if (gn.isActive() && dual.getConfigManager().getSetting(Settings.INTERFACE_TERMINAL) == YesNo.YES) {
                        this.diList.put(ih, new InvTracker(dual, dual.getPatterns(), dual.getTermName()));
                    }
                }

                for (final IGridNode gn : this.grid.getMachines(InterfacePart.class)) {
                    final IInterfaceHost ih = (IInterfaceHost) gn.getMachine();
                    final DualityInterface dual = ih.getInterfaceDuality();
                    if (gn.isActive() && dual.getConfigManager().getSetting(Settings.INTERFACE_TERMINAL) == YesNo.YES) {
                        this.diList.put(ih, new InvTracker(dual, dual.getPatterns(), dual.getTermName()));
                    }
                }
            }
        }

        data.putBoolean("clear", true);

        for (final Entry<IInterfaceHost, InvTracker> en : this.diList.entrySet()) {
            final InvTracker inv = en.getValue();
            this.byId.put(inv.which, inv);
            this.addItems(data, inv, 0, inv.server.getSlotCount());
        }
    }

    private boolean isDifferent(final ItemStack a, final ItemStack b) {
        if (a.isEmpty() && b.isEmpty()) {
            return false;
        }

        if (a.isEmpty() || b.isEmpty()) {
            return true;
        }

        return !ItemStack.areEqual(a, b);
    }

    private void addItems(final CompoundTag data, final InvTracker inv, final int offset, final int length) {
        final String name = '=' + Long.toString(inv.which, Character.MAX_RADIX);
        final CompoundTag tag = data.getCompound(name);

        if (tag.isEmpty()) {
            tag.putLong("sortBy", inv.sortBy);
            tag.putString("un", Text.Serializer.toJson(inv.name));
        }

        for (int x = 0; x < length; x++) {
            final CompoundTag itemNBT = new CompoundTag();

            final ItemStack is = inv.server.getInvStack(x + offset);

            // "update" client side.
            ItemHandlerUtil.setStackInSlot(inv.client, x + offset, is.isEmpty() ? ItemStack.EMPTY : is.copy());

            if (!is.isEmpty()) {
                is.toTag(itemNBT);
            }

            tag.put(Integer.toString(x + offset), itemNBT);
        }

        data.put(name, tag);
    }

    private static class InvTracker {

        private final long sortBy;
        private final long which = autoBase++;
        private final Text name;
        private final FixedItemInv client;
        private final FixedItemInv server;

        public InvTracker(final DualityInterface dual, final FixedItemInv patterns, final Text name) {
            this.server = patterns;
            this.client = new AppEngInternalInventory(null, this.server.getSlotCount());
            this.name = name;
            this.sortBy = dual.getSortValue();
        }
    }

    private static class PatternSlotFilter implements IAEItemFilter {
        @Override
        public boolean allowExtract(FixedItemInv inv, int slot, int amount) {
            return true;
        }

        @Override
        public boolean allowInsert(FixedItemInv inv, int slot, ItemStack stack) {
            return !stack.isEmpty() && stack.getItem() instanceof EncodedPatternItem;
        }
    }
}
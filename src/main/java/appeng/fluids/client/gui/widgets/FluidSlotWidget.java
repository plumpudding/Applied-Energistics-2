package appeng.fluids.client.gui.widgets;

import java.math.RoundingMode;
import java.util.Collections;

import alexiil.mc.lib.attributes.Simulation;
import alexiil.mc.lib.attributes.fluid.FluidAttributes;
import alexiil.mc.lib.attributes.fluid.FluidExtractable;
import alexiil.mc.lib.attributes.fluid.amount.FluidAmount;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import alexiil.mc.lib.attributes.fluid.volume.FluidVolume;

import appeng.api.storage.data.IAEFluidStack;
import appeng.client.gui.widgets.CustomSlotWidget;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.FluidSlotPacket;
import appeng.fluids.util.AEFluidStack;
import appeng.fluids.util.IAEFluidTank;

public class FluidSlotWidget extends CustomSlotWidget {
    private final IAEFluidTank fluids;
    private final int slot;

    public FluidSlotWidget(final IAEFluidTank fluids, final int slot, final int id, final int x, final int y) {
        super(id, x, y);
        this.fluids = fluids;
        this.slot = slot;
    }

    @Override
    public void drawContent(final MinecraftClient mc, final int mouseX, final int mouseY, final float partialTicks) {
        final IAEFluidStack fs = this.getFluidStack();
        if (fs != null) {
            fs.getFluidStack().renderGuiRect(xPos(), yPos(), xPos() + getWidth(), yPos() + getHeight());
        }
    }

    @Override
    public boolean canClick(final PlayerEntity player) {
        final ItemStack mouseStack = player.inventory.getCursorStack();
        return mouseStack.isEmpty()
                || FluidAttributes.EXTRACTABLE.getFirstOrNull(mouseStack) != null;
    }

    @Override
    public void slotClicked(final ItemStack clickStack, int mouseButton) {
        if (clickStack.isEmpty() || mouseButton == 1) {
            this.setFluidStack(null);
        } else if (mouseButton == 0) {
            FluidExtractable extractable = FluidAttributes.EXTRACTABLE.getFirstOrNull(clickStack);
            if (extractable != null && extractable.couldExtractAnything()) {
                FluidVolume volume = extractable.attemptAnyExtraction(FluidAmount.MAX_VALUE, Simulation.ACTION);
                this.setFluidStack(AEFluidStack.fromFluidVolume(volume, RoundingMode.DOWN));
            }
        }
    }

    @Override
    public Text getMessage() {
        final IAEFluidStack fluid = this.getFluidStack();
        if (fluid != null) {
            return fluid.getFluidStack().getName();
        }
        return null;
    }

    @Override
    public boolean isVisible() {
        return true;
    }

    public IAEFluidStack getFluidStack() {
        return this.fluids.getFluidInSlot(this.slot);
    }

    public void setFluidStack(final IAEFluidStack stack) {
        this.fluids.setFluidInSlot(this.slot, stack);
        NetworkHandler.instance()
                .sendToServer(new FluidSlotPacket(Collections.singletonMap(this.getId(), this.getFluidStack())));
    }
}
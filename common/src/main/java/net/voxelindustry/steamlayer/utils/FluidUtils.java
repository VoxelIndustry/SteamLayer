package net.voxelindustry.steamlayer.utils;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

import javax.annotation.Nonnull;
import java.util.function.Predicate;

import static net.minecraftforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE;
import static net.minecraftforge.fluids.capability.IFluidHandler.FluidAction.SIMULATE;

public class FluidUtils
{
    public static final Predicate<FluidStack> WATER_FILTER = stack ->
            stack != null && stack.getFluid() != null && stack.getFluid().equals(Fluids.WATER);

    public static boolean drainPlayerHand(IFluidHandler fluidHandler, PlayerEntity player)
    {
        ItemStack input = player.getHeldItemMainhand();
        ItemStack output;

        LazyOptional<IFluidHandlerItem> lazyHandler = FluidUtils.getFluidHandler(input);

        if (fluidHandler == null || !lazyHandler.isPresent())
            return false;

        IFluidHandlerItem handler = lazyHandler.orElse(null);

        if (handler.getFluidInTank(0).isEmpty())
            return false;

        int simulated = fluidHandler.fill(handler.drain(Integer.MAX_VALUE, SIMULATE), SIMULATE);
        if (simulated > 0)
        {
            fluidHandler.fill(handler.drain(simulated, EXECUTE), EXECUTE);
            if ((handler.getFluidInTank(0).isEmpty() || handler.getFluidInTank(0).getAmount() == 0) && !player.isCreative())
            {
                output = handler.getContainer();
                if (input.getCount() == 1)
                    player.inventory.setInventorySlotContents(player.inventory.currentItem, output);
                else
                {
                    player.inventory.decrStackSize(player.inventory.currentItem, 1);
                    if (!player.inventory.addItemStackToInventory(output))
                        player.entityDropItem(output, 0);
                }
            }
            return true;
        }
        return false;
    }

    public static boolean fillPlayerHand(IFluidHandler fluidHandler, PlayerEntity player)
    {
        ItemStack input = player.getHeldItemMainhand();
        ItemStack output;

        LazyOptional<IFluidHandlerItem> lazyHandler = FluidUtils.getFluidHandler(input);

        if (fluidHandler != null && lazyHandler.isPresent() && !fluidHandler.getFluidInTank(0).isEmpty())
        {
            IFluidHandlerItem handler = lazyHandler.orElse(null);

            int simulated = handler.fill(fluidHandler.drain(Integer.MAX_VALUE, SIMULATE), SIMULATE);
            if (simulated > 0)
            {
                handler.fill(fluidHandler.drain(simulated, EXECUTE), EXECUTE);

                output = handler.getContainer();
                if (input.getCount() == 1)
                {
                    if (!player.isCreative())
                        player.inventory.setInventorySlotContents(player.inventory.currentItem, output);
                    else
                        player.inventory.addItemStackToInventory(output);
                }
                else
                {
                    if (!player.isCreative())
                        player.inventory.decrStackSize(player.inventory.currentItem, 1);
                    if (!player.inventory.addItemStackToInventory(output))
                        player.entityDropItem(output, 0);
                }
                return true;
            }
        }
        return false;

    }

    public static boolean drainContainers(IFluidHandler fluidHandler, IInventory inv,
                                          int inputSlot, int outputSlot)
    {
        ItemStack input = inv.getStackInSlot(inputSlot);
        ItemStack output = inv.getStackInSlot(outputSlot);

        LazyOptional<IFluidHandlerItem> lazyHandler = FluidUtils.getFluidHandler(input);

        if (!lazyHandler.isPresent())
            return false;

        IFluidHandlerItem handler = lazyHandler.orElse(null);

        if (FluidUtil.tryFluidTransfer(fluidHandler, handler,
                handler.getTankCapacity(0), false).isEmpty())
            return false;

        FluidStack drained = FluidUtil.tryFluidTransfer(fluidHandler, handler,
                handler.getTankCapacity(0), true);

        if (!drained.isEmpty() && handler.getContainer() != ItemStack.EMPTY)
        {
            if (output == ItemStack.EMPTY)
            {
                inv.setInventorySlotContents(outputSlot, handler.getContainer());
                inv.decrStackSize(inputSlot, 1);
            }
            else
            {
                if (ItemUtils.deepEquals(output, handler.getContainer()))
                {
                    inv.getStackInSlot(outputSlot).setCount(inv.getStackInSlot(outputSlot).getCount() + 1);
                    inv.decrStackSize(inputSlot, 1);
                }
                else
                {
                    FluidUtil.tryFluidTransfer(handler, fluidHandler, drained.getAmount(), true);
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean fillContainers(IFluidHandler fluidHandler, IInventory inv, int inputSlot,
                                         int outputSlot, Fluid fluidToFill)
    {
        ItemStack input = inv.getStackInSlot(inputSlot);
        ItemStack output = inv.getStackInSlot(outputSlot);

        if (input != ItemStack.EMPTY)
        {
            LazyOptional<IFluidHandlerItem> lazyHandler = FluidUtils.getFluidHandler(input);

            /*
             * The copy is needed to get the filled container without altering
             * the original ItemStack.
             */
            ItemStack containerCopy = input.copy();
            containerCopy.setCount(1);

            /*
             * It's necessary to check before any alterations that the resulting
             * ItemStack can be placed into the outputSlot.
             */
            if (lazyHandler.isPresent() && (output == ItemStack.EMPTY || output.getCount() < output.getMaxStackSize()
                    && ItemUtils.deepEquals(FluidUtils.getFilledContainer(fluidToFill, containerCopy), output)))
            {
                IFluidHandlerItem handler = lazyHandler.orElse(null);
                /*
                 * Making a simulation to check if the fluid can be transfered
                 * into the fluidhandler.
                 */
                if (!FluidUtil.tryFluidTransfer(handler, fluidHandler,
                        handler.getTankCapacity(0), false).isEmpty())
                {
                    // Changes are really applied and the fluid is transfered.
                    FluidUtil.tryFluidTransfer(handler, fluidHandler,
                            handler.getTankCapacity(0), true);

                    // The inventory is modified and stacks are merged.
                    if (output == ItemStack.EMPTY)
                        inv.setInventorySlotContents(outputSlot, handler.getContainer());
                    else
                        inv.getStackInSlot(outputSlot).setCount(inv.getStackInSlot(outputSlot).getCount() + 1);
                    inv.decrStackSize(inputSlot, 1);
                    return true;
                }
            }
        }
        return false;
    }

    @Nonnull
    public static LazyOptional<IFluidHandlerItem> getFluidHandler(ItemStack container)
    {
        ItemStack copy = container.copy();
        copy.setCount(1);
        return FluidUtil.getFluidHandler(copy);
    }

    @Nonnull
    public static ItemStack getFilledContainer(Fluid fluid, ItemStack empty)
    {
        if (fluid == null || empty == ItemStack.EMPTY)
            return ItemStack.EMPTY;

        FluidUtil.getFluidHandler(empty)
                .ifPresent(handler -> handler.fill(new FluidStack(fluid, handler.getTankCapacity(0)), EXECUTE));
        return empty;
    }

    public static float getTankPressure(IFluidTank tank)
    {
        return (float) tank.getFluidAmount() / tank.getCapacity();
    }

    public static int getFluidDifference(IFluidTank tank, float fromPressure)
    {
        return (int) Math.ceil((getTankPressure(tank) - fromPressure) * tank.getCapacity());
    }
}

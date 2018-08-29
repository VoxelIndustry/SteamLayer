package net.voxelindustry.steamlayer.utils;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.*;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Predicate;

public class FluidUtils
{
    public static final Predicate<FluidStack> WATER_FILTER = stack ->
            stack != null && stack.getFluid() != null && stack.getFluid().equals(FluidRegistry.WATER);

    public static boolean drainPlayerHand(IFluidHandler fluidHandler, EntityPlayer player)
    {
        ItemStack input = player.getHeldItemMainhand();
        ItemStack output;

        IFluidHandlerItem inputFluidHandler = FluidUtils.getFluidHandler(input);

        if (fluidHandler != null && inputFluidHandler != null
                && inputFluidHandler.getTankProperties()[0].getContents() != null)
        {

            int simulated = fluidHandler.fill(inputFluidHandler.drain(Integer.MAX_VALUE, false), false);
            if (simulated > 0)
            {
                fluidHandler.fill(inputFluidHandler.drain(simulated, true), true);
                if ((inputFluidHandler.getTankProperties()[0].getContents() == null
                        || inputFluidHandler.getTankProperties()[0].getContents().amount == 0) && !player.isCreative())
                {
                    output = inputFluidHandler.getContainer();
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
        }
        return false;
    }

    public static boolean fillPlayerHand(IFluidHandler fluidHandler, EntityPlayer player)
    {
        ItemStack input = player.getHeldItemMainhand();
        ItemStack output;

        IFluidHandlerItem inputFluidHandler = FluidUtils.getFluidHandler(input);

        if (fluidHandler != null && inputFluidHandler != null
                && fluidHandler.getTankProperties()[0].getContents() != null)
        {

            int simulated = inputFluidHandler.fill(fluidHandler.drain(Integer.MAX_VALUE, false), false);
            if (simulated > 0)
            {
                inputFluidHandler.fill(fluidHandler.drain(simulated, true), true);

                output = inputFluidHandler.getContainer();
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

        IFluidHandlerItem inputFluidHandler = FluidUtils.getFluidHandler(input);

        if (inputFluidHandler != null)
        {
            /*
             * Making a simulation to check if the fluid can be drained into the
             * fluidhandler.
             */
            if (FluidUtil.tryFluidTransfer(fluidHandler, inputFluidHandler,
                    inputFluidHandler.getTankProperties()[0].getCapacity(), false) != null)
            {
                // Changes are really applied and the fluid is drained.
                FluidStack drained = FluidUtil.tryFluidTransfer(fluidHandler, inputFluidHandler,
                        inputFluidHandler.getTankProperties()[0].getCapacity(), true);

                /*
                 * If the drained container doesn't disappear we need to update
                 * the inventory accordingly.
                 */
                if (drained != null && inputFluidHandler.getContainer() != ItemStack.EMPTY)
                    if (output == ItemStack.EMPTY)
                    {
                        inv.setInventorySlotContents(outputSlot, inputFluidHandler.getContainer());
                        inv.decrStackSize(inputSlot, 1);
                    }
                    else
                    {
                        /*
                         * When output is not EMPTY, it is needed to check if
                         * the two stacks can be merged together, there was no
                         * simple way to make that check before.
                         */
                        if (ItemUtils.deepEquals(output, inputFluidHandler.getContainer()))
                        {
                            inv.getStackInSlot(outputSlot).setCount(inv.getStackInSlot(outputSlot).getCount() + 1);
                            inv.decrStackSize(inputSlot, 1);
                        }
                        else
                        {
                            /*
                             * Due to the late check of stacks merge we need to
                             * reverse any changes made to the FluidHandlers
                             * when the merge fail.
                             */
                            FluidUtil.tryFluidTransfer(inputFluidHandler, fluidHandler, drained.amount, true);
                            return false;
                        }
                    }
                return true;
            }
        }
        return false;
    }

    public static boolean fillContainers(IFluidHandler fluidHandler, IInventory inv, int inputSlot,
                                         int outputSlot, Fluid fluidToFill)
    {
        ItemStack input = inv.getStackInSlot(inputSlot);
        ItemStack output = inv.getStackInSlot(outputSlot);

        if (input != ItemStack.EMPTY)
        {
            IFluidHandlerItem inputFluidHandler = FluidUtils.getFluidHandler(input);

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
            if (inputFluidHandler != null && (output == ItemStack.EMPTY || output.getCount() < output.getMaxStackSize()
                    && ItemUtils.deepEquals(FluidUtils.getFilledContainer(fluidToFill, containerCopy), output)))
            {
                /*
                 * Making a simulation to check if the fluid can be transfered
                 * into the fluidhandler.
                 */
                if (FluidUtil.tryFluidTransfer(inputFluidHandler, fluidHandler,
                        inputFluidHandler.getTankProperties()[0].getCapacity(), false) != null)
                {
                    // Changes are really applied and the fluid is transfered.
                    FluidUtil.tryFluidTransfer(inputFluidHandler, fluidHandler,
                            inputFluidHandler.getTankProperties()[0].getCapacity(), true);

                    // The inventory is modified and stacks are merged.
                    if (output == ItemStack.EMPTY)
                        inv.setInventorySlotContents(outputSlot, inputFluidHandler.getContainer());
                    else
                        inv.getStackInSlot(outputSlot).setCount(inv.getStackInSlot(outputSlot).getCount() + 1);
                    inv.decrStackSize(inputSlot, 1);
                    return true;
                }
            }
        }
        return false;
    }

    @Nullable
    public static IFluidHandlerItem getFluidHandler(ItemStack container)
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
        IFluidHandlerItem fluidHandler = FluidUtil.getFluidHandler(empty);
        fluidHandler.fill(new FluidStack(fluid, fluidHandler.getTankProperties()[0].getCapacity()), true);
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

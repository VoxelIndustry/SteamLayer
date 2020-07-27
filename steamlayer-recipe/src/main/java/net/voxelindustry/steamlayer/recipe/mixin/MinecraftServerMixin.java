package net.voxelindustry.steamlayer.recipe.mixin;

import net.minecraft.resource.ServerResourceManager;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

import static net.voxelindustry.steamlayer.recipe.SteamLayerRecipe.END_DATA_PACK_RELOAD;
import static net.voxelindustry.steamlayer.recipe.SteamLayerRecipe.SERVER_STARTING;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin
{
    @Shadow
    private ServerResourceManager serverResourceManager;

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;setupServer()Z"), method = "method_29741")
    private void beforeSetupServer(CallbackInfo info)
    {
        SERVER_STARTING.invoker().onServerStarting((MinecraftServer) (Object) this);
    }

    @Inject(method = "reloadResources", at = @At("TAIL"))
    private void endResourceReload(Collection<String> collection, CallbackInfoReturnable<CompletableFuture<Void>> cir)
    {
        cir.getReturnValue().handleAsync((value, throwable) ->
        {
            // Hook into fail
            END_DATA_PACK_RELOAD.invoker().endDataPackReload((MinecraftServer) (Object) this, serverResourceManager, throwable == null);
            return value;
        }, (MinecraftServer) (Object) this);
    }
}
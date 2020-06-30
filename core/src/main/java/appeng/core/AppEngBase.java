package appeng.core;

import appeng.api.features.IRegistryContainer;
import appeng.api.networking.IGridCacheRegistry;
import appeng.bootstrap.components.ITileEntityRegistrationComponent;
import appeng.client.render.effects.ParticleTypes;
import appeng.core.features.registries.cell.BasicCellHandler;
import appeng.core.features.registries.cell.BasicItemCellGuiHandler;
import appeng.core.features.registries.cell.CreativeCellHandler;
import appeng.core.stats.AdvancementTriggers;
import appeng.core.stats.AeStats;
import appeng.core.sync.BasePacket;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.network.TargetPoint;
import appeng.hooks.ToolItemHook;
import appeng.mixins.CriteriaRegisterMixin;
import appeng.recipes.handlers.*;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

public abstract class AppEngBase implements AppEng {

    protected AdvancementTriggers advancementTriggers;

    public AppEngBase() {
        if (AppEng.instance() != null) {
            throw new IllegalStateException();
        }

        AEConfig.load(FabricLoader.getInstance().getConfigDirectory());

        CreativeTab.init();
        // FIXME FABRIC new FacadeItemGroup(); // This call has a side-effect (adding it to the creative screen)

        AeStats.register();
        advancementTriggers = new AdvancementTriggers(CriteriaRegisterMixin::callRegister);

        ToolItemHook.install();

        Api.INSTANCE = new Api();
        registerTileEntities();

        registerParticleTypes();
        registerRecipeTypes();
        registerRecipeSerializers();

        setupInternalRegistries();

    }

    public static void setupInternalRegistries() {
        // TODO: Do not use the internal API
        final Api api = Api.INSTANCE;
        final IRegistryContainer registries = api.registries();

        final IGridCacheRegistry gcr = registries.gridCache();
// FIXME FABRIC        gcr.registerGridCache(ITickManager.class, TickManagerCache.class);
// FIXME FABRIC        gcr.registerGridCache(IEnergyGrid.class, EnergyGridCache.class);
// FIXME FABRIC        gcr.registerGridCache(IPathingGrid.class, PathGridCache.class);
// FIXME FABRIC        gcr.registerGridCache(IStorageGrid.class, GridStorageCache.class);
// FIXME FABRIC        gcr.registerGridCache(P2PCache.class, P2PCache.class);
// FIXME FABRIC        gcr.registerGridCache(ISpatialCache.class, SpatialPylonCache.class);
// FIXME FABRIC        gcr.registerGridCache(ISecurityGrid.class, SecurityCache.class);
// FIXME FABRIC        gcr.registerGridCache(ICraftingGrid.class, CraftingGridCache.class);

        registries.cell().addCellHandler(new BasicCellHandler());
        registries.cell().addCellHandler(new CreativeCellHandler());
        registries.cell().addCellGuiHandler(new BasicItemCellGuiHandler());
// FIXME FABRIC        registries.cell().addCellGuiHandler(new BasicFluidCellGuiHandler());

        registries.matterCannon().registerAmmoItem(api.definitions().materials().matterBall().item(), 32);
    }

    protected void registerParticleTypes() {
        Registry.register(Registry.PARTICLE_TYPE, AppEng.makeId("charged_ore_fx"), ParticleTypes.CHARGED_ORE);
        Registry.register(Registry.PARTICLE_TYPE, AppEng.makeId("crafting_fx"), ParticleTypes.CRAFTING);
        Registry.register(Registry.PARTICLE_TYPE, AppEng.makeId("energy_fx"), ParticleTypes.ENERGY);
        Registry.register(Registry.PARTICLE_TYPE, AppEng.makeId("lightning_arc_fx"), ParticleTypes.LIGHTNING_ARC);
        Registry.register(Registry.PARTICLE_TYPE, AppEng.makeId("lightning_fx"), ParticleTypes.LIGHTNING);
        Registry.register(Registry.PARTICLE_TYPE, AppEng.makeId("matter_cannon_fx"), ParticleTypes.MATTER_CANNON);
        Registry.register(Registry.PARTICLE_TYPE, AppEng.makeId("vibrant_fx"), ParticleTypes.VIBRANT);
    }

    private void registerTileEntities() {
        final ApiDefinitions definitions = Api.INSTANCE.definitions();
        definitions.getRegistry().getBootstrapComponents(ITileEntityRegistrationComponent.class)
                .forEachRemaining(ITileEntityRegistrationComponent::register);
    }

    private static <T extends Recipe<?>> RecipeType<T> registerRecipeType(String id) {
        Identifier fullId = AppEng.makeId(id);
        return Registry.register(Registry.RECIPE_TYPE, fullId, new AERecipeType<>(fullId));
    }

    private void registerRecipeTypes() {
        GrinderRecipe.TYPE = registerRecipeType("grinder");
        InscriberRecipe.TYPE = registerRecipeType("inscriber");
    }

    private void registerRecipeSerializers() {
        Registry.register(Registry.RECIPE_SERIALIZER, AppEng.makeId("quartz_knife"), QuartzKnifeRecipeSerializer.INSTANCE);
        Registry.register(Registry.RECIPE_SERIALIZER, AppEng.makeId("grinder"), GrinderRecipeSerializer.INSTANCE);
        Registry.register(Registry.RECIPE_SERIALIZER, AppEng.makeId("inscriber"), InscriberRecipeSerializer.INSTANCE);

        // FIXME FABRIC FacadeItem facadeItem = (FacadeItem) Api.INSTANCE.definitions().items().facade().item();
        // FIXME FABRIC r.registerAll(DisassembleRecipe.SERIALIZER,
        // FIXME FABRIC         FacadeRecipe.getSerializer(facadeItem));
    }

    @Override
    public void sendToAllNearExcept(final PlayerEntity p, final double x, final double y, final double z,
                                    final double dist, final World w, final BasePacket packet) {
        if (w.isClient()) {
            return;
        }

        NetworkHandler.instance().sendToAllAround(packet, new TargetPoint(
                x, y, z, dist, w
        ));
    }

}

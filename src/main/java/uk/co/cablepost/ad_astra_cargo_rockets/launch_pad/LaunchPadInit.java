package uk.co.cablepost.ad_astra_cargo_rockets.launch_pad;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import uk.co.cablepost.ad_astra_cargo_rockets.AdAstraCargoRockets;

public class LaunchPadInit {

    private RegistryObject<LaunchPadBlock> block;
    private RegistryObject<BlockEntityType<LaunchPadBlockEntity>> blockEntityType;
    private RegistryObject<MenuType<LaunchPadMenu>> menuType;

    public void register(IEventBus bus) {
        // Blocks
        DeferredRegister<Block> blocks = DeferredRegister.create(ForgeRegistries.BLOCKS, AdAstraCargoRockets.MOD_ID);
        block = blocks.register("launch_pad", () -> new LaunchPadBlock(BlockBehaviour.Properties.of()
                .strength(3.5f).requiresCorrectToolForDrops().noOcclusion()));
        blocks.register(bus);

        // Block Items (register into shared ITEMS register)
        AdAstraCargoRockets.ITEMS.register("launch_pad",
                () -> new BlockItem(block.get(), new Item.Properties()));

        // Block Entity Types
        blockEntityType = AdAstraCargoRockets.BLOCK_ENTITY_TYPES.register("launch_pad",
                () -> BlockEntityType.Builder.of(LaunchPadBlockEntity::new, block.get()).build(null));

        // Menu Types - IForgeMenuType.create handles the extra buf argument
        DeferredRegister<MenuType<?>> menus = DeferredRegister.create(ForgeRegistries.MENU_TYPES, AdAstraCargoRockets.MOD_ID);
        menuType = menus.register("launch_pad",
                () -> IForgeMenuType.create((id, inv, buf) -> new LaunchPadMenu(id, inv, null)));
        menus.register(bus);

        // Client events
        bus.addListener(this::onRegisterRenderers);
    }

    public RegistryObject<LaunchPadBlock> getBlock() { return block; }
    public RegistryObject<BlockEntityType<LaunchPadBlockEntity>> getBlockEntity() { return blockEntityType; }
    public RegistryObject<MenuType<LaunchPadMenu>> getMenuType() { return menuType; }

    private void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(blockEntityType.get(), LaunchPadBlockEntityRenderer::new);
    }

    public void clientSetup() {
        MenuScreens.register(menuType.get(), LaunchPadScreen::new);
    }
}

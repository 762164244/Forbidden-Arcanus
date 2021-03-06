package com.stal111.forbidden_arcanus.event;

import com.stal111.forbidden_arcanus.Main;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootEntry;
import net.minecraft.world.storage.loot.LootPool;
import net.minecraft.world.storage.loot.TableLootEntry;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class LootTableListener {

    @SubscribeEvent
    public static void onLootTableLoad(LootTableLoadEvent event) {
        String prefix = "minecraft:chests/";
        String name = event.getName().toString();

        if (name.startsWith(prefix)) {
            String file = name.substring(name.indexOf(prefix) + prefix.length());
            switch (file) {
                case "abandoned_mineshaft":
                case "end_city_treasure": event.getTable().addPool(getInjectPool(file)); break;
                default: break;
            }
        }
    }

    private static LootPool getInjectPool(String entryName) {
        return LootPool.builder()
                .addEntry(getInjectEntry(entryName, 1))
                .bonusRolls(0, 3)
                .name("forbidden_arcanus_inject")
                .build();
    }

    private static LootEntry.Builder getInjectEntry(String name, int weight) {
        ResourceLocation table = new ResourceLocation(Main.MOD_ID, "inject/" + name);
        return TableLootEntry.builder(table).weight(weight);
    }
}

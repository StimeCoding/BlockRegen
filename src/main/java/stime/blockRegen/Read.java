package stime.blockRegen;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Random;

class Read implements Listener {

    private final FileConfiguration config;
    private final BlockRegen plugin;
    private final Random random = new Random();

    public Read(FileConfiguration config, BlockRegen plugin){
        this.config = config;
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockHit(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (event.getAction() != Action.LEFT_CLICK_BLOCK) return;

        if (player.getGameMode().equals(GameMode.CREATIVE)) return;

        assert event.getClickedBlock() != null;
        String blockname = event.getClickedBlock().getType().getKey().getKey();
        if(!(config.getKeys(false).contains(blockname))) return;


        assert event.getClickedBlock() != null;
        if (!event.getClickedBlock().isPreferredTool(event.getPlayer().getInventory().getItemInMainHand())) event.setCancelled(true);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event){
        Player player = event.getPlayer();

        if (player.getGameMode().equals(GameMode.CREATIVE)) return;

        String blockname = event.getBlock().getType().getKey().getKey();
        if(!(config.getKeys(false).contains(blockname))) return;

        event.setCancelled(true);

        //block Regen Logic

        ConfigurationSection blockConfigData = config.getConfigurationSection(blockname);
        int time = blockConfigData.getInt("time");

        Material type = event.getBlock().getType();

        if (time > 0) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                event.getBlock().setType(type);
            }, time);

            event.getBlock().setType(Material.BEDROCK);
        }

        //block drops Logic

        if (!event.getBlock().isPreferredTool(player.getInventory().getItemInMainHand())) return;

        ConfigurationSection blockDropsConfigData = blockConfigData.getConfigurationSection("drops");

        if (blockDropsConfigData != null) {
            ArrayList<ItemStack> drops = new ArrayList<>();

            for (String itemName : blockDropsConfigData.getKeys(false)) {
                ConfigurationSection itemSection = blockDropsConfigData.getConfigurationSection(itemName);
                if (itemSection == null) continue;
                Material item = Material.matchMaterial(itemName);

                double chance = itemSection.getDouble("chance", 1.0);
                int min = itemSection.getInt("amount", itemSection.getInt("min", 1));
                int max = itemSection.getInt("amount", itemSection.getInt("max", 1));
                boolean overwrite = itemSection.getBoolean("overwrite", false);

                ConfigurationSection fortuneSection = itemSection.getConfigurationSection("fortune");
                if (fortuneSection != null &&
                        player.getInventory().getItemInMainHand().hasItemMeta() &&
                        player.getInventory().getItemInMainHand().getItemMeta().hasEnchant(Enchantment.FORTUNE)) {

                    int fortune = player.getInventory().getItemInMainHand().getItemMeta().getEnchants().get(Enchantment.FORTUNE);

                    chance += fortuneSection.getInt("chance", 0) * fortune;
                    min += fortuneSection.getInt("amount", fortuneSection.getInt("min", 0));
                    max += fortuneSection.getInt("amount", fortuneSection.getInt("max", 0));
                }

                if (random.nextDouble() > Math.min(chance, 1)) continue;
                int amount = random.nextInt(min, max + 1);

                if (overwrite) drops.clear();
                drops.add(new ItemStack(item, amount));

                if (overwrite) break;
            }
            for (ItemStack drop : drops) player.getInventory().addItem(drop);
        }
    }
}

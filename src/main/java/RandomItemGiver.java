package com.example.randomitem;

import com.google.gson.Gson;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.william278.husksync.api.BukkitHuskSyncAPI;
import net.william278.husksync.api.HuskSyncAPI;
import net.william278.husksync.event.BukkitSyncCompleteEvent;
import net.william278.husksync.user.BukkitUser;
import net.william278.husksync.user.OnlineUser;
import net.william278.husksync.user.User;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class RandomItemGiver extends JavaPlugin implements Listener {

    private static final Map<Material, Integer> ITEM_WEIGHTS = new ConcurrentHashMap<>();
    private final Random random = new Random();
    private final Map<UUID, LocalDate> lastDailyReward = new ConcurrentHashMap<>();
    private boolean isFolia = false;
    private Optional<BukkitHuskSyncAPI> huskSyncAPI = Optional.empty();
    private NamespacedKey lastRewardKey;

    @Override
    public void onEnable() {
        // Folia detection
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            isFolia = true;
            getLogger().info("Folia環境を検出しました。");
        } catch (ClassNotFoundException e) {
            isFolia = false;
        }

        // Register events
        getServer().getPluginManager().registerEvents(this, this);

        // NamespacedKeyを初期化
        lastRewardKey = new NamespacedKey(this, "last_reward_date");

        // HuskSync APIのセットアップ
        if (getServer().getPluginManager().getPlugin("HuskSync") != null) {
            huskSyncAPI = Optional.of(BukkitHuskSyncAPI.getInstance());
            getServer().getPluginManager().registerEvents(new HuskSyncListener(), this);
            getLogger().info("HuskSync APIとの連携が有効になりました。");
        } else {
            getLogger().info("HuskSyncが見つかりませんでした。ローカルデータでフォールバックします。");
        }

        // Initialize item weights
        initializeWeights();
        getLogger().info("RandomItemGiver が有効になりました！");
    }

    @Override
    public void onDisable() {
        getLogger().info("RandomItemGiver が無効になりました！");
    }

    // HuskSyncイベントを処理するための内部クラス
    private class HuskSyncListener implements Listener {
        @EventHandler(priority = EventPriority.MONITOR) // 他のプラグインの処理が終わった後に実行
        public void onSyncComplete(BukkitSyncCompleteEvent event) {
            if (event.getUser() instanceof BukkitUser) {
                BukkitUser bukkitUser = (BukkitUser) event.getUser();
                Player player = bukkitUser.getPlayer();
                getLogger().info(player.getName() + "のHuskSyncが完了しました。デイリー報酬をチェックします。");
                // 少し遅延させてからチェック
                runDelayed(() -> handlePlayerJoinReward(player), 20L);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        // HuskSyncがインストールされていない場合のみ、このイベントで処理する
        if (huskSyncAPI.isEmpty()) {
            getLogger().info(event.getPlayer().getName() + "が参加しました。デイリー報酬をチェックします（HuskSyncなし）。");
            handlePlayerJoinReward(event.getPlayer());
        }
    }

    private void handlePlayerJoinReward(Player player) {
        if (!player.isOnline()) return;

        // 初回参加時のアイテム付与
        if (!player.hasPlayedBefore() && player.getGameMode() == GameMode.SURVIVAL) {
            giveRandomSpawnItem(player);
        }

        // デイリーボーナスチェック
        if (player.getGameMode() != GameMode.SURVIVAL) {
            return;
        }

        final LocalDate today = LocalDate.now();

        // プレイヤーのPersistentDataContainerから最終報酬日を取得
        PersistentDataContainer pdc = player.getPersistentDataContainer();
        String lastRewardDateStr = pdc.get(lastRewardKey, PersistentDataType.STRING);
        
        LocalDate lastRewardDate = null;
        if (lastRewardDateStr != null) {
            try {
                lastRewardDate = LocalDate.parse(lastRewardDateStr, DateTimeFormatter.ISO_LOCAL_DATE);
            } catch (Exception e) {
                getLogger().warning("最終報酬日のパースに失敗しました: " + e.getMessage());
            }
        }

        if (lastRewardDate == null || lastRewardDate.isBefore(today)) {
            getLogger().info(player.getName() + "にデイリーボーナスを付与します。");
            performItemGive(player);
            
            // 最終報酬日を更新
            pdc.set(lastRewardKey, PersistentDataType.STRING, today.format(DateTimeFormatter.ISO_LOCAL_DATE));
            lastDailyReward.put(player.getUniqueId(), today);
            
            // HuskSyncを使用している場合は、プレイヤーの変更を通知するだけ
            // PersistentDataContainerの変更は自動的に同期される
            if (huskSyncAPI.isPresent()) {
                getLogger().info(player.getName() + "の最終報酬日を " + today + " に更新しました。");
            }
        } else {
            getLogger().info(player.getName() + "は本日すでにデイリーボーナスを受け取っています。");
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();

        runDelayed(() -> {
            if (player.getGameMode() == GameMode.SURVIVAL) {
                giveRandomSpawnItem(player);
            }
        }, 60L);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerAdvancement(PlayerAdvancementDoneEvent event) {
        if (event.getAdvancement().getKey().getKey().startsWith("recipes/")) {
            return;
        }

        Player player = event.getPlayer();
        runDelayed(() -> giveRandomSpawnItem(player), 60L);
    }

private void giveRandomSpawnItem(Player player) {
    // アイテム付与は常に performItemGive を経由させる
    performItemGive(player);
}

private void performItemGive(Player player) {
    if (player.getInventory().firstEmpty() == -1) {
        player.getWorld().dropItemNaturally(player.getLocation(), createRandomItem());
        player.sendMessage("インベントリが満杯のため、アイテムを足元にドロップしました。");
    } else {
        player.getInventory().addItem(createRandomItem());
        player.sendMessage("ランダムなアイテムを受け取りました！");
    }
}

    private ItemStack createRandomItem() {
        Material selectedMaterial = getWeightedRandomMaterial();
        ItemStack item = new ItemStack(selectedMaterial, 1);

        if (item.getType() == Material.ENCHANTED_BOOK) {
            EnchantmentStorageMeta meta = (EnchantmentStorageMeta) item.getItemMeta();
            Enchantment randomEnchant = getRandomEnchantment();
            if (randomEnchant != null) {
                meta.addStoredEnchant(randomEnchant, random.nextInt(randomEnchant.getMaxLevel()) + 1, true);
            }
            item.setItemMeta(meta);
        }

        return item;
    }

    private Material getWeightedRandomMaterial() {
        int totalWeight = ITEM_WEIGHTS.values().stream().mapToInt(Integer::intValue).sum();
        if (totalWeight <= 0) return Material.STONE; // 重みがない場合はデフォルトアイテム
        int randomValue = random.nextInt(totalWeight);
        int currentWeight = 0;

        for (Map.Entry<Material, Integer> entry : ITEM_WEIGHTS.entrySet()) {
            currentWeight += entry.getValue();
            if (randomValue < currentWeight) {
                return entry.getKey();
            }
        }
        return Material.STONE; // Fallback
    }

    private Enchantment getRandomEnchantment() {
        List<Enchantment> enchantments = new ArrayList<>();
        Registry.ENCHANTMENT.forEach(enchantments::add);
        if (enchantments.isEmpty()) {
            return null;
        }
        return enchantments.get(random.nextInt(enchantments.size()));
    }

    private void initializeWeights() {
        // アイテムの重み付け (例)
        ITEM_WEIGHTS.put(Material.DIAMOND, 10);
        ITEM_WEIGHTS.put(Material.IRON_INGOT, 100);
        ITEM_WEIGHTS.put(Material.GOLD_INGOT, 50);
        ITEM_WEIGHTS.put(Material.EMERALD, 20);
        ITEM_WEIGHTS.put(Material.NETHERITE_INGOT, 1);
        ITEM_WEIGHTS.put(Material.ENCHANTED_BOOK, 30);
        ITEM_WEIGHTS.put(Material.TOTEM_OF_UNDYING, 5);
        ITEM_WEIGHTS.put(Material.ELYTRA, 2);
    }

    private void runDelayed(Runnable task, long delayTicks) {
        if (isFolia) {
            getServer().getGlobalRegionScheduler().runDelayed(this, t -> task.run(), delayTicks);
        } else {
            getServer().getScheduler().runTaskLater(this, task, delayTicks);
        }
    }
}
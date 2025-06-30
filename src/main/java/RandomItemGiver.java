package com.example.randomitem;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;
import org.bukkit.potion.PotionData;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.Random;
import java.time.LocalDate;

public class RandomItemGiver extends JavaPlugin implements Listener {

    private boolean isFolia = false;
    private final Random random = new Random();
    private final Map<UUID, LocalDate> lastDailyReward = new ConcurrentHashMap<>();

    private static final Map<Material, Integer> ITEM_WEIGHTS = new HashMap<>();
    private static final int TOTAL_WEIGHT;

    static {
        // アイテムの重み付けリストを静的に初期化
        // ===== 食料系 =====
        ITEM_WEIGHTS.put(Material.BREAD, 12);
        ITEM_WEIGHTS.put(Material.BEETROOT, 12);
        ITEM_WEIGHTS.put(Material.DRIED_KELP, 12);
        ITEM_WEIGHTS.put(Material.APPLE, 12);
        ITEM_WEIGHTS.put(Material.CAKE, 12);
        ITEM_WEIGHTS.put(Material.GLOW_BERRIES, 12);
        ITEM_WEIGHTS.put(Material.COOKIE, 12);
        ITEM_WEIGHTS.put(Material.COOKED_BEEF, 12);
        ITEM_WEIGHTS.put(Material.HONEY_BOTTLE, 12);
        ITEM_WEIGHTS.put(Material.COOKED_PORKCHOP, 12);
        ITEM_WEIGHTS.put(Material.COOKED_MUTTON, 12);
        ITEM_WEIGHTS.put(Material.PUMPKIN_PIE, 12);
        ITEM_WEIGHTS.put(Material.SWEET_BERRIES, 12);
        ITEM_WEIGHTS.put(Material.CHORUS_FRUIT, 12);
        ITEM_WEIGHTS.put(Material.SUSPICIOUS_STEW, 12);
        ITEM_WEIGHTS.put(Material.COOKED_RABBIT, 12);
        ITEM_WEIGHTS.put(Material.RABBIT_STEW, 12);
        ITEM_WEIGHTS.put(Material.COOKED_CHICKEN, 12);
        ITEM_WEIGHTS.put(Material.COOKED_COD, 12);
        ITEM_WEIGHTS.put(Material.BAKED_POTATO, 12);
        ITEM_WEIGHTS.put(Material.CARROT, 12);
        ITEM_WEIGHTS.put(Material.COOKED_SALMON, 12);
        ITEM_WEIGHTS.put(Material.POTATO, 12);
        ITEM_WEIGHTS.put(Material.POISONOUS_POTATO, 12);
        ITEM_WEIGHTS.put(Material.PUFFERFISH, 12);
        ITEM_WEIGHTS.put(Material.MELON_SLICE, 12);
        ITEM_WEIGHTS.put(Material.GOLDEN_APPLE, 12);
        ITEM_WEIGHTS.put(Material.GOLDEN_CARROT, 12);
        ITEM_WEIGHTS.put(Material.ENCHANTED_GOLDEN_APPLE, 3);

        // ===== ツール・武器系 =====
        // 木製ツール
        ITEM_WEIGHTS.put(Material.WOODEN_SWORD, 15);
        ITEM_WEIGHTS.put(Material.WOODEN_PICKAXE, 15);
        ITEM_WEIGHTS.put(Material.WOODEN_AXE, 15);
        ITEM_WEIGHTS.put(Material.WOODEN_SHOVEL, 15);
        ITEM_WEIGHTS.put(Material.WOODEN_HOE, 15);

        // 石製ツール
        ITEM_WEIGHTS.put(Material.STONE_SWORD, 12);
        ITEM_WEIGHTS.put(Material.STONE_PICKAXE, 12);
        ITEM_WEIGHTS.put(Material.STONE_AXE, 12);
        ITEM_WEIGHTS.put(Material.STONE_SHOVEL, 12);
        ITEM_WEIGHTS.put(Material.STONE_HOE, 12);

        // 金製ツール
        ITEM_WEIGHTS.put(Material.GOLDEN_SWORD, 8);
        ITEM_WEIGHTS.put(Material.GOLDEN_PICKAXE, 8);
        ITEM_WEIGHTS.put(Material.GOLDEN_AXE, 8);
        ITEM_WEIGHTS.put(Material.GOLDEN_SHOVEL, 8);
        ITEM_WEIGHTS.put(Material.GOLDEN_HOE, 8);

        // 鉄製ツール
        ITEM_WEIGHTS.put(Material.IRON_SWORD, 6);
        ITEM_WEIGHTS.put(Material.IRON_PICKAXE, 6);
        ITEM_WEIGHTS.put(Material.IRON_AXE, 6);
        ITEM_WEIGHTS.put(Material.IRON_SHOVEL, 6);
        ITEM_WEIGHTS.put(Material.IRON_HOE, 6);

        // ダイヤモンドツール
        ITEM_WEIGHTS.put(Material.DIAMOND_SWORD, 4);
        ITEM_WEIGHTS.put(Material.DIAMOND_PICKAXE, 4);
        ITEM_WEIGHTS.put(Material.DIAMOND_AXE, 4);
        ITEM_WEIGHTS.put(Material.DIAMOND_SHOVEL, 4);
        ITEM_WEIGHTS.put(Material.DIAMOND_HOE, 4);

        // ネザーライトツール (1.16+)
        ITEM_WEIGHTS.put(Material.NETHERITE_SWORD, 2);
        ITEM_WEIGHTS.put(Material.NETHERITE_PICKAXE, 2);
        ITEM_WEIGHTS.put(Material.NETHERITE_AXE, 2);
        ITEM_WEIGHTS.put(Material.NETHERITE_SHOVEL, 2);
        ITEM_WEIGHTS.put(Material.NETHERITE_HOE, 2);

        // ===== 防具系 =====
        // 革防具
        ITEM_WEIGHTS.put(Material.LEATHER_HELMET, 15);
        ITEM_WEIGHTS.put(Material.LEATHER_CHESTPLATE, 15);
        ITEM_WEIGHTS.put(Material.LEATHER_LEGGINGS, 15);
        ITEM_WEIGHTS.put(Material.LEATHER_BOOTS, 15);
        ITEM_WEIGHTS.put(Material.LEATHER_HORSE_ARMOR, 15);

        // チェーンメイル
        ITEM_WEIGHTS.put(Material.CHAINMAIL_HELMET, 12);
        ITEM_WEIGHTS.put(Material.CHAINMAIL_CHESTPLATE, 12);
        ITEM_WEIGHTS.put(Material.CHAINMAIL_LEGGINGS, 12);
        ITEM_WEIGHTS.put(Material.CHAINMAIL_BOOTS, 12);

        // 金防具
        ITEM_WEIGHTS.put(Material.GOLDEN_HELMET, 8);
        ITEM_WEIGHTS.put(Material.GOLDEN_CHESTPLATE, 8);
        ITEM_WEIGHTS.put(Material.GOLDEN_LEGGINGS, 8);
        ITEM_WEIGHTS.put(Material.GOLDEN_BOOTS, 8);
        ITEM_WEIGHTS.put(Material.GOLDEN_HORSE_ARMOR, 8);

        // 鉄防具
        ITEM_WEIGHTS.put(Material.IRON_HELMET, 6);
        ITEM_WEIGHTS.put(Material.IRON_CHESTPLATE, 6);
        ITEM_WEIGHTS.put(Material.IRON_LEGGINGS, 6);
        ITEM_WEIGHTS.put(Material.IRON_BOOTS, 6);
        ITEM_WEIGHTS.put(Material.IRON_HORSE_ARMOR, 6);

        // ダイヤモンド防具
        ITEM_WEIGHTS.put(Material.DIAMOND_HELMET, 4);
        ITEM_WEIGHTS.put(Material.DIAMOND_CHESTPLATE, 4);
        ITEM_WEIGHTS.put(Material.DIAMOND_LEGGINGS, 4);
        ITEM_WEIGHTS.put(Material.DIAMOND_BOOTS, 4);
        ITEM_WEIGHTS.put(Material.DIAMOND_HORSE_ARMOR, 4);

        // ネザーライト防具 (1.16+)
        ITEM_WEIGHTS.put(Material.NETHERITE_HELMET, 2);
        ITEM_WEIGHTS.put(Material.NETHERITE_CHESTPLATE, 2);
        ITEM_WEIGHTS.put(Material.NETHERITE_LEGGINGS, 2);
        ITEM_WEIGHTS.put(Material.NETHERITE_BOOTS, 2);

        // ===== ユーティリティ・その他 =====
        ITEM_WEIGHTS.put(Material.COMPASS, 12);
        ITEM_WEIGHTS.put(Material.HEART_OF_THE_SEA, 12);
        ITEM_WEIGHTS.put(Material.EXPERIENCE_BOTTLE, 12);
        ITEM_WEIGHTS.put(Material.WARPED_FUNGUS_ON_A_STICK, 12);
        ITEM_WEIGHTS.put(Material.CARROT_ON_A_STICK, 12);
        ITEM_WEIGHTS.put(Material.TURTLE_HELMET, 12);
        ITEM_WEIGHTS.put(Material.CLOCK, 12);
        ITEM_WEIGHTS.put(Material.FISHING_ROD, 12);
        ITEM_WEIGHTS.put(Material.SHEARS, 12);
        ITEM_WEIGHTS.put(Material.SHIELD, 12);
        ITEM_WEIGHTS.put(Material.BOW, 12);
        ITEM_WEIGHTS.put(Material.CROSSBOW, 12);
        ITEM_WEIGHTS.put(Material.ENDER_PEARL, 12);
        ITEM_WEIGHTS.put(Material.ENDER_EYE, 12);
        ITEM_WEIGHTS.put(Material.BOOK, 12);
        ITEM_WEIGHTS.put(Material.MAP, 12);
        ITEM_WEIGHTS.put(Material.FLINT_AND_STEEL, 12);
        ITEM_WEIGHTS.put(Material.BUCKET, 12);
        ITEM_WEIGHTS.put(Material.WATER_BUCKET, 12);
        ITEM_WEIGHTS.put(Material.LAVA_BUCKET, 12);
        ITEM_WEIGHTS.put(Material.MILK_BUCKET, 12);
        ITEM_WEIGHTS.put(Material.SNOWBALL, 12);
        ITEM_WEIGHTS.put(Material.EGG, 12);
        ITEM_WEIGHTS.put(Material.FIRE_CHARGE, 12);
        ITEM_WEIGHTS.put(Material.OAK_BOAT, 12);
        ITEM_WEIGHTS.put(Material.WIND_CHARGE, 12);
        ITEM_WEIGHTS.put(Material.POTION, 12);
        ITEM_WEIGHTS.put(Material.SPLASH_POTION, 12);
        ITEM_WEIGHTS.put(Material.LINGERING_POTION, 12);
        ITEM_WEIGHTS.put(Material.ENCHANTED_BOOK, 12);
        ITEM_WEIGHTS.put(Material.GOAT_HORN, 12);
        ITEM_WEIGHTS.put(Material.NAME_TAG, 12);
        ITEM_WEIGHTS.put(Material.SADDLE, 12);
        ITEM_WEIGHTS.put(Material.TORCH, 12);
        ITEM_WEIGHTS.put(Material.LEAD, 12);
        ITEM_WEIGHTS.put(Material.FIREWORK_ROCKET, 12);

        // ===== 音楽ディスク =====
        ITEM_WEIGHTS.put(Material.MUSIC_DISC_11, 5);
        ITEM_WEIGHTS.put(Material.MUSIC_DISC_13, 5);
        ITEM_WEIGHTS.put(Material.MUSIC_DISC_CAT, 5);
        ITEM_WEIGHTS.put(Material.MUSIC_DISC_BLOCKS, 5);
        ITEM_WEIGHTS.put(Material.MUSIC_DISC_CHIRP, 5);
        ITEM_WEIGHTS.put(Material.MUSIC_DISC_FAR, 5);
        ITEM_WEIGHTS.put(Material.MUSIC_DISC_MALL, 5);
        ITEM_WEIGHTS.put(Material.MUSIC_DISC_MELLOHI, 5);
        ITEM_WEIGHTS.put(Material.MUSIC_DISC_STAL, 5);
        ITEM_WEIGHTS.put(Material.MUSIC_DISC_STRAD, 5);
        ITEM_WEIGHTS.put(Material.MUSIC_DISC_WAIT, 5);
        ITEM_WEIGHTS.put(Material.MUSIC_DISC_WARD, 5);
        ITEM_WEIGHTS.put(Material.MUSIC_DISC_CREATOR, 5);
        // 1.16+ Pigstep
        ITEM_WEIGHTS.put(Material.MUSIC_DISC_PIGSTEP, 5);
        ITEM_WEIGHTS.put(Material.MUSIC_DISC_PRECIPICE, 5);
        // 1.18+ Otherside
        ITEM_WEIGHTS.put(Material.MUSIC_DISC_OTHERSIDE, 5);
        // 1.19+ Disc 5
        ITEM_WEIGHTS.put(Material.DISC_FRAGMENT_5, 12);
        // 1.20+ Disc Relic
        ITEM_WEIGHTS.put(Material.MUSIC_DISC_RELIC, 5);

        // ===== クラフト材料・その他 =====
        ITEM_WEIGHTS.put(Material.BUNDLE, 12);
        ITEM_WEIGHTS.put(Material.DRAGON_BREATH, 12);
        ITEM_WEIGHTS.put(Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE, 1);
        ITEM_WEIGHTS.put(Material.COAST_ARMOR_TRIM_SMITHING_TEMPLATE, 1);
        ITEM_WEIGHTS.put(Material.DUNE_ARMOR_TRIM_SMITHING_TEMPLATE, 1);
        ITEM_WEIGHTS.put(Material.EYE_ARMOR_TRIM_SMITHING_TEMPLATE, 1);
        ITEM_WEIGHTS.put(Material.HOST_ARMOR_TRIM_SMITHING_TEMPLATE, 1);
        ITEM_WEIGHTS.put(Material.RAISER_ARMOR_TRIM_SMITHING_TEMPLATE, 1);
        ITEM_WEIGHTS.put(Material.RIB_ARMOR_TRIM_SMITHING_TEMPLATE, 1);
        ITEM_WEIGHTS.put(Material.SENTRY_ARMOR_TRIM_SMITHING_TEMPLATE, 1);
        ITEM_WEIGHTS.put(Material.SHAPER_ARMOR_TRIM_SMITHING_TEMPLATE, 1);
        ITEM_WEIGHTS.put(Material.SILENCE_ARMOR_TRIM_SMITHING_TEMPLATE, 1);
        ITEM_WEIGHTS.put(Material.SNOUT_ARMOR_TRIM_SMITHING_TEMPLATE, 1);
        ITEM_WEIGHTS.put(Material.SPIRE_ARMOR_TRIM_SMITHING_TEMPLATE, 1);
        ITEM_WEIGHTS.put(Material.TIDE_ARMOR_TRIM_SMITHING_TEMPLATE, 1);
        ITEM_WEIGHTS.put(Material.VEX_ARMOR_TRIM_SMITHING_TEMPLATE, 1);
        ITEM_WEIGHTS.put(Material.WARD_ARMOR_TRIM_SMITHING_TEMPLATE, 1);
        ITEM_WEIGHTS.put(Material.WAYFINDER_ARMOR_TRIM_SMITHING_TEMPLATE, 1);
        ITEM_WEIGHTS.put(Material.WILD_ARMOR_TRIM_SMITHING_TEMPLATE, 1);
        ITEM_WEIGHTS.put(Material.SKELETON_SKULL, 1);
        ITEM_WEIGHTS.put(Material.WITHER_SKELETON_SKULL, 1);
        ITEM_WEIGHTS.put(Material.ZOMBIE_HEAD, 1);
        ITEM_WEIGHTS.put(Material.CREEPER_HEAD, 1);
        ITEM_WEIGHTS.put(Material.DRAGON_HEAD, 1);
        ITEM_WEIGHTS.put(Material.PIGLIN_HEAD, 1);

        // ===== 特殊・希少 =====
        ITEM_WEIGHTS.put(Material.TOTEM_OF_UNDYING, 1);
        ITEM_WEIGHTS.put(Material.TRIDENT, 1);
        ITEM_WEIGHTS.put(Material.ELYTRA, 1);
        ITEM_WEIGHTS.put(Material.NETHER_STAR, 1);
        ITEM_WEIGHTS.put(Material.MACE, 1);

        TOTAL_WEIGHT = ITEM_WEIGHTS.values().stream().mapToInt(Integer::intValue).sum();
    }

    @Override
    public void onEnable() {
        try {
            Class.forName("io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler");
            this.isFolia = true;
            getLogger().info("Folia environment detected. Using Folia-specific schedulers.");
        } catch (ClassNotFoundException e) {
            this.isFolia = false;
            getLogger().info("Standard Paper/Spigot environment detected.");
        }

        // Listener 登録
        getServer().getPluginManager().registerEvents(this, this);

        // サーバー再起動時にリセット（＝毎日リセット） - この行は不要になります
        // dailyReceivedPlayers.clear();

        getLogger().info("RandomItemGiver が有効になりました！");
    }

    @Override
    public void onDisable() {
        getLogger().info("RandomItemGiver が無効になりました！");
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (isFolia) {
            if (!player.hasPlayedBefore()) {
                // 新規ユーザー処理
                player.getScheduler().runDelayed(this, scheduledTask -> {
                    // 今のゲームモードがサバイバルならアイテム付与
                    if (player.getGameMode() == GameMode.SURVIVAL) {
                        giveRandomSpawnItem(player);
                    }
                }, null, 1L);
            }
        }

        // 最後にボーナスを受け取った日付を取得
        LocalDate today = LocalDate.now();
        LocalDate lastReward = lastDailyReward.get(uuid);

        // まだ当日ログインボーナスを受け取っていない場合のみ付与
        if (lastReward == null || lastReward.isBefore(today)) {
            giveRandomSpawnItem(player);
            lastDailyReward.put(uuid, today);
        }
    }

    /**
     * 初回参加時にランダムアイテムを付与
     */

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        // Folia環境ではこの処理を実行しない
        if (isFolia) {
            return;
        }

        // PaperMC環境でのみ、従来通りの処理を実行
        Player player = event.getPlayer();
        player.getScheduler().runDelayed(this, scheduledTask -> {
            // 今のゲームモードがサバイバルならアイテム付与
            if (player.getGameMode() == GameMode.SURVIVAL) {
                giveRandomSpawnItem(player);
            }
        }, null, 1L);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDeath(PlayerDeathEvent event) {
        // PaperMC環境ではこの処理を実行しない
        if (!isFolia) {
            return;
        }

        // Folia環境でのみ、遅延させてリスポーンを確認する代替処理を実行
        Player player = event.getEntity();
        player.getScheduler().runDelayed(this, scheduledTask -> {
            if (player.isOnline() && !player.isDead()) {
                if (player.getGameMode() == GameMode.SURVIVAL) {
                    giveRandomSpawnItem(player);
                }
            }
        }, null, 50L); // 2.5秒後に実行
    }

    @EventHandler
    public void onPlayerAdvancement(PlayerAdvancementDoneEvent event) {
        // レシピ解放は除外
        if (event.getAdvancement().getKey().getKey().startsWith("recipes/")) {
            return;
        }

        // レシピ以外の純粋な実績解除時だけアイテムを付与
        giveRandomSpawnItem(event.getPlayer());
    }

    /**
     * リスポーン時にランダムアイテムを付与
     */

    /**
     * 重み付きランダム選択でアイテムを付与
     */
    private void giveRandomSpawnItem(Player player) {
        int randomWeight = random.nextInt(TOTAL_WEIGHT) + 1;

        Material selected = null;
        for (Map.Entry<Material, Integer> entry : ITEM_WEIGHTS.entrySet()) {
            randomWeight -= entry.getValue();
            if (randomWeight <= 0) {
                selected = entry.getKey();
                break;
            }
        }

        if (selected != null) {
            ItemStack itemStack = new ItemStack(selected, 1);
            Material type = itemStack.getType();

            // エンチャント本にランダムなエンチャントを付与
            if (type == Material.ENCHANTED_BOOK) {
                EnchantmentStorageMeta bookMeta = (EnchantmentStorageMeta) itemStack.getItemMeta();
                Enchantment enchantment = getRandomEnchantment();
                int level = random.nextInt(enchantment.getMaxLevel()) + 1;
                bookMeta.addStoredEnchant(enchantment, level, true);
                itemStack.setItemMeta(bookMeta);
            }

            // ツール・防具類は30%の確率でエンチャントを付与
            if (type.name().matches(
                    ".*(SWORD|PICKAXE|AXE|SHOVEL|HOE|HELMET|CHESTPLATE|LEGGINGS|BOOTS|BOW|CROSSBOW|TRIDENT|FISHING_ROD|SHEARS|SHIELD|MACE|ELYTRA|CARROT_ON_A_STICK|WARPED_FUNGUS_ON_A_STICK)$")) {
                if (random.nextDouble() <= 0.3) {
                    ItemMeta toolMeta = itemStack.getItemMeta();
                    Enchantment enchantment = getRandomEnchantmentForItem(type);
                    if (enchantment != null) { // ← ここでnullチェック
                        int level = random.nextInt(enchantment.getMaxLevel()) + 1;
                        toolMeta.addEnchant(enchantment, level, true);
                        itemStack.setItemMeta(toolMeta);
                    }
                    // nullならエンチャント無しでスキップされる
                }
            }

            // ポーションの場合、PotionDataを明示的に設定
            if (type == Material.POTION || type == Material.SPLASH_POTION || type == Material.LINGERING_POTION) {
                PotionMeta potionMeta = (PotionMeta) itemStack.getItemMeta();

                // ベースのポーションタイプをランダムで設定
                PotionType potionType = getRandomCraftablePotionType();
                potionMeta.setBasePotionData(new PotionData(potionType));

                itemStack.setItemMeta(potionMeta);
            }

            Map<Integer, ItemStack> overflow = player.getInventory().addItem(itemStack);

            if (overflow.isEmpty()) {
                broadcastItemReceive(player, selected);
            } else {
                player.getWorld().dropItemNaturally(player.getLocation(), itemStack);
                broadcastItemReceiveInventoryFull(player, selected);
            }
        } // ← この波括弧はメソッドgiveRandomSpawnItemの閉じ
    }

    private void broadcastItemReceive(Player player, Material item) {
        String message = "§a" + player.getName() + "がランダムアイテム「" + item.name() + "」を受け取りました！";
        for (Player onlinePlayer : getServer().getOnlinePlayers()) {
            onlinePlayer.sendMessage(message);
        }
    }

    private void broadcastItemReceiveInventoryFull(Player player, Material item) {
        String message = "§e" + player.getName() + "がランダムアイテム「" + item.name() + "」を受け取りましたが、インベントリが一杯だったため地面にドロップしました！";
        for (Player onlinePlayer : getServer().getOnlinePlayers()) {
            onlinePlayer.sendMessage(message);
        }
    }

    // このメソッドは現在使用されていないため、削除またはコメントアウトを検討
    // private boolean isCraftablePotion(PotionType type) { ... }

    // サバイバルモードで入手可能なエンチャントをランダムに選択するメソッド
    private Enchantment getRandomEnchantmentForItem(Material material) {
        List<Enchantment> validEnchantments = new ArrayList<>();
        for (Enchantment enchantment : Enchantment.values()) {
            if (!enchantment.isTreasure() && enchantment.canEnchantItem(new ItemStack(material))) {
                validEnchantments.add(enchantment);
            }
        }
        if (validEnchantments.isEmpty()) {
            return null; // エンチャント可能なものが無ければnullを返す
        }
        return validEnchantments.get(random.nextInt(validEnchantments.size()));
    }

    // サバイバルモードで入手可能な全てのエンチャントからランダムに一つ取得
    private Enchantment getRandomEnchantment() {
        List<Enchantment> validEnchantments = new ArrayList<>();
        for (Enchantment enchantment : Enchantment.values()) {
            if (!enchantment.isTreasure()) {
                validEnchantments.add(enchantment);
            }
        }
        if (validEnchantments.isEmpty()) {
            return null; // 通常は起こりませんが安全策としてnullチェックが望ましい
        }
        return validEnchantments.get(random.nextInt(validEnchantments.size()));
    }

    // ランダムでクラフト可能なPotionTypeを取得
    private PotionType getRandomCraftablePotionType() {
        PotionType[] craftableTypes = {
                PotionType.FIRE_RESISTANCE,
                PotionType.HARMING,
                PotionType.HEALING,
                PotionType.INVISIBILITY,
                PotionType.LEAPING,
                PotionType.NIGHT_VISION,
                PotionType.POISON,
                PotionType.REGENERATION,
                PotionType.SLOW_FALLING,
                PotionType.SLOWNESS,
                PotionType.STRENGTH,
                PotionType.SWIFTNESS,
                PotionType.WATER_BREATHING,
                PotionType.WEAKNESS,
                PotionType.TURTLE_MASTER
        };
        return craftableTypes[random.nextInt(craftableTypes.length)];
    }

}
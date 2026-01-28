package Universal;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.MusicInstrument;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.MusicInstrumentMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SuspiciousStewMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;

public enum BoxPool {
    INSTANCE;
    ItemStack[]enchantedBooks = new ItemStack[0];
    ItemStack[] horns = new ItemStack[0];
    ItemStack[] potions = new ItemStack[0];
    MusicInstrument[] musics = {
            MusicInstrument.ADMIRE_GOAT_HORN,
            MusicInstrument.CALL_GOAT_HORN,
            MusicInstrument.DREAM_GOAT_HORN,
            MusicInstrument.FEEL_GOAT_HORN,
            MusicInstrument.PONDER_GOAT_HORN,
            MusicInstrument.SEEK_GOAT_HORN,
            MusicInstrument.SING_GOAT_HORN,
            MusicInstrument.YEARN_GOAT_HORN,
    };
    Enchantment[] enchants = {
            Enchantment.AQUA_AFFINITY,
            Enchantment.AQUA_AFFINITY,
            Enchantment.AQUA_AFFINITY,
            Enchantment.BANE_OF_ARTHROPODS,
            Enchantment.BANE_OF_ARTHROPODS,
            Enchantment.BANE_OF_ARTHROPODS,
            Enchantment.BREACH,
            Enchantment.BREACH,
            Enchantment.BREACH,
            Enchantment.BLAST_PROTECTION,
            Enchantment.BLAST_PROTECTION,
            Enchantment.BLAST_PROTECTION,
            Enchantment.CHANNELING,
            Enchantment.CHANNELING,
            Enchantment.DENSITY,
            Enchantment.DENSITY,
            Enchantment.DENSITY,
            Enchantment.DEPTH_STRIDER,
            Enchantment.DEPTH_STRIDER,
            Enchantment.DEPTH_STRIDER,
            Enchantment.EFFICIENCY,
            Enchantment.EFFICIENCY,
            Enchantment.EFFICIENCY,
            Enchantment.FIRE_ASPECT,
            Enchantment.FIRE_ASPECT,
            Enchantment.FIRE_ASPECT,
            Enchantment.FLAME,
            Enchantment.FLAME,
            Enchantment.FORTUNE,
            Enchantment.FORTUNE,
            Enchantment.FEATHER_FALLING,
            Enchantment.FEATHER_FALLING,
            Enchantment.FEATHER_FALLING,
            Enchantment.FROST_WALKER,
            Enchantment.FROST_WALKER,
            Enchantment.FROST_WALKER,
            Enchantment.FIRE_PROTECTION,
            Enchantment.FIRE_PROTECTION,
            Enchantment.FIRE_PROTECTION,
            Enchantment.IMPALING,
            Enchantment.IMPALING,
            Enchantment.IMPALING,
            Enchantment.INFINITY,
            Enchantment.INFINITY,
            Enchantment.KNOCKBACK,
            Enchantment.KNOCKBACK,
            Enchantment.KNOCKBACK,
            Enchantment.LOOTING,
            Enchantment.LOOTING,
            Enchantment.LOYALTY,
            Enchantment.LOYALTY,
            Enchantment.LOYALTY,
            Enchantment.LUNGE,
            Enchantment.LUNGE,
            Enchantment.LUNGE,
            Enchantment.LURE,
            Enchantment.LURE,
            Enchantment.LURE,
            Enchantment.LUCK_OF_THE_SEA,
            Enchantment.LUCK_OF_THE_SEA,
            Enchantment.LUCK_OF_THE_SEA,
            Enchantment.MENDING,
            Enchantment.MULTISHOT,
            Enchantment.MULTISHOT,
            Enchantment.PIERCING,
            Enchantment.PIERCING,
            Enchantment.PIERCING,
            Enchantment.POWER,
            Enchantment.POWER,
            Enchantment.POWER,
            Enchantment.PROJECTILE_PROTECTION,
            Enchantment.PROJECTILE_PROTECTION,
            Enchantment.PROJECTILE_PROTECTION,
            Enchantment.PROTECTION,
            Enchantment.PROTECTION,
            Enchantment.PROTECTION,
            Enchantment.PUNCH,
            Enchantment.PUNCH,
            Enchantment.PUNCH,
            Enchantment.QUICK_CHARGE,
            Enchantment.QUICK_CHARGE,
            Enchantment.QUICK_CHARGE,
            Enchantment.RESPIRATION,
            Enchantment.RESPIRATION,
            Enchantment.RESPIRATION,
            Enchantment.RIPTIDE,
            Enchantment.RIPTIDE,
            Enchantment.RIPTIDE,
            Enchantment.SMITE,
            Enchantment.SMITE,
            Enchantment.SMITE,
            Enchantment.SHARPNESS,
            Enchantment.SHARPNESS,
            Enchantment.SHARPNESS,
            Enchantment.SILK_TOUCH,
            Enchantment.SILK_TOUCH,
            Enchantment.SOUL_SPEED,
            Enchantment.SWEEPING_EDGE,
            Enchantment.SWEEPING_EDGE,
            Enchantment.SWEEPING_EDGE,
            Enchantment.SWIFT_SNEAK,
            Enchantment.THORNS,
            Enchantment.UNBREAKING,
            Enchantment.UNBREAKING,
            Enchantment.UNBREAKING,
            Enchantment.WIND_BURST
    };
    PotionEffectType[] effectTypes = {
            PotionEffectType.FIRE_RESISTANCE,
            PotionEffectType.RESISTANCE,
            PotionEffectType.REGENERATION,
            PotionEffectType.NIGHT_VISION,
            PotionEffectType.JUMP_BOOST,
            PotionEffectType.SPEED,
            PotionEffectType.INSTANT_HEALTH,
            PotionEffectType.STRENGTH,
            PotionEffectType.INVISIBILITY,
            PotionEffectType.HASTE,
    };
    PotionEffectType[] splashEffectTypes = {
            PotionEffectType.WEAKNESS,
            PotionEffectType.POISON,
            PotionEffectType.HUNGER,
            PotionEffectType.INSTANT_HEALTH,
            PotionEffectType.INSTANT_DAMAGE,
            PotionEffectType.REGENERATION,
    };
    ItemStack[] foods = {
            new ItemStack(Material.COOKED_BEEF),
            new ItemStack(Material.COOKED_BEEF),
            new ItemStack(Material.COOKED_PORKCHOP),
            new ItemStack(Material.COOKED_PORKCHOP),
            new ItemStack(Material.COOKED_MUTTON),
            new ItemStack(Material.COOKED_MUTTON),
            new ItemStack(Material.COOKED_CHICKEN),
            new ItemStack(Material.COOKED_CHICKEN),
            new ItemStack(Material.COOKED_RABBIT),
            new ItemStack(Material.COOKED_RABBIT),
            new ItemStack(Material.GOLDEN_CARROT),
            new ItemStack(Material.GOLDEN_CARROT),
            new ItemStack(Material.BAKED_POTATO),
            new ItemStack(Material.BAKED_POTATO),
            new ItemStack(Material.PUMPKIN_PIE),
            new ItemStack(Material.PUMPKIN_PIE),
            new ItemStack(Material.HONEY_BOTTLE),
            new ItemStack(Material.HONEY_BOTTLE),
            new ItemStack(Material.MUSHROOM_STEW),
            new ItemStack(Material.MUSHROOM_STEW),
            new ItemStack(Material.RABBIT_STEW),
            new ItemStack(Material.RABBIT_STEW),
            new ItemStack(Material.BEETROOT_SOUP),
            new ItemStack(Material.BEETROOT_SOUP),
            goodSoup(),
    };
    ItemStack[] plants = {
            new ItemStack(Material.DANDELION),
            new ItemStack(Material.POPPY),
            new ItemStack(Material.BLUE_ORCHID),
            new ItemStack(Material.ALLIUM),
            new ItemStack(Material.AZURE_BLUET),
            new ItemStack(Material.OXEYE_DAISY),
            new ItemStack(Material.CORNFLOWER),
            new ItemStack(Material.LILY_OF_THE_VALLEY),
            new ItemStack(Material.ORANGE_TULIP),
            new ItemStack(Material.RED_TULIP),
            new ItemStack(Material.PINK_TULIP),
            new ItemStack(Material.ORANGE_TULIP),
            new ItemStack(Material.WHITE_TULIP),
            new ItemStack(Material.CLOSED_EYEBLOSSOM),
            new ItemStack(Material.OPEN_EYEBLOSSOM),
            new ItemStack(Material.SUNFLOWER),
            new ItemStack(Material.LILAC),
            new ItemStack(Material.ROSE_BUSH),
            new ItemStack(Material.PEONY),
            new ItemStack(Material.FIREFLY_BUSH),
            new ItemStack(Material.NETHER_WART),
            new ItemStack(Material.MELON),
            new ItemStack(Material.SWEET_BERRIES),
            new ItemStack(Material.PUMPKIN),
            new ItemStack(Material.COCOA_BEANS),
            new ItemStack(Material.BEETROOT_SEEDS),
            new ItemStack(Material.BROWN_MUSHROOM),
            new ItemStack(Material.RED_MUSHROOM),
            new ItemStack(Material.CARROT),
            new ItemStack(Material.POTATO),
            new ItemStack(Material.GLOW_BERRIES),
            new ItemStack(Material.SUGAR_CANE),
            new ItemStack(Material.SMALL_DRIPLEAF),
            new ItemStack(Material.BIG_DRIPLEAF),
            new ItemStack(Material.WITHER_ROSE),
            new ItemStack(Material.LILY_PAD),
    };
    ItemStack[] sea = {
            new ItemStack(Material.LILY_PAD),
            new ItemStack(Material.LILY_PAD),
            new ItemStack(Material.SEAGRASS),
            new ItemStack(Material.SEAGRASS),
            new ItemStack(Material.SEA_PICKLE),
            new ItemStack(Material.SEA_PICKLE),
            new ItemStack(Material.HORN_CORAL),
            new ItemStack(Material.HORN_CORAL),
            new ItemStack(Material.BRAIN_CORAL),
            new ItemStack(Material.BRAIN_CORAL),
            new ItemStack(Material.BUBBLE_CORAL),
            new ItemStack(Material.BUBBLE_CORAL),
            new ItemStack(Material.FIRE_CORAL),
            new ItemStack(Material.FIRE_CORAL),
            new ItemStack(Material.TUBE_CORAL),
            new ItemStack(Material.TUBE_CORAL),
            new ItemStack(Material.HORN_CORAL_FAN),
            new ItemStack(Material.HORN_CORAL_FAN),
            new ItemStack(Material.BRAIN_CORAL_FAN),
            new ItemStack(Material.BRAIN_CORAL_FAN),
            new ItemStack(Material.BUBBLE_CORAL_FAN),
            new ItemStack(Material.BUBBLE_CORAL_FAN),
            new ItemStack(Material.FIRE_CORAL_FAN),
            new ItemStack(Material.FIRE_CORAL_FAN),
            new ItemStack(Material.TUBE_CORAL_FAN),
            new ItemStack(Material.TUBE_CORAL_FAN),
            new ItemStack(Material.HORN_CORAL_BLOCK),
            new ItemStack(Material.HORN_CORAL_BLOCK),
            new ItemStack(Material.BRAIN_CORAL_BLOCK),
            new ItemStack(Material.BRAIN_CORAL_BLOCK),
            new ItemStack(Material.BUBBLE_CORAL_BLOCK),
            new ItemStack(Material.BUBBLE_CORAL_BLOCK),
            new ItemStack(Material.FIRE_CORAL_BLOCK),
            new ItemStack(Material.FIRE_CORAL_BLOCK),
            new ItemStack(Material.TUBE_CORAL_BLOCK),
            new ItemStack(Material.TUBE_CORAL_BLOCK),
            new ItemStack(Material.SPONGE),
            new ItemStack(Material.SPONGE),
            new ItemStack(Material.WET_SPONGE),
            new ItemStack(Material.WET_SPONGE),
            new ItemStack(Material.ICE),
            new ItemStack(Material.ICE),
            new ItemStack(Material.BLUE_ICE),
            new ItemStack(Material.BLUE_ICE),
            new ItemStack(Material.FROGSPAWN),
            new ItemStack(Material.TURTLE_EGG),

    };
    ItemStack[] saplings = {
            new ItemStack(Material.AZALEA),
            new ItemStack(Material.FLOWERING_AZALEA),
            new ItemStack(Material.ACACIA_SAPLING),
            new ItemStack(Material.BIRCH_SAPLING),
            new ItemStack(Material.OAK_SAPLING),
            new ItemStack(Material.JUNGLE_SAPLING),
            new ItemStack(Material.CHERRY_SAPLING),
            new ItemStack(Material.SPRUCE_SAPLING),
            new ItemStack(Material.PALE_OAK_SAPLING),
            new ItemStack(Material.BAMBOO_SAPLING),
            new ItemStack(Material.DARK_OAK_SAPLING),
            new ItemStack(Material.CRIMSON_FUNGUS),
            new ItemStack(Material.WARPED_FUNGUS),
    };
    ItemStack[] discs = {
            new ItemStack(Material.MUSIC_DISC_5),
            new ItemStack(Material.MUSIC_DISC_11),
            new ItemStack(Material.MUSIC_DISC_13),
            new ItemStack(Material.MUSIC_DISC_CAT),
            new ItemStack(Material.MUSIC_DISC_BLOCKS),
            new ItemStack(Material.MUSIC_DISC_CHIRP),
            new ItemStack(Material.MUSIC_DISC_CREATOR),
            new ItemStack(Material.MUSIC_DISC_LAVA_CHICKEN),
            new ItemStack(Material.MUSIC_DISC_CREATOR_MUSIC_BOX),
            new ItemStack(Material.MUSIC_DISC_PIGSTEP),
            new ItemStack(Material.MUSIC_DISC_PRECIPICE),
            new ItemStack(Material.MUSIC_DISC_STAL),
            new ItemStack(Material.MUSIC_DISC_STRAD),
            new ItemStack(Material.MUSIC_DISC_FAR),
            new ItemStack(Material.MUSIC_DISC_MALL),
            new ItemStack(Material.MUSIC_DISC_MELLOHI),
            new ItemStack(Material.MUSIC_DISC_OTHERSIDE),
            new ItemStack(Material.MUSIC_DISC_WAIT),
            new ItemStack(Material.MUSIC_DISC_WARD),
            new ItemStack(Material.MUSIC_DISC_TEARS),
            new ItemStack(Material.MUSIC_DISC_RELIC),
    };
    ItemStack[] patterns = {
            new ItemStack(Material.BOLT_ARMOR_TRIM_SMITHING_TEMPLATE),
            new ItemStack(Material.COAST_ARMOR_TRIM_SMITHING_TEMPLATE),
            new ItemStack(Material.DUNE_ARMOR_TRIM_SMITHING_TEMPLATE),
            new ItemStack(Material.EYE_ARMOR_TRIM_SMITHING_TEMPLATE),
            new ItemStack(Material.FLOW_ARMOR_TRIM_SMITHING_TEMPLATE),
            new ItemStack(Material.HOST_ARMOR_TRIM_SMITHING_TEMPLATE),
            new ItemStack(Material.RAISER_ARMOR_TRIM_SMITHING_TEMPLATE),
            new ItemStack(Material.RIB_ARMOR_TRIM_SMITHING_TEMPLATE),
            new ItemStack(Material.SHAPER_ARMOR_TRIM_SMITHING_TEMPLATE),
            new ItemStack(Material.SPIRE_ARMOR_TRIM_SMITHING_TEMPLATE),
            new ItemStack(Material.SILENCE_ARMOR_TRIM_SMITHING_TEMPLATE),
            new ItemStack(Material.SNOUT_ARMOR_TRIM_SMITHING_TEMPLATE),
            new ItemStack(Material.SENTRY_ARMOR_TRIM_SMITHING_TEMPLATE),
            new ItemStack(Material.TIDE_ARMOR_TRIM_SMITHING_TEMPLATE),
            new ItemStack(Material.VEX_ARMOR_TRIM_SMITHING_TEMPLATE),
            new ItemStack(Material.WARD_ARMOR_TRIM_SMITHING_TEMPLATE),
            new ItemStack(Material.WAYFINDER_ARMOR_TRIM_SMITHING_TEMPLATE),
            new ItemStack(Material.WILD_ARMOR_TRIM_SMITHING_TEMPLATE),
    };
    ItemStack[] potteries = {
            new ItemStack(Material.ANGLER_POTTERY_SHERD),
            new ItemStack(Material.ARCHER_POTTERY_SHERD),
            new ItemStack(Material.ARMS_UP_POTTERY_SHERD),
            new ItemStack(Material.BLADE_POTTERY_SHERD),
            new ItemStack(Material.BREWER_POTTERY_SHERD),
            new ItemStack(Material.BURN_POTTERY_SHERD),
            new ItemStack(Material.DANGER_POTTERY_SHERD),
            new ItemStack(Material.EXPLORER_POTTERY_SHERD),
            new ItemStack(Material.FLOW_POTTERY_SHERD),
            new ItemStack(Material.FRIEND_POTTERY_SHERD),
            new ItemStack(Material.GUSTER_POTTERY_SHERD),
            new ItemStack(Material.HEART_POTTERY_SHERD),
            new ItemStack(Material.HEARTBREAK_POTTERY_SHERD),
            new ItemStack(Material.HOWL_POTTERY_SHERD),
            new ItemStack(Material.MINER_POTTERY_SHERD),
            new ItemStack(Material.MOURNER_POTTERY_SHERD),
            new ItemStack(Material.PLENTY_POTTERY_SHERD),
            new ItemStack(Material.PRIZE_POTTERY_SHERD),
            new ItemStack(Material.SCRAPE_POTTERY_SHERD),
            new ItemStack(Material.SHEAF_POTTERY_SHERD),
            new ItemStack(Material.SHELTER_POTTERY_SHERD),
            new ItemStack(Material.SKULL_POTTERY_SHERD),
            new ItemStack(Material.SNORT_POTTERY_SHERD),
    };
    public void registerBooks(){
        List<ItemStack>books = new ArrayList<>();
        for(Enchantment e : enchants){
            ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
            EnchantmentStorageMeta meta = (EnchantmentStorageMeta) book.getItemMeta();
            meta.addStoredEnchant(e,1,false);
            meta.setMaxStackSize(1);
            book.setItemMeta(meta);
            books.add(book);
        }
        enchantedBooks = books.toArray(new ItemStack[0]);
    }
    public void registerHorns(){
        List<ItemStack>list = new ArrayList<>();
        for (MusicInstrument m : musics){
            ItemStack horn = new ItemStack(Material.GOAT_HORN);
            MusicInstrumentMeta meta = (MusicInstrumentMeta) horn.getItemMeta();
            meta.setInstrument(m);
            horn.setItemMeta(meta);
            list.add(horn);
        }
        horns = list.toArray(new ItemStack[0]);
    }
    public void registerPotions(){
        List<ItemStack>list = new ArrayList<>();
        for(PotionEffectType e : effectTypes){
            int duration = 1200;
            int amp = 0;
            if(e.isInstant()){
                duration = 0;
                amp = 1;
            }
            PotionEffect effect = new PotionEffect(e,duration,amp);
            ItemStack potion = new ItemStack(Material.POTION);
            PotionMeta meta = (PotionMeta) potion.getItemMeta();
            meta.setDisplayName(ChatColor.LIGHT_PURPLE + "秘制药水");
            meta.addCustomEffect(effect,false);
            potion.setItemMeta(meta);
            list.add(potion);
        }
        for(PotionEffectType e : splashEffectTypes){
            int duration = 600;
            if(e.isInstant()){
                duration = 0;
            }
            PotionEffect effect = new PotionEffect(e,duration,0);
            ItemStack potion = new ItemStack(Material.SPLASH_POTION);
            PotionMeta meta = (PotionMeta) potion.getItemMeta();
            meta.setDisplayName(ChatColor.LIGHT_PURPLE + "秘制药水");
            meta.addCustomEffect(effect,false);
            potion.setItemMeta(meta);
            list.add(potion);
        }
        potions = list.toArray(new ItemStack[0]);
    }
    public ItemStack[] getDiscs() {
        return discs.clone();
    }

    public ItemStack[] getEnchantedBooks() {
        return enchantedBooks.clone();
    }

    public ItemStack[] getFoods() {
        return foods.clone();
    }

    public ItemStack[] getHorns() {
        return horns.clone();
    }

    public ItemStack[] getPlants() {
        return plants.clone();
    }

    public ItemStack[] getPatterns() {
        return patterns.clone();
    }

    public ItemStack[] getPotteries() {
        return potteries.clone();
    }

    public ItemStack[] getSaplings() {
        return saplings.clone();
    }

    public ItemStack[] getPotions() {
        return potions.clone();
    }

    public ItemStack[] getSea() {
        return sea.clone();
    }

    public ItemStack goodSoup(){
        ItemStack soup = new ItemStack(Material.SUSPICIOUS_STEW);
        SuspiciousStewMeta meta = (SuspiciousStewMeta) soup.getItemMeta();
        meta.addCustomEffect(new PotionEffect(PotionEffectType.NIGHT_VISION
                ,200,0,false),false);
        meta.addCustomEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE
                ,200,0,false),false);
        meta.addCustomEffect(new PotionEffect(PotionEffectType.SATURATION
                ,10,0,false),false);
        meta.addCustomEffect(new PotionEffect(PotionEffectType.JUMP_BOOST
                ,200,0,false),false);
        meta.addCustomEffect(new PotionEffect(PotionEffectType.REGENERATION
                ,200,0,false),false);
        soup.setItemMeta(meta);
        return soup;
    }
}

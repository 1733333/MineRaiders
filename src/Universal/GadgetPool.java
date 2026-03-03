package Universal;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.components.FoodComponent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;

public enum GadgetPool {
    INSTANCE;
    public ItemStack[]gadgets = {
            snowGolem(),
            ironGolem(),
            wolfGolem(),
            zombieGolem(),
            speedNeedle(),
            healNeedle(),
            soup(),
            meat(),
            energyDrink(),
            energyDrinkPro(),
            energyDrinkProMax(),
            baitNade(),
            fragNade(),
            gasNade(),
            pyroNade(),
            smokeNade(),
            glitchNade(),
            fireCamp(),
            glowCamp(),
            explodeMine(),
            pyroMine(),
            gasMine(),
            slowMine(),
            copperBattery(),
            ironBattery(),
            goldenBattery(),
            diamondBattery(),
            netherBattery(),
    };
    public ItemStack[]recipeGadgets = new ItemStack[]{
            snowGolem(),
            ironGolem(),
            wolfGolem(),
            zombieGolem(),
            speedNeedle(),
            healNeedle(),
            soup(),
            meat(),
            baitNade(),
            fragNade(),
            gasNade(),
            pyroNade(),
            smokeNade(),
            glitchNade(),
            fireCamp(),
            glowCamp(),
            explodeMine(),
            pyroMine(),
            gasMine(),
            slowMine(),
            diamondBattery(),
            netherBattery(),
    };
    public ItemStack[] getGadgets() {
        return gadgets.clone();
    }

    public ItemStack[] getRecipeGadgets() {
        return recipeGadgets.clone();
    }

    public ItemStack snowGolem(){
        ItemStack item = Bukkit.getItemFactory().createItemStack(
                "flower_banner_pattern[consumable={animation:\"bow\",sound:\"intentionally_empty\",has_consume_particles:false,on_consume_effects:[{type:\"minecraft:play_sound\",sound:\"block.enchantment_table.use\"}]}] "
        );
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.AQUA + "霜雪图腾");
        itemMeta.setMaxStackSize(4);
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "霜雪图腾");
        lore.add(ChatColor.WHITE + "按住" + ChatColor.AQUA + "鼠标右键"
                + ChatColor.WHITE + "使用");
        lore.add(ChatColor.WHITE + "生成一个雪傀儡炮塔");
        lore.add(ChatColor.WHITE + "自动攻击附近的敌人");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
    public ItemStack ironGolem() {
        ItemStack item = Bukkit.getItemFactory().createItemStack(
                "field_masoned_banner_pattern[consumable={animation:\"bow\",sound:\"intentionally_empty\",has_consume_particles:false,on_consume_effects:[{type:\"minecraft:play_sound\",sound:\"block.enchantment_table.use\"}]}] "
        );
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.AQUA + "钢铁图腾");
        itemMeta.setMaxStackSize(4);
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "钢铁图腾");
        lore.add(ChatColor.WHITE + "按住" + ChatColor.AQUA + "鼠标右键"
                + ChatColor.WHITE + "使用");
        lore.add(ChatColor.WHITE + "生成一个铁傀儡守卫");
        lore.add(ChatColor.WHITE + "自动攻击附近的敌人");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
    public ItemStack wolfGolem(){
        ItemStack item = Bukkit.getItemFactory().createItemStack(
                "guster_banner_pattern[consumable={animation:\"bow\",sound:\"intentionally_empty\",has_consume_particles:false,on_consume_effects:[{type:\"minecraft:play_sound\",sound:\"block.enchantment_table.use\"}]}] "
        );
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.AQUA + "狩猎图腾");
        itemMeta.setMaxStackSize(4);
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "狩猎图腾");
        lore.add(ChatColor.WHITE + "按住" + ChatColor.AQUA + "鼠标右键"
                + ChatColor.WHITE + "使用");
        lore.add(ChatColor.WHITE + "生成一个冤魂");
        lore.add(ChatColor.WHITE + "自动索敌附近的玩家");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
    public ItemStack zombieGolem(){
        ItemStack item = Bukkit.getItemFactory().createItemStack(
                "skull_banner_pattern[consumable={animation:\"bow\",sound:\"intentionally_empty\",has_consume_particles:false,on_consume_effects:[{type:\"minecraft:play_sound\",sound:\"block.enchantment_table.use\"}]}] "
        );
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.AQUA + "瘟疫图腾");
        itemMeta.setMaxStackSize(4);
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "瘟疫图腾");
        lore.add(ChatColor.WHITE + "按住" + ChatColor.AQUA + "鼠标右键"
                + ChatColor.WHITE + "投掷");
        lore.add(ChatColor.WHITE + "在落点处生成2个小僵尸");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
    public ItemStack speedNeedle(){
        ItemStack item = Bukkit.getItemFactory().createItemStack(
                "stick[consumable={consume_seconds:1,animation:\"block\",sound:\"intentionally_empty\",has_consume_particles:false,on_consume_effects:[{type:\"minecraft:play_sound\",sound:\"item.trident.hit\"}]},item_model=\"cyan_candle\"]"
        );
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.AQUA + "肾上腺素");
        itemMeta.setMaxStackSize(4);
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "肾上腺素");
        lore.add(ChatColor.WHITE + "按住" + ChatColor.AQUA + "鼠标右键"
                + ChatColor.WHITE + "使用");
        lore.add(ChatColor.WHITE + "获得速度、力量和凋零效果");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
    public ItemStack healNeedle() {
        ItemStack item = Bukkit.getItemFactory().createItemStack(
                "stick[consumable={consume_seconds:1,animation:\"block\",sound:\"intentionally_empty\",has_consume_particles:false,on_consume_effects:[{type:\"minecraft:play_sound\",sound:\"item.trident.hit\"}]},item_model=\"orange_candle\"]"
        );
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.AQUA + "生命针剂");
        itemMeta.setMaxStackSize(4);
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "生命针剂");
        lore.add(ChatColor.WHITE + "按住" + ChatColor.AQUA + "鼠标右键"
                + ChatColor.WHITE + "使用");
        lore.add(ChatColor.WHITE + "获得生命回复、生命吸收、挖掘疲劳和虚弱效果");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
    public ItemStack soup(){
        ItemStack item = new ItemStack(Material.SUSPICIOUS_STEW);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.AQUA + "压缩浓汤");
        itemMeta.setMaxStackSize(4);
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "压缩浓汤");
        lore.add(ChatColor.WHITE + "食用后获得大量饱食度和饱和度");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
    public ItemStack meat(){
        ItemStack item = new ItemStack(Material.COOKED_BEEF);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.AQUA + "贪婪肉排");
        itemMeta.setMaxStackSize(4);
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "贪婪肉排");
        lore.add(ChatColor.WHITE + "食用后暂时增加生命值上限");
        lore.add(ChatColor.WHITE + "但是会清空饱食度");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
    public ItemStack energyDrink(){
        ItemStack item = new ItemStack(Material.POTION);
        ItemMeta itemMeta = item.getItemMeta();
        ((PotionMeta)itemMeta).setColor(Color.YELLOW);
        ((PotionMeta)itemMeta).addCustomEffect(
                new PotionEffect(PotionEffectType.SPEED,12000,0),false);
        itemMeta.setDisplayName(ChatColor.AQUA + "能量饮料");
        itemMeta.setMaxStackSize(16);
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "能量饮料");
        lore.add(ChatColor.WHITE + "饮用后获得速度效果");
        lore.add(ChatColor.WHITE + "跑图好帮手");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
    public ItemStack fragNade(){
        ItemStack item = new ItemStack(Material.CREEPER_SPAWN_EGG);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.AQUA + "破片手雷");
        itemMeta.setMaxStackSize(4);
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "破片手雷");
        lore.add(ChatColor.WHITE + "按" + ChatColor.AQUA + "鼠标右键"
                + ChatColor.WHITE + "投掷");
        lore.add(ChatColor.WHITE + "投掷后经过一段时间引爆并释放破片");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
    public ItemStack gasNade(){
        ItemStack item = new ItemStack(Material.SLIME_SPAWN_EGG);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.AQUA + "毒气手雷");
        itemMeta.setMaxStackSize(4);
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "毒气手雷");
        lore.add(ChatColor.WHITE + "按" + ChatColor.AQUA + "鼠标右键"
                + ChatColor.WHITE + "投掷");
        lore.add(ChatColor.WHITE + "投掷后碰到障碍物引爆");
        lore.add(ChatColor.WHITE + "释放范围持续性伤害的毒气");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
    public ItemStack pyroNade(){
        ItemStack item = new ItemStack(Material.BLAZE_SPAWN_EGG);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.AQUA + "火焰手雷");
        itemMeta.setMaxStackSize(4);
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "火焰手雷");
        lore.add(ChatColor.WHITE + "按" + ChatColor.AQUA + "鼠标右键"
                + ChatColor.WHITE + "投掷");
        lore.add(ChatColor.WHITE + "投掷后碰到障碍物引爆");
        lore.add(ChatColor.WHITE + "释放范围持续性伤害的火焰");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
    public ItemStack glitchNade(){
        ItemStack item = new ItemStack(Material.PHANTOM_SPAWN_EGG);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.AQUA + "紊乱手雷");
        itemMeta.setMaxStackSize(4);
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "紊乱手雷");
        lore.add(ChatColor.WHITE + "按" + ChatColor.AQUA + "鼠标右键"
                + ChatColor.WHITE + "投掷");
        lore.add(ChatColor.WHITE + "投掷后碰到障碍物引爆");
        lore.add(ChatColor.WHITE + "影响范围内生物的视角");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
    public ItemStack smokeNade(){
        ItemStack item = new ItemStack(Material.SKELETON_SPAWN_EGG);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.AQUA + "烟雾手雷");
        itemMeta.setMaxStackSize(4);
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "烟雾手雷");
        lore.add(ChatColor.WHITE + "按" + ChatColor.AQUA + "鼠标右键"
                + ChatColor.WHITE + "投掷");
        lore.add(ChatColor.WHITE + "投掷后碰到障碍物引爆");
        lore.add(ChatColor.WHITE + "释放干扰视线和索敌的烟雾");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
    public ItemStack baitNade(){
        ItemStack item = new ItemStack(Material.ALLAY_SPAWN_EGG);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.AQUA + "诱饵手雷");
        itemMeta.setMaxStackSize(4);
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "诱饵手雷");
        lore.add(ChatColor.WHITE + "按" + ChatColor.AQUA + "鼠标右键"
                + ChatColor.WHITE + "投掷");
        lore.add(ChatColor.WHITE + "投掷后碰到障碍物引爆");
        lore.add(ChatColor.WHITE + "生成吸引怪物仇恨的诱饵");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
    public ItemStack glowCamp(){
        ItemStack item = new ItemStack(Material.SOUL_CAMPFIRE);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.AQUA + "察觉之锣");
        itemMeta.setMaxStackSize(4);
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "察觉之锣");
        lore.add(ChatColor.WHITE + "按" + ChatColor.AQUA + "鼠标右键"
                + ChatColor.WHITE + "使用");
        lore.add(ChatColor.WHITE + "发出巨大的锣声");
        lore.add(ChatColor.WHITE + "范围内的所有实体获得发光、失明、缓慢效果");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
    public ItemStack fireCamp(){
        ItemStack item = new ItemStack(Material.CAMPFIRE);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.AQUA + "狂欢之锣");
        itemMeta.setMaxStackSize(4);
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "狂欢之锣");
        lore.add(ChatColor.WHITE + "按" + ChatColor.AQUA + "鼠标右键"
                + ChatColor.WHITE + "使用");
        lore.add(ChatColor.WHITE + "发出巨大的锣声");
        lore.add(ChatColor.WHITE + "清除范围内所有实体的负面效果");
        lore.add(ChatColor.WHITE + "范围内的所有实体获得速度效果");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
    public ItemStack slowMine(){
        ItemStack item = new ItemStack(Material.WITHER_SKELETON_SKULL);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.AQUA + "电击地雷");
        itemMeta.setMaxStackSize(4);
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "电击地雷");
        lore.add(ChatColor.WHITE + "按" + ChatColor.AQUA + "鼠标右键"
                + ChatColor.WHITE + "部署");
        lore.add(ChatColor.WHITE + "实体进入范围内触发");
        lore.add(ChatColor.WHITE + "触发后跳至半空，并对范围内的实体施加电击效果");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
    public ItemStack gasMine(){
        ItemStack item = new ItemStack(Material.ZOMBIE_HEAD);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.AQUA + "毒气地雷");
        itemMeta.setMaxStackSize(4);
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "毒气地雷");
        lore.add(ChatColor.WHITE + "按" + ChatColor.AQUA + "鼠标右键"
                + ChatColor.WHITE + "部署");
        lore.add(ChatColor.WHITE + "实体进入范围内触发");
        lore.add(ChatColor.WHITE + "触发后跳至半空并释放毒气");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
    public ItemStack explodeMine(){
        ItemStack item = new ItemStack(Material.CREEPER_HEAD);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.AQUA + "爆炸地雷");
        itemMeta.setMaxStackSize(4);
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "爆炸地雷");
        lore.add(ChatColor.WHITE + "按" + ChatColor.AQUA + "鼠标右键"
                + ChatColor.WHITE + "部署");
        lore.add(ChatColor.WHITE + "实体进入范围内触发");
        lore.add(ChatColor.WHITE + "触发后跳至半空，之后爆炸");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
    public ItemStack pyroMine(){
        ItemStack item = new ItemStack(Material.PIGLIN_HEAD);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.AQUA + "火焰地雷");
        itemMeta.setMaxStackSize(4);
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "火焰地雷");
        lore.add(ChatColor.WHITE + "按" + ChatColor.AQUA + "鼠标右键"
                + ChatColor.WHITE + "部署");
        lore.add(ChatColor.WHITE + "实体进入范围内触发");
        lore.add(ChatColor.WHITE + "触发后跳至半空，之后释放火焰");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
    public ItemStack energyDrinkPro(){
        ItemStack item = new ItemStack(Material.POTION);
        ItemMeta itemMeta = item.getItemMeta();
        ((PotionMeta)itemMeta).setColor(Color.YELLOW.mixColors(Color.ORANGE));
        ((PotionMeta)itemMeta).addCustomEffect(
                new PotionEffect(PotionEffectType.SPEED,9600,0),false);
        ((PotionMeta)itemMeta).addCustomEffect(
                new PotionEffect(PotionEffectType.HASTE,9600,0),false);
        itemMeta.setDisplayName(ChatColor.YELLOW + "能量饮料-Pro");
        itemMeta.setMaxStackSize(16);
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "能量饮料-Pro");
        lore.add(ChatColor.WHITE + "饮用后获得速度、急迫效果");
        lore.add(ChatColor.WHITE + "刮地皮好帮手");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
    public ItemStack energyDrinkProMax(){
        ItemStack item = new ItemStack(Material.POTION);
        ItemMeta itemMeta = item.getItemMeta();
        ((PotionMeta)itemMeta).setColor(Color.ORANGE);
        ((PotionMeta)itemMeta).addCustomEffect(
                new PotionEffect(PotionEffectType.SPEED,7200,0),false);
        ((PotionMeta)itemMeta).addCustomEffect(
                new PotionEffect(PotionEffectType.HASTE,7200,0),false);
        ((PotionMeta)itemMeta).addCustomEffect(
                new PotionEffect(PotionEffectType.LUCK,7200,0),false);
        itemMeta.setDisplayName(ChatColor.GOLD + "能量饮料-ProMax");
        itemMeta.setMaxStackSize(16);
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "能量饮料-ProMax");
        lore.add(ChatColor.WHITE + "饮用后获得速度、急迫、幸运效果");
        lore.add(ChatColor.WHITE + "刮地皮最好的搭档");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
    public ItemStack copperBattery(){
        ItemStack item = Bukkit.getItemFactory().createItemStack(
                "copper_nautilus_armor[consumable={consume_seconds:1,animation:\"bow\",sound:\"intentionally_empty\",has_consume_particles:false,on_consume_effects:[{type:\"minecraft:play_sound\",sound:\"block.enchantment_table.use\"}]}] "
        );
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.GRAY + "铜质电池");
        itemMeta.setMaxStackSize(8);
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "铜质电池");
        lore.add(ChatColor.WHITE + "按住" + ChatColor.AQUA + "鼠标右键"
                + ChatColor.WHITE + "使用");
        lore.add(ChatColor.WHITE + "使用后缓慢回复少量的护盾");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
    public ItemStack ironBattery(){
        ItemStack item = Bukkit.getItemFactory().createItemStack(
                "iron_nautilus_armor[consumable={consume_seconds:1.5,animation:\"bow\",sound:\"intentionally_empty\",has_consume_particles:false,on_consume_effects:[{type:\"minecraft:play_sound\",sound:\"block.enchantment_table.use\"}]}] "
        );
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.GREEN + "铁质电池");
        itemMeta.setMaxStackSize(8);
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "铁质电池");
        lore.add(ChatColor.WHITE + "按住" + ChatColor.AQUA + "鼠标右键"
                + ChatColor.WHITE + "使用");
        lore.add(ChatColor.WHITE + "使用后快速回复一定量的护盾");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
    public ItemStack goldenBattery(){
        ItemStack item = Bukkit.getItemFactory().createItemStack(
                "golden_nautilus_armor[consumable={consume_seconds:2,animation:\"bow\",sound:\"intentionally_empty\",has_consume_particles:false,on_consume_effects:[{type:\"minecraft:play_sound\",sound:\"block.enchantment_table.use\"}]}] "
        );
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.AQUA + "黄金电池");
        itemMeta.setMaxStackSize(8);
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "黄金电池");
        lore.add(ChatColor.WHITE + "按住" + ChatColor.AQUA + "鼠标右键"
                + ChatColor.WHITE + "使用");
        lore.add(ChatColor.WHITE + "使用后稍慢回复多量的护盾");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
    public ItemStack diamondBattery(){
        ItemStack item = Bukkit.getItemFactory().createItemStack(
                "diamond_nautilus_armor[consumable={consume_seconds:2,animation:\"bow\",sound:\"intentionally_empty\",has_consume_particles:false,on_consume_effects:[{type:\"minecraft:play_sound\",sound:\"block.enchantment_table.use\"}]}] "
        );
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.LIGHT_PURPLE + "钻石电池");
        itemMeta.setMaxStackSize(8);
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "钻石电池");
        lore.add(ChatColor.WHITE + "按住" + ChatColor.AQUA + "鼠标右键"
                + ChatColor.WHITE + "使用");
        lore.add(ChatColor.WHITE + "使用后快速回复大量的护盾");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
    public ItemStack netherBattery(){
        ItemStack item = Bukkit.getItemFactory().createItemStack(
                "netherite_nautilus_armor[consumable={consume_seconds:3,animation:\"bow\",sound:\"intentionally_empty\",has_consume_particles:false,on_consume_effects:[{type:\"minecraft:play_sound\",sound:\"block.enchantment_table.use\"}]}] "
        );
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.RED + "下界电池");
        itemMeta.setMaxStackSize(8);
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "下界电池");
        lore.add(ChatColor.WHITE + "按住" + ChatColor.AQUA + "鼠标右键"
                + ChatColor.WHITE + "使用");
        lore.add(ChatColor.WHITE + "使用后立刻补满护盾");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
}

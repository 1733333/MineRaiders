package Universal;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
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
            baitNade(),
            fragNade(),
            gasNade(),
            pyroNade(),
            smokeNade(),
            glitchNade(),
            fireCamp(),
            glowCamp(),
            explodeMine(),
            fragMine(),
            gasMine(),
            slowMine(),
    };

    public ItemStack[] getGadgets() {
        return gadgets.clone();
    }
    public ItemStack snowGolem(){
        ItemStack item = new ItemStack(Material.FLOWER_BANNER_PATTERN);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.AQUA + "霜雪图腾");
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "霜雪图腾");
        lore.add(ChatColor.WHITE + "按" + ChatColor.AQUA + "鼠标右键"
                + ChatColor.WHITE + "使用");
        lore.add(ChatColor.WHITE + "生成一个雪傀儡炮塔");
        lore.add(ChatColor.WHITE + "自动攻击附近的敌人");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
    public ItemStack ironGolem(){
        ItemStack item = new ItemStack(Material.FIELD_MASONED_BANNER_PATTERN);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.AQUA + "钢铁图腾");
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "钢铁图腾");
        lore.add(ChatColor.WHITE + "按" + ChatColor.AQUA + "鼠标右键"
                + ChatColor.WHITE + "使用");
        lore.add(ChatColor.WHITE + "生成一个铁傀儡守卫");
        lore.add(ChatColor.WHITE + "自动攻击附近的敌人");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
    public ItemStack wolfGolem(){
        ItemStack item = new ItemStack(Material.PIGLIN_BANNER_PATTERN);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.AQUA + "狩猎图腾");
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "狩猎图腾");
        lore.add(ChatColor.WHITE + "按" + ChatColor.AQUA + "鼠标右键"
                + ChatColor.WHITE + "使用");
        lore.add(ChatColor.WHITE + "生成一匹狼");
        lore.add(ChatColor.WHITE + "自动索敌附近的玩家");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
    public ItemStack zombieGolem(){
        ItemStack item = new ItemStack(Material.SKULL_BANNER_PATTERN);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.AQUA + "瘟疫图腾");
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "瘟疫图腾");
        lore.add(ChatColor.WHITE + "按" + ChatColor.AQUA + "鼠标右键"
                + ChatColor.WHITE + "投掷");
        lore.add(ChatColor.WHITE + "在落点处生成2个小僵尸");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
    public ItemStack speedNeedle(){
        ItemStack item = new ItemStack(Material.WARPED_FUNGUS_ON_A_STICK);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.AQUA + "肾上腺素");
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "肾上腺素");
        lore.add(ChatColor.WHITE + "按" + ChatColor.AQUA + "鼠标右键"
                + ChatColor.WHITE + "使用");
        lore.add(ChatColor.WHITE + "获得速度、力量和凋零效果");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
    public ItemStack healNeedle(){
        ItemStack item = new ItemStack(Material.CARROT_ON_A_STICK);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.AQUA + "生命针剂");
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "生命针剂");
        lore.add(ChatColor.WHITE + "按" + ChatColor.AQUA + "鼠标右键"
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
        ((PotionMeta)itemMeta).setColor(Color.ORANGE);
        ((PotionMeta)itemMeta).addCustomEffect(
                new PotionEffect(PotionEffectType.SPEED,12000,0),false);
        itemMeta.setDisplayName(ChatColor.AQUA + "能量饮料");
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
    public ItemStack fragMine(){
        ItemStack item = new ItemStack(Material.SKELETON_SKULL);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.AQUA + "破片地雷");
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "破片地雷");
        lore.add(ChatColor.WHITE + "按" + ChatColor.AQUA + "鼠标右键"
                + ChatColor.WHITE + "部署");
        lore.add(ChatColor.WHITE + "实体进入范围内触发");
        lore.add(ChatColor.WHITE + "触发后跳至半空，之后向触发方向飞散破片");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
}

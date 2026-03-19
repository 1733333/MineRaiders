package Universal;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;

public enum DropPool {
    INSTANCE;
    ItemStack[]allDrops = new ItemStack[]{
            rottenFlesh(),
            bone(),
            gunpowder(),
            string(),
            spiderEye(),
            flint(),
            quartz(),
            snitchScanner(),
            popCore(),
            fireballCore(),
            tickEye(),
            shredderCore(),
            blazeRod(),
            breezeRod(),
            bastionCore(),
            leaperUnit(),
            dukeCore(),
    };

    public ItemStack[] getAllDrops() {
        return allDrops.clone();
    }

    public ItemStack rottenFlesh() {
        ItemStack i = new ItemStack(Material.ROTTEN_FLESH);
        ItemMeta meta = i.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        meta.setMaxStackSize(16);
        meta.setDisplayName(ChatColor.GRAY + "【普通】腐肉");
        lore.add(ChatColor.WHITE + "已经腐烂的肉，散发着古怪的味道");
        meta.setLore(lore);
        i.setItemMeta(meta);
        return i.clone();
    }
    public ItemStack bone() {
        ItemStack i = new ItemStack(Material.BONE);
        ItemMeta meta = i.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        meta.setMaxStackSize(16);
        meta.setDisplayName(ChatColor.GRAY + "【普通】骨头");
        lore.add(ChatColor.WHITE + "你知道吗，人体内的骨头含量");
        lore.add(ChatColor.WHITE + "刚好可以拼成一副完整的骨架");
        meta.setLore(lore);
        i.setItemMeta(meta);
        return i.clone();
    }
    public ItemStack gunpowder() {
        ItemStack i = new ItemStack(Material.GUNPOWDER);
        ItemMeta meta = i.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        meta.setMaxStackSize(16);
        meta.setDisplayName(ChatColor.GRAY + "【普通】火药");
        lore.add(ChatColor.WHITE + "易燃又易爆");
        meta.setLore(lore);
        i.setItemMeta(meta);
        return i.clone();
    }
    public ItemStack string() {
        ItemStack i = new ItemStack(Material.STRING);
        ItemMeta meta = i.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        meta.setMaxStackSize(16);
        meta.setDisplayName(ChatColor.GRAY + "【普通】线");
        lore.add(ChatColor.WHITE + "还活着的线");
        meta.setLore(lore);
        i.setItemMeta(meta);
        return i.clone();
    }
    public ItemStack spiderEye() {
        ItemStack i = new ItemStack(Material.SPIDER_EYE);
        ItemMeta meta = i.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        meta.setMaxStackSize(16);
        meta.setDisplayName(ChatColor.GRAY + "【普通】蜘蛛眼");
        lore.add(ChatColor.WHITE + "不建议食用，能不能吃点正常的食物");
        meta.setLore(lore);
        i.setItemMeta(meta);
        return i.clone();
    }
    public ItemStack flint() {
        ItemStack i = new ItemStack(Material.FLINT);
        ItemMeta meta = i.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        meta.setMaxStackSize(16);
        meta.setDisplayName(ChatColor.GRAY + "【普通】燧石");
        lore.add(ChatColor.WHITE + "硬度不是那么高的石头");
        lore.add(ChatColor.WHITE + "和硬物碰撞可以擦出火花");
        meta.setLore(lore);
        i.setItemMeta(meta);
        return i.clone();
    }
    public ItemStack quartz() {
        ItemStack i = new ItemStack(Material.QUARTZ);
        ItemMeta meta = i.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        meta.setMaxStackSize(16);
        meta.setDisplayName(ChatColor.GRAY + "【普通】下界石英");
        lore.add(ChatColor.WHITE + "拥有特殊性质的矿物");
        lore.add(ChatColor.WHITE + "通常用在建材或者装饰上");
        meta.setLore(lore);
        i.setItemMeta(meta);
        return i.clone();
    }
    public ItemStack snitchScanner() {
        ItemStack i = new ItemStack(Material.SPYGLASS);
        ItemMeta meta = i.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        meta.setMaxStackSize(8);
        meta.setDisplayName(ChatColor.GREEN + "【寻常】告密者扫描仪");
        lore.add(ChatColor.WHITE + "居然能当望远镜用");
        meta.setLore(lore);
        i.setItemMeta(meta);
        return i.clone();
    }
    public ItemStack tickEye() {
        ItemStack i = new ItemStack(Material.FERMENTED_SPIDER_EYE);
        ItemMeta meta = i.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        meta.setMaxStackSize(8);
        meta.setDisplayName(ChatColor.GREEN + "【寻常】跳蚤之眼");
        lore.add(ChatColor.WHITE + "居然还能炼药");
        meta.setLore(lore);
        i.setItemMeta(meta);
        return i.clone();
    }
    public ItemStack fireballCore() {
        ItemStack i = new ItemStack(Material.FIRE_CHARGE);
        ItemMeta meta = i.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        meta.setMaxStackSize(8);
        meta.setDisplayName(ChatColor.GREEN + "【寻常】火球燃烧炉");
        lore.add(ChatColor.WHITE + "火球燃烧炉");
        lore.add(ChatColor.WHITE + "可以投掷");
        lore.add(ChatColor.WHITE + "在落点生成一片火焰");
        meta.setLore(lore);
        i.setItemMeta(meta);
        return i.clone();
    }
    public ItemStack popCore() {
        ItemStack i = new ItemStack(Material.MAGMA_CREAM);
        ItemMeta meta = i.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        meta.setMaxStackSize(8);
        meta.setDisplayName(ChatColor.GREEN + "【寻常】爆爆燃料舱");
        lore.add(ChatColor.WHITE + "导致爆爆原地爆炸的装置");
        meta.setLore(lore);
        i.setItemMeta(meta);
        return i.clone();
    }
    public ItemStack shredderCore() {
        ItemStack i = new ItemStack(Material.WITHER_SKELETON_SKULL);
        ItemMeta meta = i.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        meta.setMaxStackSize(4);
        meta.setDisplayName(ChatColor.AQUA + "【稀有】粉碎者陀螺仪");
        lore.add(ChatColor.WHITE + "粉碎者的平衡装置");
        meta.setLore(lore);
        i.setItemMeta(meta);
        return i.clone();
    }
    public ItemStack blazeRod() {
        ItemStack i = new ItemStack(Material.BLAZE_ROD);
        ItemMeta meta = i.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        meta.setMaxStackSize(4);
        meta.setDisplayName(ChatColor.AQUA + "【稀有】烈焰棒");
        lore.add(ChatColor.WHITE + "腐竹黑市经常会售卖的烧火棍");
        meta.setLore(lore);
        i.setItemMeta(meta);
        return i.clone();
    }
    public ItemStack breezeRod() {
        ItemStack i = new ItemStack(Material.BREEZE_ROD);
        ItemMeta meta = i.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        meta.setMaxStackSize(4);
        meta.setDisplayName(ChatColor.AQUA + "【稀有】旋风棒");
        lore.add(ChatColor.WHITE + "轻便但有韧性");
        lore.add(ChatColor.WHITE + "用来合成武器道具应该不错");
        meta.setLore(lore);
        i.setItemMeta(meta);
        return i.clone();
    }
    public ItemStack bastionCore() {
        ItemStack i = new ItemStack(Material.CREAKING_HEART);
        ItemMeta meta = i.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        meta.setMaxStackSize(2);
        meta.setDisplayName(ChatColor.LIGHT_PURPLE + "【罕见】堡垒电池");
        lore.add(ChatColor.WHITE + "堡垒巨大身躯的动力来源");
        meta.setLore(lore);
        i.setItemMeta(meta);
        return i.clone();
    }
    public ItemStack leaperUnit() {
        ItemStack i = new ItemStack(Material.FIREWORK_STAR);
        ItemMeta meta = i.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        meta.setMaxStackSize(2);
        meta.setDisplayName(ChatColor.LIGHT_PURPLE + "【罕见】跳跃者脉冲单元");
        lore.add(ChatColor.WHITE + "跳跃者脉冲单元");
        lore.add(ChatColor.WHITE + "可以投掷");
        lore.add(ChatColor.WHITE + "在落点制造一个持续吸引周围实体的奇点");
        meta.setLore(lore);
        i.setItemMeta(meta);
        return i.clone();
    }
    public ItemStack dukeCore() {
        ItemStack i = new ItemStack(Material.TNT_MINECART);
        ItemMeta meta = i.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        meta.setMaxStackSize(1);
        meta.setDisplayName(ChatColor.GOLD + "【珍奇】公爵反应堆");
        lore.add(ChatColor.WHITE + "不稳定的公爵核心，蕴含巨大能量");
        lore.add(ChatColor.WHITE + "稍微碰一下都有可能直接炸开");
        meta.setLore(lore);
        i.setItemMeta(meta);
        return i.clone();
    }
}

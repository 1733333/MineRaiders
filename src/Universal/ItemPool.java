package Universal;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.damage.DamageType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import java.awt.*;
import java.util.ArrayList;
import java.util.UUID;

public enum ItemPool {
    INSTANCE;
    ItemStack[] weapons = {
            boneStick(),
            glassSword(),
            warHammer(),
            flameBow(),
            bambooSpear(),
            crystalSword(),
            echoAxe(),
            cactusSword(),
            flintSword(),
            quartzSword(),
            netherSword(),
            goldenCarrot(),
            echoSword(),
            fireSword(),
            broom(),
            emeraldWand(),
            windBow(),
            masterStick(),
            seaHammer(),
            echoBow()
    };
    ItemStack[] armors = {

    };
    ItemStack[] gadgets = {

    };

    public ItemStack[] getWeapons() {
        return weapons.clone();
    }

    public ItemStack[] getArmors() {
        return armors.clone();
    }

    public ItemStack[] getGadgets() {
        return gadgets.clone();
    }

    public ItemStack boneStick(){
        ItemStack item = new ItemStack(Material.IRON_AXE);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.RED + "大骨棒");
        Attribute attribute1 = Attribute.ATTACK_SPEED;
        Attribute attribute2 = Attribute.ATTACK_DAMAGE;
        AttributeModifier modifier1 = new AttributeModifier(attribute1.getKeyOrThrow(),-3.25, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.HAND);
        AttributeModifier modifier2 = new AttributeModifier(attribute2.getKeyOrThrow(),5, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.HAND);
        itemMeta.addAttributeModifier(attribute1,modifier1);
        itemMeta.addAttributeModifier(attribute2,modifier2);
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "大骨棒");
        lore.add(ChatColor.WHITE + "上面绑了许多骨头");
        lore.add(ChatColor.WHITE + "可以砸晕敌人");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
    public ItemStack glassSword(){
        ItemStack item = new ItemStack(Material.IRON_SWORD);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.RED + "玻璃剑");
        ((Damageable)itemMeta).setMaxDamage(1);
        itemMeta.addEnchant(Enchantment.UNBREAKING,2,true);
        Attribute attribute1 = Attribute.ATTACK_SPEED;
        Attribute attribute2 = Attribute.ATTACK_DAMAGE;
        AttributeModifier modifier1 = new AttributeModifier(attribute1.getKeyOrThrow(),-1, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.HAND);
        AttributeModifier modifier2 = new AttributeModifier(attribute2.getKeyOrThrow(),8, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.HAND);
        itemMeta.addAttributeModifier(attribute1,modifier1);
        itemMeta.addAttributeModifier(attribute2,modifier2);
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "玻璃剑");
        lore.add(ChatColor.WHITE + "非常锋利，但是易碎");
        lore.add(ChatColor.WHITE + "受到伤害后会碎掉");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
    public ItemStack warHammer(){
        ItemStack item = new ItemStack(Material.COPPER_SHOVEL);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.RED + "战锤");
        Attribute attribute1 = Attribute.ATTACK_SPEED;
        Attribute attribute2 = Attribute.ATTACK_DAMAGE;
        AttributeModifier modifier1 = new AttributeModifier(attribute1.getKeyOrThrow(),-3.2, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.HAND);
        AttributeModifier modifier2 = new AttributeModifier(attribute2.getKeyOrThrow(),9, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.HAND);
        itemMeta.addAttributeModifier(attribute1,modifier1);
        itemMeta.addAttributeModifier(attribute2,modifier2);
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "战锤");
        lore.add(ChatColor.WHITE + "对有盔甲的目标造成额外伤害");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
    public ItemStack flameBow(){
        ItemStack item = new ItemStack(Material.BOW);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.RED + "烈焰弓");
        itemMeta.addEnchant(Enchantment.FLAME,1,true);
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "烈焰弓");
        lore.add(ChatColor.WHITE + "可以增加箭矢的飞行速度");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
    public ItemStack bambooSpear(){
        ItemStack item = new ItemStack(Material.WOODEN_SPEAR);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.RED + "竹叶青");
        Attribute attribute1 = Attribute.ATTACK_SPEED;
        Attribute attribute2 = Attribute.ATTACK_DAMAGE;
        AttributeModifier modifier1 = new AttributeModifier(attribute1.getKeyOrThrow(),-3.2, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.HAND);
        AttributeModifier modifier2 = new AttributeModifier(attribute2.getKeyOrThrow(),5, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.HAND);
        itemMeta.addAttributeModifier(attribute1,modifier1);
        itemMeta.addAttributeModifier(attribute2,modifier2);
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "竹叶青");
        lore.add(ChatColor.WHITE + "用竹子制成的长矛，上面附有剧毒");
        lore.add(ChatColor.WHITE + "攻击会附带中毒效果");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
    public ItemStack crystalSword(){
        ItemStack item = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.RED + "紫水晶刺剑");
        Attribute attribute1 = Attribute.ATTACK_SPEED;
        Attribute attribute2 = Attribute.ATTACK_DAMAGE;
        AttributeModifier modifier1 = new AttributeModifier(attribute1.getKeyOrThrow(),-1.5, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.HAND);
        AttributeModifier modifier2 = new AttributeModifier(attribute2.getKeyOrThrow(),5, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.HAND);
        itemMeta.addAttributeModifier(attribute1,modifier1);
        itemMeta.addAttributeModifier(attribute2,modifier2);
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "紫水晶刺剑");
        lore.add(ChatColor.WHITE + "用紫水晶制成的刺剑");
        lore.add(ChatColor.WHITE + "攻击会附带缓慢和发光效果");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
    public ItemStack echoAxe(){
        ItemStack item = new ItemStack(Material.DIAMOND_AXE);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.RED + "回响战斧");
        Attribute attribute1 = Attribute.ATTACK_SPEED;
        Attribute attribute2 = Attribute.ATTACK_DAMAGE;
        AttributeModifier modifier1 = new AttributeModifier(attribute1.getKeyOrThrow(),-3.2, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.HAND);
        AttributeModifier modifier2 = new AttributeModifier(attribute2.getKeyOrThrow(),10, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.HAND);
        itemMeta.addAttributeModifier(attribute1,modifier1);
        itemMeta.addAttributeModifier(attribute2,modifier2);
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "回响战斧");
        lore.add(ChatColor.WHITE + "蕴含远古力量");
        lore.add(ChatColor.WHITE + "攻击会附带挖掘疲劳效果");
        lore.add(ChatColor.WHITE + "按" + ChatColor.AQUA + "鼠标右键"
                + ChatColor.WHITE + "让自己获得黑暗和急迫效果");
        lore.add(ChatColor.WHITE + "并且让周围的生物获得发光效果");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
    public ItemStack cactusSword(){
        ItemStack item = new ItemStack(Material.WOODEN_SWORD);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDamageType(DamageType.CACTUS);
        itemMeta.setDisplayName(ChatColor.AQUA + "仙人掌剑");
        Attribute attribute1 = Attribute.ATTACK_SPEED;
        Attribute attribute2 = Attribute.ATTACK_DAMAGE;
        AttributeModifier modifier1 = new AttributeModifier(attribute1.getKeyOrThrow(),-2.5, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.HAND);
        AttributeModifier modifier2 = new AttributeModifier(attribute2.getKeyOrThrow(),6, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.HAND);
        itemMeta.addAttributeModifier(attribute1,modifier1);
        itemMeta.addAttributeModifier(attribute2,modifier2);
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "仙人掌剑");
        lore.add(ChatColor.WHITE + "不是一个好主意");
        lore.add(ChatColor.WHITE + "对敌人造成伤害时，自己也会受到伤害");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
    public ItemStack flintSword(){
        ItemStack item = new ItemStack(Material.STONE_SWORD);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.AQUA + "燧石剑");
        itemMeta.addEnchant(Enchantment.FIRE_ASPECT,1,true);
        Attribute attribute1 = Attribute.ATTACK_SPEED;
        Attribute attribute2 = Attribute.ATTACK_DAMAGE;
        AttributeModifier modifier1 = new AttributeModifier(attribute1.getKeyOrThrow(),-2.5, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.HAND);
        AttributeModifier modifier2 = new AttributeModifier(attribute2.getKeyOrThrow(),5, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.HAND);
        itemMeta.addAttributeModifier(attribute1,modifier1);
        itemMeta.addAttributeModifier(attribute2,modifier2);
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "燧石剑");
        lore.add(ChatColor.WHITE + "可以用来打火");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
    public ItemStack quartzSword(){
        ItemStack item = new ItemStack(Material.IRON_SWORD);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.AQUA + "石英剑");
        Attribute attribute1 = Attribute.ATTACK_SPEED;
        Attribute attribute2 = Attribute.ATTACK_DAMAGE;
        AttributeModifier modifier1 = new AttributeModifier(attribute1.getKeyOrThrow(),-2.4, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.HAND);
        AttributeModifier modifier2 = new AttributeModifier(attribute2.getKeyOrThrow(),6, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.HAND);
        itemMeta.addAttributeModifier(attribute1,modifier1);
        itemMeta.addAttributeModifier(attribute2,modifier2);
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "石英剑");
        lore.add(ChatColor.WHITE + "看起来很像铁剑");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
    public ItemStack netherSword(){
        ItemStack item = new ItemStack(Material.NETHERITE_SWORD);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.AQUA + "真·下界合金剑");
        Attribute attribute1 = Attribute.ATTACK_SPEED;
        Attribute attribute2 = Attribute.ATTACK_DAMAGE;
        AttributeModifier modifier1 = new AttributeModifier(attribute1.getKeyOrThrow(),-2.2, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.HAND);
        AttributeModifier modifier2 = new AttributeModifier(attribute2.getKeyOrThrow(),10, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.HAND);
        itemMeta.addAttributeModifier(attribute1,modifier1);
        itemMeta.addAttributeModifier(attribute2,modifier2);
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "真·下界合金剑");
        lore.add(ChatColor.WHITE + "比石斧攻击高");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
    public ItemStack goldenCarrot(){
        ItemStack item = new ItemStack(Material.GOLDEN_CARROT);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.AQUA + "金胡萝卜神的赐福");
        itemMeta.addEnchant(Enchantment.DENSITY,5,true);
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "金胡萝卜神的赐福");
        lore.add(ChatColor.WHITE + "可以使用金胡萝卜神的魔法");
        lore.add(ChatColor.WHITE + "按" + ChatColor.AQUA + "鼠标右键"
                + ChatColor.WHITE + "发射远程攻击");
        lore.add(ChatColor.WHITE + "但是需要消耗饥饿值");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
    public ItemStack echoSword(){
        ItemStack item = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.AQUA + "回响之刃");
        Attribute attribute1 = Attribute.ATTACK_SPEED;
        Attribute attribute2 = Attribute.ATTACK_DAMAGE;
        AttributeModifier modifier1 = new AttributeModifier(attribute1.getKeyOrThrow(),-2.5, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.HAND);
        AttributeModifier modifier2 = new AttributeModifier(attribute2.getKeyOrThrow(),8, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.HAND);
        itemMeta.addAttributeModifier(attribute1,modifier1);
        itemMeta.addAttributeModifier(attribute2,modifier2);
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "回响之刃");
        lore.add(ChatColor.WHITE + "蕴含远古的力量");
        lore.add(ChatColor.WHITE + "攻击会附带虚弱效果");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
    public ItemStack fireSword(){
        ItemStack item = new ItemStack(Material.COPPER_SWORD);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.YELLOW + "烈焰树脂剑");
        itemMeta.addEnchant(Enchantment.FIRE_ASPECT,2,true);
        itemMeta.addEnchant(Enchantment.SMITE,2,true);
        Attribute attribute1 = Attribute.ATTACK_SPEED;
        Attribute attribute2 = Attribute.ATTACK_DAMAGE;
        AttributeModifier modifier1 = new AttributeModifier(attribute1.getKeyOrThrow(),-2, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.HAND);
        AttributeModifier modifier2 = new AttributeModifier(attribute2.getKeyOrThrow(),5, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.HAND);
        itemMeta.addAttributeModifier(attribute1,modifier1);
        itemMeta.addAttributeModifier(attribute2,modifier2);
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "烈焰树脂剑");
        lore.add(ChatColor.WHITE + "着火辣！");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
    public ItemStack broom(){
        ItemStack item = new ItemStack(Material.BRUSH);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.YELLOW + "扫把");
        itemMeta.addEnchant(Enchantment.KNOCKBACK,3,true);
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "扫把");
        lore.add(ChatColor.WHITE + "扫清你面前不想看到的东西");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
    public ItemStack emeraldWand(){
        ItemStack item = new ItemStack(Material.OXIDIZED_LIGHTNING_ROD);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.YELLOW + "绿宝石权杖");
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "绿宝石权杖");
        lore.add(ChatColor.WHITE + "按" + ChatColor.AQUA + "鼠标右键"
                + ChatColor.WHITE + "释放治疗魔法");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
    public ItemStack windBow(){
        ItemStack item = new ItemStack(Material.BOW);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.YELLOW + "风之弓");
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "风之弓");
        lore.add(ChatColor.WHITE + "略微增加射出箭矢的速度");
        lore.add(ChatColor.WHITE + "箭矢命中的位置会产生风暴");
        lore.add(ChatColor.WHITE + "风暴会拉近附近的敌人");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
    public ItemStack masterStick(){
        ItemStack item = new ItemStack(Material.STICK);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.YELLOW + "大师长棍");
        itemMeta.addEnchant(Enchantment.KNOCKBACK,2,true);
        itemMeta.addEnchant(Enchantment.FIRE_ASPECT,2,true);
        itemMeta.addEnchant(Enchantment.SMITE,3,true);
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "大师长棍");
        lore.add(ChatColor.WHITE + "拿上他就能成为Sensei Wu的关门大弟子");
        lore.add(ChatColor.WHITE + "成为幻影旋转大师");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
    public ItemStack seaHammer(){
        ItemStack item = new ItemStack(Material.DIAMOND_AXE);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.YELLOW + "海神重锤");
        Attribute attribute1 = Attribute.ATTACK_SPEED;
        Attribute attribute2 = Attribute.ATTACK_DAMAGE;
        AttributeModifier modifier1 = new AttributeModifier(attribute1.getKeyOrThrow(),-3.25, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.HAND);
        AttributeModifier modifier2 = new AttributeModifier(attribute2.getKeyOrThrow(),10, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.HAND);
        itemMeta.addAttributeModifier(attribute1,modifier1);
        itemMeta.addAttributeModifier(attribute2,modifier2);
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "海神重锤");
        lore.add(ChatColor.WHITE + "拥有海神之力的重锤");
        lore.add(ChatColor.WHITE + "按" + ChatColor.AQUA + "鼠标右键"
                + ChatColor.WHITE + "向前踏步，并释放范围攻击");
        lore.add(ChatColor.WHITE + "范围攻击命中怪物后，自身获得抗性提升效果");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
    public ItemStack echoBow(){
        ItemStack item = new ItemStack(Material.BOW);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.YELLOW + "回响长弓");
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "回响长弓");
        lore.add(ChatColor.WHITE + "蕴含远古的力量");
        lore.add(ChatColor.WHITE + "拉满弓射击时，会释放音爆");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
    public ItemStack dirtHelm(){
        ItemStack item = new ItemStack(Material.LEATHER_HELMET);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.WHITE + "泥土头盔");
        Attribute a1 = Attribute.ARMOR;
        AttributeModifier m1 = new AttributeModifier(a1.getKeyOrThrow(),0.5, AttributeModifier.Operation.ADD_NUMBER,EquipmentSlotGroup.HEAD);
        itemMeta.addAttributeModifier(a1,m1);
        ((LeatherArmorMeta)itemMeta).setColor(Color.fromRGB(192,150,55));
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "泥土头盔");
        lore.add(ChatColor.WHITE + "几乎没有防御力");
        lore.add(ChatColor.WHITE + "当受到伤害时");
        lore.add(ChatColor.WHITE + "每个部位的泥土材质护甲");
        lore.add(ChatColor.WHITE + "都会提供1点的饱食度回复");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
    public ItemStack dirtChest(){
        ItemStack item = new ItemStack(Material.LEATHER_CHESTPLATE);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.WHITE + "泥土胸甲");
        Attribute a1 = Attribute.ARMOR;
        AttributeModifier m1 = new AttributeModifier(a1.getKeyOrThrow(),0.5, AttributeModifier.Operation.ADD_NUMBER,EquipmentSlotGroup.CHEST);
        itemMeta.addAttributeModifier(a1,m1);
        ((LeatherArmorMeta)itemMeta).setColor(Color.fromRGB(192,150,55));
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "泥土胸甲");
        lore.add(ChatColor.WHITE + "几乎没有防御力");
        lore.add(ChatColor.WHITE + "当受到伤害时");
        lore.add(ChatColor.WHITE + "每个部位的泥土材质护甲");
        lore.add(ChatColor.WHITE + "都会提供1点的饱食度回复");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
    public ItemStack dirtLeg(){
        ItemStack item = new ItemStack(Material.LEATHER_LEGGINGS);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.WHITE + "泥土护腿");
        Attribute a1 = Attribute.ARMOR;
        AttributeModifier m1 = new AttributeModifier(a1.getKeyOrThrow(),0.5, AttributeModifier.Operation.ADD_NUMBER,EquipmentSlotGroup.LEGS);
        itemMeta.addAttributeModifier(a1,m1);
        ((LeatherArmorMeta)itemMeta).setColor(Color.fromRGB(192,150,55));
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "泥土护腿");
        lore.add(ChatColor.WHITE + "几乎没有防御力");
        lore.add(ChatColor.WHITE + "当受到伤害时");
        lore.add(ChatColor.WHITE + "每个部位的泥土材质护甲");
        lore.add(ChatColor.WHITE + "都会提供1点的饱食度回复");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
    public ItemStack dirtBoot(){
        ItemStack item = new ItemStack(Material.LEATHER_BOOTS);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.WHITE + "泥土靴子");
        Attribute a1 = Attribute.ARMOR;
        AttributeModifier m1 = new AttributeModifier(a1.getKeyOrThrow(),0.5, AttributeModifier.Operation.ADD_NUMBER,EquipmentSlotGroup.FEET);
        itemMeta.addAttributeModifier(a1,m1);
        ((LeatherArmorMeta)itemMeta).setColor(Color.fromRGB(192,150,55));
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "泥土靴子");
        lore.add(ChatColor.WHITE + "几乎没有防御力");
        lore.add(ChatColor.WHITE + "当受到伤害时");
        lore.add(ChatColor.WHITE + "每个部位的泥土材质护甲");
        lore.add(ChatColor.WHITE + "都会提供1点的饱食度回复");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
    public ItemStack woodHelm(){
        ItemStack item = new ItemStack(Material.LEATHER_HELMET);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.WHITE + "白桦木头盔");
        Attribute a1 = Attribute.ARMOR;
        AttributeModifier m1 = new AttributeModifier(a1.getKeyOrThrow(),0.5, AttributeModifier.Operation.ADD_NUMBER,EquipmentSlotGroup.HEAD);
        itemMeta.addAttributeModifier(a1,m1);
        ((LeatherArmorMeta)itemMeta).setColor(Color.fromRGB(254,238,159));
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "白桦木头盔");
        lore.add(ChatColor.WHITE + "稍微坚固了一点");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
    public ItemStack woodChest(){
        ItemStack item = new ItemStack(Material.LEATHER_CHESTPLATE);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.WHITE + "白桦木胸甲");
        Attribute a1 = Attribute.ARMOR;
        AttributeModifier m1 = new AttributeModifier(a1.getKeyOrThrow(),1, AttributeModifier.Operation.ADD_NUMBER,EquipmentSlotGroup.CHEST);
        itemMeta.addAttributeModifier(a1,m1);
        ((LeatherArmorMeta)itemMeta).setColor(Color.fromRGB(254,238,159));
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "白桦木胸甲");
        lore.add(ChatColor.WHITE + "稍微坚固了一点");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
    public ItemStack woodLeg(){
        ItemStack item = new ItemStack(Material.LEATHER_LEGGINGS);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.WHITE + "白桦木护腿");
        Attribute a1 = Attribute.ARMOR;
        AttributeModifier m1 = new AttributeModifier(a1.getKeyOrThrow(),0.5, AttributeModifier.Operation.ADD_NUMBER,EquipmentSlotGroup.LEGS);
        itemMeta.addAttributeModifier(a1,m1);
        ((LeatherArmorMeta)itemMeta).setColor(Color.fromRGB(254,238,159));
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "白桦木护腿");
        lore.add(ChatColor.WHITE + "稍微坚固了一点");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
    public ItemStack woodBoot(){
        ItemStack item = new ItemStack(Material.LEATHER_BOOTS);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.WHITE + "白桦木靴子");
        Attribute a1 = Attribute.ARMOR;
        AttributeModifier m1 = new AttributeModifier(a1.getKeyOrThrow(),0.5, AttributeModifier.Operation.ADD_NUMBER,EquipmentSlotGroup.FEET);
        itemMeta.addAttributeModifier(a1,m1);
        ((LeatherArmorMeta)itemMeta).setColor(Color.fromRGB(254,238,159));
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "白桦木靴子");
        lore.add(ChatColor.WHITE + "稍微坚固了一点");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
    public ItemStack featherHelm(){
        ItemStack item = new ItemStack(Material.LEATHER_HELMET);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.WHITE + "羽毛头盔");
        Attribute a1 = Attribute.ARMOR;
        Attribute a2 = Attribute.MOVEMENT_SPEED;
        AttributeModifier m1 = new AttributeModifier(a1.getKeyOrThrow(),0.5, AttributeModifier.Operation.ADD_NUMBER,EquipmentSlotGroup.HEAD);
        AttributeModifier m2 = new AttributeModifier(a2.getKeyOrThrow(),0.05, AttributeModifier.Operation.ADD_SCALAR,EquipmentSlotGroup.HEAD);
        itemMeta.addAttributeModifier(a1,m1);
        itemMeta.addAttributeModifier(a2,m2);
        ((LeatherArmorMeta)itemMeta).setColor(Color.WHITE);
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "羽毛头盔");
        lore.add(ChatColor.WHITE + "非常轻便");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
    public ItemStack featherChest(){
        ItemStack item = new ItemStack(Material.LEATHER_CHESTPLATE);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.WHITE + "羽毛胸甲");
        Attribute a1 = Attribute.ARMOR;
        Attribute a2 = Attribute.MOVEMENT_SPEED;
        AttributeModifier m1 = new AttributeModifier(a1.getKeyOrThrow(),0.5, AttributeModifier.Operation.ADD_NUMBER,EquipmentSlotGroup.CHEST);
        AttributeModifier m2 = new AttributeModifier(a2.getKeyOrThrow(),0.05, AttributeModifier.Operation.ADD_SCALAR,EquipmentSlotGroup.CHEST);
        itemMeta.addAttributeModifier(a1,m1);
        itemMeta.addAttributeModifier(a2,m2);
        ((LeatherArmorMeta)itemMeta).setColor(Color.WHITE);
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "羽毛胸甲");
        lore.add(ChatColor.WHITE + "非常轻便");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
    public ItemStack featherLeg(){
        ItemStack item = new ItemStack(Material.LEATHER_CHESTPLATE);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.WHITE + "羽毛护腿");
        Attribute a1 = Attribute.ARMOR;
        Attribute a2 = Attribute.MOVEMENT_SPEED;
        AttributeModifier m1 = new AttributeModifier(a1.getKeyOrThrow(),0.5, AttributeModifier.Operation.ADD_NUMBER,EquipmentSlotGroup.LEGS);
        AttributeModifier m2 = new AttributeModifier(a2.getKeyOrThrow(),0.05, AttributeModifier.Operation.ADD_SCALAR,EquipmentSlotGroup.LEGS);
        itemMeta.addAttributeModifier(a1,m1);
        itemMeta.addAttributeModifier(a2,m2);
        ((LeatherArmorMeta)itemMeta).setColor(Color.WHITE);
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "羽毛护腿");
        lore.add(ChatColor.WHITE + "非常轻便");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
    public ItemStack featherBoot(){
        ItemStack item = new ItemStack(Material.LEATHER_BOOTS);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.WHITE + "羽毛靴子");
        Attribute a1 = Attribute.ARMOR;
        Attribute a2 = Attribute.MOVEMENT_SPEED;
        AttributeModifier m1 = new AttributeModifier(a1.getKeyOrThrow(),0.5, AttributeModifier.Operation.ADD_NUMBER,EquipmentSlotGroup.FEET);
        AttributeModifier m2 = new AttributeModifier(a2.getKeyOrThrow(),0.05, AttributeModifier.Operation.ADD_SCALAR,EquipmentSlotGroup.FEET);
        itemMeta.addAttributeModifier(a1,m1);
        itemMeta.addAttributeModifier(a2,m2);
        ((LeatherArmorMeta)itemMeta).setColor(Color.WHITE);
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "羽毛靴子");
        lore.add(ChatColor.WHITE + "非常轻便");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
    public ItemStack boneHelm(){
        ItemStack item = new ItemStack(Material.LEATHER_HELMET);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.WHITE + "骨头头盔");
        Attribute a1 = Attribute.ARMOR;
        AttributeModifier m1 = new AttributeModifier(a1.getKeyOrThrow(),0.5, AttributeModifier.Operation.ADD_NUMBER,EquipmentSlotGroup.HEAD);
        itemMeta.addAttributeModifier(a1,m1);
        itemMeta.addEnchant(Enchantment.PROJECTILE_PROTECTION,2,true);
        ((LeatherArmorMeta)itemMeta).setColor(Color.SILVER);
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "骨头头盔");
        lore.add(ChatColor.WHITE + "独特的结构可以有效抵御投射物");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
    public ItemStack boneChest(){
        ItemStack item = new ItemStack(Material.LEATHER_CHESTPLATE);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.WHITE + "骨头胸甲");
        Attribute a1 = Attribute.ARMOR;
        AttributeModifier m1 = new AttributeModifier(a1.getKeyOrThrow(),1, AttributeModifier.Operation.ADD_NUMBER,EquipmentSlotGroup.CHEST);
        itemMeta.addAttributeModifier(a1,m1);
        itemMeta.addEnchant(Enchantment.PROJECTILE_PROTECTION,2,true);
        ((LeatherArmorMeta)itemMeta).setColor(Color.SILVER);
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "骨头胸甲");
        lore.add(ChatColor.WHITE + "独特的结构可以有效抵御投射物");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
    public ItemStack boneLeg(){
        ItemStack item = new ItemStack(Material.LEATHER_LEGGINGS);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.WHITE + "骨头护腿");
        Attribute a1 = Attribute.ARMOR;
        AttributeModifier m1 = new AttributeModifier(a1.getKeyOrThrow(),1, AttributeModifier.Operation.ADD_NUMBER,EquipmentSlotGroup.LEGS);
        itemMeta.addAttributeModifier(a1,m1);
        itemMeta.addEnchant(Enchantment.PROJECTILE_PROTECTION,2,true);
        ((LeatherArmorMeta)itemMeta).setColor(Color.SILVER);
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "骨头护腿");
        lore.add(ChatColor.WHITE + "独特的结构可以有效抵御投射物");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
    public ItemStack boneBoot(){
        ItemStack item = new ItemStack(Material.LEATHER_BOOTS);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.WHITE + "骨头靴子");
        Attribute a1 = Attribute.ARMOR;
        AttributeModifier m1 = new AttributeModifier(a1.getKeyOrThrow(),0.5, AttributeModifier.Operation.ADD_NUMBER,EquipmentSlotGroup.FEET);
        itemMeta.addAttributeModifier(a1,m1);
        itemMeta.addEnchant(Enchantment.PROJECTILE_PROTECTION,2,true);
        ((LeatherArmorMeta)itemMeta).setColor(Color.SILVER);
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "骨头靴子");
        lore.add(ChatColor.WHITE + "独特的结构可以有效抵御投射物");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
    public ItemStack cactusHelm(){
        ItemStack item = new ItemStack(Material.LEATHER_HELMET);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.WHITE + "仙人掌头盔");
        Attribute a1 = Attribute.ARMOR;
        AttributeModifier m1 = new AttributeModifier(a1.getKeyOrThrow(),1, AttributeModifier.Operation.ADD_NUMBER,EquipmentSlotGroup.HEAD);
        itemMeta.addAttributeModifier(a1,m1);
        itemMeta.addEnchant(Enchantment.THORNS,1,true);
        ((LeatherArmorMeta)itemMeta).setColor(Color.GREEN);
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "仙人掌头盔");
        lore.add(ChatColor.WHITE + "穿的时候小心点");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
    public ItemStack cactusChest(){
        ItemStack item = new ItemStack(Material.LEATHER_CHESTPLATE);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.WHITE + "仙人掌胸甲");
        Attribute a1 = Attribute.ARMOR;
        AttributeModifier m1 = new AttributeModifier(a1.getKeyOrThrow(),1, AttributeModifier.Operation.ADD_NUMBER,EquipmentSlotGroup.CHEST);
        itemMeta.addAttributeModifier(a1,m1);
        itemMeta.addEnchant(Enchantment.THORNS,1,true);
        ((LeatherArmorMeta)itemMeta).setColor(Color.GREEN);
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "仙人掌胸甲");
        lore.add(ChatColor.WHITE + "穿的时候小心点");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
    public ItemStack cactusLeg(){
        ItemStack item = new ItemStack(Material.LEATHER_LEGGINGS);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.WHITE + "仙人掌护腿");
        Attribute a1 = Attribute.ARMOR;
        AttributeModifier m1 = new AttributeModifier(a1.getKeyOrThrow(),1, AttributeModifier.Operation.ADD_NUMBER,EquipmentSlotGroup.LEGS);
        itemMeta.addAttributeModifier(a1,m1);
        itemMeta.addEnchant(Enchantment.THORNS,1,true);
        ((LeatherArmorMeta)itemMeta).setColor(Color.GREEN);
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "仙人掌护腿");
        lore.add(ChatColor.WHITE + "穿的时候小心点");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
    public ItemStack cactusBoot(){
        ItemStack item = new ItemStack(Material.LEATHER_BOOTS);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.WHITE + "仙人掌靴子");
        Attribute a1 = Attribute.ARMOR;
        AttributeModifier m1 = new AttributeModifier(a1.getKeyOrThrow(),1, AttributeModifier.Operation.ADD_NUMBER,EquipmentSlotGroup.FEET);
        itemMeta.addAttributeModifier(a1,m1);
        itemMeta.addEnchant(Enchantment.THORNS,1,true);
        ((LeatherArmorMeta)itemMeta).setColor(Color.GREEN);
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "仙人掌靴子");
        lore.add(ChatColor.WHITE + "穿的时候小心点");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }

}

package Universal;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.damage.DamageType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;

public enum WeaponPool {
    INSTANCE;
    ItemStack[] recipeWeapons = {
            warHammer(),
            flameBow(),
            bambooSpear(),
            crystalSword(),
            echoAxe(),
            quartzSword(),
            netherSword(),
            goldenCarrot(),
            echoSword(),
            fireSword(),
            raiderTool(),
            windBow(),
            masterStick(),
            seaHammer(),
            echoBow(),
            echoCrossBow(),
            trident(),
    };
    public ItemStack[] containerWeapons = {
        new ItemStack(Material.WOODEN_SWORD),
        new ItemStack(Material.STONE_SWORD),
        new ItemStack(Material.COPPER_SWORD),
        new ItemStack(Material.GOLDEN_SWORD),
        new ItemStack(Material.IRON_SWORD),
        new ItemStack(Material.WOODEN_SPEAR),
        new ItemStack(Material.STONE_SPEAR),
        new ItemStack(Material.COPPER_SPEAR),
        new ItemStack(Material.GOLDEN_SPEAR),
        new ItemStack(Material.IRON_SPEAR),
        new ItemStack(Material.WOODEN_AXE),
        new ItemStack(Material.STONE_AXE),
        new ItemStack(Material.COPPER_AXE),
        new ItemStack(Material.GOLDEN_AXE),
        new ItemStack(Material.IRON_AXE),
            boneStick(),
            glassSword(),
            warHammer(),
            bambooSpear(),
            cactusSword(),
            flintSword(),
            quartzSword(),
            goldenCarrot(),
            fireSword(),
            broom(),
            raiderTool(),
    };
    public ItemStack[] boxWeapons = {
        new ItemStack(Material.WOODEN_SWORD),
        new ItemStack(Material.WOODEN_SWORD),
        new ItemStack(Material.STONE_SWORD),
        new ItemStack(Material.STONE_SWORD),
        new ItemStack(Material.COPPER_SWORD),
        new ItemStack(Material.COPPER_SWORD),
        new ItemStack(Material.GOLDEN_SWORD),
        new ItemStack(Material.GOLDEN_SWORD),
        new ItemStack(Material.IRON_SWORD),
        new ItemStack(Material.IRON_SWORD),
        new ItemStack(Material.WOODEN_SPEAR),
        new ItemStack(Material.WOODEN_SPEAR),
        new ItemStack(Material.STONE_SPEAR),
        new ItemStack(Material.STONE_SPEAR),
        new ItemStack(Material.COPPER_SPEAR),
        new ItemStack(Material.COPPER_SPEAR),
        new ItemStack(Material.GOLDEN_SPEAR),
        new ItemStack(Material.GOLDEN_SPEAR),
        new ItemStack(Material.IRON_SPEAR),
        new ItemStack(Material.IRON_SPEAR),
        new ItemStack(Material.WOODEN_AXE),
        new ItemStack(Material.WOODEN_AXE),
        new ItemStack(Material.STONE_AXE),
        new ItemStack(Material.STONE_AXE),
        new ItemStack(Material.COPPER_AXE),
        new ItemStack(Material.COPPER_AXE),
        new ItemStack(Material.GOLDEN_AXE),
        new ItemStack(Material.GOLDEN_AXE),
        new ItemStack(Material.IRON_AXE),
        new ItemStack(Material.IRON_AXE),
            boneStick(),
            glassSword(),
            warHammer(),
            bambooSpear(),
            cactusSword(),
            flintSword(),
            quartzSword(),
            goldenCarrot(),
            fireSword(),
            broom(),
            raiderTool(),
    };
    public ItemStack[] pluginWeapons = {
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
            ferro(),
            broom(),
            fireSword(),
            raiderTool(),
            windBow(),
            masterStick(),
            seaHammer(),
            echoBow(),
            echoCrossBow(),
    };

    public ItemStack[] getRecipeWeapons() {
        return recipeWeapons.clone();
    }

    public ItemStack[] getContainerWeapons() {
        return containerWeapons.clone();
    }

    public ItemStack[] getBoxWeapons() {
        return boxWeapons.clone();
    }

    public ItemStack[] getPluginWeapons() {
        return pluginWeapons.clone();
    }

    public ItemStack boneStick(){
        ItemStack item = new ItemStack(Material.IRON_AXE);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.RED + "大骨棒");
        Attribute attribute1 = Attribute.ATTACK_SPEED;
        Attribute attribute2 = Attribute.ATTACK_DAMAGE;
        AttributeModifier modifier1 = new AttributeModifier(NamespacedKey.randomKey(),-3.5, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.HAND);
        AttributeModifier modifier2 = new AttributeModifier(NamespacedKey.randomKey(),4, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.HAND);
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
        AttributeModifier modifier1 = new AttributeModifier(NamespacedKey.randomKey(),-1, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.HAND);
        AttributeModifier modifier2 = new AttributeModifier(NamespacedKey.randomKey(),7, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.HAND);
        itemMeta.addAttributeModifier(attribute1,modifier1);
        itemMeta.addAttributeModifier(attribute2,modifier2);
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "玻璃剑");
        lore.add(ChatColor.WHITE + "非常锋利，但是易碎");
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
        AttributeModifier modifier1 = new AttributeModifier(NamespacedKey.randomKey(),-3.2, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.HAND);
        AttributeModifier modifier2 = new AttributeModifier(NamespacedKey.randomKey(),8, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.HAND);
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
        AttributeModifier modifier1 = new AttributeModifier(NamespacedKey.randomKey(),-3.2, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.HAND);
        AttributeModifier modifier2 = new AttributeModifier(NamespacedKey.randomKey(),4, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.HAND);
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
        AttributeModifier modifier1 = new AttributeModifier(NamespacedKey.randomKey(),-1.5, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.HAND);
        AttributeModifier modifier2 = new AttributeModifier(NamespacedKey.randomKey(),5, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.HAND);
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
        AttributeModifier modifier1 = new AttributeModifier(NamespacedKey.randomKey(),-3.2, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.HAND);
        AttributeModifier modifier2 = new AttributeModifier(NamespacedKey.randomKey(),9, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.HAND);
        itemMeta.addAttributeModifier(attribute1,modifier1);
        itemMeta.addAttributeModifier(attribute2,modifier2);
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "回响战斧");
        lore.add(ChatColor.WHITE + "蕴含远古力量");
        lore.add(ChatColor.WHITE + "攻击会附带挖掘疲劳效果");
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
        AttributeModifier modifier1 = new AttributeModifier(NamespacedKey.randomKey(),-2.5, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.HAND);
        AttributeModifier modifier2 = new AttributeModifier(NamespacedKey.randomKey(),5, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.HAND);
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
        AttributeModifier modifier1 = new AttributeModifier(NamespacedKey.randomKey(),-2.5, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.HAND);
        AttributeModifier modifier2 = new AttributeModifier(NamespacedKey.randomKey(),4, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.HAND);
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
        AttributeModifier modifier1 = new AttributeModifier(NamespacedKey.randomKey(),-2.4, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.HAND);
        AttributeModifier modifier2 = new AttributeModifier(NamespacedKey.randomKey(),5, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.HAND);
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
        AttributeModifier modifier1 = new AttributeModifier(NamespacedKey.randomKey(),-2.2, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.HAND);
        AttributeModifier modifier2 = new AttributeModifier(NamespacedKey.randomKey(),9, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.HAND);
        itemMeta.addAttributeModifier(attribute1,modifier1);
        itemMeta.addAttributeModifier(attribute2,modifier2);
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "真·下界合金剑");
        lore.add(ChatColor.WHITE + "比石斧攻击高");
        lore.add(ChatColor.WHITE + "恭喜你多使用一个下界合金锭");
        lore.add(ChatColor.WHITE + "换来没有多少的性能提升");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
    public ItemStack goldenCarrot(){
        ItemStack item = Bukkit.getItemFactory().createItemStack(
                "stick[consumable={consume_seconds:1,animation:\"bow\",sound:\"intentionally_empty\",has_consume_particles:false,on_consume_effects:[{type:\"minecraft:play_sound\",sound:\"entity.experience_orb.pickup\"}]},item_model=\"minecraft:golden_carrot\"]"
        );
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
        AttributeModifier modifier1 = new AttributeModifier(NamespacedKey.randomKey(),-2.5, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.HAND);
        AttributeModifier modifier2 = new AttributeModifier(NamespacedKey.randomKey(),7, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.HAND);
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
        AttributeModifier modifier1 = new AttributeModifier(NamespacedKey.randomKey(),-2, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.HAND);
        AttributeModifier modifier2 = new AttributeModifier(NamespacedKey.randomKey(),4, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.HAND);
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
        Attribute attribute1 = Attribute.ATTACK_SPEED;
        Attribute attribute2 = Attribute.ATTACK_DAMAGE;
        AttributeModifier modifier1 = new AttributeModifier(NamespacedKey.randomKey(),-2, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.HAND);
        AttributeModifier modifier2 = new AttributeModifier(NamespacedKey.randomKey(),2, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.HAND);
        itemMeta.addAttributeModifier(attribute1,modifier1);
        itemMeta.addAttributeModifier(attribute2,modifier2);
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "扫把");
        lore.add(ChatColor.WHITE + "扫清你面前不想看到的东西");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
    public ItemStack raiderTool(){
        ItemStack item = new ItemStack(Material.IRON_HOE);
        ItemMeta itemMeta = item.getItemMeta();
        ((Damageable)itemMeta).setMaxDamage(15);
        itemMeta.setDisplayName(ChatColor.YELLOW + "奇袭者工具");
        Attribute attribute1 = Attribute.ATTACK_SPEED;
        Attribute attribute2 = Attribute.ATTACK_DAMAGE;
        AttributeModifier modifier1 = new AttributeModifier(NamespacedKey.randomKey(),-2, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.HAND);
        AttributeModifier modifier2 = new AttributeModifier(NamespacedKey.randomKey(),3, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.HAND);
        itemMeta.addAttributeModifier(attribute1,modifier1);
        itemMeta.addAttributeModifier(attribute2,modifier2);
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "奇袭者工具");
        lore.add(ChatColor.WHITE + "使用此工具搜索容器");
        lore.add(ChatColor.WHITE + "可以加快搜索容器的速度");
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
        lore.add(ChatColor.WHITE + "风暴会牵引附近的敌人");
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
        Attribute attribute1 = Attribute.ATTACK_SPEED;
        Attribute attribute2 = Attribute.ATTACK_DAMAGE;
        AttributeModifier modifier1 = new AttributeModifier(NamespacedKey.randomKey(),-1.5, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.HAND);
        AttributeModifier modifier2 = new AttributeModifier(NamespacedKey.randomKey(),3, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.HAND);
        itemMeta.addAttributeModifier(attribute1,modifier1);
        itemMeta.addAttributeModifier(attribute2,modifier2);
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
        AttributeModifier modifier1 = new AttributeModifier(NamespacedKey.randomKey(),-3.25, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.HAND);
        AttributeModifier modifier2 = new AttributeModifier(NamespacedKey.randomKey(),9, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.HAND);
        itemMeta.addAttributeModifier(attribute1,modifier1);
        itemMeta.addAttributeModifier(attribute2,modifier2);
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "海神重锤");
        lore.add(ChatColor.WHITE + "拥有海神之力的重锤");
        lore.add(ChatColor.WHITE + "按" + ChatColor.AQUA + "鼠标右键"
                + ChatColor.WHITE + "向前踏步，并释放范围攻击");
        lore.add(ChatColor.WHITE + "范围攻击命中后，自身获得抗性提升效果");
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
        lore.add(ChatColor.WHITE + "拉满弓射击时，会射出一支带有爆炸尾迹的箭矢");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
    public ItemStack echoCrossBow(){
        ItemStack item = new ItemStack(Material.CROSSBOW);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.YELLOW + "深渊十字弩");
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "深渊十字弩");
        lore.add(ChatColor.WHITE + "蕴含远古的力量");
        lore.add(ChatColor.WHITE + "射出一道音波");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
    public ItemStack ferro(){
        ItemStack item = new ItemStack(Material.CROSSBOW);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.YELLOW + "费洛");
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "费洛");
        lore.add(ChatColor.WHITE + "单发装填步枪");
        lore.add(ChatColor.WHITE + "看起来很像弩");
        lore.add(ChatColor.WHITE + "发射可以穿透怪物的强力弹药");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
    public ItemStack trident(){
        ItemStack item = new ItemStack(Material.TRIDENT);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.YELLOW + "三叉戟");
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "三叉戟");
        lore.add(ChatColor.WHITE + "可以合成的三叉戟");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
}

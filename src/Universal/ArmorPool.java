package Universal;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ArmorMeta;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;

import java.util.ArrayList;

public enum ArmorPool {
    INSTANCE;

    ItemStack[] containerArmors = {
            new ItemStack(Material.LEATHER_HELMET),
            new ItemStack(Material.LEATHER_CHESTPLATE),
            new ItemStack(Material.LEATHER_LEGGINGS),
            new ItemStack(Material.LEATHER_BOOTS),
            new ItemStack(Material.COPPER_HELMET),
            new ItemStack(Material.COPPER_CHESTPLATE),
            new ItemStack(Material.COPPER_LEGGINGS),
            new ItemStack(Material.COPPER_BOOTS),
            new ItemStack(Material.IRON_HELMET),
            new ItemStack(Material.IRON_CHESTPLATE),
            new ItemStack(Material.IRON_LEGGINGS),
            new ItemStack(Material.IRON_BOOTS),
            new ItemStack(Material.GOLDEN_HELMET),
            new ItemStack(Material.GOLDEN_CHESTPLATE),
            new ItemStack(Material.GOLDEN_LEGGINGS),
            new ItemStack(Material.GOLDEN_BOOTS),
            new ItemStack(Material.CHAINMAIL_HELMET),
            new ItemStack(Material.CHAINMAIL_CHESTPLATE),
            new ItemStack(Material.CHAINMAIL_LEGGINGS),
            new ItemStack(Material.CHAINMAIL_BOOTS),
            wheatHelm(),
            wheatChest(),
            wheatLeg(),
            wheatBoot(),
            dirtHelm(),
            dirtChest(),
            dirtLeg(),
            dirtBoot(),
            woodHelm(),
            woodChest(),
            woodLeg(),
            woodBoot(),
            featherHelm(),
            featherChest(),
            featherLeg(),
            featherBoot(),
            boneHelm(),
            boneChest(),
            boneLeg(),
            boneBoot(),
            cactusHelm(),
            cactusChest(),
            cactusLeg(),
            cactusBoot(),
            flintHelm(),
            flintChest(),
            flintLeg(),
            flintBoot(),
            treeHelm(),
            treeChest(),
            treeLeg(),
            treeBoot(),
            stoneHelm(),
            stoneChest(),
            stoneLeg(),
            stoneBoot(),
            quartzHelm(),
            quartzChest(),
            quartzLeg(),
            quartzBoot(),
            phantomHelm(),
            phantomChest(),
            phantomLeg(),
            phantomBoot(),
            purpleHelm(),
            purpleChest(),
            purpleLeg(),
            purpleBoot(),
            greenHelm(),
            greenChest(),
            greenLeg(),
            greenBoot(),
            obHelm(),
            obChest(),
            obLeg(),
            obBoot(),
    };
    public ItemStack[] recipeArmors = new ItemStack[]{
            wheatHelm(),
            wheatChest(),
            wheatLeg(),
            wheatBoot(),
            dirtHelm(),
            dirtChest(),
            dirtLeg(),
            dirtBoot(),
            woodHelm(),
            woodChest(),
            woodLeg(),
            woodBoot(),
            featherHelm(),
            featherChest(),
            featherLeg(),
            featherBoot(),
            boneHelm(),
            boneChest(),
            boneLeg(),
            boneBoot(),
            cactusHelm(),
            cactusChest(),
            cactusLeg(),
            cactusBoot(),
            flintHelm(),
            flintChest(),
            flintLeg(),
            flintBoot(),
            treeHelm(),
            treeChest(),
            treeLeg(),
            treeBoot(),
            stoneHelm(),
            stoneChest(),
            stoneLeg(),
            stoneBoot(),
            quartzHelm(),
            quartzChest(),
            quartzLeg(),
            quartzBoot(),
            phantomHelm(),
            phantomChest(),
            phantomLeg(),
            phantomBoot(),
            purpleHelm(),
            purpleChest(),
            purpleLeg(),
            purpleBoot(),
            greenHelm(),
            greenChest(),
            greenLeg(),
            greenBoot(),
            obHelm(),
            obChest(),
            obLeg(),
            obBoot(),
            echoHelm(),
            echoChest(),
            echoLeg(),
            echoBoot(),
    };
    public ItemStack[] getContainerArmors() {
        return containerArmors.clone();
    }

    public ItemStack[] getRecipeArmors() {return recipeArmors.clone();}

    public ItemStack wheatHelm(){
        ItemStack item = new ItemStack(Material.LEATHER_HELMET);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.WHITE + "干草头盔");
        ((Damageable)itemMeta).setMaxDamage(25);
        Attribute a1 = Attribute.ARMOR;
        AttributeModifier m1 = new AttributeModifier(NamespacedKey.randomKey(),1, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HEAD.getGroup());
        itemMeta.addAttributeModifier(a1,m1);
        ((LeatherArmorMeta)itemMeta).setColor(Color.fromRGB(216,202,94));
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "干草头盔");
        lore.add(ChatColor.WHITE + "几乎没有防御力");
        lore.add(ChatColor.WHITE + "当受到伤害时");
        lore.add(ChatColor.WHITE + "每个部位的干草材质护甲");
        lore.add(ChatColor.WHITE + "都会提供1点的饱食度回复");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
    public ItemStack wheatChest(){
        ItemStack item = new ItemStack(Material.LEATHER_CHESTPLATE);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.WHITE + "干草胸甲");
        ((Damageable)itemMeta).setMaxDamage(30);
        Attribute a1 = Attribute.ARMOR;
        AttributeModifier m1 = new AttributeModifier(NamespacedKey.randomKey(),1.5, AttributeModifier.Operation.ADD_NUMBER,EquipmentSlot.CHEST.getGroup());
        itemMeta.addAttributeModifier(a1,m1);
        ((LeatherArmorMeta)itemMeta).setColor(Color.fromRGB(216,202,94));
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "干草胸甲");
        lore.add(ChatColor.WHITE + "几乎没有防御力");
        lore.add(ChatColor.WHITE + "当受到伤害时");
        lore.add(ChatColor.WHITE + "每个部位的干草材质护甲");
        lore.add(ChatColor.WHITE + "都会提供1点的饱食度回复");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
    public ItemStack wheatLeg(){
        ItemStack item = new ItemStack(Material.LEATHER_LEGGINGS);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.WHITE + "干草护腿");
        ((Damageable)itemMeta).setMaxDamage(25);
        Attribute a1 = Attribute.ARMOR;
        AttributeModifier m1 = new AttributeModifier(NamespacedKey.randomKey(),1, AttributeModifier.Operation.ADD_NUMBER,EquipmentSlot.LEGS.getGroup());
        itemMeta.addAttributeModifier(a1,m1);
        ((LeatherArmorMeta)itemMeta).setColor(Color.fromRGB(216,202,94));
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "干草护腿");
        lore.add(ChatColor.WHITE + "几乎没有防御力");
        lore.add(ChatColor.WHITE + "当受到伤害时");
        lore.add(ChatColor.WHITE + "每个部位的干草材质护甲");
        lore.add(ChatColor.WHITE + "都会提供1点的饱食度回复");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
    public ItemStack wheatBoot(){
        ItemStack item = new ItemStack(Material.LEATHER_BOOTS);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.WHITE + "干草靴子");
        ((Damageable)itemMeta).setMaxDamage(20);
        Attribute a1 = Attribute.ARMOR;
        AttributeModifier m1 = new AttributeModifier(NamespacedKey.randomKey(),0.5, AttributeModifier.Operation.ADD_NUMBER,EquipmentSlot.FEET.getGroup());
        itemMeta.addAttributeModifier(a1,m1);
        ((LeatherArmorMeta)itemMeta).setColor(Color.fromRGB(216,202,94));
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "干草靴子");
        lore.add(ChatColor.WHITE + "几乎没有防御力");
        lore.add(ChatColor.WHITE + "当受到伤害时");
        lore.add(ChatColor.WHITE + "每个部位的干草材质护甲");
        lore.add(ChatColor.WHITE + "都会提供1点的饱食度回复");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
    public ItemStack dirtHelm(){
        ItemStack item = new ItemStack(Material.LEATHER_HELMET);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.WHITE + "泥土头盔");
        ((Damageable)itemMeta).setMaxDamage(45);
        Attribute a1 = Attribute.ARMOR;
        AttributeModifier m1 = new AttributeModifier(NamespacedKey.randomKey(),1, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HEAD.getGroup());
        itemMeta.addAttributeModifier(a1,m1);
        ((LeatherArmorMeta)itemMeta).setColor(Color.fromRGB(192,150,55));
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "泥土头盔");
        lore.add(ChatColor.WHITE + "防御力很低，但是耐久高一些");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
    public ItemStack dirtChest(){
        ItemStack item = new ItemStack(Material.LEATHER_CHESTPLATE);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.WHITE + "泥土胸甲");
        ((Damageable)itemMeta).setMaxDamage(55);
        Attribute a1 = Attribute.ARMOR;
        AttributeModifier m1 = new AttributeModifier(NamespacedKey.randomKey(),1.5, AttributeModifier.Operation.ADD_NUMBER,EquipmentSlot.CHEST.getGroup());
        itemMeta.addAttributeModifier(a1,m1);
        ((LeatherArmorMeta)itemMeta).setColor(Color.fromRGB(192,150,55));
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "泥土胸甲");
        lore.add(ChatColor.WHITE + "防御力很低，但是耐久高一些");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
    public ItemStack dirtLeg(){
        ItemStack item = new ItemStack(Material.LEATHER_LEGGINGS);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.WHITE + "泥土护腿");
        ((Damageable)itemMeta).setMaxDamage(50);
        Attribute a1 = Attribute.ARMOR;
        AttributeModifier m1 = new AttributeModifier(NamespacedKey.randomKey(),1, AttributeModifier.Operation.ADD_NUMBER,EquipmentSlot.LEGS.getGroup());
        itemMeta.addAttributeModifier(a1,m1);
        ((LeatherArmorMeta)itemMeta).setColor(Color.fromRGB(192,150,55));
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "泥土护腿");
        lore.add(ChatColor.WHITE + "防御力很低，但是耐久高一些");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
    public ItemStack dirtBoot(){
        ItemStack item = new ItemStack(Material.LEATHER_BOOTS);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.WHITE + "泥土靴子");
        ((Damageable)itemMeta).setMaxDamage(40);
        Attribute a1 = Attribute.ARMOR;
        AttributeModifier m1 = new AttributeModifier(NamespacedKey.randomKey(),0.5, AttributeModifier.Operation.ADD_NUMBER,EquipmentSlot.FEET.getGroup());
        itemMeta.addAttributeModifier(a1,m1);
        ((LeatherArmorMeta)itemMeta).setColor(Color.fromRGB(192,150,55));
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "泥土靴子");
        lore.add(ChatColor.WHITE + "防御力很低，但是耐久高一些");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
    public ItemStack woodHelm(){
        ItemStack item = new ItemStack(Material.LEATHER_HELMET);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.WHITE + "白桦木头盔");
        ((Damageable)itemMeta).setMaxDamage(40);
        Attribute a1 = Attribute.ARMOR;
        AttributeModifier m1 = new AttributeModifier(NamespacedKey.randomKey(),1, AttributeModifier.Operation.ADD_NUMBER,EquipmentSlot.HEAD.getGroup());
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
        ((Damageable)itemMeta).setMaxDamage(50);
        Attribute a1 = Attribute.ARMOR;
        AttributeModifier m1 = new AttributeModifier(NamespacedKey.randomKey(),2, AttributeModifier.Operation.ADD_NUMBER,EquipmentSlot.CHEST.getGroup());
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
        ((Damageable)itemMeta).setMaxDamage(45);
        Attribute a1 = Attribute.ARMOR;
        AttributeModifier m1 = new AttributeModifier(NamespacedKey.randomKey(),1.5, AttributeModifier.Operation.ADD_NUMBER,EquipmentSlot.LEGS.getGroup());
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
        ((Damageable)itemMeta).setMaxDamage(35);
        Attribute a1 = Attribute.ARMOR;
        AttributeModifier m1 = new AttributeModifier(NamespacedKey.randomKey(),0.5, AttributeModifier.Operation.ADD_NUMBER,EquipmentSlot.FEET.getGroup());
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
        ((Damageable)itemMeta).setMaxDamage(25);
        Attribute a1 = Attribute.ARMOR;
        Attribute a2 = Attribute.MOVEMENT_SPEED;
        AttributeModifier m1 = new AttributeModifier(NamespacedKey.randomKey(),1, AttributeModifier.Operation.ADD_NUMBER,EquipmentSlot.HEAD.getGroup());
        AttributeModifier m2 = new AttributeModifier(NamespacedKey.randomKey(),0.05, AttributeModifier.Operation.ADD_SCALAR,EquipmentSlot.HEAD.getGroup());
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
        ((Damageable)itemMeta).setMaxDamage(30);
        Attribute a1 = Attribute.ARMOR;
        Attribute a2 = Attribute.MOVEMENT_SPEED;
        AttributeModifier m1 = new AttributeModifier(NamespacedKey.randomKey(),1.5, AttributeModifier.Operation.ADD_NUMBER,EquipmentSlot.CHEST.getGroup());
        AttributeModifier m2 = new AttributeModifier(NamespacedKey.randomKey(),0.05, AttributeModifier.Operation.ADD_SCALAR,EquipmentSlot.CHEST.getGroup());
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
        ItemStack item = new ItemStack(Material.LEATHER_LEGGINGS);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.WHITE + "羽毛护腿");
        ((Damageable)itemMeta).setMaxDamage(25);
        Attribute a1 = Attribute.ARMOR;
        Attribute a2 = Attribute.MOVEMENT_SPEED;
        AttributeModifier m1 = new AttributeModifier(NamespacedKey.randomKey(),1, AttributeModifier.Operation.ADD_NUMBER,EquipmentSlot.LEGS.getGroup());
        AttributeModifier m2 = new AttributeModifier(NamespacedKey.randomKey(),0.05, AttributeModifier.Operation.ADD_SCALAR,EquipmentSlot.LEGS.getGroup());
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
        ((Damageable)itemMeta).setMaxDamage(20);
        Attribute a1 = Attribute.ARMOR;
        Attribute a2 = Attribute.MOVEMENT_SPEED;
        AttributeModifier m1 = new AttributeModifier(NamespacedKey.randomKey(),0.5, AttributeModifier.Operation.ADD_NUMBER,EquipmentSlot.FEET.getGroup());
        AttributeModifier m2 = new AttributeModifier(NamespacedKey.randomKey(),0.05, AttributeModifier.Operation.ADD_SCALAR,EquipmentSlot.FEET.getGroup());
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
        ((Damageable)itemMeta).setMaxDamage(45);
        Attribute a1 = Attribute.ARMOR;
        AttributeModifier m1 = new AttributeModifier(NamespacedKey.randomKey(),1, AttributeModifier.Operation.ADD_NUMBER,EquipmentSlot.HEAD.getGroup());
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
        ((Damageable)itemMeta).setMaxDamage(55);
        Attribute a1 = Attribute.ARMOR;
        AttributeModifier m1 = new AttributeModifier(NamespacedKey.randomKey(),2.5, AttributeModifier.Operation.ADD_NUMBER,EquipmentSlot.CHEST.getGroup());
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
        ((Damageable)itemMeta).setMaxDamage(50);
        Attribute a1 = Attribute.ARMOR;
        AttributeModifier m1 = new AttributeModifier(NamespacedKey.randomKey(),2, AttributeModifier.Operation.ADD_NUMBER,EquipmentSlot.LEGS.getGroup());
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
        ((Damageable)itemMeta).setMaxDamage(40);
        Attribute a1 = Attribute.ARMOR;
        AttributeModifier m1 = new AttributeModifier(NamespacedKey.randomKey(),0.5, AttributeModifier.Operation.ADD_NUMBER,EquipmentSlot.FEET.getGroup());
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
        ((Damageable)itemMeta).setMaxDamage(120);
        Attribute a1 = Attribute.ARMOR;
        AttributeModifier m1 = new AttributeModifier(NamespacedKey.randomKey(),1.5, AttributeModifier.Operation.ADD_NUMBER,EquipmentSlot.HEAD.getGroup());
        itemMeta.addAttributeModifier(a1,m1);
        itemMeta.addEnchant(Enchantment.THORNS,1,true);
        ((LeatherArmorMeta)itemMeta).setColor(Color.GREEN);
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "仙人掌头盔");
        lore.add(ChatColor.WHITE + "小心不要穿反了");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
    public ItemStack cactusChest(){
        ItemStack item = new ItemStack(Material.LEATHER_CHESTPLATE);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.WHITE + "仙人掌胸甲");
        ((Damageable)itemMeta).setMaxDamage(170);
        Attribute a1 = Attribute.ARMOR;
        AttributeModifier m1 = new AttributeModifier(NamespacedKey.randomKey(),3.5, AttributeModifier.Operation.ADD_NUMBER,EquipmentSlot.CHEST.getGroup());
        itemMeta.addAttributeModifier(a1,m1);
        itemMeta.addEnchant(Enchantment.THORNS,1,true);
        ((LeatherArmorMeta)itemMeta).setColor(Color.GREEN);
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "仙人掌胸甲");
        lore.add(ChatColor.WHITE + "小心不要穿反了");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
    public ItemStack cactusLeg(){
        ItemStack item = new ItemStack(Material.LEATHER_LEGGINGS);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.WHITE + "仙人掌护腿");
        ((Damageable)itemMeta).setMaxDamage(160);
        Attribute a1 = Attribute.ARMOR;
        AttributeModifier m1 = new AttributeModifier(NamespacedKey.randomKey(),2, AttributeModifier.Operation.ADD_NUMBER,EquipmentSlot.LEGS.getGroup());
        itemMeta.addAttributeModifier(a1,m1);
        itemMeta.addEnchant(Enchantment.THORNS,1,true);
        ((LeatherArmorMeta)itemMeta).setColor(Color.GREEN);
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "仙人掌护腿");
        lore.add(ChatColor.WHITE + "小心不要穿反了");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
    public ItemStack cactusBoot(){
        ItemStack item = new ItemStack(Material.LEATHER_BOOTS);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.WHITE + "仙人掌靴子");
        ((Damageable)itemMeta).setMaxDamage(130);
        Attribute a1 = Attribute.ARMOR;
        AttributeModifier m1 = new AttributeModifier(NamespacedKey.randomKey(),1, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.FEET.getGroup());
        itemMeta.addAttributeModifier(a1,m1);
        itemMeta.addEnchant(Enchantment.THORNS,1,true);
        ((LeatherArmorMeta)itemMeta).setColor(Color.GREEN);
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "仙人掌靴子");
        lore.add(ChatColor.WHITE + "小心不要穿反了");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
    public ItemStack flintHelm(){
        ItemStack item = new ItemStack(Material.LEATHER_HELMET);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.WHITE + "燧石头盔");
        ((Damageable)itemMeta).setMaxDamage(90);
        Attribute a1 = Attribute.ARMOR;
        AttributeModifier m1 = new AttributeModifier(NamespacedKey.randomKey(),2, AttributeModifier.Operation.ADD_NUMBER,EquipmentSlot.HEAD.getGroup());
        itemMeta.addAttributeModifier(a1,m1);
        ((LeatherArmorMeta)itemMeta).setColor(Color.GRAY.mixColors(Color.BLACK));
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "燧石头盔");
        lore.add(ChatColor.WHITE + "远离易燃物");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
    public ItemStack flintChest(){
        ItemStack item = new ItemStack(Material.LEATHER_CHESTPLATE);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.WHITE + "燧石胸甲");
        ((Damageable)itemMeta).setMaxDamage(130);
        Attribute a1 = Attribute.ARMOR;
        AttributeModifier m1 = new AttributeModifier(NamespacedKey.randomKey(),3, AttributeModifier.Operation.ADD_NUMBER,EquipmentSlot.CHEST.getGroup());
        itemMeta.addAttributeModifier(a1,m1);
        ((LeatherArmorMeta)itemMeta).setColor(Color.GRAY.mixColors(Color.BLACK));
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "燧石胸甲");
        lore.add(ChatColor.WHITE + "远离易燃物");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
    public ItemStack flintLeg(){
        ItemStack item = new ItemStack(Material.LEATHER_LEGGINGS);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.WHITE + "燧石护腿");
        ((Damageable)itemMeta).setMaxDamage(120);
        Attribute a1 = Attribute.ARMOR;
        AttributeModifier m1 = new AttributeModifier(NamespacedKey.randomKey(),2.5, AttributeModifier.Operation.ADD_NUMBER,EquipmentSlot.LEGS.getGroup());
        itemMeta.addAttributeModifier(a1,m1);
        ((LeatherArmorMeta)itemMeta).setColor(Color.GRAY.mixColors(Color.BLACK));
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "燧石护腿");
        lore.add(ChatColor.WHITE + "远离易燃物");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
    public ItemStack flintBoot(){
        ItemStack item = new ItemStack(Material.LEATHER_BOOTS);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.WHITE + "燧石靴子");
        ((Damageable)itemMeta).setMaxDamage(100);
        Attribute a1 = Attribute.ARMOR;
        AttributeModifier m1 = new AttributeModifier(NamespacedKey.randomKey(),1.5, AttributeModifier.Operation.ADD_NUMBER,EquipmentSlot.FEET.getGroup());
        itemMeta.addAttributeModifier(a1,m1);
        ((LeatherArmorMeta)itemMeta).setColor(Color.GRAY.mixColors(Color.BLACK));
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "燧石靴子");
        lore.add(ChatColor.WHITE + "远离易燃物");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
    public ItemStack treeHelm(){
        ItemStack item = new ItemStack(Material.COPPER_HELMET);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.WHITE + "树脂头盔");
        ((Damageable)itemMeta).setMaxDamage(360);
        Attribute a1 = Attribute.ARMOR;
        Attribute a2 = Attribute.ARMOR_TOUGHNESS;
        AttributeModifier m1 = new AttributeModifier(NamespacedKey.randomKey(),2, AttributeModifier.Operation.ADD_NUMBER,EquipmentSlot.HEAD.getGroup());
        AttributeModifier m2 = new AttributeModifier(NamespacedKey.randomKey(),3.5, AttributeModifier.Operation.ADD_NUMBER,EquipmentSlot.HEAD.getGroup());
        itemMeta.addAttributeModifier(a1,m1);
        itemMeta.addAttributeModifier(a2,m2);
        ((ArmorMeta)itemMeta).setTrim(new ArmorTrim(TrimMaterial.IRON, TrimPattern.SHAPER));
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "树脂头盔");
        lore.add(ChatColor.WHITE + "韧性非常高");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
    public ItemStack treeChest(){
        ItemStack item = new ItemStack(Material.COPPER_CHESTPLATE);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.WHITE + "树脂胸甲");
        ((Damageable)itemMeta).setMaxDamage(520);
        Attribute a1 = Attribute.ARMOR;
        Attribute a2 = Attribute.ARMOR_TOUGHNESS;
        AttributeModifier m1 = new AttributeModifier(NamespacedKey.randomKey(),5, AttributeModifier.Operation.ADD_NUMBER,EquipmentSlot.CHEST.getGroup());
        AttributeModifier m2 = new AttributeModifier(NamespacedKey.randomKey(),5.5, AttributeModifier.Operation.ADD_NUMBER,EquipmentSlot.CHEST.getGroup());
        itemMeta.addAttributeModifier(a1,m1);
        itemMeta.addAttributeModifier(a2,m2);
        ((ArmorMeta)itemMeta).setTrim(new ArmorTrim(TrimMaterial.IRON, TrimPattern.SHAPER));
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "树脂胸甲");
        lore.add(ChatColor.WHITE + "韧性非常高");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
    public ItemStack treeLeg(){
        ItemStack item = new ItemStack(Material.COPPER_LEGGINGS);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.WHITE + "树脂护腿");
        ((Damageable)itemMeta).setMaxDamage(490);
        Attribute a1 = Attribute.ARMOR;
        Attribute a2 = Attribute.ARMOR_TOUGHNESS;
        AttributeModifier m1 = new AttributeModifier(NamespacedKey.randomKey(),3.5, AttributeModifier.Operation.ADD_NUMBER,EquipmentSlot.LEGS.getGroup());
        AttributeModifier m2 = new AttributeModifier(NamespacedKey.randomKey(),5, AttributeModifier.Operation.ADD_NUMBER,EquipmentSlot.LEGS.getGroup());
        itemMeta.addAttributeModifier(a1,m1);
        itemMeta.addAttributeModifier(a2,m2);
        ((ArmorMeta)itemMeta).setTrim(new ArmorTrim(TrimMaterial.IRON, TrimPattern.SHAPER));
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "树脂护腿");
        lore.add(ChatColor.WHITE + "韧性非常高");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
    public ItemStack treeBoot(){
        ItemStack item = new ItemStack(Material.COPPER_BOOTS);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.WHITE + "树脂靴子");
        ((Damageable)itemMeta).setMaxDamage(420);
        Attribute a1 = Attribute.ARMOR;
        Attribute a2 = Attribute.ARMOR_TOUGHNESS;
        AttributeModifier m1 = new AttributeModifier(NamespacedKey.randomKey(),1.5, AttributeModifier.Operation.ADD_NUMBER,EquipmentSlot.FEET.getGroup());
        AttributeModifier m2 = new AttributeModifier(NamespacedKey.randomKey(),2, AttributeModifier.Operation.ADD_NUMBER,EquipmentSlot.FEET.getGroup());
        itemMeta.addAttributeModifier(a1,m1);
        itemMeta.addAttributeModifier(a2,m2);
        ((ArmorMeta)itemMeta).setTrim(new ArmorTrim(TrimMaterial.IRON, TrimPattern.SHAPER));
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "树脂靴子");
        lore.add(ChatColor.WHITE + "韧性非常高");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
    public ItemStack stoneHelm(){
        ItemStack item = new ItemStack(Material.LEATHER_HELMET);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.WHITE + "圆石头盔");
        ((Damageable)itemMeta).setMaxDamage(160);
        Attribute a1 = Attribute.ARMOR;
        Attribute a2 = Attribute.MOVEMENT_SPEED;
        Attribute a3 = Attribute.KNOCKBACK_RESISTANCE;
        AttributeModifier m1 = new AttributeModifier(NamespacedKey.randomKey(),2, AttributeModifier.Operation.ADD_NUMBER,EquipmentSlot.HEAD.getGroup());
        AttributeModifier m2 = new AttributeModifier(NamespacedKey.randomKey(),-0.035, AttributeModifier.Operation.ADD_SCALAR,EquipmentSlot.HEAD.getGroup());
        AttributeModifier m3 = new AttributeModifier(NamespacedKey.randomKey(),0.05, AttributeModifier.Operation.ADD_NUMBER,EquipmentSlot.HEAD.getGroup());
        itemMeta.addAttributeModifier(a1,m1);
        itemMeta.addAttributeModifier(a2,m2);
        itemMeta.addAttributeModifier(a3,m3);
        ((LeatherArmorMeta)itemMeta).setColor(Color.GRAY);
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "圆石头盔");
        lore.add(ChatColor.WHITE + "非常厚实，非常重");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
    public ItemStack stoneChest(){
        ItemStack item = new ItemStack(Material.LEATHER_CHESTPLATE);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.WHITE + "圆石胸甲");
        ((Damageable)itemMeta).setMaxDamage(230);
        Attribute a1 = Attribute.ARMOR;
        Attribute a2 = Attribute.MOVEMENT_SPEED;
        Attribute a3 = Attribute.KNOCKBACK_RESISTANCE;
        AttributeModifier m1 = new AttributeModifier(NamespacedKey.randomKey(),5.5, AttributeModifier.Operation.ADD_NUMBER,EquipmentSlot.CHEST.getGroup());
        AttributeModifier m2 = new AttributeModifier(NamespacedKey.randomKey(),-0.035, AttributeModifier.Operation.ADD_SCALAR,EquipmentSlot.CHEST.getGroup());
        AttributeModifier m3 = new AttributeModifier(NamespacedKey.randomKey(),0.05, AttributeModifier.Operation.ADD_NUMBER,EquipmentSlot.CHEST.getGroup());
        itemMeta.addAttributeModifier(a1,m1);
        itemMeta.addAttributeModifier(a2,m2);
        itemMeta.addAttributeModifier(a3,m3);
        ((LeatherArmorMeta)itemMeta).setColor(Color.GRAY);
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "圆石胸甲");
        lore.add(ChatColor.WHITE + "非常厚实，非常重");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
    public ItemStack stoneLeg(){
        ItemStack item = new ItemStack(Material.LEATHER_LEGGINGS);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.WHITE + "圆石护腿");
        ((Damageable)itemMeta).setMaxDamage(220);
        Attribute a1 = Attribute.ARMOR;
        Attribute a2 = Attribute.MOVEMENT_SPEED;
        Attribute a3 = Attribute.KNOCKBACK_RESISTANCE;
        AttributeModifier m1 = new AttributeModifier(NamespacedKey.randomKey(),4, AttributeModifier.Operation.ADD_NUMBER,EquipmentSlot.LEGS.getGroup());
        AttributeModifier m2 = new AttributeModifier(NamespacedKey.randomKey(),-0.035, AttributeModifier.Operation.ADD_SCALAR,EquipmentSlot.LEGS.getGroup());
        AttributeModifier m3 = new AttributeModifier(NamespacedKey.randomKey(),0.05, AttributeModifier.Operation.ADD_NUMBER,EquipmentSlot.LEGS.getGroup());
        itemMeta.addAttributeModifier(a1,m1);
        itemMeta.addAttributeModifier(a2,m2);
        itemMeta.addAttributeModifier(a3,m3);
        ((LeatherArmorMeta)itemMeta).setColor(Color.GRAY);
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "圆石护腿");
        lore.add(ChatColor.WHITE + "非常厚实，非常重");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
    public ItemStack stoneBoot(){
        ItemStack item = new ItemStack(Material.LEATHER_BOOTS);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.WHITE + "圆石靴子");
        ((Damageable)itemMeta).setMaxDamage(190);
        Attribute a1 = Attribute.ARMOR;
        Attribute a2 = Attribute.MOVEMENT_SPEED;
        Attribute a3 = Attribute.KNOCKBACK_RESISTANCE;
        AttributeModifier m1 = new AttributeModifier(NamespacedKey.randomKey(),1.5, AttributeModifier.Operation.ADD_NUMBER,EquipmentSlot.FEET.getGroup());
        AttributeModifier m2 = new AttributeModifier(NamespacedKey.randomKey(),-0.035, AttributeModifier.Operation.ADD_SCALAR,EquipmentSlot.FEET.getGroup());
        AttributeModifier m3 = new AttributeModifier(NamespacedKey.randomKey(),0.05, AttributeModifier.Operation.ADD_NUMBER,EquipmentSlot.FEET.getGroup());
        itemMeta.addAttributeModifier(a1,m1);
        itemMeta.addAttributeModifier(a2,m2);
        itemMeta.addAttributeModifier(a3,m3);
        ((LeatherArmorMeta)itemMeta).setColor(Color.GRAY);
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "圆石靴子");
        lore.add(ChatColor.WHITE + "非常厚实，非常重");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
    public ItemStack quartzHelm(){
        ItemStack item = new ItemStack(Material.IRON_HELMET);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.WHITE + "石英头盔");
        ((Damageable)itemMeta).setMaxDamage(170);
        Attribute a1 = Attribute.ARMOR;
        Attribute a2 = Attribute.ARMOR_TOUGHNESS;
        AttributeModifier m1 = new AttributeModifier(NamespacedKey.randomKey(),2, AttributeModifier.Operation.ADD_NUMBER,EquipmentSlot.HEAD.getGroup());
        AttributeModifier m2 = new AttributeModifier(NamespacedKey.randomKey(),1, AttributeModifier.Operation.ADD_NUMBER,EquipmentSlot.HEAD.getGroup());
        itemMeta.addAttributeModifier(a1,m1);
        itemMeta.addAttributeModifier(a2,m2);
        ((ArmorMeta)itemMeta).setTrim(new ArmorTrim(TrimMaterial.QUARTZ, TrimPattern.EYE));
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "石英头盔");
        lore.add(ChatColor.WHITE + "防御和韧性都适中");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
    public ItemStack quartzChest(){
        ItemStack item = new ItemStack(Material.IRON_CHESTPLATE);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.WHITE + "石英胸甲");
        ((Damageable)itemMeta).setMaxDamage(240);
        Attribute a1 = Attribute.ARMOR;
        Attribute a2 = Attribute.ARMOR_TOUGHNESS;
        AttributeModifier m1 = new AttributeModifier(NamespacedKey.randomKey(),6, AttributeModifier.Operation.ADD_NUMBER,EquipmentSlot.CHEST.getGroup());
        AttributeModifier m2 = new AttributeModifier(NamespacedKey.randomKey(),1.5, AttributeModifier.Operation.ADD_NUMBER,EquipmentSlot.CHEST.getGroup());
        itemMeta.addAttributeModifier(a1,m1);
        itemMeta.addAttributeModifier(a2,m2);
        ((ArmorMeta)itemMeta).setTrim(new ArmorTrim(TrimMaterial.QUARTZ, TrimPattern.SHAPER));
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "石英胸甲");
        lore.add(ChatColor.WHITE + "防御和韧性都适中");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
    public ItemStack quartzLeg(){
        ItemStack item = new ItemStack(Material.IRON_LEGGINGS);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.WHITE + "石英护腿");
        ((Damageable)itemMeta).setMaxDamage(220);
        Attribute a1 = Attribute.ARMOR;
        Attribute a2 = Attribute.ARMOR_TOUGHNESS;
        AttributeModifier m1 = new AttributeModifier(NamespacedKey.randomKey(),4, AttributeModifier.Operation.ADD_NUMBER,EquipmentSlot.LEGS.getGroup());
        AttributeModifier m2 = new AttributeModifier(NamespacedKey.randomKey(),1, AttributeModifier.Operation.ADD_NUMBER,EquipmentSlot.LEGS.getGroup());
        itemMeta.addAttributeModifier(a1,m1);
        itemMeta.addAttributeModifier(a2,m2);
        ((ArmorMeta)itemMeta).setTrim(new ArmorTrim(TrimMaterial.QUARTZ, TrimPattern.EYE));
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "石英护腿");
        lore.add(ChatColor.WHITE + "防御和韧性都适中");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
    public ItemStack quartzBoot(){
        ItemStack item = new ItemStack(Material.IRON_BOOTS);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.WHITE + "石英靴子");
        ((Damageable)itemMeta).setMaxDamage(190);
        Attribute a1 = Attribute.ARMOR;
        Attribute a2 = Attribute.ARMOR_TOUGHNESS;
        AttributeModifier m1 = new AttributeModifier(NamespacedKey.randomKey(),2, AttributeModifier.Operation.ADD_NUMBER,EquipmentSlot.FEET.getGroup());
        AttributeModifier m2 = new AttributeModifier(NamespacedKey.randomKey(),0.5, AttributeModifier.Operation.ADD_NUMBER,EquipmentSlot.FEET.getGroup());
        itemMeta.addAttributeModifier(a1,m1);
        itemMeta.addAttributeModifier(a2,m2);
        ((ArmorMeta)itemMeta).setTrim(new ArmorTrim(TrimMaterial.QUARTZ, TrimPattern.EYE));
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "石英靴子");
        lore.add(ChatColor.WHITE + "防御和韧性都适中");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
    public ItemStack phantomHelm(){
        ItemStack item = new ItemStack(Material.LEATHER_HELMET);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.WHITE + "幻翼头盔");
        ((Damageable)itemMeta).setMaxDamage(350);
        Attribute a1 = Attribute.ARMOR;
        AttributeModifier m1 = new AttributeModifier(NamespacedKey.randomKey(),2.5, AttributeModifier.Operation.ADD_NUMBER,EquipmentSlot.HEAD.getGroup());
        itemMeta.addAttributeModifier(a1,m1);
        ((LeatherArmorMeta)itemMeta).setColor(Color.fromRGB(35,61,87));
        ((ArmorMeta)itemMeta).setTrim(new ArmorTrim(TrimMaterial.EMERALD, TrimPattern.EYE));
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "幻翼头盔");
        lore.add(ChatColor.WHITE + "受到伤害会获得速度效果，无法叠加");
        lore.add(ChatColor.WHITE + "套装奖励：当护盾破碎时，会原地释放一次烟雾弹");
        lore.add(ChatColor.WHITE + "冷却时间30秒");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
    public ItemStack phantomChest(){
        ItemStack item = new ItemStack(Material.LEATHER_CHESTPLATE);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.WHITE + "幻翼胸甲");
        ((Damageable)itemMeta).setMaxDamage(510);
        Attribute a1 = Attribute.ARMOR;
        AttributeModifier m1 = new AttributeModifier(NamespacedKey.randomKey(),6, AttributeModifier.Operation.ADD_NUMBER,EquipmentSlot.CHEST.getGroup());
        itemMeta.addAttributeModifier(a1,m1);
        ((LeatherArmorMeta)itemMeta).setColor(Color.fromRGB(35,61,87));
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "幻翼胸甲");
        lore.add(ChatColor.WHITE + "受到伤害会获得速度效果，无法叠加");
        lore.add(ChatColor.WHITE + "套装奖励：当护盾破碎时，会原地释放一次烟雾弹");
        lore.add(ChatColor.WHITE + "冷却时间30秒");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
    public ItemStack phantomLeg(){
        ItemStack item = new ItemStack(Material.LEATHER_LEGGINGS);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.WHITE + "幻翼护腿");
        ((Damageable)itemMeta).setMaxDamage(480);
        Attribute a1 = Attribute.ARMOR;
        AttributeModifier m1 = new AttributeModifier(NamespacedKey.randomKey(),4.5, AttributeModifier.Operation.ADD_NUMBER,EquipmentSlot.LEGS.getGroup());
        itemMeta.addAttributeModifier(a1,m1);
        ((LeatherArmorMeta)itemMeta).setColor(Color.fromRGB(35,61,87));
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "幻翼护腿");
        lore.add(ChatColor.WHITE + "受到伤害会获得速度效果，无法叠加");
        lore.add(ChatColor.WHITE + "套装奖励：当护盾破碎时，会原地释放一次烟雾弹");
        lore.add(ChatColor.WHITE + "冷却时间30秒");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
    public ItemStack phantomBoot(){
        ItemStack item = new ItemStack(Material.LEATHER_BOOTS);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.WHITE + "幻翼靴子");
        ((Damageable)itemMeta).setMaxDamage(420);
        Attribute a1 = Attribute.ARMOR;
        AttributeModifier m1 = new AttributeModifier(NamespacedKey.randomKey(),2, AttributeModifier.Operation.ADD_NUMBER,EquipmentSlot.FEET.getGroup());
        itemMeta.addAttributeModifier(a1,m1);
        ((LeatherArmorMeta)itemMeta).setColor(Color.fromRGB(35,61,87));
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "幻翼靴子");
        lore.add(ChatColor.WHITE + "受到伤害会获得速度效果，无法叠加");
        lore.add(ChatColor.WHITE + "套装奖励：当护盾破碎时，会原地释放一次烟雾弹");
        lore.add(ChatColor.WHITE + "冷却时间30秒");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
    public ItemStack purpleHelm(){
        ItemStack item = new ItemStack(Material.LEATHER_HELMET);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.WHITE + "紫水晶头盔");
        ((Damageable)itemMeta).setMaxDamage(360);
        Attribute a1 = Attribute.ARMOR;
        Attribute a2 = Attribute.MOVEMENT_SPEED;
        AttributeModifier m1 = new AttributeModifier(NamespacedKey.randomKey(),2.5, AttributeModifier.Operation.ADD_NUMBER,EquipmentSlot.HEAD.getGroup());
        AttributeModifier m2 = new AttributeModifier(NamespacedKey.randomKey(),0.025, AttributeModifier.Operation.ADD_SCALAR,EquipmentSlot.HEAD.getGroup());
        itemMeta.addAttributeModifier(a1,m1);
        itemMeta.addAttributeModifier(a2,m2);
        ((LeatherArmorMeta)itemMeta).setColor(Color.fromRGB(206, 176, 220));
        ((ArmorMeta)itemMeta).setTrim(new ArmorTrim(TrimMaterial.AMETHYST, TrimPattern.SHAPER));
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "紫水晶头盔");
        lore.add(ChatColor.WHITE + "套装奖励：来自玩家的伤害减少25%");
        lore.add(ChatColor.WHITE + "当护盾破碎时，会释放一次破片");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
    public ItemStack purpleChest(){
        ItemStack item = new ItemStack(Material.LEATHER_CHESTPLATE);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.WHITE + "紫水晶胸甲");
        ((Damageable)itemMeta).setMaxDamage(520);
        Attribute a1 = Attribute.ARMOR;
        Attribute a2 = Attribute.MOVEMENT_SPEED;
        AttributeModifier m1 = new AttributeModifier(NamespacedKey.randomKey(),6, AttributeModifier.Operation.ADD_NUMBER,EquipmentSlot.CHEST.getGroup());
        AttributeModifier m2 = new AttributeModifier(NamespacedKey.randomKey(),0.025, AttributeModifier.Operation.ADD_SCALAR,EquipmentSlot.CHEST.getGroup());
        itemMeta.addAttributeModifier(a1,m1);
        itemMeta.addAttributeModifier(a2,m2);
        ((LeatherArmorMeta)itemMeta).setColor(Color.fromRGB(206, 176, 220));
        ((ArmorMeta)itemMeta).setTrim(new ArmorTrim(TrimMaterial.AMETHYST, TrimPattern.SHAPER));
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "紫水晶胸甲");
        lore.add(ChatColor.WHITE + "套装奖励：来自玩家的伤害减少25%");
        lore.add(ChatColor.WHITE + "当护盾破碎时，会释放一次破片");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
    public ItemStack purpleLeg(){
        ItemStack item = new ItemStack(Material.LEATHER_LEGGINGS);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.WHITE + "紫水晶护腿");
        ((Damageable)itemMeta).setMaxDamage(490);
        Attribute a1 = Attribute.ARMOR;
        Attribute a2 = Attribute.MOVEMENT_SPEED;
        AttributeModifier m1 = new AttributeModifier(NamespacedKey.randomKey(),5, AttributeModifier.Operation.ADD_NUMBER,EquipmentSlot.LEGS.getGroup());
        AttributeModifier m2 = new AttributeModifier(NamespacedKey.randomKey(),0.025, AttributeModifier.Operation.ADD_SCALAR,EquipmentSlot.LEGS.getGroup());
        itemMeta.addAttributeModifier(a1,m1);
        itemMeta.addAttributeModifier(a2,m2);
        ((LeatherArmorMeta)itemMeta).setColor(Color.fromRGB(206, 176, 220));
        ((ArmorMeta)itemMeta).setTrim(new ArmorTrim(TrimMaterial.AMETHYST, TrimPattern.SILENCE));
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "紫水晶护腿");
        lore.add(ChatColor.WHITE + "套装奖励：来自玩家的伤害减少25%");
        lore.add(ChatColor.WHITE + "当护盾破碎时，会释放一次破片");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
    public ItemStack purpleBoot(){
        ItemStack item = new ItemStack(Material.LEATHER_BOOTS);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.WHITE + "紫水晶靴子");
        ((Damageable)itemMeta).setMaxDamage(430);
        Attribute a1 = Attribute.ARMOR;
        Attribute a2 = Attribute.MOVEMENT_SPEED;
        AttributeModifier m1 = new AttributeModifier(NamespacedKey.randomKey(),2.5, AttributeModifier.Operation.ADD_NUMBER,EquipmentSlot.FEET.getGroup());
        AttributeModifier m2 = new AttributeModifier(NamespacedKey.randomKey(),0.025, AttributeModifier.Operation.ADD_SCALAR,EquipmentSlot.FEET.getGroup());
        itemMeta.addAttributeModifier(a1,m1);
        itemMeta.addAttributeModifier(a2,m2);
        ((LeatherArmorMeta)itemMeta).setColor(Color.fromRGB(206, 176, 220));
        ((ArmorMeta)itemMeta).setTrim(new ArmorTrim(TrimMaterial.AMETHYST, TrimPattern.SHAPER));
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "紫水晶靴子");
        lore.add(ChatColor.WHITE + "套装奖励：来自玩家的伤害减少25%");
        lore.add(ChatColor.WHITE + "当护盾破碎时，会释放一次破片");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
    public ItemStack greenHelm(){
        ItemStack item = new ItemStack(Material.LEATHER_HELMET);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.WHITE + "绿宝石头盔");
        ((Damageable)itemMeta).setMaxDamage(370);
        Attribute a1 = Attribute.ARMOR;
        Attribute a2 = Attribute.MAX_HEALTH;
        AttributeModifier m1 = new AttributeModifier(NamespacedKey.randomKey(),2.5, AttributeModifier.Operation.ADD_NUMBER,EquipmentSlot.HEAD.getGroup());
        AttributeModifier m2 = new AttributeModifier(NamespacedKey.randomKey(),1, AttributeModifier.Operation.ADD_NUMBER,EquipmentSlot.HEAD.getGroup());
        itemMeta.addAttributeModifier(a1,m1);
        itemMeta.addAttributeModifier(a2,m2);
        ((LeatherArmorMeta)itemMeta).setColor(Color.LIME);
        ((ArmorMeta)itemMeta).setTrim(new ArmorTrim(TrimMaterial.EMERALD, TrimPattern.SHAPER));
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "绿宝石头盔");
        lore.add(ChatColor.WHITE + "套装奖励：当护盾破碎时，会回复一定量的护盾");
        lore.add(ChatColor.WHITE + "冷却时间30秒");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
    public ItemStack greenChest(){
        ItemStack item = new ItemStack(Material.LEATHER_CHESTPLATE);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.WHITE + "绿宝石胸甲");
        ((Damageable)itemMeta).setMaxDamage(530);
        Attribute a1 = Attribute.ARMOR;
        Attribute a2 = Attribute.MAX_HEALTH;
        AttributeModifier m1 = new AttributeModifier(NamespacedKey.randomKey(),6.5, AttributeModifier.Operation.ADD_NUMBER,EquipmentSlot.CHEST.getGroup());
        AttributeModifier m2 = new AttributeModifier(NamespacedKey.randomKey(),1, AttributeModifier.Operation.ADD_NUMBER,EquipmentSlot.CHEST.getGroup());
        itemMeta.addAttributeModifier(a1,m1);
        itemMeta.addAttributeModifier(a2,m2);
        ((LeatherArmorMeta)itemMeta).setColor(Color.LIME);
        ((ArmorMeta)itemMeta).setTrim(new ArmorTrim(TrimMaterial.EMERALD, TrimPattern.SHAPER));
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "绿宝石胸甲");
        lore.add(ChatColor.WHITE + "套装奖励：当护盾破碎时，会回复一定量的护盾");
        lore.add(ChatColor.WHITE + "冷却时间30秒");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
    public ItemStack greenLeg(){
        ItemStack item = new ItemStack(Material.LEATHER_LEGGINGS);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.WHITE + "绿宝石护腿");
        ((Damageable)itemMeta).setMaxDamage(500);
        Attribute a1 = Attribute.ARMOR;
        Attribute a2 = Attribute.MAX_HEALTH;
        AttributeModifier m1 = new AttributeModifier(NamespacedKey.randomKey(),5.5, AttributeModifier.Operation.ADD_NUMBER,EquipmentSlot.LEGS.getGroup());
        AttributeModifier m2 = new AttributeModifier(NamespacedKey.randomKey(),1, AttributeModifier.Operation.ADD_NUMBER,EquipmentSlot.LEGS.getGroup());
        itemMeta.addAttributeModifier(a1,m1);
        itemMeta.addAttributeModifier(a2,m2);
        ((LeatherArmorMeta)itemMeta).setColor(Color.LIME);
        ((ArmorMeta)itemMeta).setTrim(new ArmorTrim(TrimMaterial.EMERALD, TrimPattern.SILENCE));
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "绿宝石护腿");
        lore.add(ChatColor.WHITE + "套装奖励：当护盾破碎时，会回复一定量的护盾");
        lore.add(ChatColor.WHITE + "冷却时间30秒");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
    public ItemStack greenBoot(){
        ItemStack item = new ItemStack(Material.LEATHER_BOOTS);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.WHITE + "绿宝石靴子");
        ((Damageable)itemMeta).setMaxDamage(430);
        Attribute a1 = Attribute.ARMOR;
        Attribute a2 = Attribute.MAX_HEALTH;
        AttributeModifier m1 = new AttributeModifier(NamespacedKey.randomKey(),2.5, AttributeModifier.Operation.ADD_NUMBER,EquipmentSlot.FEET.getGroup());
        AttributeModifier m2 = new AttributeModifier(NamespacedKey.randomKey(),1, AttributeModifier.Operation.ADD_NUMBER,EquipmentSlot.FEET.getGroup());
        itemMeta.addAttributeModifier(a1,m1);
        itemMeta.addAttributeModifier(a2,m2);
        ((LeatherArmorMeta)itemMeta).setColor(Color.LIME);
        ((ArmorMeta)itemMeta).setTrim(new ArmorTrim(TrimMaterial.EMERALD, TrimPattern.SHAPER));
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "绿宝石靴子");
        lore.add(ChatColor.WHITE + "套装奖励：当护盾破碎时，会回复一定量的护盾");
        lore.add(ChatColor.WHITE + "冷却时间30秒");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }

    public ItemStack obHelm(){
        ItemStack item = new ItemStack(Material.LEATHER_HELMET);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.WHITE + "黑曜石头盔");
        ((Damageable)itemMeta).setMaxDamage(400);
        Attribute a1 = Attribute.ARMOR;
        Attribute a2 = Attribute.MOVEMENT_SPEED;
        Attribute a3 = Attribute.KNOCKBACK_RESISTANCE;
        AttributeModifier m1 = new AttributeModifier(NamespacedKey.randomKey(),3, AttributeModifier.Operation.ADD_NUMBER,EquipmentSlot.HEAD.getGroup());
        AttributeModifier m2 = new AttributeModifier(NamespacedKey.randomKey(),-0.05, AttributeModifier.Operation.ADD_SCALAR,EquipmentSlot.HEAD.getGroup());
        AttributeModifier m3 = new AttributeModifier(NamespacedKey.randomKey(),0.2, AttributeModifier.Operation.ADD_NUMBER,EquipmentSlot.HEAD.getGroup());
        itemMeta.addAttributeModifier(a1,m1);
        itemMeta.addAttributeModifier(a2,m2);
        itemMeta.addAttributeModifier(a3,m3);
        ((LeatherArmorMeta)itemMeta).setColor(Color.fromRGB(61, 15, 68));
        ((ArmorMeta)itemMeta).setTrim(new ArmorTrim(TrimMaterial.AMETHYST, TrimPattern.RAISER));
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "黑曜石头盔");
        lore.add(ChatColor.WHITE + "套装奖励：当生命值满时，受到的伤害会大幅降低");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
    public ItemStack obChest(){
        ItemStack item = new ItemStack(Material.LEATHER_CHESTPLATE);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.WHITE + "黑曜石胸甲");
        ((Damageable)itemMeta).setMaxDamage(580);
        Attribute a1 = Attribute.ARMOR;
        Attribute a2 = Attribute.MOVEMENT_SPEED;
        Attribute a3 = Attribute.KNOCKBACK_RESISTANCE;
        AttributeModifier m1 = new AttributeModifier(NamespacedKey.randomKey(),7, AttributeModifier.Operation.ADD_NUMBER,EquipmentSlot.CHEST.getGroup());
        AttributeModifier m2 = new AttributeModifier(NamespacedKey.randomKey(),-0.05, AttributeModifier.Operation.ADD_SCALAR,EquipmentSlot.CHEST.getGroup());
        AttributeModifier m3 = new AttributeModifier(NamespacedKey.randomKey(),0.2, AttributeModifier.Operation.ADD_NUMBER,EquipmentSlot.CHEST.getGroup());
        itemMeta.addAttributeModifier(a1,m1);
        itemMeta.addAttributeModifier(a2,m2);
        itemMeta.addAttributeModifier(a3,m3);
        ((LeatherArmorMeta)itemMeta).setColor(Color.fromRGB(61, 15, 68));
        ((ArmorMeta)itemMeta).setTrim(new ArmorTrim(TrimMaterial.AMETHYST, TrimPattern.RAISER));
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "黑曜石胸甲");
        lore.add(ChatColor.WHITE + "套装奖励：当生命值满时，受到的伤害会大幅降低");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
    public ItemStack obLeg(){
        ItemStack item = new ItemStack(Material.LEATHER_LEGGINGS);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.WHITE + "黑曜石护腿");
        ((Damageable)itemMeta).setMaxDamage(540);
        Attribute a1 = Attribute.ARMOR;
        Attribute a2 = Attribute.MOVEMENT_SPEED;
        Attribute a3 = Attribute.KNOCKBACK_RESISTANCE;
        AttributeModifier m1 = new AttributeModifier(NamespacedKey.randomKey(),5, AttributeModifier.Operation.ADD_NUMBER,EquipmentSlot.LEGS.getGroup());
        AttributeModifier m2 = new AttributeModifier(NamespacedKey.randomKey(),-0.05, AttributeModifier.Operation.ADD_SCALAR,EquipmentSlot.LEGS.getGroup());
        AttributeModifier m3 = new AttributeModifier(NamespacedKey.randomKey(),0.2, AttributeModifier.Operation.ADD_NUMBER,EquipmentSlot.LEGS.getGroup());
        itemMeta.addAttributeModifier(a1,m1);
        itemMeta.addAttributeModifier(a2,m2);
        itemMeta.addAttributeModifier(a3,m3);
        ((LeatherArmorMeta)itemMeta).setColor(Color.fromRGB(61, 15, 68));
        ((ArmorMeta)itemMeta).setTrim(new ArmorTrim(TrimMaterial.AMETHYST, TrimPattern.SILENCE));
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "黑曜石护腿");
        lore.add(ChatColor.WHITE + "套装奖励：当生命值满时，受到的伤害会大幅降低");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
    public ItemStack obBoot(){
        ItemStack item = new ItemStack(Material.LEATHER_BOOTS);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.WHITE + "黑曜石靴子");
        ((Damageable)itemMeta).setMaxDamage(470);
        Attribute a1 = Attribute.ARMOR;
        Attribute a2 = Attribute.MOVEMENT_SPEED;
        Attribute a3 = Attribute.KNOCKBACK_RESISTANCE;
        AttributeModifier m1 = new AttributeModifier(NamespacedKey.randomKey(),3, AttributeModifier.Operation.ADD_NUMBER,EquipmentSlot.FEET.getGroup());
        AttributeModifier m2 = new AttributeModifier(NamespacedKey.randomKey(),-0.05, AttributeModifier.Operation.ADD_SCALAR,EquipmentSlot.FEET.getGroup());
        AttributeModifier m3 = new AttributeModifier(NamespacedKey.randomKey(),0.2, AttributeModifier.Operation.ADD_NUMBER,EquipmentSlot.FEET.getGroup());
        itemMeta.addAttributeModifier(a1,m1);
        itemMeta.addAttributeModifier(a2,m2);
        itemMeta.addAttributeModifier(a3,m3);
        ((LeatherArmorMeta)itemMeta).setColor(Color.fromRGB(61, 15, 68));
        ((ArmorMeta)itemMeta).setTrim(new ArmorTrim(TrimMaterial.AMETHYST, TrimPattern.RAISER));
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "黑曜石靴子");
        lore.add(ChatColor.WHITE + "套装奖励：当生命值满时，受到的伤害会大幅降低");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
    public ItemStack echoHelm(){
        ItemStack item = new ItemStack(Material.LEATHER_HELMET);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.WHITE + "回响头盔");
        ((Damageable)itemMeta).setMaxDamage(400);
        Attribute a1 = Attribute.ARMOR;
        Attribute a2 = Attribute.ARMOR_TOUGHNESS;
        AttributeModifier m1 = new AttributeModifier(NamespacedKey.randomKey(),3, AttributeModifier.Operation.ADD_NUMBER,EquipmentSlot.HEAD.getGroup());
        AttributeModifier m2 = new AttributeModifier(NamespacedKey.randomKey(),2, AttributeModifier.Operation.ADD_NUMBER,EquipmentSlot.HEAD.getGroup());
        itemMeta.addAttributeModifier(a1,m1);
        itemMeta.addAttributeModifier(a2,m2);
        ((LeatherArmorMeta)itemMeta).setColor(Color.fromRGB(10,45,74));
        ((ArmorMeta)itemMeta).setTrim(new ArmorTrim(TrimMaterial.DIAMOND, TrimPattern.SILENCE));
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "回响头盔");
        lore.add(ChatColor.WHITE + "套装奖励：来自怪物的伤害减少50%");
        lore.add(ChatColor.WHITE + "当护盾破碎时，会释放一次诱捕手雷");
        lore.add(ChatColor.WHITE + "冷却时间30秒");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
    public ItemStack echoChest(){
        ItemStack item = new ItemStack(Material.LEATHER_CHESTPLATE);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.WHITE + "回响胸甲");
        ((Damageable)itemMeta).setMaxDamage(590);
        Attribute a1 = Attribute.ARMOR;
        Attribute a2 = Attribute.ARMOR_TOUGHNESS;
        AttributeModifier m1 = new AttributeModifier(NamespacedKey.randomKey(),8, AttributeModifier.Operation.ADD_NUMBER,EquipmentSlot.CHEST.getGroup());
        AttributeModifier m2 = new AttributeModifier(NamespacedKey.randomKey(),3.5, AttributeModifier.Operation.ADD_NUMBER,EquipmentSlot.CHEST.getGroup());
        itemMeta.addAttributeModifier(a1,m1);
        itemMeta.addAttributeModifier(a2,m2);
        ((LeatherArmorMeta)itemMeta).setColor(Color.fromRGB(10,45,74));
        ((ArmorMeta)itemMeta).setTrim(new ArmorTrim(TrimMaterial.DIAMOND, TrimPattern.SILENCE));
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "回响胸甲");
        lore.add(ChatColor.WHITE + "套装奖励：来自怪物的伤害减少50%");
        lore.add(ChatColor.WHITE + "当护盾破碎时，会释放一次诱捕手雷");
        lore.add(ChatColor.WHITE + "冷却时间30秒");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
    public ItemStack echoLeg(){
        ItemStack item = new ItemStack(Material.LEATHER_LEGGINGS);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.WHITE + "回响护腿");
        ((Damageable)itemMeta).setMaxDamage(550);
        Attribute a1 = Attribute.ARMOR;
        Attribute a2 = Attribute.ARMOR_TOUGHNESS;
        AttributeModifier m1 = new AttributeModifier(NamespacedKey.randomKey(),6, AttributeModifier.Operation.ADD_NUMBER,EquipmentSlot.LEGS.getGroup());
        AttributeModifier m2 = new AttributeModifier(NamespacedKey.randomKey(),3, AttributeModifier.Operation.ADD_NUMBER,EquipmentSlot.LEGS.getGroup());
        itemMeta.addAttributeModifier(a1,m1);
        itemMeta.addAttributeModifier(a2,m2);
        ((LeatherArmorMeta)itemMeta).setColor(Color.fromRGB(10,45,74));
        ((ArmorMeta)itemMeta).setTrim(new ArmorTrim(TrimMaterial.DIAMOND, TrimPattern.SILENCE));
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "回响护腿");
        lore.add(ChatColor.WHITE + "套装奖励：来自怪物的伤害减少50%");
        lore.add(ChatColor.WHITE + "当护盾破碎时，会释放一次诱捕手雷");
        lore.add(ChatColor.WHITE + "冷却时间30秒");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
    public ItemStack echoBoot(){
        ItemStack item = new ItemStack(Material.LEATHER_BOOTS);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.WHITE + "回响靴子");
        ((Damageable)itemMeta).setMaxDamage(480);
        Attribute a1 = Attribute.ARMOR;
        Attribute a2 = Attribute.ARMOR_TOUGHNESS;
        AttributeModifier m1 = new AttributeModifier(NamespacedKey.randomKey(),3, AttributeModifier.Operation.ADD_NUMBER,EquipmentSlot.FEET.getGroup());
        AttributeModifier m2 = new AttributeModifier(NamespacedKey.randomKey(),1.5, AttributeModifier.Operation.ADD_NUMBER,EquipmentSlot.FEET.getGroup());
        itemMeta.addAttributeModifier(a1,m1);
        itemMeta.addAttributeModifier(a2,m2);
        ((LeatherArmorMeta)itemMeta).setColor(Color.fromRGB(10,45,74));
        ((ArmorMeta)itemMeta).setTrim(new ArmorTrim(TrimMaterial.DIAMOND, TrimPattern.SILENCE));
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "回响靴子");
        lore.add(ChatColor.WHITE + "套装奖励：来自怪物的伤害减少50%");
        lore.add(ChatColor.WHITE + "当护盾破碎时，会释放一次诱捕手雷");
        lore.add(ChatColor.WHITE + "冷却时间30秒");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }

    public ItemStack mobHelm(Color c){
        ItemStack item = new ItemStack(Material.LEATHER_HELMET);
        LeatherArmorMeta itemMeta = (LeatherArmorMeta) item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.WHITE + "怪物头盔");
        Attribute a1 = Attribute.ARMOR;
        AttributeModifier m1 = new AttributeModifier(NamespacedKey.randomKey(),0, AttributeModifier.Operation.ADD_NUMBER,EquipmentSlot.HEAD.getGroup());
        itemMeta.addAttributeModifier(a1,m1);
        itemMeta.setUnbreakable(true);
        itemMeta.setColor(c);
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "给怪物用的装饰性护甲");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
    public ItemStack mobChest(Color c){
        ItemStack item = new ItemStack(Material.LEATHER_CHESTPLATE);
        LeatherArmorMeta itemMeta = (LeatherArmorMeta) item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.WHITE + "怪物胸甲");
        Attribute a1 = Attribute.ARMOR;
        AttributeModifier m1 = new AttributeModifier(NamespacedKey.randomKey(),0, AttributeModifier.Operation.ADD_NUMBER,EquipmentSlot.CHEST.getGroup());
        itemMeta.addAttributeModifier(a1,m1);
        itemMeta.setUnbreakable(true);
        itemMeta.setColor(c);
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "给怪物用的装饰性护甲");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
    public ItemStack mobLeg(Color c){
        ItemStack item = new ItemStack(Material.LEATHER_LEGGINGS);
        LeatherArmorMeta itemMeta = (LeatherArmorMeta) item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.WHITE + "怪物护腿");
        Attribute a1 = Attribute.ARMOR;
        AttributeModifier m1 = new AttributeModifier(NamespacedKey.randomKey(),0, AttributeModifier.Operation.ADD_NUMBER,EquipmentSlot.LEGS.getGroup());
        itemMeta.addAttributeModifier(a1,m1);
        itemMeta.setUnbreakable(true);
        itemMeta.setColor(c);
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "给怪物用的装饰性护甲");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
    public ItemStack mobBoot(Color c){
        ItemStack item = new ItemStack(Material.LEATHER_BOOTS);
        LeatherArmorMeta itemMeta = (LeatherArmorMeta) item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.WHITE + "怪物靴子");
        Attribute a1 = Attribute.ARMOR;
        AttributeModifier m1 = new AttributeModifier(NamespacedKey.randomKey(),0, AttributeModifier.Operation.ADD_NUMBER,EquipmentSlot.FEET.getGroup());
        itemMeta.addAttributeModifier(a1,m1);
        itemMeta.setUnbreakable(true);
        itemMeta.setColor(c);
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "给怪物用的装饰性护甲");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
}

package Universal;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public enum LootPool {
    INSTANCE;
    Random r = new Random();
    ItemStack[] commonItem = {
            i0(),
            i0(),
            i1(),
            i1(),
            i2(),
            i2(),
            i3(),
            i3(),
            i4(),
            i4(),
            i5(),
            i5(),
            i6(),
            i6(),
            i7(),
            i7(),
            i8(),
            i8(),
            i9(),
            i9(),
            i10(),
            i10(),
            i11(),
            i11(),
            i12(),
            i12(),
            i13(),
            i13(),
            i14(),
            i14(),
            i15(),
            i15(),
            i16(),
            i17(),
            i18(),
            i19(),
            i20(),
            i21(),
            i22(),
            i22(),
            i23(),
            i23(),
            i24(),
            i24(),
            i25(),
            i25(),
            i26(),
            i26(),
    };
    ItemStack[] uncommonItem = {
            i27(),
            i27(),
            i28(),
            i28(),
            i29(),
            i29(),
            i30(),
            i30(),
            i31(),
            i31(),
            i32(),
            i32(),
            i33(),
            i33(),
            i34(),
            i34(),
            i35(),
            i35(),
            i36(),
            i36(),
            i37(),
            i37(),
            i38(),
            i39(),
            i40(),
            i40(),
            i41(),
            i42(),
            i43(),
            i44(),
            i45(),
            i46(),
            i47(),
            i48(),
            i49(),
            i50(),
            i51(),
            i52(),
            i53(),
            i104(),
            i105(),
            i106(),
            i107(),
            i110(),
            i110(),
            i111(),
            i111(),
    };
    ItemStack[] rareItem = {
            i54(),
            i54(),
            i54(),
            i55(),
            i55(),
            i56(),
            i56(),
            i57(),
            i57(),
            i58(),
            i58(),
            i59(),
            i60(),
            i61(),
            i62(),
            i63(),
            i64(),
            i65(),
            i66(),
            i67(),
            i67(),
            i67(),
            i75(),
            i108(),
    };
    ItemStack[] epicItem = {
            i68(),
            i68(),
            i69(),
            i69(),
            i70(),
            i70(),
            i71(),
            i72(),
            i73(),
            i74(),
            i76(),
            i77(),
            i78(),
            i79(),
            i80(),
            i81(),
            i82(),
            i109(),
    };
    ItemStack[] legendaryItem = {
            i83(),
            i84(),
            i85(),
            i86(),
            i87(),
            i88(),
            i88(),
            i89(),
            i89(),
            i90(),
            i90(),
            i91(),
            i92(),
            i93(),
            i93(),
            i94(),
            i95(),
    };
    ItemStack[] mysticItem = {
            i96(),
            i97(),
            i98(),
            i99(),
            i100(),
            i101(),
            i102(),
            i103(),
    };
    ItemStack[] keys = {
            i62(),
            k0(),
            k0(),
            k0(),
            k0(),
            k1(),
            k1(),
            k1(),
            k2(),
            k2(),
            k3(),
    };
    ItemStack[] boxes = {
            i47(),
            i48(),
            i64(),
            i65(),
            i66(),
            i67(),
            i76(),
            i77(),
            i78(),
            i79(),
            i80(),
            i81(),
            i82(),
            i95(),
    };
    double[] prices = {
            0.5,1,3,10,35,150
    };

    public double[] getPrices() {
        return prices;
    }

    public ItemStack[] getKeys() {
        return keys.clone();
    }

    public ItemStack[] getBoxes() {
        return boxes.clone();
    }

    public void calculate(float[] chancePool, float[] chances) {
        float f = 0;
        for (int i = 0; i < chancePool.length; i++) {
            f = f + chancePool[i];
            chances[i] = f;
        }
        if (f != 1) {
            for (int i = 0; i < chances.length; i++) {
                chances[i] = chances[i] / f;
            }
        }
    }

    public int compare(float[] chances, float randFloat) {
        for (int i = 0; i < chances.length; i++) {
            if (randFloat < chances[i]) {
                return i;
            }
        }
        return -1;
    }
    public ItemStack[] getAllLoots(){
        List<ItemStack> content = new ArrayList<>();
        content.addAll(List.of(commonItem));
        content.addAll(List.of(uncommonItem));
        content.addAll(List.of(rareItem));
        content.addAll(List.of(epicItem));
        content.addAll(List.of(legendaryItem));
        content.addAll(List.of(mysticItem));
        content.add(i112());
        content.add(i113());
        return content.toArray(new ItemStack[0]).clone();
    }

    public ItemStack[] getContent(int counts, float[] weights) {
        List<ItemStack> content = new ArrayList<>();
        float[] chances = new float[weights.length];
        calculate(weights, chances);
        for (int i = 0; i < counts; i++) {
            float randFloat = r.nextFloat();
            int result = compare(chances, randFloat);
            if (result < 0) {
                Bukkit.getLogger().info(ChatColor.RED + "ItemPool出错！result < 0");
                continue;
            }
            ItemStack randItem = switch (result) {
                case 1 -> uncommonItem[r.nextInt(uncommonItem.length)];
                case 2 -> rareItem[r.nextInt(rareItem.length)];
                case 3 -> epicItem[r.nextInt(epicItem.length)];
                case 4 -> legendaryItem[r.nextInt(legendaryItem.length)];
                case 5 -> mysticItem[r.nextInt(mysticItem.length)];
                default -> commonItem[r.nextInt(commonItem.length)];
            };
            content.add(randItem);
        }
        return content.toArray(new ItemStack[0]);
    }

    public int getRarity(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        String name = meta.getDisplayName();
        if (!name.contains("§")) return -1;
        String cutName = name.substring(0, 3);
        return switch (cutName) {
            case "§7【" -> 0;
            case "§a【" -> 1;
            case "§b【" -> 2;
            case "§d【" -> 3;
            case "§6【" -> 4;
            case "§c【" -> 5;
            case "§4【" -> 6;
            default -> -1;
        };
    }
    public ItemStack pageUp() {
        ItemStack item = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.RED + "上一页");
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "点击回到上一页");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
    public ItemStack pageDown() {
        ItemStack item = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.GREEN + "下一页");
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "点击去到下一页");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
    public ItemStack close() {
        ItemStack item = new ItemStack(Material.BARRIER);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.YELLOW + "返回主菜单");
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "点击返回主菜单");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }

    public ItemStack k0() {
        ItemStack i = new ItemStack(Material.COPPER_TORCH);
        ItemMeta meta = i.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        meta.setMaxStackSize(16);
        meta.setDisplayName(ChatColor.GREEN + "森林钥匙");
        lore.add(ChatColor.WHITE + "一次性钥匙");
        lore.add(ChatColor.WHITE + "能够打开绿色的门");
        lore.add(ChatColor.BLACK + "EMERALD_BLOCK");
        meta.setLore(lore);
        i.setItemMeta(meta);
        return i.clone();
    }

    public ItemStack k1() {
        ItemStack i = new ItemStack(Material.TORCH);
        ItemMeta meta = i.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        meta.setMaxStackSize(16);
        meta.setDisplayName(ChatColor.YELLOW + "沙漠钥匙");
        lore.add(ChatColor.WHITE + "一次性钥匙");
        lore.add(ChatColor.WHITE + "能够打开金色的门");
        lore.add(ChatColor.BLACK + "GOLD_BLOCK");
        meta.setLore(lore);
        i.setItemMeta(meta);
        return i.clone();
    }

    public ItemStack k2() {
        ItemStack i = new ItemStack(Material.SOUL_TORCH);
        ItemMeta meta = i.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        meta.setMaxStackSize(16);
        meta.setDisplayName(ChatColor.AQUA + "海洋钥匙");
        lore.add(ChatColor.WHITE + "一次性钥匙");
        lore.add(ChatColor.WHITE + "能够打开蓝色的门");
        lore.add(ChatColor.BLACK + "DIAMOND_BLOCK");
        meta.setLore(lore);
        i.setItemMeta(meta);
        return i.clone();
    }

    public ItemStack k3() {
        ItemStack i = new ItemStack(Material.REDSTONE_TORCH);
        ItemMeta meta = i.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        meta.setMaxStackSize(16);
        meta.setDisplayName(ChatColor.RED + "下界钥匙");
        lore.add(ChatColor.WHITE + "一次性钥匙");
        lore.add(ChatColor.WHITE + "能够打开红色的门");
        lore.add(ChatColor.BLACK + "REDSTONE_BLOCK");
        meta.setLore(lore);
        i.setItemMeta(meta);
        return i.clone();
    }


    public ItemStack i0() {
        ItemStack i = new ItemStack(Material.COBBLESTONE);
        ItemMeta meta = i.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        meta.setMaxStackSize(16);
        meta.setDisplayName(ChatColor.GRAY + "【普通】圆石");
        lore.add(ChatColor.WHITE + "随处可见的石头");
        meta.setLore(lore);
        i.setItemMeta(meta);
        return i.clone();
    }

    public ItemStack i1() {
        ItemStack i = new ItemStack(Material.COBBLED_DEEPSLATE);
        ItemMeta meta = i.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        meta.setMaxStackSize(16);
        meta.setDisplayName(ChatColor.GRAY + "【普通】深板岩圆石");
        lore.add(ChatColor.WHITE + "还是圆石，但是黑了点");
        meta.setLore(lore);
        i.setItemMeta(meta);
        return i.clone();
    }

    public ItemStack i2() {
        ItemStack i = new ItemStack(Material.BLACKSTONE);
        ItemMeta meta = i.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        meta.setMaxStackSize(16);
        meta.setDisplayName(ChatColor.GRAY + "【普通】黑石");
        lore.add(ChatColor.WHITE + "就是内个石头");
        meta.setLore(lore);
        i.setItemMeta(meta);
        return i.clone();
    }

    public ItemStack i3() {
        ItemStack i = new ItemStack(Material.ANDESITE);
        ItemMeta meta = i.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        meta.setMaxStackSize(16);
        meta.setDisplayName(ChatColor.GRAY + "【普通】安山岩");
        lore.add(ChatColor.WHITE + "如果有机械动力的话...");
        meta.setLore(lore);
        i.setItemMeta(meta);
        return i.clone();
    }

    public ItemStack i4() {
        ItemStack i = new ItemStack(Material.DIORITE);
        ItemMeta meta = i.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        meta.setMaxStackSize(16);
        meta.setDisplayName(ChatColor.GRAY + "【普通】闪长岩");
        lore.add(ChatColor.WHITE + "这是什么？哦，闪长岩");
        meta.setLore(lore);
        i.setItemMeta(meta);
        return i.clone();
    }

    public ItemStack i5() {
        ItemStack i = new ItemStack(Material.GRANITE);
        ItemMeta meta = i.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        meta.setMaxStackSize(16);
        meta.setDisplayName(ChatColor.GRAY + "【普通】花岗岩");
        lore.add(ChatColor.WHITE + "至少能拿来做建筑材料");
        meta.setLore(lore);
        i.setItemMeta(meta);
        return i.clone();
    }

    public ItemStack i6() {
        ItemStack i = new ItemStack(Material.CALCITE);
        ItemMeta meta = i.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        meta.setMaxStackSize(16);
        meta.setDisplayName(ChatColor.GRAY + "【普通】方解石");
        lore.add(ChatColor.WHITE + "不要与闪长岩搞混");
        meta.setLore(lore);
        i.setItemMeta(meta);
        return i.clone();
    }

    public ItemStack i7() {
        ItemStack i = new ItemStack(Material.TUFF);
        ItemMeta meta = i.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        meta.setMaxStackSize(16);
        meta.setDisplayName(ChatColor.GRAY + "【普通】凝灰岩");
        lore.add(ChatColor.WHITE + "存在感很低");
        meta.setLore(lore);
        i.setItemMeta(meta);
        return i.clone();
    }

    public ItemStack i8() {
        ItemStack i = new ItemStack(Material.SAND);
        ItemMeta meta = i.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        meta.setMaxStackSize(16);
        meta.setDisplayName(ChatColor.GRAY + "【普通】沙子");
        lore.add(ChatColor.WHITE + "形态不固定，容易随风飘散");
        lore.add(ChatColor.WHITE + "还好这游戏没有风");
        meta.setLore(lore);
        i.setItemMeta(meta);
        return i.clone();
    }

    public ItemStack i9() {
        ItemStack i = new ItemStack(Material.RED_SAND);
        ItemMeta meta = i.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        meta.setMaxStackSize(16);
        meta.setDisplayName(ChatColor.GRAY + "【普通】红沙");
        lore.add(ChatColor.WHITE + "红温的沙子");
        meta.setLore(lore);
        i.setItemMeta(meta);
        return i.clone();
    }

    public ItemStack i10() {
        ItemStack i = new ItemStack(Material.CLAY);
        ItemMeta meta = i.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        meta.setMaxStackSize(16);
        meta.setDisplayName(ChatColor.GRAY + "【普通】粘土");
        lore.add(ChatColor.WHITE + "非常基础的材料");
        meta.setLore(lore);
        i.setItemMeta(meta);
        return i.clone();
    }

    public ItemStack i11() {
        ItemStack i = new ItemStack(Material.NETHERRACK);
        ItemMeta meta = i.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        meta.setMaxStackSize(16);
        meta.setDisplayName(ChatColor.GRAY + "【普通】下界岩");
        lore.add(ChatColor.WHITE + "通常可以在壁炉里找到");
        meta.setLore(lore);
        i.setItemMeta(meta);
        return i.clone();
    }

    public ItemStack i12() {
        ItemStack i = new ItemStack(Material.END_STONE);
        ItemMeta meta = i.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        meta.setMaxStackSize(16);
        meta.setDisplayName(ChatColor.GRAY + "【普通】末地石");
        lore.add(ChatColor.WHITE + "归根到底还是圆石");
        meta.setLore(lore);
        i.setItemMeta(meta);
        return i.clone();
    }

    public ItemStack i13() {
        ItemStack i = new ItemStack(Material.DRIPSTONE_BLOCK);
        ItemMeta meta = i.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        meta.setMaxStackSize(16);
        meta.setDisplayName(ChatColor.GRAY + "【普通】滴水石块");
        lore.add(ChatColor.WHITE + "就不能把这个砸成滴水石锥吗");
        meta.setLore(lore);
        i.setItemMeta(meta);
        return i.clone();
    }

    public ItemStack i14() {
        ItemStack i = new ItemStack(Material.BASALT);
        ItemMeta meta = i.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        meta.setMaxStackSize(16);
        meta.setDisplayName(ChatColor.GRAY + "【普通】玄武岩");
        lore.add(ChatColor.WHITE + "可以用作建材");
        meta.setLore(lore);
        i.setItemMeta(meta);
        return i.clone();
    }

    public ItemStack i15() {
        ItemStack i = new ItemStack(Material.MUD);
        ItemMeta meta = i.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        meta.setMaxStackSize(16);
        meta.setDisplayName(ChatColor.GRAY + "【普通】泥巴");
        lore.add(ChatColor.WHITE + "我在东北玩泥巴");
        meta.setLore(lore);
        i.setItemMeta(meta);
        return i.clone();
    }

    public ItemStack i16() {
        ItemStack i = new ItemStack(Material.BREAD);
        ItemMeta meta = i.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        meta.setMaxStackSize(16);
        meta.setDisplayName(ChatColor.GRAY + "【普通】面包");
        lore.add(ChatColor.WHITE + "方便携带的干粮");
        meta.setLore(lore);
        i.setItemMeta(meta);
        return i.clone();
    }

    public ItemStack i17() {
        ItemStack i = new ItemStack(Material.COD);
        ItemMeta meta = i.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        meta.setMaxStackSize(16);
        meta.setDisplayName(ChatColor.GRAY + "【普通】生鳕鱼");
        lore.add(ChatColor.WHITE + "跟它不太熟，不建议生吃");
        meta.setLore(lore);
        i.setItemMeta(meta);
        return i.clone();
    }

    public ItemStack i18() {
        ItemStack i = new ItemStack(Material.DRIED_KELP);
        ItemMeta meta = i.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        meta.setMaxStackSize(16);
        meta.setDisplayName(ChatColor.GRAY + "【普通】干海带");
        lore.add(ChatColor.WHITE + "什么香香脆脆我们都爱");
        meta.setLore(lore);
        i.setItemMeta(meta);
        return i.clone();
    }

    public ItemStack i19() {
        ItemStack i = new ItemStack(Material.COPPER_NUGGET);
        ItemMeta meta = i.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        meta.setMaxStackSize(16);
        meta.setDisplayName(ChatColor.GRAY + "【普通】铜粒");
        lore.add(ChatColor.WHITE + "一小粒的铜，可以攒起来合成铜锭");
        meta.setLore(lore);
        i.setItemMeta(meta);
        return i.clone();
    }

    public ItemStack i20() {
        ItemStack i = new ItemStack(Material.IRON_NUGGET);
        ItemMeta meta = i.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        meta.setMaxStackSize(16);
        meta.setDisplayName(ChatColor.GRAY + "【普通】铁粒");
        lore.add(ChatColor.WHITE + "一小粒的铁，可以攒起来合成铁锭");
        meta.setLore(lore);
        i.setItemMeta(meta);
        return i.clone();
    }

    public ItemStack i21() {
        ItemStack i = new ItemStack(Material.GOLD_NUGGET);
        ItemMeta meta = i.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        meta.setMaxStackSize(16);
        meta.setDisplayName(ChatColor.GRAY + "【普通】金粒");
        lore.add(ChatColor.WHITE + "一小粒金子，可以攒起来合成金锭");
        meta.setLore(lore);
        i.setItemMeta(meta);
        return i.clone();
    }

    public ItemStack i22() {
        ItemStack i = new ItemStack(Material.STICK);
        ItemMeta meta = i.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        meta.setMaxStackSize(16);
        meta.setDisplayName(ChatColor.GRAY + "【普通】木棍");
        lore.add(ChatColor.WHITE + "不是林昆，也不是棍木");
        meta.setLore(lore);
        i.setItemMeta(meta);
        return i.clone();
    }

    public ItemStack i23() {
        ItemStack i = new ItemStack(Material.BOWL);
        ItemMeta meta = i.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        meta.setMaxStackSize(16);
        meta.setDisplayName(ChatColor.GRAY + "【普通】碗");
        lore.add(ChatColor.WHITE + "一只空碗，擦擦灰尘还能用");
        meta.setLore(lore);
        i.setItemMeta(meta);
        return i.clone();
    }

    public ItemStack i24() {
        ItemStack i = new ItemStack(Material.GLASS_BOTTLE);
        ItemMeta meta = i.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        meta.setMaxStackSize(16);
        meta.setDisplayName(ChatColor.GRAY + "【普通】玻璃瓶");
        lore.add(ChatColor.WHITE + "一个空瓶子，可以装液体");
        meta.setLore(lore);
        i.setItemMeta(meta);
        return i.clone();
    }

    public ItemStack i25() {
        ItemStack i = new ItemStack(Material.COBWEB);
        ItemMeta meta = i.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        meta.setMaxStackSize(16);
        meta.setDisplayName(ChatColor.GRAY + "【普通】蜘蛛网");
        lore.add(ChatColor.WHITE + "这个容器很久没动过了，以至于出现了这东西");
        meta.setLore(lore);
        i.setItemMeta(meta);
        return i.clone();
    }

    public ItemStack i26() {
        ItemStack i = new ItemStack(Material.POISONOUS_POTATO);
        ItemMeta meta = i.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        meta.setMaxStackSize(16);
        meta.setDisplayName(ChatColor.GRAY + "【普通】毒马铃薯");
        lore.add(ChatColor.WHITE + "除非实在没有东西吃了，不然不建议吃");
        meta.setLore(lore);
        i.setItemMeta(meta);
        return i.clone();
    }

    public ItemStack i27() {
        ItemStack i = new ItemStack(Material.OAK_LOG);
        ItemMeta meta = i.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        meta.setMaxStackSize(8);
        meta.setDisplayName(ChatColor.GREEN + "【寻常】橡木原木");
        lore.add(ChatColor.WHITE + "刚砍下来的原木，用途很广");
        meta.setLore(lore);
        i.setItemMeta(meta);
        return i.clone();
    }

    public ItemStack i28() {
        ItemStack i = new ItemStack(Material.BIRCH_LOG);
        ItemMeta meta = i.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        meta.setMaxStackSize(8);
        meta.setDisplayName(ChatColor.GREEN + "【寻常】白桦原木");
        lore.add(ChatColor.WHITE + "刚砍下来的原木，用途很广");
        meta.setLore(lore);
        i.setItemMeta(meta);
        return i.clone();
    }

    public ItemStack i29() {
        ItemStack i = new ItemStack(Material.SPRUCE_LOG);
        ItemMeta meta = i.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        meta.setMaxStackSize(8);
        meta.setDisplayName(ChatColor.GREEN + "【寻常】云杉原木");
        lore.add(ChatColor.WHITE + "刚砍下来的原木，用途很广");
        meta.setLore(lore);
        i.setItemMeta(meta);
        return i.clone();
    }

    public ItemStack i30() {
        ItemStack i = new ItemStack(Material.DARK_OAK_LOG);
        ItemMeta meta = i.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        meta.setMaxStackSize(8);
        meta.setDisplayName(ChatColor.GREEN + "【寻常】深色橡木原木");
        lore.add(ChatColor.WHITE + "刚砍下来的原木，用途很广");
        meta.setLore(lore);
        i.setItemMeta(meta);
        return i.clone();
    }

    public ItemStack i31() {
        ItemStack i = new ItemStack(Material.JUNGLE_LOG);
        ItemMeta meta = i.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        meta.setMaxStackSize(8);
        meta.setDisplayName(ChatColor.GREEN + "【寻常】丛林原木");
        lore.add(ChatColor.WHITE + "刚砍下来的原木，用途很广");
        meta.setLore(lore);
        i.setItemMeta(meta);
        return i.clone();
    }

    public ItemStack i32() {
        ItemStack i = new ItemStack(Material.ACACIA_LOG);
        ItemMeta meta = i.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        meta.setMaxStackSize(8);
        meta.setDisplayName(ChatColor.GREEN + "【寻常】金合欢原木");
        lore.add(ChatColor.WHITE + "刚砍下来的原木，用途很广");
        meta.setLore(lore);
        i.setItemMeta(meta);
        return i.clone();
    }

    public ItemStack i33() {
        ItemStack i = new ItemStack(Material.PALE_OAK_LOG);
        ItemMeta meta = i.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        meta.setMaxStackSize(8);
        meta.setDisplayName(ChatColor.GREEN + "【寻常】苍白原木");
        lore.add(ChatColor.WHITE + "刚砍下来的原木，用途很广");
        meta.setLore(lore);
        i.setItemMeta(meta);
        return i.clone();
    }

    public ItemStack i34() {
        ItemStack i = new ItemStack(Material.CHERRY_LOG);
        ItemMeta meta = i.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        meta.setMaxStackSize(8);
        meta.setDisplayName(ChatColor.GREEN + "【寻常】樱花原木");
        lore.add(ChatColor.WHITE + "刚砍下来的原木，用途很广");
        meta.setLore(lore);
        i.setItemMeta(meta);
        return i.clone();
    }

    public ItemStack i35() {
        ItemStack i = new ItemStack(Material.MANGROVE_LOG);
        ItemMeta meta = i.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        meta.setMaxStackSize(8);
        meta.setDisplayName(ChatColor.GREEN + "【寻常】红树原木");
        lore.add(ChatColor.WHITE + "刚砍下来的原木，用途很广");
        meta.setLore(lore);
        i.setItemMeta(meta);
        return i.clone();
    }

    public ItemStack i36() {
        ItemStack i = new ItemStack(Material.CRIMSON_STEM);
        ItemMeta meta = i.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        meta.setMaxStackSize(8);
        meta.setDisplayName(ChatColor.GREEN + "【寻常】绯红菌柄");
        lore.add(ChatColor.WHITE + "刚砍下来的原木(？)，用途很广");
        meta.setLore(lore);
        i.setItemMeta(meta);
        return i.clone();
    }

    public ItemStack i37() {
        ItemStack i = new ItemStack(Material.WARPED_STEM);
        ItemMeta meta = i.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        meta.setMaxStackSize(8);
        meta.setDisplayName(ChatColor.GREEN + "【寻常】诡异菌柄");
        lore.add(ChatColor.WHITE + "刚砍下来的原木(？)，用途很广");
        meta.setLore(lore);
        i.setItemMeta(meta);
        return i.clone();
    }

    public ItemStack i38() {
        ItemStack i = new ItemStack(Material.BAMBOO);
        ItemMeta meta = i.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        meta.setMaxStackSize(8);
        meta.setDisplayName(ChatColor.GREEN + "【寻常】竹子");
        lore.add(ChatColor.WHITE + "生长速度很快，适合作为建材");
        meta.setLore(lore);
        i.setItemMeta(meta);
        return i.clone();
    }

    public ItemStack i39() {
        ItemStack i = new ItemStack(Material.DIRT);
        ItemMeta meta = i.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        meta.setMaxStackSize(8);
        meta.setDisplayName(ChatColor.GREEN + "【寻常】泥土");
        lore.add(ChatColor.WHITE + "可以用来种植农作物");
        meta.setLore(lore);
        i.setItemMeta(meta);
        return i.clone();
    }

    public ItemStack i40() {
        ItemStack i = new ItemStack(Material.GRAVEL);
        ItemMeta meta = i.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        meta.setMaxStackSize(8);
        meta.setDisplayName(ChatColor.GREEN + "【寻常】沙砾");
        lore.add(ChatColor.WHITE + "沙子，但是会挖出来燧石");
        meta.setLore(lore);
        i.setItemMeta(meta);
        return i.clone();
    }

    public ItemStack i41() {
        ItemStack i = new ItemStack(Material.COAL);
        ItemMeta meta = i.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        meta.setMaxStackSize(8);
        meta.setDisplayName(ChatColor.GREEN + "【寻常】煤炭");
        lore.add(ChatColor.WHITE + "非常好的燃料");
        meta.setLore(lore);
        i.setItemMeta(meta);
        return i.clone();
    }

    public ItemStack i42() {
        ItemStack i = new ItemStack(Material.COPPER_INGOT);
        ItemMeta meta = i.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        meta.setMaxStackSize(8);
        meta.setDisplayName(ChatColor.GREEN + "【寻常】铜锭");
        lore.add(ChatColor.WHITE + "铜制成的金属锭，最基础的金属材料");
        meta.setLore(lore);
        i.setItemMeta(meta);
        return i.clone();
    }

    public ItemStack i43() {
        ItemStack i = new ItemStack(Material.QUARTZ);
        ItemMeta meta = i.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        meta.setMaxStackSize(8);
        meta.setDisplayName(ChatColor.GREEN + "【寻常】下界石英");
        lore.add(ChatColor.WHITE + "腐竹最喜欢的建材");
        meta.setLore(lore);
        i.setItemMeta(meta);
        return i.clone();
    }

    public ItemStack i44() {
        ItemStack i = new ItemStack(Material.GLOWSTONE_DUST);
        ItemMeta meta = i.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        meta.setMaxStackSize(8);
        meta.setDisplayName(ChatColor.GREEN + "【寻常】萤石粉");
        lore.add(ChatColor.WHITE + "亮晶晶的粉末");
        meta.setLore(lore);
        i.setItemMeta(meta);
        return i.clone();
    }

    public ItemStack i45() {
        ItemStack i = new ItemStack(Material.LEATHER);
        ItemMeta meta = i.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        meta.setMaxStackSize(8);
        meta.setDisplayName(ChatColor.GREEN + "【寻常】皮革");
        lore.add(ChatColor.WHITE + "动物身上的皮，应该有用");
        meta.setLore(lore);
        i.setItemMeta(meta);
        return i.clone();
    }

    public ItemStack i46() {
        ItemStack i = new ItemStack(Material.EGG);
        ItemMeta meta = i.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        meta.setMaxStackSize(8);
        meta.setDisplayName(ChatColor.GREEN + "【寻常】鸡蛋");
        lore.add(ChatColor.WHITE + "激荡！！！");
        meta.setLore(lore);
        i.setItemMeta(meta);
        return i.clone();
    }

    public ItemStack i47() {
        ItemStack i = new ItemStack(Material.PINK_SHULKER_BOX);
        ItemMeta meta = i.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        meta.setMaxStackSize(8);
        meta.setDisplayName(ChatColor.GREEN + "【寻常】食物收纳盒");
        lore.add(ChatColor.WHITE + "食物收纳盒");
        lore.add(ChatColor.WHITE + "里面装有各种食物");
        lore.add(ChatColor.WHITE + "拿在手上，使用" +
                ChatColor.AQUA + " 鼠标右键 " +
                ChatColor.WHITE + "打开");
        meta.setLore(lore);
        i.setItemMeta(meta);
        return i.clone();
    }

    public ItemStack i48() {
        ItemStack i = new ItemStack(Material.LIME_SHULKER_BOX);
        ItemMeta meta = i.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        meta.setMaxStackSize(8);
        meta.setDisplayName(ChatColor.GREEN + "【寻常】植物收纳盒");
        lore.add(ChatColor.WHITE + "植物收纳盒");
        lore.add(ChatColor.WHITE + "里面装有各种植物");
        lore.add(ChatColor.WHITE + "拿在手上，使用" +
                ChatColor.AQUA + " 鼠标右键 " +
                ChatColor.WHITE + "打开");
        meta.setLore(lore);
        i.setItemMeta(meta);
        return i.clone();
    }

    public ItemStack i49() {
        ItemStack i = new ItemStack(Material.HONEYCOMB);
        ItemMeta meta = i.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        meta.setMaxStackSize(8);
        meta.setDisplayName(ChatColor.GREEN + "【寻常】蜜脾");
        lore.add(ChatColor.WHITE + "给我擦皮鞋");
        meta.setLore(lore);
        i.setItemMeta(meta);
        return i.clone();
    }

    public ItemStack i50() {
        ItemStack i = new ItemStack(Material.FEATHER);
        ItemMeta meta = i.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        meta.setMaxStackSize(8);
        meta.setDisplayName(ChatColor.GREEN + "【寻常】羽毛");
        lore.add(ChatColor.WHITE + "可以拿来做箭矢");
        meta.setLore(lore);
        i.setItemMeta(meta);
        return i.clone();
    }

    public ItemStack i51() {
        ItemStack i = new ItemStack(Material.GLOW_INK_SAC);
        ItemMeta meta = i.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        meta.setMaxStackSize(8);
        meta.setDisplayName(ChatColor.GREEN + "【寻常】发光墨囊");
        lore.add(ChatColor.WHITE + "估计是辐射量超标的墨鱼的掉落物");
        meta.setLore(lore);
        i.setItemMeta(meta);
        return i.clone();
    }

    public ItemStack i52() {
        ItemStack i = new ItemStack(Material.RABBIT_FOOT);
        ItemMeta meta = i.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        meta.setMaxStackSize(8);
        meta.setDisplayName(ChatColor.GREEN + "【寻常】兔子脚");
        lore.add(ChatColor.WHITE + "好运的象征");
        meta.setLore(lore);
        i.setItemMeta(meta);
        return i.clone();
    }

    public ItemStack i53() {
        ItemStack i = new ItemStack(Material.WHITE_WOOL);
        ItemMeta meta = i.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        meta.setMaxStackSize(8);
        meta.setDisplayName(ChatColor.GREEN + "【寻常】羊毛");
        lore.add(ChatColor.WHITE + "快来薅羊毛");
        meta.setLore(lore);
        i.setItemMeta(meta);
        return i.clone();
    }

    public ItemStack i54() {
        ItemStack i = new ItemStack(Material.SNOWBALL);
        ItemMeta meta = i.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        meta.setMaxStackSize(4);
        meta.setDisplayName(ChatColor.AQUA + "【稀有】雪球");
        lore.add(ChatColor.WHITE + "不要在里面包石头");
        meta.setLore(lore);
        i.setItemMeta(meta);
        return i.clone();
    }

    public ItemStack i55() {
        ItemStack i = new ItemStack(Material.IRON_INGOT);
        ItemMeta meta = i.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        meta.setMaxStackSize(4);
        meta.setDisplayName(ChatColor.AQUA + "【稀有】铁锭");
        lore.add(ChatColor.WHITE + "由铁制成的金属锭，很实用的金属材料");
        meta.setLore(lore);
        i.setItemMeta(meta);
        return i.clone();
    }

    public ItemStack i56() {
        ItemStack i = new ItemStack(Material.GOLD_INGOT);
        ItemMeta meta = i.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        meta.setMaxStackSize(4);
        meta.setDisplayName(ChatColor.AQUA + "【稀有】金锭");
        lore.add(ChatColor.WHITE + "亮闪闪的，很受人们欢迎");
        meta.setLore(lore);
        i.setItemMeta(meta);
        return i.clone();
    }

    public ItemStack i57() {
        ItemStack i = new ItemStack(Material.EMERALD);
        ItemMeta meta = i.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        meta.setMaxStackSize(4);
        meta.setDisplayName(ChatColor.AQUA + "【稀有】绿宝石");
        lore.add(ChatColor.WHITE + "村民的最爱");
        lore.add(ChatColor.WHITE + "不要跟“VR”弄混了");
        meta.setLore(lore);
        i.setItemMeta(meta);
        return i.clone();
    }

    public ItemStack i58() {
        ItemStack i = new ItemStack(Material.RESIN_CLUMP);
        ItemMeta meta = i.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        meta.setMaxStackSize(4);
        meta.setDisplayName(ChatColor.AQUA + "【稀有】树脂团");
        lore.add(ChatColor.WHITE + "一团树脂，黏糊糊的");
        lore.add(ChatColor.WHITE + "丢出去的时候有可能会黏在手上");
        meta.setLore(lore);
        i.setItemMeta(meta);
        return i.clone();
    }

    public ItemStack i59() {
        ItemStack i = new ItemStack(Material.NAME_TAG);
        ItemMeta meta = i.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        meta.setMaxStackSize(4);
        meta.setDisplayName(ChatColor.AQUA + "【稀有】命名牌");
        lore.add(ChatColor.WHITE + "可以防止你的宠物被服务器刷掉");
        meta.setLore(lore);
        i.setItemMeta(meta);
        return i.clone();
    }

    public ItemStack i60() {
        ItemStack i = new ItemStack(Material.TOTEM_OF_UNDYING);
        ItemMeta meta = i.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        meta.setMaxStackSize(1);
        meta.setDisplayName(ChatColor.AQUA + "【稀有】不死图腾");
        lore.add(ChatColor.WHITE + "关键时刻可以救你一命");
        lore.add(ChatColor.WHITE + "放在副手可以抵挡一次致命伤害");
        meta.setLore(lore);
        i.setItemMeta(meta);
        return i.clone();
    }

    public ItemStack i61() {
        ItemStack i = new ItemStack(Material.GOLDEN_APPLE);
        ItemMeta meta = i.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        meta.setMaxStackSize(4);
        meta.setDisplayName(ChatColor.AQUA + "【稀有】金苹果");
        lore.add(ChatColor.WHITE + "不知道是镀金还是纯金");
        meta.setLore(lore);
        i.setItemMeta(meta);
        return i.clone();
    }

    public ItemStack i62() {
        ItemStack i = new ItemStack(Material.TRIPWIRE_HOOK);
        ItemMeta meta = i.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        meta.setMaxStackSize(4);
        meta.setDisplayName(ChatColor.AQUA + "【稀有】破解装置");
        lore.add(ChatColor.WHITE + "原理未知的破解装置");
        lore.add(ChatColor.WHITE + "可以直接开启特殊撤离点");
        meta.setLore(lore);
        i.setItemMeta(meta);
        return i.clone();
    }

    public ItemStack i63() {
        ItemStack i = new ItemStack(Material.SADDLE);
        ItemMeta meta = i.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        meta.setMaxStackSize(1);
        meta.setDisplayName(ChatColor.AQUA + "【稀有】鞍");
        lore.add(ChatColor.WHITE + "当猪飞的时候");
        meta.setLore(lore);
        i.setItemMeta(meta);
        return i.clone();
    }

    public ItemStack i64() {
        ItemStack i = new ItemStack(Material.GREEN_SHULKER_BOX);
        ItemMeta meta = i.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        meta.setMaxStackSize(4);
        meta.setDisplayName(ChatColor.AQUA + "【稀有】树苗收纳盒");
        lore.add(ChatColor.WHITE + "树苗收纳盒");
        lore.add(ChatColor.WHITE + "里面装有各种树苗");
        lore.add(ChatColor.WHITE + "拿在手上，使用" +
                ChatColor.AQUA + " 鼠标右键 " +
                ChatColor.WHITE + "打开");
        meta.setLore(lore);
        i.setItemMeta(meta);
        return i.clone();
    }

    public ItemStack i65() {
        ItemStack i = new ItemStack(Material.LIGHT_BLUE_SHULKER_BOX);
        ItemMeta meta = i.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        meta.setMaxStackSize(4);
        meta.setDisplayName(ChatColor.AQUA + "【稀有】海洋收纳盒");
        lore.add(ChatColor.WHITE + "海洋收纳盒");
        lore.add(ChatColor.WHITE + "里面装有各种水产品");
        lore.add(ChatColor.WHITE + "拿在手上，使用" +
                ChatColor.AQUA + " 鼠标右键 " +
                ChatColor.WHITE + "打开");
        meta.setLore(lore);
        i.setItemMeta(meta);
        return i.clone();
    }

    public ItemStack i66() {
        ItemStack i = new ItemStack(Material.LIGHT_GRAY_SHULKER_BOX);
        ItemMeta meta = i.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        meta.setMaxStackSize(4);
        meta.setDisplayName(ChatColor.AQUA + "【稀有】唱片收纳盒");
        lore.add(ChatColor.WHITE + "唱片收纳盒");
        lore.add(ChatColor.WHITE + "里面装有各种唱片");
        lore.add(ChatColor.WHITE + "拿在手上，使用" +
                ChatColor.AQUA + " 鼠标右键 " +
                ChatColor.WHITE + "打开");
        meta.setLore(lore);
        i.setItemMeta(meta);
        return i.clone();
    }

    public ItemStack i67() {
        ItemStack i = new ItemStack(Material.CYAN_SHULKER_BOX);
        ItemMeta meta = i.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        meta.setMaxStackSize(4);
        meta.setDisplayName(ChatColor.AQUA + "【稀有】道具收纳盒");
        lore.add(ChatColor.WHITE + "道具收纳盒");
        lore.add(ChatColor.WHITE + "里面装有各种道具");
        lore.add(ChatColor.WHITE + "拿在手上，使用" +
                ChatColor.AQUA + " 鼠标右键 " +
                ChatColor.WHITE + "打开");
        meta.setLore(lore);
        i.setItemMeta(meta);
        return i.clone();
    }

    public ItemStack i68() {
        ItemStack i = new ItemStack(Material.AMETHYST_SHARD);
        ItemMeta meta = i.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        meta.setMaxStackSize(2);
        meta.setDisplayName(ChatColor.LIGHT_PURPLE + "【罕见】紫水晶碎片");
        lore.add(ChatColor.WHITE + "不是能源紫水晶");
        meta.setLore(lore);
        i.setItemMeta(meta);
        return i.clone();
    }

    public ItemStack i69() {
        ItemStack i = new ItemStack(Material.DIAMOND);
        ItemMeta meta = i.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        meta.setMaxStackSize(2);
        meta.setDisplayName(ChatColor.LIGHT_PURPLE + "【罕见】钻石");
        lore.add(ChatColor.WHITE + "我挖到钻石辣！");
        meta.setLore(lore);
        i.setItemMeta(meta);
        return i.clone();
    }

    public ItemStack i70() {
        ItemStack i = new ItemStack(Material.LAPIS_LAZULI);
        ItemMeta meta = i.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        meta.setMaxStackSize(2);
        meta.setDisplayName(ChatColor.LIGHT_PURPLE + "【罕见】青金石");
        lore.add(ChatColor.WHITE + "蕴含魔力的矿物");
        meta.setLore(lore);
        i.setItemMeta(meta);
        return i.clone();
    }

    public ItemStack i71() {
        ItemStack i = new ItemStack(Material.WATER_BUCKET);
        ItemMeta meta = i.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        meta.setMaxStackSize(1);
        meta.setDisplayName(ChatColor.LIGHT_PURPLE + "【罕见】水桶");
        lore.add(ChatColor.WHITE + "装满水的桶");
        lore.add(ChatColor.WHITE + "落地，水");
        meta.setLore(lore);
        i.setItemMeta(meta);
        return i.clone();
    }

    public ItemStack i72() {
        ItemStack i = new ItemStack(Material.LAVA_BUCKET);
        ItemMeta meta = i.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        meta.setMaxStackSize(1);
        meta.setDisplayName(ChatColor.LIGHT_PURPLE + "【罕见】岩浆桶");
        lore.add(ChatColor.WHITE + "装满岩浆的桶");
        lore.add(ChatColor.WHITE + "和水桶搭配使用更好");
        meta.setLore(lore);
        i.setItemMeta(meta);
        return i.clone();
    }

    public ItemStack i73() {
        ItemStack i = new ItemStack(Material.OBSIDIAN);
        ItemMeta meta = i.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        meta.setMaxStackSize(2);
        meta.setDisplayName(ChatColor.LIGHT_PURPLE + "【罕见】黑曜石");
        lore.add(ChatColor.WHITE + "当你倒岩浆的时候倒错了位置，你就得到了这个");
        meta.setLore(lore);
        i.setItemMeta(meta);
        return i.clone();
    }

    public ItemStack i74() {
        ItemStack i = new ItemStack(Material.GHAST_TEAR);
        ItemMeta meta = i.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        meta.setMaxStackSize(2);
        meta.setDisplayName(ChatColor.LIGHT_PURPLE + "【罕见】恶魂之泪");
        lore.add(ChatColor.WHITE + "一滴散发着不详气息的眼泪");
        lore.add(ChatColor.WHITE + "见鬼去吧！");
        meta.setLore(lore);
        i.setItemMeta(meta);
        return i.clone();
    }

    public ItemStack i75() {
        ItemStack i = new ItemStack(Material.PHANTOM_MEMBRANE);
        ItemMeta meta = i.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        meta.setMaxStackSize(4);
        meta.setDisplayName(ChatColor.AQUA + "【稀有】幻翼膜");
        lore.add(ChatColor.WHITE + "拥有特殊纹理的一层膜");
        lore.add(ChatColor.WHITE + "结实又轻便");
        meta.setLore(lore);
        i.setItemMeta(meta);
        return i.clone();
    }

    public ItemStack i76() {
        ItemStack i = new ItemStack(Material.RED_SHULKER_BOX);
        ItemMeta meta = i.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        meta.setMaxStackSize(2);
        meta.setDisplayName(ChatColor.LIGHT_PURPLE + "【罕见】武器收纳盒");
        lore.add(ChatColor.WHITE + "武器收纳盒");
        lore.add(ChatColor.WHITE + "里面装有各种武器");
        lore.add(ChatColor.WHITE + "拿在手上，使用" +
                ChatColor.AQUA + " 鼠标右键 " +
                ChatColor.WHITE + "打开");
        meta.setLore(lore);
        i.setItemMeta(meta);
        return i.clone();
    }

    public ItemStack i77() {
        ItemStack i = new ItemStack(Material.BLUE_SHULKER_BOX);
        ItemMeta meta = i.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        meta.setMaxStackSize(2);
        meta.setDisplayName(ChatColor.LIGHT_PURPLE + "【罕见】盔甲收纳盒");
        lore.add(ChatColor.WHITE + "盔甲收纳盒");
        lore.add(ChatColor.WHITE + "里面装有各种盔甲");
        lore.add(ChatColor.WHITE + "拿在手上，使用" +
                ChatColor.AQUA + " 鼠标右键 " +
                ChatColor.WHITE + "打开");
        meta.setLore(lore);
        i.setItemMeta(meta);
        return i.clone();
    }

    public ItemStack i78() {
        ItemStack i = new ItemStack(Material.PURPLE_SHULKER_BOX);
        ItemMeta meta = i.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        meta.setMaxStackSize(2);
        meta.setDisplayName(ChatColor.LIGHT_PURPLE + "【罕见】魔咒收纳盒");
        lore.add(ChatColor.WHITE + "魔咒收纳盒");
        lore.add(ChatColor.WHITE + "里面装有各种附魔书");
        lore.add(ChatColor.WHITE + "拿在手上，使用" +
                ChatColor.AQUA + " 鼠标右键 " +
                ChatColor.WHITE + "打开");
        meta.setLore(lore);
        i.setItemMeta(meta);
        return i.clone();
    }

    public ItemStack i79() {
        ItemStack i = new ItemStack(Material.ORANGE_SHULKER_BOX);
        ItemMeta meta = i.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        meta.setMaxStackSize(2);
        meta.setDisplayName(ChatColor.LIGHT_PURPLE + "【罕见】陶片收纳盒");
        lore.add(ChatColor.WHITE + "陶片收纳盒");
        lore.add(ChatColor.WHITE + "里面装有各种陶片");
        lore.add(ChatColor.WHITE + "拿在手上，使用" +
                ChatColor.AQUA + " 鼠标右键 " +
                ChatColor.WHITE + "打开");
        meta.setLore(lore);
        i.setItemMeta(meta);
        return i.clone();
    }

    public ItemStack i80() {
        ItemStack i = new ItemStack(Material.GRAY_SHULKER_BOX);
        ItemMeta meta = i.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        meta.setMaxStackSize(2);
        meta.setDisplayName(ChatColor.LIGHT_PURPLE + "【罕见】号角收纳盒");
        lore.add(ChatColor.WHITE + "号角收纳盒");
        lore.add(ChatColor.WHITE + "里面装有各种号角");
        lore.add(ChatColor.WHITE + "拿在手上，使用" +
                ChatColor.AQUA + " 鼠标右键 " +
                ChatColor.WHITE + "打开");
        meta.setLore(lore);
        i.setItemMeta(meta);
        return i.clone();
    }

    public ItemStack i81() {
        ItemStack i = new ItemStack(Material.BROWN_SHULKER_BOX);
        ItemMeta meta = i.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        meta.setMaxStackSize(2);
        meta.setDisplayName(ChatColor.LIGHT_PURPLE + "【罕见】纹饰收纳盒");
        lore.add(ChatColor.WHITE + "纹饰收纳盒");
        lore.add(ChatColor.WHITE + "里面装有各种盔甲纹饰");
        lore.add(ChatColor.WHITE + "拿在手上，使用" +
                ChatColor.AQUA + " 鼠标右键 " +
                ChatColor.WHITE + "打开");
        meta.setLore(lore);
        i.setItemMeta(meta);
        return i.clone();
    }

    public ItemStack i82() {
        ItemStack i = new ItemStack(Material.WHITE_SHULKER_BOX);
        ItemMeta meta = i.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        meta.setMaxStackSize(2);
        meta.setDisplayName(ChatColor.LIGHT_PURPLE + "【罕见】钥匙收纳盒");
        lore.add(ChatColor.WHITE + "钥匙收纳盒");
        lore.add(ChatColor.WHITE + "里面装有各种钥匙");
        lore.add(ChatColor.WHITE + "拿在手上，使用" +
                ChatColor.AQUA + " 鼠标右键 " +
                ChatColor.WHITE + "打开");
        meta.setLore(lore);
        i.setItemMeta(meta);
        return i.clone();
    }

    public ItemStack i83() {
        ItemStack i = new ItemStack(Material.ANCIENT_DEBRIS);
        ItemMeta meta = i.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        meta.setMaxStackSize(1);
        meta.setDisplayName(ChatColor.GOLD + "【珍奇】远古残骸");
        lore.add(ChatColor.WHITE + "蕴含古老的力量");
        meta.setLore(lore);
        i.setItemMeta(meta);
        return i.clone();
    }

    public ItemStack i84() {
        ItemStack i = new ItemStack(Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE);
        ItemMeta meta = i.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        meta.setMaxStackSize(1);
        meta.setDisplayName(ChatColor.GOLD + "【珍奇】锻造模板");
        lore.add(ChatColor.WHITE + "升级装备的必需品");
        meta.setLore(lore);
        i.setItemMeta(meta);
        return i.clone();
    }

    public ItemStack i85() {
        ItemStack i = new ItemStack(Material.CRYING_OBSIDIAN);
        ItemMeta meta = i.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        meta.setMaxStackSize(1);
        meta.setDisplayName(ChatColor.GOLD + "【珍奇】哭泣的黑曜石");
        lore.add(ChatColor.WHITE + "谁在切洋葱");
        meta.setLore(lore);
        i.setItemMeta(meta);
        return i.clone();
    }

    public ItemStack i86() {
        ItemStack i = new ItemStack(Material.ELYTRA);
        ItemMeta meta = i.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        meta.setMaxStackSize(1);
        meta.setDisplayName(ChatColor.GOLD + "【珍奇】鞘翅");
        lore.add(ChatColor.WHITE + "我要当太空人");
        meta.setLore(lore);
        i.setItemMeta(meta);
        return i.clone();
    }

    public ItemStack i87() {
        ItemStack i = new ItemStack(Material.ENCHANTED_GOLDEN_APPLE);
        ItemMeta meta = i.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        meta.setMaxStackSize(1);
        meta.setDisplayName(ChatColor.GOLD + "【珍奇】附魔金苹果");
        lore.add(ChatColor.WHITE + "是附魔金苹果");
        lore.add(ChatColor.WHITE + "不是附魔的金苹果");
        meta.setLore(lore);
        i.setItemMeta(meta);
        return i.clone();
    }

    public ItemStack i88() {
        ItemStack i = new ItemStack(Material.ARMADILLO_SCUTE);
        ItemMeta meta = i.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        meta.setMaxStackSize(1);
        meta.setDisplayName(ChatColor.GOLD + "【珍奇】犰狳鳞甲");
        lore.add(ChatColor.WHITE + "犰狳其实就是起了全装的负鼠");
        meta.setLore(lore);
        i.setItemMeta(meta);
        return i.clone();
    }

    public ItemStack i89() {
        ItemStack i = new ItemStack(Material.TURTLE_SCUTE);
        ItemMeta meta = i.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        meta.setMaxStackSize(1);
        meta.setDisplayName(ChatColor.GOLD + "【珍奇】海龟鳞甲");
        lore.add(ChatColor.WHITE + "要想生活过得去...");
        meta.setLore(lore);
        i.setItemMeta(meta);
        return i.clone();
    }

    public ItemStack i90() {
        ItemStack i = new ItemStack(Material.NAUTILUS_SHELL);
        ItemMeta meta = i.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        meta.setMaxStackSize(1);
        meta.setDisplayName(ChatColor.GOLD + "【珍奇】鹦鹉螺壳");
        lore.add(ChatColor.WHITE + "可以听到大海的声音");
        meta.setLore(lore);
        i.setItemMeta(meta);
        return i.clone();
    }

    public ItemStack i91() {
        ItemStack i = new ItemStack(Material.HEART_OF_THE_SEA);
        ItemMeta meta = i.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        meta.setMaxStackSize(1);
        meta.setDisplayName(ChatColor.GOLD + "【珍奇】海洋之心");
        lore.add(ChatColor.WHITE + "非洲之心 + 海洋之泪");
        meta.setLore(lore);
        i.setItemMeta(meta);
        return i.clone();
    }

    public ItemStack i92() {
        ItemStack i = new ItemStack(Material.ECHO_SHARD);
        ItemMeta meta = i.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        meta.setMaxStackSize(1);
        meta.setDisplayName(ChatColor.GOLD + "【珍奇】回响碎片");
        lore.add(ChatColor.WHITE + "深暗之域的回声结晶");
        lore.add(ChatColor.WHITE + "贴在耳边可以听到远古的声音");
        lore.add(ChatColor.WHITE + "用来制作武器装备一定很不错");
        meta.setLore(lore);
        i.setItemMeta(meta);
        return i.clone();
    }

    public ItemStack i93() {
        ItemStack i = new ItemStack(Material.DRAGON_BREATH);
        ItemMeta meta = i.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        meta.setMaxStackSize(1);
        meta.setDisplayName(ChatColor.GOLD + "【珍奇】龙息");
        lore.add(ChatColor.WHITE + "你需要来点薄荷糖");
        meta.setLore(lore);
        i.setItemMeta(meta);
        return i.clone();
    }

    public ItemStack i94() {
        ItemStack i = new ItemStack(Material.SNIFFER_EGG);
        ItemMeta meta = i.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        meta.setMaxStackSize(1);
        meta.setDisplayName(ChatColor.GOLD + "【珍奇】嗅探兽的蛋");
        lore.add(ChatColor.WHITE + "不是嗅探兽的刷怪蛋");
        meta.setLore(lore);
        i.setItemMeta(meta);
        return i.clone();
    }

    public ItemStack i95() {
        ItemStack i = new ItemStack(Material.YELLOW_SHULKER_BOX);
        ItemMeta meta = i.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        meta.setMaxStackSize(1);
        meta.setDisplayName(ChatColor.GOLD + "【珍奇】配方收纳盒");
        lore.add(ChatColor.WHITE + "配方收纳盒");
        lore.add(ChatColor.WHITE + "里面装有各种配方");
        lore.add(ChatColor.WHITE + "拿在手上，使用" +
                ChatColor.AQUA + " 鼠标右键 " +
                ChatColor.WHITE + "打开");
        meta.setLore(lore);
        i.setItemMeta(meta);
        return i.clone();
    }

    public ItemStack i96() {
        ItemStack i = new ItemStack(Material.DRAGON_EGG);
        ItemMeta meta = i.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        meta.setMaxStackSize(1);
        meta.setDisplayName(ChatColor.RED + "【典藏】龙蛋");
        lore.add(ChatColor.WHITE + "传说中的龙蛋，孵化方法至今还是未解之谜");
        lore.add(ChatColor.WHITE + "不建议用这个做煎蛋");
        meta.setLore(lore);
        i.setItemMeta(meta);
        return i.clone();
    }

    public ItemStack i97() {
        ItemStack i = new ItemStack(Material.OMINOUS_BOTTLE);
        ItemMeta meta = i.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        meta.setMaxStackSize(1);
        meta.setDisplayName(ChatColor.RED + "【典藏】不详之瓶");
        lore.add(ChatColor.WHITE + "看起来非常的不对劲");
        lore.add(ChatColor.WHITE + "里面的液体不建议饮用");
        meta.setLore(lore);
        i.setItemMeta(meta);
        return i.clone();
    }

    public ItemStack i98() {
        ItemStack i = new ItemStack(Material.SPORE_BLOSSOM);
        ItemMeta meta = i.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        meta.setMaxStackSize(1);
        meta.setDisplayName(ChatColor.RED + "【典藏】孢子花");
        lore.add(ChatColor.WHITE + "希望你没有花粉过敏");
        meta.setLore(lore);
        i.setItemMeta(meta);
        return i.clone();
    }

    public ItemStack i99() {
        ItemStack i = new ItemStack(Material.DRAGON_HEAD);
        ItemMeta meta = i.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        meta.setMaxStackSize(1);
        meta.setDisplayName(ChatColor.RED + "【典藏】龙首");
        lore.add(ChatColor.WHITE + "杀掉末影龙的证明");
        lore.add(ChatColor.WHITE + "可以戴在头上吓唬朋友");
        meta.setLore(lore);
        i.setItemMeta(meta);
        return i.clone();
    }

    public ItemStack i100() {
        ItemStack i = new ItemStack(Material.DRIED_GHAST);
        ItemMeta meta = i.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        meta.setMaxStackSize(1);
        meta.setDisplayName(ChatColor.RED + "【典藏】失水恶魂");
        lore.add(ChatColor.WHITE + "一滴也不剩的恶魂");
        meta.setLore(lore);
        i.setItemMeta(meta);
        return i.clone();
    }

    public ItemStack i101() {
        ItemStack i = new ItemStack(Material.HEAVY_CORE);
        ItemMeta meta = i.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        meta.setMaxStackSize(1);
        meta.setDisplayName(ChatColor.RED + "【典藏】沉重核心");
        lore.add(ChatColor.WHITE + "重得让人怀疑人生的神秘物体");
        lore.add(ChatColor.WHITE + "有可能是地心的物质碎片");
        meta.setLore(lore);
        i.setItemMeta(meta);
        return i.clone();
    }

    public ItemStack i102() {
        ItemStack i = new ItemStack(Material.DEAD_BUSH);
        ItemMeta meta = i.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        meta.setMaxStackSize(1);
        meta.setDisplayName(ChatColor.RED + "【典藏】枯死的灌木");
        lore.add(ChatColor.WHITE + "MEME物品");
        meta.setLore(lore);
        i.setItemMeta(meta);
        return i.clone();
    }

    public ItemStack i103() {
        ItemStack i = new ItemStack(Material.COMPASS);
        ItemMeta meta = i.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        meta.setMaxStackSize(1);
        meta.setDisplayName(ChatColor.RED + "【典藏】指南针");
        lore.add(ChatColor.WHITE + "在没有红石的世界里");
        lore.add(ChatColor.WHITE + "指南针的收藏价值极高");
        meta.setLore(lore);
        i.setItemMeta(meta);
        return i.clone();
    }

    public ItemStack i104() {
        ItemStack i = new ItemStack(Material.CACTUS);
        ItemMeta meta = i.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        meta.setMaxStackSize(8);
        meta.setDisplayName(ChatColor.GREEN + "【寻常】仙人掌");
        lore.add(ChatColor.WHITE + "上面有很多刺，不要被扎到");
        meta.setLore(lore);
        i.setItemMeta(meta);
        return i.clone();
    }

    public ItemStack i105() {
        ItemStack i = new ItemStack(Material.EXPERIENCE_BOTTLE);
        ItemMeta meta = i.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        meta.setMaxStackSize(8);
        meta.setDisplayName(ChatColor.GREEN + "【寻常】附魔之瓶");
        lore.add(ChatColor.WHITE + "经验+3");
        meta.setLore(lore);
        i.setItemMeta(meta);
        return i.clone();
    }

    public ItemStack i106() {
        ItemStack i = new ItemStack(Material.SOUL_SAND);
        ItemMeta meta = i.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        meta.setMaxStackSize(8);
        meta.setDisplayName(ChatColor.GREEN + "【寻常】灵魂沙");
        lore.add(ChatColor.WHITE + "拿在手上能感受到不详的气息");
        meta.setLore(lore);
        i.setItemMeta(meta);
        return i.clone();
    }

    public ItemStack i107() {
        ItemStack i = new ItemStack(Material.BOOK);
        ItemMeta meta = i.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        meta.setMaxStackSize(8);
        meta.setDisplayName(ChatColor.GREEN + "【寻常】书");
        lore.add(ChatColor.WHITE + "没人知道里面写了什么");
        meta.setLore(lore);
        i.setItemMeta(meta);
        return i.clone();
    }

    public ItemStack i108() {
        ItemStack i = new ItemStack(Material.POPPED_CHORUS_FRUIT);
        ItemMeta meta = i.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        meta.setMaxStackSize(4);
        meta.setDisplayName(ChatColor.AQUA + "【稀有】爆裂紫颂果");
        lore.add(ChatColor.WHITE + "已经不能吃了");
        meta.setLore(lore);
        i.setItemMeta(meta);
        return i.clone();
    }

    public ItemStack i109() {
        ItemStack i = new ItemStack(Material.MOSS_BLOCK);
        ItemMeta meta = i.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        meta.setMaxStackSize(2);
        meta.setDisplayName(ChatColor.LIGHT_PURPLE + "【罕见】苔藓块");
        lore.add(ChatColor.WHITE + "如果有骨粉的话...");
        meta.setLore(lore);
        i.setItemMeta(meta);
        return i.clone();
    }
    public ItemStack i110() {
        ItemStack i = new ItemStack(Material.PRISMARINE_SHARD);
        ItemMeta meta = i.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        meta.setMaxStackSize(8);
        meta.setDisplayName(ChatColor.GREEN + "【寻常】海晶碎片");
        lore.add(ChatColor.WHITE + "海底神殿中的碎片");
        lore.add(ChatColor.WHITE + "上面隐隐约约能看到残缺的古老文字");
        meta.setLore(lore);
        i.setItemMeta(meta);
        return i.clone();
    }
    public ItemStack i111() {
        ItemStack i = new ItemStack(Material.PRISMARINE_CRYSTALS);
        ItemMeta meta = i.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        meta.setMaxStackSize(8);
        meta.setDisplayName(ChatColor.GREEN + "【寻常】海晶砂粒");
        lore.add(ChatColor.WHITE + "像鹅卵石一样，圆滚滚亮晶晶的砂粒");
        lore.add(ChatColor.WHITE + "放在手心触感非常不错");
        meta.setLore(lore);
        i.setItemMeta(meta);
        return i.clone();
    }
    public ItemStack i112() {
        ItemStack i = new ItemStack(Material.LEVER);
        ItemMeta meta = i.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        meta.setMaxStackSize(8);
        meta.setDisplayName(ChatColor.DARK_RED + "【神话】没(mei)收库房钥匙");
        lore.add(ChatColor.WHITE + "没(mei)收库房的钥匙");
        lore.add(ChatColor.WHITE + "可以开启没(mei)收库房");
        meta.setLore(lore);
        i.setItemMeta(meta);
        return i.clone();
    }
    public ItemStack i113() {
        ItemStack i = new ItemStack(Material.TALL_GRASS);
        ItemMeta meta = i.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        meta.setMaxStackSize(8);
        meta.setDisplayName(ChatColor.DARK_RED + "【神话】大蕊毛草");
        lore.add(ChatColor.WHITE + "可以制成绝世药品");
        meta.setLore(lore);
        i.setItemMeta(meta);
        return i.clone();
    }
}

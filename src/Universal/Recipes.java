package Universal;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public enum Recipes {
    INSTANCE;
    JavaPlugin plugin;
    WeaponPool wp = WeaponPool.INSTANCE;
    ArmorPool ap = ArmorPool.INSTANCE;
    GadgetPool gp = GadgetPool.INSTANCE;
    LootPool lp = LootPool.INSTANCE;
    Kit k = Kit.INSTANCE;
    ItemStack[] boxRecipes = new ItemStack[0];
    ItemStack[] recipeBooks = new ItemStack[0];
    ItemStack[] menuItems = new ItemStack[0];
    HashMap<ItemStack, ItemStack> recipeMap = new HashMap<>();
    HashMap<String, ShapedRecipe> shapedRecipeMap = new HashMap<>();
    public String[]freeRecipes = new String[0];
    ItemStack[]freeRecipeItems = new ItemStack[]{
            wp.boneStick(),
            wp.glassSword(),
            wp.cactusSword(),
            wp.flintSword(),
            wp.broom(),
            wp.ferro(),
            gp.copperBattery(),
            gp.ironBattery(),
            gp.goldenBattery(),
    };
    int key = 0;
    public void setPlugin(JavaPlugin plugin) {
        this.plugin = plugin;
    }
    public void registerFreeRecipe(){
        int index = 0;
        for(ItemStack i : freeRecipeItems){
            String lore = k.getLore(i);
            if(lore.isEmpty())continue;
            freeRecipes[index] = lore;
            index ++;
        }
    }

    public void registerStack() {
        ItemStack[] weapons = wp.getRecipeWeapons();
        ItemStack[] gadgets = gp.getRecipeGadgets();
        List<ItemStack> weaponRecipes = new ArrayList<>();
        List<ItemStack> gadgetRecipes = new ArrayList<>();
        List<ItemStack> menuItemsList = new ArrayList<>();
        List<String>freeRecipeString = Arrays.stream(freeRecipes).toList();
        for (ItemStack i : weapons) {
            String lore = k.getLore(i);
            if(!lore.isEmpty()) {
                if (freeRecipeString.contains(lore))continue;
            }
            ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
            ItemMeta itemMeta = i.getItemMeta();
            ItemMeta bookMeta = book.getItemMeta();
            bookMeta.setMaxStackSize(1);
            if (!itemMeta.hasDisplayName()) continue;
            bookMeta.setDisplayName(itemMeta.getDisplayName()
                    + ChatColor.GOLD + "配方");
            ArrayList<String> lores = new ArrayList<>();
            lores.add(ChatColor.WHITE + "一本古老的书");
            lores.add(ChatColor.WHITE + "记载着合成" + itemMeta.getDisplayName()
                    + ChatColor.WHITE + "的配方");
            lores.add(ChatColor.WHITE + "放在背包里才能合成对应物品");
            bookMeta.setLore(lores);
            book.setItemMeta(bookMeta);
            recipeMap.put(i, book);
            weaponRecipes.add(book);
        }
        for (ItemStack i : gadgets) {
            String lore = k.getLore(i);
            if(!lore.isEmpty()) {
                if (freeRecipeString.contains(lore))continue;
            }
            ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
            ItemMeta itemMeta = i.getItemMeta();
            ItemMeta bookMeta = book.getItemMeta();
            bookMeta.setMaxStackSize(1);
            if (!itemMeta.hasDisplayName()) continue;
            bookMeta.setDisplayName(itemMeta.getDisplayName()
                    + ChatColor.GOLD + "配方");
            ArrayList<String> lores = new ArrayList<>();
            lores.add(ChatColor.WHITE + "一本古老的书");
            lores.add(ChatColor.WHITE + "记载着合成" + itemMeta.getDisplayName()
                    + ChatColor.WHITE + "的配方");
            lores.add(ChatColor.WHITE + "放在背包里才能合成对应物品");
            bookMeta.setLore(lores);
            book.setItemMeta(bookMeta);
            recipeMap.put(i, book);
            gadgetRecipes.add(book);
        }
        List<ItemStack> recipeList = new ArrayList<>(weaponRecipes);
        List<ItemStack> boxRecipeList = new ArrayList<>(weaponRecipes);
        recipeList.addAll(gadgetRecipes);
        menuItemsList.addAll(List.of(weapons));
        menuItemsList.addAll(List.of(gadgets));
        for (int j = 0; j < 5; j++) {
            boxRecipeList.addAll(gadgetRecipes);
        }
        boxRecipes = boxRecipeList.toArray(new ItemStack[0]);
        recipeBooks = recipeList.toArray(new ItemStack[0]);
        menuItems = menuItemsList.toArray(new ItemStack[0]);
    }

    public ItemStack[] getBoxRecipes() {
        return boxRecipes.clone();
    }

    public ItemStack[] getRecipeBooks() {
        return recipeBooks.clone();
    }

    public ItemStack[] getMenuItems() {
        return menuItems.clone();
    }

    public ItemStack[] getFreeRecipeItems() {
        return freeRecipeItems.clone();
    }

    public HashMap<String, ShapedRecipe> getShapedRecipeMap() {
        return shapedRecipeMap;
    }

    public void registerRecipe() {
        try {
            r0();r10();r20();r30();r40();r50();r60();r70();r80();r90();r100();r110();
            r1();r11();r21();r31();r41();r51();r61();r71();r81();r91();r101();r111();
            r2();r12();r22();r32();r42();r52();r62();r72();r82();r92();r102();r112();
            r3();r13();r23();r33();r43();r53();r63();r73();r83();r93();r103();r113();
            r4();r14();r24();r34();r44();r54();r64();r74();r84();r94();r104();r114();
            r5();r15();r25();r35();r45();r55();r65();r75();r85();r95();r105();r115();
            r6();r16();r26();r36();r46();r56();r66();r76();r86();r96();r106();r116();
            r7();r17();r27();r37();r47();r57();r67();r77();r87();r97();r107();
            r8();r18();r28();r38();r48();r58();r68();r78();r88();r98();r108();
            r9();r19();r29();r39();r49();r59();r69();r79();r89();r99();r109();
        }
        catch (Exception ignored) {
        }
    }

    public void r0() {
        ItemStack i = wp.boneStick();
        NamespacedKey w = new NamespacedKey(plugin, "r" + key);
        ShapedRecipe r = new ShapedRecipe(w, i);
        r.shape(" B ", " B ", " S ");
        r.setIngredient('B', Material.BONE_BLOCK);
        r.setIngredient('S', Material.BONE);
        Bukkit.addRecipe(r);
        key += 1;
        String lore = k.getLore(i);
        if(!lore.isEmpty()){
            shapedRecipeMap.put(lore,r);
        }
    }

    public void r1() {
        ItemStack i = wp.glassSword();
        NamespacedKey w = new NamespacedKey(plugin, "r" + key);
        ShapedRecipe r = new ShapedRecipe(w, i);
        r.shape(" A ", " A ", " B ");
        r.setIngredient('A', Material.GLASS);
        r.setIngredient('B', Material.STICK);
        Bukkit.addRecipe(r);
        key += 1;
        String lore = k.getLore(i);
        if(!lore.isEmpty()){
            shapedRecipeMap.put(lore,r);
        }
    }

    public void r2() {
        ItemStack i = wp.warHammer();
        NamespacedKey w = new NamespacedKey(plugin, "r" + key);
        ShapedRecipe r = new ShapedRecipe(w, i);
        r.shape(" A ", " B ", " B ");
        r.setIngredient('A', Material.COPPER_BLOCK);
        r.setIngredient('B', Material.STICK);
        Bukkit.addRecipe(r);
        key += 1;
        String lore = k.getLore(i);
        if(!lore.isEmpty()){
            shapedRecipeMap.put(lore,r);
        }
    }

    public void r3() {
        ItemStack i = wp.flameBow();
        NamespacedKey w3 = new NamespacedKey(plugin, "r" + key);
        ShapedRecipe r = new ShapedRecipe(w3, i);
        r.shape(" AB", "A B", " AB");
        r.setIngredient('A', Material.BLAZE_ROD);
        r.setIngredient('B', Material.COPPER_CHAIN);
        Bukkit.addRecipe(r);
        key += 1;
        String lore = k.getLore(i);
        if(!lore.isEmpty()){
            shapedRecipeMap.put(lore,r);
        }
    }

    public void r4() {
        ItemStack i = wp.bambooSpear();
        NamespacedKey w4 = new NamespacedKey(plugin, "r" + key);
        ShapedRecipe r = new ShapedRecipe(w4, i);
        r.shape(" A ", " A ", " A ");
        r.setIngredient('A', Material.BAMBOO);
        Bukkit.addRecipe(r);
        key += 1;
        String lore = k.getLore(i);
        if(!lore.isEmpty()){
            shapedRecipeMap.put(lore,r);
        }
    }

    public void r5() {
        ItemStack i = wp.crystalSword();
        NamespacedKey w5 = new NamespacedKey(plugin, "r" + key);
        ShapedRecipe r = new ShapedRecipe(w5, i);
        r.shape(" A ", " A ", " B ");
        r.setIngredient('A', Material.AMETHYST_SHARD);
        r.setIngredient('B', Material.END_ROD);
        Bukkit.addRecipe(r);
        key += 1;
        String lore = k.getLore(i);
        if(!lore.isEmpty()){
            shapedRecipeMap.put(lore,r);
        }
    }

    public void r6() {
        ItemStack i = wp.echoAxe();
        NamespacedKey w6 = new NamespacedKey(plugin, "r" + key);
        ShapedRecipe r = new ShapedRecipe(w6, i);
        r.shape("AA ", "AB ", " B ");
        r.setIngredient('A', Material.ECHO_SHARD);
        r.setIngredient('B', Material.BREEZE_ROD);
        Bukkit.addRecipe(r);
        key += 1;
        String lore = k.getLore(i);
        if(!lore.isEmpty()){
            shapedRecipeMap.put(lore,r);
        }
    }

    public void r7() {
        ItemStack i = wp.cactusSword();
        NamespacedKey w7 = new NamespacedKey(plugin, "r" + key);
        ShapedRecipe r = new ShapedRecipe(w7, i);
        r.shape(" A ", " A ", " B ");
        r.setIngredient('A', Material.CACTUS);
        r.setIngredient('B', Material.BAMBOO);
        Bukkit.addRecipe(r);
        key += 1;
        String lore = k.getLore(i);
        if(!lore.isEmpty()){
            shapedRecipeMap.put(lore,r);
        }
    }

    public void r8() {
        ItemStack i = wp.flintSword();
        NamespacedKey w8 = new NamespacedKey(plugin, "r" + key);
        ShapedRecipe r = new ShapedRecipe(w8, i);
        r.shape(" A ", " A ", " B ");
        r.setIngredient('A', Material.FLINT);
        r.setIngredient('B', Material.TORCH);
        Bukkit.addRecipe(r);
        key += 1;
        String lore = k.getLore(i);
        if(!lore.isEmpty()){
            shapedRecipeMap.put(lore,r);
        }
    }

    public void r9() {
        ItemStack i = wp.quartzSword();
        NamespacedKey w9 = new NamespacedKey(plugin, "r" + key);
        ShapedRecipe r = new ShapedRecipe(w9, i);
        r.shape(" A ", " A ", " B ");
        r.setIngredient('A', Material.QUARTZ);
        r.setIngredient('B', Material.STICK);
        Bukkit.addRecipe(r);
        key += 1;
        String lore = k.getLore(i);
        if(!lore.isEmpty()){
            shapedRecipeMap.put(lore,r);
        }
    }

    public void r10() {
        ItemStack i = wp.netherSword();
        NamespacedKey w10 = new NamespacedKey(plugin, "r" + key);
        ShapedRecipe r = new ShapedRecipe(w10, i);
        r.shape(" A ", " A ", " B ");
        r.setIngredient('A', Material.NETHERITE_INGOT);
        r.setIngredient('B', Material.END_ROD);
        Bukkit.addRecipe(r);
        key += 1;
        String lore = k.getLore(i);
        if(!lore.isEmpty()){
            shapedRecipeMap.put(lore,r);
        }
    }

    public void r11() {
        ItemStack i = wp.goldenCarrot();
        NamespacedKey w11 = new NamespacedKey(plugin, "r" + key);
        ShapedRecipe r = new ShapedRecipe(w11, i);
        r.shape("AAA", "ABA", "AAA");
        r.setIngredient('A', Material.GOLD_INGOT);
        r.setIngredient('B', Material.GOLDEN_CARROT);
        Bukkit.addRecipe(r);
        key += 1;
        String lore = k.getLore(i);
        if(!lore.isEmpty()){
            shapedRecipeMap.put(lore,r);
        }
    }

    public void r12() {
        ItemStack i = new ItemStack(Material.TRIDENT);
        NamespacedKey w12 = new NamespacedKey(plugin, "r" + key);
        ShapedRecipe r = new ShapedRecipe(w12, i);
        r.shape(" AA", " BA", "B  ");
        r.setIngredient('A', Material.END_ROD);
        r.setIngredient('B', Material.BREEZE_ROD);
        Bukkit.addRecipe(r);
        key += 1;
        String lore = k.getLore(i);
        if(!lore.isEmpty()){
            shapedRecipeMap.put(lore,r);
        }
    }

    public void r13() {
        ItemStack i = wp.echoSword();
        NamespacedKey w13 = new NamespacedKey(plugin, "r" + key);
        ShapedRecipe r = new ShapedRecipe(w13, i);
        r.shape(" A ", " A ", " B ");
        r.setIngredient('A', Material.ECHO_SHARD);
        r.setIngredient('B', Material.BREEZE_ROD);
        Bukkit.addRecipe(r);
        key += 1;
        String lore = k.getLore(i);
        if(!lore.isEmpty()){
            shapedRecipeMap.put(lore,r);
        }
    }

    public void r14() {
        ItemStack i = wp.fireSword();
        NamespacedKey w = new NamespacedKey(plugin, "r" + key);
        ShapedRecipe r = new ShapedRecipe(w, i);
        r.shape(" B ", " B ", " S ");
        r.setIngredient('B', Material.RESIN_BRICK);
        r.setIngredient('S', Material.BLAZE_ROD);
        Bukkit.addRecipe(r);
        key += 1;
        String lore = k.getLore(i);
        if(!lore.isEmpty()){
            shapedRecipeMap.put(lore,r);
        }
    }

    public void r15() {
        ItemStack i = wp.broom();
        NamespacedKey w = new NamespacedKey(plugin, "r" + key);
        ShapedRecipe r = new ShapedRecipe(w, i);
        r.shape(" A ", " B ", " B ");
        r.setIngredient('A', Material.WHEAT);
        r.setIngredient('B', Material.STICK);
        Bukkit.addRecipe(r);
        key += 1;
        String lore = k.getLore(i);
        if(!lore.isEmpty()){
            shapedRecipeMap.put(lore,r);
        }
    }

    public void r16() {
        ItemStack i = wp.raiderTool();
        NamespacedKey w = new NamespacedKey(plugin, "r" + key);
        ShapedRecipe r = new ShapedRecipe(w, i);
        r.shape(" AA", " B ", " B ");
        r.setIngredient('A', Material.IRON_INGOT);
        r.setIngredient('B', Material.BONE);
        Bukkit.addRecipe(r);
        key += 1;
        String lore = k.getLore(i);
        if(!lore.isEmpty()){
            shapedRecipeMap.put(lore,r);
        }
    }

    public void r17() {
        ItemStack i = wp.windBow();
        NamespacedKey w = new NamespacedKey(plugin, "r" + key);
        ShapedRecipe r = new ShapedRecipe(w, i);
        r.shape(" AB", "A B", " AB");
        r.setIngredient('A', Material.BREEZE_ROD);
        r.setIngredient('B', Material.IRON_CHAIN);
        Bukkit.addRecipe(r);
        key += 1;
        String lore = k.getLore(i);
        if(!lore.isEmpty()){
            shapedRecipeMap.put(lore,r);
        }
    }

    public void r18() {
        ItemStack i = wp.masterStick();
        NamespacedKey w = new NamespacedKey(plugin, "r" + key);
        ShapedRecipe r = new ShapedRecipe(w, i);
        r.shape(" A ", " B ", " C ");
        r.setIngredient('A', Material.BLAZE_ROD);
        r.setIngredient('B', Material.BREEZE_ROD);
        r.setIngredient('C', Material.BONE);
        Bukkit.addRecipe(r);
        key += 1;
        String lore = k.getLore(i);
        if(!lore.isEmpty()){
            shapedRecipeMap.put(lore,r);
        }
    }

    public void r19() {
        ItemStack i = wp.seaHammer();
        NamespacedKey w = new NamespacedKey(plugin, "r" + key);
        ShapedRecipe r = new ShapedRecipe(w, i);
        r.shape(" A ", " B ", " B ");
        r.setIngredient('A', Material.HEART_OF_THE_SEA);
        r.setIngredient('B', Material.BREEZE_ROD);
        Bukkit.addRecipe(r);
        key += 1;
        String lore = k.getLore(i);
        if(!lore.isEmpty()){
            shapedRecipeMap.put(lore,r);
        }
    }

    public void r20() {
        ItemStack i = wp.echoBow();
        NamespacedKey w = new NamespacedKey(plugin, "r" + key);
        ShapedRecipe r = new ShapedRecipe(w, i);
        r.shape(" AB", "A B", " AB");
        r.setIngredient('A', Material.ECHO_SHARD);
        r.setIngredient('B', Material.BREEZE_ROD);
        Bukkit.addRecipe(r);
        key += 1;
        String lore = k.getLore(i);
        if(!lore.isEmpty()){
            shapedRecipeMap.put(lore,r);
        }
    }

    public void r21() {
        ItemStack i = gp.snowGolem();
        NamespacedKey w = new NamespacedKey(plugin, "r" + key);
        ShapedRecipe r = new ShapedRecipe(w, i);
        r.shape("ABA", "BCB", "ABA");
        r.setIngredient('A', Material.SNOW_BLOCK);
        r.setIngredient('B', Material.ICE);
        r.setIngredient('C', Material.JACK_O_LANTERN);
        Bukkit.addRecipe(r);
        key += 1;
        String lore = k.getLore(i);
        if(!lore.isEmpty()){
            shapedRecipeMap.put(lore,r);
        }
    }

    public void r22() {
        ItemStack i = gp.ironGolem();
        NamespacedKey w = new NamespacedKey(plugin, "r" + key);
        ShapedRecipe r = new ShapedRecipe(w, i);
        r.shape("AAA", "BCB", "AAA");
        r.setIngredient('A', Material.IRON_INGOT);
        r.setIngredient('B', Material.POPPY);
        r.setIngredient('C', Material.JACK_O_LANTERN);
        Bukkit.addRecipe(r);
        key += 1;
        String lore = k.getLore(i);
        if(!lore.isEmpty()){
            shapedRecipeMap.put(lore,r);
        }
    }

    public void r23() {
        ItemStack i = gp.wolfGolem();
        NamespacedKey w = new NamespacedKey(plugin, "r" + key);
        ShapedRecipe r = new ShapedRecipe(w, i);
        r.shape("ABA", "BCB", "ABA");
        r.setIngredient('A', Material.LEATHER);
        r.setIngredient('B', Material.BONE);
        r.setIngredient('C', Material.EGG);
        Bukkit.addRecipe(r);
        key += 1;
        String lore = k.getLore(i);
        if(!lore.isEmpty()){
            shapedRecipeMap.put(lore,r);
        }
    }

    public void r24() {
        ItemStack i = gp.zombieGolem();
        NamespacedKey w = new NamespacedKey(plugin, "r" + key);
        ShapedRecipe r = new ShapedRecipe(w, i);
        r.shape("AAA", "BCB", "AAA");
        r.setIngredient('A', Material.COPPER_INGOT);
        r.setIngredient('B', Material.ROTTEN_FLESH);
        r.setIngredient('C', Material.BEETROOT_SOUP);
        Bukkit.addRecipe(r);
        key += 1;
        String lore = k.getLore(i);
        if(!lore.isEmpty()){
            shapedRecipeMap.put(lore,r);
        }
    }

    public void r25() {
        ItemStack i = gp.speedNeedle();
        NamespacedKey w = new NamespacedKey(plugin, "r" + key);
        ShapedRecipe r = new ShapedRecipe(w, i);
        r.shape(" AA", "BCA", "DB ");
        r.setIngredient('A', Material.SUGAR);
        r.setIngredient('B', Material.GLOW_INK_SAC);
        r.setIngredient('C', Material.AMETHYST_SHARD);
        r.setIngredient('D', Material.BREEZE_ROD);
        Bukkit.addRecipe(r);
        key += 1;
        String lore = k.getLore(i);
        if(!lore.isEmpty()){
            shapedRecipeMap.put(lore,r);
        }
    }

    public void r26() {
        ItemStack i = gp.healNeedle();
        NamespacedKey w = new NamespacedKey(plugin, "r" + key);
        ShapedRecipe r = new ShapedRecipe(w, i);
        r.shape(" AA", "BCA", "DB ");
        r.setIngredient('A', Material.RESIN_CLUMP);
        r.setIngredient('B', Material.GHAST_TEAR);
        r.setIngredient('C', Material.GOLDEN_APPLE);
        r.setIngredient('D', Material.BLAZE_ROD);
        Bukkit.addRecipe(r);
        key += 1;
        String lore = k.getLore(i);
        if(!lore.isEmpty()){
            shapedRecipeMap.put(lore,r);
        }
    }

    public void r27() {
        ItemStack i = gp.soup();
        NamespacedKey w = new NamespacedKey(plugin, "r" + key);
        ShapedRecipe r = new ShapedRecipe(w, i);
        r.shape("ACA", "BDB", "AEA");
        r.setIngredient('A', Material.GOLDEN_CARROT);
        r.setIngredient('B', Material.DANDELION);
        r.setIngredient('C', Material.RED_MUSHROOM);
        r.setIngredient('D', Material.BOWL);
        r.setIngredient('E', Material.BROWN_MUSHROOM);
        Bukkit.addRecipe(r);
        key += 1;
        String lore = k.getLore(i);
        if(!lore.isEmpty()){
            shapedRecipeMap.put(lore,r);
        }
    }

    public void r28() {
        ItemStack i = gp.fragNade();
        NamespacedKey w = new NamespacedKey(plugin, "r" + key);
        ShapedRecipe r = new ShapedRecipe(w, i);
        r.shape(" A ", "BCB", " A ");
        r.setIngredient('A', Material.GUNPOWDER);
        r.setIngredient('B', Material.COPPER_INGOT);
        r.setIngredient('C', Material.HONEYCOMB);
        Bukkit.addRecipe(r);
        key += 1;
        String lore = k.getLore(i);
        if(!lore.isEmpty()){
            shapedRecipeMap.put(lore,r);
        }
    }

    public void r29() {
        ItemStack i = gp.pyroNade();
        NamespacedKey w = new NamespacedKey(plugin, "r" + key);
        ShapedRecipe r = new ShapedRecipe(w, i);
        r.shape(" A ", "BCB", " A ");
        r.setIngredient('A', Material.BLAZE_POWDER);
        r.setIngredient('B', Material.FLINT);
        r.setIngredient('C', Material.HONEYCOMB);
        Bukkit.addRecipe(r);
        key += 1;
        String lore = k.getLore(i);
        if(!lore.isEmpty()){
            shapedRecipeMap.put(lore,r);
        }
    }

    public void r30() {
        ItemStack i = gp.gasNade();
        NamespacedKey w = new NamespacedKey(plugin, "r" + key);
        ShapedRecipe r = new ShapedRecipe(w, i);
        r.shape(" A ", "BCB", " A ");
        r.setIngredient('A', Material.SLIME_BALL);
        r.setIngredient('B', Material.PUFFERFISH);
        r.setIngredient('C', Material.HONEYCOMB);
        Bukkit.addRecipe(r);
        key += 1;
        String lore = k.getLore(i);
        if(!lore.isEmpty()){
            shapedRecipeMap.put(lore,r);
        }
    }

    public void r31() {
        ItemStack i = gp.smokeNade();
        NamespacedKey w = new NamespacedKey(plugin, "r" + key);
        ShapedRecipe r = new ShapedRecipe(w, i);
        r.shape(" A ", "BCB", " A ");
        r.setIngredient('A', Material.BONE);
        r.setIngredient('B', Material.QUARTZ);
        r.setIngredient('C', Material.HONEYCOMB);
        Bukkit.addRecipe(r);
        key += 1;
        String lore = k.getLore(i);
        if(!lore.isEmpty()){
            shapedRecipeMap.put(lore,r);
        }
    }

    public void r32() {
        ItemStack i = gp.glitchNade();
        NamespacedKey w = new NamespacedKey(plugin, "r" + key);
        ShapedRecipe r = new ShapedRecipe(w, i);
        r.shape(" A ", "BCB", " A ");
        r.setIngredient('A', Material.PRISMARINE_CRYSTALS);
        r.setIngredient('B', Material.POPPED_CHORUS_FRUIT);
        r.setIngredient('C', Material.HONEYCOMB);
        Bukkit.addRecipe(r);
        key += 1;
        String lore = k.getLore(i);
        if(!lore.isEmpty()){
            shapedRecipeMap.put(lore,r);
        }
    }

    public void r33() {
        ItemStack i = gp.baitNade();
        NamespacedKey w = new NamespacedKey(plugin, "r" + key);
        ShapedRecipe r = new ShapedRecipe(w, i);
        r.shape(" A ", "BCB", " A ");
        r.setIngredient('A', Material.PRISMARINE_SHARD);
        r.setIngredient('B', Material.EMERALD);
        r.setIngredient('C', Material.HONEYCOMB);
        Bukkit.addRecipe(r);
        key += 1;
        String lore = k.getLore(i);
        if(!lore.isEmpty()){
            shapedRecipeMap.put(lore,r);
        }
    }

    public void r34() {
        ItemStack i = gp.glowCamp();
        NamespacedKey w = new NamespacedKey(plugin, "r" + key);
        ShapedRecipe r = new ShapedRecipe(w, i);
        r.shape("AAA", "ABA", "AAA");
        r.setIngredient('A', Material.IRON_INGOT);
        r.setIngredient('B', Material.JUKEBOX);
        Bukkit.addRecipe(r);
        key += 1;
        String lore = k.getLore(i);
        if(!lore.isEmpty()){
            shapedRecipeMap.put(lore,r);
        }
    }

    public void r35() {
        ItemStack i = gp.fireCamp();
        NamespacedKey w = new NamespacedKey(plugin, "r" + key);
        ShapedRecipe r = new ShapedRecipe(w, i);
        r.shape("AAA", "ABA", "AAA");
        r.setIngredient('A', Material.GOLD_INGOT);
        r.setIngredient('B', Material.JUKEBOX);
        Bukkit.addRecipe(r);
        key += 1;
        String lore = k.getLore(i);
        if(!lore.isEmpty()){
            shapedRecipeMap.put(lore,r);
        }
    }

    public void r36() {
        ItemStack i = gp.explodeMine();
        NamespacedKey w = new NamespacedKey(plugin, "r" + key);
        ShapedRecipe r = new ShapedRecipe(w, i);
        r.shape("AAA", "BCB", "AAA");
        r.setIngredient('A', Material.GUNPOWDER);
        r.setIngredient('B', Material.LAPIS_LAZULI);
        r.setIngredient('C', Material.ARMOR_STAND);
        Bukkit.addRecipe(r);
        key += 1;
        String lore = k.getLore(i);
        if(!lore.isEmpty()){
            shapedRecipeMap.put(lore,r);
        }
    }

    public void r37() {
        ItemStack i = gp.gasMine();
        NamespacedKey w = new NamespacedKey(plugin, "r" + key);
        ShapedRecipe r = new ShapedRecipe(w, i);
        r.shape("AAA", "BCB", "AAA");
        r.setIngredient('A', Material.ROTTEN_FLESH);
        r.setIngredient('B', Material.LAPIS_LAZULI);
        r.setIngredient('C', Material.ARMOR_STAND);
        Bukkit.addRecipe(r);
        key += 1;
        String lore = k.getLore(i);
        if(!lore.isEmpty()){
            shapedRecipeMap.put(lore,r);
        }
    }

    public void r38() {
        ItemStack i = gp.pyroMine();
        NamespacedKey w = new NamespacedKey(plugin, "r" + key);
        ShapedRecipe r = new ShapedRecipe(w, i);
        r.shape("AAA", "BCB", "AAA");
        r.setIngredient('A', Material.GOLD_NUGGET);
        r.setIngredient('B', Material.LAPIS_LAZULI);
        r.setIngredient('C', Material.ARMOR_STAND);
        Bukkit.addRecipe(r);
        key += 1;
        String lore = k.getLore(i);
        if(!lore.isEmpty()){
            shapedRecipeMap.put(lore,r);
        }
    }

    public void r39() {
        ItemStack i = gp.slowMine();
        NamespacedKey w = new NamespacedKey(plugin, "r" + key);
        ShapedRecipe r = new ShapedRecipe(w, i);
        r.shape("AAA", "BCB", "AAA");
        r.setIngredient('A', Material.COAL);
        r.setIngredient('B', Material.LAPIS_LAZULI);
        r.setIngredient('C', Material.ARMOR_STAND);
        Bukkit.addRecipe(r);
        key += 1;
        String lore = k.getLore(i);
        if(!lore.isEmpty()){
            shapedRecipeMap.put(lore,r);
        }
    }

    public void r40() {
        ItemStack item = gp.energyDrink().clone();
        item.setAmount(4);
        NamespacedKey w = new NamespacedKey(plugin, "r" + key);
        ShapedRecipe r = new ShapedRecipe(w, item);
        r.shape("ABA", "BCB", "ABA");
        r.setIngredient('A', Material.COPPER_NUGGET);
        r.setIngredient('B', Material.SUGAR);
        r.setIngredient('C', Material.WATER_BUCKET);
        Bukkit.addRecipe(r);
        key += 1;
        String lore = k.getLore(item);
        if(!lore.isEmpty()){
            shapedRecipeMap.put(lore,r);
        }
    }

    public void r41() {
        ItemStack i = gp.meat();
        NamespacedKey w = new NamespacedKey(plugin, "r" + key);
        ShapedRecipe r = new ShapedRecipe(w, i);
        r.shape("ABA", "CDE", "AFA");
        r.setIngredient('A', Material.ROTTEN_FLESH);
        r.setIngredient('B', Material.COOKED_PORKCHOP);
        r.setIngredient('C', Material.COOKED_MUTTON);
        r.setIngredient('D', Material.FERMENTED_SPIDER_EYE);
        r.setIngredient('E', Material.COOKED_CHICKEN);
        r.setIngredient('F', Material.COOKED_BEEF);
        Bukkit.addRecipe(r);
        key += 1;
        String lore = k.getLore(i);
        if(!lore.isEmpty()){
            shapedRecipeMap.put(lore,r);
        }
    }

    public void r42() {
        NamespacedKey w = new NamespacedKey(plugin, "r" + key);
        ShapedRecipe r = new ShapedRecipe(w, ap.dirtHelm());
        r.shape("AAA", "A A", "   ");
        r.setIngredient('A', Material.DIRT);
        Bukkit.addRecipe(r);
        key += 1;
    }

    public void r43() {
        NamespacedKey w = new NamespacedKey(plugin, "r" + key);
        ShapedRecipe r = new ShapedRecipe(w, ap.dirtChest());
        r.shape("A A", "AAA", "AAA");
        r.setIngredient('A', Material.DIRT);
        Bukkit.addRecipe(r);
        key += 1;
    }

    public void r44() {
        NamespacedKey w = new NamespacedKey(plugin, "r" + key);
        ShapedRecipe r = new ShapedRecipe(w, ap.dirtLeg());
        r.shape("AAA", "A A", "A A");
        r.setIngredient('A', Material.DIRT);
        Bukkit.addRecipe(r);
        key += 1;
    }

    public void r45() {
        NamespacedKey w = new NamespacedKey(plugin, "r" + key);
        ShapedRecipe r = new ShapedRecipe(w, ap.dirtBoot());
        r.shape("A A", "A A", "   ");
        r.setIngredient('A', Material.DIRT);
        Bukkit.addRecipe(r);
        key += 1;
    }

    public void r46() {
        NamespacedKey w = new NamespacedKey(plugin, "r" + key);
        ShapedRecipe r = new ShapedRecipe(w, ap.woodHelm());
        r.shape("AAA", "A A", "   ");
        r.setIngredient('A', Material.BIRCH_PLANKS);
        Bukkit.addRecipe(r);
        key += 1;
    }

    public void r47() {
        NamespacedKey w = new NamespacedKey(plugin, "r" + key);
        ShapedRecipe r = new ShapedRecipe(w, ap.woodChest());
        r.shape("A A", "AAA", "AAA");
        r.setIngredient('A', Material.BIRCH_PLANKS);
        Bukkit.addRecipe(r);
        key += 1;
    }

    public void r48() {
        NamespacedKey w = new NamespacedKey(plugin, "r" + key);
        ShapedRecipe r = new ShapedRecipe(w, ap.woodLeg());
        r.shape("AAA", "A A", "A A");
        r.setIngredient('A', Material.BIRCH_PLANKS);
        Bukkit.addRecipe(r);
        key += 1;
    }

    public void r49() {
        NamespacedKey w = new NamespacedKey(plugin, "r" + key);
        ShapedRecipe r = new ShapedRecipe(w, ap.woodBoot());
        r.shape("A A", "A A", "   ");
        r.setIngredient('A', Material.BIRCH_PLANKS);
        Bukkit.addRecipe(r);
        key += 1;
    }

    public void r50() {
        NamespacedKey w = new NamespacedKey(plugin, "r" + key);
        ShapedRecipe r = new ShapedRecipe(w, ap.featherHelm());
        r.shape("AAA", "A A", "   ");
        r.setIngredient('A', Material.FEATHER);
        Bukkit.addRecipe(r);
        key += 1;
    }

    public void r51() {
        NamespacedKey w = new NamespacedKey(plugin, "r" + key);
        ShapedRecipe r = new ShapedRecipe(w, ap.featherChest());
        r.shape("A A", "AAA", "AAA");
        r.setIngredient('A', Material.FEATHER);
        Bukkit.addRecipe(r);
        key += 1;
    }

    public void r52() {
        NamespacedKey w = new NamespacedKey(plugin, "r" + key);
        ShapedRecipe r = new ShapedRecipe(w, ap.featherLeg());
        r.shape("AAA", "A A", "A A");
        r.setIngredient('A', Material.FEATHER);
        Bukkit.addRecipe(r);
        key += 1;
    }

    public void r53() {
        NamespacedKey w = new NamespacedKey(plugin, "r" + key);
        ShapedRecipe r = new ShapedRecipe(w, ap.featherBoot());
        r.shape("A A", "A A", "   ");
        r.setIngredient('A', Material.FEATHER);
        Bukkit.addRecipe(r);
        key += 1;
    }

    public void r54() {
        NamespacedKey w = new NamespacedKey(plugin, "r" + key);
        ShapedRecipe r = new ShapedRecipe(w, ap.boneHelm());
        r.shape("AAA", "A A", "   ");
        r.setIngredient('A', Material.BONE);
        Bukkit.addRecipe(r);
        key += 1;
    }

    public void r55() {
        NamespacedKey w = new NamespacedKey(plugin, "r" + key);
        ShapedRecipe r = new ShapedRecipe(w, ap.boneChest());
        r.shape("A A", "AAA", "AAA");
        r.setIngredient('A', Material.BONE);
        Bukkit.addRecipe(r);
        key += 1;
    }

    public void r56() {
        NamespacedKey w = new NamespacedKey(plugin, "r" + key);
        ShapedRecipe r = new ShapedRecipe(w, ap.boneLeg());
        r.shape("AAA", "A A", "A A");
        r.setIngredient('A', Material.BONE);
        Bukkit.addRecipe(r);
        key += 1;
    }

    public void r57() {
        NamespacedKey w = new NamespacedKey(plugin, "r" + key);
        ShapedRecipe r = new ShapedRecipe(w, ap.boneBoot());
        r.shape("A A", "A A", "   ");
        r.setIngredient('A', Material.BONE);
        Bukkit.addRecipe(r);
        key += 1;
    }

    public void r58() {
        NamespacedKey w = new NamespacedKey(plugin, "r" + key);
        ShapedRecipe r = new ShapedRecipe(w, ap.cactusHelm());
        r.shape("AAA", "A A", "   ");
        r.setIngredient('A', Material.CACTUS);
        Bukkit.addRecipe(r);
        key += 1;
    }

    public void r59() {
        NamespacedKey w = new NamespacedKey(plugin, "r" + key);
        ShapedRecipe r = new ShapedRecipe(w, ap.cactusLeg());
        r.shape("A A", "AAA", "AAA");
        r.setIngredient('A', Material.CACTUS);
        Bukkit.addRecipe(r);
        key += 1;
    }

    public void r60() {
        NamespacedKey w = new NamespacedKey(plugin, "r" + key);
        ShapedRecipe r = new ShapedRecipe(w, ap.cactusLeg());
        r.shape("AAA", "A A", "A A");
        r.setIngredient('A', Material.CACTUS);
        Bukkit.addRecipe(r);
        key += 1;
    }

    public void r61() {
        NamespacedKey w = new NamespacedKey(plugin, "r" + key);
        ShapedRecipe r = new ShapedRecipe(w, ap.cactusBoot());
        r.shape("A A", "A A", "   ");
        r.setIngredient('A', Material.CACTUS);
        Bukkit.addRecipe(r);
        key += 1;
    }

    public void r62() {
        NamespacedKey w = new NamespacedKey(plugin, "r" + key);
        ShapedRecipe r = new ShapedRecipe(w, ap.flintHelm());
        r.shape("AAA", "A A", "   ");
        r.setIngredient('A', Material.FLINT);
        Bukkit.addRecipe(r);
        key += 1;
    }

    public void r63() {
        NamespacedKey w = new NamespacedKey(plugin, "r" + key);
        ShapedRecipe r = new ShapedRecipe(w, ap.flintChest());
        r.shape("A A", "AAA", "AAA");
        r.setIngredient('A', Material.FLINT);
        Bukkit.addRecipe(r);
        key += 1;
    }

    public void r64() {
        NamespacedKey w = new NamespacedKey(plugin, "r" + key);
        ShapedRecipe r = new ShapedRecipe(w, ap.featherLeg());
        r.shape("AAA", "A A", "A A");
        r.setIngredient('A', Material.FLINT);
        Bukkit.addRecipe(r);
        key += 1;
    }

    public void r65() {
        NamespacedKey w = new NamespacedKey(plugin, "r" + key);
        ShapedRecipe r = new ShapedRecipe(w, ap.flintBoot());
        r.shape("A A", "A A", "   ");
        r.setIngredient('A', Material.FLINT);
        Bukkit.addRecipe(r);
        key += 1;
    }

    public void r66() {
        NamespacedKey w = new NamespacedKey(plugin, "r" + key);
        ShapedRecipe r = new ShapedRecipe(w, ap.treeHelm());
        r.shape("AAA", "A A", "   ");
        r.setIngredient('A', Material.RESIN_BRICK);
        Bukkit.addRecipe(r);
        key += 1;
    }

    public void r67() {
        NamespacedKey w = new NamespacedKey(plugin, "r" + key);
        ShapedRecipe r = new ShapedRecipe(w, ap.treeChest());
        r.shape("A A", "AAA", "AAA");
        r.setIngredient('A', Material.RESIN_BRICK);
        Bukkit.addRecipe(r);
        key += 1;
    }

    public void r68() {
        NamespacedKey w = new NamespacedKey(plugin, "r" + key);
        ShapedRecipe r = new ShapedRecipe(w, ap.treeLeg());
        r.shape("AAA", "A A", "A A");
        r.setIngredient('A', Material.RESIN_BRICK);
        Bukkit.addRecipe(r);
        key += 1;
    }

    public void r69() {
        NamespacedKey w = new NamespacedKey(plugin, "r" + key);
        ShapedRecipe r = new ShapedRecipe(w, ap.treeBoot());
        r.shape("A A", "A A", "   ");
        r.setIngredient('A', Material.RESIN_BRICK);
        Bukkit.addRecipe(r);
        key += 1;
    }

    public void r70() {
        NamespacedKey w = new NamespacedKey(plugin, "r" + key);
        ShapedRecipe r = new ShapedRecipe(w, ap.stoneHelm());
        r.shape("AAA", "A A", "   ");
        r.setIngredient('A', Material.COBBLESTONE);
        Bukkit.addRecipe(r);
        key += 1;
    }

    public void r71() {
        NamespacedKey w = new NamespacedKey(plugin, "r" + key);
        ShapedRecipe r = new ShapedRecipe(w, ap.stoneChest());
        r.shape("A A", "AAA", "AAA");
        r.setIngredient('A', Material.COBBLESTONE);
        Bukkit.addRecipe(r);
        key += 1;
    }

    public void r72() {
        NamespacedKey w = new NamespacedKey(plugin, "r" + key);
        ShapedRecipe r = new ShapedRecipe(w, ap.stoneLeg());
        r.shape("AAA", "A A", "A A");
        r.setIngredient('A', Material.COBBLESTONE);
        Bukkit.addRecipe(r);
        key += 1;
    }

    public void r73() {
        NamespacedKey w = new NamespacedKey(plugin, "r" + key);
        ShapedRecipe r = new ShapedRecipe(w, ap.stoneBoot());
        r.shape("A A", "A A", "   ");
        r.setIngredient('A', Material.COBBLESTONE);
        Bukkit.addRecipe(r);
        key += 1;
    }

    public void r74() {
        NamespacedKey w = new NamespacedKey(plugin, "r" + key);
        ShapedRecipe r = new ShapedRecipe(w, ap.quartzHelm());
        r.shape("AAA", "A A", "   ");
        r.setIngredient('A', Material.QUARTZ_BLOCK);
        Bukkit.addRecipe(r);
        key += 1;
    }

    public void r75() {
        NamespacedKey w = new NamespacedKey(plugin, "r" + key);
        ShapedRecipe r = new ShapedRecipe(w, ap.quartzChest());
        r.shape("A A", "AAA", "AAA");
        r.setIngredient('A', Material.QUARTZ_BLOCK);
        Bukkit.addRecipe(r);
        key += 1;
    }

    public void r76() {
        NamespacedKey w = new NamespacedKey(plugin, "r" + key);
        ShapedRecipe r = new ShapedRecipe(w, ap.quartzLeg());
        r.shape("AAA", "A A", "A A");
        r.setIngredient('A', Material.QUARTZ_BLOCK);
        Bukkit.addRecipe(r);
        key += 1;
    }

    public void r77() {
        NamespacedKey w = new NamespacedKey(plugin, "r" + key);
        ShapedRecipe r = new ShapedRecipe(w, ap.quartzBoot());
        r.shape("A A", "A A", "   ");
        r.setIngredient('A', Material.QUARTZ_BLOCK);
        Bukkit.addRecipe(r);
        key += 1;
    }

    public void r78() {
        NamespacedKey w = new NamespacedKey(plugin, "r" + key);
        ShapedRecipe r = new ShapedRecipe(w, ap.phantomHelm());
        r.shape("AAA", "A A", "   ");
        r.setIngredient('A', Material.PHANTOM_MEMBRANE);
        Bukkit.addRecipe(r);
        key += 1;
    }

    public void r79() {
        NamespacedKey w = new NamespacedKey(plugin, "r" + key);
        ShapedRecipe r = new ShapedRecipe(w, ap.phantomChest());
        r.shape("A A", "AAA", "AAA");
        r.setIngredient('A', Material.PHANTOM_MEMBRANE);
        Bukkit.addRecipe(r);
        key += 1;
    }

    public void r80() {
        NamespacedKey w = new NamespacedKey(plugin, "r" + key);
        ShapedRecipe r = new ShapedRecipe(w, ap.phantomLeg());
        r.shape("AAA", "A A", "A A");
        r.setIngredient('A', Material.PHANTOM_MEMBRANE);
        Bukkit.addRecipe(r);
        key += 1;
    }

    public void r81() {
        NamespacedKey w = new NamespacedKey(plugin, "r" + key);
        ShapedRecipe r = new ShapedRecipe(w, ap.phantomBoot());
        r.shape("A A", "A A", "   ");
        r.setIngredient('A', Material.PHANTOM_MEMBRANE);
        Bukkit.addRecipe(r);
        key += 1;
    }

    public void r82() {
        NamespacedKey w = new NamespacedKey(plugin, "r" + key);
        ShapedRecipe r = new ShapedRecipe(w, ap.purpleHelm());
        r.shape("AAA", "A A", "   ");
        r.setIngredient('A', Material.AMETHYST_SHARD);
        Bukkit.addRecipe(r);
        key += 1;
    }

    public void r83() {
        NamespacedKey w = new NamespacedKey(plugin, "r" + key);
        ShapedRecipe r = new ShapedRecipe(w, ap.purpleChest());
        r.shape("A A", "AAA", "AAA");
        r.setIngredient('A', Material.AMETHYST_SHARD);
        Bukkit.addRecipe(r);
        key += 1;
    }

    public void r84() {
        NamespacedKey w = new NamespacedKey(plugin, "r" + key);
        ShapedRecipe r = new ShapedRecipe(w, ap.purpleLeg());
        r.shape("AAA", "A A", "A A");
        r.setIngredient('A', Material.AMETHYST_SHARD);
        Bukkit.addRecipe(r);
        key += 1;
    }

    public void r85() {
        NamespacedKey w = new NamespacedKey(plugin, "r" + key);
        ShapedRecipe r = new ShapedRecipe(w, ap.purpleBoot());
        r.shape("A A", "A A", "   ");
        r.setIngredient('A', Material.AMETHYST_SHARD);
        Bukkit.addRecipe(r);
        key += 1;
    }

    public void r86() {
        NamespacedKey w = new NamespacedKey(plugin, "r" + key);
        ShapedRecipe r = new ShapedRecipe(w, ap.greenHelm());
        r.shape("AAA", "A A", "   ");
        r.setIngredient('A', Material.EMERALD);
        Bukkit.addRecipe(r);
        key += 1;
    }

    public void r87() {
        NamespacedKey w = new NamespacedKey(plugin, "r" + key);
        ShapedRecipe r = new ShapedRecipe(w, ap.greenChest());
        r.shape("A A", "AAA", "AAA");
        r.setIngredient('A', Material.EMERALD);
        Bukkit.addRecipe(r);
        key += 1;
    }

    public void r88() {
        NamespacedKey w = new NamespacedKey(plugin, "r" + key);
        ShapedRecipe r = new ShapedRecipe(w, ap.greenLeg());
        r.shape("AAA", "A A", "A A");
        r.setIngredient('A', Material.EMERALD);
        Bukkit.addRecipe(r);
        key += 1;
    }

    public void r89() {
        NamespacedKey w = new NamespacedKey(plugin, "r" + key);
        ShapedRecipe r = new ShapedRecipe(w, ap.greenBoot());
        r.shape("A A", "A A", "   ");
        r.setIngredient('A', Material.EMERALD);
        Bukkit.addRecipe(r);
        key += 1;
    }

    public void r90() {
        NamespacedKey w = new NamespacedKey(plugin, "r" + key);
        ShapedRecipe r = new ShapedRecipe(w, ap.obHelm());
        r.shape("AAA", "A A", "   ");
        r.setIngredient('A', Material.OBSIDIAN);
        Bukkit.addRecipe(r);
        key += 1;
    }

    public void r91() {
        NamespacedKey w = new NamespacedKey(plugin, "r" + key);
        ShapedRecipe r = new ShapedRecipe(w, ap.obChest());
        r.shape("A A", "AAA", "AAA");
        r.setIngredient('A', Material.OBSIDIAN);
        Bukkit.addRecipe(r);
        key += 1;
    }

    public void r92() {
        NamespacedKey w = new NamespacedKey(plugin, "r" + key);
        ShapedRecipe r = new ShapedRecipe(w, ap.obLeg());
        r.shape("AAA", "A A", "A A");
        r.setIngredient('A', Material.OBSIDIAN);
        Bukkit.addRecipe(r);
        key += 1;
    }

    public void r93() {
        NamespacedKey w = new NamespacedKey(plugin, "r" + key);
        ShapedRecipe r = new ShapedRecipe(w, ap.obBoot());
        r.shape("A A", "A A", "   ");
        r.setIngredient('A', Material.OBSIDIAN);
        Bukkit.addRecipe(r);
        key += 1;
    }

    public void r94() {
        NamespacedKey w = new NamespacedKey(plugin, "r" + key);
        ShapedRecipe r = new ShapedRecipe(w, ap.echoHelm());
        r.shape("AAA", "A A", "   ");
        r.setIngredient('A', Material.ECHO_SHARD);
        Bukkit.addRecipe(r);
        key += 1;
    }

    public void r95() {
        NamespacedKey w = new NamespacedKey(plugin, "r" + key);
        ShapedRecipe r = new ShapedRecipe(w, ap.echoChest());
        r.shape("A A", "AAA", "AAA");
        r.setIngredient('A', Material.ECHO_SHARD);
        Bukkit.addRecipe(r);
        key += 1;
    }

    public void r96() {
        NamespacedKey w = new NamespacedKey(plugin, "r" + key);
        ShapedRecipe r = new ShapedRecipe(w, ap.echoLeg());
        r.shape("AAA", "A A", "A A");
        r.setIngredient('A', Material.ECHO_SHARD);
        Bukkit.addRecipe(r);
        key += 1;
    }

    public void r97() {
        NamespacedKey w = new NamespacedKey(plugin, "r" + key);
        ShapedRecipe r = new ShapedRecipe(w, ap.echoBoot());
        r.shape("A A", "A A", "   ");
        r.setIngredient('A', Material.ECHO_SHARD);
        Bukkit.addRecipe(r);
        key += 1;
    }

    public void r98() {
        NamespacedKey w = new NamespacedKey(plugin, "r" + key);
        ShapedRecipe r = new ShapedRecipe(w, new ItemStack(Material.CHAINMAIL_HELMET));
        r.shape("AAA", "A A", "   ");
        r.setIngredient('A', Material.IRON_NUGGET);
        Bukkit.addRecipe(r);
        key += 1;
    }

    public void r99() {
        NamespacedKey w = new NamespacedKey(plugin, "r" + key);
        ShapedRecipe r = new ShapedRecipe(w, new ItemStack(Material.CHAINMAIL_CHESTPLATE));
        r.shape("A A", "AAA", "AAA");
        r.setIngredient('A', Material.IRON_NUGGET);
        Bukkit.addRecipe(r);
        key += 1;
    }

    public void r100() {
        NamespacedKey w = new NamespacedKey(plugin, "r" + key);
        ShapedRecipe r = new ShapedRecipe(w, new ItemStack(Material.CHAINMAIL_LEGGINGS));
        r.shape("AAA", "A A", "A A");
        r.setIngredient('A', Material.IRON_NUGGET);
        Bukkit.addRecipe(r);
        key += 1;
    }

    public void r101() {
        NamespacedKey w = new NamespacedKey(plugin, "r" + key);
        ShapedRecipe r = new ShapedRecipe(w, new ItemStack(Material.CHAINMAIL_BOOTS));
        r.shape("A A", "A A", "   ");
        r.setIngredient('A', Material.IRON_NUGGET);
        Bukkit.addRecipe(r);
        key += 1;
    }

    public void r102() {
        NamespacedKey w = new NamespacedKey(plugin, "r" + key);
        ShapedRecipe r = new ShapedRecipe(w, ap.wheatHelm());
        r.shape("AAA", "A A", "   ");
        r.setIngredient('A', Material.WHEAT);
        Bukkit.addRecipe(r);
        key += 1;
    }

    public void r103() {
        NamespacedKey w = new NamespacedKey(plugin, "r" + key);
        ShapedRecipe r = new ShapedRecipe(w, ap.wheatChest());
        r.shape("A A", "AAA", "AAA");
        r.setIngredient('A', Material.WHEAT);
        Bukkit.addRecipe(r);
        key += 1;
    }

    public void r104() {
        NamespacedKey w = new NamespacedKey(plugin, "r" + key);
        ShapedRecipe r = new ShapedRecipe(w, ap.wheatLeg());
        r.shape("AAA", "A A", "A A");
        r.setIngredient('A', Material.WHEAT);
        Bukkit.addRecipe(r);
        key += 1;
    }

    public void r105() {
        NamespacedKey w = new NamespacedKey(plugin, "r" + key);
        ShapedRecipe r = new ShapedRecipe(w, ap.wheatBoot());
        r.shape("A A", "A A", "   ");
        r.setIngredient('A', Material.WHEAT);
        Bukkit.addRecipe(r);
        key += 1;
    }

    public void r106() {
        ItemStack item = gp.energyDrinkPro().clone();
        item.setAmount(4);
        NamespacedKey w = new NamespacedKey(plugin, "r" + key);
        ShapedRecipe r = new ShapedRecipe(w, item);
        r.shape("ABA", "BCB", "ABA");
        r.setIngredient('A', Material.IRON_NUGGET);
        r.setIngredient('B', Material.HONEYCOMB);
        r.setIngredient('C', Material.WATER_BUCKET);
        Bukkit.addRecipe(r);
        key += 1;
        String lore = k.getLore(item);
        if(!lore.isEmpty()){
            shapedRecipeMap.put(lore,r);
        }
    }

    public void r107() {
        ItemStack item = gp.energyDrinkProMax().clone();
        item.setAmount(4);
        NamespacedKey w = new NamespacedKey(plugin, "r" + key);
        ShapedRecipe r = new ShapedRecipe(w, item);
        r.shape("ABA", "BCB", "ABA");
        r.setIngredient('A', Material.GOLD_NUGGET);
        r.setIngredient('B', Material.RESIN_CLUMP);
        r.setIngredient('C', Material.WATER_BUCKET);
        Bukkit.addRecipe(r);
        key += 1;
        String lore = k.getLore(item);
        if(!lore.isEmpty()){
            shapedRecipeMap.put(lore,r);
        }
    }

    public void r108() {
        ItemStack i = lp.wolfPack();
        NamespacedKey w = new NamespacedKey(plugin, "r" + key);
        ShapedRecipe r = new ShapedRecipe(w, i);
        r.shape("ABA", "ACA", "ADA");
        r.setIngredient('A', Material.AMETHYST_SHARD);
        r.setIngredient('B', Material.END_CRYSTAL);
        r.setIngredient('C', Material.FIREWORK_ROCKET);
        r.setIngredient('D', Material.TNT);
        Bukkit.addRecipe(r);
        key += 1;
        String lore = k.getLore(i);
        if(!lore.isEmpty()){
            shapedRecipeMap.put(lore,r);
        }
    }

    public void r109() {
        ItemStack i = gp.copperBattery();
        NamespacedKey w = new NamespacedKey(plugin, "r" + key);
        ShapedRecipe r = new ShapedRecipe(w, i);
        r.shape("AB ", "BB ", "   ");
        r.setIngredient('A', Material.EGG);
        r.setIngredient('B', Material.COPPER_NUGGET);
        Bukkit.addRecipe(r);
        key += 1;
        String lore = k.getLore(i);
        if(!lore.isEmpty()){
            shapedRecipeMap.put(lore,r);
        }
    }

    public void r110() {
        ItemStack i = gp.ironBattery();
        NamespacedKey w = new NamespacedKey(plugin, "r" + key);
        ShapedRecipe r = new ShapedRecipe(w, i);
        r.shape("AB ", "BB ", "   ");
        r.setIngredient('A', Material.EGG);
        r.setIngredient('B', Material.IRON_NUGGET);
        Bukkit.addRecipe(r);
        key += 1;
        String lore = k.getLore(i);
        if(!lore.isEmpty()){
            shapedRecipeMap.put(lore,r);
        }
    }

    public void r111() {
        ItemStack i = gp.goldenBattery();
        NamespacedKey w = new NamespacedKey(plugin, "r" + key);
        ShapedRecipe r = new ShapedRecipe(w, i);
        r.shape("AB ", "BB ", "   ");
        r.setIngredient('A', Material.EGG);
        r.setIngredient('B', Material.GOLD_NUGGET);
        Bukkit.addRecipe(r);
        key += 1;
        String lore = k.getLore(i);
        if(!lore.isEmpty()){
            shapedRecipeMap.put(lore,r);
        }
    }

    public void r112() {
        ItemStack i = gp.netherBattery().clone();
        i.setAmount(4);
        NamespacedKey w = new NamespacedKey(plugin, "r" + key);
        ShapedRecipe r = new ShapedRecipe(w, i);
        r.shape(" A ", "ABA", " A ");
        r.setIngredient('A', Material.EGG);
        r.setIngredient('B', Material.DIAMOND);
        Bukkit.addRecipe(r);
        key += 1;
        String lore = k.getLore(i);
        if(!lore.isEmpty()){
            shapedRecipeMap.put(lore,r);
        }
    }

    public void r113() {
        ItemStack i = gp.netherBattery().clone();
        i.setAmount(8);
        NamespacedKey w = new NamespacedKey(plugin, "r" + key);
        ShapedRecipe r = new ShapedRecipe(w, i);
        r.shape("AAA", "ABA", "AAA");
        r.setIngredient('A', Material.EGG);
        r.setIngredient('B', Material.NETHERITE_SCRAP);
        Bukkit.addRecipe(r);
        key += 1;
        String lore = k.getLore(i);
        if(!lore.isEmpty()){
            shapedRecipeMap.put(lore,r);
        }
    }

    public void r114() {
        ItemStack i = wp.ferro();
        NamespacedKey w = new NamespacedKey(plugin, "r" + key);
        ShapedRecipe r = new ShapedRecipe(w, i);
        r.shape("ADA", "BCB", " A ");
        r.setIngredient('A', Material.BONE);
        r.setIngredient('B', Material.STRING);
        r.setIngredient('C', Material.TRIPWIRE_HOOK);
        r.setIngredient('D', Material.COPPER_INGOT);
        Bukkit.addRecipe(r);
        key += 1;
        String lore = k.getLore(i);
        if(!lore.isEmpty()){
            shapedRecipeMap.put(lore,r);
        }
    }

    public void r115() {
        ItemStack i = lp.deadLine();
        NamespacedKey w = new NamespacedKey(plugin, "r" + key);
        ShapedRecipe r = new ShapedRecipe(w, i);
        r.shape("ACA", "BDB", "ACA");
        r.setIngredient('A', Material.DIAMOND);
        r.setIngredient('B', Material.LODESTONE);
        r.setIngredient('C', Material.TNT);
        r.setIngredient('D', Material.END_CRYSTAL);
        Bukkit.addRecipe(r);
        key += 1;
        String lore = k.getLore(i);
        if(!lore.isEmpty()){
            shapedRecipeMap.put(lore,r);
        }
    }

    public void r116() {
        ItemStack i = wp.echoCrossBow();
        NamespacedKey w = new NamespacedKey(plugin, "r" + key);
        ShapedRecipe r = new ShapedRecipe(w, i);
        r.shape("ADA", "BCB", " A ");
        r.setIngredient('A', Material.BREEZE_ROD);
        r.setIngredient('B', Material.IRON_CHAIN);
        r.setIngredient('C', Material.ANCIENT_DEBRIS);
        r.setIngredient('D', Material.ECHO_SHARD);
        Bukkit.addRecipe(r);
        key += 1;
        String lore = k.getLore(i);
        if(!lore.isEmpty()){
            shapedRecipeMap.put(lore,r);
        }
    }
}

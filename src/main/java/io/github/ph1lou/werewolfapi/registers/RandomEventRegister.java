package io.github.ph1lou.werewolfapi.registers;

import io.github.ph1lou.werewolfapi.ListenerManager;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Ph1Lou
 */
@SuppressWarnings("unused")

public class RandomEventRegister implements IRegister {


    private final String key;
    private final AddonRegister addonRegister;
    private int defaultValue = 0;
    private List<String> loreKey =new ArrayList<>();
    private final ListenerManager randomEvent;
    private ItemStack item=null;

    public RandomEventRegister(AddonRegister addonRegister, String key, ListenerManager randomEvent) {
        this.addonRegister = addonRegister;
        this.key=key;
        this.randomEvent=randomEvent;
    }

    public int getDefaultValue(){
        return defaultValue;
    }

    public RandomEventRegister setDefaultValue(int probability){
        this.defaultValue=probability;
        return this;
    }


    public RandomEventRegister setLoreKey(List<String> loreKey){
        this.loreKey = loreKey;
        return this;
    }

    public RandomEventRegister setLoreKey(String lore){
        this.loreKey.add(lore);
        return this;
    }

    public List<String> getLoreKey() {
        return loreKey;
    }

    @Override
    public AddonRegister getAddon() {
        return this.addonRegister;
    }

    @Override
    public String getKey() {
        return key;
    }

    public ListenerManager getRandomEvent() {
        return randomEvent;
    }

    public ItemStack getItem() {
        return item;
    }

    public RandomEventRegister setItem(ItemStack item) {
        this.item = item;
        return this;
    }
}

package org.elliotnash.chestlocker.materialutils;

import org.bukkit.Material; 

public class Materials12 implements MaterialUtils{
    public boolean isLockable(Material mat){
        return mat == Material.CHEST
                || mat == Material.TRAPPED_CHEST
                || mat == Material.FURNACE
                || mat == Material.DISPENSER
                || mat == Material.DROPPER
                || mat == Material.BREWING_STAND
                || mat == Material.HOPPER
                || mat == Material.BEACON
                || mat == Material.ENDER_CHEST
                || isShulker(mat);
    }

    public boolean isShulker(Material mat){
        try{
        return mat == Material.BLACK_SHULKER_BOX
                || mat == Material.BLUE_SHULKER_BOX
                || mat == Material.BROWN_SHULKER_BOX
                || mat == Material.CYAN_SHULKER_BOX
                || mat == Material.GRAY_SHULKER_BOX
                || mat == Material.GREEN_SHULKER_BOX
                || mat == Material.LIGHT_BLUE_SHULKER_BOX
                || mat == (Material) Material.class.getField("SILVER_SHULKER_BOX").get(this) //thanks minecraft
                || mat == Material.LIME_SHULKER_BOX
                || mat == Material.MAGENTA_SHULKER_BOX
                || mat == Material.ORANGE_SHULKER_BOX
                || mat == Material.PINK_SHULKER_BOX
                || mat == Material.PURPLE_SHULKER_BOX
                || mat == Material.RED_SHULKER_BOX
                || mat == Material.WHITE_SHULKER_BOX
                || mat == Material.YELLOW_SHULKER_BOX;
        } catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public boolean canBeDouble(Material mat){
        return mat == Material.CHEST
                || mat == Material.TRAPPED_CHEST;
    }

    public boolean isDrainable(Material mat){
        return mat == Material.CHEST
                || mat == Material.TRAPPED_CHEST
                || mat == Material.DISPENSER
                || mat == Material.DROPPER;
    }
}

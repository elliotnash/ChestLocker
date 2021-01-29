package org.elliotnash.chestlocker.materialutils;

import org.bukkit.Material;

public interface MaterialUtils {
    public boolean isLockable(Material mat);

    public boolean isShulker(Material mat);

    public boolean canBeDouble(Material mat);

    public boolean isDrainable(Material mat);
}
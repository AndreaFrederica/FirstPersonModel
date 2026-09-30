package dev.tr7zw.firstperson.api;

import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.util.math.Vec3d;

@FunctionalInterface
public interface PlayerOffsetHandler {
    Vec3d applyOffset(AbstractClientPlayer entity, float delta, Vec3d original, Vec3d current);
}

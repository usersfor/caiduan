package com.example.util;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;

/**
 * 只负责 6 个锻造属性的序列化/反序列化
 * 不依赖 PlayerEntity，也不依赖锻造台
 */
public final class DataUtil {

    public final float tidalRate;
    public final float starMark;
    public final float mountainStand;
    public final float moonReflection;
    public final float cloudRoam;
    public final float skyBalance;

    /* ===== 构造：直接给值 ===== */
    public DataUtil(float tidalRate,
                    float starMark,
                    float mountainStand,
                    float moonReflection,
                    float cloudRoam,
                    float skyBalance) {
        this.tidalRate      = tidalRate;
        this.starMark       = starMark;
        this.mountainStand  = mountainStand;
        this.moonReflection = moonReflection;
        this.cloudRoam      = cloudRoam;
        this.skyBalance     = skyBalance;
    }

    /* ===== 网络字节流 ←→ DataUtil ===== */
    public void write(PacketByteBuf buf) {
        buf.writeFloat(tidalRate);
        buf.writeFloat(starMark);
        buf.writeFloat(mountainStand);
        buf.writeFloat(moonReflection);
        buf.writeFloat(cloudRoam);
        buf.writeFloat(skyBalance);
    }

    public static DataUtil read(PacketByteBuf buf) {
        return new DataUtil(
                buf.readFloat(),
                buf.readFloat(),
                buf.readFloat(),
                buf.readFloat(),
                buf.readFloat(),
                buf.readFloat()
        );
    }

    /* ===== NBT ←→ DataUtil ===== */
    public void writeToNbt(NbtCompound tag) {
        tag.putFloat("tidal_rate",      tidalRate);
        tag.putFloat("star_mark",       starMark);
        tag.putFloat("mountain_stand",  mountainStand);
        tag.putFloat("moon_reflection", moonReflection);
        tag.putFloat("cloud_roam",      cloudRoam);
        tag.putFloat("sky_balance",     skyBalance);
    }

    public static DataUtil readFromNbt(NbtCompound tag) {
        return new DataUtil(
                tag.getFloat("tidal_rate"),
                tag.getFloat("star_mark"),
                tag.getFloat("mountain_stand"),
                tag.getFloat("moon_reflection"),
                tag.getFloat("cloud_roam"),
                tag.getFloat("sky_balance")
        );
    }
}
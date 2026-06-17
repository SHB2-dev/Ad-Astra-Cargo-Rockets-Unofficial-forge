package uk.co.cablepost.ad_astra_cargo_rockets.scanner;

import net.minecraft.network.FriendlyByteBuf;

import java.util.ArrayList;
import java.util.List;

/** ロケット1台分の表示用情報。サーバー→クライアント転送用のDTO。 */
public class RocketInfo {
    public int entityId;
    public String name;
    public String dimension;
    public int x, y, z;
    public int tier;
    public String flightState;     // "grounded" / "ascending" / "descending" / "in_flight"
    public String statusOverride;  // Luaから送られた状態文字列（空なら自動推測を使う）
    public String autoWaitReason;  // MOD側の自動推測（"not_enough_energy" 等）
    public List<SlotInfo> inventory = new ArrayList<>();

    public static class SlotInfo {
        public int slot;
        public String itemId;
        public String displayName;
        public int count;

        public void write(FriendlyByteBuf buf) {
            buf.writeInt(slot);
            buf.writeUtf(itemId);
            buf.writeUtf(displayName);
            buf.writeInt(count);
        }

        public static SlotInfo read(FriendlyByteBuf buf) {
            SlotInfo s = new SlotInfo();
            s.slot = buf.readInt();
            s.itemId = buf.readUtf();
            s.displayName = buf.readUtf();
            s.count = buf.readInt();
            return s;
        }
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeInt(entityId);
        buf.writeUtf(name);
        buf.writeUtf(dimension);
        buf.writeInt(x); buf.writeInt(y); buf.writeInt(z);
        buf.writeInt(tier);
        buf.writeUtf(flightState);
        buf.writeUtf(statusOverride);
        buf.writeUtf(autoWaitReason);
        buf.writeInt(inventory.size());
        for (SlotInfo s : inventory) s.write(buf);
    }

    public static RocketInfo read(FriendlyByteBuf buf) {
        RocketInfo r = new RocketInfo();
        r.entityId = buf.readInt();
        r.name = buf.readUtf();
        r.dimension = buf.readUtf();
        r.x = buf.readInt(); r.y = buf.readInt(); r.z = buf.readInt();
        r.tier = buf.readInt();
        r.flightState = buf.readUtf();
        r.statusOverride = buf.readUtf();
        r.autoWaitReason = buf.readUtf();
        int n = buf.readInt();
        for (int i = 0; i < n; i++) r.inventory.add(SlotInfo.read(buf));
        return r;
    }
}

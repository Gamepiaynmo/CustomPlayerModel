package com.gpiay.cpm.config;

import com.gpiay.cpm.CPMMod;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;

@Config(name = CPMMod.MOD_ID)
public class Configs implements ConfigData {
    @Comment("Do not apply custom models on these entities. Entity locations seperated by semicolons.")
    public String entityBlacklist = "minecraft:armor_stand";

    @ConfigEntry.Gui.CollapsibleObject
    Client client = new Client();

    @ConfigEntry.Gui.CollapsibleObject
    Server server = new Server();

    @ConfigEntry.Gui.CollapsibleObject
    Permissions perm = new Permissions();

    public static class Client {
        @Comment("Hide particles that are too close to player's view point.")
        public boolean hideNearParticles = true;
        @Comment("Hide armors of custom models.")
        public boolean hideArmors = false;
        @Comment("Send models that are not located at the server from client.")
        public boolean sendModels = true;
    }

    public static class Server {
        @Comment("Allow models to change eye height.")
        public boolean customEyeHeight = true;
        @Comment("Allow models to change bounding box.")
        public boolean customBoundingBox = true;
        @Comment("Allow models to bind eye position to a specified bone.")
        public boolean customEyePosition = true;
        @Comment("Receive models that are not located at the server from clients.")
        public boolean receiveModels = true;
    }

    public static class Permissions {
        @Comment("Permission for selecting own model.")
        public int selectSelf = 0;
        @Comment("Permission for selecting other's model.")
        public int selectOthers = 2;
        @Comment("Permission for resizing own model.")
        public int scaleSelf = 0;
        @Comment("Permission for resizing other's model.")
        public int scaleOthers = 2;
        @Comment("Permission for refreshing local model files.")
        public int refresh = 2;
        @Comment("Permission for creating model changing items.")
        public int createItem = 2;
    }
}

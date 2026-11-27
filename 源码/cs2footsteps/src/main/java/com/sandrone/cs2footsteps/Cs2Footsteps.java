package com.sandrone.cs2footsteps;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod(Cs2Footsteps.MODID)
public final class Cs2Footsteps {
    public static final String MODID = "cs2footsteps";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Cs2Footsteps() {
        if (FMLEnvironment.dist.isClient()) {
            NeoForge.EVENT_BUS.addListener(Cs2Footsteps::tick);
        }
    }

    private static void tick(ClientTickEvent.Post event) {
        ClientFootsteps.tick();
    }
}

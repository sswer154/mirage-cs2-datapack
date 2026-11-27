package com.sandrone.cs2footsteps;

import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod(Cs2Footsteps.MODID)
public final class Cs2Footsteps {
    public static final String MODID = "cs2footsteps";

    public Cs2Footsteps() {
        if (FMLEnvironment.dist.isClient()) {
            NeoForge.EVENT_BUS.addListener(Cs2Footsteps::tick);
        }
    }

    private static void tick(ClientTickEvent.Post event) {
        ClientFootsteps.tick();
    }
}

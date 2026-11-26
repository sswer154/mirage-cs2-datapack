package com.sandrone.cs2footsteps;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@Mod(Cs2Footsteps.MODID)
public class Cs2Footsteps {
    public static final String MODID = "cs2footsteps";

    public Cs2Footsteps(IEventBus modBus, ModContainer container) {
        if (FMLEnvironment.dist.isClient()) {
            NeoForge.EVENT_BUS.addListener(this::onClientTick);
        }
    }

    private void onClientTick(ClientTickEvent.Post event) {
        ClientFootsteps.tick();
    }
}

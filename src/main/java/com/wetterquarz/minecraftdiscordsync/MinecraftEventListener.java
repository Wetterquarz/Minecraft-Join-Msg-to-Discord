package com.wetterquarz.minecraftdiscordsync;

import java.util.function.Consumer;

import org.bukkit.event.*;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import reactor.core.publisher.FluxSink;

public class MinecraftEventListener implements Listener, Consumer<FluxSink<String>> {
	
	private FluxSink<String> eventSink;
	private final JavaPlugin plugin;
	
	public MinecraftEventListener(JavaPlugin plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		eventSink.next(event.getJoinMessage());
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerQuitEvent event) {
		eventSink.next(event.getQuitMessage());
	}

	@Override
	public void accept(FluxSink<String> sink) {
		eventSink = sink;
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
		eventSink.onDispose(() -> HandlerList.unregisterAll(this));
	}
	
}

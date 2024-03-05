package com.wetterquarz.minecraftdiscordsync;

import org.bukkit.plugin.java.JavaPlugin;

import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.channel.GuildMessageChannel;
import net.md_5.bungee.api.ChatColor;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class DiscordSyncPlugin extends JavaPlugin {
	
	private Disposable discordAccess = null;
	
	@Override
	public void onEnable() {
		saveDefaultConfig();
		Flux<String> events = Flux.create(new MinecraftEventListener(this))
				.map(ChatColor::stripColor);
		final String token = getConfig().getString("bot_token");
		DiscordClient client = DiscordClient.create(token);
		discordAccess = client.login()
				.flatMap(gateway -> this.printMessages(gateway, events))
				.subscribe();
	}
	
	@Override
	public void onDisable() {
		discordAccess.dispose();
		discordAccess = null;
	}
	
	private Mono<Void> printMessages(GatewayDiscordClient gateway, Flux<String> messages) {
		final String channelId = getConfig().getString("channel_id");
		GuildMessageChannel channel = (GuildMessageChannel) gateway.getChannelById(Snowflake.of(channelId)).block();
		getLogger().info("Showing join and leave activity in channel " + channel.getName());
		return Flux.create(new MinecraftEventListener(this))
				.map(ChatColor::stripColor)
				.flatMap(channel::createMessage)
				.onErrorContinue((t, msg) -> getLogger().severe(t.toString()))
				.then();
	}
	
}

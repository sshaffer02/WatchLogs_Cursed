package fr.Boulldogo.WatchLogs.Discord.Commands;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;

import java.awt.Color;

import com.vdurmont.emoji.EmojiParser;

import fr.Boulldogo.WatchLogs.Main;
import fr.Boulldogo.WatchLogs.Discord.SetupDiscordBot;

public class InfoCommand implements SlashCommand {

    private final SetupDiscordBot main;
    private final Main plugin;

    public InfoCommand(Main plugin, SetupDiscordBot main) {
        this.main = main;
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "infos";
    }

    @Override
    public String getDescription() {
        return "Provides informations about the bot.";
    }
    
    @Override
    public CommandData getCommandData() {
        return Commands.slash(getName(), getDescription());
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        JDA jda = main.getJDA();
        String botName = jda.getSelfUser().getName();
        String botId = jda.getSelfUser().getId();
        long ping = jda.getGatewayPing();

        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle(translateEmoji(plugin.getLang().getString("discord.info_command_title")));
        embed.setColor(Color.CYAN);
        embed.addField(translateEmoji(plugin.getLang().getString("discord.info_command_bot_name")), botName, true);
        embed.addField(translateEmoji(plugin.getLang().getString("discord.info_command_bot_id")), botId, true);
        embed.addField(translateEmoji(plugin.getLang().getString("discord.info_command_bot_latency")), ping + " ms", true);
        embed.addField(translateEmoji(plugin.getLang().getString("discord.info_command_bot_watchlogs_version")), Main.version, true);
        embed.setFooter(translateEmoji(":white_check_mark: Generated by WatchLogs plugin by Boulldogo, type /project for project !"));

        event.replyEmbeds(embed.build()).queue();
    }
    
    public String translateEmoji(String s) {
    	return EmojiParser.parseToUnicode(s);
    }
}


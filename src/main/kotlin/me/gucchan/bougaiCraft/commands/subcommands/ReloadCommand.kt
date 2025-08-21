package me.gucchan.bougaiCraft.commands.subcommands

import me.gucchan.bougaiCraft.BougaiCraft
import me.gucchan.bougaiCraft.commands.SubCommand
import org.bukkit.command.CommandSender

class ReloadCommand(private val plugin: BougaiCraft) : SubCommand {
    override fun execute(sender: CommandSender, args: Array<out String>) {
        plugin.configManager.reloadConfig()
        sender.sendMessage("§aBougaiCraftの設定ファイルをリロードしました。")
    }
}

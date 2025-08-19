package me.gucchan.bougaiCraft.commands.subcommands

import me.gucchan.bougaiCraft.BougaiCraft
import me.gucchan.bougaiCraft.commands.SubCommand
import org.bukkit.command.CommandSender

class StopCommand(private val plugin: BougaiCraft) : SubCommand {
    override fun execute(sender: CommandSender, args: Array<out String>) {
        var stopped = false

        if (plugin.activeChatListener != null) {
            plugin.activeChatListener?.stop()
            plugin.activeChatListener = null
            stopped = true
        }
        if (plugin.activeStatsListener != null) {
            plugin.activeStatsListener?.stop()
            plugin.activeStatsListener = null
            stopped = true
        }

        if (stopped) {
            sender.sendMessage("§a全てのYouTube監視を停止しました。")
        } else {
            sender.sendMessage("§c実行中のリスナーはありません。")
        }
    }
}
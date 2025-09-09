package me.gucchan.bougaiCraft.commands

import me.gucchan.bougaiCraft.BougaiCraft
import me.gucchan.bougaiCraft.commands.subcommands.AuthCodeCommand
import me.gucchan.bougaiCraft.commands.subcommands.AuthorizeCommand
import me.gucchan.bougaiCraft.commands.subcommands.ReloadCommand
import me.gucchan.bougaiCraft.commands.subcommands.StartCommand
import me.gucchan.bougaiCraft.commands.subcommands.StopCommand
import me.gucchan.bougaiCraft.commands.subcommands.TestSuperChatCommand
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class BougaiCommand(plugin: BougaiCraft) : CommandExecutor {

    private val subCommands = mapOf(
        "authorize" to AuthorizeCommand(plugin.authManager),
        "authcode" to AuthCodeCommand(plugin),
        "start" to StartCommand(plugin),
        "stop" to StopCommand(plugin),
        "reload" to ReloadCommand(plugin),
        "testsc" to TestSuperChatCommand(plugin)
    )

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        // 引数がない場合はヘルプを表示
        if (args.isEmpty()) {
            sendHelp(sender)
            return true
        }

        val subCommand = subCommands[args[0].lowercase()]
        if (subCommand == null) {
            sender.sendMessage("§c不明なサブコマンドです。")
            sendHelp(sender)
            return true
        }

        // サブコマンドに、それ以降の引数を渡して実行
        subCommand.execute(sender, args.sliceArray(1 until args.size))
        return true
    }

    private fun sendHelp(sender: CommandSender) {
        sender.sendMessage("§e==== BougaiCraft コマンドヘルプ ====")
        sender.sendMessage("§a/bg authorize §7- YouTubeアカウントを認証します。(初回のみ)")
        sender.sendMessage("§a/bg start <YouTubeライブURL> <player> §7- チャットといいね数の監視を開始します。")
        sender.sendMessage("§a/bg stop §7- 全ての監視を停止します。")
    }
}
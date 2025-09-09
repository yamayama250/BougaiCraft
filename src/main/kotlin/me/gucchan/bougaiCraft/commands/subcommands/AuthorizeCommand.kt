package me.gucchan.bougaiCraft.commands.subcommands

import me.gucchan.bougaiCraft.commands.SubCommand
import me.gucchan.bougaiCraft.managers.YoutubeAuthManager
import org.bukkit.command.CommandSender

class AuthorizeCommand(private val authManager: YoutubeAuthManager) : SubCommand {

    override fun execute(sender: CommandSender, args: Array<out String>) {
        if (args.isNotEmpty()) {
            sender.sendMessage("使い方: /bougai authorize")
            return
        }

        try {
            val authUrl = authManager.generateAuthUrl()
            sender.sendMessage("§e==== YouTube認証 (ステップ1/2) ====")
            sender.sendMessage("§7以下のURLにアクセスして認証を許可してください。")
            sender.sendMessage("§b$authUrl") // URLを直接クリックできるよう、チャットに表示
            sender.sendMessage("§7認証後、ブラウザのアドレスバーに表示されたURLを§cすべてコピー§7し、")
            sender.sendMessage("§a/bg authcode <コピーしたURL> §7を実行してください。")
        } catch (e: Exception) {
            sender.sendMessage("§c認証URLの生成に失敗しました: ${e.message}")
        }
    }
}
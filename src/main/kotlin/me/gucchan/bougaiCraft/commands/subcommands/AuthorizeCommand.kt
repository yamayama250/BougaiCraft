package me.gucchan.bougaiCraft.commands.subcommands

import me.gucchan.bougaiCraft.commands.SubCommand
import me.gucchan.bougaiCraft.managers.YoutubeAuthManager
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.plugin.java.JavaPlugin

class AuthorizeCommand(private val plugin: JavaPlugin, private val authManager: YoutubeAuthManager) : SubCommand {

    override fun execute(sender: CommandSender, args: Array<out String>) {
        if (args.isNotEmpty()) {
            sender.sendMessage("使い方: /bougai authorize")
            return
        }

        sender.sendMessage("§eYouTubeの認証を開始します...")
        sender.sendMessage("§eサーバーのコンソール（黒い画面）に表示されるURLにアクセスしてください。")

        // 認証プロセスはサーバーをブロックする可能性があるので非同期で実行する
        Bukkit.getScheduler().runTaskAsynchronously(plugin, Runnable {
            try {
                authManager.authorize()
                sender.sendMessage("§a認証が完了しました！")
            } catch (_: Exception) {
                sender.sendMessage("§c認証中にエラーが発生しました。コンソールを確認してください。")
            }
        })
        return
    }
}
package me.gucchan.bougaiCraft.commands.subcommands

import me.gucchan.bougaiCraft.BougaiCraft
import me.gucchan.bougaiCraft.commands.SubCommand
import me.gucchan.bougaiCraft.utils.UrlUtils
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender

class AuthCodeCommand(private val plugin: BougaiCraft) : SubCommand {
    override fun execute(sender: CommandSender, args: Array<out String>) {
        if (args.isEmpty()) {
            sender.sendMessage("§c使い方: /bg authcode <認証画面URL>")
            return
        }

        // URLにスペースが含まれて引数が分割された場合も考慮して、すべて結合する
        val fullUrl = args.joinToString("")

        // UrlUtilsを使ってURLから認証コードを抽出する
        val code = UrlUtils.extractAuthCodeFromUrl(fullUrl)

        if (code == null) {
            sender.sendMessage("§cURLから認証コードが見つかりませんでした。正しいURLを貼り付けてください。")
            return
        }

        sender.sendMessage("§e認証コードを処理しています...")

        Bukkit.getScheduler().runTaskAsynchronously(plugin, Runnable {
            try {
                plugin.authManager.exchangeCodeForCredentials(code)
                Bukkit.getScheduler().runTask(plugin, Runnable {
                    sender.sendMessage("§a認証に成功しました！これで/bg startが使用できます。")
                })
            } catch (e: Exception) {
                Bukkit.getScheduler().runTask(plugin, Runnable {
                    sender.sendMessage("§c認証に失敗しました: ${e.message}")
                })
            }
        })
    }
}

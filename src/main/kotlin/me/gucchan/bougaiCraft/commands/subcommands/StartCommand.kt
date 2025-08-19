package me.gucchan.bougaiCraft.commands.subcommands

import me.gucchan.bougaiCraft.BougaiCraft
import me.gucchan.bougaiCraft.commands.SubCommand
import me.gucchan.bougaiCraft.listeners.YoutubeChatListener
import me.gucchan.bougaiCraft.listeners.YoutubeStatsListener
import me.gucchan.bougaiCraft.utils.UrlUtils
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender

class StartCommand(private val plugin: BougaiCraft) : SubCommand {
    override fun execute(sender: CommandSender, args: Array<out String>) {
        if (args.size != 2) {
            sender.sendMessage("§c使い方: /bg start <videoId> <player>")
            return
        }
        // どちらかのリスナーが既に動いていたらエラーにする
        if (plugin.activeChatListener != null || plugin.activeStatsListener != null) {
            sender.sendMessage("§c既にリスナーが実行中です。一度/bg stopで停止してください。")
            return
        }

        val url = args[0]
        val videoId = UrlUtils.extractVideoIdFromUrl(url) // URLからVideo IDを抽出
        if (videoId == null) {
            sender.sendMessage("§c無効なYouTube URLです。正しいURLを入力してください。")
            return
        }

        val targetPlayer = Bukkit.getPlayer(args[1])
        if (targetPlayer == null) {
            sender.sendMessage("§cプレイヤー '${args[1]}' が見つかりません。")
            return
        }

        val credential = plugin.authManager.loadCredentials()
        if (credential == null) {
            sender.sendMessage("§c認証情報が見つかりません。先に /bg authorize で認証を行ってください。")
            return
        }

        sender.sendMessage("§aYouTubeチャットの監視を開始します...")

        // 1. チャットリスナーを生成して開始
        val chatListener = YoutubeChatListener(plugin, videoId, targetPlayer, credential)
        chatListener.start()

        // 2. 統計リスナー（いいね数）を生成して開始
        val statsListener = YoutubeStatsListener(plugin, videoId, targetPlayer, credential)
        statsListener.start()

        // 両方のリスナーをプラグインの管理変数にセット
        plugin.activeChatListener = chatListener
        plugin.activeStatsListener = statsListener
    }
}
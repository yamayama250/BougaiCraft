package me.gucchan.bougaiCraft.listeners

import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.youtube.YouTube
import me.gucchan.bougaiCraft.BougaiCraft
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitRunnable

class YoutubeStatsListener(
    private val plugin: BougaiCraft,
    private val videoId: String,
    private val targetPlayer: Player,
    credential: Credential
) {
    private val youtubeService: YouTube
    private var pollingTask: BukkitRunnable? = null

    // 初回高評価取得時スキップのためのフラグ
    private var isInitialFetch = true
    // 高評価の数を格納する変数
    private var lastLikeCount : Long = 0

    init {
        val httpTransport = GoogleNetHttpTransport.newTrustedTransport()
        youtubeService = YouTube.Builder(httpTransport, GsonFactory.getDefaultInstance(), credential)
            .setApplicationName(plugin.description.name)
            .build()
    }

    fun start() {
        // 15秒ごとに動画情報をチェックするタスクを開始
        pollingTask = object : BukkitRunnable() {
            override fun run() {
                if (!targetPlayer.isOnline) {
                    stop()
                    return
                }
                pollVideoStatistics()
            }
        }.also { it.runTaskTimerAsynchronously(plugin, 0L, 300L) } // 15 seconds * 20 ticks = 300L
    }

    fun stop() {
        pollingTask?.cancel()
        pollingTask = null
        plugin.logger.info("YouTube動画の統計監視を停止しました。")
    }

    private fun pollVideoStatistics() {
        try {
            val response = youtubeService.videos()
                .list(listOf("statistics")) // 統計情報をリクエスト
                .setId(listOf(videoId))
                .execute()

            val stats = response.items.firstOrNull()?.statistics ?: return
            val currentLikes = stats.likeCount.toLong()

            // 最初のチェックの場合、処理をスキップする
            if (isInitialFetch) {
                isInitialFetch = false
                lastLikeCount = currentLikes
                tellPlayer("§aいいね数の監視を開始しました。")
            }

            if (lastLikeCount < currentLikes) {
                // 取得した高評価と最後に取得した高評価の差分を取得
                val likeCount = currentLikes - lastLikeCount
                lastLikeCount = currentLikes

                repeat(likeCount.toInt()) {
                    targetPlayer.inventory.addItem(ItemStack(Material.ENCHANTED_GOLDEN_APPLE))
                }
            }

        } catch (e: Exception) {
            plugin.logger.warning("動画の統計情報取得に失敗: ${e.message}")
        }
    }

    private fun tellPlayer(message: String) {
        if (targetPlayer.isOnline) {
            Bukkit.getScheduler().runTask(plugin, Runnable {
                targetPlayer.sendMessage(message)
            })
        }
    }

}
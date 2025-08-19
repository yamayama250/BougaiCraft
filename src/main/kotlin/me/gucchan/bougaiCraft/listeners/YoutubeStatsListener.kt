package me.gucchan.bougaiCraft.listeners

import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.youtube.YouTube
import me.gucchan.bougaiCraft.BougaiCraft
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitRunnable

class YoutubeStatsListener(
    private val plugin: BougaiCraft,
    private val videoId: String,
    private val targetPlayer: Player,
    credential: Credential
) {
    private val youtubeService: YouTube
    private var pollingTask: BukkitRunnable? = null

    // いいね数のチェックに使用する変数
    private var lastLikeCount: Long = -1
    private var nextMilestone: Long = 0

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

            // 最初のチェックの場合、現在のいいね数を基準に次の目標を設定
            if (lastLikeCount == -1L) {
                lastLikeCount = currentLikes
                nextMilestone = (currentLikes / 5 + 1) * 5
                tellPlayer("§aいいね数の監視を開始しました。(現在: $currentLikes, 次の目標: $nextMilestone)")
                return
            }

            // 目標（100の倍数）を超えていないかチェック
            if (currentLikes > lastLikeCount) {
                plugin.logger.info("いいね数更新: $lastLikeCount -> $currentLikes (次の目標: $nextMilestone)")
                // 複数の目標を一気に達成した場合も考慮してループ処理
                while (currentLikes >= nextMilestone) {
                    tellPlayer("§d♥ §fいいね数が§d${nextMilestone}§fを達成！特殊効果を付与します！")
                    applyEffects()
                    nextMilestone += 5 // 次の目標を更新
                }
            }
            lastLikeCount = currentLikes

        } catch (e: Exception) {
            plugin.logger.warning("動画の統計情報取得に失敗: ${e.message}")
        }
    }

    private fun applyEffects() {
        // ポーション効果の付与はメインスレッドで行う
        Bukkit.getScheduler().runTask(plugin, Runnable {
            // 効果: ジャンプ力上昇 II (1分)
            val jumpEffect = PotionEffect(PotionEffectType.JUMP_BOOST, 1200, 1) // 60s * 20tick, Amplifier 1 = Level II
            // 効果: 採掘速度上昇 II (1分)
            val hasteEffect = PotionEffect(PotionEffectType.HASTE, 1200, 1)

            targetPlayer.addPotionEffect(jumpEffect)
            targetPlayer.addPotionEffect(hasteEffect)
        })
    }

    private fun tellPlayer(message: String) {
        if (targetPlayer.isOnline) {
            Bukkit.getScheduler().runTask(plugin, Runnable {
                targetPlayer.sendMessage(message)
            })
        }
    }

}
package me.gucchan.bougaiCraft.listeners

import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.youtube.YouTube
import com.google.api.services.youtube.model.LiveChatMessage
import me.gucchan.bougaiCraft.BougaiCraft
import me.gucchan.bougaiCraft.utils.SpawnUtils
import org.bukkit.Bukkit
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.entity.Wolf
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitRunnable
import java.math.BigInteger

class YoutubeChatListener(
    private val plugin: BougaiCraft,
    private val videoId: String,
    private val targetPlayer: Player,
    credential: Credential
) {
    private val youtubeService: YouTube
    private var liveChatId: String? = null
    private var nextPageToken: String? = null
    private var pollingTask: BukkitRunnable? = null

    // 初回コメント取得時スキップのためのフラグ
    private var isInitialFetch = true

    init {
        val httpTransport = GoogleNetHttpTransport.newTrustedTransport()
        youtubeService = YouTube.Builder(
            httpTransport,
            GsonFactory.getDefaultInstance(),
            credential
        ).setApplicationName(plugin.description.name).build()
    }

    fun start() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, Runnable {
            try {
                val response = youtubeService.videos().list(listOf("liveStreamingDetails"))
                    .setId(listOf(videoId)).execute()
                liveChatId = response.items.firstOrNull()?.liveStreamingDetails?.activeLiveChatId

                if (liveChatId == null) {
                    tellPlayer("§cライブ配信が見つかりません。Video IDが正しいか確認してください。")
                    return@Runnable
                }
                tellPlayer("§aYouTubeチャットへの接続に成功しました。監視を開始します。")
                startPolling()
            } catch (e: Exception) {
                plugin.logger.warning("YouTubeへの接続に失敗しました: ${e.message}")
                tellPlayer("§cYouTubeへの接続に失敗しました。詳細はサーバーログを確認してください。")
            }
        })
    }

    private fun startPolling() {
        pollingTask = object : BukkitRunnable() {
            override fun run() {
                if (!targetPlayer.isOnline) {
                    stop()
                    return
                }
                fetchMessages()
            }
        }.also { it.runTaskTimerAsynchronously(plugin, 0L, 120L) } // 6秒ごと
    }

    private fun fetchMessages() {
        val currentLiveChatId = liveChatId ?: return
        try {
            val response = youtubeService.liveChatMessages()
                .list(currentLiveChatId, listOf("snippet", "authorDetails"))
                .setPageToken(nextPageToken)
                .execute()

            // 初回取得時は過去メッセージが取得されてしまうので処理をスキップ
            if (isInitialFetch) {
                isInitialFetch = false
                nextPageToken = response.nextPageToken
                Thread.sleep(response.pollingIntervalMillis)
                return
            }

            response.items.forEach { message ->
                Bukkit.getScheduler().runTask(plugin, Runnable {
                    handleMessage(message)
                })
            }
            nextPageToken = response.nextPageToken
            Thread.sleep(response.pollingIntervalMillis)
        } catch (e: Exception) {
            // エラーが起きてもタスクは止めず、次のポーリングを待つ
            plugin.logger.warning("チャットの取得に失敗: ${e.message}")
        }
    }

    private fun handleMessage(message: LiveChatMessage) {
        val text = message.snippet.displayMessage
        val author = message.authorDetails.displayName
        val superChat = message.snippet.superChatDetails

        if (superChat != null) {
            val amount = superChat.amountMicros.divide(BigInteger.valueOf(1000000)).toLong()
            plugin.server.broadcastMessage("§6[§fYouTube§6] §e${author}さんから§c${amount}${superChat.currency}§eのSuperChat！")

            when {
                amount >= 10000 -> {
                    val spawnLoc = SpawnUtils.getSafeSpawnLocation(targetPlayer.location, 5, 10)
                    val wither = targetPlayer.world.spawnEntity(spawnLoc, EntityType.WITHER)
                    // 5分後 (6000 ticks) にウィザーを削除するタスクをスケジュールする
                    Bukkit.getScheduler().runTaskLater(plugin, Runnable {
                        // ウィザーがまだサーバーに存在し、かつ死んでいない場合のみ削除を実行
                        if (wither.isValid && !wither.isDead) {
                            wither.remove()
                            plugin.server.broadcastMessage("§a5分が経過したため、ウィザーが消滅しました。")
                        }
                    }, 6000L)
                }
                amount in 200..<500 -> {
                    // 効果: 採掘速度上昇 II (1分)
                    val blindEffect = PotionEffect(PotionEffectType.BLINDNESS, 200, 1)
                    targetPlayer.addPotionEffect(blindEffect)
                }
                200 > amount -> {
                    // クリーパー10体
                    repeat(10) {
                        val spawnLoc = SpawnUtils.getSafeSpawnLocation(targetPlayer.location, 5, 10)
                        targetPlayer.world.spawnEntity(spawnLoc, EntityType.CREEPER)
                    }
                }
                else -> {}
            }
        } else {
            // 通常コメントの処理
            plugin.logger.info("チャット受信: $text")
            for ((keyword, entityType) in plugin.configManager.commentSpawns) {
                if(text.contains(keyword)) {
                    val spawnLoc = SpawnUtils.getSafeSpawnLocation(targetPlayer.location, 5, 10)

                    if (entityType == EntityType.WOLF) {
                        // オオカミをスポーンさせ、Wolf型にキャストする
                        val wolf = targetPlayer.world.spawnEntity(targetPlayer.location, EntityType.WOLF) as Wolf

                        // 召喚したオオカミを懐かせ、飼い主を対象プレイヤーに設定
                        wolf.isTamed = true
                        wolf.owner = targetPlayer

                        // 名札を付け、名前を常に表示する設定にする
                        wolf.customName = "§b${author}" // 色を付けて分かりやすく
                        wolf.isCustomNameVisible = true
                    } else {
                        targetPlayer.world.spawnEntity(spawnLoc, entityType)
                    }

                    // 一致するキーワードが見つかったら、他のキーワードは探さずにループを抜ける
                    break
                }
            }
        }
    }

    fun stop() {
        pollingTask?.cancel()
        pollingTask = null
        plugin.logger.info("YouTubeチャットの監視を停止しました。")
    }


    // 非同期処理からプレイヤーにメッセージを送るためのヘルパー
    private fun tellPlayer(message: String) {
        if (targetPlayer.isOnline) {
            Bukkit.getScheduler().runTask(plugin, Runnable {
                targetPlayer.sendMessage(message)
            })
        }
    }
}
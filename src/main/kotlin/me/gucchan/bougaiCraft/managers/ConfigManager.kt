package me.gucchan.bougaiCraft.managers

import me.gucchan.bougaiCraft.BougaiCraft
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.entity.EntityType

class ConfigManager(private val plugin: BougaiCraft) {
    private lateinit var config: FileConfiguration

    // コメントとモンスターのマッピングを保持するMap
    var commentSpawns: Map<String, EntityType> = emptyMap()
        private set

    init {
        // config.ymlが存在しない場合、jar内のデフォルトファイルをコピー
        plugin.saveDefaultConfig()
        // ロード処理を初回実行
        reloadConfig()
    }

    fun reloadConfig() {
        // プラグインのconfigをリロード
        plugin.reloadConfig()
        config = plugin.config

        // 設定からコメントとモンスターのペアを読み込む
        loadCommentSpawns()

        plugin.logger.info("config.ymlをリロードしました。")
    }

    private fun loadCommentSpawns() {
        val newSpawns = mutableMapOf<String, EntityType>()
        // "comment-spawns" セクションを取得
        val spawnsSection = config.getConfigurationSection("comment-spawns")

        spawnsSection?.getKeys(false)?.forEach { keyword ->
            val entityName = spawnsSection.getString(keyword, "") ?: ""
            try {
                // 文字列からEntityTypeに変換
                val entityType = EntityType.valueOf(entityName.uppercase())
                newSpawns[keyword] = entityType
            } catch (_: IllegalArgumentException) {
                plugin.logger.warning("config.ymlの'${entityName}'は無効なモンスター名です。")
            }
        }
        commentSpawns = newSpawns
        plugin.logger.info("${commentSpawns.size}件のコメントスポーン設定を読み込みました。")
    }
}
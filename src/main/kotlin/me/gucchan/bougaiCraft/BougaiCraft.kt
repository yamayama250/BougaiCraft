package me.gucchan.bougaiCraft

import me.gucchan.bougaiCraft.commands.BougaiCommand
import me.gucchan.bougaiCraft.commands.TabCompleter
import me.gucchan.bougaiCraft.listeners.YoutubeChatListener
import me.gucchan.bougaiCraft.listeners.YoutubeStatsListener
import me.gucchan.bougaiCraft.managers.YoutubeAuthManager
import org.bukkit.plugin.java.JavaPlugin

class BougaiCraft : JavaPlugin() {

    companion object {
        lateinit var plugin: JavaPlugin
    }

    // 他のクラスからアクセスできるようにlate initで宣言
    lateinit var authManager: YoutubeAuthManager
    var activeChatListener: YoutubeChatListener? = null
    var activeStatsListener: YoutubeStatsListener? = null

    override fun onEnable() {
        // Plugin startup logic
        logger.info { "BougaiCraft Plugin enabled!" }

        plugin = this
        // config.ymlが無い場合は作成する
        saveDefaultConfig()

        // プラグインフォルダが無い場合は作成する
        if(!dataFolder.exists()) {
            dataFolder.mkdirs()
        }

        // 認証マネージャーを初期化
        authManager = YoutubeAuthManager(this)

        getCommand("bougai")?.setExecutor(BougaiCommand(this))
        getCommand("bougai")?.tabCompleter = TabCompleter()
    }

    override fun onDisable() {
        // プラグイン終了時にリスナーが動いていれば停止する
        activeChatListener?.stop()
        activeStatsListener?.stop()
        logger.info { "BougaiCraft Plugin disabled!" }
    }
}

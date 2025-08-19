package me.gucchan.bougaiCraft.managers

import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.util.store.FileDataStoreFactory
import me.gucchan.bougaiCraft.BougaiCraft
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.InputStreamReader

class YoutubeAuthManager(private val plugin: BougaiCraft) {
    private val jsonFactory = GsonFactory.getDefaultInstance()
    private val httpTransport = NetHttpTransport()
    private val dataStoreFactory = FileDataStoreFactory(File(plugin.dataFolder, "credentials"))

    private val userIdentifier = "user"

    /**
     * ブラウザを使った認証フローを開始し、認証情報をファイルに保存する
     */
    fun authorize() {
        val clientSecretsFile = File(plugin.dataFolder, "client_secrets.json")
        // ファイルが存在しない場合のエラー処理
        if (!clientSecretsFile.exists()) {
            plugin.logger.severe("----------------------------------------------------")
            plugin.logger.severe(" 'plugins/" + plugin.name + "/client_secrets.json' が見つかりません！")
            plugin.logger.severe(" ファイルを設置してください。")
            plugin.logger.severe("----------------------------------------------------")

            throw FileNotFoundException()
        }

        val clientSecrets = FileInputStream(clientSecretsFile).use {
            GoogleClientSecrets.load(jsonFactory, InputStreamReader(it))
        }

        val flow = GoogleAuthorizationCodeFlow.Builder(
            httpTransport, jsonFactory, clientSecrets,
            listOf("https://www.googleapis.com/auth/youtube.readonly")
        ).setDataStoreFactory(dataStoreFactory).build()

        // コンソールにURLが表示され、ユーザーがブラウザで認証する
        val receiver = LocalServerReceiver.Builder().setPort(8888).build()
        AuthorizationCodeInstalledApp(flow, receiver).authorize(userIdentifier)
        plugin.logger.info("認証に成功しました！認証情報は保存されました。")
    }

    /**
     * 保存された認証情報を読み込む
     */
    fun loadCredentials(): Credential? {
        val clientSecretsFile = File(plugin.dataFolder, "client_secrets.json")
        if (!clientSecretsFile.exists()) return null

        val clientSecrets = FileInputStream(clientSecretsFile).use {
            GoogleClientSecrets.load(jsonFactory, InputStreamReader(it))
        }

        val flow = GoogleAuthorizationCodeFlow.Builder(
            httpTransport, jsonFactory, clientSecrets,
            listOf("https://www.googleapis.com/auth/youtube.readonly")
        ).setDataStoreFactory(dataStoreFactory).build()

        return flow.loadCredential(userIdentifier)
    }
}
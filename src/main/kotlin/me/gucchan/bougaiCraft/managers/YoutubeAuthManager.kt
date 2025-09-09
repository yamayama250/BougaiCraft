package me.gucchan.bougaiCraft.managers

import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.util.store.FileDataStoreFactory
import me.gucchan.bougaiCraft.BougaiCraft
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStreamReader

class YoutubeAuthManager(private val plugin: BougaiCraft) {
    private val jsonFactory = GsonFactory.getDefaultInstance()
    private val httpTransport = NetHttpTransport()
    private val credentialsFolder = File(plugin.dataFolder, "credentials")
    private val dataStoreFactory = FileDataStoreFactory(credentialsFolder)

    private val scopes = listOf("https://www.googleapis.com/auth/youtube.readonly")
    private val userIdentifier = "user"
    private val redirectUri = "http://localhost:8888" // GCPで設定するリダイレクトURI

    /**
     * 認証フローのコアとなるGoogleAuthorizationCodeFlowオブジェクトを生成して返す。
     * client_secrets.jsonが存在しない場合は例外をスローする。
     */
    private fun getFlow(): GoogleAuthorizationCodeFlow {
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

        return GoogleAuthorizationCodeFlow.Builder(httpTransport, jsonFactory, clientSecrets, scopes)
            .setDataStoreFactory(dataStoreFactory)
            .setAccessType("offline") // サーバー再起動後も使えるようにリフレッシュトークンを要求
            .build()
    }

    /**
     * ステップ1: ユーザーがブラウザでアクセスするための認証URLを生成する。
     * @return 生成された認証URL
     */
    fun generateAuthUrl(): String {
        val flow = getFlow()
        return flow.newAuthorizationUrl()
            .setRedirectUri(redirectUri)
            .build()
    }

    /**
     * ステップ2: ユーザーがブラウザから取得した認証コードを、アクセストークンと交換してファイルに保存する。
     * この処理はネットワーク通信を伴うため、非同期で実行する必要がある。
     * @param code ブラウザのアドレスバーからコピーした認証コード
     * @throws IOException 認証コードが無効な場合や、通信に失敗した場合
     */
    fun exchangeCodeForCredentials(code: String) {
        val flow = getFlow()
        try {
            val response = flow.newTokenRequest(code)
                .setRedirectUri(redirectUri)
                .execute()

            flow.createAndStoreCredential(response, userIdentifier)
        } catch (e: IOException) {
            // エラーの詳細をラップして、より分かりやすいメッセージを添えて再スローする
            throw IOException("無効な認証コード、または認証プロセスでエラーが発生しました。", e)
        }
    }

    /**
     * 保存済みの認証情報(Credential)をファイルから読み込む。
     * 認証が完了していない、またはファイルが見つからない場合はnullを返す。
     * @return 読み込んだCredentialオブジェクト、またはnull
     */
    fun loadCredentials(): Credential? {
        return try {
            getFlow().loadCredential(userIdentifier)
        } catch (e: Exception) {
            // client_secrets.jsonが見つからない場合など
            plugin.logger.warning("認証情報の読み込みに失敗しました: ${e.message}")
            null
        }
    }
}
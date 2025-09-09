package me.gucchan.bougaiCraft.utils

object UrlUtils {
    // 様々な形式のYouTube URLからVideo IDを抽出する正規表現
    private val youtubeUrlPattern = """(?:https?://)?(?:www\.)?(?:youtube\.com/(?:watch\?v=|live/)|youtu\.be/)([a-zA-Z0-9_-]{11})""".toRegex()

    /**
     * YouTubeのURL文字列からVideo IDを抽出する
     * @param url 解析したいURL
     * @return 抽出したVideo ID。見つからなければnullを返す
     */
    fun extractVideoIdFromUrl(url: String): String? {
        val matchResult = youtubeUrlPattern.find(url)
        // マッチ結果から1番目のキャプチャグループ（Video ID部分）を返す
        return matchResult?.groupValues?.get(1)
    }

    /**
     * リダイレクトURLから 'code' パラメータの値を抽出する
     * @param url ブラウザのアドレスバーに表示されたURL全体
     * @return 抽出した認証コード。見つからなければnullを返す
     */
    fun extractAuthCodeFromUrl(url: String): String? {
        // パターン: 「?code=」または「&code=」の直後から始まり、'&'以外の文字の連続
        val pattern = """(?<=[?&]code=)[^&]+""".toRegex()

        // .find()で見つかった最初のマッチオブジェクトを取得し、その.value（値そのもの）を返す
        return pattern.find(url)?.value
    }
}
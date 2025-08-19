# BougaiCraft
YouTube上で貰ったコメントやスーパーチャットに応じてMineCraft上でアクションを起こすプラグイン

## 動作環境
- Minecraft 1.21.8
- SpigotMC 1.21.8

## コマンド一覧
- bougai
    - authorize  
      YouTubeApiとの認証を行う  
      一度行うと認証情報が保存されるため初回のみ実行
    - start <配信URL> <プレイヤー名>  
      指定した配信を対象としてプラグインの動作を始める  
      プレイヤー名を指定して、そのプレイヤーに対してバフやモンスターが湧くように
    - stop  
      プラグインの実行を終了する

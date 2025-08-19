package me.gucchan.bougaiCraft.commands.subcommands

import me.gucchan.bougaiCraft.BougaiCraft
import me.gucchan.bougaiCraft.commands.SubCommand
import me.gucchan.bougaiCraft.utils.SpawnUtils
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class TestSuperChatCommand(private val plugin: BougaiCraft) : SubCommand {
    override fun execute(sender: CommandSender, args: Array<out String>) {
        // このコマンドはプレイヤーからのみ実行可能にする
        if (sender !is Player) {
            sender.sendMessage("このコマンドはプレイヤーのみ実行できます。")
            return
        }

        if (args.isEmpty()) {
            sender.sendMessage("§c使い方: /bg testsc <金額> [プレイヤー名]")
            sender.sendMessage("§7例: /bg testsc 5000 ${sender.name}")
            return
        }

        val amount = args[0].toLongOrNull()
        if (amount == null || amount <= 0) {
            sender.sendMessage("§c金額には正の数値を入力してください。")
            return
        }

        // プレイヤー名の指定がなければコマンド実行者、あれば指定されたプレイヤーを対象にする
        val targetPlayer = if (args.size > 1) {
            Bukkit.getPlayer(args[1])
        } else {
            sender
        }

        if (targetPlayer == null) {
            sender.sendMessage("§c対象プレイヤーが見つかりません。")
            return
        }

        sender.sendMessage("§a[テスト] ${amount}円のスーパーチャットをシミュレートします。(対象: ${targetPlayer.name})")

        val loc = targetPlayer.location
        when {
            amount >= 10000 -> {
                val spawnLoc = SpawnUtils.getSafeSpawnLocation(loc, 5, 10)
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
                // 効果: 盲目 II (1分)
                val blindEffect = PotionEffect(PotionEffectType.BLINDNESS, 200, 1)
                targetPlayer.addPotionEffect(blindEffect)
            }
            200 > amount -> {
                // クリーパー10体
                repeat(10) {
                    val spawnLoc = SpawnUtils.getSafeSpawnLocation(loc, 5, 10)
                    targetPlayer.world.spawnEntity(spawnLoc, EntityType.CREEPER)
                }
            }
            else -> {}
        }
    }
}
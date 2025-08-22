package me.gucchan.bougaiCraft.commands.subcommands

import me.gucchan.bougaiCraft.BougaiCraft
import me.gucchan.bougaiCraft.commands.SubCommand
import me.gucchan.bougaiCraft.utils.SpawnUtils
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.command.CommandSender
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
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

        val tier = when {
            amount < 200 -> 1
            amount in 200..<500 -> 2
            amount in 500..<1000 -> 3
            amount in 1000..<2000 -> 4
            amount in 2000..<5000 -> 5
            amount in 5000..<10000 -> 6
            else -> 7
        }

        when (tier) {
            // 青: クリーパー10体
            1 -> {
                repeat(10) {
                    val spawnLoc = SpawnUtils.getSafeSpawnLocation(targetPlayer.location, 5, 10)
                    targetPlayer.world.spawnEntity(spawnLoc, EntityType.CREEPER)
                }
            }
            // 水: 採掘速度上昇 II (1分)
            2 -> {
                val blindEffect = PotionEffect(PotionEffectType.BLINDNESS, 200, 1)
                targetPlayer.addPotionEffect(blindEffect)
            }
            // 緑: 移動速度上昇 II (1分)
            3 -> {
                val blindEffect = PotionEffect(PotionEffectType.SPEED, 200, 1)
                targetPlayer.addPotionEffect(blindEffect)
            }
            // 黄: ダイヤ装備全身
            4 -> {
                val inventory = targetPlayer.inventory
                // 既に装備があれば地面にドロップさせるため、直接セットする
                inventory.helmet = ItemStack(Material.DIAMOND_HELMET)
                inventory.chestplate = ItemStack(Material.DIAMOND_CHESTPLATE)
                inventory.leggings = ItemStack(Material.DIAMOND_LEGGINGS)
                inventory.boots = ItemStack(Material.DIAMOND_BOOTS)
            }
            // 橙: ワンパン剣
            5 -> {
                val onePunchSword = ItemStack(Material.GOLDEN_SWORD)
                val meta = onePunchSword.itemMeta
                if (meta != null) {
                    meta.setDisplayName("§6すごく強い剣")
                    meta.addEnchant(Enchantment.SHARPNESS, 255, true) // Sharpness 255
                    onePunchSword.itemMeta = meta
                    targetPlayer.inventory.addItem(onePunchSword)
                }
            }
            // マゼンタ: ウィザー (5分)
            6 -> {
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
            // 赤: ウォーデン (5分)
            7 -> {
                val spawnLoc = SpawnUtils.getSafeSpawnLocation(targetPlayer.location, 5, 10)
                val warden = targetPlayer.world.spawnEntity(spawnLoc, EntityType.WARDEN)
                // 5分後 (6000 ticks) にウォーデンを削除するタスクをスケジュールする
                Bukkit.getScheduler().runTaskLater(plugin, Runnable {
                    // ウォーデンがまだサーバーに存在し、かつ死んでいない場合のみ削除を実行
                    if (warden.isValid && !warden.isDead) {
                        warden.remove()
                        plugin.server.broadcastMessage("§a5分が経過したため、ウォーデンが消滅しました。")
                    }
                }, 6000L)
            }
            else -> {}
        }
    }
}
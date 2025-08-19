package me.gucchan.bougaiCraft.utils

import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import java.util.concurrent.ThreadLocalRandom
import kotlin.math.cos
import kotlin.math.sin

object SpawnUtils {
    /**
     * 指定された中心地の周囲に、安全なスポーン地点を探して返す
     * @param center 中心となる場所 (例: プレイヤーの位置)
     * @param minRadius 最小半径 (これより内側には湧かない)
     * @param maxRadius 最大半径 (これより外側には湧かない)
     * @param maxAttempts 最大試行回数
     * @return 安全なスポーン地点。見つからなければ中心地を返す
     */
    fun getSafeSpawnLocation(center: Location, minRadius: Int, maxRadius: Int, maxAttempts: Int = 10): Location {
        val world = center.world ?: return center // ワールドがなければ中心地を返す

        repeat(maxAttempts) {
            val random = ThreadLocalRandom.current()
            val angle = random.nextDouble(Math.PI * 2)
            val radius = random.nextInt(minRadius, maxRadius + 1)

            val x = center.x + radius * cos(angle)
            val z = center.z + radius * sin(angle)

            // 地表の最も高いブロックを探す
            val y = world.getHighestBlockYAt(x.toInt(), z.toInt()).toDouble()

            val spawnLocation = Location(world, x, y, z)

            // スポーン地点が安全かチェック
            if (isSafeLocation(spawnLocation)) {
                return spawnLocation.add(0.0, 1.0, 0.0) // 地表の1ブロック上にスポーン
            }
        }

        // 試行回数内に見つからなければ、安全策として中心地の上空にスポーン
        return center.add(0.0, 3.0, 0.0)
    }

    /**
     * その場所がモンスターのスポーンに適しているか判定する
     */
    private fun isSafeLocation(location: Location): Boolean {
        // 足元のブロックと、その下のブロックを取得
        val ground: Block = location.block
        val head: Block = location.clone().add(0.0, 1.0, 0.0).block

        // 足元が奈落や溶岩ではないこと
        if (ground.type == Material.LAVA || ground.type == Material.WATER || ground.isPassable) {
            return false
        }
        // 頭の位置にブロックがないこと（窒息防止）
        if (!head.isPassable) {
            return false
        }

        return true
    }
}
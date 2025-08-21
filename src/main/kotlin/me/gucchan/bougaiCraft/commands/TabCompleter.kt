package me.gucchan.bougaiCraft.commands

import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter

class TabCompleter : TabCompleter {
    override fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<out String>): List<String?> {
        var result: MutableList<String> = mutableListOf()

        when (args.size) {
            1 -> {
                result.addAll(listOf("authorize", "start", "stop", "reload"))
                result = result.filter {
                    it.startsWith(args[0])
                }.toMutableList()
            }
            2 if args[0] == "start" -> {
                result.clear()
                result.add("ライブ配信URL")
            }
            3 if args[0] == "start" -> {
                result.clear()
                result.add(sender.name)
            }
        }
        return result
    }
}
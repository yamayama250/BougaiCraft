package me.gucchan.bougaiCraft.commands

import org.bukkit.command.CommandSender

interface SubCommand {
    fun execute(sender: CommandSender, args: Array<out String>)
}

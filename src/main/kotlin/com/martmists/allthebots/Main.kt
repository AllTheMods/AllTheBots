package com.martmists.allthebots

import com.google.gson.Gson
import com.martmists.allthebots.commands.AllTheBots
import java.io.File

fun main(args: Array<String>){
    val conf = Gson().fromJson(File("config.json").reader(), Config::class.java)
    val core = AllTheBots(conf.prefixes, conf.owners, conf.commandPath)
    core.run(conf.token)
}

package com.martmists.allthebots

import com.google.gson.Gson
import com.martmists.chitose.entities.Core
import java.io.File

fun main(args: Array<String>){
    val conf = Gson().fromJson(File("config.json").reader(), Config::class.java)
    val core = Core(conf.prefixes, conf.owners, conf.commandPath)
    core.run(conf.token)
}

package com.martmists.allthebots.extensions

fun String.discordEscaped(): String {
    return this.apply{
        replace("_", "\\_")
        replace("*", "\\*")
        replace("`", "\\`")
        replace("@", "\\@")
    }
}
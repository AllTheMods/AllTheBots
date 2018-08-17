package com.martmists.allthebots

data class Config(
        val prefixes: Array<String>,
        val owners: Array<Long>,
        val commandPath: String,
        val token: String
)
package com.martmists.allthebots.entities.ars

import jdk.nashorn.internal.runtime.regexp.joni.exception.SyntaxException

class ARSHandler(input: String) {
    private val tokens = Tokenizer(input).tokenize().toMutableList()

    private var current: Tokenizer.Token = tokens.removeAt(0)
    private var last: Tokenizer.Token? = null


    fun parse(): Set {
        eat("ID")
        val name = last!!.content
        eat("ASSIGN")
        val actions = statements()
        return Set(name, actions)
    }

    private fun statements(): Array<Token> {
        val parsed = mutableListOf<Token>()
        parsed += statement()
        while (current.id == "SEMI"){
            eat("SEMI")
            parsed += statement()
        }
        return parsed.toTypedArray()
    }

    private fun embed(): Array<Token>{
        val embedProperties = mutableListOf<Token>()
        fun single(): Token {
            eat("LBRACKET")
            eat("ID")
            val property = last!!.content
            eat("COLON")
            val words = mutableListOf<String>()
            while (current.id == "ID") {
                eat("ID")
                words += last!!.content
            }
            val value = words.joinToString(" ")
            eat("RBRACKET")
            return EmbedProperty(property, value)
        }
        embedProperties += single()
        while (current.id == "SEMI"){
            eat("SEMI")
            if (current.id == "LBRACKET")
                embedProperties += single()
        }
        return embedProperties.toTypedArray()
    }

    private fun statement(): Token {
        eat("LBRACKET")
        eat("ID")
        val type = last!!.content
        eat("COLON")
        val token = when (type){
            "embed" -> {
                eat("SEMI")
                val children = embed()
                Embed(children)
            }
            "message" -> {
                val words = mutableListOf<String>()
                while (current.id == "ID") {
                    eat("ID")
                    words += last!!.content
                }
                Message(words.joinToString(" "))
            }
            "react" -> {
                eat("ID")
                val emote = last!!.content
                React(emote)
            }
            else -> NOP()
        }

        eat("RBRACKET")

        return token
    }

    private fun eat(type: String) {
        val next = tokens.removeAt(0)
        last = current
        if (current.id != type)
            throw SyntaxException("Expected $type, found ${current.id}")
        current = next
    }
}

package com.martmists.allthebots.entities.ars


class Tokenizer(val input: String) {
    data class Token(val id: String, val content: String)

    private var pos = 0
    private var current: Char?

    init {
        current = input[0]
    }

    private fun advance() {
        pos++
        current = if (pos > input.length - 1) {
            null
        } else {
            input[pos]
        }
    }

    private fun peek(): Char? {
        val peekPos = pos + 1
        return if (peekPos > input.length - 1){
            null
        } else {
            input[peekPos]
        }
    }

    private fun skipWhitespace(){
        while (current != null && current!! == ' ')
            advance()
    }

    private fun identifier(): Token {
        var result = ""

        while (current != null && !current!!.isWhitespace() && !"\n{}:.,=".toCharArray().contains(current!!)){
            result += current
            advance()
        }
        return Token("ID", result)
    }

    fun tokenize(): Array<Token> {
        val tokens = mutableListOf<Token>()

        while (current != null){
            when {
                current == ' ' -> skipWhitespace()
                current!! == '\n' -> {
                    tokens += Token("SEMI", ";")
                    advance()
                }
                current == '{' -> {
                    tokens += Token("LBRACKET", "{")
                    advance()
                }
                current == '}' -> {
                    tokens += Token("RBRACKET", "}")
                    advance()
                }
                current == ':' -> {
                    tokens += Token("COLON", ":")
                    advance()
                }
                current == '.' -> {
                    tokens += Token("DOT", ".")
                    advance()
                }
                current == ',' -> {
                    tokens += Token("COMMA", ",")
                    advance()
                }
                current == '=' -> {
                    tokens += Token("ASSIGN", "=")
                    advance()
                }
                else -> tokens += identifier()
                // else -> throw SyntaxException("Unexpected character '$current' at position $pos")
            }
        }

        tokens += Token("EOF", "")

        return tokens.toTypedArray()
    }
}
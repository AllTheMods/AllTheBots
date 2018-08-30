package com.martmists.allthebots.entities.ars

import org.parboiled.BaseParser
import org.parboiled.Rule
import org.parboiled.annotations.BuildParseTree


@BuildParseTree
open class ARSParser : BaseParser<Any>() {
    open fun ARS(): Rule {
        return Sequence(Expressions(), EOI)
    }

    open fun Whitespace(): Rule {
        return ZeroOrMore(AnyOf(" \t\n"))
    }

    open fun Expressions(): Rule {
        return OneOrMore(Expression())
    }

    open fun Set(): Rule {
        return Sequence(ID(), Whitespace(), '=', Whitespace(), ARS())
    }

    open fun Word(): Rule {
        return OneOrMore(FirstOf(Char(), Int(), Misc(), Emote(), AnyOf(": =")))
    }

    open fun ID(): Rule {
        return Sequence(Char(), ZeroOrMore(FirstOf(Char(), Int(), Misc())))
    }

    open fun Char(): Rule {
        return FirstOf(CharRange('a', 'z'), CharRange('A', 'Z'), "!")
    }

    open fun Emote(): Rule {
        return OneOrMore(CharRange('\uD000', '\uDFFF'))
    }

    open fun Int(): Rule {
        return CharRange('0', '9')
    }

    open fun Misc(): Rule {
        return AnyOf("[]()|.?_-/&$%*+@#,<>")
    }

    open fun Expression(): Rule {
        return Sequence(
                '{', Whitespace(), ID(), Whitespace(), ':', Whitespace(), Arguments(), Whitespace(), '}', Whitespace())
    }

    open fun Arguments(): Rule {
        return OneOrMore(Argument())
    }

    open fun Argument(): Rule {
        return Sequence(Whitespace(), FirstOf(Expression(), Word()), Whitespace())
    }
}
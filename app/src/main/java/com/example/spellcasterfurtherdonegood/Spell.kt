package com.example.spellcasterfurtherdonegood

class Spell {

    var name: String = ""
    var description: String = ""
    var level: Int = 0
    var category: String = ""

    constructor(name: String, description: String, level: Int, category: String) {
        this.name = name
        this.description = description
        this.level = level
        this.category = category
    }

    constructor()

    override fun toString(): String {
        return "Spell(name='$name', description='$description', level=$level, category='$category')"
    }
}

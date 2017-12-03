package com.squadup.squadup.data

import java.util.*

class Group(name: String) {

    var id: String

    var name: String = name

    var memberIDs: MutableList<String> = mutableListOf()

    var members: MutableList<User> = mutableListOf()

    init {
        val formattedName = name.replace(" ", "-").toLowerCase()
        id = formattedName + "-" + UUID.randomUUID().toString()
    }

    constructor(id: String, name: String): this(name) {
        this.id = id
    }



}
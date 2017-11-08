package com.squadup.squadup.data

class Group(id: String, name: String) {

    var id: String = id

    var name: String = name

    var members: MutableList<String> = mutableListOf()

}
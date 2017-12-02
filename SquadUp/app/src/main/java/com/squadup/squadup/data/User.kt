package com.squadup.squadup.data

class User(id: String, name: String) {

    var id: String = id

    var name: String = name

    var friendIDs: MutableList<String> = mutableListOf()

    var friends: MutableList<User> = mutableListOf()

    var groupIDs: MutableList<String> = mutableListOf()

    var groups: MutableList<Group> = mutableListOf()

    var registrationToken: String? = null

    var selected: Boolean = false

}
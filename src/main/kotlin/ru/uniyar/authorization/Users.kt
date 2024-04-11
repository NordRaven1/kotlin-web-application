package ru.uniyar.authorization

class Users(gotList: List<User>) {
    private val listOfUsers = gotList.toMutableList()
    val usersList: List<User> = listOfUsers

    fun add(user: User) {
        listOfUsers.add(user)
    }

    fun findUserByName(username: String): User? {
        return listOfUsers.find { it.userName == username }
    }

    fun findUserById(id: String): User? {
        return listOfUsers.find { it.userId == id }
    }

    fun editUser(
        id: String,
        user: User,
    ) {
        val i = getUserPositionById(id)
        user.userId = id
        listOfUsers[i] = user
    }

    private fun getUserPositionById(id: String): Int {
        listOfUsers.forEachIndexed { index, user ->
            if (user.userId == id) {
                return index
            }
        }
        return -1
    }
}

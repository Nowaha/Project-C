package xyz.nowaha.chengetawildlife.data.pojo

object EventStatus {

    private val statusStrings = hashMapOf(
        0 to "New",
        5 to "Underway",
        10 to "False alarm",
        25 to "Handled"
    )

    fun of(index: Int): String? {
        return statusStrings[index]
    }

}
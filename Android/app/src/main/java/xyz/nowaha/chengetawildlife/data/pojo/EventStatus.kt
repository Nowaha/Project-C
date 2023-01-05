package xyz.nowaha.chengetawildlife.data.pojo

object EventStatus {

    private val statusStrings = linkedMapOf(
        0 to "New",
        5 to "Underway",
        10 to "False alarm",
        25 to "Handled"
    )

    fun of(index: Int): String? = statusStrings[index]
    fun numberOf(value: String): Int? = statusStrings.filter { it.value.lowercase() == value.lowercase() }.map { it.key }.firstOrNull()
    fun getValues() = statusStrings.values
    fun getCopy() = HashMap(statusStrings)
    fun indexOfNumber(num: Int) = statusStrings.keys.indexOf(num)
    fun numberAtIndex(num: Int) = statusStrings.keys.toTypedArray()[num]

}
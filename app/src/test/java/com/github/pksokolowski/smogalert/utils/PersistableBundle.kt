package android.os

class PersistableBundle() {
    private val map = HashMap<String, Any>()

    fun putInt(key: String, value: Int) {
        map[key] = value
    }

    fun putBoolean(key: String, value: Boolean) {
        map[key] = value
    }

    fun getInt(key: String, default: Int): Int = (map[key] ?: default) as Int
    fun getBoolean(key: String, default: Boolean): Boolean = (map[key] ?: default) as Boolean

}
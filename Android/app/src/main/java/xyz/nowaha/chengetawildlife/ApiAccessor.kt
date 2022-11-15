package xyz.nowaha.chengetawildlife

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import xyz.nowaha.chengetawildlife.pojo.Event
import xyz.nowaha.chengetawildlife.extensions.*
import java.net.*
import java.util.*
import kotlin.collections.ArrayList

object ApiAccessor {

    private const val URL_BASE = "http://173.249.1.107:34100"

    suspend fun attemptLogin(username: String, password: String): JsonObject? =
        withContext(Dispatchers.IO) {
            return@withContext sendGet(
                "/user/login",
                "username" to username,
                "password" to password
            )
        }

    suspend fun getLatestEvents(): ArrayList<Event> =
        withContext(Dispatchers.IO) {
            return@withContext getLatestEvents(rows = null, offset = null)
        }

    suspend fun getLatestEvents(rows: Int?): ArrayList<Event> =
        withContext(Dispatchers.IO) {
            return@withContext getLatestEvents(rows, offset = null)
        }

    suspend fun getLatestEvents(rows: Int?, offset: Int?): ArrayList<Event> =
        withContext(Dispatchers.IO) {
            var args = ArrayList<Pair<String, String>>()
            if (rows != null) args.add("rows" to rows.toString())
            if (offset != null) args.add("offset" to offset.toString())

            var result = ArrayList<Event>()
            var responseObject: JsonObject =
                sendGet("/events/latest", *args.toTypedArray()) ?: return@withContext result

            if (!responseObject.getBoolean("success", false)) return@withContext result

            val data = responseObject.getJsonArray("data")
            for (entry in data) {
                val entryObject = entry as JsonObject
                result.add(
                    Event(
                        id = entryObject.getInt("Id", -1),
                        nodeId = entryObject.getInt("NodeId", -1),
                        date = entryObject.getLong("Date", -1),
                        latitude = entryObject.getFloat("Latitude", 0f),
                        longitude = entryObject.getFloat("Longitude", 0f),
                        soundLabel = entryObject.getString("SoundLabel", "ERROR"),
                        probability = entryObject.getInt("Probability", -1),
                        soundUrl = entryObject.getString("SoundURL", "ERROR")
                    )
                )
            }

            return@withContext result
        }

    suspend fun sendGet(path: String, vararg args: Pair<String, String>): JsonObject? =
        withContext(Dispatchers.IO) {
            var connection: HttpURLConnection? = null

            var attempts = 0

            while (attempts < 5) {
                try {
                    kotlin.runCatching {
                        val url = URL("$URL_BASE$path?" + args.joinToString("&") {
                            URLEncoder.encode(it.first) + "=" + URLEncoder.encode(it.second)
                        })

                        connection = url.openConnection() as HttpURLConnection;
                        with(connection as HttpURLConnection) {
                            requestMethod = "GET"
                            connectTimeout = 5000

                            setRequestProperty("Authorization", Session.key ?: "NO_SESSION")
                            setRequestProperty("Accept", "*")

                            connection!!.inputStream.bufferedReader().use {
                                it.readText()
                            }
                        }
                    }.onSuccess {
                        return@withContext Json.decodeFromString(it)
                    }.onFailure {
                        if (connection == null || (connection as HttpURLConnection).responseCode == 404 || (connection as HttpURLConnection).errorStream == null) {
                            return@withContext null;
                        }

                        (connection as HttpURLConnection).errorStream.bufferedReader().use {
                            return@withContext Json.decodeFromString(it.readText())
                        }
                    }
                } catch (ex: ProtocolException) {
                    ex.printStackTrace()
                    attempts++
                } catch (ex: SocketTimeoutException) {
                    ex.printStackTrace()
                    return@withContext null;
                }
            }

            return@withContext null;
        }

}
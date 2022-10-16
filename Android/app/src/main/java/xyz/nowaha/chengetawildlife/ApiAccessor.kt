package xyz.nowaha.chengetawildlife

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import java.io.BufferedInputStream
import java.net.*

object ApiAccessor {

    private const val URL_BASE = "http://192.168.178.175:34100"

    suspend fun attemptLogin(username: String, password: String): JsonObject? =
        withContext(Dispatchers.IO) {
            return@withContext sendGet(
                "/user/login",
                "username" to username,
                "password" to password
            )
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

                            println("Sent 'GET' request to URL : $url; Response Code : $responseCode")

                            connection!!.inputStream.bufferedReader().use {
                                it.readText()
                            }
                        }
                    }.onSuccess {
                        return@withContext Json.decodeFromString(it)
                    }.onFailure {
                        if (connection == null || (connection as HttpURLConnection).responseCode == 404) {
                            return@withContext null;
                        }

                        connection!!.errorStream.bufferedReader().use {
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
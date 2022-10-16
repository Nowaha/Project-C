package xyz.nowaha.chengetawildlife

import androidx.lifecycle.LifecycleCoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.concurrent.Future

object ApiAccessor {

    val urlBase = "http://192.168.178.175:34100"

    fun attemptLogin(username: String, password: String): String {
        return sendGet("/user/login", Pair("username", username), Pair("password", password))
    }

    fun sendGet(path: String, vararg args: Pair<String, String>): String {
        val url = URL(urlBase + path + "?" + args.joinToString("&") { URLEncoder.encode(it.first) + "=" + URLEncoder.encode(it.second) })

        with(url.openConnection() as HttpURLConnection) {
            requestMethod = "GET"
            setRequestProperty("Authorization", Session.key ?: "NO_SESSION")
            setRequestProperty("Accept", "text/html")

            println("\nSent 'GET' request to URL : $url; Response Code : $responseCode")

            inputStream.bufferedReader().use {
                println(it.readLines().joinToString("\n"))
            }
        }

        return "";
    }

}
package xyz.nowaha.chengetawildlife.data

import java.util.*

data class Event(var id: Int, var nodeId: Int, var date: Date, var latitude: Float, var longitude: Float, var soundLabel: String, var probability: Int, var soundUrl: String)
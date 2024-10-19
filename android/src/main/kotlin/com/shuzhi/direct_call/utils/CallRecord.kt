package com.shuzhi.direct_call.utils

data class CallRecord(
    val number: String,
    val type: Int,
    val date: Long,
    val duration: Long
) {
    // 将 CallRecord 转换为 Map
    fun toMap(): Map<String, Any> {
        return mapOf(
            "number" to number,
            "type" to type,
            "date" to date,
            "duration" to duration
        )
    }
}
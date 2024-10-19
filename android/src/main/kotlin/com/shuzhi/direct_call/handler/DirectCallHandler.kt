package com.shuzhi.direct_call.handler

import Permissions
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.CallLog
import android.telephony.TelephonyManager
import android.util.Log
import com.shuzhi.direct_call.utils.Constants
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import com.shuzhi.direct_call.listener.DirectCallListener
import com.shuzhi.direct_call.utils.CallRecord
import com.shuzhi.direct_call.utils.PhoneStateStatus

class DirectCallHandler(private val binding: FlutterPlugin.FlutterPluginBinding): MethodChannel.MethodCallHandler {
    // 权限
    private lateinit var permissions: Permissions

    private var phoneNumber = ""
    private var simSlot = 0

    // 消息传递器
    private var binaryMessenger: BinaryMessenger = binding.binaryMessenger;
    private lateinit var activityBinding: ActivityPluginBinding

    private var directCallMethodChannel : MethodChannel = MethodChannel(binaryMessenger, Constants.METHOD_CHANNEL)
    private lateinit var methodChannelResult: MethodChannel.Result
    private var directCallEventSink: EventChannel.EventSink? = null
    private var listener: DirectCallListener? = null

    // Context
    private var context: Context = binding.applicationContext

    init {
        directCallMethodChannel.setMethodCallHandler(this)

        EventChannel(binaryMessenger, Constants.EVENT_CHANNEL).setStreamHandler(
            object : EventChannel.StreamHandler {
                override fun onListen(arguments: Any?, events: EventChannel.EventSink?) {
                    directCallEventSink = events
                    initListener()
                }

                override fun onCancel(arguments: Any?) {
                    listener?.removeCallListener()
                }
            }
        )
    }

    fun bindActivityBinding(_activityBinding: ActivityPluginBinding) {
        activityBinding = _activityBinding

        permissions = object : Permissions(activityBinding, binding) {
            override fun onSuccess() {
                listener?.registerListener()
                // 继续拨打
                makePhoneCall()
            }

            override fun onReject() {
                methodChannelResult.success(false)
            }
        }
        permissions.addListener()
    }

    fun initListener() {
        listener = object : DirectCallListener(binding, permissions) {
            override fun onCallStateChangedCallback(_status: Int) {
                var status:PhoneStateStatus = PhoneStateStatus.CALL_STATE_IDLE
                var callRecord: CallRecord? = null

                when (_status) {
                    TelephonyManager.CALL_STATE_IDLE -> {
                        // 空闲状态：通话结束
                        // 通话结束
                        Log.i(Constants.TAG_NAME, "手机状态：通话结束")
                        status = PhoneStateStatus.CALL_STATE_IDLE
                        callRecord = getOutGoingCallRecord()
                        Log.i(Constants.TAG_NAME, "通话记录：$callRecord")
                    }
                    TelephonyManager.CALL_STATE_RINGING -> {
                        // 有来电
                        Log.i(Constants.TAG_NAME, "手机状态：来电话状态")
                        status = PhoneStateStatus.CALL_STATE_RINGING
                    }
                    // 正在拨号不一定触发
                    TelephonyManager.CALL_STATE_OFFHOOK -> {
                        // 至少存在一个正在拨号、处于活动状态或处于挂起状态的呼叫，并且没有有来电。
                        // 接听电话或者拨打电话
                        Log.i(Constants.TAG_NAME, "手机状态：正在接听电话/拨号")
                        status = PhoneStateStatus.CALL_STATE_OFFHOOK
                    }
                    else -> {
                        // 未知状态
                        Log.i(Constants.TAG_NAME, "手机状态，非通话相关")
                        status = PhoneStateStatus.CALL_STATE_OTHER
                    }
                }

                directCallEventSink?.success(mapOf(
                    "status" to status.name,
                    "callRecord" to callRecord?.toMap()
                ))
            }
        }
    }

    fun dispose() {
        directCallMethodChannel.setMethodCallHandler(null)
    }

    private fun getOutGoingCallRecord(): CallRecord? {
        val callRecords: MutableList<CallRecord> = getCallRecord()

        val callRecord = callRecords.find { it ->
            it.number == phoneNumber && it.type == CallLog.Calls.OUTGOING_TYPE
        }

        return callRecord
    }

    private fun getCallLogCount(): Int {
        val cursor = context.contentResolver.query(
            CallLog.Calls.CONTENT_URI,
            arrayOf(CallLog.Calls._ID),
            null,
            null,
            null
        )
        val count = cursor?.count ?: 0
        cursor?.close()
        return count
    }

    @SuppressLint("Range")
    private fun getCallRecord(limit: Int = 2000): MutableList<CallRecord> {
        val callRecords = mutableListOf<CallRecord>()

        val totalCalls = getCallLogCount()
        val queryLimit = if (totalCalls <= limit) totalCalls else limit

        val callRecord = context.contentResolver.query(CallLog.Calls.CONTENT_URI, arrayOf(
            CallLog.Calls.NUMBER,
            CallLog.Calls.DATE,
            CallLog.Calls.DURATION,
            CallLog.Calls.TYPE,
        ), null, null,"${CallLog.Calls.DATE} DESC")

        if (callRecord != null && callRecord.moveToFirst()) {
            // 创建 Map 来统计每条通话记录的通话次数
            var count = 0
            // 遍历查询结果
            while (callRecord.moveToNext() && count < queryLimit) {
                val callNumber = callRecord.getString(callRecord.getColumnIndex(CallLog.Calls.NUMBER))
                val callDate = callRecord.getLong(callRecord.getColumnIndex(CallLog.Calls.DATE))
                val callDuration = callRecord.getLong(callRecord.getColumnIndex(CallLog.Calls.DURATION))
                val callType = callRecord.getInt(callRecord.getColumnIndex(CallLog.Calls.TYPE))
//                val callType = when (callStatus) {
//                    CallLog.Calls.INCOMING_TYPE -> "incoming"
//                    CallLog.Calls.OUTGOING_TYPE -> "outgoing"
//                    else -> null
//                }

                val callRecord = CallRecord(callNumber, callType, callDate, callDuration)
                callRecords.add(callRecord)
                count++
            }
        }

        return callRecords
    }

    private fun makePhoneCall() {
        val activity = activityBinding.activity

        val intent = Intent(Intent.ACTION_CALL).apply {
            data = Uri.parse("tel:$phoneNumber")
            // 这里需要设置 SIM 卡的插槽索引
            putExtra("com.android.phone.force.slot", true)
            putExtra("com.android.phone.extra.slot", simSlot)
        }

        if (intent.resolveActivity(context.packageManager) != null) {
            activity.startActivity(intent)
            methodChannelResult.success(true)
        } else {
            methodChannelResult.error("ERROR", "No app found to handle phone call", null)
        }

    }

    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        when (call.method) {
            "makePhoneCall" -> {
                 phoneNumber = call.argument<String>("number")!!
                 simSlot = call.argument<Int>("simSlot") ?: 0
                 methodChannelResult = result

                if (permissions.requestPermissions()) {
                    makePhoneCall()
                }
            }

            else -> result.notImplemented()
        }
    }
}

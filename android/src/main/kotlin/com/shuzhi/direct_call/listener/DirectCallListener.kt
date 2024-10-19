
package com.shuzhi.direct_call.listener

import Permissions
import android.content.Context
import android.os.Build
import android.telephony.PhoneStateListener
import android.telephony.TelephonyCallback
import android.telephony.TelephonyManager
import android.util.Log
import com.shuzhi.direct_call.utils.Constants
import io.flutter.embedding.engine.plugins.FlutterPlugin

open class DirectCallListener(private val binding: FlutterPlugin.FlutterPluginBinding, private val permissions: Permissions) {
    // 是否正在监听
    private var isRegisterListen: Boolean = false

    // 电话管理
    private var telephonyManager: TelephonyManager? = null

    // 电话状态监听器
    private var high_phoneStateListener: TelephonyCallback? = null
    private var low_phoneStateListener: PhoneStateListener? = null

    // Context
    private var context: Context = binding.applicationContext

    init {
        registerListener()
    }

    // 若没有权限 监听是无效的
    fun registerListener() {
        // 通话监听
        if (isRegisterListen || !permissions.isPermission()) return

        // 获取 TelephonyManager
        telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            high_phoneStateListener = object : TelephonyCallback(), TelephonyCallback.CallStateListener {
                override fun onCallStateChanged(state: Int) {
                    onCallStateChangedCallback(state)
                }
            }

            // 注册监听
            telephonyManager?.registerTelephonyCallback(context.mainExecutor, high_phoneStateListener!!)
            isRegisterListen = true
        } else {
            low_phoneStateListener = object : PhoneStateListener() {
                override fun onCallStateChanged(state: Int, _phoneNumber: String?) {
                    onCallStateChangedCallback(state)
                }
            }

            // 注册监听
            telephonyManager?.listen(low_phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
            isRegisterListen = true
        }
    }

    open fun onCallStateChangedCallback(state: Int) {}

    fun removeCallListener() {
        isRegisterListen = false

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (high_phoneStateListener != null) telephonyManager?.unregisterTelephonyCallback(high_phoneStateListener!!)
        } else {
            if (low_phoneStateListener != null) telephonyManager?.listen(low_phoneStateListener, PhoneStateListener.LISTEN_NONE)
        }
    }
}
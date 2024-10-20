package com.shuzhi.direct_call

import android.content.pm.PackageManager
import com.shuzhi.direct_call.handler.DirectCallHandler
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding

/** DirectCallPlugin */
class DirectCallPlugin(): FlutterPlugin, ActivityAware {
  /// The MethodChannel that will the communication between Flutter and native Android
  ///
  /// This local reference serves to register the plugin with the Flutter Engine and unregister it
  /// when the Flutter Engine is detached from the Activity
  private lateinit var directtCallHandler: DirectCallHandler
  private lateinit var activityBinding: ActivityPluginBinding

  override fun onAttachedToEngine(binding: FlutterPlugin.FlutterPluginBinding) {
    directtCallHandler = DirectCallHandler(binding)
  }

  override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
    directtCallHandler.dispose()
  }

  override fun onAttachedToActivity(binding: ActivityPluginBinding) {
    activityBinding = binding
    directtCallHandler.bindActivityBinding(binding)
  }

  override fun onDetachedFromActivity() {
//    activityBinding = null
  }

  override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
    activityBinding = binding
    directtCallHandler.bindActivityBinding(binding)
  }

  override fun onDetachedFromActivityForConfigChanges() {
//    activityBinding = null
  }
}

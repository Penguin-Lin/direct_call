import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.shuzhi.direct_call.utils.Constants
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding

open class Permissions(private val activityBinding: ActivityPluginBinding, private val binding: FlutterPlugin.FlutterPluginBinding) {
    // 权限
    private val REQUEST_CODE_CALL = 1000

    // Context
    private var context: Context = binding.applicationContext

    private fun findPermissions(): Array<String> {
        val permission = arrayOf(
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.CALL_PHONE
        ).filter { it -> ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED }.toTypedArray()

        return permission
    }

    fun isPermission(): Boolean {
        return findPermissions().isEmpty()
    }

    fun requestPermissions(): Boolean {
        val activity = activityBinding.activity
        val permissions = findPermissions()

        return if (permissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(activity, permissions, REQUEST_CODE_CALL)
            false
        } else true
    }

    open fun onSuccess (){}

    open fun onReject() {}

    fun addListener() {
        // 添加权限申请监听器
        activityBinding.addRequestPermissionsResultListener { requestCode, permissions, grantResults ->
            onRequestPermissionsResult(requestCode, permissions, grantResults)
            true
        }
    }

    // 权限回调
    private fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == REQUEST_CODE_CALL) {
            Log.i(Constants.TAG_NAME, "权限：$${grantResults}")

            val isAllGranted = permissions.all { it ->
                val index = permissions.indexOf(it)
                val grantResult = grantResults[index]

                grantResult == PackageManager.PERMISSION_GRANTED
            }

            if (isAllGranted) {
                // 同意权限 继续拨打电话
                Log.i(Constants.TAG_NAME, "拨打电话的权限被允许")
                onSuccess()
            } else {
                // 权限被拒绝，提示用户
                Log.i(Constants.TAG_NAME, "拨打电话的权限被拒绝")
                onReject()
            }
        }
    }
}
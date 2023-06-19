package com.example.fire_warning_app

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.fragment.app.Fragment
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class UI_main : Fragment() {

    private lateinit var userApi: UserApi
    private lateinit var sensorApi: SensorApi
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var handler: Handler
    private val delayMillis2: Long = 2000L
    private val delayMillis1: Long = 1000L
    private val delayMillis10: Long = 10000L

    // Khai báo ID cho kênh thông báo (phải là duy nhất)
    private val CHANNEL_ID_FIRE = "fire_warning_channel"
    private val CHANNEL_ID_SMOKE = "smoke_warning_channel"
    // Khai báo MediaPlayer để phát âm thanh
    private lateinit var mediaPlayer: MediaPlayer


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.activity_ui_main, container, false)
        sharedPreferences = requireActivity().getSharedPreferences("MY_PREFS_NAME", Context.MODE_PRIVATE)

        // Khởi tạo UserApi
        val userRetrofit = Retrofit.Builder()
            .baseUrl("https://anor.pythonanywhere.com/user/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        userApi = userRetrofit.create(UserApi::class.java)

        // Khởi tạo SensorApi
        val sensorRetrofit = Retrofit.Builder()
            .baseUrl("https://anor.pythonanywhere.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        sensorApi = sensorRetrofit.create(SensorApi::class.java)

        val email = sharedPreferences.getString("email", null)

        val etUsername: TextView = view.findViewById(R.id.et_user_name)
        val smokeTextView: TextView = view.findViewById(R.id.sensor_gas)
        val temperatureTextView: TextView = view.findViewById(R.id.sensor_temp)

        handler = Handler(Looper.getMainLooper())

        // Hàm kiểm tra và cập nhật giá trị smoke và fire
        fun checkSensorData() {
            val smoke = sharedPreferences.getFloat("smokeValue", 0.0f)
            val fire = sharedPreferences.getBoolean("fireDetectedValue", false)

            // Kiểm tra giá trị smoke và fire và thực hiện các hành động tương ứng
            if (fire) {
                showFireNotification()
            }

            if (smoke > 0) {
                showSmokeNotification()
            }
        }

        userApi.getUsers().enqueue(object : Callback<List<User>> {
            override fun onResponse(call: Call<List<User>>, response: Response<List<User>>) {
                val users = response.body()
                if (users != null) {
                    // Tìm kiếm người dùng có email cần tìm
                    val emailToFind = email
                    val foundUser = users.find { it.email == emailToFind }
                    if (foundUser != null) {
                        // Lấy thông tin người dùng
                        val userId = foundUser.id
                        val username = foundUser.username
                        val password = foundUser.password
                        // Tiếp tục xử lý tại đây
                        val editor = sharedPreferences.edit()
                        editor.putString("username", username)
                        editor.putString("password", password)
                        editor.apply()

                        etUsername.text = username
                    }
                } else {
                    Toast.makeText(requireContext(), "failed?", Toast.LENGTH_SHORT).show()
                }
                // Gọi lại API sau 2 giây
                handler.postDelayed({
                    userApi.getUsers().enqueue(this)
                }, delayMillis2)
            }

            override fun onFailure(call: Call<List<User>>, t: Throwable) {
                // Xử lý lỗi tại đây

                // Gọi lại API sau 2 giây khi có lỗi xảy ra
                handler.postDelayed({
                    userApi.getUsers().enqueue(this)
                }, delayMillis2)
            }
        })

        sensorApi.getSensorData().enqueue(object : Callback<SensorApi.SensorData> {
            override fun onResponse(
                call: Call<SensorApi.SensorData>,
                response: Response<SensorApi.SensorData>
            ) {
                if (response.isSuccessful) {
                    val sensorData = response.body()
                    if (sensorData != null) {
                        // Xử lý dữ liệu cảm biến ở đây
                        val fireDetectedValue = sensorData.fireDetectedValue
                        val smokeValue = sensorData.smokeValue
                        val temperatureValue = sensorData.temperatureValue

                        // Lưu giá trị smokeValue và temperatureValue vào SharedPreferences
                        val editor = sharedPreferences.edit()
                        editor.putFloat("smokeValue", smokeValue)
                        editor.putFloat("temperatureValue", temperatureValue)
                        editor.putBoolean("fireDetectedValue", fireDetectedValue)
                        editor.apply()
                        // Tiếp tục xử lý và cập nhật giao diện
                        // Ví dụ: Hiển thị giá trị smokeValue và temperatureValue lên TextView
                        smokeTextView.text = smokeValue.toString() + " V"
                        temperatureTextView.text = temperatureValue.toString() + " ℃"

                        // Kiểm tra và cập nhật giá trị smoke và fire
//                        checkSensorData()
                    }
                } else {
                    // Xử lý lỗi khi không thành công
                    Toast.makeText(requireContext(), "sensor failed", Toast.LENGTH_SHORT).show()
                }

                // Gọi lại API sau 1 giây
                handler.postDelayed({
                    sensorApi.getSensorData().enqueue(this)
                }, delayMillis1)
            }

            override fun onFailure(call: Call<SensorApi.SensorData>, t: Throwable) {
                // Xử lý lỗi kết nối
                Toast.makeText(requireContext(), "Sensor failed", Toast.LENGTH_SHORT).show()

                // Gọi lại API sau 1 giây khi có lỗi xảy ra
                handler.postDelayed({
                    sensorApi.getSensorData().enqueue(this)
                }, delayMillis1)
            }
        })

        // Khởi động việc kiểm tra và cập nhật giá trị smoke và fire thường xuyên
        handler.postDelayed(object : Runnable {
            override fun run() {
                checkSensorData()
                handler.postDelayed(this, delayMillis10)
            }
        }, delayMillis1)
        checkSensorData()
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        createNotificationChannels()
        // Khởi tạo MediaPlayer
        mediaPlayer = MediaPlayer()
        createNotificationChannels()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Hủy handler khi Fragment bị hủy
//        handler.removeCallbacksAndMessages(null)
        // Giải phóng MediaPlayer khi Fragment bị hủy
        mediaPlayer.release()
        handler.removeCallbacksAndMessages(null)
    }

    // Hàm tạo kênh thông báo
    private fun createNotificationChannels() {
        // Kiểm tra phiên bản Android
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val fireChannel = NotificationChannel(
                CHANNEL_ID_FIRE,
                "Fire Warning",
                NotificationManager.IMPORTANCE_HIGH
            )
            fireChannel.description = "Channel for Fire Warning"
            // Tạo kênh thông báo cho cảnh báo cháy
            val fireNotificationManager =
                requireActivity().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            fireNotificationManager.createNotificationChannel(fireChannel)

            val smokeChannel = NotificationChannel(
                CHANNEL_ID_SMOKE,
                "Smoke Warning",
                NotificationManager.IMPORTANCE_HIGH
            )
            smokeChannel.description = "Channel for Smoke Warning"
            // Tạo kênh thông báo cho cảnh báo khói
            val smokeNotificationManager =
                requireActivity().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            smokeNotificationManager.createNotificationChannel(smokeChannel)
        }
    }

    private fun playNotificationSound(soundUri: Uri) {
        val mediaPlayer = MediaPlayer()
        mediaPlayer.setDataSource(requireContext(), soundUri)
        mediaPlayer.setOnPreparedListener { player ->
            player.start()
        }
        mediaPlayer.setOnCompletionListener { player ->
            player.reset() // Đặt lại trạng thái MediaPlayer sau khi hoàn thành phát
        }
        mediaPlayer.setOnErrorListener { player, _, _ ->
            player.reset() // Đặt lại trạng thái MediaPlayer nếu có lỗi xảy ra
            false
        }
        mediaPlayer.prepareAsync()
    }
    private fun showFireNotification() {
        val soundUri = Uri.parse("android.resource://com.example.fire_warning_app/${R.raw.female_voice}")
        playNotificationSound(soundUri)

        val notificationId = 1
        val notificationBuilder = NotificationCompat.Builder(requireContext(), CHANNEL_ID_FIRE)
            .setContentTitle("Fire Detected!")
            .setContentText("There is a fire detected in your area.")
            .setSmallIcon(R.drawable.fire96)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setSound(soundUri) // Thêm âm thanh vào thông báo

        val notificationManager =
            requireActivity().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, notificationBuilder.build())
        notificationBuilder.setSound(null)
    }

    private fun showSmokeNotification() {
        val soundUri = Uri.parse("android.resource://com.example.fire_warning_app/${R.raw.female_voice}")
        playNotificationSound(soundUri)

        val notificationId = 2
        val notificationBuilder = NotificationCompat.Builder(requireContext(), CHANNEL_ID_SMOKE)
            .setContentTitle("Smoke Detected!")
            .setContentText("There is a smoke detected in your area.")
            .setSmallIcon(R.drawable.fire96)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setSound(soundUri) // Thêm âm thanh vào thông báo

        val notificationManager =
            requireActivity().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, notificationBuilder.build())

        notificationBuilder.setSound(null)
    }

}

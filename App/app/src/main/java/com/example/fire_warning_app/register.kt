package com.example.fire_warning_app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class register : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        // get reference to all views
        val btn_login = findViewById<Button>(R.id.btn_login)
        val et_gmail = findViewById(R.id.et_gmail) as EditText
        var et_user_name = findViewById(R.id.et_user_name) as EditText
        var et_password = findViewById(R.id.et_password) as EditText
        var btn_reset = findViewById(R.id.btn_reset) as Button
        var btn_submit = findViewById(R.id.btn_submit) as Button

        val retrofit = Retrofit.Builder()
            .baseUrl("https://anor.pythonanywhere.com/user/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()


        btn_reset.setOnClickListener {
            // clearing user_name and password edit text views on reset button click
            et_user_name.setText("")
            et_password.setText("")
            et_gmail.setText("")
        }

        // set on-click listener
//        btn_submit.setOnClickListener {
//            val gmail = et_gmail.text;
//            val user_name = et_user_name.text;
//            val password = et_password.text;
//            val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
//            val isEmailValid = gmail.matches(emailPattern.toRegex())
//            if (isEmailValid && password.isNotEmpty() && user_name.isNotEmpty()) {
//                Toast.makeText(this@register, "connect successed", Toast.LENGTH_LONG).show()
//                val intent = Intent(this@register, UI_main::class.java)
//                startActivity(intent)
//            } else {
//                Toast.makeText(this@register, "connect failed", Toast.LENGTH_LONG).show()
//            }
//
//        }

        btn_submit.setOnClickListener {
            val gmail = et_gmail.text.toString()
            val user_name = et_user_name.text.toString()
            val password = et_password.text.toString()
            val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
            val isEmailValid = gmail.matches(emailPattern.toRegex())

            if (isEmailValid && password.isNotEmpty() && user_name.isNotEmpty()) {
                // Tạo đối tượng UserRegister từ dữ liệu nhập vào
                val user = UserRegister(user_name, password, gmail)

                // Khởi tạo Retrofit và gọi API đăng ký người dùng
                val registerApi = retrofit.create(RegisterApi::class.java)
                val call = registerApi.createUser(user)

                // Xử lý kết quả trả về
                call.enqueue(object : Callback<Void> {
                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                        if (response.isSuccessful) {
                            // Lưu thông tin người dùng vào SharedPreferences
                            val sharedPref = getSharedPreferences("MY_PREFS_NAME", MODE_PRIVATE)
                            val editor = sharedPref.edit()
                            editor.putString("email", gmail)
                            editor.putString("username", user_name)
                            editor.putString("password", password)
                            editor.apply()

                            // Chuyển sang màn hình UI_main
                            val intent = Intent(this@register, Bottom::class.java)
                            startActivity(intent)
                        } else {
                            Toast.makeText(this@register, "Đăng ký thất bại", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<Void>, t: Throwable) {
                        Toast.makeText(this@register, "Lỗi: " + t.message, Toast.LENGTH_SHORT).show()
                    }
                })
            } else {
                Toast.makeText(this@register, "Vui lòng nhập đủ thông tin và email hợp lệ", Toast.LENGTH_LONG).show()
            }
        }

// Thêm sự kiện vào button
        btn_login.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
}
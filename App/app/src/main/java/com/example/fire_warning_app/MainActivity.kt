package com.example.fire_warning_app
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
class MainActivity : AppCompatActivity() {

    private lateinit var etGmail: EditText
    private lateinit var etPassword: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // get reference to all views
        val btnSignin = findViewById<Button>(R.id.btn_signin)
        etGmail = findViewById(R.id.et_gmail)
        etPassword = findViewById(R.id.et_password)
        val btnReset = findViewById<Button>(R.id.btn_reset)
        val btnSubmit = findViewById<Button>(R.id.btn_submit)
        val btn_signin = findViewById<Button>(R.id.btn_signin)
        val retrofit = Retrofit.Builder()
            .baseUrl("https://anor.pythonanywhere.com/user/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val sharedPreferences = getSharedPreferences("MY_PREFS_NAME", MODE_PRIVATE)

        // Kiểm tra xem người dùng đã đăng nhập hay chưa
        val sharedPref = getSharedPreferences("MY_PREFS_NAME", MODE_PRIVATE)
        val email = sharedPref.getString("email", null)
        if (email != null) {
            val intent = Intent(this@MainActivity, Bottom::class.java)
            startActivity(intent)
            finish()
        }

        btnReset.setOnClickListener {
            // clearing user_name and password edit text views on reset button click
            etGmail.setText("")
            etPassword.setText("")
        }

        btnSubmit.setOnClickListener {
            val email = etGmail.text.toString()
            val password = etPassword.text.toString()
            // Check if the email is valid
            val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
            val isEmailValid = email.matches(emailPattern.toRegex())
            if (isEmailValid && password.isNotEmpty()) {


                val loginApi = retrofit.create(LoginApi::class.java)

                val request = LoginRequest(email, password)

                loginApi.login(request).enqueue(object : Callback<LoginResponse> {
                    override fun onResponse(
                        call: Call<LoginResponse>,
                        response: Response<LoginResponse>
                    ) {
                        if (response.isSuccessful) {
                            // Phản hồi thành công
                            val loginResponse = response.body()
                            if (loginResponse != null ) {
//                                val sessionManager = SessionManager(this@MainActivity)
//                                sessionManager.saveCredentials(email, password)

                                Toast.makeText(
                                    this@MainActivity,
                                    loginResponse.message,
                                    Toast.LENGTH_SHORT
                                ).show()
                                // Lưu tên đăng nhập vào Shared Preferences
                                val sharedPref = getSharedPreferences("MY_PREFS_NAME", MODE_PRIVATE)
                                val editor = sharedPref.edit()
                                editor.putString("email", email)
                                editor.apply()
                                val intent = Intent(this@MainActivity, Bottom::class.java)
                                startActivity(intent)
                            } else {
                                Toast.makeText(
                                    this@MainActivity,
                                    "Invalid email or password",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } else {
                            // Phản hồi thất bại
//                            val errorBody = response.errorBody()?.string()
//                            Toast.makeText(this@MainActivity, errorBody, Toast.LENGTH_SHORT).show()
                            Toast.makeText(this@MainActivity, "connect failed", Toast.LENGTH_LONG).show()
                        }
                    }

                    override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                        // Xử lý lỗi khi gọi API thất bại
                        Toast.makeText(this@MainActivity, "Failed to call API", Toast.LENGTH_SHORT)
                            .show()
                    }
                })
            }
        else{
                Toast.makeText(this@MainActivity, "connect failed", Toast.LENGTH_LONG).show()
        }
        }
        btn_signin.setOnClickListener {
            val intent = Intent(this, register::class.java)
            startActivity(intent)
        }
    }
    // Khai báo phương thức onDestroy() ở đây
//    override fun onDestroy() {
//        // Đoạn code xử lý khi Activity bị destroy ở đây
//        val sharedPref = getSharedPreferences("MY_PREFS_NAME", MODE_PRIVATE)
//        val editor = sharedPref.edit()
//        editor.remove("email")
//        editor.apply()
//        super.onDestroy()
//    }
}






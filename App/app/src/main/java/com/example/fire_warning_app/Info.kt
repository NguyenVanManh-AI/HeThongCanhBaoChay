package com.example.fire_warning_app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class Info : Fragment() {
    private lateinit var etEmailAddress: EditText
    private lateinit var etUserName: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnSubmit: Button
    private lateinit var btnLogout: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.activity_info, container, false)
        val sharedPreferences =
            requireActivity().getSharedPreferences("MY_PREFS_NAME", Context.MODE_PRIVATE)

        etEmailAddress = view.findViewById(R.id.et_email_address)
        etUserName = view.findViewById(R.id.et_user_name)
        etPassword = view.findViewById(R.id.et_password)
        btnSubmit = view.findViewById(R.id.btn_submit)
        btnLogout = view.findViewById(R.id.btn_logout)

        val email = sharedPreferences.getString("email", "")
        etEmailAddress.setText(email)
        val username = sharedPreferences.getString("username", "")
        etUserName.setText(username)
        val password = sharedPreferences.getString("password", "")
        etPassword.setText(password)

        val retrofit = Retrofit.Builder()
            .baseUrl("https://anor.pythonanywhere.com/user/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        btnSubmit.setOnClickListener {
            val user_name = etUserName.text.toString()
            val password = etPassword.text.toString()
            val gmail = etEmailAddress.text.toString()

            if (password.isNotEmpty() && user_name.isNotEmpty()) {
                val user = UserUpdate(user_name, password, gmail)

                val UpdateUserApi = retrofit.create(UpdateUserApi::class.java)
                val call = UpdateUserApi.updateUser(user)

                call.enqueue(object : Callback<Void> {
                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                        if (response.isSuccessful) {
                            val sharedPref =
                                requireActivity().getSharedPreferences(
                                    "MY_PREFS_NAME",
                                    Context.MODE_PRIVATE
                                )
                            val editor = sharedPref.edit()
                            editor.putString("username", user_name)
                            editor.putString("password", password)
//                            editor.putString("email", gmail)
                            editor.apply()

                            val intent = Intent(requireActivity(), Bottom::class.java)
                            startActivity(intent)
                        } else {
                            Toast.makeText(
                                requireContext(),
                                "Cập nhật thất bại",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        }
                    }

                    override fun onFailure(call: Call<Void>, t: Throwable) {
                        Toast.makeText(
                            requireContext(),
                            "Lỗi: " + t.message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })
            } else {
                Toast.makeText(
                    requireContext(),
                    "Vui lòng nhập đủ thông tin và email hợp lệ",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        btnLogout.setOnClickListener {
            val editor = sharedPreferences.edit()
            editor.remove("email")
            editor.remove("username")
            editor.remove("password")
            editor.apply()
            Toast.makeText(requireContext(), "logout", Toast.LENGTH_SHORT).show()

            // Chuyển đến màn hình đăng nhập
            val intent = Intent(requireActivity(), MainActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }

        return view
    }
}

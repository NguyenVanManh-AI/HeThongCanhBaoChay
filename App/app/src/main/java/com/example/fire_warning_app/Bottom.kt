package com.example.fire_warning_app

import android.icu.text.IDNA
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView


class Bottom : AppCompatActivity() {
    private lateinit var bottomNavigationView: BottomNavigationView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bottom)
        bottomNavigationView = findViewById(R.id.bottomNavigationView)
        bottomNavigationView.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.layout_Home -> {
                    // Xử lý khi người dùng chọn Home
                    val UI_Fragment = UI_main()
                    replaceFragment(UI_Fragment)

                    true
                }
                R.id.layout_History -> {
                    // Chuyển sang Dashboard Fragment
                    val HistoryFragment = ImageListActivity()
                    replaceFragment(HistoryFragment)
                    true
                }
                R.id.layout_info -> {
                    // Xử lý khi người dùng chọn Notifications
                    val InfoFragment = Info()
                    replaceFragment(InfoFragment)
                    true
                }
                else -> false
            }
        }
        // Thiết lập mặc định là tab Home
        bottomNavigationView.selectedItemId = R.id.layout_Home
    }
    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}
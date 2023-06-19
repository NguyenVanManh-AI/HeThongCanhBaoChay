package com.example.fire_warning_app

import android.content.Context
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
import com.google.android.material.bottomnavigation.BottomNavigationView
class ImageListActivity : Fragment() {

    private lateinit var listView: ListView
    private lateinit var adapter: ImageDataAdapter
    private val IMAGES_PER_PAGE = 10
    private var currentPage = 1
    private lateinit var showMoreButton: Button
    private var shouldShowMoreButton = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.activity_image_list, container, false)

        listView = view.findViewById(R.id.listView)
        showMoreButton = view.findViewById(R.id.showMoreButton)

        // Khởi tạo adapter
        adapter = ImageDataAdapter(requireContext(), R.layout.item_image, mutableListOf())
        listView.adapter = adapter

        // Gọi API để lấy danh sách ImageData
        val retrofit = Retrofit.Builder()
            .baseUrl("https://anor.pythonanywhere.com/image/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(ImageApiService::class.java)
        fetchImageDataList(api, currentPage)

        showMoreButton.setOnClickListener {
            currentPage++
            fetchImageDataList(api, currentPage)
        }
        listView.setOnScrollListener(object : AbsListView.OnScrollListener {
            override fun onScroll(
                view: AbsListView?,
                firstVisibleItem: Int,
                visibleItemCount: Int,
                totalItemCount: Int
            ) {
                val lastVisibleItem = firstVisibleItem + visibleItemCount - 1
                shouldShowMoreButton = (lastVisibleItem >= totalItemCount - 1)

                if (shouldShowMoreButton) {
                    showMoreButton.visibility = View.VISIBLE
                } else {
                    showMoreButton.visibility = View.GONE
                }
            }

            override fun onScrollStateChanged(view: AbsListView?, scrollState: Int) {
                // Không cần xử lý
            }
        })
    return view
    }

    private fun fetchImageDataList(api: ImageApiService, page: Int) {
        GlobalScope.launch {
            try {
                val imageDataList = api.getAllImages().toMutableList()
                imageDataList.sortByDescending { it.created_at }
                val start = (page - 1) * IMAGES_PER_PAGE
                val imagesToAdd = imageDataList.subList(start, start + IMAGES_PER_PAGE)

                withContext(Dispatchers.Main) {
                    if (currentPage == 1) {
                        adapter.clear()
                    }
                    adapter.addAll(imagesToAdd)
                    adapter.notifyDataSetChanged()

                    val lastVisibleItem = listView.lastVisiblePosition
                    val totalItemCount = listView.count

                    shouldShowMoreButton = (lastVisibleItem >= totalItemCount - 1)

                    if (imagesToAdd.size < IMAGES_PER_PAGE) {
                        showMoreButton.visibility = View.GONE
                    } else {
                        showMoreButton.visibility = if (shouldShowMoreButton) View.VISIBLE else View.GONE
                    }
                }
            } catch (e: Exception) {
                // Xử lý lỗi
            }
        }
    }



    inner class ImageDataAdapter(
        context: Context,
        private val resource: Int,
        private val imageDataList: MutableList<ImageData>
    ) : ArrayAdapter<ImageData>(context, resource, imageDataList) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view: View = convertView ?: LayoutInflater.from(context)
                .inflate(resource, parent, false)

            val createdAtTextView: TextView = view.findViewById(R.id.createdAtTextView)
            val imageView: ImageView = view.findViewById(R.id.imageView)

            val imageData: ImageData = imageDataList[position]

            val dateString = imageData.created_at
            val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val date = dateFormat.parse(dateString)

            val formattedDateFormat =
                java.text.SimpleDateFormat("HH:mm:ss' 'dd/MM/yyyy", Locale.getDefault())
            val formattedDate = formattedDateFormat.format(date)

            createdAtTextView.text = formattedDate

            Picasso.get().load(imageData.image_url).into(imageView)

            return view
        }
    }
}

package com.example.ziakaa

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.ziakaa.databinding.ActivityPayOutBinding
import com.example.ziakaa.databinding.FragmentCongratesBottomSheetBinding

class PayOutActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPayOutBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPayOutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.placeMyOrder.setOnClickListener {
            val bottomSheetDialog = congrates_bottom_sheet()
            bottomSheetDialog.show(supportFragmentManager,"Text")
        }

    }
}
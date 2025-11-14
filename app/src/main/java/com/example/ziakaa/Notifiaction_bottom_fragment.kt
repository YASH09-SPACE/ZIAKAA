package com.example.ziakaa

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ziakaa.adapter.NotificationAdapter
import com.example.ziakaa.databinding.FragmentNotifiactionBottomFragmentBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


class Notifiaction_bottom_fragment : BottomSheetDialogFragment(){
    private lateinit var binding: FragmentNotifiactionBottomFragmentBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentNotifiactionBottomFragmentBinding.inflate(layoutInflater,container,false)
        val notification = listOf("Your order has been Canceled Successfully","Order has been taken by the driver","Congrats Your Order Placed")
        val notificationImage = listOf(R.drawable.sademoji,R.drawable.truck,R.drawable.congrats)

        val adapter = NotificationAdapter(
            ArrayList(notification),
            ArrayList(notificationImage)
        )
        binding.notificationRecyclerview.layoutManager = LinearLayoutManager(requireContext())
        binding.notificationRecyclerview.adapter = adapter
        return binding.root
    }

    companion object {

    }
}
package com.example.ziakaa.Fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.ziakaa.R
import com.example.ziakaa.databinding.FragmentProfileBinding
import com.example.ziakaa.model.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        // 1. Load existing user data
        setUserData()

        // 2. Handle Save button click (ID: button5)
        binding.button5.setOnClickListener {
            updateUserData()
        }

        return binding.root
    }

    private fun setUserData() {
        val userId = auth.currentUser?.uid ?: return

        // Reference to the user's profile node
        val userReference = database.reference.child("user").child(userId)

        userReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Check if data exists and populate fields
                val user = snapshot.getValue(UserModel::class.java)
                user?.let {
                    // Using assumed IDs based on required change above
                    binding.nameEt.setText(it.name)
                    binding.addressEt.setText(it.address)
                    binding.phoneEt.setText(it.phone)
                    // Email is typically read-only or from Firebase Auth
                    binding.emailEt.setText(it.email)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Failed to load profile: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateUserData() {
        // Get data from EditText fields using assumed IDs
        val name = binding.nameEt.text.toString().trim()
        val address = binding.addressEt.text.toString().trim()
        val phone = binding.phoneEt.text.toString().trim()
        val email = auth.currentUser?.email ?: binding.emailEt.text.toString().trim() // Default to Auth email

        if (name.isEmpty() || address.isEmpty() || phone.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill Name, Address, and Phone fields.", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = auth.currentUser?.uid ?: return

        // Create the UserModel to save
        val userModel = UserModel(name, email, phone, address)

        database.reference.child("user").child(userId).setValue(userModel)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Profile updated successfully!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to save profile.", Toast.LENGTH_SHORT).show()
            }
    }
}
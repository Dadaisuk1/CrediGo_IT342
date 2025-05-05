package com.sia.credigo.fragments

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.sia.credigo.HomeActivity
import com.sia.credigo.WishlistActivity
import com.sia.credigo.ProfileActivity
import com.sia.credigo.R
import com.sia.credigo.SearchActivity

class NavbarFragment : Fragment() {

    private lateinit var homeNavItem: LinearLayout
    private lateinit var searchNavItem: LinearLayout
    private lateinit var likesNavItem: LinearLayout
    private lateinit var profileNavItem: LinearLayout

    private lateinit var homeIcon: ImageView
    private lateinit var searchIcon: ImageView
    private lateinit var likesIcon: ImageView
    private lateinit var profileIcon: ImageView

    private lateinit var homeText: TextView
    private lateinit var searchText: TextView
    private lateinit var likesText: TextView
    private lateinit var profileText: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_navbar, container, false)

        // Initialize views
        homeNavItem = view.findViewById(R.id.homeNavItem)
        searchNavItem = view.findViewById(R.id.searchNavItem)
        likesNavItem = view.findViewById(R.id.likesNavItem)
        profileNavItem = view.findViewById(R.id.profileNavItem)

        homeIcon = view.findViewById(R.id.homeIcon)
        searchIcon = view.findViewById(R.id.searchIcon)
        likesIcon = view.findViewById(R.id.likesIcon)
        profileIcon = view.findViewById(R.id.profileIcon)

        homeText = view.findViewById(R.id.homeText)
        searchText = view.findViewById(R.id.searchText)
        likesText = view.findViewById(R.id.likesText)
        profileText = view.findViewById(R.id.profileText)

        // Set click listeners
        homeNavItem.setOnClickListener {
            if (activity !is HomeActivity) {
                val intent = Intent(activity, HomeActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                startActivity(intent)
                activity?.finish()
                activity?.overridePendingTransition(0, 0)
            }
        }

        searchNavItem.setOnClickListener {
            if (activity !is SearchActivity) {
                val intent = Intent(activity, SearchActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                startActivity(intent)
                activity?.finish()
                activity?.overridePendingTransition(0, 0)
            }
        }

        likesNavItem.setOnClickListener {
            if (activity !is WishlistActivity) {
                val intent = Intent(activity, WishlistActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                startActivity(intent)
                activity?.finish()
                activity?.overridePendingTransition(0, 0)
            }
        }

        profileNavItem.setOnClickListener {
            if (activity !is ProfileActivity) {
                val intent = Intent(activity, ProfileActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                startActivity(intent)
                activity?.finish()
                activity?.overridePendingTransition(0, 0)
            }
        }

        // Update icons based on current activity
        updateIcons()
        return view
    }

    private fun updateIcons() {
        // Reset all icons to default
        homeIcon.setImageResource(R.drawable.ic_home)
        searchIcon.setImageResource(R.drawable.ic_search)
        likesIcon.setImageResource(R.drawable.ic_heart)
        profileIcon.setImageResource(R.drawable.ic_profile)

        // Reset all text colors to default
        homeText.setTextColor(Color.BLACK)
        searchText.setTextColor(Color.BLACK)
        likesText.setTextColor(Color.BLACK)
        profileText.setTextColor(Color.BLACK)

        // Set the appropriate icon and text color to green based on current activity
        when (activity) {
            is HomeActivity -> {
                homeIcon.setImageResource(R.drawable.ic_home_green)
                homeText.setTextColor(Color.parseColor("#00CA63"))
            }
            is SearchActivity -> {
                searchIcon.setImageResource(R.drawable.ic_search_green)
                searchText.setTextColor(Color.parseColor("#00CA63"))
            }
            is WishlistActivity -> {
                likesIcon.setImageResource(R.drawable.ic_heart_green)
                likesText.setTextColor(Color.parseColor("#00CA63"))
            }
            is ProfileActivity -> {
                profileIcon.setImageResource(R.drawable.ic_profile_green)
                profileText.setTextColor(Color.parseColor("#00CA63"))
            }
        }
    }
}
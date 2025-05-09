package com.sia.credigo.fragments

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
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
import com.sia.credigo.app.CredigoApp
import com.sia.credigo.model.User

class NavbarFragment : Fragment() {
    private val TAG = "NavbarFragment"

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
        Log.d(TAG, "onCreateView")
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
                Log.d(TAG, "Navigating to HomeActivity")
                try {
                    val intent = Intent(activity, HomeActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                    startActivity(intent)
                    // Don't finish the current activity to avoid state loss
                    activity?.overridePendingTransition(0, 0)
                } catch (e: Exception) {
                    Log.e(TAG, "Error navigating to Home: ${e.message}")
                }
            }
        }

        searchNavItem.setOnClickListener {
            if (activity !is SearchActivity) {
                Log.d(TAG, "Navigating to SearchActivity")
                try {
                    val intent = Intent(activity, SearchActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                    startActivity(intent)
                    // Don't finish the current activity to avoid state loss
                    activity?.overridePendingTransition(0, 0)
                } catch (e: Exception) {
                    Log.e(TAG, "Error navigating to Search: ${e.message}")
                }
            }
        }

        likesNavItem.setOnClickListener {
            if (activity !is WishlistActivity) {
                Log.d(TAG, "Navigating to WishlistActivity")
                try {
                    val intent = Intent(activity, WishlistActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                    startActivity(intent)
                    // Don't finish the current activity to avoid state loss
                    activity?.overridePendingTransition(0, 0)
                } catch (e: Exception) {
                    Log.e(TAG, "Error navigating to Wishlist: ${e.message}")
                }
            }
        }

        profileNavItem.setOnClickListener {
            if (activity !is ProfileActivity) {
                Log.d(TAG, "Navigating to ProfileActivity")
                try {
                    // Get the application context and force create test user if needed
                    val app = requireActivity().application as CredigoApp
                    
                    if (!app.sessionManager.isLoggedIn() || app.loggedInuser == null) {
                        Log.d(TAG, "No valid session - creating test user for development")
                        val testUser = User(
                            id = 1,
                            username = "testuser",
                            email = "test@example.com"
                        )
                        app.sessionManager.saveUserData(testUser)
                        app.sessionManager.saveLoginState(1)
                        app.sessionManager.saveAuthToken("test_token")
                        app.loggedInuser = testUser
                        app.isLoggedIn = true
                    }
                    
                    // Verify session is active
                    Log.d(TAG, "User data before navigation: ${app.loggedInuser?.username}, Session active: ${app.sessionManager.isLoggedIn()}")
                    
                    // Create and start activity with user info
                    val intent = Intent(activity, ProfileActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                    startActivity(intent)
                    activity?.overridePendingTransition(0, 0)
                } catch (e: Exception) {
                    Log.e(TAG, "Error navigating to Profile: ${e.message}")
                    e.printStackTrace()
                }
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
        profileIcon.setImageResource(R.drawable.ic_user_linear)

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
                searchIcon.setImageResource(R.drawable.ic_search_pink)
                searchText.setTextColor(Color.parseColor("#00CA63"))
            }
            is WishlistActivity -> {
                likesIcon.setImageResource(R.drawable.ic_heart_fill_pink)
                likesText.setTextColor(Color.parseColor("#00CA63"))
            }
            is ProfileActivity -> {
                profileIcon.setImageResource(R.drawable.ic_user_pink)
                profileText.setTextColor(Color.parseColor("#00CA63"))
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume")
        updateIcons()
    }
}
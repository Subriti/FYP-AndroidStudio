package com.example.notificationpermissions.Fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.notificationpermissions.Adapters.DonationGivenAdapter
import com.example.notificationpermissions.Adapters.DonationRecievedAdapter
import com.example.notificationpermissions.Adapters.PostRecycleAdapter
import com.example.notificationpermissions.Adapters.ReportedPostsAdapter
import com.example.notificationpermissions.R
import com.example.notificationpermissions.Services.PostService
import com.example.notificationpermissions.Services.ReportService
import com.example.notificationpermissions.Services.TransactionService
import com.example.notificationpermissions.Utilities.EXTRA_POST
import org.json.JSONObject

class ReportFragment : Fragment() {

    lateinit var ReportsAdapter: ReportedPostsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_report, container, false)

        val reportsRV = view.findViewById<RecyclerView>(R.id.reportRV)

        val noData= view.findViewById<TextView>(R.id.noReportsTextView)

        ReportService.getReports { complete ->
            println("Get Reports Success: $complete")
            if (complete) {
                if (ReportService.reports.isNotEmpty()) {
                    noData.visibility = View.GONE
                    ReportsAdapter = ReportedPostsAdapter(
                        requireContext().applicationContext,
                        ReportService.reports
                    ){reports ->
                        //open full post details
                        val post= JSONObject(reports.post_id)
                        val postId= post.getString("post_id")
                        PostService.findPost(postId){complete ->
                            if (complete){
                                val secondFragment = AdminViewPostFragment()
                                val bundle = Bundle()
                                bundle.putSerializable(EXTRA_POST, PostService.notificationPost)
                                secondFragment.arguments = bundle

                                // Replace the current fragment with the new instance of the view post fragment
                                activity?.supportFragmentManager?.beginTransaction()?.apply {
                                    replace(R.id.replaceLayout, secondFragment)
                                    addToBackStack(null)
                                    commit()
                                }
                            }
                        }
                    }
                    val layoutManager = LinearLayoutManager(context)
                    reportsRV.layoutManager = layoutManager
                    reportsRV.adapter = ReportsAdapter

                } else if (ReportService.reports.isEmpty()) {
                    noData.visibility = View.VISIBLE
                }

            } else {
                noData.visibility = View.VISIBLE
                noData.text = "Reports could not be loaded"
            }
        }
        return view
    }
}
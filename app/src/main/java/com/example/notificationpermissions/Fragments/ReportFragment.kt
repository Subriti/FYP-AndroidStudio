package com.example.notificationpermissions.Fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.notificationpermissions.Adapters.ReportedPostsAdapter
import com.example.notificationpermissions.Models.Reports
import com.example.notificationpermissions.R
import com.example.notificationpermissions.Services.PostService
import com.example.notificationpermissions.Services.ReportService
import com.example.notificationpermissions.Services.ReportService.getReports
import com.example.notificationpermissions.Utilities.EXTRA_POST
import com.google.android.material.snackbar.Snackbar
import org.json.JSONObject

class ReportFragment : Fragment() {

    lateinit var ReportsAdapter: ReportedPostsAdapter
    lateinit var reportsRV: RecyclerView
    lateinit var noData:TextView
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_report, container, false)

        reportsRV = view.findViewById(R.id.reportRV)

        // on below line we are creating a method to create item touch helper
        // method for adding swipe to delete functionality.
        // in this we are specifying drag direction and position to left
        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                // this method is called when the item is moved.
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                // this method is called when we swipe our item to left direction.
                // on below line we are getting the item at a particular position.
                val selectedReport: Reports = ReportService.reports[viewHolder.adapterPosition]

                // below line is to get the position of the item at that position.
                val position = viewHolder.adapterPosition

                // this method is called when item is swiped.
                // below line is to remove item from our array list.
                ReportService.reports.removeAt(viewHolder.adapterPosition)

                //Mark report as reviewed in the database
                ReportService.reviewReport(selectedReport.report_id, true){complete ->
                    if (complete){
                        // below line is to notify our item is removed from adapter.
                        ReportsAdapter.notifyItemRemoved(viewHolder.adapterPosition)

                        // below line is to display our snackbar with action.
                        Snackbar.make(reportsRV, "Reviewed and Discarded Report " + selectedReport.report_id, Snackbar.LENGTH_LONG)
                            .setAction(
                                "Undo"
                            ) {
                                // adding on click listener to our action of snack bar.
                                // below line is to add our item to array list with a position.
                                ReportService.reports.add(position, selectedReport)

                                //set isReviewed to false in the database as well
                                ReportService.reviewReport(selectedReport.report_id, false){complete ->
                                    if (complete) {
                                        // below line is to notify item is added to our adapter class.
                                        ReportsAdapter.notifyItemInserted(position)
                                    }
                                }
                            }.show()
                    }
                }
            }
            // at last we are adding this to our recycler view.
        }).attachToRecyclerView(reportsRV)

        noData= view.findViewById(R.id.noReportsTextView)

        loadReport()

        val swipeToRefresh= view.findViewById<SwipeRefreshLayout>(R.id.swipeToRefresh)
        swipeToRefresh.setOnRefreshListener {
            loadReport()
            Toast.makeText(requireContext(),"Page Refreshed",Toast.LENGTH_SHORT).show()
            swipeToRefresh.isRefreshing=false
        }
        return view
    }
    private fun loadReport() {
        getReports { complete ->
            println("Get Reports Success: $complete")
            if (complete) {
                if (ReportService.reports.isNotEmpty()) {
                    noData.visibility = View.GONE
                    ReportsAdapter = ReportedPostsAdapter(
                        requireContext().applicationContext,
                        ReportService.reports
                    ) { reports ->
                        //open full post details
                        val post = JSONObject(reports.post_id)
                        val postId = post.getString("post_id")
                        PostService.findPost(postId) { complete ->
                            if (complete) {
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
    }
}
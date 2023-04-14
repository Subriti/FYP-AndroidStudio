package com.example.notificationpermissions.Fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.notificationpermissions.Activities.DashboardActivity
import com.example.notificationpermissions.Adapters.DonationGivenAdapter
import com.example.notificationpermissions.Adapters.DonationRecievedAdapter
import com.example.notificationpermissions.Adapters.NotificationAdapter
import com.example.notificationpermissions.R
import com.example.notificationpermissions.Services.NotificationService
import com.example.notificationpermissions.Services.TransactionService

class HistoryFragment : Fragment(){

    lateinit var GivenAdapter: DonationGivenAdapter
    lateinit var OngoingTransactionsAdapter: DonationGivenAdapter
    lateinit var RecieverAdapter: DonationRecievedAdapter
    lateinit var mContext: Context

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_history, container, false)

        //(activity as DashboardActivity?)!!.currentFragment = this

        val givenRV = view.findViewById<RecyclerView>(R.id.donationGivenRV)
        val recievedRV= view.findViewById<RecyclerView>(R.id.donationRecievedRV)
        val onGoingRV= view.findViewById<RecyclerView>(R.id.ongoingTransactionRV)

        val noDataGiven= view.findViewById<TextView>(R.id.noDataGivenTextView)
        val noDataRecieved= view.findViewById<TextView>(R.id.noDataReceivedTextView)
        val noDataOngoing= view.findViewById<TextView>(R.id.noDataOngoingTextView)

        TransactionService.findGiven { complete ->
            if (complete) {
                if (TransactionService.givenTransaction.isNotEmpty()) {
                    noDataGiven.visibility = View.GONE
                    GivenAdapter = DonationGivenAdapter(
                        //requireContext().applicationContext,
                        mContext,
                        TransactionService.givenTransaction
                    )
                    val layoutManager = LinearLayoutManager(context)
                    givenRV.layoutManager = layoutManager
                    givenRV.adapter = GivenAdapter
                } else if (TransactionService.givenTransaction.isEmpty()) {
                    noDataGiven.visibility = View.VISIBLE
                }
            } else {
                noDataGiven.visibility = View.VISIBLE
                noDataGiven.text = "Donations could not be loaded"
            }
        }

        TransactionService.findRecieved {complete ->
            if (complete) {
                if (TransactionService.recievedTransactions.isNotEmpty()) {
                    noDataRecieved.visibility = View.GONE
                RecieverAdapter = DonationRecievedAdapter(
                    //requireContext().applicationContext,
                    mContext,
                    TransactionService.recievedTransactions
                )
                val layoutManager = LinearLayoutManager(context)
                recievedRV.layoutManager = layoutManager
                recievedRV.adapter = RecieverAdapter
                } else if (TransactionService.recievedTransactions.isEmpty()) {
                    noDataRecieved.visibility = View.VISIBLE
                }
            } else {
                noDataRecieved.visibility = View.VISIBLE
                noDataRecieved.text = "Donations could not be loaded"
            }
        }

        TransactionService.findOngoingTransactions {complete ->
            if (complete) {
                if (TransactionService.onGoingTransactions.isNotEmpty()) {
                    noDataOngoing.visibility = View.GONE
                OngoingTransactionsAdapter = DonationGivenAdapter(
                    //requireActivity().applicationContext,
                    mContext,
                    TransactionService.onGoingTransactions
                )
                val layoutManager = LinearLayoutManager(context)
                onGoingRV.layoutManager = layoutManager
                onGoingRV.adapter = OngoingTransactionsAdapter
            }else if (TransactionService.onGoingTransactions.isEmpty()) {
                noDataOngoing.visibility = View.VISIBLE
            }
        } else {
                noDataOngoing.visibility = View.VISIBLE
                noDataOngoing.text = "Transactions could not be loaded"
        }
        }
        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext= context
    }

    override fun onResume() {
        super.onResume()
        // Invalidate the options menu to force onPrepareOptionsMenu to be called again
        activity?.invalidateOptionsMenu()
    }
}
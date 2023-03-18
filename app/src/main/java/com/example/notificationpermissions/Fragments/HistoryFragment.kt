package com.example.notificationpermissions.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.notificationpermissions.Activities.DashboardActivity
import com.example.notificationpermissions.Adapters.DonationGivenAdapter
import com.example.notificationpermissions.Adapters.DonationRecievedAdapter
import com.example.notificationpermissions.Adapters.NotificationAdapter
import com.example.notificationpermissions.R
import com.example.notificationpermissions.Services.TransactionService

class HistoryFragment : Fragment(){

    lateinit var GivenAdapter: DonationGivenAdapter
    lateinit var OngoingTransactionsAdapter: DonationGivenAdapter
    lateinit var RecieverAdapter: DonationRecievedAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_history, container, false)

        (activity as DashboardActivity?)!!.currentFragment = this

        val givenRV = view.findViewById<RecyclerView>(R.id.donationGivenRV)
        val recievedRV= view.findViewById<RecyclerView>(R.id.donationRecievedRV)
        val onGoingRV= view.findViewById<RecyclerView>(R.id.ongoingTransactionRV)

        TransactionService.findGiven {complete ->
            if (complete) {
                GivenAdapter = DonationGivenAdapter(
                    requireContext().applicationContext,
                    TransactionService.givenTransaction
                )
                val layoutManager = LinearLayoutManager(context)
                givenRV.layoutManager = layoutManager
                givenRV.adapter = GivenAdapter
            }
        }

        TransactionService.findRecieved {complete ->
            if (complete) {
                RecieverAdapter = DonationRecievedAdapter(
                    requireContext().applicationContext,
                    TransactionService.recievedTransactions
                )
                val layoutManager = LinearLayoutManager(context)
                recievedRV.layoutManager = layoutManager
                recievedRV.adapter = RecieverAdapter
            }
        }

        TransactionService.findOngoingTransactions {complete ->
            if (complete) {
                OngoingTransactionsAdapter = DonationGivenAdapter(
                    requireActivity().applicationContext,
                    TransactionService.onGoingTransactions
                )
                val layoutManager = LinearLayoutManager(context)
                onGoingRV.layoutManager = layoutManager
                onGoingRV.adapter = OngoingTransactionsAdapter
            }
        }
        return view
    }
}
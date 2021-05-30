package com.newlibre.btmessenger

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.newlibre.btmessenger.databinding.FragmentFirstBinding


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private var btDeviceSpinner: Spinner? = null
    private var adapter: ArrayAdapter<String>? = null
    var listViewItems = ArrayList<String>()
    var pairedDevices: Set<BluetoothDevice>? = null
    var btAdapter: BluetoothAdapter? = null
    var btHandler: BtHandler? = null

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter =
            activity?.let { ArrayAdapter<String>(it, R.layout.support_simple_spinner_dropdown_item, listViewItems) }
        btDeviceSpinner = view.findViewById(R.id.btDeviceSpinner) as Spinner
        adapter?.add("first");
        adapter?.add("second");
        btDeviceSpinner?.setAdapter(adapter)
        adapter?.notifyDataSetChanged()
        btAdapter = BluetoothAdapter.getDefaultAdapter()
        pairedDevices = GetPairedDevices(btAdapter)

        binding.buttonFirst.setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        }
    }

    private fun GetPairedDevices(btAdapter: BluetoothAdapter?): Set<BluetoothDevice>? {
        val pairedDevices = btAdapter?.bondedDevices
        // If there are paired devices
        Log.i("FirstFrag", "checking paired devices")
        if (pairedDevices != null) {
            Log.i("FirstFrag", "pairedDevices NOT null")
            if (pairedDevices.size > 0) {
                Log.i("FirstFrag", pairedDevices.size.toString())
                for (device in pairedDevices) {
                    // Add the name and address to an array adapter to show in a ListView
                    adapter!!.add(device.name) // + "\n" + device.getAddress());
                }
                adapter!!.notifyDataSetChanged()
            }
        }
        return pairedDevices
    }

    fun DiscoverAvailableDevices(
        adapter: ArrayAdapter<String?>,
        otherDevices: ArrayAdapter<BluetoothDevice?>
    ) {
        val mReceiver: BroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent) {
                val action = intent.action
                // When discovery finds a device
                if (BluetoothDevice.ACTION_FOUND == action) {
                    // Get the BluetoothDevice object from the Intent
                    val device =
                        intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                    // Add the name and address to an array adapter to show in a ListView
                    //btDevice = device;
                    adapter.add(device!!.name) // + "\n" + device.getAddress());
                    otherDevices.add(device)
                    adapter.notifyDataSetChanged()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
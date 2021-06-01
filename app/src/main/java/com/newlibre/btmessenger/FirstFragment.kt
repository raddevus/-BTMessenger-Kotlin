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
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.newlibre.btmessenger.databinding.FragmentFirstBinding
import java.util.*
import kotlin.collections.ArrayList


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    val REQUEST_ENABLE_BT = 1
    private var btDeviceSpinner: Spinner? = null
    private var adapter: ArrayAdapter<String>? = null
    var listViewItems = ArrayList<String>()
    var pairedDevices: Set<BluetoothDevice>? = null
    var btAdapter: BluetoothAdapter? = null
    var btHandler: BtHandler? = null
    var btCurrentDeviceName: String? = null
    private var textToSend: EditText? = null
    private var sendButton: Button? = null
    var btListener : BtHandler? = null
    var runOnce : Boolean = false;
    lateinit var bluetoothService : BluetoothService
    lateinit var currentBtDevice : BluetoothDevice

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
        textToSend = view.findViewById(R.id.textToSend) as EditText
        sendButton = view.findViewById(R.id.sendButton) as Button


        binding.buttonFirst.setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        }

        btDeviceSpinner!!.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                btCurrentDeviceName = btDeviceSpinner?.getSelectedItem().toString();
                //saveDeviceNamePref();
                Log.d("FirstFrag", "DeviceInfo : " + btCurrentDeviceName);
                //logViewAdapter.add("DeviceInfo : " + btCurrentDeviceName);
                //logViewAdapter.notifyDataSetChanged();

            }
       }

        sendButton!!.setOnClickListener(View.OnClickListener {
            Log.i("FirstFrag", btCurrentDeviceName.toString());
            if (btCurrentDeviceName === "") {
                return@OnClickListener
            }

            sendTextViaBT()
            writeData()

        })

        currentBtDevice = pairedDevices?.find { w -> w.name == "twostar" }!!
        Log.i("FirstFrag",currentBtDevice!!.name)
        try {
            bluetoothService = BluetoothService(currentBtDevice)
            bluetoothService.ct.run()
        }
        catch (ex: Exception ){
            Log.i("FirstFrag", ex.message.toString())
        }


        //Log.i("FirstFrag", uuid.toString())




    }

    private fun writeData() {
        val outText = textToSend!!.text.toString()

//        if (pairedDevices!!.size > 0) {
//            for (btItem in pairedDevices!!) {
//                if (btItem != null) {
//                    val name = btItem.name
//                    if (name == btCurrentDeviceName) {
//                        val uuid: UUID = btItem.uuids[0].uuid
//                        Log.i("FirstFrag", uuid.toString())
//                        Log.i("FirstFrag", btCurrentDeviceName!!)
//                        if (btListener == null) {
//                            btListener = BtHandler(btItem, uuid, null)
//                        }
//                        btListener?.run()
//                    }
//                }
//            }
//        }

        Log.i("FirstFrag", "sending text : $outText")

        if (outText != "") {
            btHandler!!.writeMessage(outText)
            if (!runOnce)
            {

                runOnce = true
            }
            try {
                Thread.sleep(200)
                btHandler!!.cancel()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            } finally {
                btHandler = null
            }
        }

    }

    private fun sendTextViaBT() {
        if (btAdapter == null) {
            btAdapter = BluetoothAdapter.getDefaultAdapter()
        }
        if (btAdapter != null) {
            if (!btAdapter!!.isEnabled) {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
            }
        } else {
            Log.d("FirstFrag", "no bt adapter available")
            return  // cannot get btadapter
        }
        if (pairedDevices == null) {
            pairedDevices = btAdapter!!.bondedDevices
        }
        if (pairedDevices!!.size > 0) {
            for (btItem in pairedDevices!!) {
                if (btItem != null) {
                    val name = btItem.name
                    if (name == btCurrentDeviceName) {
                        val uuid: UUID = btItem.uuids[0].uuid
                        Log.i("FirstFrag", uuid.toString())
                        if (btHandler == null) {
                            btHandler = BtHandler(btItem, uuid, null)
                        }
                        btHandler!!.run(btAdapter!!)
                        return
                    }
                }
            }
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
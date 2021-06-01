package com.newlibre.btmessenger

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.util.Log
import android.widget.ArrayAdapter
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*


class BtHandler(device: BluetoothDevice, uuid: UUID?, logViewAdapter: ArrayAdapter<String>?) :
    Thread() {
    private val mmSocket: BluetoothSocket?
    private val mmDevice: BluetoothDevice
    private var logViewAdapter: ArrayAdapter<String>? = null

    //private final InputStream mmInStream;
    private var mmOutStream: OutputStream? = null
    private var mmInStream: InputStream? = null
    private val mmBuffer: ByteArray = ByteArray(1024) // mmBuffer store for the stream


    fun writeYes() {
        try {
            val outByte = byteArrayOf(121)
            mmOutStream!!.write(outByte)
            if (logViewAdapter != null) {
                logViewAdapter?.add("Success; Wrote YES!")
                logViewAdapter?.notifyDataSetChanged()
            }
            sleep(500)
        } catch (e: IOException) {
        } catch (e: InterruptedException) {
            Log.d("MainActivity", e.stackTrace.toString())
        }
    }

    fun writeNo() {
        try {
            val outByte = byteArrayOf(110)
            mmOutStream!!.write(outByte)
            if (logViewAdapter != null) {
                logViewAdapter?.add("Success; Wrote NO")
                logViewAdapter?.notifyDataSetChanged()
            }
            mmOutStream!!.write(outByte)
        } catch (e: IOException) {
        }
    }

    fun writeCtrlAltDel() {
        try {
            val outByte = byteArrayOf(0x01)
            mmOutStream!!.write(outByte)
            if (logViewAdapter != null) {
                logViewAdapter?.add("Success; Wrote 0x01")
                logViewAdapter?.notifyDataSetChanged()
            }
        } catch (e: IOException) {
        }
    }

    fun writeMessage(message: String) {
        try {
            var outByte = ByteArray(message.length)
            outByte = message.toByteArray()
            mmOutStream!!.write(outByte)
            //logViewAdapter?.add("Success; Wrote YES!");
            logViewAdapter?.notifyDataSetChanged()
        } catch (e: IOException) {
        }
    }

    fun run(btAdapter: BluetoothAdapter) {
        // Cancel discovery because it will slow down the connection
        btAdapter.cancelDiscovery()
        try {
            // Connect the device through the socket. This will block
            // until it succeeds or throws an exception
            Log.d("FirstFrag", "Connecting...")
            if (logViewAdapter != null) {
                logViewAdapter?.add("Connecting...")
                logViewAdapter?.notifyDataSetChanged()
            }
            if (!mmSocket!!.isConnected) {
                mmSocket!!.connect()
            }
            Log.d("FirstFrag", "Connected")
            if (logViewAdapter != null) {
                logViewAdapter?.add("Connected")
                logViewAdapter?.notifyDataSetChanged()
            }
            if (mmOutStream != null) {
                if (logViewAdapter != null) {
                    mmOutStream!!.write(byteArrayOf(65, 66))
                    logViewAdapter?.add("Success; Wrote 2 bytes!")
                    logViewAdapter?.notifyDataSetChanged()
                }
            }
        } catch (connectException: IOException) {
            // Unable to connect; close the socket and get out
            Log.d("MainActivity", "Failed! : " + connectException.message)
            if (logViewAdapter != null) {
                logViewAdapter?.add("Failed! : " + connectException.message)
                logViewAdapter?.notifyDataSetChanged()
            }
            try {
                mmSocket!!.close()
            } catch (closeException: IOException) {
            }
            return
        }

        // Do work to manage the connection (in a separate thread)
        //manageConnectedSocket(mmSocket);
    }

    override fun run() {
        var numBytes: Int // bytes returned from read()
        Log.i("FirstFrag","in run()...")
        if (mmSocket?.isConnected!!){
            mmSocket?.connect()
            Log.i("FirstFrag","Connected")
        }
        // Keep listening to the InputStream until an exception occurs.
        while (true) {
            // Read from the InputStream.
            numBytes = try {
                mmInStream!!.read(mmBuffer)
               // Log.i("FirstFrag", "trying...")
            } catch (e: IOException) {
                Log.d("FirstFrag", "Input stream was disconnected", e)
                break
            }

            // Send the obtained bytes to the UI activity.
//                val readMsg = handler.obtainMessage(
//                    MESSAGE_READ, numBytes, -1,
//                    mmBuffer)
//                readMsg.sendToTarget()
            Log.i("FirstFrag", mmBuffer.toString())
        }
    }

    /** Will cancel an in-progress connection, and close the socket  */
    fun cancel() {
        try {
            mmSocket!!.close()
        } catch (e: IOException) {
        }
    }

    init {
        // Use a temporary object that is later assigned to mmSocket,
        // because mmSocket is final
        var tmp: BluetoothSocket? = null
        mmDevice = device
        if (logViewAdapter != null) {
            this.logViewAdapter = logViewAdapter
            logViewAdapter?.add("in ConnectThread()...")
            logViewAdapter?.notifyDataSetChanged()
        }
        val tmpOut: OutputStream
        val tmpIn: InputStream

        // Get a BluetoothSocket to connect with the given BluetoothDevice
        try {
            // MY_UUID is the app's UUID string, also used by the server code
            Log.d("MainActivity", "creating RfcommSocket...")
            if (logViewAdapter != null) {
                logViewAdapter?.add("creating RfcommSocket...")
                logViewAdapter?.notifyDataSetChanged()
            }
            tmp = device.createRfcommSocketToServiceRecord(uuid)
            Log.d("MainActivity", "created.")
            if (logViewAdapter != null) {
                logViewAdapter?.add("created")
                logViewAdapter?.notifyDataSetChanged()
            }
        } catch (e: IOException) {
            Log.d("MainActivity", "FAILED! : " + e.message)
            if (logViewAdapter != null) {
                logViewAdapter?.add("FAILED! : " + e.message)
                logViewAdapter?.notifyDataSetChanged()
            }
        }
        mmSocket = tmp
        try {
            tmpOut = tmp!!.outputStream
            mmOutStream = tmpOut
            tmpIn = mmSocket!!.inputStream
            mmInStream = tmpIn
            //mmInStream = tmp.getInputStream();
        } catch (iox: IOException) {
            Log.d("MainActivity", "failed to get stream : " + iox.message)
        } catch (npe: NullPointerException) {
            Log.d("MainActivity", "null pointer on stream : " + npe.message)
        }
    }
}

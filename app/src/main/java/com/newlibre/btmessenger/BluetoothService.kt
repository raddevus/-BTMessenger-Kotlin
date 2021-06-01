package com.newlibre.btmessenger

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.util.Log
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*

private const val TAG = "FirstFrag"

// Defines several constants used when transmitting messages between the
// service and the UI.
const val MESSAGE_READ: Int = 0
const val MESSAGE_WRITE: Int = 1
const val MESSAGE_TOAST: Int = 2
// ... (Add other message types here as needed.)

class BluetoothService(
    device: BluetoothDevice?
    // handler that gets info from Bluetooth service
) {
    var socket : BluetoothSocket
    var ct : ConnectedThread
   init{
      socket = device?.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"))!!
      if (socket == null){
          Log.i("FirstFrag", "socket is NULL")
      }
       ct = ConnectedThread(socket)

   }

    public inner class ConnectedThread(private val mmSocket: BluetoothSocket) : Thread() {
        private lateinit var mmInStream : InputStream
        private lateinit var mmOutStream: OutputStream
        private lateinit var mmBuffer: ByteArray
        init {
            mmInStream= mmSocket.inputStream
            mmOutStream = mmSocket.outputStream
           mmBuffer  = ByteArray(1024) // mmBuffer store for the stream
        }
        override fun run() {
            Log.i(TAG, "In bt servcie run")
            var numBytes: Int // bytes returned from read()
            if (!socket.isConnected) {
              //  socket.connect()
            }
            // Keep listening to the InputStream until an exception occurs.
            while (true) {
                // Read from the InputStream.
                numBytes = try {
                    Log.i("FirstFrag", (mmBuffer == null).toString())
                    if (mmInStream != null) {
                        mmInStream.read(mmBuffer)
                    }
                    else{
                        0
                    }
                } catch (e: IOException) {
                    Log.i(TAG, "Input stream was disconnected", e)
                    break
                }
                if (numBytes>0) {
                    Log.i("FirstFrag", mmBuffer.toString())
                }
                // Send the obtained bytes to the UI activity.
//                val readMsg = handler.obtainMessage(
//                    MESSAGE_READ, numBytes, -1,
//                    mmBuffer)
//                readMsg.sendToTarget()
                //Log.i(TAG,"couldn't do a thing")
            }
        }

        // Call this from the main activity to send data to the remote device.
        fun write(bytes: ByteArray) {
            try {
                mmOutStream.write(bytes)
            } catch (e: IOException) {
                Log.e(TAG, "Error occurred when sending data", e)

                // Send a failure message back to the activity.
//                val writeErrorMsg = handler.obtainMessage(MESSAGE_TOAST)
//                val bundle = Bundle().apply {
//                    putString("toast", "Couldn't send data to the other device")
//                }
//                writeErrorMsg.data = bundle
//                handler.sendMessage(writeErrorMsg)
                Log.i(TAG,"Error - " + e.message)
                return
            }

            // Share the sent message with the UI activity.
//            val writtenMsg = handler.obtainMessage(
//                MESSAGE_WRITE, -1, -1, mmBuffer)
//            writtenMsg.sendToTarget()
            Log.i(TAG, "couldn't do another thing 2222")
        }

        // Call this method from the main activity to shut down the connection.
        fun cancel() {
            try {
                mmSocket.close()
            } catch (e: IOException) {
                Log.e(TAG, "Could not close the connect socket", e)
            }
        }
    }
}

package br.com.interativo.friotooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothAdapter.STATE_CONNECTING
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.ContentValues
import android.nfc.Tag
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.ParcelUuid
import android.util.Log
import br.com.interativo.friotooth.databinding.ActivityMainBinding
import kotlinx.android.synthetic.main.activity_main.*
import java.io.IOException
import java.util.*


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    val uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    var mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

    interface onMessageListener{
        fun onMessage(str : String)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_main
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)


       // var device = Set<BluetoothDevice>

        binding.btnSave.setOnClickListener {


            //binding.labelData.setText(readFriotooth(socket))
            val mDevice = txtMac.text.toString()
            val conn = ConnectThread(mBluetoothAdapter.getRemoteDevice(mDevice), object : onMessageListener{
                override fun onMessage(str: String) {
                    binding.labelData.append(str + "\n")
                }

            })

            conn.start()
        }

    }

    private inner class ConnectThread(val device: BluetoothDevice, val listener:onMessageListener) : Thread() {



        private val mmSocket: BluetoothSocket? by lazy(LazyThreadSafetyMode.NONE) {
            device.createRfcommSocketToServiceRecord(uuid)
        }

        public override fun run() {

            mBluetoothAdapter?.cancelDiscovery()

            val clientSocket = device.createInsecureRfcommSocketToServiceRecord(uuid)
            device.createBond()
            device.describeContents()
            device.fetchUuidsWithSdp()
            sleep(500)

            mmSocket?.use { socket ->
                socket.connect()

                readFriotooth(socket, listener)
            }
        }

    }

    private fun readFriotooth(bluetoothSocket: BluetoothSocket,   listener:onMessageListener) {
        Log.i(ContentValues.TAG, Thread.currentThread().name)
        val bluetoothSocketInputStream = bluetoothSocket.inputStream
        val buffer = ByteArray(1024)
        var bytes: Int

        while (true) {
            try {
                bytes = bluetoothSocketInputStream.read(buffer)
                val readMessage = String(buffer, 0, bytes)
                val dataRep = readMessage

                this@MainActivity.runOnUiThread {
                    listener.onMessage(dataRep)
                }




            } catch (e: IOException) {
                e.printStackTrace()

                break
            }
        }
    }
}
package com.example.bluetoothelevator

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattCharacteristic
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_call.*
import org.jetbrains.anko.alert
import timber.log.Timber
import java.io.IOException

class CallActivity : AppCompatActivity() {
    lateinit var device: BluetoothDevice
    override fun onSupportNavigateUp(): Boolean {
        disconnectDialog()
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_call)
        device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)!!
        callElevator(device, ScanActivity.bluetoothGatt?.getService(ScanActivity.elevatorServiceUUID)
            ?.getCharacteristic(ScanActivity.elevatorCharacteristicUUID)!!
        )
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowTitleEnabled(true)
            title = "BLE Elevator"
        }
    }

    fun callElevator(device: BluetoothDevice,characteristic: BluetoothGattCharacteristic) {
        elevatorcall.setOnClickListener { writeCharacteristic(device,characteristic,5000) }
    }

    fun BluetoothGattCharacteristic.isWritable(): Boolean =
        containsProperty(BluetoothGattCharacteristic.PROPERTY_WRITE)

    fun BluetoothGattCharacteristic.isWritableWithoutResponse(): Boolean =
        containsProperty(BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE)

    fun BluetoothGattCharacteristic.containsProperty(property: Int): Boolean {
        return properties and property != 0
    }

    fun writeCharacteristic(
        device: BluetoothDevice,
        characteristic: BluetoothGattCharacteristic,
        payload: Int
    ) {
        try {
            val writeType = when {
                characteristic.isWritable() -> BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
                characteristic.isWritableWithoutResponse() -> {
                    BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
                }
                else -> error("Characteristic ${characteristic.uuid} cannot be written to")
            }

            ScanActivity.bluetoothGatt?.let { gatt ->
                if (device == ScanActivity.bluetoothGatt!!.device)
                    characteristic.writeType = writeType
                characteristic.setValue(payload, BluetoothGattCharacteristic.FORMAT_UINT32, 0)
                gatt.writeCharacteristic(characteristic)


            } ?: error("Not connected to a BLE device!")
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
    fun disconnectDialog(){
        runOnUiThread {
            alert {
                title = "disconnected"
                message = "please resart app and try again"
                disconnectGatt()
                positiveButton("OK") {
                    onBackPressed()
                }
            }.show()
        }
    }
    private fun disconnectGatt() {
        ScanActivity.bluetoothGatt?.disconnect()
        ScanActivity.bluetoothGatt?.close()
        Timber.w("disconnected")
    }
}
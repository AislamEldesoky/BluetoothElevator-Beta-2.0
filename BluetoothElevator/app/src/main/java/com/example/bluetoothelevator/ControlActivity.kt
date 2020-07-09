package com.example.bluetoothelevator

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_control.*
import org.jetbrains.anko.alert
import timber.log.Timber
import java.io.IOException

class ControlActivity : AppCompatActivity() {
    private val bluetoothGatt: BluetoothGatt? = null
    lateinit var device: BluetoothDevice
    var isDisconnected = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_control)
        device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)!!
        updateTextView(
            device,
            ScanActivity.bluetoothGatt?.getService(ScanActivity.elevatorServiceUUID)
                ?.getCharacteristic(ScanActivity.elevatorCharacteristicUUID)!!
        )
        countToDisconnect(ScanActivity.bluetoothGatt)

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowTitleEnabled(true)
            title = "Elevator Cabinet"
        }
    }

    fun updateTextView(device: BluetoothDevice, characteristic: BluetoothGattCharacteristic) {

        one.setOnClickListener { numtext.setText(numtext.text.toString() + "1") }
        two.setOnClickListener { numtext.setText(numtext.text.toString() + "2") }
        three.setOnClickListener { numtext.setText(numtext.text.toString() + "3") }
        four.setOnClickListener { numtext.setText(numtext.text.toString() + "4") }
        five.setOnClickListener { numtext.setText(numtext.text.toString() + "5") }
        six.setOnClickListener { numtext.setText(numtext.text.toString() + "6") }
        seven.setOnClickListener { numtext.setText(numtext.text.toString() + "7") }
        eight.setOnClickListener { numtext.setText(numtext.text.toString() + "8") }
        nine.setOnClickListener { numtext.setText(numtext.text.toString() + "9") }
        ground.setOnClickListener { numtext.setText(numtext.text.toString() + "0") }
        send.setOnClickListener {

            writeCharacteristic(device, characteristic, numtext.text.toString().toInt())
            numtext.setText("")
        }
        clear.setOnClickListener { numtext.setText("") }
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

    private fun countToDisconnect(device: BluetoothGatt?) {
        val timer = object : CountDownTimer(8000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                Timber.w("countdown : " + millisUntilFinished / 1000)
            }

            override fun onFinish() {
                try {
                    if (isDisconnected == false) {
                        runOnUiThread {
                            alert {
                                title = "disconnected"
                                message = "please resart app and try again"
                                disconnectGatt()
                                isDisconnected = true
                                positiveButton("OK") {
                                    onBackPressed()
                                }
                            }.show()
                        }
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }
        }
        timer.start()

    }

    private fun disconnectGatt() {
        ScanActivity.bluetoothGatt?.disconnect()
        ScanActivity.bluetoothGatt?.close()
        Timber.w("disconnected")
    }

    override fun onSupportNavigateUp(): Boolean {
        disconnectDialog()
        return true
    }

    fun disconnectDialog() {
        if(isDisconnected == false) {
            runOnUiThread {
                alert {
                    title = "disconnected"
                    message = "please resart app and try again"
                    disconnectGatt()
                    isDisconnected = true
                    positiveButton("OK") {
                        onBackPressed()
                    }
                }.show()
            }
        }
    }
}



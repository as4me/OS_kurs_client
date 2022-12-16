package ru.mtuci.os_kurs_client

import android.annotation.SuppressLint
import android.app.Person
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.beust.klaxon.Json
import com.beust.klaxon.Klaxon
import com.jayway.jsonpath.JsonPath
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI

class MainActivity : AppCompatActivity() {

    private var socket1: WebSocketClient? = null
    private var socket2: WebSocketClient? = null
    lateinit var  s1: TextView
    lateinit var s2: TextView
    private var state1: Boolean = false
    private var state2: Boolean = false

    lateinit var t1: TextView
    private var handler1: Handler = Handler()
    private var handler2: Handler = Handler()
    lateinit var t2: TextView
    lateinit var t3: TextView
    lateinit var t4: TextView
    lateinit var t5: TextView

    var cur = arrayOf("Байты", "Мегабайты", "Гигабайты")

    private var cur_n = 1

    private val runnableCode: Runnable = object : Runnable {
        override fun run() {
            socket1?.send("cpu")
            socket1?.send("disk")
            handler1.postDelayed(this, 2000)
        }
    }

    private val runnableCode2: Runnable = object : Runnable {
        override fun run() {
            socket2?.send("pm")
            socket2?.send("free")
            handler2.postDelayed(this, 2000)
        }
    }

    @SuppressLint("MissingInflatedId", "WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val c1: Button = findViewById(R.id.conn_1)
        val c2: Button = findViewById(R.id.conn_2)
        val i1: EditText = findViewById(R.id.i_1)
        val i2: EditText = findViewById(R.id.i_2)
        val d1: Button = findViewById(R.id.diss_1)
        val d2: Button = findViewById(R.id.diss_2)
        s1 = findViewById(R.id.s_1)
        s2 = findViewById(R.id.s_2)
        t1 = findViewById(R.id.t_1)
        t2 = findViewById(R.id.t_2)
        t3 = findViewById(R.id.t_3)
        t4 = findViewById(R.id.t_4)
        t5 = findViewById(R.id.t_5)

        val spinner: Spinner = findViewById(R.id.spinn_2)


        if (spinner != null) {
            val adapter = ArrayAdapter(
                this,
                android.R.layout.simple_spinner_item, cur
            )
            spinner.adapter = adapter

            spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    Toast.makeText(this@MainActivity, cur[p2] + "", Toast.LENGTH_SHORT).show()
                    if (p2 == 0) {
                        cur_n = 1
                    }
                    if (p2 == 1) {
                        cur_n = 2

                    }
                    if (p2 == 2) {
                        cur_n = 3
                    }
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                    TODO("Not yet implemented")
                }
            }
        }
        c1.setOnClickListener {
            if (!i1.text.toString().equals("")) {
                connect(i1.text.toString(), 1)
            }

        }
        c2.setOnClickListener {
            if (!i2.text.toString().equals("")) {
                connect(i2.text.toString(), 2)
            }
        }
        d1.setOnClickListener { disconnect(1) }
        d2.setOnClickListener { disconnect(2
        )
        Log.d("APP","2")}


    }
    fun connect(url:String, socketClient: Int) {
        if (socketClient == 1){
            socket1?.close()
            val uri = URI("ws://" + url)
            socket1 = SocketClient(uri,socketClient)
            socket1!!.connectBlocking()
            socket1?.send("init")
        }else{
            socket2?.close()
            val uri = URI("ws://" + url)
            socket2 = SocketClient(uri,socketClient)
            socket2!!.connectBlocking()
            socket2?.send("init")
        }

    }

    fun disconnect(socketClient: Int) {
        if (socketClient == 1) {
            socket1?.close()
            socket1 = null
        }
        if (socketClient == 2){
            socket2?.close()
            socket2 = null
        }

    }



    fun updateView(id: Int,type: String,message: String){
        if (id == 1 && type.equals("status") && message.equals("connected")){
            s1.setText("Статус: подключен")
        }
        if (id == 1 && type.equals("status") && message.equals("disconnected")){
            s1.setText("Статус: не подключен")
        }

        if (id == 2 && type.equals("status") && message.equals("connected")){
            s2.setText("Статус: подключен")
        }
        if (id == 2 && type.equals("status") && message.equals("disconnected")){
            s2.setText("Статус: не подключен")
        }
        if (id == 1 && type.equals("disk_c")){
            t1.setText("Кол-во жестких дисков: " + message.toString())
        }
        if (id == 1 && type.equals("disk")){
            t2.setText("Информация о дисках подробно:\n" + message.toString())
        }
        if (id == 1 && type.equals("cpu")){
            t3.setText("Кол-во логических процессоров:\n" + message.toString())
        }
        if (id == 2 && type.equals("pm")){
            if(cur_n == 1){
                t4.setText("Обьем физической памяти:\n" + message.toString() + " байта")
            }
            if (cur_n == 2){
                t4.setText("Обьем физической памяти:\n" + (message.toFloat() / 1024 / 1024) +" мегабайта")

            }
            if (cur_n == 3){
                t4.setText("Обьем физической памяти:\n" + (message.toFloat() / 1024 / 1024 / 1024) + " гигабайта")
            }

        }
        if (id == 2 && type.equals("free")){
            if(cur_n == 1){
                t5.setText("Обьем свободной физической памяти:\n" +  (message.toFloat()) + " байта")
            }
            if (cur_n == 2){
                t5.setText("Обьем свободной физической памяти:\n" + (message.toFloat() / 1024 / 1024 ) +  " мегабайта")
            }
            if (cur_n == 3){
                t5.setText("Обьем свободной физической памяти:\n" + (message.toFloat() / 1024 / 1024 / 1024) + " гигабайта")
            }

        }

    }

    inner class SocketClient(uri: URI,socketClient: Int) : WebSocketClient(uri) {
        var socketClientt: Int = -1
        init {
            socketClientt = socketClient
        }
        override fun onOpen(handshakedata: ServerHandshake?) {
            runOnUiThread {
                Log.d("APP","open: " + socketClientt.toString())
                Log.d("APP",handshakedata?.httpStatus.toString())


                if (socketClientt == 1){
                    updateView(1,"status","connected")
                    handler1.post(runnableCode)
                }
                if (socketClientt == 2){
                    updateView(2,"status","connected")
                    handler2.post(runnableCode2)
                }

            }
        }

        override fun onClose(code: Int, reason: String?, remote: Boolean) {
            runOnUiThread {
                if (socketClientt == 1) {
                    // Removes pending code execution
                    handler1.removeCallbacks(runnableCode);
                    updateView(1,"status","disconnected")
                }
                if (socketClientt == 2) {
                    // Removes pending code execution
                    handler2.removeCallbacks(runnableCode2);
                    updateView(2,"status","disconnected")
                }
                Log.d("APP","close")
                Log.d("APP",code.toString() + remote.toString() + reason.toString())

            }
        }

        override fun onMessage(message: String?) {
            runOnUiThread {


                val context = JsonPath.parse(message.toString())
                println(message.toString())
                try {
                    val user_id = context.read<String>("user_id")
                    val disk_c = context.read<String>("disk_c")
                    val disk = context.read<String>("disk")


                    if (user_id.equals("1")){
                        if (!disk_c.none()){
                            updateView(socketClientt,"disk_c",disk_c)
                        }
                        if (!disk.none()){
                            updateView(socketClientt,"disk",disk)
                        }
                    }


                }catch (e: java.lang.Exception){
                    println()
                }
                try {
                    val user_id = context.read<String>("user_id")
                    val cpu = context.read<String>("cpu")

                    if (user_id.equals("1")){
                        if (!cpu.isNullOrEmpty()){
                            updateView(socketClientt,"cpu",cpu)
                        }
                    }
                }
                catch (e: java.lang.Exception){
                    println()
                }
                try {
                    val user_id = context.read<String>("user_id")
                    val pm = context.read<String>("pm")

                    if (user_id.equals("2")){
                        if (!pm.isNullOrEmpty()){
                            updateView(socketClientt,"pm",pm)
                        }
                    }
                }
                catch (e: java.lang.Exception){
                    println()
                }

                try {
                    val user_id = context.read<String>("user_id")
                    val free = context.read<String>("free")

                    if (user_id.equals("2")){
                        if (!free.isNullOrEmpty()){
                            updateView(socketClientt,"free",free)
                        }
                    }
                }
                catch (e: java.lang.Exception){
                    println()
                }


            }
        }

        override fun onError(ex: Exception?) {
            runOnUiThread {
                Log.d("E",ex.toString())
            }
        }

    }
    class Data(val user_id: String, val disk_c: String,val disk: String)

}



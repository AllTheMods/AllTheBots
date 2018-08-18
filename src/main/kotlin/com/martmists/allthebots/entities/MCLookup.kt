package com.martmists.allthebots.entities


import java.io.*
import java.net.*

class MCLookup(val address: String, val port: Int = 25565, val timeout: Int = DEFAULT_TIMEOUT) {
    var isServerUp: Boolean = false
    var motd: String? = null
    var version: String? = null
    var currentPlayers: String? = null
    var maximumPlayers: String? = null

    init {
        refresh()
    }

    /**
     * Refresh state of the server
     * @return `true`; `false` if the server is down
     */
    fun refresh(): Boolean {
        val serverData: Array<String>?
        val rawServerData: String?
        try {
            //Socket clientSocket = new Socket(getAddress(), getPort());
            val clientSocket = Socket()
            clientSocket.connect(InetSocketAddress(address, port), timeout)
            val dos = DataOutputStream(clientSocket.getOutputStream())
            val br = BufferedReader(InputStreamReader(clientSocket.getInputStream()))
            val payload = byteArrayOf(0xFE.toByte(), 0x01.toByte()) + byteArrayOf(0x00.toByte(),0x00.toByte(),0x00.toByte(),0x00.toByte())
            //dos.writeBytes("\u00FE\u0001");
            dos.write(payload, 0, payload.size)
            rawServerData = br.readLine()
            clientSocket.close()
        } catch (e: Exception) {
            isServerUp = false
            //e.printStackTrace();
            return isServerUp
        }

        if (rawServerData == null)
            isServerUp = false
        else {
            serverData = rawServerData.split("\u0000\u0000\u0000".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            serverData.forEach {
                println(it)
            }
            if (serverData.size >= NUM_FIELDS) {
                isServerUp = true
                version = serverData[2].replace("\u0000", "")
                motd = serverData[3].replace("\u0000", "")
                currentPlayers = serverData[4].replace("\u0000", "")
                maximumPlayers = serverData[5].replace("\u0000", "")
            } else
                isServerUp = false
        }
        return isServerUp
    }

    companion object {
        val NUM_FIELDS: Byte = 6        // expected number of fields returned from server after query
        val DEFAULT_TIMEOUT = 7000 // default TCP socket connection timeout in milliseconds
    }
}
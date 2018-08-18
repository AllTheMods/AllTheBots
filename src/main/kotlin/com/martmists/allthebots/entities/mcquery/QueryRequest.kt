package com.martmists.allthebots.entities.mcquery


import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.io.IOException

class QueryRequest {
    lateinit var byteStream: ByteArrayOutputStream
    lateinit var dataStream: DataOutputStream
    internal var type: Byte = 0
    internal var sessionID: Int = 0
    lateinit var payload: ByteArray

    constructor() {
        val size = 1460
        byteStream = ByteArrayOutputStream(size)
        dataStream = DataOutputStream(byteStream)
    }

    constructor(type: Byte) {
        this.type = type
        //TODO move static type variables to Request
    }

    //convert the data in this request to a byte array to send to the server
    internal fun toBytes(): ByteArray {
        byteStream.reset()

        try {
            dataStream.write(MAGIC)
            dataStream.write(type.toInt())
            dataStream.writeInt(sessionID)
            dataStream.write(payloadBytes())
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return byteStream.toByteArray()
    }

    private fun payloadBytes(): ByteArray {
        return if (type == MCQuery.HANDSHAKE) {
            byteArrayOf() //return empty byte array
        } else
        //(type == mcquery.STAT)
        {
            payload
        }
    }

    fun setPayload(load: Int) {
        this.payload = ByteUtils.intToBytes(load)
    }

    companion object {

        internal var MAGIC = byteArrayOf(0xFE.toByte(), 0xFD.toByte())
    }
}
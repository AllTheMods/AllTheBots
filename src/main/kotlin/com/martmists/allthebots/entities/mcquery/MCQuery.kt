package com.martmists.allthebots.entities.mcquery

import java.net.*

/**
 * A class that handles Minecraft Query protocol requests
 *
 * Original source: https://github.com/rmmccann/Minecraft-Status-Query
 *
 * @author Ryan McCann
 */
class MCQuery(private val serverAddress: String, private val queryPort: Int = 25565) {
    private var localPort = 25565
    private var socket: DatagramSocket? = null //prevent socket already bound exception
    private var token: Int = 0

    // used to get a session token
    private fun handshake() {
        val req = QueryRequest()
        req.type = HANDSHAKE
        req.sessionID = generateSessionID()

        val `val` = 11 - req.toBytes().size //should be 11 bytes total
        val input = ByteUtils.padArrayEnd(req.toBytes(), `val`)
        val result = sendUDP(input)

        token = Integer.parseInt(String(result!!).trim { it <= ' ' })
    }

    /**
     * Use this to get basic status information from the server.
     * @return a `QueryResponse` object
     */
    fun basicStat(): QueryResponse {
        handshake() //get the session token first

        val req = QueryRequest() //create a request
        req.type = STAT
        req.sessionID = generateSessionID()
        req.setPayload(token)
        val send = req.toBytes()

        val result = sendUDP(send)

        return QueryResponse(result!!, false)
    }

    /**
     * Use this to get more information, including players, from the server.
     * @return a `QueryResponse` object
     */
    fun fullStat(): QueryResponse {
        //		basicStat() calls handshake()
        //		QueryResponse basicResp = this.basicStat();
        //		int numPlayers = basicResp.onlinePlayers; //TODO use to determine max length of full stat

        handshake()

        val req = QueryRequest()
        req.type = STAT
        req.sessionID = generateSessionID()
        req.setPayload(token)
        req.payload = ByteUtils.padArrayEnd(req.payload, 4) //for full stat, pad the payload with 4 null bytes

        val send = req.toBytes()

        val result = sendUDP(send)

        /*
		 * note: buffer size = base + #players(online) * 16(max username length)
		 */

        return QueryResponse(result!!, true)
    }

    private fun sendUDP(input: ByteArray): ByteArray? {
        try {
            while (socket == null) {
                try {
                    socket = DatagramSocket(localPort) //create the socket
                } catch (e: BindException) {
                    ++localPort // increment if port is already in use
                }

            }

            //create a packet from the input data and send it on the socket
            val address = InetAddress.getByName(serverAddress) //create InetAddress object from the address
            val packet1 = DatagramPacket(input, input.size, address, queryPort)
            socket!!.send(packet1)
            socket!!.soTimeout = 60_000

            //receive a response in a new packet
            val out = ByteArray(1024) //TODO guess at max size
            val packet = DatagramPacket(out, out.size)
            socket!!.soTimeout = 500 //one half second timeout
            socket!!.receive(packet)

            return packet.data
        } catch (e: SocketException) {
            e.printStackTrace()
        } catch (e: SocketTimeoutException) {
            System.err.println("Socket Timeout! Is the server offline?")
            //System.exit(1);
            // throw exception
        } catch (e: UnknownHostException) {
            System.err.println("Unknown host!")
            e.printStackTrace()
            //System.exit(1);
            // throw exception
        } catch (e: Exception) //any other exceptions that may occur
        {
            e.printStackTrace()
        }

        return null
    }

    private fun generateSessionID(): Int {
        /*
		 * Can be anything, so we'll just use 1 for now. Apparently it can be omitted altogether.
		 * TODO: increment each time, or use a random int
		 */
        return 1
    }

    fun finalize() {
        socket!!.close()
    }

    companion object {
        internal val HANDSHAKE: Byte = 9
        internal val STAT: Byte = 0

        //debug
        internal fun printBytes(arr: ByteArray) {
            for (b in arr) print(b.toString() + " ")
            println()
        }

        internal fun printHex(arr: ByteArray) {
            println(toHex(arr))
        }

        internal fun toHex(b: ByteArray): String {
            var out = ""
            for (bb in b) {
                out += String.format("%02X ", bb)
            }
            return out
        }
    }
}
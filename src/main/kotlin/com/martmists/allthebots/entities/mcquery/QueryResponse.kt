package com.martmists.allthebots.entities.mcquery


import java.util.ArrayList

class QueryResponse(oData: ByteArray, private val fullstat: Boolean) {

    //for simple stat
    /**
     * @return the MOTD, as displayed in the client
     */
    var motd: String? = null
        private set
    var gameMode: String? = null
        private set
    var mapName: String? = null
        private set
    var onlinePlayers: Int = 0
        private set
    var maxPlayers: Int = 0
        private set
    private var port: Short = 0
    private var hostname: String? = null

    //for full stat only
    private var gameID: String? = null
    private var version: String? = null
    private var plugins: String? = null
    /**
     * Returns an `ArrayList` of strings containing the connected players' usernames.
     * Note that this will return null for basic status requests.
     * @return An `ArrayList` of player names
     */
    var playerList: ArrayList<String>? = null
        private set

    init {
        var data = oData

        data = ByteUtils.trim(data)
        val temp = ByteUtils.split(data)

        //		if(temp.length == 6) //short stat
        if (!fullstat) {
            motd = String(ByteUtils.subarray(temp[0], 1, temp[0].size - 1))
            gameMode = String(temp[1])
            mapName = String(temp[2])
            onlinePlayers = Integer.parseInt(String(temp[3]))
            maxPlayers = Integer.parseInt(String(temp[4]))
            port = ByteUtils.bytesToShort(temp[5])
            hostname = String(ByteUtils.subarray(temp[5], 2, temp[5].size - 1))
        } else
        //full stat
        {
            motd = String(temp[3])
            gameMode = String(temp[5])
            mapName = String(temp[13])
            onlinePlayers = Integer.parseInt(String(temp[15]))
            maxPlayers = Integer.parseInt(String(temp[17]))
            port = java.lang.Short.parseShort(String(temp[19]))
            hostname = String(temp[21])

            //only available with full stat:
            gameID = String(temp[7])
            version = String(temp[9])
            plugins = String(temp[11])

            playerList = ArrayList()
            for (i in 25 until temp.size) {
                playerList!!.add(String(temp[i]))
            }
        }
    }

    /**
     * Returns a JSON string representation of the data returned from the server, useful for JSP/servlet pages with javascript.
     * @return a JSON string
     */
    fun asJSON(): String {
        val json = StringBuilder()
        json.append("\'{")
        json.append("\"motd\":")                            // "motd":
        json.append('"').append(motd).append("\",")        // "A Minecraft Server",

        json.append("\"gamemode\":")                        // "gamemode":
        json.append('"').append(gameMode).append("\",")    // "SMP",

        json.append("\"map\":")                            // "map":
        json.append('"').append(mapName).append("\",")        // "world1",

        json.append("\"onlinePlayers\":")                    // "onlinePlayers":
        json.append(onlinePlayers).append(',')                // 0,

        json.append("\"maxPlayers\":")                        // "maxPlayers":
        json.append(maxPlayers).append(',')                // 20,

        json.append("\"port\":")                            // "port":
        json.append(port.toInt()).append(',')                        // 25565,

        json.append("\"host\":")                            // "hostname":
        json.append('"').append(hostname).append('"')        // "0.0.0.0",

        if (fullstat) {
            json.append(',')
            json.append("\"gameID\":")                        // "gameID":
            json.append('"').append(gameID).append("\",")    // "MINECRAFT",

            json.append("\"version\":")                    // "version":
            json.append('"').append(version).append("\",")    // "1.2.5",

            json.append("\"players\":")
            json.append('[')
            for (player in playerList!!) {
                json.append("\"" + player + "\"")
                if (playerList!!.indexOf(player) != playerList!!.size - 1) {
                    json.append(',')
                }
            }
            json.append(']')
        }

        json.append("}\'")

        return json.toString()
    }

    override fun toString(): String {
        val delimiter = ", "
        val str = StringBuilder()
        str.append(motd)
        str.append(delimiter)
        str.append(gameMode)
        str.append(delimiter)
        str.append(mapName)
        str.append(delimiter)
        str.append(onlinePlayers)
        str.append(delimiter)
        str.append(maxPlayers)
        str.append(delimiter)
        str.append(port.toInt())
        str.append(delimiter)
        str.append(hostname)

        if (fullstat) {
            str.append(delimiter)
            str.append(gameID)
            str.append(delimiter)
            str.append(version)

            //plugins for non-vanilla (eg. Bukkit) servers
            if (plugins!!.length > 0) {
                str.append(delimiter)
                str.append(plugins)
            }

            // player list
            str.append(delimiter)
            str.append("Players: ")
            str.append('[')
            for (player in playerList!!) {
                str.append(player)
                if (playerList!!.indexOf(player) != playerList!!.size - 1) {
                    str.append(',')
                }
            }
            str.append(']')
        }

        return str.toString()
    }

    companion object {
        internal var NULL: Byte = 0
        internal var SPACE: Byte = 20
    }

    //TODO getPlayers return hashmap/array/arraylist
}
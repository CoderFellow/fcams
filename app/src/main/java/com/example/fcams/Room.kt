package com.example.fcams

/**
 * Data class representing a physical or virtual room available in the system.
 *
 * @property roomID Unique identifier for the room.
 * @property roomName Display name or label of the room.
 * @property capacity Maximum capacity or occupant limit of the room.
 */
data class Room(
    val roomID: String = "",
    val roomName: String = "",
    val capacity: String = ""
)
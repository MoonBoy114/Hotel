package com.example.hotel


import android.app.Application
import io.appwrite.Client
import io.appwrite.services.Account
import io.appwrite.services.Databases
import io.appwrite.services.Storage


class HotelApp : Application() {
    companion object {
        lateinit var client: Client
            private set
        lateinit var databases: Databases
            private set
        lateinit var storage: Storage
            private set
        lateinit var projectId: String
            private set
    }

    override fun onCreate() {
        super.onCreate()

        projectId = "67f3ec48002c243abde7"
        client = Client(this)
            .setEndpoint("https://cloud.appwrite.io/v1")
            .setProject(projectId)
            .setSelfSigned(true)

        databases = Databases(client)
        storage = Storage(client)
    }
}
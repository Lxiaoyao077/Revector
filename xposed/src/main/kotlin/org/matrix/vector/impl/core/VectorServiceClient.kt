package org.matrix.vector.impl.core

import android.os.IBinder
import android.os.ParcelFileDescriptor
import org.matrix.vector.ipc.LoadedModule
import org.matrix.vector.ipc.IProcessChannel
import org.matrix.vector.ipc.IFrameworkService

/**
 * Singleton client for managing IPC communication with the injected manager service. Handles Binder
 * death gracefully and ensures safe remote execution.
 */
object VectorServiceClient : IFrameworkService, IBinder.DeathRecipient {

    private var service: IFrameworkService? = null
    var processName: String = ""
        private set

    @Synchronized
    fun init(appService: IFrameworkService?, niceName: String) {
        val binder = appService?.asBinder()
        if (service == null && binder != null) {
            runCatching {
                    service = appService
                    processName = niceName
                    binder.linkToDeath(this, 0)
                }
                .onFailure { service = null }

            // Handed over here rather than after module loading, and carrying no module identity:
            // system_server loads its modules before the daemon's module cache exists, so anything
            // that had to name a module here could not work for it.
            service?.let {
                try {
                    it.attachProcessChannel(VectorProcessChannel)
                } catch (_: Throwable) {
                }
            }
        }
    }

    override fun attachProcessChannel(channel: IProcessChannel?) {
        try {
            service?.attachProcessChannel(channel)
        } catch (_: Throwable) {
        }
    }

    override fun getLegacyModules(): List<LoadedModule> {
        return runCatching { service?.legacyModules }.getOrNull() ?: emptyList()
    }

    override fun getModules(): List<LoadedModule> {
        return runCatching { service?.modules }.getOrNull() ?: emptyList()
    }

    override fun getPrefsPath(packageName: String): String? {
        return runCatching { service?.getPrefsPath(packageName) }.getOrNull()
    }

    override fun openManagerApk(): ParcelFileDescriptor? {
        return runCatching { service?.openManagerApk() }.getOrNull()
    }

    override fun requestManagerService(): IBinder? {
        return runCatching { service?.requestManagerService() }.getOrNull()
    }

    override fun asBinder(): IBinder? {
        return service?.asBinder()
    }

    override fun binderDied() {
        service?.asBinder()?.unlinkToDeath(this, 0)
        service = null
    }
}

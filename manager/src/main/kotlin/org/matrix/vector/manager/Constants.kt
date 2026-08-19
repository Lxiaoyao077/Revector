package org.matrix.vector.manager

import android.os.IBinder
import kotlin.system.exitProcess
import org.matrix.vector.ipc.IManagerService
import org.matrix.vector.manager.di.ServiceLocator

/**
 * The one entry point the framework reaches by reflection.
 *
 * `ParasiticManagerHooker.sendBinderToManager` loads `<managerPackage>.Constants` out of the
 * injected dex and invokes the static `setBinder(IBinder)` below. Nothing inside this APK calls it,
 * so R8 must be told to keep both — see `proguard-rules.pro`. Renaming this class or the method
 * breaks the handshake silently, at runtime, with no compile error anywhere.
 */
object Constants {
    @JvmStatic
    fun setBinder(binder: IBinder): Boolean {
        // The interface's fully qualified name is its binder descriptor, and this APK can be older
        // or newer than the framework that pushed the binder: `getManagerApk` exists so the manager
        // can be installed as an ordinary app, and an installed copy survives every later flash.
        //
        // Nothing about that mismatch is loud on its own. `Stub.asInterface` wraps any binder in a
        // proxy without checking, the binder stays alive so `isBinderAlive()` keeps answering true,
        // and every transaction then throws SecurityException out of the daemon's
        // `enforceInterface` — which `DaemonClient.runIpc` turns into a failed Result and every
        // screen draws as empty. So ask first.
        //
        // This one question is exempt by construction: INTERFACE_TRANSACTION sits outside the
        // FIRST_CALL_TRANSACTION..LAST_CALL_TRANSACTION band the generated dispatcher checks the
        // token for, so it is answered across any mismatch. It can still throw — the call is remote
        // and the daemon may have died between the push and here — and a throw is not evidence of a
        // mismatch, so it falls through to binding and lets linkToDeath below report the death.
        val theirDescriptor = runCatching { binder.interfaceDescriptor }.getOrNull()
        if (theirDescriptor != null && theirDescriptor != IManagerService.DESCRIPTOR) {
            ServiceLocator.bindMismatch(theirDescriptor)
            return false
        }

        val service = IManagerService.Stub.asInterface(binder)

        // A matching descriptor means the two ends agree on what this interface is called, not on
        // what is in it. Transaction ids follow declaration order, so a daemon built from a
        // different revision of the AIDL maps the same numbers to different methods, and every call
        // would land somewhere plausible and wrong -- which is worse than failing, because nothing
        // throws. getProtocolVersion is declared first and is therefore transaction zero in every
        // revision, so it is the one question both ends are guaranteed to agree on. A daemon too
        // old to implement it answers 0 out of an untouched reply parcel, which is below the floor
        // and refused for the right reason.
        val theirProtocol = runCatching { service.protocolVersion }.getOrNull()
        if (theirProtocol != null && theirProtocol != IManagerService.PROTOCOL_VERSION) {
            ServiceLocator.bindMismatch("protocol $theirProtocol")
            return false
        }

        ServiceLocator.bind(service)

        try {
            // If the daemon dies the manager is holding a dead binder and every screen would
            // silently show empty state, which reads as "you have no modules" rather than "the
            // framework is gone". Exiting is blunt but honest.
            binder.linkToDeath(
                {
                    exitProcess(0)
                },
                0,
            )
        } catch (_: Exception) {
            exitProcess(0)
        }

        return binder.isBinderAlive
    }
}

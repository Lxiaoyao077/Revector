Revector 2.3 is the fork's first stable release, and it brings three upstream fixes plus the fork's own build identity: the manager's update page reads this repository's builds, and the builds are signed with the fork's key instead of the debug key.

### 🔧 Upstream fixes
*   🛑 **The daemon comes down on an emulated soft reboot.** `emulated_soft_reboot` left the previous daemon running, and the fresh one could not take the port it holds. The module now stops it on the way in (#914).
*   🎯 **OEM inline hook veneers are resolved, not re-entered.** Some OEMs bounce a hooked method through a veneer that lands back on the hook's own entry; the aarch64 path now resolves the veneer instead of recursing into it (#579).
*   🧱 **The toolchain moved forward.** Gradle 9.7 and ktfmt 0.27, wrapper and formatter in step (#903).

### 🧭 The update page reports this build
The manager used to read JingMatrix's releases, where every version code matched a different build with the same number — a Revector build could be marked "installed" against a build this repository never published. It now reads the repository the build was stamped with: a Revector manager offers Revector canaries and Revector stable releases, and the canary page links to this repository's own CI runs. The release line moved here too — `update.json` serves v2.3 from this repository, so Magisk offers the fork's builds under the fork's name.

### ✍️ Signed builds
Release builds are signed with the fork's own keystore, kept in this repository's secrets. The manager the module serves passes its own InstallerVerifier, and the debug-key fallback is gone from the builds people actually install.

---

*Built on JingMatrix's work. Please keep telling us what breaks.*

package com.oussama_chatri.productivityx.core.sync

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConflictResolver @Inject constructor() {

    /**
     * Returns true if the remote version should win over the local version.
     * Strategy: last-write-wins by updatedAt epoch milliseconds.
     * If timestamps are equal, remote wins to avoid divergence.
     */
    fun remoteWins(localUpdatedAtMs: Long, remoteUpdatedAtMs: Long): Boolean =
        remoteUpdatedAtMs >= localUpdatedAtMs

    /**
     * Returns true if the delta qualifies as an unresolvable conflict
     * that should be surfaced to the user (e.g., both sides edited substantive content).
     * Currently we only surface conflicts where local has PENDING changes AND remote is newer.
     */
    fun isUnresolvable(
        localHasPendingChanges: Boolean,
        localUpdatedAtMs: Long,
        remoteUpdatedAtMs: Long
    ): Boolean = localHasPendingChanges && remoteUpdatedAtMs > localUpdatedAtMs
}
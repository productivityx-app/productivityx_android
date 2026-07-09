package com.oussama_chatri.productivityx.core.sync

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ConflictResolverTest {

    private lateinit var resolver: ConflictResolver

    @Before
    fun setup() {
        resolver = ConflictResolver()
    }

    @Test
    fun `remoteWins returns true when remote is newer`() {
        assertTrue(resolver.remoteWins(localUpdatedAtMs = 1000L, remoteUpdatedAtMs = 2000L))
    }

    @Test
    fun `remoteWins returns true when timestamps are equal`() {
        assertTrue(resolver.remoteWins(localUpdatedAtMs = 1500L, remoteUpdatedAtMs = 1500L))
    }

    @Test
    fun `remoteWins returns false when local is newer`() {
        assertFalse(resolver.remoteWins(localUpdatedAtMs = 2000L, remoteUpdatedAtMs = 1000L))
    }

    @Test
    fun `isUnresolvable returns true when local has pending changes and remote is newer`() {
        assertTrue(
            resolver.isUnresolvable(
                localHasPendingChanges = true,
                localUpdatedAtMs = 1000L,
                remoteUpdatedAtMs = 2000L
            )
        )
    }

    @Test
    fun `isUnresolvable returns false when local has NO pending changes even if remote is newer`() {
        assertFalse(
            resolver.isUnresolvable(
                localHasPendingChanges = false,
                localUpdatedAtMs = 1000L,
                remoteUpdatedAtMs = 2000L
            )
        )
    }

    @Test
    fun `isUnresolvable returns false when local has pending changes but local is newer`() {
        assertFalse(
            resolver.isUnresolvable(
                localHasPendingChanges = true,
                localUpdatedAtMs = 2000L,
                remoteUpdatedAtMs = 1000L
            )
        )
    }

    @Test
    fun `isUnresolvable returns false when local has pending changes and timestamps are equal`() {
        assertFalse(
            resolver.isUnresolvable(
                localHasPendingChanges = true,
                localUpdatedAtMs = 1500L,
                remoteUpdatedAtMs = 1500L
            )
        )
    }
}

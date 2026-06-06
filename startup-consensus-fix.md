# Implementation Plan: Jamii Network Startup & Consensus Stabilization

## Background & Motivation
The Jamii network is currently experiencing a total stall upon restart. Nodes resume at Block #3653 but immediately face severe P2P (DTS) disconnection storms ("Mutual termination"). Furthermore, despite being at the same height, nodes report divergent block hashes (Heads) for Block #3653. This split-brain scenario prevents the IBFT consensus from producing Block #3654, as nodes reject proposals with mismatched `ParentHash`. Additionally, shutting down the node abruptly abandons speculative blocks in the `AsyncCommitter` queue.

## Scope & Impact
- **`pkg/dts/engine.go`**: Adjust the twin-channel termination logic to prevent tie-break races from killing active channels.
- **`pkg/core/types/extra_data.go`**: Ensure IBFT seals are added and encoded deterministically to guarantee uniform block hashes across the network.
- **`pkg/node/node.go`**: Implement graceful shutdown for the `AsyncCommitter` to prevent state corruption on restart.
- **Rollback Tool**: Create a utility to safely prune the divergent Block #3653 so nodes can resync or reach consensus cleanly from #3652.

## Proposed Solution
1.  **DTS Stability**: Modify `unregisterPeer` to only trigger "Mutual termination" if the twin channel being closed is actually the active one in the `PeerSet`, preventing tie-break replacements from triggering false disconnects.
2.  **Deterministic Hashes**: Modify `IbftExtraData.AddSeal` to maintain the `Seals` slice in lexicographical order (using `bytes.Compare` or `slices.SortFunc`). Since seals are prefixed with the validator address suffix, this will ensure all nodes construct the exact same `ExtraData` and, consequently, the same `Header.Hash()`.
3.  **Graceful Shutdown**: Update `Node.Stop()` to close the `commitCh` and call `n.committerDone.Wait()` before closing the database, ensuring all pending I/O is flushed.
4.  **Network Recovery**: Provide a standalone Go script that loads the database and deletes the block at height 3653 (and updates the `LatestKey` and `HeightPrefix` pointers to 3652) to manually resolve the current fork.

## Alternatives Considered
-   **Synchronizing Mismatched Heads**: Modifying `SyncManager` to detect and resolve forks at the same height. This is complex for IBFT (which one is the "canonical" fork if both have valid quorums?) and doesn't fix the root cause (non-deterministic hashes). Fixing the hash and manually pruning is safer.
-   **Removing Twin Channels**: Moving to a single P2P channel. This would require a massive rewrite of the DTS engine and its QoS routing, violating the atomic change mandate.

## Implementation Plan

### Phase 1: DTS Stability (Tie-Break Fix)
-   **File**: `pkg/dts/engine.go`
-   **Change**: In `unregisterPeer`, wrap the "Mutual termination" logic in a check that verifies the twin channel is not already replaced by a newer connection before closing it.

### Phase 2: Deterministic Consensus Hashes
-   **File**: `pkg/core/types/extra_data.go`
-   **Change**: Update `AddSeal` to insert the new seal while maintaining a sorted order, or simply sort `e.Seals` before appending/returning in `Encode`. This ensures `Header.Hash()` is identical regardless of the order seals arrived.

### Phase 3: Graceful Shutdown (AsyncCommitter)
-   **File**: `pkg/node/node.go`
-   **Change**: In `Stop()`, close `n.commitCh` and wait on the `WaitGroup` (`n.committerDone.Wait()`) before calling `n.db.Close()`.

### Phase 4: Recovery Utility
-   **File**: `cmd/recovery/prune_head.go` (New)
-   **Change**: Write a script that safely rewinds the local StateDB and Blockchain pointers by 1 block, allowing operators to fix the divergent Block 3653.

## Verification
-   Start 2 nodes locally, verify they can maintain stable DTS connections without mutual termination loops.
-   Run a small consensus test and verify the block hashes match exactly on both nodes.
-   Run the pruning script and verify the node successfully boots from the previous block.

## Migration & Rollback
-   The deterministic seal fix applies to new blocks. It won't alter historical blocks unless they are reconstructed from skeletons.
-   If the fix causes consensus failures, we can revert `extra_data.go` and investigate alternative serialization methods.

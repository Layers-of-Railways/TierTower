/*
 * Tier Tower
 * Copyright (c) 2025 The Tier Tower Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package io.github.slimeistdev.tier_tower.foundation.storage;

import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;

@SuppressWarnings("UnstableApiUsage")
public class WrappedSingleSlotStorage<T> implements SingleSlotStorage<T> {
    private final SingleSlotStorage<T> inner;
    private final SnapshotParticipant<?>[] participants;

    public WrappedSingleSlotStorage(SingleSlotStorage<T> inner, SnapshotParticipant<?>... participants) {
        this.inner = inner;
        this.participants = participants;
    }

    private void updateParticipants(@Nullable TransactionContext transaction) {
        if (transaction == null) return;
        for (SnapshotParticipant<?> participant : participants) {
            participant.updateSnapshots(transaction);
        }
    }

    @Override
    public long insert(T resource, long maxAmount, TransactionContext transaction) {
        updateParticipants(transaction);
        return inner.insert(resource, maxAmount, transaction);
    }

    @Override
    public long extract(T resource, long maxAmount, TransactionContext transaction) {
        updateParticipants(transaction);
        return inner.extract(resource, maxAmount, transaction);
    }

    @Override
    public boolean isResourceBlank() {
        return inner.isResourceBlank();
    }

    @Override
    public T getResource() {
        return inner.getResource();
    }

    @Override
    public long getAmount() {
        return inner.getAmount();
    }

    @Override
    public long getCapacity() {
        return inner.getCapacity();
    }

    @Override
    public boolean supportsInsertion() {
        return inner.supportsInsertion();
    }

    @Override
    public boolean supportsExtraction() {
        return inner.supportsExtraction();
    }

    @Override
    public Iterator<StorageView<T>> nonEmptyIterator() {
        return Iterators.transform(inner.nonEmptyIterator(), WrappedStorageView::new);
    }

    @Override
    public Iterable<StorageView<T>> nonEmptyViews() {
        return Iterables.transform(inner.nonEmptyViews(), WrappedStorageView::new);
    }

    @Override
    public long getVersion() {
        return inner.getVersion();
    }

    @SuppressWarnings("removal")
    @Override
    public long simulateInsert(T resource, long maxAmount, @Nullable TransactionContext transaction) {
        return inner.simulateInsert(resource, maxAmount, transaction);
    }

    @SuppressWarnings("removal")
    @Override
    public long simulateExtract(T resource, long maxAmount, @Nullable TransactionContext transaction) {
        return inner.simulateExtract(resource, maxAmount, transaction);
    }

    @SuppressWarnings("removal")
    @Override
    public @Nullable StorageView<T> exactView(T resource) {
        StorageView<T> exact = inner.exactView(resource);
        if (exact == null) return null;
        return new WrappedStorageView(exact);
    }

    @Override
    public StorageView<T> getUnderlyingView() {
        return inner.getUnderlyingView();
    }

    @Override
    public @NotNull Iterator<StorageView<T>> iterator() {
        return Iterators.transform(inner.iterator(), WrappedStorageView::new);
    }

    @Override
    public int getSlotCount() {
        return inner.getSlotCount();
    }

    @Override
    public SingleSlotStorage<T> getSlot(int slot) {
        SingleSlotStorage<T> slot$ = inner.getSlot(slot);
        return slot$ == inner ? this : new WrappedSingleSlotStorage<>(slot$);
    }

    private class WrappedStorageView implements StorageView<T> {
        private final StorageView<T> innerView;

        private WrappedStorageView(StorageView<T> innerView) {
            this.innerView = innerView;
        }

        @Override
        public long extract(T resource, long maxAmount, TransactionContext transaction) {
            updateParticipants(transaction);
            return innerView.extract(resource, maxAmount, transaction);
        }

        @Override
        public boolean isResourceBlank() {
            return innerView.isResourceBlank();
        }

        @Override
        public T getResource() {
            return innerView.getResource();
        }

        @Override
        public long getAmount() {
            return innerView.getAmount();
        }

        @Override
        public long getCapacity() {
            return innerView.getCapacity();
        }

        @Override
        public StorageView<T> getUnderlyingView() {
            return innerView.getUnderlyingView();
        }
    }
}

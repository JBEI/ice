package org.jbei.ice.lib.search;

import org.hibernate.search.batchindexing.MassIndexerProgressMonitor;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Hector Plahar
 */
public class IndexerProgressMonitor implements MassIndexerProgressMonitor {

    private static IndexerProgressMonitor INSTANCE = new IndexerProgressMonitor();
    private final AtomicLong documentsDoneCounter = new AtomicLong();
    private final AtomicLong totalCounter = new AtomicLong();

    private IndexerProgressMonitor() {
    }

    public static IndexerProgressMonitor getInstance() {
        return INSTANCE;
    }

    @Override
    /**
     * {@inheritDoc}
     */
    public void documentsBuilt(int number) {
    }

    @Override
    /**
     * {@inheritDoc}
     */
    public void entitiesLoaded(int size) {
        // ignore
    }

    @Override
    /**
     * {@inheritDoc}
     */
    public void addToTotalCount(long count) {
        totalCounter.addAndGet(count);
    }

    @Override
    /**
     * {@inheritDoc}
     */
    public void indexingCompleted() {
        documentsDoneCounter.set(0l);
        totalCounter.set(0l);
    }

    @Override
    /**
     * {@inheritDoc}
     */
    public void documentsAdded(long increment) {
        documentsDoneCounter.getAndAdd(increment);
    }

    public void indexingInterrupted() {

    }

    public IndexBuildStatus getStatus() {
        return new IndexBuildStatus(documentsDoneCounter.get(), totalCounter.get());
    }
}

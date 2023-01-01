package org.jbei.ice.search;

import org.hibernate.search.mapper.pojo.massindexing.MassIndexingMonitor;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Hector Plahar
 */
public class IndexerProgressMonitor implements MassIndexingMonitor {

    private static final IndexerProgressMonitor INSTANCE = new IndexerProgressMonitor();
    private final AtomicLong documentsDoneCounter = new AtomicLong();
    private final AtomicLong totalCounter = new AtomicLong();

    private IndexerProgressMonitor() {
    }

    public static IndexerProgressMonitor getInstance() {
        return INSTANCE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addToTotalCount(long count) {
        totalCounter.addAndGet(count);
    }

    @Override
    public void indexingCompleted() {
        documentsDoneCounter.set(0L);
        totalCounter.set(0L);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void documentsAdded(long increment) {
        documentsDoneCounter.getAndAdd(increment);
    }

    @Override
    public void documentsBuilt(long increment) {

    }

    @Override
    public void entitiesLoaded(long increment) {

    }

    public void indexingInterrupted() {

    }

    public IndexBuildStatus getStatus() {
        return new IndexBuildStatus(documentsDoneCounter.get(), totalCounter.get());
    }
}

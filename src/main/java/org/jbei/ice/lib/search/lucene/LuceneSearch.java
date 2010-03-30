package org.jbei.ice.lib.search.lucene;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.util.Version;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.managers.EntryManager;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.managers.SampleManager;
import org.jbei.ice.lib.managers.UtilsManager;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.models.Part;
import org.jbei.ice.lib.models.Plasmid;
import org.jbei.ice.lib.models.Sample;
import org.jbei.ice.lib.models.Strain;
import org.jbei.ice.lib.utils.JbeirSettings;
import org.jbei.ice.lib.utils.Job;
import org.jbei.ice.lib.utils.JobCue;
import org.jbei.ice.lib.utils.Utils;

/**
 * Full text search of the registry.
 * It uses a single instance of the IndexSearcher for efficiency, and
 * most importantly to be able to rebuild the underlying index transparently.
 * When rebuildIndex() is called, the index is rebuilt, after which a new
 * IndexSearcher is instantiated with the updated index.
 * 
 * @author tham
 * 
 */
public class LuceneSearch {
    private IndexSearcher indexSearcher = null;
    private File indexFile = null;
    private boolean newIndex = false;
    private final int SEARCH_MAX_RESULT = 1000;

    private LuceneSearch() {
        initializeIndexSearcher();
    }

    private static class SingletonHolder {
        private static final LuceneSearch INSTANCE = new LuceneSearch();
    }

    public static LuceneSearch getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public void rebuildIndex() throws SearchException {
        File indexFile = new File(JbeirSettings.getSetting("SEARCH_INDEX_FILE"));
        FSDirectory directory = null;

        try {
            directory = FSDirectory.open(indexFile);
        } catch (IOException e) {
            throw new SearchException("Failed to open index file!", e);
        }

        IndexWriter indexWriter = null;
        try {
            indexWriter = new IndexWriter(directory, new StandardAnalyzer(Version.LUCENE_CURRENT),
                    true, IndexWriter.MaxFieldLength.UNLIMITED);

            ArrayList<Entry> entries = EntryManager.getAllEntries();

            for (Entry entry : entries) {
                Document document = createDocument(entry);
                indexWriter.addDocument(document);
            }

            indexWriter.commit();
            indexWriter.close();

            indexSearcher = new IndexSearcher(directory, true);
        } catch (CorruptIndexException e) {
            throw new SearchException(e);
        } catch (LockObtainFailedException e) {
            throw new SearchException(e);
        } catch (IOException e) {
            throw new SearchException(e);
        } catch (ManagerException e) {
            throw new SearchException(e);
        }
    }

    public ArrayList<SearchResult> query(String queryString) throws SearchException {
        ArrayList<SearchResult> result = new ArrayList<SearchResult>();
        Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_CURRENT);

        if (newIndex == true) {
            newIndex = false;

            Logger.info("Creating search index for the first time");
            JobCue jobCue = JobCue.getInstance();
            jobCue.addJob(Job.REBUILD_SEARCH_INDEX);
            jobCue.processIn(5000);
        } else if (indexSearcher == null) {

        } else {
            try {
                QueryParser parser = new QueryParser(Version.LUCENE_CURRENT, "content", analyzer);
                Query query;
                query = parser.parse(queryString);
                IndexSearcher searcher = getIndexSearcher();
                TopDocs hits = searcher.search(query, SEARCH_MAX_RESULT);

                ArrayList<ScoreDoc> hitsArray = new ArrayList<ScoreDoc>(Arrays
                        .asList(hits.scoreDocs));

                for (ScoreDoc scoreDoc : hitsArray) {
                    float score = scoreDoc.score;
                    int docId = scoreDoc.doc;
                    Document doc = indexSearcher.doc(docId);
                    String recordId = doc.get("Record ID");
                    result.add(new SearchResult(EntryManager.getByRecordId(recordId), score));
                }
            } catch (ParseException e) {
                throw new SearchException(e);
            } catch (IOException e) {
                throw new SearchException(e);
            } catch (ManagerException e) {
                throw new SearchException(e);
            }
        }

        return result;
    }

    private IndexSearcher getIndexSearcher() throws SearchException {
        if (indexSearcher == null) {
            try {
                createEmptyIndex();
            } catch (Exception e) {
                String msg = "Could not initiate search. Is the search index directory writable?";
                Logger.error(msg, e);
                throw new SearchException(msg);
            }
        }
        return indexSearcher;
    }

    private void initializeIndexSearcher() {
        indexFile = new File(JbeirSettings.getSetting("SEARCH_INDEX_FILE"));
        if (!indexFile.canWrite()) {
            String msg = "Search index " + JbeirSettings.getSetting("SEARCH_INDEX_FILE")
                    + " is not writable.";
            Logger.error(msg, new Exception("Error"));
        }
        FSDirectory directory;
        try {
            directory = FSDirectory.open(indexFile);
            indexSearcher = new IndexSearcher(directory, true);

        } catch (IOException e) {
            String msg = "Could not open index file";
            Logger.error(msg, e);
            try {
                msg = "Trying to create index file " + indexFile.getAbsolutePath();
                Logger.error(msg, e);
                createEmptyIndex();
                directory = FSDirectory.open(indexFile);
                indexSearcher = new IndexSearcher(directory, true);
                newIndex = true;
            } catch (IOException e1) {
                msg = "Directory exists, but could not create empty index.";
                Logger.error(msg, e);
                e.printStackTrace();
                e1.printStackTrace();

                indexFile = null;
                indexSearcher = null;
            }
        }
    }

    private void createEmptyIndex() throws IOException {
        FSDirectory directory = null;
        try {
            directory = FSDirectory.open(indexFile);
        } catch (IOException e) {
            String msg = "Could not create Empty Index";
            Logger.error(msg, e);
            throw e;
        }

        IndexWriter indexWriter = new IndexWriter(directory, new StandardAnalyzer(
                Version.LUCENE_CURRENT), true, IndexWriter.MaxFieldLength.UNLIMITED);

        indexWriter.commit();
        indexWriter.close();
        Logger.info("Created empty Index");
    }

    protected static Document createDocument(Entry entry) throws SearchException {
        Document document = new Document();
        String content = "";

        String recordId = (entry.getRecordId() != null) ? entry.getRecordId() : "";
        String recordType = (entry.getRecordType() != null) ? entry.getRecordType() : "";
        String owner = (entry.getOwner() != null) ? entry.getOwner() : "";
        String ownerEmail = (entry.getOwnerEmail() != null) ? entry.getOwnerEmail() : "";
        String creator = (entry.getCreator() != null) ? entry.getCreator() : "";
        String creatorEmail = (entry.getCreatorEmail() != null) ? entry.getCreatorEmail() : "";
        String alias = (entry.getAlias() != null) ? entry.getAlias() : "";
        String keywords = (entry.getKeywords() != null) ? entry.getKeywords() : "";
        String shortDescription = (entry.getShortDescription() != null) ? entry
                .getShortDescription() : "";
        String longDescription = (entry.getLongDescription() != null) ? entry.getLongDescription()
                : "";
        String references = (entry.getReferences() != null) ? entry.getReferences() : "";
        String selectionMarkers = Utils.toCommaSeparatedStringFromSelectionMarkers(entry
                .getSelectionMarkers());
        String links = Utils.toCommaSeparatedStringFromLinks(entry.getLinks());
        String names = Utils.toCommaSeparatedStringFromNames(entry.getNames());
        String partNumbers = Utils.toCommaSeparatedStringFromPartNumbers(entry.getPartNumbers());
        String intellectualProperty = (entry.getIntellectualProperty() != null) ? entry
                .getIntellectualProperty() : "";
        String fundingSources = Utils.toCommaSeparatedStringFromEntryFundingSources(entry
                .getEntryFundingSources());

        document.add(new Field("Record ID", recordId, Field.Store.YES, Field.Index.ANALYZED));
        content = content + recordId + " ";
        document.add(new Field("Record Type", recordType, Field.Store.YES, Field.Index.ANALYZED));
        content = content + recordType + " ";
        document.add(new Field("Owner", owner, Field.Store.YES, Field.Index.ANALYZED));
        content = content + owner + " ";
        document.add(new Field("Owner Email", ownerEmail, Field.Store.YES, Field.Index.ANALYZED));
        content = content + ownerEmail + " ";
        document.add(new Field("Creator", creator, Field.Store.YES, Field.Index.ANALYZED));
        content = content + creator + " ";
        document
                .add(new Field("Creator Email", creatorEmail, Field.Store.YES, Field.Index.ANALYZED));
        content = content + creatorEmail + " ";
        document.add(new Field("Alias", alias, Field.Store.YES, Field.Index.ANALYZED));
        content = content + alias + " ";
        document.add(new Field("Keywords", keywords, Field.Store.YES, Field.Index.ANALYZED));
        content = content + keywords + " ";
        document.add(new Field("Summary", shortDescription, Field.Store.YES, Field.Index.ANALYZED));
        content = content + shortDescription + " ";
        document.add(new Field("Notes", longDescription, Field.Store.YES, Field.Index.ANALYZED));
        content = content + longDescription + " ";
        document.add(new Field("References", references, Field.Store.YES, Field.Index.ANALYZED));
        content = content + references + " ";

        document.add(new Field("Selection Markers", selectionMarkers, Field.Store.YES,
                Field.Index.ANALYZED));
        content = content + selectionMarkers + " ";

        document.add(new Field("Links", links, Field.Store.YES, Field.Index.ANALYZED));
        content = content + links + " ";

        document.add(new Field("Names", names, Field.Store.YES, Field.Index.ANALYZED));
        content = content + names + " ";

        document.add(new Field("Part Number", partNumbers, Field.Store.YES, Field.Index.ANALYZED));
        content = content + partNumbers + " ";

        document.add(new Field("Intellectual Property", intellectualProperty, Field.Store.YES,
                Field.Index.ANALYZED));
        content = content + intellectualProperty + " ";

        document.add(new Field("Funding Source", fundingSources, Field.Store.YES,
                Field.Index.ANALYZED));
        content = content + fundingSources + " ";

        ArrayList<Sample> samples = null;
        try {
            samples = SampleManager.getSamplesByEntry(entry);
        } catch (ManagerException e) {
            e.printStackTrace();
        }

        if (samples != null) {
            ArrayList<String> samplesArray = new ArrayList<String>();
            for (Sample sample : samples) {
                ArrayList<String> temp = new ArrayList<String>();
                temp.add(sample.getLabel());
                temp.add(sample.getDepositor());
                temp.add(sample.getNotes());
                temp.add(sample.getUuid());
                samplesArray.add(Utils.join(", ", temp));
            }
            String samplesString = Utils.join("; ", samplesArray);
            document
                    .add(new Field("Samples", samplesString, Field.Store.YES, Field.Index.ANALYZED));
            content = content + samplesString + " ";
        }

        if (entry instanceof Plasmid) {
            Plasmid plasmid = (Plasmid) entry;
            String backbone = (plasmid.getBackbone() != null) ? plasmid.getBackbone() : "";
            String origin = (plasmid.getOriginOfReplication() != null) ? plasmid
                    .getOriginOfReplication() : "";
            String promoters = (plasmid.getPromoters() != null) ? plasmid.getPromoters() : "";

            document.add(new Field("Backbone", backbone, Field.Store.YES, Field.Index.ANALYZED));
            content = content + backbone + " ";
            document.add(new Field("Origin of Replication", origin, Field.Store.YES,
                    Field.Index.ANALYZED));
            content = content + origin + " ";
            document.add(new Field("Promoters", promoters, Field.Store.YES, Field.Index.ANALYZED));
            content = content + promoters + " ";
            LinkedHashSet<Strain> strains = new LinkedHashSet<Strain>();
            try {
                strains = UtilsManager.getStrainsForPlasmid(plasmid);
            } catch (ManagerException e) {
                throw new SearchException(e);
            }
            StringBuilder strainStringBuilder = new StringBuilder();
            for (Strain strain : strains) {
                strainStringBuilder.append(strain.getPartNumbersAsString());
                strainStringBuilder.append(" ");
            }
            document.add(new Field("Strains", strainStringBuilder.toString(), Field.Store.YES,
                    Field.Index.ANALYZED));
            content = content + strainStringBuilder.toString() + " ";

        } else if (entry instanceof Strain) {
            Strain strain = (Strain) entry;
            String host = (strain.getHost() != null) ? strain.getHost() : "";
            String genotype = (strain.getGenotypePhenotype() != null) ? strain
                    .getGenotypePhenotype() : "";
            String plasmids = (strain.getPlasmids() != null) ? strain.getPlasmids() : "";

            document.add(new Field("Host", host, Field.Store.YES, Field.Index.ANALYZED));
            content = content + host + " ";
            document.add(new Field("Genotype Phenotype", genotype, Field.Store.YES,
                    Field.Index.ANALYZED));
            content = content + genotype + " ";
            document.add(new Field("Plasmids", plasmids, Field.Store.YES, Field.Index.ANALYZED));
            content = content + plasmids + " ";

        } else if (entry instanceof Part) {
            Part part = (Part) entry;
            String format = (part.getPackageFormat() != null) ? part.getPackageFormat() : "";
            document
                    .add(new Field("Package Format", format, Field.Store.YES, Field.Index.ANALYZED));
            content = content + format + " ";
        }

        document.add(new Field("content", content, Field.Store.NO, Field.Index.ANALYZED));

        return document;
    }

    public static void main(String[] args) {
        /*try {
            //Search.getInstance().rebuildIndex();
            Search searcher = Search.getInstance();
            ArrayList<SearchResult> results = searcher.query("thesis");
            for (SearchResult result : results) {
                System.out.println("Score " + result.getScore() + " RecordId "
                        + Utils.toCommaSeparatedStringFromNames(result.getEntry().getNames()));
            }
            System.out.println("" + results.size());
        } catch (Exception e) {
            e.printStackTrace();
        }*/
    }
}

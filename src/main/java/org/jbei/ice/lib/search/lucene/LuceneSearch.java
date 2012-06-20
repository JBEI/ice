package org.jbei.ice.lib.search.lucene;

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
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.account.AccountController;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.entry.EntryController;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.entry.model.Part;
import org.jbei.ice.lib.entry.model.PartNumber;
import org.jbei.ice.lib.entry.model.Plasmid;
import org.jbei.ice.lib.entry.model.Strain;
import org.jbei.ice.lib.entry.sample.SampleController;
import org.jbei.ice.lib.entry.sample.model.Sample;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.models.FundingSource;
import org.jbei.ice.lib.permissions.PermissionException;
import org.jbei.ice.lib.utils.JbeirSettings;
import org.jbei.ice.lib.utils.Job;
import org.jbei.ice.lib.utils.JobCue;
import org.jbei.ice.lib.utils.ParameterGeneratorParser;
import org.jbei.ice.lib.utils.Utils;
import org.jbei.ice.lib.utils.UtilsDAO;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;

/**
 * Full text search of the registry using Lucene search engine.
 * <p/>
 * It uses a single instance of the IndexSearcher for efficiency, and most importantly to be able to
 * rebuild the underlying index transparently. When rebuildIndex() is called, the index is rebuilt,
 * after which a new IndexSearcher is instantiated with the updated index.
 *
 * @author Timothy Ham, Zinovii Dmytriv
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

    /**
     * Retrieve the singleton instance of this classe.
     *
     * @return LuceneSearch instance.
     */
    public static LuceneSearch getInstance() {
        return SingletonHolder.INSTANCE;
    }

    /**
     * Rebuild the lucene index.
     * <p/>
     * Serialize {@link Entry} and some associated classes such as {@link PartNumber},
     *
     * @throws SearchException
     */
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

            ArrayList<Entry> entries = new EntryController().getAllEntries();

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
        } catch (ControllerException e) {
            throw new SearchException(e);
        }
    }

    /**
     * Search the lucene index for the queryString.
     * <p/>
     * If the database does not exist, create one.
     *
     * @param queryString query to be performed.
     * @return ArrayList of {@link SearchResult}s.
     * @throws SearchException
     */
    public ArrayList<SearchResult> query(String queryString) throws SearchException {
        ArrayList<SearchResult> result = new ArrayList<SearchResult>();
        Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_CURRENT);
        EntryController controller = new EntryController();
        AccountController accountController = new AccountController();

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
                Account systemAccount = accountController.getSystemAccount();

                ArrayList<ScoreDoc> hitsArray = new ArrayList<ScoreDoc>(
                        Arrays.asList(hits.scoreDocs));

                for (ScoreDoc scoreDoc : hitsArray) {
                    float score = scoreDoc.score;
                    int docId = scoreDoc.doc;
                    Document doc = indexSearcher.doc(docId);
                    String recordId = doc.get("Record ID");
                    Entry entry;
                    try {
                        entry = controller.getByRecordId(systemAccount, recordId);
                    } catch (PermissionException e) {
                        Logger.warn("No permission to add search result " + recordId);
                        continue;
                    }
                    result.add(new SearchResult(entry, score));
                }
            } catch (ParseException e) {
                throw new SearchException(e);
            } catch (IOException e) {
                throw new SearchException(e);
            } catch (ControllerException e) {
                throw new SearchException(e);
            }
        }

        return result;
    }

    /**
     * Creates an empty index on disk.
     *
     * @return {@link IndexSearcher}.
     * @throws SearchException
     */
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

    /**
     * Initialize a new index on disk.
     */
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

    /**
     * Create a new empty index on disk.
     *
     * @throws IOException
     */
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

    /**
     * Create a new lucene {@link Document} from the given {@link Entry}.
     * <p/>
     * Related objects, such as {@link PartNumber} or {@link org.jbei.ice.lib.entry.model.Name Name}s or
     * {@link FundingSource}s are also put into the document.
     *
     * @param entry
     * @return Document.
     * @throws SearchException
     */
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
        String parameters = ParameterGeneratorParser
                .generateParametersString(entry.getParameters());

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
        document.add(new Field("Creator Email", creatorEmail, Field.Store.YES, Field.Index.ANALYZED));
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

        document.add(new Field("Parameters", parameters, Field.Store.YES, Field.Index.ANALYZED));
        content = content + parameters + " ";

        ArrayList<Sample> samples = null;
        try {
            SampleController controller = new SampleController();
            samples = controller.getSamplesByEntry(entry);
        } catch (ControllerException e) {
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
            document.add(new Field("Samples", samplesString, Field.Store.YES, Field.Index.ANALYZED));
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
                strains = UtilsDAO.getStrainsForPlasmid(plasmid);
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
            String format = (part.getPackageFormat().toString() != null) ? part.getPackageFormat()
                                                                               .toString() : "";
            document.add(new Field("Package Format", format, Field.Store.YES, Field.Index.ANALYZED));
            content = content + format + " ";
        }

        document.add(new Field("content", content, Field.Store.NO, Field.Index.ANALYZED));

        return document;
    }
}

package org.jbei.ice.lib.search;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.managers.EntryManager;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.managers.SampleManager;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.models.Part;
import org.jbei.ice.lib.models.Plasmid;
import org.jbei.ice.lib.models.Sample;
import org.jbei.ice.lib.models.Strain;
import org.jbei.ice.lib.utils.JbeirSettings;
import org.jbei.ice.lib.utils.Utils;

public class Search {
	
	public static IndexWriter createIndex() throws Exception {
		File indexFile = new File(JbeirSettings.getSetting("SEARCH_INDEX_FILE"));
		FSDirectory directory = null;
		try {
			directory = FSDirectory.open(indexFile);
		} catch (IOException e) {
			String msg = "Could not open index file as a lucene directory";
			Logger.error(msg);
			e.printStackTrace();
			throw new Exception(msg, e);
		}
		
		IndexWriter indexWriter = new IndexWriter(directory, 
				new StandardAnalyzer(Version.LUCENE_CURRENT), 
				true, IndexWriter.MaxFieldLength.UNLIMITED);
		
		Set<Entry> entries = EntryManager.getAll();
		for (Entry entry: entries) {
			Document document = createDocument(entry);
			indexWriter.addDocument(document);
		}
		
		indexWriter.commit();
		return indexWriter;
	}
	
	
	protected static Document createDocument(Entry entry) {
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
		String shortDescription = (entry.getShortDescription() != null) ? entry.getShortDescription() : "";
		String longDescription = (entry.getLongDescription() != null) ? entry.getLongDescription() : "";
		String references = (entry.getReferences() != null) ? entry.getReferences() : "";
		String selectionMarkers = Utils.toCommaSeparatedStringFromSelectionMarkers(entry.getSelectionMarkers());
		String links = Utils.toCommaSeparatedStringFromLinks(entry.getLinks());
		String names = Utils.toCommaSeparatedStringFromNames(entry.getNames());
		String partNumbers = Utils.toCommaSeparatedStringFromPartNumbers(entry.getPartNumbers());
		
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
		
		document.add(new Field("Selection Markers", selectionMarkers, Field.Store.YES, Field.Index.ANALYZED));
		content = content + selectionMarkers + " ";
		
		document.add(new Field("Links", links, Field.Store.YES, Field.Index.ANALYZED));
		content = content + links + " ";
		
		document.add(new Field("Names", names, Field.Store.YES, Field.Index.ANALYZED));
		content = content + names + " ";
		
		document.add(new Field("Selection Markers", partNumbers, Field.Store.YES, Field.Index.ANALYZED));
		content = content + partNumbers + " ";

		Set<Sample> samples = null;
		try {
			samples = SampleManager.get(entry);
		} catch (ManagerException e) {
			e.printStackTrace();
		}
		if (samples != null) {
			ArrayList<String> samplesArray = new ArrayList<String>();
			for (Sample sample: samples) {
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
		
		if (entry.getClass().isInstance(Plasmid.class)) {
			Plasmid plasmid = (Plasmid) entry;
			String backbone = (plasmid.getBackbone() != null) ? plasmid.getBackbone() : "";
			String origin = (plasmid.getOriginOfReplication() != null) ? plasmid.getOriginOfReplication() : "";
			String promoters = (plasmid.getPromoters() != null) ? plasmid.getOriginOfReplication() : "";
			
			document.add(new Field("Backbone", backbone, Field.Store.YES, Field.Index.ANALYZED));
			content = content + backbone + " ";
			document.add(new Field("Origin of Replication", origin, Field.Store.YES, Field.Index.ANALYZED));
			content = content + origin + " ";
			document.add(new Field("Promoters", promoters, Field.Store.YES, Field.Index.ANALYZED));
			content = content + promoters + " ";
			
		} else if (entry.getClass().isInstance(Strain.class)) {
			Strain strain = (Strain) entry;
			String host = (strain.getHost() != null) ? strain.getHost() : "";
			String genotype = (strain.getGenotypePhenotype() != null) ? strain.getGenotypePhenotype() : "";
			String plasmids = (strain.getPlasmids() != null) ? strain.getPlasmids() : "";
			
			document.add(new Field("Host", host, Field.Store.YES, Field.Index.ANALYZED));
			content = content + host + " ";
			document.add(new Field("Genotype Phenotype", genotype, Field.Store.YES, Field.Index.ANALYZED));
			content = content + genotype + " ";
			document.add(new Field("Plasmids", plasmids, Field.Store.YES, Field.Index.ANALYZED));
			content = content + plasmids + " ";
			
		} else if (entry.getClass().isInstance(Part.class)) {
			Part part = (Part) entry;
			String format = (part.getPackageFormat() != null) ? part.getPackageFormat() : "" ;
			document.add(new Field("Package Format", format, Field.Store.YES, Field.Index.ANALYZED));
			content = content + format + " ";
		}
		
		document.add(new Field("content", content, Field.Store.NO, Field.Index.ANALYZED));
		return document;
		
	}
	
	public static ArrayList<Entry> query(String queryString) throws Exception {
		ArrayList<Entry> result = new ArrayList<Entry>();
		Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_CURRENT);
		File indexFile = new File(JbeirSettings.getSetting("SEARCH_INDEX_FILE"));
		Directory directory;
		try {
			directory = FSDirectory.open(indexFile);
			IndexSearcher indexSearcher = new IndexSearcher(directory, true);
			
			QueryParser parser = new QueryParser(Version.LUCENE_CURRENT, "content", analyzer);
			Query query = parser.parse(queryString);
			TopDocs hits = indexSearcher.search(query, 1000);
			Logger.info("" + hits.totalHits + " results found");
			
			ArrayList<ScoreDoc> hitsArray = new ArrayList<ScoreDoc>(Arrays.asList(hits.scoreDocs));
			
			for (ScoreDoc scoreDoc : hitsArray) {
				String score = "" + scoreDoc.score;
				int docId = scoreDoc.doc;
				Document doc = indexSearcher.doc(docId);
				String recordId = doc.get("Record ID");
				Entry entry = EntryManager.getByRecordId(recordId);
				result.add(entry);
			}
			
		} catch (IOException e) {
			String msg = "Search could not open index file";
			Logger.error(msg);
			e.printStackTrace();
			throw new Exception(msg, e);
		} catch (ParseException e) {
			String msg = "Search could not parse query: " + queryString;
			Logger.error(msg);
			e.printStackTrace();
			throw new Exception(msg, e);
		}
		
		return result;
	}
	
	public static void main(String[] args) {
		
		try {
			//IndexWriter indexWriter = createIndex();
			
			ArrayList<Entry> result = query("thesis");
			for (Entry entry : result) {
				System.out.println("RecordId " + entry.getRecordId());
			}
			System.out.println("" + result.size());
		} catch (Exception e) {
			
			e.printStackTrace();
		}
	}
}

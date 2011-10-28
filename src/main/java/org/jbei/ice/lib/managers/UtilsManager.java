package org.jbei.ice.lib.managers;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.jbei.ice.lib.dao.DAO;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.models.Account;
import org.jbei.ice.lib.models.Comment;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.models.PartNumber;
import org.jbei.ice.lib.models.Plasmid;
import org.jbei.ice.lib.models.SelectionMarker;
import org.jbei.ice.lib.models.Strain;
import org.jbei.ice.lib.models.Vote;
import org.jbei.ice.lib.utils.JbeirSettings;
import org.jbei.ice.lib.utils.Utils;

/**
 * Manager to deal with various objects in the database for utility purposes.
 * <p>
 * Methods here do not clearly belong with the manipulation of objects.
 * 
 * @author Timothy Ham, Zinovii Dmytriv, Joanna Chen
 * 
 */
@SuppressWarnings("unchecked")
public class UtilsManager {

    /**
     * Retrieve all the unique {@link SelectionMarker}s as collection of Strings.
     * <p>
     * This method is useful for fetching auto-complete list of selection markers.
     * 
     * @return
     * @throws ManagerException
     */
    public static TreeSet<String> getUniqueSelectionMarkers() throws ManagerException {
        TreeSet<String> results = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
        Session session = DAO.newSession();
        Query query = session
                .createQuery("select distinct selectionMarker.name from SelectionMarker selectionMarker");
        HashSet<String> rawMarkers = null;
        try {
            rawMarkers = new HashSet<String>(query.list());
        } catch (HibernateException e) {
            throw new ManagerException(e);
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }
        /* Markers are comma separated lists, so must parse them 
        getting from the database */

        for (String item : rawMarkers) {
            if (item == null) {
                continue;
            }

            LinkedHashSet<String> markers = Utils.toHashSetFromCommaSeparatedString(item);
            for (String marker : markers) {
                results.add(marker);
            }
        }

        return results;
    }

    /**
     * Retrieve unique, publicly visible plasmid names from the database.
     * <p>
     * This method is useful for fetching auto-complete list of plasmid names.
     * 
     * @return
     */
    public static TreeSet<String> getUniquePublicPlasmidNames() {
        TreeSet<String> results = new TreeSet<String>();
        Session session = DAO.newSession();
        Query query = session
                .createQuery("select distinct name.name from Plasmid plasmid inner join plasmid.names as name where name.name <> '' order by name.name asc");
        HashSet<String> names = new HashSet<String>();
        try {
            new HashSet<String>(query.list());
        } catch (HibernateException e) {
            Logger.error("Could not get unique public plasmid names " + e.toString(), e);
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }

        for (String name : names) {
            results.add(name);
        }

        return results;
    }

    /**
     * Retrieve unique promoters from the database.
     * <p>
     * This method is useful for fetching auto-complete list of promoters.
     * 
     * @return
     */
    public static TreeSet<String> getUniquePromoters() {
        TreeSet<String> results = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
        Session session = DAO.newSession();
        Query query = session
                .createQuery("select distinct plasmid.promoters from Plasmid plasmid ");
        HashSet<String> rawPromoters = new HashSet<String>();
        try {
            rawPromoters = new HashSet<String>(query.list());
        } catch (HibernateException e) {
            Logger.error("Could not get unique promoters " + e.toString(), e);
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }
        /* Prometers are comma separated lists, so must parse them 
        getting from the database */

        for (String item : rawPromoters) {
            if (item == null) {
                continue;
            }

            LinkedHashSet<String> promoters = Utils.toHashSetFromCommaSeparatedString(item);
            for (String promoter : promoters) {
                results.add(promoter);
            }
        }

        return results;
    }

    /**
     * Retrieve unique origin of replications from the database.
     * <p>
     * This method is useful for fetching auto-complete list of origin of replications.
     * 
     * @return
     */
    public static TreeSet<String> getUniqueOriginOfReplications() {
        TreeSet<String> results = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
        Session session = DAO.newSession();
        Query query = session
                .createQuery("select distinct plasmid.originOfReplication from Plasmid plasmid ");

        HashSet<String> rawOrigins = new HashSet<String>();

        try {
            rawOrigins = new HashSet<String>(query.list());
        } catch (HibernateException e) {
            Logger.error("Could not get unique origins of replication " + e.toString(), e);
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }
        /* Origin of replications are comma separated lists, so must parse them 
        getting from the database */

        for (String item : rawOrigins) {
            if (item == null) {
                continue;
            }

            LinkedHashSet<String> origins = Utils.toHashSetFromCommaSeparatedString(item);
            for (String origin : origins) {
                results.add(origin);
            }
        }

        return results;
    }

    /**
     * Retrieve the {@link Strain} objects associated with the given {@link Plasmid}.
     * <p>
     * Strain objects have a field "plasmids", which is maybe wiki text of plasmids that the strain
     * may harbor. However, since plasmids can be harbored in multiple strains, the reverse lookup
     * must be computed in order to find which strains harbor a plasmid. And since it is possible to
     * import/export strains separately from plasmids, it is possible that even though the strain
     * claims to have a plasmid, that plasmid may not be in this system, but some other. So, in
     * order to find out which strains actually harbor the given plasmid, we must query the strains
     * table for the plasmid, parse the wiki text, and check that those plasmids actually exist
     * before being certain that strain actually harbors this plasmid.
     * 
     * @param plasmid
     * @return LinkedHashSet of Strain objects.
     * @throws ManagerException
     */
    public static LinkedHashSet<Strain> getStrainsForPlasmid(Plasmid plasmid)
            throws ManagerException {
        LinkedHashSet<Strain> resultStrains = new LinkedHashSet<Strain>();
        HashSet<Long> strainIds = new HashSet<Long>();

        Set<PartNumber> plasmidPartNumbers = plasmid.getPartNumbers();
        Session session = DAO.newSession();
        try {
            for (PartNumber plasmidPartNumber : plasmidPartNumbers) {
                Query query = session
                        .createQuery("select strain.id from Strain strain where strain.plasmids like :partNumber");
                query.setString("partNumber", "%" + plasmidPartNumber.getPartNumber() + "%");
                strainIds.addAll(query.list());
            }
        } catch (HibernateException e) {
            Logger.error("Could not get strains for plasmid " + e.toString(), e);
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }

        Pattern basicWikiLinkPattern = Pattern.compile("\\[\\["
                + JbeirSettings.getSetting("WIKILINK_PREFIX") + ":.*?\\]\\]");
        Pattern partNumberPattern = Pattern.compile("\\[\\["
                + JbeirSettings.getSetting("WIKILINK_PREFIX") + ":(.*)\\]\\]");
        Pattern descriptivePattern = Pattern.compile("\\[\\["
                + JbeirSettings.getSetting("WIKILINK_PREFIX") + ":(.*)\\|(.*)\\]\\]");

        for (long strainId : strainIds) {
            Strain strain;
            try {
                strain = (Strain) EntryManager.get(strainId);
            } catch (ManagerException e) {
                throw new ManagerException("Failed retrieving strain");
            }

            String[] strainPlasmids = strain.getPlasmids().split(",");
            for (String strainPlasmid : strainPlasmids) {
                strainPlasmid = strainPlasmid.trim();
                Matcher basicWikiLinkMatcher = basicWikiLinkPattern.matcher(strainPlasmid);
                String strainPlasmidNumber = null;
                if (basicWikiLinkMatcher.matches()) {
                    Matcher partNumberMatcher = partNumberPattern.matcher(basicWikiLinkMatcher
                            .group());
                    Matcher descriptivePatternMatcher = descriptivePattern
                            .matcher(basicWikiLinkMatcher.group());

                    if (descriptivePatternMatcher.find()) {
                        strainPlasmidNumber = descriptivePatternMatcher.group(1).trim();
                    } else if (partNumberMatcher.find()) {
                        strainPlasmidNumber = partNumberMatcher.group(1).trim();
                    }

                    if (strainPlasmidNumber != null) {
                        for (PartNumber plasmidPartNumber : plasmidPartNumbers) {
                            if (plasmidPartNumber.getPartNumber().equals(strainPlasmidNumber)) {
                                resultStrains.add(strain);
                                break;
                            }
                        }
                    }

                } else {
                    if (plasmid.getPartNumbers().contains(strainPlasmid)) {
                        resultStrains.add(strain);
                    }
                }
            }
        }

        return resultStrains;
    }

    /**
     * TODO: remove.
     * 
     * @param entry
     * @param account
     * @param body
     * @return
     * @throws ManagerException
     */
    @Deprecated
    public static Comment addComment(Entry entry, Account account, String body)
            throws ManagerException {
        Comment comment = new Comment(entry, account, body);
        try {
            comment = (Comment) DAO.save(comment);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ManagerException("Could not add comment");
        }

        return comment;
    }

    /**
     * TODO: remove.
     * 
     * @param entry
     * @param account
     * @param score
     * @param comment
     * @return
     * @throws ManagerException
     */
    @Deprecated
    public static Vote addVote(Entry entry, Account account, int score, String comment)
            throws ManagerException {
        if (!((score == 1) || (score == -1))) {
            throw new ManagerException("Vote should be -1 or 1");
        }

        Vote vote = new Vote(entry, account, score, comment);
        try {
            vote = (Vote) DAO.save(vote);

        } catch (Exception e) {
            e.printStackTrace();
            throw new ManagerException("Could not add new vote");
        }

        return vote;
    }

    /**
     * TODO Remove.
     * 
     * @param entry
     * @return upVotes
     */
    @Deprecated
    public static int countUpVotes(Entry entry) {
        LinkedHashSet<Vote> votes;
        int result = 0;

        try {
            votes = getVotes(entry);
            for (Vote vote : votes) {
                if (vote.getScore() == 1) {
                    result = result + 1;
                }
            }

        } catch (ManagerException e) {
            e.printStackTrace();
            result = 0;
        }

        return result;
    }

    /**
     * TODO Remove.
     * 
     * @param entry
     * @return downVotes
     */
    @Deprecated
    public static int countDownVotes(Entry entry) {
        LinkedHashSet<Vote> votes;
        int result = 0;

        try {
            votes = getVotes(entry);

            for (Vote vote : votes) {
                if (vote.getScore() == -1) {
                    result = result + 1;
                }
            }

        } catch (ManagerException e) {
            e.printStackTrace();
            result = 0;
        }

        return result;
    }

    /**
     * TODO remove
     * 
     * @param entry
     * @return
     * @throws ManagerException
     */
    @Deprecated
    public static LinkedHashSet<Comment> getComments(Entry entry) throws ManagerException {
        LinkedHashSet<Comment> result = null;
        Session session = DAO.newSession();
        try {
            Query query = session
                    .createQuery("select comment from Comment comment where comment.entry = :entry order by comment.creationTime");
            query.setEntity("entry", entry);
            result = new LinkedHashSet<Comment>(query.list());

        } catch (Exception e) {
            throw new ManagerException("Could not get votes by account");
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }

        return result;
    }

    /**
     * TODO remove.
     * 
     * @param account
     * @return
     * @throws ManagerException
     */
    @Deprecated
    public static LinkedHashSet<Comment> getComments(Account account) throws ManagerException {
        LinkedHashSet<Comment> result = null;
        Session session = DAO.newSession();
        try {
            Query query = session
                    .createQuery("select comment from Comment comment where comment.account = :account order by comment.creationTime");
            query.setEntity("account", account);
            result = new LinkedHashSet<Comment>(query.list());

        } catch (Exception e) {
            throw new ManagerException("Could not get votes by account");
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }

        return result;
    }

    /**
     * TODO Remove
     * 
     * @param entry
     * @return
     * @throws ManagerException
     */
    @Deprecated
    public static LinkedHashSet<Vote> getVotes(Entry entry) throws ManagerException {
        LinkedHashSet<Vote> result = null;
        Session session = DAO.newSession();
        try {
            Query query = session
                    .createQuery("select vote from Vote vote where vote.entry = :entry order by vote.creationTime");
            query.setEntity("entry", entry);
            result = new LinkedHashSet<Vote>(query.list());

        } catch (Exception e) {
            throw new ManagerException("Could not get votes by entry");
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }

        return result;
    }

    /**
     * TODO Remove
     * 
     * @param account
     * @return
     * @throws ManagerException
     */
    public static LinkedHashSet<Vote> getVotes(Account account) throws ManagerException {
        LinkedHashSet<Vote> result = null;
        Session session = DAO.newSession();
        try {
            Query query = session
                    .createQuery("select vote from Vote vote where vote.account = :account order by vote.creationTime");
            query.setEntity("account", account);
            result = new LinkedHashSet<Vote>(query.list());

        } catch (Exception e) {
            throw new ManagerException("Could not get votes by account");
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }

        return result;
    }

    /**
     * TODO Remove
     * 
     * @param entry
     * @param account
     * @return
     * @throws ManagerException
     */
    public static Vote getVote(Entry entry, Account account) throws ManagerException {
        Vote vote = null;
        Session session = DAO.newSession();
        try {
            Query query = session
                    .createQuery("select vote from Vote vote where vote.account = :account vote.entry = :entry");
            query.setEntity("account", account);
            query.setEntity("entry", entry);

            vote = (Vote) query.uniqueResult();
        } catch (Exception e) {
            throw new ManagerException("Could not get vote by entry and account");
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }

        return vote;
    }

    /**
     * TODO Remove
     * 
     * @param vote
     * @param score
     * @param comment
     * @throws ManagerException
     */
    public static Vote updateVote(Vote vote, int score, String comment) throws ManagerException {
        if (!((score == 1) || (score == -1))) {
            throw new ManagerException("Invalid vote! Vote should be -1 or 1.");
        }
        vote.setScore(score);
        vote.setComment(comment);
        try {
            DAO.save(vote);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ManagerException("Update vote failed");
        }

        return vote;
    }

    /**
     * TODO Remove
     * 
     * @return
     * @throws ManagerException
     */
    public static LinkedHashSet<Entry> getMostVoted() throws ManagerException {
        LinkedHashSet<Entry> result = new LinkedHashSet<Entry>();
        Session session = DAO.newSession();
        try {
            Query query = session.createQuery(
            //TODO: Tim; redo this in proper hql
                    "select entry.id from Vote vote group by entry.id order by sum(comment.score) desc");
            @SuppressWarnings("rawtypes")
            List entries = query.list();
            for (Object item : entries) {
                Integer id = (Integer) item;
                Entry entry = EntryManager.get(id.intValue());
                result.add(entry);
            }

        } catch (Exception e) {
            throw new ManagerException("Could not get most voted");
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }

        return result;
    }

    /**
     * TODO Remove
     * 
     * @return
     * @throws ManagerException
     */
    public static LinkedHashSet<Entry> getMostCommented() throws ManagerException {
        LinkedHashSet<Entry> result = new LinkedHashSet<Entry>();
        Session session = DAO.newSession();
        try {
            Query query = session.createQuery(
            // TODO: Tim; redo this in proper hql
            // working sql query:
            // test_registry=> select entries.id, count(comments.id) from entries join comments on comments.entries_id=entries.id group by entries.id order by count(comments.id) desc;
            // This is a WORKAROUND.
                    "select entry.id from Comment comment group by entry.id order by count(comment) desc");
            @SuppressWarnings("rawtypes")
            List entries = query.list();
            for (Object item : entries) {
                Integer item2 = (Integer) item;
                Entry entry = EntryManager.get(item2.intValue());
                result.add(entry);
            }

        } catch (Exception e) {
            throw new ManagerException("Could not get most commented");
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }

        return result;
    }

    public static void main(String[] args) throws ManagerException {
        TreeSet<String> result = getUniqueSelectionMarkers();
        for (String item : result) {
            System.out.println(item);
        }
        System.out.println("" + result.size());
    }
}

package org.jbei.ice.lib.managers;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hibernate.Query;
import org.jbei.ice.lib.models.Account;
import org.jbei.ice.lib.models.Comment;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.models.PartNumber;
import org.jbei.ice.lib.models.Plasmid;
import org.jbei.ice.lib.models.Strain;
import org.jbei.ice.lib.models.Vote;
import org.jbei.ice.lib.utils.Utils;

@SuppressWarnings("unchecked")
public class UtilsManager extends Manager {
    public static TreeSet<String> getUniqueSelectionMarkers() {
        TreeSet<String> results = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
        Query query = HibernateHelper.getSession().createQuery(
                "select distinct selectionMarker.name from SelectionMarker selectionMarker");
        HashSet<String> rawMarkers = new HashSet<String>(query.list());
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

    public static TreeSet<String> getUniquePublicPlasmidNames() {
        TreeSet<String> results = new TreeSet<String>();

        Query query = HibernateHelper
                .getSession()
                .createQuery(
                        "select distinct name.name from Plasmid plasmid inner join plasmid.names as name where name.name <> '' order by name.name asc");
        HashSet<String> names = new HashSet<String>(query.list());

        for (String name : names) {
            results.add(name);
        }

        return results;
    }

    public static TreeSet<String> getUniquePromoters() {
        TreeSet<String> results = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);

        Query query = HibernateHelper.getSession().createQuery(
                "select distinct plasmid.promoters from Plasmid plasmid ");
        HashSet<String> rawPromoters = new HashSet<String>(query.list());
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

    public static TreeSet<String> getUniqueOriginOfReplications() {
        TreeSet<String> results = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);

        Query query = HibernateHelper.getSession().createQuery(
                "select distinct plasmid.originOfReplication from Plasmid plasmid ");

        HashSet<String> rawOrigins = new HashSet<String>(query.list());
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

    public static LinkedHashSet<Strain> getStrainsForPlasmid(Plasmid plasmid)
            throws ManagerException {
        LinkedHashSet<Strain> resultStrains = new LinkedHashSet<Strain>();
        HashSet<Integer> strainIds = new HashSet<Integer>();

        Set<PartNumber> partNumbers = plasmid.getPartNumbers();

        for (PartNumber partNumber : partNumbers) {
            Query query = HibernateHelper.getSession().createQuery(
                    "select strain.id from Strain strain where strain.plasmids like :partNumber");
            query.setString("partNumber", "%" + partNumber.getPartNumber() + "%");
            strainIds.addAll(query.list());
        }

        Pattern basicJbeiPattern = Pattern.compile("\\[\\[jbei:.*?\\]\\]");
        Pattern partNumberPattern = Pattern.compile("\\[\\[jbei:(.*)\\]\\]");
        Pattern descriptivePattern = Pattern.compile("\\[\\[jbei:(.*)\\|(.*)\\]\\]");

        for (int strainId : strainIds) {
            Strain strain;
            try {
                strain = (Strain) EntryManager.get(strainId);
            } catch (ManagerException e) {
                throw new ManagerException("Failed retrieving strain");
            }

            String[] strainPlasmids = strain.getPlasmids().split(",");
            for (String strainPlasmid : strainPlasmids) {
                strainPlasmid = strainPlasmid.trim();
                //Matcher jbeiLinkMatcher = basicJbeiPattern.matcher(strainPlasmid);
                Matcher basicJbeiMatcher = basicJbeiPattern.matcher(strainPlasmid);
                String strainNumber = null;
                if (basicJbeiMatcher.matches()) {
                    Matcher partNumberMatcher = partNumberPattern.matcher(basicJbeiMatcher.group());
                    Matcher descriptivePatternMatcher = descriptivePattern.matcher(basicJbeiMatcher
                            .group());

                    if (descriptivePatternMatcher.find()) {
                        strainNumber = descriptivePatternMatcher.group(1).trim();
                    } else if (partNumberMatcher.find()) {
                        strainNumber = partNumberMatcher.group(1).trim();
                    }
                    if (strainNumber != null) {
                        resultStrains.add(strain);
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

    public static Comment addComment(Entry entry, Account account, String body)
            throws ManagerException {
        Comment comment = new Comment(entry, account, body);
        try {
            comment = (Comment) dbSave(comment);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ManagerException("Could not add comment");
        }

        return comment;
    }

    public static Vote addVote(Entry entry, Account account, int score, String comment)
            throws ManagerException {
        if (!((score == 1) || (score == -1))) {
            throw new ManagerException("Vote should be -1 or 1");
        }

        Vote vote = new Vote(entry, account, score, comment);
        try {
            vote = (Vote) dbSave(vote);

        } catch (Exception e) {
            e.printStackTrace();
            throw new ManagerException("Could not add new vote");
        }

        return vote;
    }

    /**
     * Count number of up votes for an entry
     * 
     * @param entry
     * @return upVotes
     */
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
     * Count number of down votes for entry
     * 
     * @param entry
     * @return downVotes
     */
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

    public static LinkedHashSet<Comment> getComments(Entry entry) throws ManagerException {
        LinkedHashSet<Comment> result = null;
        try {
            Query query = HibernateHelper
                    .getSession()
                    .createQuery(
                            "select comment from Comment comment where comment.entry = :entry order by comment.creationTime");
            query.setEntity("entry", entry);
            result = new LinkedHashSet<Comment>(query.list());

        } catch (Exception e) {
            throw new ManagerException("Could not get votes by account");
        }

        return result;
    }

    public static LinkedHashSet<Comment> getComments(Account account) throws ManagerException {
        LinkedHashSet<Comment> result = null;
        try {
            Query query = HibernateHelper
                    .getSession()
                    .createQuery(
                            "select comment from Comment comment where comment.account = :account order by comment.creationTime");
            query.setEntity("account", account);
            result = new LinkedHashSet<Comment>(query.list());

        } catch (Exception e) {
            throw new ManagerException("Could not get votes by account");
        }

        return result;
    }

    public static LinkedHashSet<Vote> getVotes(Entry entry) throws ManagerException {
        LinkedHashSet<Vote> result = null;
        try {

            Query query = HibernateHelper
                    .getSession()
                    .createQuery(
                            "select vote from Vote vote where vote.entry = :entry order by vote.creationTime");
            query.setEntity("entry", entry);
            result = new LinkedHashSet<Vote>(query.list());

        } catch (Exception e) {
            throw new ManagerException("Could not get votes by entry");
        }

        return result;
    }

    public static LinkedHashSet<Vote> getVotes(Account account) throws ManagerException {
        LinkedHashSet<Vote> result = null;
        try {

            Query query = HibernateHelper
                    .getSession()
                    .createQuery(
                            "select vote from Vote vote where vote.account = :account order by vote.creationTime");
            query.setEntity("account", account);
            result = new LinkedHashSet<Vote>(query.list());

        } catch (Exception e) {
            throw new ManagerException("Could not get votes by account");
        }

        return result;
    }

    public static Vote getVote(Entry entry, Account account) throws ManagerException {
        Vote vote = null;
        try {
            Query query = HibernateHelper.getSession().createQuery(
                    "select vote from Vote vote where vote.account = :account vote.entry = :entry");
            query.setEntity("account", account);
            query.setEntity("entry", entry);

            vote = (Vote) query.uniqueResult();
        } catch (Exception e) {
            throw new ManagerException("Could not get vote by entry and account");
        }

        return vote;
    }

    /**
     * Updates an existing vote
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
            dbSave(vote);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ManagerException("Update vote failed");
        }

        return vote;
    }

    public static LinkedHashSet<Entry> getMostVoted() throws ManagerException {
        LinkedHashSet<Entry> result = new LinkedHashSet<Entry>();
        try {
            Query query = HibernateHelper
                    .getSession()
                    .createQuery(
                    //TODO: redo this in proper hql
                            "select entry.id from Vote vote group by entry.id order by sum(comment.score) desc");
            List entries = query.list();
            for (Object item : entries) {
                Integer id = (Integer) item;
                Entry entry = EntryManager.get(id.intValue());
                result.add(entry);
            }

        } catch (Exception e) {
            throw new ManagerException("Could not get most voted");
        }

        return result;
    }

    public static LinkedHashSet<Entry> getMostCommented() throws ManagerException {
        LinkedHashSet<Entry> result = new LinkedHashSet<Entry>();
        try {
            Query query = HibernateHelper
                    .getSession()
                    .createQuery(
                    // TODO: redo this in proper hql
                            // working sql query:
                            // test_registry=> select entries.id, count(comments.id) from entries join comments on comments.entries_id=entries.id group by entries.id order by count(comments.id) desc;
                            // This is a WORKAROUND.
                            "select entry.id from Comment comment group by entry.id order by count(comment) desc");
            List entries = query.list();
            for (Object item : entries) {
                Integer item2 = (Integer) item;
                Entry entry = EntryManager.get(item2.intValue());
                result.add(entry);
            }

        } catch (Exception e) {
            throw new ManagerException("Could not get most commented");
        }

        return result;
    }

    public static void main(String[] args) throws ManagerException {
        /*Query query = HibernateHelper.getSession().createQuery(
                "select entry from Entry entry where entry = 3810");
        //Entry entry = (Entry) query.uniqueResult();
        
        query = HibernateHelper.getSession().createQuery(
        		"select account from Account account where id = 1"
        );
        Account account = (Account) query.uniqueResult();
        
        Comment newComment = addComment(entry, account,"New comment");
        
        System.out.println("new comment " + newComment.getId() + ":" + newComment.getEntry().getId());
        
        LinkedHashSet<Comment> comments = getComments(account);
        for (Comment comment : comments) {
        	System.out.println("old comment " + comment.getId() + ":" + comment.getEntry().getId());
        	
        }
        
        
        try {
        	dbDelete(newComment);
        } catch (Exception e) {
        	e.printStackTrace();
        }*/

        //TreeSet<String> result = getUniquePlasmidNames();

        //LinkedHashSet<Strain> result = getStrainsForPlasmid(entry);

        TreeSet<String> result = getUniqueSelectionMarkers();
        for (String item : result) {
            System.out.println(item);
        }
        System.out.println("" + result.size());
    }
}

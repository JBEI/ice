package org.jbei.ice.lib.utils;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.account.AccountController;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.dao.hibernate.HibernateRepository;
import org.jbei.ice.lib.entry.EntryController;
import org.jbei.ice.lib.entry.model.PartNumber;
import org.jbei.ice.lib.entry.model.Plasmid;
import org.jbei.ice.lib.entry.model.Strain;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.models.SelectionMarker;
import org.jbei.ice.lib.permissions.PermissionException;
import org.jbei.ice.shared.dto.ConfigurationKey;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;

/**
 * Manager to deal with various objects in the database for utility purposes.
 * <p/>
 * Methods here do not clearly belong with the manipulation of objects.
 *
 * @author Hector Plahar, Timothy Ham, Zinovii Dmytriv, Joanna Chen
 */
@SuppressWarnings("unchecked")
public class UtilsDAO extends HibernateRepository {

    public Set<String> getMatchingSelectionMarkers(String token, int limit) throws DAOException {
        Session session = newSession();

        try {
            token = token.toUpperCase();
            String queryString = "select distinct selectionMarker.name from SelectionMarker selectionMarker where "
                    + " UPPER(selectionMarker.name) like '%" + token + "%'";
            Query query = session.createQuery(queryString);
            if (limit > 0)
                query.setMaxResults(limit);
            HashSet<String> results = new HashSet<String>(query.list());
            return results;
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        } finally {
            closeSession(session);
        }
    }

    /**
     * Retrieve all the unique {@link SelectionMarker}s as collection of Strings.
     * <p/>
     * This method is useful for fetching auto-complete list of selection markers.
     *
     * @return Set of selection markers.
     * @throws DAOException
     */
    public TreeSet<String> getUniqueSelectionMarkers() throws DAOException {
        TreeSet<String> results = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
//        Session session = newSession();
//        Query query = session
//                .createQuery("select distinct selectionMarker.name from SelectionMarker selectionMarker");
//        HashSet<String> rawMarkers = null;
//        try {
//            rawMarkers = new HashSet<String>(query.list());
//        } catch (HibernateException e) {
//            throw new DAOException(e);
//        } finally {
//            if (session.isOpen()) {
//                session.close();
//            }
//        }
//        /* Markers are comma separated lists, so must parse them
//        getting from the database */
//
//        for (String item : rawMarkers) {
//            if (item == null) {
//                continue;
//            }
//
//            LinkedHashSet<String> markers = Utils.toHashSetFromCommaSeparatedString(item);
//            for (String marker : markers) {
//                results.add(marker);
//            }
//        }

        return results;
    }

    /**
     * Retrieve unique, publicly visible plasmid names from the database.
     * <p/>
     * This method is useful for fetching auto-complete list of plasmid names.
     *
     * @return Set of plasmid names.
     */
    public TreeSet<String> getUniquePublicPlasmidNames() {
        TreeSet<String> results = new TreeSet<String>();
//        Session session = newSession();
//        Query query = session
//                .createQuery(
//                        "select distinct name.name from Plasmid plasmid inner join plasmid.names as name where name" +
//                                ".name <> '' order by name.name asc");
//        HashSet<String> names = new HashSet<String>();
//        try {
//            new HashSet<String>(query.list());
//        } catch (HibernateException e) {
//            Logger.error("Could not get unique public plasmid names " + e.toString(), e);
//        } finally {
//            if (session.isOpen()) {
//                session.close();
//            }
//        }
//
//        for (String name : names) {
//            results.add(name);
//        }

        return results;
    }

    /**
     * Retrieve unique promoters from the database.
     * <p/>
     * This method is useful for fetching auto-complete list of promoters.
     *
     * @return Set of promoters.
     */
    public TreeSet<String> getUniquePromoters() {
        TreeSet<String> results = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
//        Session session = newSession();
//        Query query = session
//                .createQuery("select distinct plasmid.promoters from Plasmid plasmid ");
//        HashSet<String> rawPromoters = new HashSet<String>();
//        try {
//            rawPromoters = new HashSet<String>(query.list());
//        } catch (HibernateException e) {
//            Logger.error("Could not get unique promoters " + e.toString(), e);
//        } finally {
//            closeSession(session);
//        }
//        /* Prometers are comma separated lists, so must parse them
//        getting from the database */
//
//        for (String item : rawPromoters) {
//            if (item == null) {
//                continue;
//            }
//
//            LinkedHashSet<String> promoters = Utils.toHashSetFromCommaSeparatedString(item);
//            for (String promoter : promoters) {
//                results.add(promoter);
//            }
//        }

        return results;
    }

    /**
     * Retrieve unique origin of replications from the database.
     * <p/>
     * This method is useful for fetching auto-complete list of origin of replications.
     *
     * @return Set of origin of replications.
     */
    public TreeSet<String> getUniqueOriginOfReplications() {
        TreeSet<String> results = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
//        Session session = newSession();
//        Query query = session
//                .createQuery("select distinct plasmid.originOfReplication from Plasmid plasmid ");
//
//        HashSet<String> rawOrigins = new HashSet<String>();
//
//        try {
//            rawOrigins = new HashSet<String>(query.list());
//        } catch (HibernateException e) {
//            Logger.error("Could not get unique origins of replication " + e.toString(), e);
//        } finally {
//            closeSession(session);
//        }
//        /* Origin of replications are comma separated lists, so must parse them
//        getting from the database */
//
//        for (String item : rawOrigins) {
//            if (item == null) {
//                continue;
//            }
//
//            LinkedHashSet<String> origins = Utils.toHashSetFromCommaSeparatedString(item);
//            for (String origin : origins) {
//                results.add(origin);
//            }
//        }

        return results;
    }

    /**
     * Retrieve the {@link Strain} objects associated with the given {@link Plasmid}.
     * <p/>
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
     * @throws DAOException
     */
    public static LinkedHashSet<Strain> getStrainsForPlasmid(Plasmid plasmid)
            throws DAOException {
        LinkedHashSet<Strain> resultStrains = new LinkedHashSet<Strain>();
        EntryController entryController = new EntryController();
        String wikiLink = Utils.getConfigValue(ConfigurationKey.WIKILINK_PREFIX);

        Pattern basicWikiLinkPattern = Pattern.compile("\\[\\[" + wikiLink + ":.*?\\]\\]");
        Pattern partNumberPattern = Pattern.compile("\\[\\[" + wikiLink + ":(.*)\\]\\]");
        Pattern descriptivePattern = Pattern.compile("\\[\\[" + wikiLink + ":(.*)\\|(.*)\\]\\]");

        AccountController accountController = new AccountController();
        HashSet<Long> strainIds;

        Account account;
        try {
            // TODO : temp measure till utils manager is also converted
            strainIds = entryController.retrieveStrainsForPlasmid(plasmid);
            account = accountController.getSystemAccount();
        } catch (ControllerException e) {
            Logger.error(e);
            throw new DAOException(e);
        }

        for (long strainId : strainIds) {
            Strain strain;
            try {
                strain = (Strain) entryController.get(account, strainId);
            } catch (ControllerException e) {
                throw new DAOException("Failed retrieving strain");
            } catch (PermissionException e) {
                throw new DAOException(e);
            }

            String[] strainPlasmids = strain.getPlasmids().split(",");
            for (String strainPlasmid : strainPlasmids) {
                strainPlasmid = strainPlasmid.trim();
                Matcher basicWikiLinkMatcher = basicWikiLinkPattern.matcher(strainPlasmid);
                String strainPlasmidNumber = null;
                if (basicWikiLinkMatcher.matches()) {
                    Matcher partNumberMatcher = partNumberPattern.matcher(basicWikiLinkMatcher.group());
                    Matcher descriptivePatternMatcher = descriptivePattern.matcher(basicWikiLinkMatcher.group());

                    if (descriptivePatternMatcher.find()) {
                        strainPlasmidNumber = descriptivePatternMatcher.group(1).trim();
                    } else if (partNumberMatcher.find()) {
                        strainPlasmidNumber = partNumberMatcher.group(1).trim();
                    }

                    if (strainPlasmidNumber != null) {
                        for (PartNumber plasmidPartNumber : plasmid.getPartNumbers()) {
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
}

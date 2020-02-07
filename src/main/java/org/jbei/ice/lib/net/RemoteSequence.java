package org.jbei.ice.lib.net;

import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dto.ConfigurationKey;
import org.jbei.ice.lib.dto.FeaturedDNASequence;
import org.jbei.ice.lib.entry.sequence.InputStreamWrapper;
import org.jbei.ice.lib.entry.sequence.SequenceUtil;
import org.jbei.ice.lib.entry.sequence.composers.formatters.*;
import org.jbei.ice.lib.utils.Utils;
import org.jbei.ice.services.rest.IceRestClient;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.hibernate.dao.RemotePartnerDAO;
import org.jbei.ice.storage.model.RemotePartner;
import org.jbei.ice.storage.model.Sequence;

import java.io.ByteArrayOutputStream;

/**
 * Sequence that is available remotely
 *
 * @author Hector Plahar
 */
public class RemoteSequence {

    private final RemoteContact remoteContact;
    private final RemotePartner partner;
    private final long remotePartId;

    public RemoteSequence(long remoteId, long remotePartId) {
        if (!hasRemoteAccessEnabled())
            throw new IllegalArgumentException("Not a member of web of registries");

        RemotePartnerDAO partnerDAO = DAOFactory.getRemotePartnerDAO();
        partner = partnerDAO.get(remoteId);
        if (partner == null)
            throw new IllegalArgumentException("Cannot retrieve partner with id " + remoteId);

        this.remoteContact = new RemoteContact();
        this.remotePartId = remotePartId;
    }

    /**
     * Checks if the web of registries admin config value has been set to enable this ICE instance
     * to join the web of registries configuration
     *
     * @return true if value has been set to the affirmative, false otherwise
     */
    private boolean hasRemoteAccessEnabled() {
        String value = Utils.getConfigValue(ConfigurationKey.JOIN_WEB_OF_REGISTRIES);
        return ("yes".equalsIgnoreCase(value) || "true".equalsIgnoreCase(value));
    }

    public FeaturedDNASequence getRemoteSequence() {
        try {
            IceRestClient client = new IceRestClient(partner.getUrl(), partner.getApiKey());
            String restPath = "rest/parts/" + remotePartId + "/sequence";
            return client.get(restPath, FeaturedDNASequence.class);
        } catch (Exception e) {
            Logger.error(e.getMessage());
            return null;
        }
    }

    public InputStreamWrapper get(String type) {
        FeaturedDNASequence featuredDNASequence = remoteContact.getPublicEntrySequence(partner.getUrl(),
                Long.toString(remotePartId),
                partner.getApiKey());
        if (featuredDNASequence == null)
            return new InputStreamWrapper(new byte[]{'\0'}, "no_sequence");

        String name = featuredDNASequence.getName();

        try {
            Sequence sequence = SequenceUtil.dnaSequenceToSequence(featuredDNASequence);
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            AbstractFormatter formatter;

            switch (type.toLowerCase()) {
                case "genbank":
                default:
                    name = name + ".gb";
                    formatter = new GenbankFormatter(name);
                    break;

                case "fasta":
                    formatter = new FastaFormatter();
                    break;

                case "sbol1":
                    formatter = new SBOLFormatter();
                    name = name + ".xml";
                    break;

                case "sbol2":
                    formatter = new SBOL2Formatter();
                    name = name + ".xml";
                    break;

                case "gff3":
                    formatter = new GFF3Formatter();
                    name = name + ".gff3";
                    break;
            }

            formatter.format(sequence, byteStream);
            return new InputStreamWrapper(byteStream.toByteArray(), name);
        } catch (Exception e) {
            Logger.error(e);
            return new InputStreamWrapper(new byte[]{'\0'}, "no_sequence");
        }
    }
}

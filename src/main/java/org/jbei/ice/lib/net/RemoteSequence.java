package org.jbei.ice.lib.net;

import org.apache.commons.io.IOUtils;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dto.ConfigurationKey;
import org.jbei.ice.lib.dto.FeaturedDNASequence;
import org.jbei.ice.lib.entry.sequence.ByteArrayWrapper;
import org.jbei.ice.lib.entry.sequence.SequenceController;
import org.jbei.ice.lib.entry.sequence.composers.formatters.AbstractFormatter;
import org.jbei.ice.lib.entry.sequence.composers.formatters.FastaFormatter;
import org.jbei.ice.lib.entry.sequence.composers.formatters.GenbankFormatter;
import org.jbei.ice.lib.entry.sequence.composers.formatters.SBOL1Formatter;
import org.jbei.ice.lib.entry.sequence.composers.formatters.SBOL2Formatter;
import org.jbei.ice.lib.entry.sequence.composers.pigeon.PigeonSBOLv;
import org.jbei.ice.lib.utils.Utils;
import org.jbei.ice.services.rest.IceRestClient;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.hibernate.dao.RemotePartnerDAO;
import org.jbei.ice.storage.model.RemotePartner;
import org.jbei.ice.storage.model.Sequence;

import java.io.ByteArrayOutputStream;
import java.net.URI;

/**
 * Sequence that is available remotely
 *
 * @author Hector Plahar
 */
public class RemoteSequence {

    private final RemoteContact remoteContact;
    private final RemotePartner partner;
    private final long remotePartId;
    private final IceRestClient iceRestClient;

    public RemoteSequence(long remoteId, long remotePartId) {
        if (!hasRemoteAccessEnabled())
            throw new IllegalArgumentException("Not a member of web of registries");

        RemotePartnerDAO partnerDAO = DAOFactory.getRemotePartnerDAO();
        partner = partnerDAO.get(remoteId);
        if (partner == null)
            throw new IllegalArgumentException("Cannot retrieve partner with id " + remoteId);

        this.remoteContact = new RemoteContact();
        this.remotePartId = remotePartId;
        this.iceRestClient = IceRestClient.getInstance();
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
            String restPath = "rest/parts/" + remotePartId + "/sequence";
            Object result = iceRestClient.getWor(partner.getUrl(), restPath, FeaturedDNASequence.class, null, partner.getApiKey());
            if (result == null)
                return null;

            return (FeaturedDNASequence) result;
        } catch (Exception e) {
            Logger.error(e.getMessage());
            return null;
        }
    }

    public ByteArrayWrapper get(String type) {
        FeaturedDNASequence featuredDNASequence = remoteContact.getPublicEntrySequence(partner.getUrl(), remotePartId,
                partner.getApiKey());
        if (featuredDNASequence == null)
            return new ByteArrayWrapper(new byte[]{'\0'}, "no_sequence");

        String name = featuredDNASequence.getName();

        try {
            Sequence sequence = SequenceController.dnaSequenceToSequence(featuredDNASequence);
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            AbstractFormatter formatter;

            switch (type.toLowerCase()) {
                case "genbank":
                default:
                    name = name + ".gb";
                    formatter = new GenbankFormatter(name);
                    break;

                case "fasta":
                    name = name + ".fasta";
                    formatter = new FastaFormatter(name);
                    break;

                case "sbol1":
                    formatter = new SBOL1Formatter();
                    name = name + ".xml";
                    break;

                case "sbol2":
                    formatter = new SBOL2Formatter();
                    name = name + ".xml";
                    break;

                case "pigeoni":
                    URI uri = PigeonSBOLv.generatePigeonVisual(sequence);
                    byte[] bytes = IOUtils.toByteArray(uri.toURL().openStream());
                    return new ByteArrayWrapper(bytes, name + ".png");

                case "pigeons":
                    String sequenceString = PigeonSBOLv.generatePigeonScript(sequence);
                    name = name + ".txt";
                    return new ByteArrayWrapper(sequenceString.getBytes(), name);
            }

            formatter.format(sequence, byteStream);
            return new ByteArrayWrapper(byteStream.toByteArray(), name);
        } catch (Exception e) {
            Logger.error(e);
            return new ByteArrayWrapper(new byte[]{'\0'}, "no_sequence");
        }
    }
}

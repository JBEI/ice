package org.jbei.ice.lib.composers.pigeon;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.jbei.ice.lib.composers.formatters.SBOLVisitor;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.models.Sequence;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.sbolstandard.core.DnaComponent;
import org.sbolstandard.core.SequenceAnnotation;
import org.sbolstandard.core.StrandType;

/**
 * Sends post request to pigeon to generate SBOLv
 *
 * @author Hector Plahar
 */
public class PigeonSBOLv {

    private static final String NEWLINE = System.getProperty("line.separator");
    private static final String mPigeonImageIdentifier = "Weyekin output image";
    private static final String PIGEON_URL = "http://cidar1.bu.edu:5801/pigeon1.php";
    private static final String PIGEON_URL2 = "http://cidar1.bu.edu:5801/pigeon.php";
    private static final HashMap<String, String> map = new HashMap<>();

    static {
        map.put("SO_0000001", "s,2");
        map.put("SO_0000002", "s,2");
        map.put("SO_0000005", "s,2");
        map.put("SO_0000013", "s,2");
        map.put("SO_0000019", "s,2");
        map.put("SO_0000057", "o,13");
        map.put("SO_0000104", "s,2");
        map.put("SO_0000112", "s,2");
        map.put("SO_0000139", "r,13");
        map.put("SO_0000140", "s,2");
        map.put("SO_0000141", "t,6");
        map.put("SO_0000147", "s,2");
        map.put("SO_0000149", "s,2");
        map.put("SO_0000155", "s,2");
        map.put("SO_0000165", "s,2");
        map.put("SO_0000167", "p,4");
        map.put("SO_0000172", "s,2");
        map.put("SO_0000173", "s,2");
        map.put("SO_0000174", "s,2");
        map.put("SO_0000175", "s,2");
        map.put("SO_0000176", "s,2");
        map.put("SO_0000185", "s,2");
        map.put("SO_0000185", "s,2");
        map.put("SO_0000188", "s,2");
        map.put("SO_0000204", "s,2");
        map.put("SO_0000205", "s,2");
        map.put("SO_0000233", "s,2");
        map.put("SO_0000234", "s,2");
        map.put("SO_0000252", "s,2");
        map.put("SO_0000253", "s,2");
        map.put("SO_0000274", "s,2");
        map.put("SO_0000286", "s,2");
        map.put("SO_0000296", "z,13");
        map.put("SO_0000297", "s,2");
        map.put("SO_0000298", "s,2");
        map.put("SO_0000305", "s,2");
        map.put("SO_0000316", "c,8");
        map.put("SO_0000323", "s,2");
        map.put("SO_0000324", "s,2");
        map.put("SO_0000327", "s,2");
        map.put("SO_0000331", "s,2");
        map.put("SO_0000409", "s,2");
        map.put("SO_0000410", "s,2");
        map.put("SO_0000413", "s,2");
        map.put("SO_0000417", "s,2");
        map.put("SO_0000418", "s,2");
        map.put("SO_0000419", "s,2");
        map.put("SO_0000458", "s,2");
        map.put("SO_0000470", "s,2");
        map.put("SO_0000551", "s,2");
        map.put("SO_0000552", "r,13");
        map.put("SO_0000553", "s,2");
        map.put("SO_0000555", "s,2");
        map.put("SO_0000557", "s,2");
        map.put("SO_0000627", "|,2");
        map.put("SO_0000657", "s,2");
        map.put("SO_0000704", "s,2");
        map.put("SO_0000723", "s,2");
        map.put("SO_0000725", "s,2");
        map.put("SO_0000726", "s,2");
        map.put("SO_0000856", "s,2");
        map.put("SO_0001017", "s,2");
        map.put("SO_0001023", "s,2");
        map.put("SO_0001054", "s,2");
        map.put("SO_0001060", "s,2");
        map.put("SO_0001645", "s,2");
        map.put("SO_0001687", "x,2");
        map.put("SO_0001833", "s,2");
        map.put("SO_0001834", "s,2");
        map.put("SO_0001835", "s,2");
        map.put("SO_0001836", "s,2");
        map.put("SO_0005836", "s,2");
        map.put("SO_0005850", "s,2");
    }

    public static URI generatePigeonVisual(Sequence sequence) {
        SBOLVisitor visitor = new SBOLVisitor();
        visitor.visit(sequence);

        StringBuilder sb = new StringBuilder();
        if (visitor.getDnaComponent() != null) {
            sb.append(toPigeon(visitor.getDnaComponent(), null));
        }

        sb.append("# Arcs").append(NEWLINE);
        long start = System.currentTimeMillis();
        try {
            return postToPigeon(sb.toString());
        } finally {
            Logger.info("Pigeon: " + (System.currentTimeMillis() - start) + "ms for " + sequence.getEntry().getId());
        }
    }

    public static String generatePigeonScript(Sequence sequence) {
        if (sequence == null)
            return "# Arcs";

        SBOLVisitor visitor = new SBOLVisitor();
        visitor.visit(sequence);

        StringBuilder sb = new StringBuilder();
        if (visitor.getDnaComponent() != null) {
            sb.append(toPigeon(visitor.getDnaComponent(), null));
        }

        sb.append("# Arcs").append(NEWLINE);
        return sb.toString();
    }

    private static String toPigeon(DnaComponent component, StrandType strandType) {
        if (component == null)
            return "";

        StringBuilder sb = new StringBuilder();
        if (component.getAnnotations() == null || component.getAnnotations().isEmpty()) {
            Iterator<URI> it = component.getTypes().iterator();
            if (!it.hasNext())
                return "";

            URI uri = it.next();
            String soType = uri.getPath().substring(uri.getPath().lastIndexOf("/") + 1);
            String pigeonTypeAndColor = map.get(soType);
            if (pigeonTypeAndColor != null && !pigeonTypeAndColor.isEmpty()) {
                String[] split = pigeonTypeAndColor.split(",");
                String pigeonType;
                if (strandType != null && strandType == StrandType.NEGATIVE && !split[0].equalsIgnoreCase("s")
                        && !split[0].equalsIgnoreCase("x") && !split[0].equalsIgnoreCase("z"))
                    pigeonType = "<" + split[0];
                else
                    pigeonType = split[0];
                String replacedSpaces = component.getName().replaceAll(" ", "_");
                if (replacedSpaces.trim().isEmpty())
                    replacedSpaces = "unnamed";
                sb.append(pigeonType).append(" ").append(replacedSpaces).append(" ").append(split[1]);
                sb.append(NEWLINE);
            }
        } else {
            Iterator<SequenceAnnotation> it = component.getAnnotations().iterator();
            while (it.hasNext()) {
                SequenceAnnotation sa = it.next();
                sb.append(toPigeon(sa.getSubComponent(), sa.getStrand()));
            }
        }

        return sb.toString();
    }

    public static URI postToPigeon(String pigeonScript) {
        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(PIGEON_URL);
        List<NameValuePair> attributes = new ArrayList<>();
        attributes.add(new BasicNameValuePair("desc", pigeonScript));
        String pigeonResponseString = null;

        try {
            httpPost.setEntity(new UrlEncodedFormEntity(attributes));
            HttpResponse response = httpClient.execute(httpPost);

            // response entity
            EntityUtils.consume(response.getEntity());
        } catch (Exception ex) {
            return null;
        } finally {
            httpPost.releaseConnection();
        }

        try {
            httpPost.setURI(new URI(PIGEON_URL2));
            HttpResponse response = httpClient.execute(httpPost);
            pigeonResponseString = EntityUtils.toString(response.getEntity());
        } catch (RuntimeException re) {
            throw re;
        } catch (Exception ce) {
            return null;
        } finally {
            httpPost.releaseConnection();
        }

        String[] split = pigeonResponseString.split("\n");
        for (String s : split) {
            if (s.contains(mPigeonImageIdentifier)) {
                String parsed = "http://cidar1.bu.edu:5801/"
                        + s.substring(s.indexOf("img src =") + 9, s.indexOf("alt =") - 2);
                try {
                    return new URI(parsed);
                } catch (URISyntaxException e) {
                    return null;
                }
            }
        }
        return null;
    }
}

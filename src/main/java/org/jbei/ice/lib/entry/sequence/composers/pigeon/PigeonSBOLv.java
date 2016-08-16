package org.jbei.ice.lib.entry.sequence.composers.pigeon;

import com.google.gson.Gson;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.entry.sequence.composers.formatters.SBOL1Visitor;
import org.jbei.ice.storage.model.Sequence;
import org.sbolstandard.core.DnaComponent;
import org.sbolstandard.core.SequenceAnnotation;
import org.sbolstandard.core.StrandType;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Sends post request to pigeon to generate SBOLv
 *
 * @author Hector Plahar
 */
public class PigeonSBOLv {

    private static final String NEWLINE = System.getProperty("line.separator");
    private static final String PIGEON_URL = "http://128.197.173.22:5801/dev/perch2.php";
    private static final HashMap<String, String> map = new HashMap<>();

    static {
        map.put("SO_0000001", "?, 13");
        map.put("SO_0000002", "?, 13");
        map.put("SO_0000005", "?, 13");
        map.put("SO_0000013", "?, 13");
        map.put("SO_0000019", "?, 13");
        map.put("SO_0000057", "o, 13");
        map.put("SO_0000104", "?, 13");
        map.put("SO_0000112", "?, 13");
        map.put("SO_0000139", "r, 13");
        map.put("SO_0000140", "?, 13");
        map.put("SO_0000141", "t, 6");
        map.put("SO_0000147", "?, 13");
        map.put("SO_0000149", "?, 13");
        map.put("SO_0000155", "?, 13");
        map.put("SO_0000165", "?, 13");
        map.put("SO_0000167", "p, 4");
        map.put("SO_0000172", "?, 13");
        map.put("SO_0000173", "?, 13");
        map.put("SO_0000174", "?, 13");
        map.put("SO_0000175", "?, 13");
        map.put("SO_0000176", "?, 13");
        map.put("SO_0000185", "?, 13");
        map.put("SO_0000185", "?, 13");
        map.put("SO_0000188", "?, 13");
        map.put("SO_0000204", "?, 13");
        map.put("SO_0000205", "?, 13");
        map.put("SO_0000233", "?, 13");
        map.put("SO_0000234", "?, 13");
        map.put("SO_0000252", "?, 13");
        map.put("SO_0000253", "?, 13");
        map.put("SO_0000274", "?, 13");
        map.put("SO_0000286", "?, 13");
        map.put("SO_0000296", "z, 13");
        map.put("SO_0000297", "?, 13");
        map.put("SO_0000298", "?, 13");
        map.put("SO_0000305", "?, 13");
        map.put("SO_0000316", "c, 8");
        map.put("SO_0000323", "?, 13");
        map.put("SO_0000324", "?, 13");
        map.put("SO_0000327", "?, 13");
        map.put("SO_0000331", "?, 13");
        map.put("SO_0000409", "?, 13");
        map.put("SO_0000410", "?, 13");
        map.put("SO_0000413", "?, 13");
        map.put("SO_0000417", "?, 13");
        map.put("SO_0000418", "?, 13");
        map.put("SO_0000419", "?, 13");
        map.put("SO_0000458", "?, 13");
        map.put("SO_0000470", "?, 13");
        map.put("SO_0000551", "?, 13");
        map.put("SO_0000552", "r, 13");
        map.put("SO_0000553", "?, 13");
        map.put("SO_0000555", "?, 13");
        map.put("SO_0000557", "?, 13");
        map.put("SO_0000627", "|, 2");
        map.put("SO_0000657", "?, 13");
        map.put("SO_0000704", "?, 13");
        map.put("SO_0000723", "?, 13");
        map.put("SO_0000725", "?, 13");
        map.put("SO_0000726", "?, 13");
        map.put("SO_0000856", "?, 13");
        map.put("SO_0001017", "?, 13");
        map.put("SO_0001023", "?, 13");
        map.put("SO_0001054", "?, 13");
        map.put("SO_0001060", "?, 13");
        map.put("SO_0001645", "?, 13");
        map.put("SO_0001687", "x, 2");
        map.put("SO_0001833", "?, 13");
        map.put("SO_0001834", "?, 13");
        map.put("SO_0001835", "?, 13");
        map.put("SO_0001836", "?, 13");
        map.put("SO_0005836", "?, 13");
        map.put("SO_0005850", "?, 13");
    }

    public static URI generatePigeonVisual(Sequence sequence) {
        SBOL1Visitor visitor = new SBOL1Visitor();
        visitor.visit(sequence);

        StringBuilder sb = new StringBuilder();
        if (visitor.getDnaComponent() != null) {
            sb.append(toPigeon(visitor.getDnaComponent(), null, false));
        }

        sb.append("# Arcs").append(NEWLINE);
        return postToPigeon(sb.toString());
    }

    public static String generatePigeonScript(Sequence sequence) {
        if (sequence == null)
            return "# Arcs";

        SBOL1Visitor visitor = new SBOL1Visitor();
        visitor.visit(sequence);

        StringBuilder sb = new StringBuilder();
        if (visitor.getDnaComponent() != null) {
            sb.append(toPigeon(visitor.getDnaComponent(), null, false));
        }

        sb.append("# Arcs").append(NEWLINE);
        return sb.toString();
    }

    private static String toPigeon(DnaComponent component, StrandType strandType, boolean addLabel) {
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
                if (!addLabel)
                    sb.append(" nl");
                sb.append(NEWLINE);
            }
        } else {
            for (SequenceAnnotation sa : component.getAnnotations()) {
                sb.append(toPigeon(sa.getSubComponent(), sa.getStrand(), false));
            }
        }

        return sb.toString();
    }

    public static URI postToPigeon(String pigeonScript) {
        try {
            URL obj = new URL(PIGEON_URL);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            //add request header
            con.setRequestMethod("POST");
            con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
            String urlParameters = "specification=" + pigeonScript;

            // Send post request
            con.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.writeBytes(urlParameters);
            wr.flush();
            wr.close();

//            int responseCode = con.getResponseCode();

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            StringBuilder response = new StringBuilder();

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            //print result
            String responseString = response.toString();
            PigeonImage image = new Gson().fromJson(responseString, PigeonImage.class);
            if (image.statusCode == 0 && "OK".equalsIgnoreCase(image.statusMessage)) {
                return new URI(image.getFileURL());
            } else
                Logger.error("Pigeon returned response of " + image.getStatusMessage());
        } catch (Exception e) {
            Logger.error(e);
        }
        return null;
    }

    /**
     * Object representation of the json return for the pigeon image
     */
    private static class PigeonImage {
        private int statusCode;
        private String statusMessage;
        private String fileURL;
        private String pigeonCode;

        public String getPigeonCode() {
            return pigeonCode;
        }

        public void setPigeonCode(String pigeonCode) {
            this.pigeonCode = pigeonCode;
        }

        public int getStatusCode() {
            return statusCode;
        }

        public void setStatusCode(int statusCode) {
            this.statusCode = statusCode;
        }

        public String getStatusMessage() {
            return statusMessage;
        }

        public void setStatusMessage(String statusMessage) {
            this.statusMessage = statusMessage;
        }

        public String getFileURL() {
            return fileURL;
        }

        public void setFileURL(String fileURL) {
            this.fileURL = fileURL;
        }
    }
}

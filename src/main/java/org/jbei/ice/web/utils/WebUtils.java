package org.jbei.ice.web.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.wicket.PageParameters;
import org.apache.wicket.protocol.http.WebRequestCycle;
import org.jbei.ice.lib.managers.EntryManager;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.web.pages.EntryViewPage;

public class WebUtils {

    public static String makeEntryLink(JbeiLink jbeiLink) {
        String result = null;

        CharSequence relativePath = WebRequestCycle.get().urlFor(EntryViewPage.class,
                new PageParameters());

        int id = 0;
        try {

            // BUG fails on 4008! 

            Entry entry = EntryManager.getByPartNumber(jbeiLink.getPartNumber());

            if (entry != null) {
                id = entry.getId();
            }
        } catch (ManagerException e) {
            e.printStackTrace();
        }

        String descriptiveLabel = "";
        if (jbeiLink.getDescriptiveLabel() == null) {

        } else if (jbeiLink.getDescriptiveLabel().equals("")) {

        } else {
            descriptiveLabel = jbeiLink.getDescriptiveLabel();
        }

        result = "<a href=" + relativePath + "/" + id + ">" + descriptiveLabel + "</a>";
        return result;
    }

    public static String makeEntryLink(int id) {

        String result = null;

        CharSequence relativePath = WebRequestCycle.get().urlFor(EntryViewPage.class,
                new PageParameters());
        // TODO this is not very elegant at all. Is there a better way than to generate <a> tag manually?
        try {
            result = "<a href=" + relativePath.toString() + "/" + id + ">"
                    + EntryManager.get(id).getOnePartNumber().getPartNumber() + "</a>";
        } catch (ManagerException e) {

            e.printStackTrace();
        }
        return result;
    }

    public static String parseJbeiLinks(String text) {
        String result = null;

        return result;
    }

    public static String makeEntryLinks(Collection<? extends Entry> entries) {
        String result = "";

        for (Entry entry : entries) {
            result = result + makeEntryLink(entry.getId()) + " ";
        }
        return result;
    }

    public static String jbeiLinkifyText(String text) {
        Pattern basicJbeiPattern = Pattern.compile("\\[\\[jbei:.*?\\]\\]");
        Pattern partNumberPattern = Pattern.compile("\\[\\[jbei:(.*)\\]\\]");
        Pattern descriptivePattern = Pattern.compile("\\[\\[jbei:(.*)\\|(.*)\\]\\]");

        if (text == null) {
            return text;
        }
        Matcher basicJbeiMatcher = basicJbeiPattern.matcher(text);

        ArrayList<JbeiLink> jbeiLinks = new ArrayList<JbeiLink>();
        ArrayList<Integer> starts = new ArrayList<Integer>();
        ArrayList<Integer> ends = new ArrayList<Integer>();

        while (basicJbeiMatcher.find()) {
            String partNumber = null;
            String descriptive = null;

            Matcher partNumberMatcher = partNumberPattern.matcher(basicJbeiMatcher.group());
            Matcher descriptivePatternMatcher = descriptivePattern
                    .matcher(basicJbeiMatcher.group());

            if (descriptivePatternMatcher.find()) {
                partNumber = descriptivePatternMatcher.group(1).trim();
                descriptive = descriptivePatternMatcher.group(2).trim();

            } else if (partNumberMatcher.find()) {
                partNumber = partNumberMatcher.group(1).trim();
            }

            if (partNumber != null) {
                Entry entry = null;
                try {
                    entry = EntryManager.getByPartNumber(partNumber);
                } catch (ManagerException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                if (entry != null) {
                    jbeiLinks.add(new JbeiLink(partNumber, descriptive));
                    starts.add(basicJbeiMatcher.start());
                    ends.add(basicJbeiMatcher.end());
                }
            }
        }

        String newText = new String(text);
        for (int i = jbeiLinks.size() - 1; i > -1; i = i - 1) {
            String before = newText.substring(0, starts.get(i));
            String after = newText.substring(ends.get(i));
            newText = before + makeEntryLink(jbeiLinks.get(i)) + after;
        }

        return newText;
    }

    public static class JbeiLink {
        private String descriptiveLabel = "";
        private String partNumber = "";

        public JbeiLink(String partNumber, String descriptiveLabel) {
            this.partNumber = partNumber;
            this.descriptiveLabel = descriptiveLabel;
        }

        public void setDescriptiveLabel(String descriptiveLabel) {
            this.descriptiveLabel = descriptiveLabel;
        }

        public String getDescriptiveLabel() {
            return descriptiveLabel;
        }

        public void setPartNumber(String partNumber) {
            this.partNumber = partNumber;
        }

        public String getPartNumber() {
            return partNumber;
        }
    }
}

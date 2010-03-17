package org.jbei.ice.web.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.wicket.PageParameters;
import org.apache.wicket.protocol.http.WebRequestCycle;
import org.jbei.ice.controllers.EntryController;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.permissions.PermissionException;
import org.jbei.ice.web.IceSession;
import org.jbei.ice.web.common.ViewException;
import org.jbei.ice.web.pages.EntryViewPage;

public class WebUtils {
    private static String makeEntryLink(JbeiLink jbeiLink) {
        String result = null;

        EntryController entryController = new EntryController(IceSession.get().getAccount());

        CharSequence relativePath = WebRequestCycle.get().urlFor(EntryViewPage.class,
                new PageParameters());

        int id = 0;
        Entry entry = null;

        try {
            entry = entryController.getByPartNumber(jbeiLink.getPartNumber());
        } catch (ControllerException e) {
            throw new ViewException(e);
        } catch (PermissionException e) {
            throw new ViewException(e);
        }

        if (entry != null) {
            id = entry.getId();
        }

        String descriptiveLabel = "";
        if (jbeiLink.getDescriptiveLabel() == null) {
            descriptiveLabel = jbeiLink.getPartNumber();
        } else if (jbeiLink.getDescriptiveLabel().equals("")) {
            descriptiveLabel = jbeiLink.getPartNumber();
        } else {
            descriptiveLabel = jbeiLink.getDescriptiveLabel();
        }

        result = "<a href=" + relativePath + "/" + id + ">" + descriptiveLabel + "</a>";

        return result;
    }

    private static String makeEntryLink(int id) {
        String result = null;

        EntryController entryController = new EntryController(IceSession.get().getAccount());

        CharSequence relativePath = WebRequestCycle.get().urlFor(EntryViewPage.class,
                new PageParameters());
        // TODO this is not very elegant at all. Is there a better way than to generate <a> tag manually? Yes, Tim!
        try {
            result = "<a href=" + relativePath.toString() + "/" + id + ">"
                    + entryController.get(id).getOnePartNumber().getPartNumber() + "</a>";
        } catch (ControllerException e) {
            throw new ViewException(e);
        } catch (PermissionException e) {
            throw new ViewException(e);
        }

        return result;
    }

    public static String makeEntryLinks(Collection<? extends Entry> entries) {
        StringBuilder result = new StringBuilder();

        for (Entry entry : entries) {
            result.append(makeEntryLink(entry.getId()));
            result.append(" ");
        }
        return result.toString();
    }

    public static String jbeiLinkifyText(String text) {
        String newText = "";

        try {
            EntryController entryController = new EntryController(IceSession.get().getAccount());

            Pattern basicJbeiPattern = Pattern.compile("\\[\\[jbei:.*?\\]\\]");
            Pattern partNumberPattern = Pattern.compile("\\[\\[jbei:(.*)\\]\\]");
            Pattern descriptivePattern = Pattern.compile("\\[\\[jbei:(.*)\\|(.*)\\]\\]");

            if (text == null) {
                return "";
            }
            Matcher basicJbeiMatcher = basicJbeiPattern.matcher(text);

            ArrayList<JbeiLink> jbeiLinks = new ArrayList<JbeiLink>();
            ArrayList<Integer> starts = new ArrayList<Integer>();
            ArrayList<Integer> ends = new ArrayList<Integer>();

            while (basicJbeiMatcher.find()) {
                String partNumber = null;
                String descriptive = null;

                Matcher partNumberMatcher = partNumberPattern.matcher(basicJbeiMatcher.group());
                Matcher descriptivePatternMatcher = descriptivePattern.matcher(basicJbeiMatcher
                        .group());

                if (descriptivePatternMatcher.find()) {
                    partNumber = descriptivePatternMatcher.group(1).trim();
                    descriptive = descriptivePatternMatcher.group(2).trim();

                } else if (partNumberMatcher.find()) {
                    partNumber = partNumberMatcher.group(1).trim();
                }

                if (partNumber != null) {
                    Entry entry = entryController.getByPartNumber(partNumber);

                    if (entry != null) {
                        jbeiLinks.add(new JbeiLink(partNumber, descriptive));
                        starts.add(basicJbeiMatcher.start());
                        ends.add(basicJbeiMatcher.end());
                    }
                }
            }

            newText = new String(text);
            for (int i = jbeiLinks.size() - 1; i > -1; i = i - 1) {
                String before = newText.substring(0, starts.get(i));
                String after = newText.substring(ends.get(i));
                newText = before + makeEntryLink(jbeiLinks.get(i)) + after;
            }
        } catch (Exception e) {
            return text;
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

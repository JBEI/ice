package org.jbei.ice.web.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.wicket.PageParameters;
import org.apache.wicket.protocol.http.WebRequestCycle;
import org.jbei.ice.controllers.EntryController;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.permissions.PermissionException;
import org.jbei.ice.lib.utils.JbeirSettings;
import org.jbei.ice.web.IceSession;
import org.jbei.ice.web.common.ViewException;
import org.jbei.ice.web.pages.EntryViewPage;

public class WebUtils {
    private static String makeEntryLink(JbeiLink jbeiLink) {
        String result = null;

        EntryController entryController = new EntryController(IceSession.get().getAccount());

        CharSequence relativePath = WebRequestCycle.get().urlFor(EntryViewPage.class,
            new PageParameters());

        long id = 0;
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

    private static String makeEntryLink(long id) {
        String result = "";

        EntryController entryController = new EntryController(IceSession.get().getAccount());

        CharSequence relativePath = WebRequestCycle.get().urlFor(EntryViewPage.class,
            new PageParameters());
        // TODO: Tim; this is not very elegant at all. Is there a better way than to generate <a> tag manually?
        try {
            if (entryController.hasReadPermissionById(id)) {
                result = "<a href=" + relativePath.toString() + "/" + id + ">"
                        + entryController.get(id).getOnePartNumber().getPartNumber() + "</a>";
            }
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

    public static String urlLinkifyText(String text) {

        Pattern urlPattern = Pattern.compile("\\bhttp://(\\S*)\\b");
        Pattern secureUrlPattern = Pattern.compile("\\bhttps://(\\S*)\\b");

        if (text == null) {
            return "";
        }

        class UrlLinkText {
            private String url = "";
            private int start = 0;
            private int end = 0;

            public UrlLinkText(String url, int start, int end) {
                setUrl(url);
                setStart(start);
                setEnd(end);
            }

            public String getUrl() {
                return url;
            }

            public void setUrl(String url) {
                this.url = url;
            }

            public int getStart() {
                return start;
            }

            public void setStart(int start) {
                this.start = start;
            }

            public int getEnd() {
                return end;
            }

            public void setEnd(int end) {
                this.end = end;
            }
        }

        class UrlComparator implements Comparator<UrlLinkText> {
            @Override
            public int compare(UrlLinkText o1, UrlLinkText o2) {
                if (o1.getStart() < o2.getStart()) {
                    return -1;
                } else if (o1.getStart() > o2.getStart()) {
                    return 1;
                } else {
                    return 0;
                }
            }
        }
        UrlComparator urlComparator = new UrlComparator();

        ArrayList<UrlLinkText> urls = new ArrayList<UrlLinkText>();
        Matcher urlMatcher = urlPattern.matcher(text);
        while (urlMatcher.find()) {
            urls.add(new UrlLinkText(urlMatcher.group(0).trim(), urlMatcher.start(), urlMatcher
                    .end()));
        }
        Matcher secureUrlMatcher = secureUrlPattern.matcher(text);
        while (secureUrlMatcher.find()) {
            urls.add(new UrlLinkText(secureUrlMatcher.group(0).trim(), secureUrlMatcher.start(),
                    secureUrlMatcher.end()));
        }
        Collections.sort(urls, urlComparator);
        String newText = text;
        for (int i = urls.size() - 1; i > -1; i = i - 1) {
            String before = newText.substring(0, urls.get(i).getStart());
            String after = newText.substring(urls.get(i).getEnd());
            newText = before + "<a href=" + urls.get(i).getUrl() + ">" + urls.get(i).getUrl()
                    + "</a>" + after;
        }

        return newText;
    }

    public static String linkifyText(String text) {
        String newText = wikiLinkifyText(text);
        newText = urlLinkifyText(newText);

        return newText;
    }

    private static String wikiLinkifyText(String text) {
        String newText = "";

        try {
            EntryController entryController = new EntryController(IceSession.get().getAccount());

            Pattern basicWikiLinkPattern = Pattern.compile("\\[\\["
                    + JbeirSettings.getSetting("WIKILINK_PREFIX") + ":.*?\\]\\]");
            Pattern partNumberPattern = Pattern.compile("\\[\\["
                    + JbeirSettings.getSetting("WIKILINK_PREFIX") + ":(.*)\\]\\]");
            Pattern descriptivePattern = Pattern.compile("\\[\\["
                    + JbeirSettings.getSetting("WIKILINK_PREFIX") + ":(.*)\\|(.*)\\]\\]");

            if (text == null) {
                return "";
            }
            Matcher basicWikiLinkMatcher = basicWikiLinkPattern.matcher(text);

            ArrayList<JbeiLink> jbeiLinks = new ArrayList<JbeiLink>();
            ArrayList<Integer> starts = new ArrayList<Integer>();
            ArrayList<Integer> ends = new ArrayList<Integer>();

            while (basicWikiLinkMatcher.find()) {
                String partNumber = null;
                String descriptive = null;

                Matcher partNumberMatcher = partNumberPattern.matcher(basicWikiLinkMatcher.group());
                Matcher descriptivePatternMatcher = descriptivePattern.matcher(basicWikiLinkMatcher
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
                        starts.add(basicWikiLinkMatcher.start());
                        ends.add(basicWikiLinkMatcher.end());
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

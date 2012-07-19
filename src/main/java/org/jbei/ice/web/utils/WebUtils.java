package org.jbei.ice.web.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.entry.EntryController;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.permissions.PermissionException;
import org.jbei.ice.lib.utils.JbeirSettings;

/**
 * Utility methods for web pages.
 *
 * @author Timothy Ham, Zinovii Dmytriv, Joanna Chen
 */
public class WebUtils {
    /**
     * Generate a clickable &lt;a&gt; link from the given {@link IceLink}.
     * <p/>
     * If the partnumber referred in the link does not exist, it creates a non-clickable text.
     *
     * @param iceLink link to create.
     * @return Html of the link.
     */
    private static String makeEntryLink(Account account, IceLink iceLink) throws ControllerException {
        String result;

        EntryController entryController = new EntryController();

        long id = 0;
        Entry entry;

        try {
            entry = entryController.getByPartNumber(account, iceLink.getPartNumber());
        } catch (PermissionException e) {
            throw new ControllerException(e);
        }

        if (entry != null) {
            id = entry.getId();
        }

        String descriptiveLabel = "";
        if (iceLink.getDescriptiveLabel() == null) {
            descriptiveLabel = iceLink.getPartNumber();
        } else if (iceLink.getDescriptiveLabel().equals("")) {
            descriptiveLabel = iceLink.getPartNumber();
        } else {
            descriptiveLabel = iceLink.getDescriptiveLabel();
        }

        result = "<a href=/entry/view/" + id + ">" + descriptiveLabel + "</a>";

        return result;
    }

    /**
     * Generate an html &lt;a&gt; link from the given url.
     *
     * @param text Url to linkify.
     * @return Html &lt;a&gt; link.
     */
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

    public static String linkifyText(Account account, String text) {
        String newText = wikiLinkifyText(account, text);
        newText = urlLinkifyText(newText);

        return newText;
    }

    /**
     * Generate an html &lt;a&gt; link from the given {@link IceLink} text.
     *
     * @param text IceLink text.
     * @return Html &lt;a&gt; link.
     */
    private static String wikiLinkifyText(Account account, String text) {

        String newText;

        try {
            EntryController entryController = new EntryController();

            Pattern basicWikiLinkPattern = Pattern.compile("\\[\\["
                                                                   + JbeirSettings.getSetting(
                    "WIKILINK_PREFIX") + ":.*?\\]\\]");
            Pattern partNumberPattern = Pattern.compile("\\[\\["
                                                                + JbeirSettings.getSetting(
                    "WIKILINK_PREFIX") + ":(.*)\\]\\]");
            Pattern descriptivePattern = Pattern.compile("\\[\\["
                                                                 + JbeirSettings.getSetting(
                    "WIKILINK_PREFIX") + ":(.*)\\|(.*)\\]\\]");

            if (text == null) {
                return "";
            }
            Matcher basicWikiLinkMatcher = basicWikiLinkPattern.matcher(text);

            ArrayList<IceLink> jbeiLinks = new ArrayList<IceLink>();
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
                    Entry entry = entryController.getByPartNumber(account, partNumber);

                    if (entry != null) {
                        jbeiLinks.add(new IceLink(partNumber, descriptive));
                        starts.add(basicWikiLinkMatcher.start());
                        ends.add(basicWikiLinkMatcher.end());
                    }
                }
            }

            newText = new String(text);
            for (int i = jbeiLinks.size() - 1; i > -1; i = i - 1) {
                String before = newText.substring(0, starts.get(i));
                String after = newText.substring(ends.get(i));
                newText = before + makeEntryLink(account, jbeiLinks.get(i)) + after;
            }
        } catch (Exception e) {
            Logger.error(e);
            return text;
        }

        return newText;
    }

    /**
     * Hold information about the ICE link.
     * <p/>
     * These links are modeled after wikipedia/mediawiki links. They are of the form
     * <p/>
     * [[jbei:JBx_000001]] or [[jbei:JBx_000001 | Descriptive label]] format, just like mediawiki
     * links.
     * <p/>
     * The prefix (for example "jbei") can be changed in the preferences file.
     *
     * @author Timothy Ham
     */
    public static class IceLink {
        private String descriptiveLabel = "";
        private String partNumber = "";

        /**
         * Contructor.
         *
         * @param partNumber       Part number.
         * @param descriptiveLabel Descriptive label.
         */
        public IceLink(String partNumber, String descriptiveLabel) {
            this.partNumber = partNumber;
            this.descriptiveLabel = descriptiveLabel;
        }

        /**
         * Get the descriptive label.
         *
         * @return descriptive label.
         */
        public String getDescriptiveLabel() {
            return descriptiveLabel;
        }

        /**
         * Get the part number string.
         *
         * @return Part number.
         */
        public String getPartNumber() {
            return partNumber;
        }
    }
}

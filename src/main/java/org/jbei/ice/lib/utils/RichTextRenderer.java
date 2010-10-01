package org.jbei.ice.lib.utils;

import org.eclipse.mylyn.wikitext.confluence.core.ConfluenceLanguage;
import org.eclipse.mylyn.wikitext.core.parser.MarkupParser;
import org.eclipse.mylyn.wikitext.mediawiki.core.MediaWikiLanguage;

public class RichTextRenderer {
    public static final String WIKI = "wiki";
    public static final String CONFLUENCE = "confluence";
    public static final String TEXT = "text";

    public static String wikiToHtml(String text) {
        return richTextToHtml(WIKI, text);
    }

    public static String confluenceToHtml(String text) {
        return richTextToHtml(CONFLUENCE, text);
    }

    public static String richTextToHtml(String type, String text) {
        String result = "";

        if (type == null) {
            result = text;
        }
        if (type.equals(WIKI)) {
            MarkupParser markupParser = new MarkupParser(new MediaWikiLanguage());

            result = markupParser.parseToHtml(text);
        } else if (type.equals(CONFLUENCE)) {
            MarkupParser markupParser = new MarkupParser(new ConfluenceLanguage());

            result = markupParser.parseToHtml(text);
        } else {
            result = text;
        }

        return result;
    }
}
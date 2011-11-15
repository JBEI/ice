package org.jbei.ice.lib.utils;

import org.eclipse.mylyn.wikitext.confluence.core.ConfluenceLanguage;
import org.eclipse.mylyn.wikitext.core.parser.MarkupParser;
import org.eclipse.mylyn.wikitext.mediawiki.core.MediaWikiLanguage;

/**
 * Render marked up text to html.
 * <p>
 * Currently MediaWiki, Confluence mark ups are supported. No markup is indicated as "text".
 * 
 * @author Zinovii Dmytriv, Timothy Ham, Hector Plahar
 * 
 */
public class RichTextRenderer {
    public static final String WIKI = "wiki";
    public static final String CONFLUENCE = "confluence";
    public static final String TEXT = "text";

    /**
     * Render Mediawiki syntax marked text to html.
     * 
     * @param text
     * @return Html rendering of marked up text.
     */
    public static String wikiToHtml(String text) {
        return richTextToHtml(WIKI, text);
    }

    /**
     * Render Confluence syntax marked text to html.
     * 
     * @param text
     * @return Html rendering of marked up text.
     */
    public static String confluenceToHtml(String text) {
        return richTextToHtml(CONFLUENCE, text);
    }

    /**
     * Render given text to html using the given type.
     * 
     * @param type
     *            - Markup type: Mediawiki, Confluence, or Text.
     * @param text
     *            - markup text.
     * @return Html rendering of given text.
     */
    public static String richTextToHtml(String type, String text) {
        String result = "";

        if (type == null) {
            result = text;
        }
        if (WIKI.equals(type)) {
            MarkupParser markupParser = new MarkupParser(new MediaWikiLanguage());

            result = markupParser.parseToHtml(text);
        } else if (CONFLUENCE.equals(type)) {
            MarkupParser markupParser = new MarkupParser(new ConfluenceLanguage());

            result = markupParser.parseToHtml(text);
        } else {
            result = text;
        }

        return result;
    }
}
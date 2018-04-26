/***************************************************************************************************
 * Copyright 2017 Regents of the University of California. Licensed under the Educational
 * Community License, Version 2.0 (the “license”); you may not use this file except in
 * compliance with the License. You may obtain a copy of the license at
 *
 * https://opensource.org/licenses/ECL-2.0
 *
 * Unless required under applicable law or agreed to in writing, software distributed under the
 * License is distributed in an “AS IS” BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for specific language governing permissions
 * and limitations under the license.
 **************************************************************************************************/

package tds.content.services;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.Optional;

import tds.itemrenderer.data.AccLookup;
import tds.itemrenderer.data.ITSDocument;
import tds.itemrenderer.data.xml.itemrelease.Rubriclist;
import tds.itemrenderer.data.xml.wordlist.Itemrelease;

/**
 * Handles loading the Item Documents for display
 */
public interface ContentService {
    /**
     * Loads the {@link tds.itemrenderer.data.IITSDocument} representing item document
     *
     * @param uri             the URI to the document
     * @param accommodations  the {@link tds.itemrenderer.data.AccLookup} associated with the document
     * @param contextPath     the context path of the web application that will serve the resources linked to by the its document
     * @param oggAudioSupport does browser support exists for the ogg-vorbis audio format
     * @return {@link tds.itemrenderer.data.IITSDocument}
     */
    ITSDocument loadItemDocument(final URI uri, final AccLookup accommodations, final String contextPath, final boolean oggAudioSupport);

    /**
     * Loads the {@link tds.itemrenderer.data.xml.itemrelease.Rubriclist} representing item's rubric
     *
     * @param uri             the URI to the document
     * @return {@link tds.itemrenderer.data.xml.itemrelease.Rubriclist}
     */
    Optional<Rubriclist> loadItemRubric(final URI uri);

    /**
     * Loads the resource at the specified path
     *
     * @param resourcePath The path of the resource
     * @return An {@link java.io.InputStream} of the resource data
     */
    String loadData(final URI resourcePath) throws IOException;


    /**
     * Loads the resource at the specified path
     *
     * @param resourcePath The path of the resource
     * @return An {@link java.io.InputStream} of the resource data
     */
    InputStream loadResource(final URI resourcePath) throws IOException;

    /**
     * Load the word list at the specific path
     *
     * @param uri             the URI to the wordlist
     * @param contextPath     the context path of the web application that will serve the resources linked to by the its document
     * @param oggAudioSupport does browser support exists for the ogg-vorbis audio format
     * @return An {@link tds.itemrenderer.data.xml.wordlist.Itemrelease} of the word list with its relative links processed
     */
    Itemrelease loadWordListItem(final URI uri, final String contextPath, final boolean oggAudioSupport) throws IOException;
}

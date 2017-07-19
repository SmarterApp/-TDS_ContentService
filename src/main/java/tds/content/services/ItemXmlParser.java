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

import javax.xml.bind.JAXBException;
import java.net.URI;

import tds.itemrenderer.data.ITSDocument;
import tds.itemrenderer.data.xml.wordlist.Itemrelease;

/**
 * A service responsible for parsing the {@link tds.itemrenderer.data.ITSDocument} item document object
 */
public interface ItemXmlParser {
    /**
     * Parses the item xml data at the {@link java.net.URI} provided
     *
     * @param uri The URI of the item metadata file
     * @param itemData The stringified item xml data
     * @return A mapped {@link tds.itemrenderer.data.ITSDocument} based on the fetched {@link tds.itemrenderer.data.xml.itemrelease.Itemrelease}
     */
    ITSDocument parseItemDocument(final URI uri, final String itemData);

    Itemrelease unmarshallWordListItem(final String itemData) throws JAXBException;
}

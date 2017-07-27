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

package tds.content.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.util.Collection;

import tds.common.cache.CacheType;
import tds.content.mappers.ItemDocumentMapper;
import tds.content.services.ItemXmlParser;
import tds.itemrenderer.data.ITSDocument;
import tds.itemrenderer.data.xml.itemrelease.Itemrelease;
import tds.itemrenderer.processing.ITSDocumentProcessingException;
import tds.itemrenderer.processing.ItemDataService;

@Service
public class ItemXmlParserImpl implements ItemXmlParser {
    public static final String UTF8_BOM = "\uFEFF";
    private final JAXBContext jaxbContext;
    private final JAXBContext wordListJaxbContext;
    private final Collection<ItemDocumentMapper> itsMappers;

    @Autowired
    public ItemXmlParserImpl(final JAXBContext jaxbContext, @Qualifier("wordListJaxbContext") final JAXBContext wordListJaxbContext,
                             final Collection<ItemDocumentMapper> itsMappers) {
        this.jaxbContext = jaxbContext;
        this.wordListJaxbContext = wordListJaxbContext;
        this.itsMappers = itsMappers;
    }

    @Override
    public ITSDocument parseItemDocument(final URI itemPath, final String itemData) {
        Itemrelease itemXml = unmarshallItemXml(itemPath, itemData);
        return mapItemReleaseToDocument(itemPath, itemXml);
    }

    @Override
    @Cacheable(CacheType.LONG_TERM)
    public tds.itemrenderer.data.xml.wordlist.Itemrelease unmarshallWordListItem(final String itemData) throws JAXBException {
        final Unmarshaller jaxbUnmarshaller = wordListJaxbContext.createUnmarshaller();
        final StringReader reader = removeBomIfPresent(itemData);
        return (tds.itemrenderer.data.xml.wordlist.Itemrelease) jaxbUnmarshaller.unmarshal(reader);
    }

    private ITSDocument mapItemReleaseToDocument(final URI itemPath, final Itemrelease itemXml) {
        ITSDocument document = new ITSDocument();
        document.setBaseUri(itemPath.toString());
        // Call all ItemDocumentMappers to map ITSDocument from Itemrelease
        itsMappers.forEach(mapper -> mapper.mapItemDocument(document, itemXml));
        document.setIsLoaded(true);

        // Port ITSDocumentParser.readMain() mapping logic
        return document;
    }

    private Itemrelease unmarshallItemXml(final URI itemPath, final String itemData) {
        try {
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            StringReader reader = removeBomIfPresent(itemData);
            return (Itemrelease) jaxbUnmarshaller.unmarshal(reader);
        } catch (JAXBException e) {
            String message = String.format("The XML schema was not valid for the file \"%s\"", itemPath);
            throw new ITSDocumentProcessingException (message + ":" + e.getMessage (), e);
        }
    }

    /*
        Data returned from S3 seems to include a BOM (Byte-Order-Mark) that will need to be removed before it can
        be unmarshalled.
     */
    private StringReader removeBomIfPresent(final String itemXml) {
        if (itemXml.startsWith(UTF8_BOM)) {
            return new StringReader(itemXml.substring(1));
        }
        return new StringReader(itemXml);
    }
}

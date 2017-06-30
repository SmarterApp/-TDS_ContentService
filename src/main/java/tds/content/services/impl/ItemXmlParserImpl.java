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
import org.springframework.stereotype.Service;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.util.Collection;

import tds.content.mappers.ItemDocumentMapper;
import tds.content.services.ItemXmlParser;
import tds.itemrenderer.data.ITSDocument;
import tds.itemrenderer.data.xml.itemrelease.Itemrelease;
import tds.itemrenderer.processing.ITSDocumentProcessingException;
import tds.itemrenderer.processing.ItemDataService;

@Service
public class ItemXmlParserImpl implements ItemXmlParser {
    public static final String UTF8_BOM = "\uFEFF";
    private final ItemDataService itemDataService;
    private final JAXBContext jaxbContext;
    private final Collection<ItemDocumentMapper> itsMappers;

    @Autowired
    public ItemXmlParserImpl(final ItemDataService itemDataService,
                             final JAXBContext jaxbContext,
                             final Collection<ItemDocumentMapper> itsMappers) {
        this.itemDataService = itemDataService;
        this.jaxbContext = jaxbContext;
        this.itsMappers = itsMappers;
    }

    @Override
    public ITSDocument parseItemDocument(final URI itemPath) {
        Itemrelease itemXml = unmarshallItemXml(itemPath);
        return mapItemReleaseToDocument(itemPath, itemXml);
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

    private Itemrelease unmarshallItemXml(final URI itemPath) {
        try {
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            StringReader reader = removeBomIfPresent(itemDataService.readData(itemPath));
            return (Itemrelease) jaxbUnmarshaller.unmarshal(reader);
        } catch (IOException ioe) {
            throw new ITSDocumentProcessingException(ioe);
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

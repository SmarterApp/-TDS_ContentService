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

import TDS.Shared.Security.IEncryption;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import tds.content.configuration.ContentServiceProperties;
import tds.content.services.ContentService;
import tds.content.services.ItemXmlParser;
import tds.itemrenderer.ITSDocumentFactory;
import tds.itemrenderer.apip.APIPMode;
import tds.itemrenderer.apip.APIPXmlProcessor;
import tds.itemrenderer.apip.BRFProcessor;
import tds.itemrenderer.data.AccLookup;
import tds.itemrenderer.data.AccProperties;
import tds.itemrenderer.data.ITSAttachment;
import tds.itemrenderer.data.ITSContent;
import tds.itemrenderer.data.ITSDocument;
import tds.itemrenderer.processing.ITSDocumentProcessingException;
import tds.itemrenderer.processing.ITSHtmlSanitizeTask;
import tds.itemrenderer.processing.ITSProcessorApipTasks;
import tds.itemrenderer.processing.ITSProcessorTasks;
import tds.itemrenderer.processing.ITSUrlResolver;
import tds.itemrenderer.processing.ITSUrlTask;
import tds.itemrenderer.processing.ItemDataService;

@Service
public class ContentServiceImpl implements ContentService {
    private final ItemXmlParser itemXmlParser;
    private final ItemDataService itemDataService;
    private final ContentServiceProperties properties;
    private final IEncryption encryption;

    @Autowired
    public ContentServiceImpl(final ItemXmlParser itemXmlParser,
                              final ItemDataService itemDataService,
                              final IEncryption encryption,
                              final ContentServiceProperties contentServiceProperties) {
        this.itemXmlParser = itemXmlParser;
        this.itemDataService = itemDataService;
        this.properties = contentServiceProperties;
        this.encryption = encryption;
    }

    @Override
    public ITSDocument loadItemDocument(final URI uri, final AccLookup accommodations, final String contextPath) {
        final String itemDataXml;

        try {
            itemDataXml = itemDataService.readData(uri);
        } catch (IOException e) {
            throw new ITSDocumentProcessingException(e);
        }

        ITSDocument itsDocument = itemXmlParser.parseItemDocument(uri, itemDataXml);

        // check if valid xml
        if (!itsDocument.getValidated()) {
            throw new RuntimeException(String.format("The XML schema was not valid for the file \"%s\"", uri.toString()));
        }

        // run any processing
        executeProcessing(itsDocument, accommodations, true, contextPath);

        return itsDocument;
    }

    @Override
    public InputStream loadResource(final URI resourcePath) throws IOException {
        return itemDataService.readResourceData(resourcePath);
    }

    private void executeProcessing(ITSDocument itsDocument, AccLookup accommodations, boolean resolveUrls, String contextPath) {
        // check if there are accommodations
        if (accommodations == null || accommodations == AccLookup.getNone())
            return;

        AccProperties accProperties = new AccProperties(accommodations);
        String language = accProperties.getLanguage();

        // create post processor
        ITSProcessorTasks processorTasks = new ITSProcessorTasks(language);

        // create xml based task container
        ITSProcessorApipTasks apipTasks = new ITSProcessorApipTasks();

        // if this language has accessibility then add APIP task
        ITSContent content = itsDocument.getContent(language);

        // get APIP mode
        APIPMode apipMode = ITSDocumentFactory.getAPIPMode(accProperties);

        // process APIP
        if (content != null && content.getApip() != null && apipMode != APIPMode.None) { // create APIP processor if possible
            APIPXmlProcessor apipProcessor = ITSDocumentFactory.createAPIPProcessor(apipMode, accProperties.getTTXBusinessRules());

            if (apipProcessor != null) {
                apipTasks.registerTask(apipProcessor);
            }
        }

        // process BRF
        if (apipMode == APIPMode.BRF) {
            apipTasks.registerTask(new BRFProcessor(accProperties.getTTXBusinessRules()));
        }

        if (apipTasks.getCount() > 0) {
            processorTasks.registerTask(apipTasks);
        }

        ITSUrlResolver resolver = new ITSUrlResolver(itsDocument.getBaseUri(), properties.isEncryptionEnabled(), contextPath, encryption);

        // add task for URL's
        if (resolveUrls && apipMode != APIPMode.BRF) {
            processorTasks.registerTask(new ITSUrlTask(properties.isEncryptionEnabled(), contextPath, encryption));
        }

        // add task to sanitize the html output to fix up any undesirable artifacts in the items coming from ITS
        processorTasks.registerTask(new ITSHtmlSanitizeTask(accProperties));

        // execute tasks for specific language
        processorTasks.process(itsDocument);

        // resolve attachment url's
        if (content != null && content.getAttachments() != null && content.getAttachments().size() > 0) {
            for (ITSAttachment attachment : content.getAttachments()) {
                attachment.setUrl(resolver.resolveUrl(attachment.getFile()));
            }
        }
    }
}

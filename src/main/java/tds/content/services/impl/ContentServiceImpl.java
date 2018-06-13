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
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import tds.common.cache.CacheType;
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
import tds.itemrenderer.data.xml.itemrelease.Content;
import tds.itemrenderer.data.xml.itemrelease.Rubriclist;
import tds.itemrenderer.data.xml.wordlist.Itemrelease;
import tds.itemrenderer.data.xml.wordlist.Keyword;
import tds.itemrenderer.processing.ITSDocumentProcessingException;
import tds.itemrenderer.processing.ITSHtmlSanitizeTask;
import tds.itemrenderer.processing.ITSProcessorApipTasks;
import tds.itemrenderer.processing.ITSProcessorTasks;
import tds.itemrenderer.processing.ITSUrlResolver;
import tds.itemrenderer.processing.ITSUrlResolver2;
import tds.itemrenderer.processing.ITSUrlTask;
import tds.itemrenderer.processing.ItemDataService;

@Service
public class ContentServiceImpl implements ContentService {
    private static final Logger logger = LoggerFactory.getLogger(ContentService.class);

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

    protected String readItemDataXml(final URI uri) {
        try {
            return itemDataService.readData(uri);
        } catch (IOException e) {
            throw new ITSDocumentProcessingException(e);
        }
    }

    @Override
    @Cacheable(CacheType.LONG_TERM)
    public ITSDocument loadItemDocument(final URI uri, final AccLookup accommodations, final String contextPath, final boolean oggAudioSupport) {
        final String itemDataXml = readItemDataXml(uri);

        ITSDocument itsDocument = itemXmlParser.parseItemDocument(uri, itemDataXml);

        // check if valid xml
        if (!itsDocument.getValidated()) {
            throw new RuntimeException(String.format("The XML schema was not valid for the file \"%s\"", uri.toString()));
        }

        // run any processing
        executeProcessing(itsDocument, accommodations, true, contextPath, oggAudioSupport);

        return itsDocument;
    }

    @Override
    public Optional<String> loadItemRubric(final URI uri) {
        final String itemDataXml = readItemDataXml(uri);
        try {
            final InputSource source = new InputSource(new StringReader(itemDataXml));
            // parse the XML as a W3C Document
            final DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            final Document document = builder.parse(source);
            final XPath xpath = XPathFactory.newInstance().newXPath();
            final String expression = "/itemrelease/item/content/rubriclist";
            final NodeList nodeList = (NodeList)xpath.evaluate(expression, document, XPathConstants.NODESET);
            if (nodeList.getLength() > 0) {
                final StringWriter sw = new StringWriter();
                final Transformer serializer = TransformerFactory.newInstance().newTransformer();
                serializer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
                serializer.transform(new DOMSource(nodeList.item(0)), new StreamResult(sw));
                final String rubricListXml = sw.toString();

                return Optional.of(rubricListXml);
            } else {
                return Optional.empty();
            }
        } catch (Exception e) {
            throw new ITSDocumentProcessingException(String.format("The XML schema was not valid for the rubric list \"%s\"", uri), e);
        }


    }

        @Override
    @Cacheable(CacheType.LONG_TERM)
    public String loadData(final URI resourcePath) throws IOException {
        return itemDataService.readData(resourcePath);
    }

    @Override
    public InputStream loadResource(final URI resourcePath) throws IOException {
        return itemDataService.readResourceData(resourcePath);
    }

    private void executeProcessing(ITSDocument itsDocument, AccLookup accommodations, boolean resolveUrls, String contextPath, boolean oggAudioSupport) {
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

        final ITSUrlResolver2 resolver2 = new ITSUrlResolver2(itsDocument.getBaseUri(), properties.isEncryptionEnabled(), contextPath, encryption) {
            @Override
            protected String audioSwapHack(String fileName) {
                return audioSwap(this._filePath, fileName, oggAudioSupport);
            }
        };

        // add task for URL's
        if (resolveUrls && apipMode != APIPMode.BRF) {
            processorTasks.registerTask(new ITSUrlTask(resolver2));
        }

        // add task to sanitize the html output to fix up any undesirable artifacts in the items coming from ITS
        processorTasks.registerTask(new ITSHtmlSanitizeTask(accProperties));

        // execute tasks for specific language
        processorTasks.process(itsDocument);

        // resolve attachment url's
        if (content != null && content.getAttachments() != null && content.getAttachments().size() > 0) {
            final ITSUrlResolver resolver = new ITSUrlResolver(itsDocument.getBaseUri(), properties.isEncryptionEnabled(), contextPath, encryption);
            for (ITSAttachment attachment : content.getAttachments()) {
                attachment.setUrl(resolver.resolveUrl(attachment.getFile()));
            }
        }
    }

    @Override
    @Cacheable(CacheType.LONG_TERM)
    public Itemrelease loadWordListItem(final URI uri, final String contextPath, final boolean oggAudioSupport) throws IOException {
        final String itemData = loadData(uri);
        final Itemrelease wordList;
        try {
            wordList = itemXmlParser.unmarshallWordListItem(itemData);
        } catch (JAXBException e) {
            throw new ITSDocumentProcessingException(String.format("The XML schema was not valid for the word list \"%s\"", uri), e);
        }

        processWordList(wordList, uri.toASCIIString(), contextPath, oggAudioSupport);

        return wordList;
    }

    /**
     * replaces links to the web application that will serve resources
     */
    private void processWordList(final Itemrelease itemrelease, final String baseUri, final String contextPath, final boolean oggAudioSupport) {
        // replace urls in the nested HTML member of Itemrelease object
        final Stream<Keyword> keywords = itemrelease.getItem().getKeywordList().getKeyword().stream();
        keywords
            .filter(keyword -> StringUtils.isBlank(keyword.getIndex()))
            .map(Keyword::getHtml)
            .flatMap(List::stream)
            .filter(html -> StringUtils.isBlank(html.getListType()) || StringUtils.isBlank(html.getListCode()) || isBlankHtmlContent(html.getContent()))
            .forEach(html -> {
                final ITSUrlResolver2 resolver = new ITSUrlResolver2(baseUri, properties.isEncryptionEnabled(), contextPath, encryption) {
                    @Override
                    protected String audioSwapHack(String fileName) {
                        return audioSwap(this._filePath, fileName, oggAudioSupport);
                    }
                };
                final String content = resolver.resolveResourceUrls(html.getContent());
                html.setContent(content);
            });
    }

    private static Pattern pattern = Pattern.compile("^<p[^>]*>(.+?)</p>$");

    private boolean isBlankHtmlContent(String htmlContent) {
        if (StringUtils.isBlank (htmlContent)) {
            return true;
        }
        final Matcher matcher = pattern.matcher(htmlContent.trim());
        while (matcher.find()) {
            final String tagContent = matcher.group(1).trim();
            if (tagContent.equals("") || tagContent.equals("&#xA0;")) {
                return true;
            }
        }
        return false;
    }

    // if the vorbis-ogg file format is not supported by the browser,
    // try to locate and use an m4a version of the audio file
    protected String audioSwap(final String filePath, final String fileName, final boolean oggAudioSupport) {
        if (!oggAudioSupport) {
            // filename with new extension
            final String newAudioFileName = FilenameUtils.removeExtension(fileName) + ".m4a";
            // current directory of ogg file
            final File newAudioFileDirectory = Paths.get(filePath).getParent().toFile();
            // full path of the audio file with the new m4a extension
            final String newAudioFile = new File(newAudioFileDirectory, newAudioFileName).toString();
            try {
                // only return the alternative file if it exists, otherwise return the original file
                if (itemDataService.dataExists(URI.create(newAudioFile))) {
                    return newAudioFileName;
                }
            } catch (IOException e) {
                logger.error(String.format("Exception occurred while testing existence of alternate audio file format. " +
                    "Original file: %s alternate file: %s", fileName, newAudioFileName), e);
            }
        }
        return fileName;
    }
}

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
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.net.URI;

import tds.content.configuration.ContentServiceProperties;
import tds.content.services.ContentService;
import tds.content.services.ItemXmlParser;
import tds.itemrenderer.data.AccLookup;
import tds.itemrenderer.data.ITSDocument;
import tds.itemrenderer.processing.ITSDocumentProcessingException;
import tds.itemrenderer.processing.ItemDataService;

import static io.github.benas.randombeans.api.EnhancedRandom.random;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ContentServiceImplTest {
    private ContentService contentService;

    @Mock
    private ItemXmlParser mockItemXmlParser;

    @Mock
    private ContentServiceProperties mockProperties;

    @Mock
    private ItemDataService mockItemDataService;

    @Mock
    private IEncryption mockEncryption;

    @Before
    public void setup() {
        contentService = new ContentServiceImpl(mockItemXmlParser, mockItemDataService, mockEncryption, mockProperties);
    }

    @Test(expected = RuntimeException.class)
    public void shouldThrowOnInvalidatedDocument() {
        final URI uri = random(URI.class);
        final String itemXml = random(String.class);
        final ITSDocument document = random(ITSDocument.class);
        document.setValidated(false);
        when(mockItemXmlParser.parseItemDocument(uri, itemXml)).thenReturn(document);
        contentService.loadItemDocument(uri, null, null, false);
    }

    @Test(expected = ITSDocumentProcessingException.class)
    public void shouldThrowOnIOException() throws IOException {
        final URI uri = random(URI.class);
        when(mockItemDataService.readData(uri)).thenThrow(IOException.class);
        contentService.loadItemDocument(uri, null, null, false);
    }

    @Test
    public void shouldLoadItemDocumentNullAccommodations() throws IOException {
        final URI uri = random(URI.class);
        final String itemXml = random(String.class);
        final ITSDocument document = random(ITSDocument.class);
        document.setValidated(true);
        when(mockItemDataService.readData(uri)).thenReturn(itemXml);
        when(mockItemXmlParser.parseItemDocument(uri, itemXml)).thenReturn(document);
        ITSDocument retDocument = contentService.loadItemDocument(uri, null, null, false);
        verify(mockItemXmlParser).parseItemDocument(uri, itemXml);
        verify(mockItemDataService).readData(uri);
        assertThat(retDocument).isEqualTo(document);
    }

    @Test
    public void shouldLoadItemDocumentWithAccommodations() throws IOException {
        final URI uri = random(URI.class);
        final String itemXml = random(String.class);
        final String contentUrl = random(String.class);
        final ITSDocument document = random(ITSDocument.class);
        document.setValidated(true);
        final AccLookup accLookup = random(AccLookup.class);
        when(mockItemDataService.readData(uri)).thenReturn(itemXml);
        when(mockItemXmlParser.parseItemDocument(uri, itemXml)).thenReturn(document);

        ITSDocument retDocument = contentService.loadItemDocument(uri, accLookup, contentUrl, false);

        verify(mockItemXmlParser).parseItemDocument(uri, itemXml);
        verify(mockItemDataService).readData(uri);
        assertThat(retDocument).isEqualTo(document);
    }

}

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


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.net.URI;

import tds.content.configuration.S3Properties;
import tds.content.services.ItemDocumentService;
import tds.content.services.ItemXmlParser;
import tds.itemrenderer.data.AccLookup;
import tds.itemrenderer.data.ITSDocument;

import static io.github.benas.randombeans.api.EnhancedRandom.random;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ItemDocumentServiceImplTest {
    private ItemDocumentService itemDocumentService;

    @Mock
    private ItemXmlParser mockItemXmlParser;

    @Mock
    private S3Properties mockS3Properties;

    @Before
    public void setup() {
        itemDocumentService = new ItemDocumentServiceImpl(mockItemXmlParser, mockS3Properties);
    }

    @Test(expected = RuntimeException.class)
    public void shouldThrowOnInvalidatedDocument() {
        final URI uri = random(URI.class);
        final ITSDocument document = random(ITSDocument.class);
        document.setValidated(false);
        when(mockItemXmlParser.parseItemDocument(uri)).thenReturn(document);
        itemDocumentService.loadItemDocument(uri, null);
    }

    @Test
    public void shouldLoadItemDocumentNullAccommodations() {
        final URI uri = random(URI.class);
        final ITSDocument document = random(ITSDocument.class);
        document.setValidated(true);
        when(mockItemXmlParser.parseItemDocument(uri)).thenReturn(document);
        ITSDocument retDocument = itemDocumentService.loadItemDocument(uri, null);
        verify(mockItemXmlParser).parseItemDocument(uri);
        assertThat(retDocument).isEqualTo(document);
    }

    @Test
    public void shouldLoadItemDocumenWithAccommodations() {
        final URI uri = random(URI.class);
        final ITSDocument document = random(ITSDocument.class);
        document.setValidated(true);
        final AccLookup accLookup = random(AccLookup.class);
        when(mockItemXmlParser.parseItemDocument(uri)).thenReturn(document);
        ITSDocument retDocument = itemDocumentService.loadItemDocument(uri, accLookup);
        verify(mockItemXmlParser).parseItemDocument(uri);
        assertThat(retDocument).isEqualTo(document);
    }
}

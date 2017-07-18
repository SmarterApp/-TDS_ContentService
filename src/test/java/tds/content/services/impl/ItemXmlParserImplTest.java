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

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.util.Collections;

import tds.content.services.ItemXmlParser;
import tds.itemrenderer.data.ITSDocument;
import tds.itemrenderer.data.xml.itemrelease.Itemrelease;
import tds.itemrenderer.processing.ITSDocumentProcessingException;
import tds.itemrenderer.processing.ItemDataService;

import static io.github.benas.randombeans.api.EnhancedRandom.random;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ItemXmlParserImplTest {
    private ItemXmlParser itemXmlParser;

    @Mock
    private ItemDataService mockItemDataService;

    @Mock
    private JAXBContext mockJaxbContext;

    @Before
    public void setup() {
        itemXmlParser = new ItemXmlParserImpl(mockJaxbContext, Collections.EMPTY_LIST);
    }

    @Test
    public void shouldParseItemDocument() throws JAXBException, IOException {
        final URI uri = random(URI.class);
        final String itemXml = random(String.class);
        final Itemrelease itemrelease = random(Itemrelease.class, "itemPassage");
        final Unmarshaller mockUnmarshaller = mock(Unmarshaller.class);

        when(mockJaxbContext.createUnmarshaller()).thenReturn(mockUnmarshaller);
        when(mockUnmarshaller.unmarshal(isA(StringReader.class))).thenReturn(itemrelease);
        when(mockItemDataService.readData(uri)).thenReturn("Test");

        final ITSDocument document = itemXmlParser.parseItemDocument(uri, itemXml);
        assertThat(document).isNotNull();
    }

    @Test(expected = ITSDocumentProcessingException.class)
    public void shouldThrowITSDocumentProcessingExceptionForJaxBException() throws JAXBException {
        final URI uri = random(URI.class);
        final String itemXml = random(String.class);
        when(mockJaxbContext.createUnmarshaller()).thenThrow(JAXBException.class);
        final ITSDocument document = itemXmlParser.parseItemDocument(uri, itemXml);
        assertThat(document).isNotNull();
    }
}

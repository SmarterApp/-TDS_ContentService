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

package tds.content.mappers;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;

import tds.itemrenderer.data.ITSDocument;
import tds.itemrenderer.data.xml.itemrelease.Item;
import tds.itemrenderer.data.xml.itemrelease.ItemPassage;
import tds.itemrenderer.data.xml.itemrelease.Itemrelease;
import tds.itemrenderer.data.xml.itemrelease.RendererSpec;
import tds.itemrenderer.processing.ITSDocumentProcessingException;
import tds.itemrenderer.processing.RendererSpecService;

import static io.github.benas.randombeans.api.EnhancedRandom.random;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RendererSpecItemDocumentMapperTest {
    @Mock
    private RendererSpecService mockRendererSpecService;

    @Test
    public void shouldMapRendererSpecToItemDocumentNullFilename() {
        ItemDocumentMapper rendererSpecItemDocumentMapper = new RendererSpecItemDocumentMapper(mockRendererSpecService);
        ITSDocument document = new ITSDocument();
        document.setBaseUri("/something/");
        Itemrelease itemrelease = random(Itemrelease.class, "itemPassage");
        ItemPassage itemPassage = new Item();
        RendererSpec rendererSpec = new RendererSpec();
        rendererSpec.setFilename(null);
        rendererSpec.setValue("specValue");
        itemPassage.setRendererSpec(rendererSpec);
        itemrelease.setItemPassage(itemPassage);

        rendererSpecItemDocumentMapper.mapItemDocument(document, itemrelease);
        assertThat(document.getRendererSpec()).isEqualTo("specValue");
    }

    @Test
    public void shouldMapRendererSpecToItemDocument() throws IOException {
        ItemDocumentMapper rendererSpecItemDocumentMapper = new RendererSpecItemDocumentMapper(mockRendererSpecService);
        ITSDocument document = new ITSDocument();
        document.setBaseUri("/something/");
        Itemrelease itemrelease = random(Itemrelease.class, "itemPassage");
        ItemPassage itemPassage = new Item();
        RendererSpec rendererSpec = new RendererSpec();
        rendererSpec.setFilename("myFile");
        itemPassage.setRendererSpec(rendererSpec);
        itemrelease.setItemPassage(itemPassage);

        when(mockRendererSpecService.findOne("/something/myFile")).thenReturn("<render/><spec/>");
        rendererSpecItemDocumentMapper.mapItemDocument(document, itemrelease);
        assertThat(document.getRendererSpec()).isEqualTo("<render/><spec/>");
    }

    @Test(expected = ITSDocumentProcessingException.class)
    public void shouldThrowITSDocumentExceptionForIOException() throws IOException {
        ItemDocumentMapper rendererSpecItemDocumentMapper = new RendererSpecItemDocumentMapper(mockRendererSpecService);
        ITSDocument document = new ITSDocument();
        document.setBaseUri("/something/");
        Itemrelease itemrelease = random(Itemrelease.class, "itemPassage");
        ItemPassage itemPassage = new Item();
        RendererSpec rendererSpec = new RendererSpec();
        rendererSpec.setFilename("myFile");
        itemPassage.setRendererSpec(rendererSpec);
        itemrelease.setItemPassage(itemPassage);

        when(mockRendererSpecService.findOne("/something/myFile")).thenThrow(IOException.class);
        rendererSpecItemDocumentMapper.mapItemDocument(document, itemrelease);
    }
}

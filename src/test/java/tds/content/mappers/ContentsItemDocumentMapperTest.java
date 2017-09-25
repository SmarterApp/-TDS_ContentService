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
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

import tds.itemrenderer.data.ITSDocument;
import tds.itemrenderer.data.xml.itemrelease.Content;
import tds.itemrenderer.data.xml.itemrelease.Item;
import tds.itemrenderer.data.xml.itemrelease.ItemPassage;
import tds.itemrenderer.data.xml.itemrelease.Itemrelease;

import static io.github.benas.randombeans.api.EnhancedRandom.random;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class ContentsItemDocumentMapperTest {
    @Test
    public void shouldMapContentToItemDocument() {
        ItemDocumentMapper contentItemDocumentMapper = new ContentsItemDocumentMapper();
        ITSDocument document = new ITSDocument();
        Itemrelease itemrelease = random(Itemrelease.class, "itemPassage");
        ItemPassage itemPassage = new Item();
        List<Content> content = itemPassage.getContent();
        Content enuContent = new Content();
        enuContent.setLanguage("ENU");
        enuContent.setIllustration("Illustration1");
        Content esnContent = new Content();
        esnContent.setLanguage("ESN");
        esnContent.setIllustration("Illustration2");
        content.add(enuContent);
        content.add(esnContent);
        itemrelease.setItemPassage(itemPassage);

        contentItemDocumentMapper.mapItemDocument(document, itemrelease);
        assertThat(document.getContent("ENU").getIllustration()).isEqualTo(enuContent.getIllustration());
        assertThat(document.getContents()).hasSize(2);
    }
}

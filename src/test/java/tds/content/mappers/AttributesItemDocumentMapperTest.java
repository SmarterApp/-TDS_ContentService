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

import tds.itemrenderer.data.ITSDocument;
import tds.itemrenderer.data.xml.itemrelease.Attrib;
import tds.itemrenderer.data.xml.itemrelease.Attriblist;
import tds.itemrenderer.data.xml.itemrelease.Item;
import tds.itemrenderer.data.xml.itemrelease.ItemPassage;
import tds.itemrenderer.data.xml.itemrelease.Itemrelease;

import static io.github.benas.randombeans.api.EnhancedRandom.random;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class AttributesItemDocumentMapperTest {

    @Test
    public void shouldMapAttributesToItemDocument() {
        ItemDocumentMapper attributesDocumentMapper = new AttributesItemDocumentMapper();
        ITSDocument document = new ITSDocument();
        Itemrelease itemrelease = random(Itemrelease.class, "itemPassage");
        ItemPassage itemPassage = new Item();

        Attrib attrib = new Attrib();
        attrib.setAttid("attId");
        attrib.setDesc("description");
        attrib.setName("attName");
        attrib.setVal("attVal");
        Attriblist attriblist = new Attriblist();
        attriblist.getAttrib().add(attrib);

        itemPassage.setAttriblist(attriblist);
        itemrelease.setItemPassage(itemPassage);

        attributesDocumentMapper.mapItemDocument(document, itemrelease);
        assertThat(document.getAttributeValue("attId")).isEqualTo("attVal");
    }
}

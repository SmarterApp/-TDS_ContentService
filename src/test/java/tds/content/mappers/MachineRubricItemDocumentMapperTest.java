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
import tds.itemrenderer.data.ITSMachineRubric;
import tds.itemrenderer.data.xml.itemrelease.Item;
import tds.itemrenderer.data.xml.itemrelease.ItemPassage;
import tds.itemrenderer.data.xml.itemrelease.Itemrelease;
import tds.itemrenderer.data.xml.itemrelease.MachineRubric;

import static io.github.benas.randombeans.api.EnhancedRandom.random;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class MachineRubricItemDocumentMapperTest {
    @Test
    public void shouldMapMachineRubricToItemDocument() {
        ItemDocumentMapper rubricItemDocumentMapper = new MachineRubricItemDocumentMapper();
        ITSDocument document = new ITSDocument();
        document.setBaseUri("/something/");
        Itemrelease itemrelease = random(Itemrelease.class, "itemPassage");
        ItemPassage itemPassage = new Item();
        MachineRubric machineRubric = new MachineRubric();
        machineRubric.setFilename("item-123-4567.xml");
        itemPassage.setMachineRubric(machineRubric);
        itemrelease.setItemPassage(itemPassage);

        rubricItemDocumentMapper.mapItemDocument(document, itemrelease);
        assertThat(document.getMachineRubric().getType()).isEqualTo(ITSMachineRubric.ITSMachineRubricType.Uri);
        assertThat(document.getMachineRubric().getData()).isEqualTo("file:/item-123-4567.xml");
    }
}

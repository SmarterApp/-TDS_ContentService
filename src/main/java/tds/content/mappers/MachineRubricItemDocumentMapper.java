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

import AIR.Common.Utilities.Path;
import org.springframework.stereotype.Component;

import java.io.File;

import tds.itemrenderer.data.ITSDocument;
import tds.itemrenderer.data.ITSMachineRubric;
import tds.itemrenderer.data.xml.itemrelease.ItemPassage;
import tds.itemrenderer.data.xml.itemrelease.Itemrelease;
import tds.itemrenderer.data.xml.itemrelease.MachineRubric;

@Component
public class MachineRubricItemDocumentMapper implements ItemDocumentMapper {
    @Override
    public void mapItemDocument(final ITSDocument document, final Itemrelease itemXml) {
        ItemPassage item = itemXml.getItemPassage();

        if (item.getMachineRubric () != null) {
            MachineRubric machineRubric = itemXml.getItemPassage().getMachineRubric();
            ITSMachineRubric itsMachineRubric = new ITSMachineRubric ();

            if (itemXml.getItemPassage().getMachineRubric() != null) {
                String fileName = itemXml.getItemPassage ().getMachineRubric().getFilename();

                if (fileName != null) {
                    itsMachineRubric.setType(ITSMachineRubric.ITSMachineRubricType.Uri);

                    String baseUri = document.getBaseUri ();
                    if (!baseUri.startsWith ("file:/") && !baseUri.startsWith ("ftp:/")) {
                        baseUri = new File(baseUri).toURI().toString();
                    }

                    itsMachineRubric.setData (baseUri.replace (Path.getFileName (baseUri), fileName.trim()));
                } else {
                    itsMachineRubric.setType(ITSMachineRubric.ITSMachineRubricType.Text);

                    // get the machine rubric String
                    itsMachineRubric.setData(machineRubric.getValue());
                }
            }

            document.setMachineRubric(itsMachineRubric);
        }
    }
}

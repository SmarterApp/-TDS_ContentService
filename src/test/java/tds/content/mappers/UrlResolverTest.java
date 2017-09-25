package tds.content.mappers;

import AIR.Common.Utilities.Path;
import org.apache.commons.io.FilenameUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import tds.itemrenderer.data.ITSAttachment;
import tds.itemrenderer.data.ITSContent;
import tds.itemrenderer.data.ITSDocument;
import tds.itemrenderer.processing.ITSUrlResolver;
import tds.itemrenderer.processing.ITSUrlResolver2;

import static junit.framework.TestCase.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class UrlResolverTest {
    ITSDocument document;

    @Before
    public void setup() {
        document = new ITSDocument();
        document.setBaseUri("/usr/local/tomcat/resources/tds/bank/stimuli/stim-187-3716/stim-187-3716.xml");
        final ITSContent content = new ITSContent();
        final String stem = "<p style=\"font-weight:bold; \">Water in <span id=\"passage_3716_TAG_1_BEGIN\">Space</span></p><p style=\"\">&#xA0;</p><p style=\"\">Listen to the presentation. Then answer the questions.</p><p style=\"\">&#xA0;</p><p style=\"\"><a href=\"passage_3716_v7_3716_audio.ogg\" type=\"audio/ogg\" class=\"sound_explicit\" autoplay=\"False\" visible=\"True\"></a></p><p style=\"\">&#xA0;</p><p style=\"\">“Water in <span id=\"passage_3716_TAG_3_BEGIN\">Space”</span> by NASA, from <span id=\"passage_3716_TAG_2_BEGIN\">http://www.nasa.gov/mov/178680main_028_ksnn_3-5_water_cap.mov</span><span id=\"passage_3716_TAG_4_BEGIN\">.</span> In the public domain.</p>";
        content.setStem(stem);
        content.setLanguage("ENU");

        final ITSAttachment attachment = new ITSAttachment();
        attachment.setType("ASL");
        attachment.setSubType("STEM");
        attachment.setFile("/usr/local/tomcat/resources/tds/bank/stimuli/stim-187-3716/passage_3716_ASL_STEM.MP4");
        final List<ITSAttachment> attachements = new ArrayList<>();
        attachements.add(attachment);
        content.setAttachments(attachements);
        document.addContent(content);
    }

    @Test
    public void testResolveResourceUrls() {
        final ITSUrlResolver resolver = new ITSUrlResolver2("path/file.ext", false, "/contextPath", null) {
            @Override
            protected String audioSwapHack(String fileName) {
                // filename with new extension
                return FilenameUtils.removeExtension(fileName) + ".m4a";
            }
        };
        final String content = resolver.resolveResourceUrls(document.getContents().get(0).getStem());
        assertEquals(content, "<p style=\"font-weight:bold; \">Water in <span id=\"passage_3716_TAG_1_BEGIN\">Space</span></p><p style=\"\">&#xA0;</p><p style=\"\">Listen to the presentation. Then answer the questions.</p><p style=\"\">&#xA0;</p><p style=\"\"><a href=\"/contextPath/Pages/API/Resources.axd?path=path%2F&amp;file=passage_3716_v7_3716_audio.m4a\" type=\"audio/ogg\" class=\"sound_explicit\" autoplay=\"False\" visible=\"True\"></a></p><p style=\"\">&#xA0;</p><p style=\"\">“Water in <span id=\"passage_3716_TAG_3_BEGIN\">Space”</span> by NASA, from <span id=\"passage_3716_TAG_2_BEGIN\">http://www.nasa.gov/mov/178680main_028_ksnn_3-5_water_cap.mov</span><span id=\"passage_3716_TAG_4_BEGIN\">.</span> In the public domain.</p>");
    }
}

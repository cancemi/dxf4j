package io.github.dxf4j;

import io.github.dxf4j.core.*;
import io.github.dxf4j.entity.DxfText;
import io.github.dxf4j.entity.DxfImage;
import io.github.dxf4j.handle.HandleAllocator;
import io.github.dxf4j.image.DxfImageInserter;
import io.github.dxf4j.object.DxfDictionary;
import io.github.dxf4j.object.DxfImageDef;
import io.github.dxf4j.object.DxfImageDefReactor;
import io.github.dxf4j.template.DxfTemplateEngine;
import org.junit.Test;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class DxfDocumentTest {

    private DxfDocument loadMinimal() throws IOException {
        InputStream is = getClass().getResourceAsStream("/minimal.dxf");
        assertNotNull("minimal.dxf not found in resources", is);
        return DxfDocument.read(is);
    }

    @Test
    public void testParseHeaderVars() throws IOException {
        DxfDocument doc = loadMinimal();
        assertEquals("AC1032", doc.getHeaderVar("$ACADVER"));
        assertEquals("ANSI_1252", doc.getCodepage());
        assertEquals(0x100, doc.getHandSeed());
    }

    @Test
    public void testParseSections() throws IOException {
        DxfDocument doc = loadMinimal();
        assertNotNull(doc.getSection(DxfSectionType.CLASSES));
        assertNotNull(doc.getSection(DxfSectionType.TABLES));
        assertNotNull(doc.getSection(DxfSectionType.BLOCKS));
        assertNotNull(doc.getEntitiesSection());
        assertNotNull(doc.getObjectsSection());
    }

    @Test
    public void testParseEntities() throws IOException {
        DxfDocument doc = loadMinimal();
        DxfSection entities = doc.getEntitiesSection();
        assertNotNull(entities);
        assertTrue(entities.isParsed());

        List<DxfEntity> list = entities.getEntities();
        assertEquals(3, list.size());

        // First entity: TEXT with HELLO_WORLD
        assertTrue(list.get(0) instanceof DxfText);
        DxfText text1 = (DxfText) list.get(0);
        assertEquals("HELLO_WORLD", text1.getText());
        assertEquals("A1", text1.getHandle());
        assertEquals(100.0, text1.getX(), 0.001);
        assertEquals(200.0, text1.getY(), 0.001);

        // Second entity: TEXT with FOTO_PROG
        assertTrue(list.get(1) instanceof DxfText);
        DxfText text2 = (DxfText) list.get(1);
        assertEquals("FOTO_PROG", text2.getText());

        // Third: LINE (generic)
        assertEquals("LINE", list.get(2).getEntityType());
    }

    @Test
    public void testParseObjects() throws IOException {
        DxfDocument doc = loadMinimal();
        DxfSection objects = doc.getObjectsSection();
        assertNotNull(objects);
        assertEquals(2, objects.getEntities().size());

        // Should find ACAD_IMAGE_DICT handle
        String imageDictHandle = doc.findImageDictHandle();
        assertEquals("BC6", imageDictHandle);
    }

    @Test
    public void testRoundTrip() throws IOException {
        DxfDocument doc = loadMinimal();
        String output = doc.serialize();

        // Parse again
        DxfDocument doc2 = DxfDocument.parse(output);
        assertEquals(doc.getHeaderVar("$ACADVER"), doc2.getHeaderVar("$ACADVER"));
        assertEquals(doc.getHandSeed(), doc2.getHandSeed());

        DxfSection entities = doc2.getEntitiesSection();
        assertEquals(3, entities.getEntities().size());
    }

    @Test
    public void testTemplateEngine() throws IOException {
        DxfDocument doc = loadMinimal();
        DxfTemplateEngine engine = new DxfTemplateEngine(doc);

        Map<String, String> fields = new HashMap<>();
        fields.put("HELLO_WORLD", "Mario Rossi");
        int count = engine.replaceAll(fields);

        assertTrue(count > 0);

        // Verify the TEXT entity now has "MARIO ROSSI" (uppercase)
        DxfText text = (DxfText) doc.getEntitiesSection().getEntities().get(0);
        assertEquals("MARIO ROSSI", text.getText());
    }

    @Test
    public void testHandleAllocator() throws IOException {
        DxfDocument doc = loadMinimal();
        HandleAllocator alloc = doc.createHandleAllocator();

        DxfHandle h1 = alloc.allocate();
        DxfHandle h2 = alloc.allocate();
        DxfHandle h3 = alloc.allocate();

        assertEquals(0x100, h1.toLong());
        assertEquals(0x101, h2.toLong());
        assertEquals(0x102, h3.toLong());
        assertEquals(0x103, alloc.getCurrentSeed().toLong());
    }

    @Test
    public void testSetHandSeed() throws IOException {
        DxfDocument doc = loadMinimal();
        doc.setHandSeed(0x200);
        assertEquals(0x200, doc.getHandSeed());

        // Verify round-trip
        String output = doc.serialize();
        DxfDocument doc2 = DxfDocument.parse(output);
        assertEquals(0x200, doc2.getHandSeed());
    }

    @Test
    public void testImageEntityBuild() {
        DxfHandle imageH = new DxfHandle(0x100);
        DxfHandle imgDefH = new DxfHandle(0x101);
        DxfHandle reactorH = new DxfHandle(0x102);

        DxfImage image = DxfImage.build(imageH, "1F", "0",
                100.0, 200.0, 640, 480, 500.0, imgDefH, reactorH);

        assertEquals("IMAGE", image.getEntityType());
        assertEquals("100", image.getHandle());
        assertEquals("1F", image.getOwnerHandle());
        assertEquals("101", image.getImageDefHandle());
        assertEquals("102", image.getReactorHandle());
    }

    @Test
    public void testImageDefBuild() {
        DxfHandle imgDefH = new DxfHandle(0x101);
        DxfHandle reactorH = new DxfHandle(0x102);

        DxfImageDef imageDef = DxfImageDef.build(imgDefH, "BC6", reactorH,
                "C:\\test\\image.png", 640, 480);

        assertEquals("IMAGEDEF", imageDef.getEntityType());
        assertEquals("101", imageDef.getHandle());
        assertEquals("C:\\test\\image.png", imageDef.getFilePath());
    }

    @Test
    public void testImageDefReactorBuild() {
        DxfHandle reactorH = new DxfHandle(0x102);
        DxfHandle imageH = new DxfHandle(0x100);

        DxfImageDefReactor reactor = DxfImageDefReactor.build(reactorH, imageH);

        assertEquals("IMAGEDEF_REACTOR", reactor.getEntityType());
        assertEquals("102", reactor.getHandle());
    }

    @Test
    public void testDictionaryEntries() throws IOException {
        DxfDocument doc = loadMinimal();
        String imageDictHandle = doc.findImageDictHandle();
        DxfDictionary dict = doc.findDictionary(imageDictHandle);
        assertNotNull(dict);

        // Initially empty (no image entries)
        assertTrue(dict.getEntries().isEmpty());

        // Add an entry
        dict.addEntry("test_image", "101");
        Map<String, String> entries = dict.getEntries();
        assertEquals(1, entries.size());
        assertEquals("101", entries.get("test_image"));
    }

    @Test
    public void testFindByType() throws IOException {
        DxfDocument doc = loadMinimal();
        DxfSection entities = doc.getEntitiesSection();

        List<DxfEntity> texts = entities.findByType("TEXT");
        assertEquals(2, texts.size());

        List<DxfEntity> lines = entities.findByType("LINE");
        assertEquals(1, lines.size());
    }

    @Test
    public void testFindByHandle() throws IOException {
        DxfDocument doc = loadMinimal();
        DxfSection entities = doc.getEntitiesSection();

        DxfEntity e = entities.findByHandle("A1");
        assertNotNull(e);
        assertTrue(e instanceof DxfText);
        assertEquals("HELLO_WORLD", ((DxfText) e).getText());
    }
}

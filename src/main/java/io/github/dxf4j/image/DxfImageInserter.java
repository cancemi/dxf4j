package io.github.dxf4j.image;

import io.github.dxf4j.DxfDocument;
import io.github.dxf4j.core.*;
import io.github.dxf4j.entity.DxfImage;
import io.github.dxf4j.entity.DxfText;
import io.github.dxf4j.handle.HandleAllocator;
import io.github.dxf4j.object.DxfDictionary;
import io.github.dxf4j.object.DxfImageDef;
import io.github.dxf4j.object.DxfImageDefReactor;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * High-level utility to replace TEXT placeholder entities with IMAGE entities in a DXF document.
 * <p>
 * Usage:
 * <pre>
 * DxfDocument doc = DxfDocument.read(new File("template.dxf"));
 * DxfImageInserter inserter = new DxfImageInserter(doc);
 * inserter.replaceTextWithImage("FOTO_PROG", "C:\\img\\foto.jpg", 500);
 * doc.write(new File("output.dxf"));
 * </pre>
 */
public class DxfImageInserter {

    private final DxfDocument doc;
    private final HandleAllocator allocator;

    public DxfImageInserter(DxfDocument doc) {
        this.doc = doc;
        this.allocator = doc.createHandleAllocator();
    }

    /**
     * Replace a TEXT entity containing the given placeholder text with an IMAGE entity.
     *
     * @param placeholder  the text content to search for (e.g. "FOTO_PROG")
     * @param imagePath    absolute path to the image file
     * @param displayWidth desired display width in DXF units
     * @return true if a replacement was made, false if the placeholder was not found
     * @throws IOException if the image file cannot be read
     */
    public boolean replaceTextWithImage(String placeholder, String imagePath, double displayWidth) throws IOException {
        DxfSection entities = doc.getEntitiesSection();
        if (entities == null) return false;

        // Find the TEXT entity with the placeholder
        int textIndex = -1;
        DxfText textEntity = null;
        List<DxfEntity> entityList = entities.getEntities();
        for (int i = 0; i < entityList.size(); i++) {
            DxfEntity e = entityList.get(i);
            if (e instanceof DxfText) {
                DxfText t = (DxfText) e;
                if (placeholder.equals(t.getText())) {
                    textIndex = i;
                    textEntity = t;
                    break;
                }
            }
        }

        if (textEntity == null) return false;

        // Read image dimensions
        int pixW, pixH;
        File imgFile = new File(imagePath);
        if (imgFile.exists()) {
            BufferedImage bimg = ImageIO.read(imgFile);
            pixW = bimg.getWidth();
            pixH = bimg.getHeight();
        } else {
            pixW = (int) displayWidth;
            pixH = (int) displayWidth;
        }

        // Allocate handles: IMAGE, IMAGEDEF, REACTOR
        DxfHandle imageH = allocator.allocate();
        DxfHandle imgDefH = allocator.allocate();
        DxfHandle reactorH = allocator.allocate();

        // Update $HANDSEED
        doc.setHandSeed(allocator.getCurrentSeed().toLong());

        // Extract properties from the TEXT entity
        String ownerHandle = textEntity.getOwnerHandle();
        String layer = textEntity.getLayer();
        double posX = textEntity.getX();
        double posY = textEntity.getY();

        // Build IMAGE entity
        DxfImage image = DxfImage.build(imageH, ownerHandle, layer,
                posX, posY, pixW, pixH, displayWidth, imgDefH, reactorH);

        // Replace TEXT with IMAGE in entities section
        entityList.set(textIndex, image);

        // Find or create ACAD_IMAGE_DICT
        String imageDictHandle = doc.findImageDictHandle();
        if (imageDictHandle == null) {
            imageDictHandle = "BC6"; // common default
        }

        // Build IMAGEDEF
        DxfImageDef imageDef = DxfImageDef.build(imgDefH, imageDictHandle, reactorH,
                imagePath, pixW, pixH);

        // Build IMAGEDEF_REACTOR
        DxfImageDefReactor reactor = DxfImageDefReactor.build(reactorH, imageH);

        // Add to OBJECTS section (before the end)
        DxfSection objects = doc.getObjectsSection();
        if (objects != null) {
            objects.getEntities().add(imageDef);
            objects.getEntities().add(reactor);

            // Register in ACAD_IMAGE_DICT
            DxfDictionary imageDict = doc.findDictionary(imageDictHandle);
            if (imageDict != null) {
                String imgName = imgFile.getName();
                int dotIdx = imgName.lastIndexOf('.');
                if (dotIdx > 0) imgName = imgName.substring(0, dotIdx);
                imageDict.addEntry(imgName, imgDefH.toHex());
            }
        }

        return true;
    }
}

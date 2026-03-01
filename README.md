# dxf4j

Java library to read, modify and write AutoCAD DXF files.

Designed for template-based workflows: load a DXF template created in AutoCAD, replace placeholder texts with real values, insert images in place of text markers, and save the result without losing any DXF structure.

## Features

- **Read / Write DXF**: parse and serialize DXF files (AutoCAD 2018 format, AC1032)
- **Lossless round-trip**: sections that are not directly manipulated (CLASSES, TABLES, BLOCKS) are preserved byte-for-byte
- **Template engine**: replace placeholder text values across all TEXT/MTEXT entities
- **Image insertion**: replace a TEXT placeholder with a raster IMAGE entity, including IMAGEDEF and IMAGEDEF_REACTOR objects
- **Handle management**: automatic `$HANDSEED` allocation for new entities
- **Zero dependencies**: only JUnit 4 for tests, the library itself has no external dependencies

## Requirements

- Java 8+
- Maven 3.6+ (wrapper included)

## Build

```bash
./mvnw clean package
```

Run tests:

```bash
./mvnw test
```

## Quick start

### Read and write a DXF file

```java
import io.github.dxf4j.DxfDocument;
import java.io.File;

DxfDocument doc = DxfDocument.read(new File("template.dxf"));
// ... modify document ...
doc.write(new File("output.dxf"));
```

### Replace placeholder texts

```java
import io.github.dxf4j.DxfDocument;
import io.github.dxf4j.template.DxfTemplateEngine;
import java.util.HashMap;
import java.util.Map;

DxfDocument doc = DxfDocument.read(new File("template.dxf"));

DxfTemplateEngine engine = new DxfTemplateEngine(doc);
Map<String, String> fields = new HashMap<>();
fields.put("CLIENTE_NOME", "Mario Rossi");
fields.put("POTENZA_KW", "6.0");
fields.put("DATA_PROGETTO", "01/03/2026");
engine.replaceAll(fields);

doc.write(new File("output.dxf"));
```

### Insert an image replacing a text placeholder

```java
import io.github.dxf4j.DxfDocument;
import io.github.dxf4j.image.DxfImageInserter;

DxfDocument doc = DxfDocument.read(new File("template.dxf"));

DxfImageInserter inserter = new DxfImageInserter(doc);
inserter.replaceTextWithImage("FOTO_PROG", "C:\\images\\foto.jpg", 500.0);

doc.write(new File("output.dxf"));
```

## Project structure

```
src/main/java/io/github/dxf4j/
  DxfGroupCode.java          # Atomic (code, value) pair
  DxfReader.java              # DXF parser
  DxfWriter.java              # DXF serializer
  DxfDocument.java            # Main entry point
  core/
    DxfEntity.java            # Base entity with group codes
    DxfSection.java           # Section container (parsed or raw)
    DxfSectionType.java       # Section type enum
    DxfHandle.java            # Hexadecimal handle value type
  entity/
    DxfText.java              # TEXT / MTEXT entity
    DxfImage.java             # IMAGE entity (AcDbRasterImage)
    DxfGenericEntity.java     # Passthrough for unknown entity types
  object/
    DxfDictionary.java        # DICTIONARY object
    DxfImageDef.java          # IMAGEDEF object
    DxfImageDefReactor.java   # IMAGEDEF_REACTOR object
    DxfGenericObject.java     # Passthrough for unknown object types
  handle/
    HandleAllocator.java      # $HANDSEED allocator
  image/
    DxfImageInserter.java     # High-level image insertion utility
  template/
    DxfTemplateEngine.java    # Placeholder text replacement
```

## License

[MIT](LICENSE)

import fs from "node:fs/promises";
import path from "node:path";
import { FileBlob, PresentationFile } from "@oai/artifact-tool";

const [sourcePath, outputDir] = process.argv.slice(2);
if (!sourcePath || !outputDir) throw new Error("usage: inspect_decks.mjs <pptx> <outputDir>");

await fs.mkdir(outputDir, { recursive: true });
const presentation = await PresentationFile.importPptx(await FileBlob.load(sourcePath));
const snapshot = await presentation.inspect({
  kind: "deck,slide,textbox,shape,image,table,chart,notes,layout",
  maxChars: 200000,
});
await fs.writeFile(path.join(outputDir, "inspect.ndjson"), snapshot.ndjson, "utf8");

const summary = {
  sourcePath,
  slideCount: presentation.slides.items.length,
  masterCount: presentation.masters.items.length,
  layoutCount: presentation.layouts.items.length,
  slideSize: presentation.slideSize,
  masters: presentation.masters.items.map((m) => ({ id: m.id, name: m.name })),
  layouts: presentation.layouts.items.map((l) => ({
    id: l.id,
    name: l.name,
    placeholders: l.placeholders.summary(),
  })),
};
await fs.writeFile(path.join(outputDir, "summary.json"), JSON.stringify(summary, null, 2), "utf8");

for (let index = 0; index < presentation.slides.items.length; index += 1) {
  const slide = presentation.slides.items[index];
  const png = await slide.export({ format: "png", scale: 1.5 });
  await fs.writeFile(path.join(outputDir, `slide-${String(index + 1).padStart(2, "0")}.png`), new Uint8Array(await png.arrayBuffer()));
  const layout = await slide.export({ format: "layout" });
  await fs.writeFile(path.join(outputDir, `slide-${String(index + 1).padStart(2, "0")}.layout.json`), await layout.text(), "utf8");
}

const montage = await presentation.export({ format: "webp", montage: true, scale: 1 });
await fs.writeFile(path.join(outputDir, "montage.webp"), new Uint8Array(await montage.arrayBuffer()));
console.log(JSON.stringify(summary, null, 2));

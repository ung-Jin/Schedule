import fs from "node:fs";
import path from "node:path";

const root = "C:/Users/GRS/.codex/plugins/cache/openai-primary-runtime/presentations/26.909.22227/skills/presentations";
const files = [
  "style_guidelines.md",
  "references/clarification-questions.md",
  "references/template-elicitation.md",
  "references/implementation.md",
  "artifact_tool_docs/API_QUICK_START.md",
  "references/cover_art_direction.md",
  "references/finalization.md",
];

for (const file of files) {
  process.stdout.write(`\n===== ${file} =====\n`);
  process.stdout.write(fs.readFileSync(path.join(root, file), "utf8"));
}

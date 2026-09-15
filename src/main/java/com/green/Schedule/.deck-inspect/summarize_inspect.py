import json
import sys
from collections import defaultdict
from pathlib import Path

source = Path(sys.argv[1])
slides = defaultdict(lambda: {"title": "", "texts": [], "notes": ""})
with source.open("r", encoding="utf-8") as handle:
    for line in handle:
        record = json.loads(line)
        number = record.get("slide")
        if not number:
            continue
        if record.get("kind") == "slide":
            slides[number]["title"] = record.get("title", "")
        elif record.get("kind") == "textbox":
            text = record.get("text", "").strip()
            if text:
                slides[number]["texts"].append((record.get("id", ""), text))
        elif record.get("kind") == "notes":
            slides[number]["notes"] = record.get("text", "").strip()

for number in sorted(slides):
    print(f"\n===== SLIDE {number}: {slides[number]['title']} =====")
    for anchor, text in slides[number]["texts"]:
        print(f"{anchor}\t{text.replace(chr(10), ' | ')}")
    if slides[number]["notes"]:
        print("[NOTES]", slides[number]["notes"].replace("\n", " | "))

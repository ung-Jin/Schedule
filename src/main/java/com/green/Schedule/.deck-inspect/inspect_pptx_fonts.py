import re
import sys
import zipfile
from collections import Counter

fonts = Counter()
with zipfile.ZipFile(sys.argv[1]) as archive:
    for name in archive.namelist():
        if name.startswith("ppt/") and name.endswith(".xml"):
            text = archive.read(name).decode("utf-8", errors="ignore")
            fonts.update(re.findall(r'typeface="([^"]+)"', text))

for font, count in fonts.most_common():
    print(f"{count}\t{font}")

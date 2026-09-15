from pathlib import Path
import shutil

source = Path(r"\\Grx-ner-403-mas\02-공유폴더\404호\프로젝트 진행 참고 자료\이전 기수 프로젝트 ppt\[별첨2-2] 팀별 프로젝트 결과보고서.pptx")
destination = Path(r"D:\01-STUDY\dev-01\workspace_spring\Schedule\src\main\java\com\green\Schedule\reference_guide.pptx")

print(f"source={source}")
print(f"exists={source.exists()} file={source.is_file()}")
if source.is_file():
    shutil.copyfile(source, destination)
    print(f"copied={destination} size={destination.stat().st_size}")

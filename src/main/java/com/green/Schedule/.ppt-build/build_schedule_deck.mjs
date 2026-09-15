import fs from "node:fs/promises";
import path from "node:path";
import { pathToFileURL } from "node:url";
import { FileBlob, PresentationFile } from "@oai/artifact-tool";

const workspaceDir = "D:\\01-STUDY\\dev-01\\workspace_spring\\Schedule\\src\\main\\java\\com\\green\\Schedule";
const sourcePath = "D:\\01-STUDY\\dev-01\\workspace_spring\\Schedule\\Schedule-발표.pptx";
const assetDir = path.join(workspaceDir, ".deck-assets");
const stagingDir = path.join(workspaceDir, ".ppt-build", "finalizer");
const outputDir = path.join(workspaceDir, "output");
const finalPath = path.join(outputDir, "결과보고서_Schedule_15분_초안_20260915_v2.pptx");
const skillDir = "C:\\Users\\GRS\\.codex\\plugins\\cache\\openai-primary-runtime\\presentations\\26.909.22227\\skills\\presentations";
const pythonExecutable = "C:\\Users\\GRS\\.cache\\codex-runtimes\\codex-primary-runtime\\dependencies\\python\\python.exe";

await fs.mkdir(stagingDir, { recursive: true });
await fs.mkdir(outputDir, { recursive: true });

const presentation = await PresentationFile.importPptx(await FileBlob.load(sourcePath));

function setText(anchor, value) {
  const shape = presentation.resolve(anchor);
  if (!shape) throw new Error(`Missing shape ${anchor}`);
  shape.text = value;
}

const replacements = {
  // 1. 표지
  "sh/dgzetcfa": "K-Digital Training 팀 프로젝트",
  "sh/0bmd476t": "TEAM [조/팀명 입력] · 4인",
  "sh/nydczmpk": "[훈련기관명 입력] | 팀원: [팀장명], 최준영, 김성준, 박성인 | 멘토: [입력 필요]",
  "sh/432dwn6l": "[발표자 입력]",
  "sh/uxgjq907": "팀 공동 결과보고",
  "sh/wzy1szid": "2026.09.08 —",
  "sh/h072l4jy": "2026.09.15",
  "sh/43y1gji9": "화면 단위 풀스택 분업",

  // 2. 목차
  "sh/cnupgny5": "주제·목적·구조·기대효과",
  "sh/s72xofmh": "선정 배경 및 차별점",
  "sh/76twva5c": "기존 방식과 무엇이 다른지",
  "sh/fadgzu58": "주도적으로 맡은 기능",
  "sh/4zmxwzmp": "프로젝트 수행 절차 및 방법",
  "sh/mlgvq147": "기획·개발·통합 과정",
  "sh/onydsbmd": "프로젝트 수행 경과",
  "sh/9o7ulg3y": "단계별 구현과 피드백",
  "sh/wbidwb29": "핵심 기능 및 결과 화면",
  "sh/xcrupg3u": "실제 구동 화면 중심",
  "sh/69wbilcj": "실무 활용 가능성",
  "sh/exgvmlcv": "달성도·완성도·개선점",

  // 3. 프로젝트 개요
  "sh/v6l4jq94": "고객 신청부터 만족도 통계까지 하나의 데이터 흐름으로",
  "sh/w7ulsvqp": "Schedule은 냉난방기기 AS 업무를 고객, 관리자, 기사가 함께 처리하는 웹 시스템입니다. 신청, 기사 배정, 방문 결과, 고객 만족도를 하나의 서비스와 데이터베이스에서 연결했습니다.",
  "sh/tgry9ofu": "2026.09.08 ~ 09.15",
  "sh/hwzihwzi": "고객부터 다시 고객에게 돌아오는 처리 흐름",
  "sh/po3ilcvm": "고객 신청",
  "sh/ds3ipcvy": "관리자 배정",
  "sh/1wnitwvu": "기사 처리·보고",
  "sh/w3it8zit": "SMS 설문·통계",

  // 4. 배경과 차별점
  "sh/jadsz2xk": "01   프로젝트 개요 · 선정 배경과 차별점",
  "sh/w72947yt": "전화와 메모로 흩어진 AS 업무를 역할별 화면으로 연결",
  "sh/3ytsrmpw": "신청·배정·방문 결과가 서로 다른 채널에 흩어져 처리 이력을 찾기 어려움",
  "sh/1wbapc7q": "관리자가 기사별 일정과 접수 상태를 동시에 확인하기 어려움",
  "sh/fu9sn2p0": "기사는 자신의 오늘 일정과 월간 일정을 별도로 전달받아야 함",
  "sh/tsrqlc7u": "서비스 종료 뒤 고객 피드백이 운영 데이터로 이어지지 않음",
  "sh/knex8j21": "특화 포인트와 차별점",
  "sh/5ony1oj6": "고객, 관리자, 기사 화면을 역할별로 분리하고 하나의 AS 상태값과 일정 데이터로 연결했습니다.",
  "sh/vy5gbe14": "기존 유사 방식과의 차이",
  "sh/wzex4z2p": "·  접수부터 결과보고까지 한 시스템에서 추적",
  "sh/xknyx4ja": "·  관리자·기사 모두 월간 캘린더로 일정 확인",
  "sh/mtcfypkn": "·  결과 등록 후 SMS 설문 링크 자동 발송",
  "sh/nulw7u18": "·  고객 응답을 기사별 월간 통계로 즉시 집계",
  "sh/fm5c3q54": "핵심 목적: 일정 누락과 전달 비용을 줄이고 처리 이력을 남기는 것",

  // 5. 팀 구성
  "sh/v2twb6dw": "02   프로젝트 팀 구성 및 역할",
  "sh/k7mxovud": "화면 단위로 맡아 백엔드와 화면을 함께 완성",
  "sh/tcfel0vu": "장",
  "sh/hgrmpwj2": "[팀장명 입력]",
  "sh/ud0ne10b": "메인·로그인, 공통 레이아웃, 역할별 화면 분기, 전체 통합",
  "sh/5kn2hszu": "FullCalendar 출력, 미배정 접수 조회, 기사·시간 배정",
  "sh/3md0bu94": "오늘·월간 일정, 결과보고·사진, SMS 설문, 만족도 통계",
  "sh/bqx0fe9g": "멘토 지원 내역",
  "sh/qp4jm98v": "[확인 필요] 멘토의 주제 선정 피드백, 질의응답, 중간 점검 내용을 발표 전 입력합니다. 팀은 화면 단위 분업으로 각 담당자가 Controller부터 화면까지 주도했습니다.",

  // 6. 수행 절차
  "sh/u1kbu1ov": "03   프로젝트 수행 절차 및 방법",
  "sh/5svutgni": "사전 기획, 기능 연결, 피드백 반영, 실환경 테스트 순으로 진행",
  "sh/svmt4v6t": "09.08",
  "sh/cbe5g3ih": "기획·역할 분담",
  "sh/dcnm98zm": "주제 확정, 역할 3종과 화면 범위 정의, Git 전략 합의",
  "sh/bq547yhw": "09.09",
  "sh/0fy5k3id": "DB·화면 뼈대",
  "sh/lg76d8zy": "핵심 테이블 설계, 로그인·공통 화면, 결과보고 초안",
  "sh/zep4byhs": "09.10~11",
  "sh/gju58ji9": "핵심 기능 연결",
  "sh/1k3mhozu": "기사 일정, 결과보고, SMS, 자체 만족도 설문까지 연결",
  "sh/idk7ehkr": "09.14",
  "sh/50bqpcj2": "피드백·품질 개선",
  "sh/4f2pgr2h": "기사번호 오류 수정, 통계 추가, 전 화면 디자인 통합",
  "sh/u907axkf": "09.15",
  "sh/hcrqlcj6": "통합·검증",
  "sh/wbipc72l": "더미 데이터로 전체 흐름 점검, 빌드·자동 테스트 통과",

  // 7. 개발 환경
  "sh/cbq1sv25": "03   프로젝트 수행 절차 및 방법 · 개발 환경",
  "sh/n6x0fqlo": "교육 과정의 MVC·DB·비동기 처리 내용을 실제 업무 흐름에 적용",
  "sh/id4ju1oz": "서버·언어",
  "sh/4fm1wb6p": "Java 17",
  "sh/50v2pgna": "Spring Boot 4.0.8",
  "sh/d4f2t0nm": "화면·일정",
  "sh/f6xkvq5c": "Thymeleaf · JavaScript",
  "sh/p0zmlk7u": "FullCalendar 6.1.11",
  "sh/x4j6p47q": "데이터 계층",
  "sh/b2h4nupk": "MariaDB",
  "sh/a18nup8z": "MyBatis · log4jdbc",
  "sh/honq18ru": "외부 연동",
  "sh/vm58zi94": "Solapi SMS",
  "sh/8jupo3ad": "Cloudflare Tunnel",
  "sh/sfy907ah": "개발 도구",
  "sh/na9sz6tk": "IntelliJ IDEA",
  "sh/290bq1cz": "Gradle · Lombok",

  // 8. 수행 경과
  "sh/n6pwfmd8": "04   프로젝트 수행 경과",
  "sh/0jydkreh": "고객 신청부터 고객 피드백까지 핵심 흐름을 구현",
  "sh/zipwbmdc": "현재 저장소와 실제 구동 화면을 기준으로 정리했습니다.",
  "sh/lgbepgvm": "공통·인증",
  "sh/tw7e9gnq": "[팀장명]",
  "sh/svydgb65": "로그인·회원가입, 역할별 접근 제어, 공통 레이아웃",
  "sh/kredcr6t": "고객 신청",
  "sh/jq5w365o": "[담당자 확인]",
  "sh/i5wva1o3": "신청·중복 확인·내역·본인 상세 조회",
  "sh/na1sb2lg": "관리자 운영",
  "sh/8bat47ml": "최준영·김성준",
  "sh/9w3uds36": "통계·기사관리, 캘린더·기사/시간 배정",
  "sh/z61s7il4": "완료",
  "sh/l8ja9s3a": "기사 현장",
  "sh/q18bad4n": "박성인",
  "sh/b2hs3il8": "오늘·월간 일정, 결과 등록·수정·사진 2장",
  "sh/4vap8j69": "고객 피드백",
  "sh/fq1obeps": "박성인",
  "sh/up872987": "SMS 설문, 3항목 평가, 월별 통계·전년 비교",
  "sh/72ho7upg": "DB 6개 테이블: MEMBER · ENGINEER · REQUEST · AS_SCHEDULE · AS_RESULT · SATISFACTION",

  // 9. 구동 화면
  "sh/3y107axw": "04   프로젝트 수행 경과 · 핵심 결과",
  "sh/cnu1kzyd": "고객 신청, 관리자 배정, 기사 처리, 결과보고 화면",
  "sh/0ru1ozyp": "",
  "sh/87a18zqt": "",
  "sh/76h0zup8": "",
  "sh/wba1cjqp": "",
  "sh/va103ep4": "",
  "sh/cf6h0fql": "",
  "sh/2l4faloj": "",
  "sh/onmxcv69": "",
  "sh/bqxg7q50": "",
  "sh/cr6xgv6l": "",
  "sh/rutwv65w": "",
  "sh/7y14jyd8": "",
  "sh/8zu5c3ut": "",
  "sh/v2l4nitk": "",
  "sh/h43mp8ba": "",
  "sh/w3u5g3u5": "",
  "sh/i5cnidcv": "",
  "sh/1wnm1sru": "",
  "sh/fa54zi9o": "",
  "sh/t8nmx8ri": "",
  "sh/r654vi9c": "",

  // 10. 활용방안 및 기대효과
  "sh/r6pcv6lw": "01   프로젝트 개요 · 활용 방안 및 기대 효과",
  "sh/0bit8b2d": "소규모 AS센터의 운영 흐름에 적용 가능한 기반",
  "sh/wvetgbud": "현장 운영",
  "sh/bu5cnqd8": "접수·배정·결과·고객 평가를 한 화면 흐름으로 관리",
  "sh/atwbelcn": "교육·포트폴리오",
  "sh/lk7ut0va": "MVC, DB 조인, 파일 업로드, 외부 API 연동 경험 제시",
  "sh/kjytkvup": "확장 방향",
  "sh/jypcrqd4": "알림 고도화, 배정 충돌 검증, 배포·보안 강화",
  "sh/dwjqtcbu": "일정 가시성 향상",
  "sh/exsrmhsf": "관리자와 기사가 같은 배정 정보를 확인",
  "sh/103qxcbq": "처리 이력 축적",
  "sh/u187adsv": "신청부터 결과·평가까지 데이터가 연결됨",
  "sh/algfy50n": "피드백 수집 자동화",
  "sh/xo7ytkzy": "결과 등록 후 고객에게 설문 링크 발송",
  "sh/ip0f2p0j": "운영 지표 확보",
  "sh/5cryx4za": "기사별 월 평균과 전년 동월 비교 제공",

  // 11. 자체평가
  "sh/nehs7iho": "05   자체 평가 의견",
  "sh/0b6tc3yx": "초안 기준 완성도 8/10, 핵심 흐름은 동작하고 운영·보안은 보완 필요",
  "sh/7650nq5s": "고객→관리자→기사→고객의 데이터 순환을 실제 화면으로 연결",
  "sh/l43ilgn2": "역할별 접근 제어와 본인 일정 확인으로 기본 데이터 보호 적용",
  "sh/j2lgjq5w": "현재 빌드와 자동 테스트 4개가 정상 통과",
  "sh/o7axk7m9": "비밀번호를 해시하지 않고 저장하는 학습용 구현",
  "sh/eh8fex47": "SMS 인증정보 외부화와 배포 환경 분리 필요",
  "sh/03qxg7mx": "일정 배정의 트랜잭션·충돌 검증을 더 강화해야 함",
  "sh/5sjah872": "서로 다른 PK를 구분하고 조인하는 데이터 모델의 중요성",
  "sh/v2hsbyp0": "화면 시각과 서버 처리시각의 책임을 분리하는 방법",
  "sh/h4zado76": "공통 디자인과 URL 규칙이 통합 품질에 미치는 영향",
  "sh/a147m5ob": "비밀번호 해시·비밀정보 환경변수화·보안 설정 강화",
  "sh/k7mpsfmd": "기사 일정 중복 방지와 상태값 Enum 정리",
  "sh/y547qp47": "모바일 반응형·배포·로그 모니터링 보완",

  // 12. 마무리
  "sh/dcv6dgz2": "[발표자 입력] · 팀 공동 결과보고",
  "sh/alwbitsn": "TEAM [조/팀명 입력] · 4인"
};

for (const [anchor, value] of Object.entries(replacements)) setText(anchor, value);

// 슬라이드 9: 실제 구동 화면 네 장을 결과 근거로 배치한다.
const resultSlide = presentation.slides.items[8];
const screenshots = [
  ["request-form.png", "고객 AS 신청 화면", { left: 58, top: 178, width: 565, height: 220 }],
  ["admin-schedule.png", "관리자 일정 배정 화면", { left: 657, top: 178, width: 565, height: 220 }],
  ["engineer-dashboard.png", "기사 일정·만족도 대시보드", { left: 58, top: 410, width: 565, height: 220 }],
  ["result-report.png", "기사 AS 결과보고 화면", { left: 657, top: 410, width: 565, height: 220 }],
];
for (const [fileName, alt, position] of screenshots) {
  resultSlide.images.add({
    blob: await fs.readFile(path.join(assetDir, fileName)),
    contentType: "image/png",
    alt,
    fit: "cover",
    geometry: "roundRect",
    borderRadius: 10,
    position,
  });
}

const notes = [
  `[0:00~0:40] 안녕하세요. 저희 팀은 냉난방기기 AS 업무의 접수부터 기사 배정, 방문 결과, 고객 만족도까지 한 시스템에서 관리하는 Schedule을 개발했습니다. 프로젝트 기간은 9월 8일부터 15일까지 6개 작업일입니다. 화면 단위로 역할을 나눠 각 담당자가 백엔드와 프론트엔드를 함께 구현했습니다. 표지의 훈련기관명, 팀명, 팀장명, 발표자와 멘토명은 제출 전에 실제 정보로 교체해 주세요.`,
  `[0:40~1:10] 발표는 가이드의 다섯 항목을 기준으로 진행합니다. 먼저 프로젝트 개요와 차별점을 설명하고, 팀 구성과 역할을 소개하겠습니다. 이어서 수행 절차와 방법, 실제 구현 경과와 구동 화면을 보여드린 뒤 자체 평가로 마무리하겠습니다. 핵심 기능 시연은 별도 5~10분 영상으로 제작할 수 있도록 화면 흐름도 함께 정리했습니다.`,
  `[1:10~2:20] Schedule의 목적은 흩어진 AS 업무를 하나의 데이터 흐름으로 연결하는 것입니다. 고객이 신청하면 관리자가 접수 현황을 보고 기사를 배정합니다. 기사는 오늘 일정과 월간 일정을 확인하고 방문 뒤 결과를 등록합니다. 신규 결과가 등록되면 고객에게 만족도 설문 링크가 문자로 발송되고, 응답은 기사별 월간 통계와 전년 동월 비교에 반영됩니다. 관리자, 기사, 고객의 역할은 분리했지만 REQUEST와 AS_SCHEDULE을 중심으로 같은 상태를 공유합니다. 근거: RequestController, ScheduleController, ResultController, SatisfactionService.`,
  `[2:20~3:40] 선정 배경은 전화와 메모 중심 업무에서 발생하는 정보 단절입니다. 관리자는 여러 기사 일정을 한눈에 보기 어렵고, 기사는 자신의 방문 일정을 다시 전달받아야 합니다. 작업이 끝나도 고객 피드백은 별도 채널에 남아 운영 데이터가 되기 어렵습니다. 저희는 역할별 화면을 만들고 접수, 배정, 결과, 만족도를 하나의 DB로 연결했습니다. 특히 결과 등록 직후 자체 설문 링크를 발송하고 응답을 통계로 반영하는 부분이 단순 일정 캘린더와 다른 특화 포인트입니다.`,
  `[3:40~4:50] 팀은 화면 단위로 역할을 나눴습니다. 팀장은 공통 화면과 로그인, 통합을 맡았습니다. 최준영은 관리자 대시보드와 기사관리, 김성준은 관리자 일정관리, 박성인은 기사 대시보드와 결과보고, SMS와 만족도 기능을 담당했습니다. 화면 단위 분업 덕분에 각 담당자가 Controller, Service, Mapper, HTML과 JavaScript까지 완결된 흐름을 경험했습니다. 다만 저장소에서 팀장 이름과 멘토 지원 내역은 확인할 수 없으므로 발표 전 실제 피드백과 질의응답 사례를 입력해야 합니다.`,
  `[4:50~6:20] 수행 과정은 다섯 단계입니다. 9월 8일에 역할과 Git 전략을 정하고, 다음 날 DB와 공통 화면 뼈대를 만들었습니다. 10일과 11일에는 기사 일정에서 결과보고, SMS 설문까지 기능을 연결했습니다. 14일에는 오류 수정과 만족도 통계, 디자인 통합을 진행했습니다. 15일에는 더미 데이터로 전체 흐름을 확인하고 자동 테스트를 실행했습니다. 대표 피드백 사례는 MEMBER 번호와 ENGINEER 번호를 혼동한 오류입니다. 기사번호를 다시 조회하는 로직을 추가해 해결했고, Google Form 대신 자체 설문으로 전환해 DB 통계까지 연결했습니다.`,
  `[6:20~7:20] 수업에서 배운 MVC 구조와 MyBatis 매핑, 데이터베이스 조인을 프로젝트에 적용했습니다. 서버는 Java 17과 Spring Boot 4.0.8, 화면은 Thymeleaf와 JavaScript를 사용했습니다. 일정은 FullCalendar로 표시하고 데이터는 MariaDB에 저장합니다. 결과 등록 뒤 Solapi로 설문 링크를 발송하고, 외부 휴대폰에서 접근하도록 Cloudflare Tunnel 주소를 설정했습니다. 개발 도구는 IntelliJ와 Gradle, 협업은 GitHub, 노션, ERDCloud를 사용했습니다. 근거: build.gradle과 application.yaml.`,
  `[7:20~9:00] 현재 핵심 흐름은 모두 구현되어 있습니다. 공통 영역에는 회원가입, 로그인, 역할별 접근 제어가 있습니다. 고객은 신청, 중복 확인, 본인 내역과 상세를 조회할 수 있습니다. 관리자는 네 가지 상태 통계, 최근 접수, 기사관리, 월간 캘린더와 배정을 사용할 수 있습니다. 기사는 오늘·월간 일정과 결과보고를 처리하고 사진 두 장까지 첨부할 수 있습니다. 고객 피드백은 세 항목 설문과 월별 통계로 이어집니다. DB는 만족도 테이블을 포함해 6개입니다. 현재 테스트 4개가 통과했으며, 이 수치는 기능 성공률이 아니라 자동 테스트 개수입니다.`,
  `[9:00~11:40] 실제 화면을 순서대로 보겠습니다. 왼쪽 위는 고객이 제품과 증상, 희망일을 입력하는 신청 화면입니다. 오른쪽 위는 관리자가 미배정 접수를 선택하고 기사와 시간을 배정하는 화면입니다. 왼쪽 아래는 기사가 오늘 일정과 월간 캘린더, 만족도 통계를 확인하는 대시보드입니다. 오른쪽 아래는 처리 내용과 사진을 남기는 결과보고입니다. 개발 중 기사번호 오류를 수정했고, 설문을 자체 페이지로 전환했습니다. 또한 다른 기사의 scheduleNo를 직접 입력해 접근하지 못하도록 소유권을 확인하고, 처리시각은 서버가 기록하도록 변경했습니다. 근거: 실제 localhost 구동 화면과 ResultController 주석.`,
  `[11:40~13:00] 활용 방안은 소규모 AS센터의 접수와 배정, 처리 이력 관리입니다. 관리자는 같은 화면에서 상태와 일정을 보고, 기사는 자신에게 배정된 일정만 확인합니다. 결과 등록 뒤 설문 발송과 통계 집계가 이어지므로 고객 피드백을 별도로 옮길 필요가 줄어듭니다. 교육 측면에서는 MVC, 조인, 파일 업로드, 외부 API를 하나의 서비스 흐름에서 경험한 포트폴리오가 됩니다. 다음 단계는 배정 충돌 검증, 알림 고도화, 배포와 보안 강화입니다. 효과는 아직 실제 운영 수치로 검증하지 않았으므로 발표에서는 기대 효과로 표현합니다.`,
  `[13:00~14:40] 자체 완성도는 초안 기준 8점으로 평가했습니다. 고객에서 다시 고객으로 돌아오는 핵심 흐름이 동작하고, 현재 빌드와 자동 테스트도 통과했다는 점이 근거입니다. 반면 비밀번호 해시 저장, SMS 인증정보의 환경변수화, 일정 배정 시 트랜잭션과 충돌 검증은 보완해야 합니다. 프로젝트를 통해 서로 다른 테이블의 기본키를 구분하는 중요성과 화면에서 보이는 시각보다 서버가 처리시각의 기준이 되어야 한다는 점을 배웠습니다. 다음에는 보안 설정, 상태값 Enum, 모바일 반응형과 운영 로그를 우선 개선하겠습니다.`,
  `[14:40~15:00] 이상으로 Schedule 프로젝트 결과보고를 마치겠습니다. 핵심은 고객 신청, 관리자 배정, 기사 처리, 고객 평가가 하나의 데이터로 이어진다는 점입니다. 질문 주시면 실제 화면과 코드 흐름을 기준으로 답변드리겠습니다. 저장소 주소와 발표자 정보는 제출 전 최종 확인해 주세요.`
];

presentation.slides.items.forEach((slide, index) => {
  slide.speakerNotes.textFrame.setText(notes[index]);
  slide.speakerNotes.setVisible(true);
});

const snapshot = await presentation.inspect({
  kind: "slide,textbox,image,notes",
  maxChars: 120000,
});
await fs.writeFile(path.join(stagingDir, "final-inspect.ndjson"), snapshot.ndjson, "utf8");

const candidatePath = path.join(stagingDir, "candidate.pptx");
await (await PresentationFile.exportPptx(presentation)).save(candidatePath);

const { finalizePresentation } = await import(pathToFileURL(
  path.join(skillDir, "container_tools", "artifact_tool_utils.mjs"),
).href);

const requirements = {
  explicitTotalSlideCount: 12,
  requiredNativeTableOwnerSlides: [],
  requiredNativeChartOwnerSlides: [],
};
const fontPolicy = {
  basis: "reference",
  families: ["맑은 고딕", "Arial"],
  referencePath: sourcePath,
  referenceSha256: "20e337c1857338a3912b47014546c0f41e9b5e0758d1f20cfe1060f3eddfed43",
};

const result = await finalizePresentation({
  ...requirements,
  workspaceDir,
  candidatePath,
  finalPath,
  pythonExecutable,
  integrityValidatorPath: path.join(skillDir, "container_tools", "inspect_presentation_package_integrity.py"),
  layoutValidatorPath: path.join(skillDir, "container_tools", "inspect_presentation_layout_geometry.py"),
  layoutArgs: [
    "--expected-slide-size-emu", "12192000,6858000",
    "--validate-bullet-geometry",
    "--validate-heading-fit",
  ],
  requiredNativeTableOwnerSlides: [],
  fontPolicy,
  verifyArtifactToolImport: true,
    receiptPath: path.join(stagingDir, "Schedule_15min_v2.validation.json"),
});

console.log(JSON.stringify({ finalPath, result }, null, 2));

import fs from "node:fs/promises";
import path from "node:path";
import { pathToFileURL } from "node:url";
import { Presentation, PresentationFile } from "@oai/artifact-tool";

const workspaceDir = "D:/01-STUDY/dev-01/workspace_spring/Schedule/src/main/java/com/green/Schedule";
const assetDir = path.join(workspaceDir, ".deck-assets");
const stagingDir = path.join(workspaceDir, ".ppt-build", "business-finalizer");
const outputDir = path.join(workspaceDir, "output");
const finalPath = path.join(outputDir, "결과보고서_Schedule_비즈니스발표_15분_20260915_v2.pptx");
const skillDir = "C:/Users/GRS/.codex/plugins/cache/openai-primary-runtime/presentations/26.909.22227/skills/presentations";
const pythonExecutable = "C:/Users/GRS/.cache/codex-runtimes/codex-primary-runtime/dependencies/python/python.exe";

await fs.mkdir(stagingDir, { recursive: true });
await fs.mkdir(outputDir, { recursive: true });

const ppt = Presentation.create({ slideSize: { width: 1280, height: 720 } });
const FONT = "맑은 고딕";
const C = {
  ink: "#07131F",
  navy: "#0A1C2B",
  navy2: "#102C3F",
  white: "#FFFFFF",
  paper: "#F4F7F8",
  muted: "#718295",
  line: "#DCE5E9",
  cyan: "#20D5C4",
  cyan2: "#00AFA8",
  coral: "#FF6B4A",
  lime: "#BBDD3C",
  sky: "#6CC8FF",
};

function rect(slide, x, y, w, h, color, radius = 0, line = "none") {
  return slide.shapes.add({
    geometry: radius ? "roundRect" : "rect",
    position: { left: x, top: y, width: w, height: h },
    fill: { type: "solid", color },
    line: line === "none" ? { fill: "none", width: 0 } : { style: "solid", fill: line, width: 1 },
  });
}

function text(slide, value, x, y, w, h, size = 24, color = C.ink, bold = false) {
  const shape = slide.shapes.add({
    geometry: "textbox",
    position: { left: x, top: y, width: w, height: h },
    fill: "none",
    line: { fill: "none", width: 0 },
  });
  shape.text = value;
  shape.text.style = { typeface: FONT, fontSize: size, color, bold, autoFit: "none" };
  return shape;
}

async function image(slide, file, x, y, w, h, fit = "cover", radius = 14) {
  return slide.images.add({
    blob: await fs.readFile(path.join(assetDir, file)),
    contentType: "image/png",
    alt: file.replace(".png", ""),
    fit,
    geometry: radius ? "roundRect" : "rect",
    borderRadius: radius,
    position: { left: x, top: y, width: w, height: h },
  });
}

function base(slide, page, dark = false) {
  slide.background.fill = dark ? C.ink : C.white;
  rect(slide, 0, 0, 1280, 7, page % 2 ? C.cyan : C.coral);
  text(slide, "SCHEDULE", 66, 28, 160, 24, 12, dark ? C.cyan : C.cyan2, true);
  text(slide, String(page).padStart(2, "0"), 1190, 668, 40, 20, 11, dark ? "#7790A2" : "#9BACB7", true);
}

function title(slide, eyebrow, heading, dark = false) {
  text(slide, eyebrow, 66, 58, 740, 25, 12, dark ? C.cyan : C.cyan2, true);
  text(slide, heading, 66, 92, 1135, 70, 34, dark ? C.white : C.ink, true);
}

function metric(slide, value, label, x, y, color = C.cyan, dark = false) {
  text(slide, value, x, y, 190, 58, 38, color, true);
  text(slide, label, x, y + 56, 205, 44, 15, dark ? "#A8B8C3" : C.muted, false);
}

function setNotes(slide, note) {
  slide.speakerNotes.textFrame.setText(note);
  slide.speakerNotes.setVisible(true);
}

// 01 Cover
{
  const s = ppt.slides.add();
  s.background.fill = C.ink;
  await image(s, "landing.png", 760, 0, 520, 720, "cover", 0);
  rect(s, 0, 0, 760, 720, C.ink);
  rect(s, 66, 104, 72, 7, C.cyan);
  text(s, "Schedule", 66, 150, 650, 88, 58, C.white, true);
  text(s, "냉난방기기 AS 운영관리 시스템", 68, 246, 610, 42, 24, "#D7E1E7", false);
  text(s, "접수부터 고객 평가까지 이어지는 현장 서비스 운영", 68, 318, 595, 76, 27, C.cyan, true);
  text(s, "TEAM PROJECT  ·  2026.09.08–09.15", 68, 580, 480, 26, 13, "#8EA2B1", true);
  text(s, "팀명·발표자 입력", 68, 620, 420, 30, 16, C.white, false);
  setNotes(s, "[0:00~0:35] 안녕하세요. 저희가 개발한 Schedule은 냉난방기기 AS 운영을 하나의 흐름으로 관리하는 웹 서비스입니다. 고객 접수, 관리자 배정, 기사 방문과 결과보고, 고객 만족도 수집까지 실제 업무 순서대로 연결했습니다. 오늘은 기능 나열보다 어떤 운영 문제를 해결했고 실제로 어떻게 동작하는지 중심으로 말씀드리겠습니다.");
}

// 02 Executive summary
{
  const s = ppt.slides.add(); base(s, 2);
  title(s, "PROJECT OVERVIEW", "AS 업무 전체를 하나의 데이터 흐름으로 연결", false);
  text(s, "전화·메모 중심의 분절된 업무를 역할별 화면과 공통 상태 데이터로 통합했습니다.", 66, 168, 1020, 35, 20, C.muted, false);
  const stages = [
    ["01", "고객 접수", "제품·증상·희망일 입력"],
    ["02", "기사 배정", "담당 기사와 방문시간 확정"],
    ["03", "현장 처리", "작업 결과와 사진 기록"],
    ["04", "고객 평가", "SMS 설문과 월간 통계"],
  ];
  stages.forEach((d, i) => {
    const x = 66 + i * 294;
    text(s, d[0], x, 255, 60, 34, 16, i === 3 ? C.coral : C.cyan2, true);
    text(s, d[1], x, 298, 240, 42, 25, C.ink, true);
    text(s, d[2], x, 350, 230, 46, 15, C.muted, false);
    if (i < 3) text(s, "›", x + 250, 300, 30, 42, 28, "#B5C4CC", false);
  });
  rect(s, 66, 456, 1148, 1, C.line);
  metric(s, "3", "사용자 역할", 66, 492, C.cyan2);
  metric(s, "6", "핵심 DB 테이블", 350, 492, C.coral);
  metric(s, "4", "자동 테스트 통과", 650, 492, C.lime);
  metric(s, "1", "통합 처리 흐름", 950, 492, C.sky);
  text(s, "개발 근거  ·  실제 저장소, 작업일지, localhost 구동 화면 기준", 66, 646, 700, 22, 12, "#95A5AF", false);
  setNotes(s, "[0:35~1:20] 프로젝트의 핵심은 네 단계입니다. 고객이 신청하면 관리자가 기사를 배정하고, 기사가 현장에서 결과를 등록하면 고객에게 설문 링크가 발송됩니다. 각 역할은 서로 다른 화면을 사용하지만 같은 접수번호와 일정 데이터를 공유합니다. 현재 구현은 사용자 역할 3종, 핵심 테이블 6개이며 자동 테스트 4개가 통과했습니다. 이 숫자는 성공률이 아니라 저장소에서 확인한 테스트 개수입니다.");
}

// 03 Problem
{
  const s = ppt.slides.add(); base(s, 3, true);
  await image(s, "request-list.png", 690, 86, 524, 568, "cover", 16);
  text(s, "WHY THIS PROJECT", 66, 76, 500, 24, 12, C.cyan, true);
  text(s, "운영 정보가 흩어지면\n일정과 책임도 함께 흐려집니다", 66, 116, 570, 112, 35, C.white, true);
  const issues = [
    ["01", "접수 이력", "전화와 메모에 남은 요청은 검색과 상태 추적이 어렵습니다."],
    ["02", "배정 현황", "관리자는 기사별 일정과 미배정 요청을 함께 보기 어렵습니다."],
    ["03", "고객 피드백", "처리 후 평가는 별도 채널에 남아 운영 지표로 축적되지 않습니다."],
  ];
  issues.forEach((d, i) => {
    const y = 278 + i * 112;
    text(s, d[0], 66, y, 44, 28, 14, i === 2 ? C.coral : C.cyan, true);
    text(s, d[1], 126, y - 2, 180, 30, 20, C.white, true);
    text(s, d[2], 126, y + 34, 474, 52, 15, "#AFC0CA", false);
  });
  setNotes(s, "[1:20~2:25] 선정 배경은 현장 서비스 업무의 정보 단절입니다. 접수 내용이 전화나 메모로 들어오면 상태를 다시 확인해야 하고, 관리자는 미배정 요청과 기사 일정을 따로 봐야 합니다. 기사는 일정을 전달받은 뒤 다시 결과를 남겨야 하며, 고객 평가는 운영 데이터로 축적되기 어렵습니다. Schedule은 이 세 가지 단절을 하나의 접수 상태와 일정 데이터로 연결하는 데서 출발했습니다.");
}

// 04 Operating model
{
  const s = ppt.slides.add(); base(s, 4);
  title(s, "SERVICE MODEL", "역할은 분리하고 업무 상태는 공유", false);
  text(s, "각 사용자는 필요한 화면만 보지만 REQUEST와 AS_SCHEDULE을 중심으로 같은 진행 상태를 사용합니다.", 66, 166, 1080, 40, 19, C.muted, false);
  const roles = [
    ["CUSTOMER", "고객", "신청\n내역 확인\n만족도 응답", C.sky],
    ["ADMIN", "관리자", "접수 현황\n기사 관리\n일정 배정", C.coral],
    ["ENGINEER", "기사", "오늘 일정\n현장 처리\n결과보고", C.cyan2],
  ];
  roles.forEach((d, i) => {
    const x = 66 + i * 386;
    text(s, d[0], x, 256, 300, 24, 11, d[3], true);
    text(s, d[1], x, 292, 300, 50, 30, C.ink, true);
    rect(s, x, 352, 300, 2, d[3]);
    text(s, d[2], x, 376, 300, 120, 18, C.muted, false);
  });
  rect(s, 66, 550, 1148, 70, C.ink, 12);
  text(s, "공통 데이터", 92, 570, 160, 28, 14, C.cyan, true);
  text(s, "접수 상태 · 기사 일정 · 처리 결과 · 만족도", 258, 566, 780, 36, 22, C.white, true);
  setNotes(s, "[2:25~3:25] 서비스 구조는 고객, 관리자, 기사 세 역할로 나뉩니다. 고객은 신청과 자신의 내역, 만족도 설문을 사용합니다. 관리자는 접수 현황과 기사 관리, 일정 배정을 담당합니다. 기사는 본인 일정과 결과보고만 확인합니다. 화면과 권한은 분리했지만 접수 상태, 기사 일정, 처리 결과와 만족도 데이터는 순차적으로 이어집니다. 역할별 접근 제어와 본인 소유 데이터 확인도 적용했습니다.");
}

// 05 Customer intake
{
  const s = ppt.slides.add(); base(s, 5);
  await image(s, "request-form.png", 44, 92, 770, 548, "cover", 16);
  text(s, "CUSTOMER EXPERIENCE", 860, 94, 330, 24, 12, C.cyan2, true);
  text(s, "고객 접수", 860, 132, 340, 50, 32, C.ink, true);
  text(s, "필요한 정보만 입력하고\n처리 상태를 직접 확인", 860, 196, 340, 74, 23, C.ink, true);
  const items = [
    ["01", "제품·증상·희망일 입력"],
    ["02", "중복 신청 사전 확인"],
    ["03", "본인 신청내역과 상세 조회"],
  ];
  items.forEach((d, i) => {
    const y = 328 + i * 72;
    text(s, d[0], 860, y, 38, 26, 13, C.coral, true);
    text(s, d[1], 910, y - 1, 292, 36, 17, C.muted, false);
  });
  rect(s, 860, 566, 320, 2, C.line);
  text(s, "REQUEST", 860, 585, 100, 24, 11, C.cyan2, true);
  text(s, "접수 데이터의 시작점", 970, 581, 220, 30, 16, C.ink, true);
  setNotes(s, "[3:25~4:25] 고객 화면은 접수 데이터의 시작점입니다. 고객은 제품 종류, 증상, 주소와 희망일을 입력합니다. 같은 조건의 중복 신청을 확인하고, 로그인한 고객은 자신의 신청내역과 상세만 조회할 수 있습니다. 이 화면에서 만들어진 REQUEST 데이터가 관리자 배정과 기사 일정으로 이어집니다.");
}

// 06 Admin command center
{
  const s = ppt.slides.add(); base(s, 6, true);
  text(s, "ADMIN OPERATIONS", 66, 62, 420, 24, 12, C.cyan, true);
  text(s, "관리자가 먼저 보는 운영 현황", 66, 98, 760, 56, 34, C.white, true);
  await image(s, "admin-dashboard.png", 66, 186, 1148, 378, "cover", 16);
  const labels = [
    ["상태 통계", "접수·배정·처리 상태를 즉시 확인"],
    ["최근 요청", "신규 접수의 우선순위를 빠르게 판단"],
    ["상세 조회", "고객 정보와 증상을 한 화면에서 확인"],
  ];
  labels.forEach((d, i) => {
    const x = 66 + i * 382;
    text(s, d[0], x, 592, 150, 25, 16, i === 1 ? C.coral : C.cyan, true);
    text(s, d[1], x, 620, 340, 40, 14, "#AFC0CA", false);
  });
  setNotes(s, "[4:25~5:30] 관리자는 로그인 직후 대시보드에서 전체 운영 상태를 확인합니다. 상태별 건수와 최근 접수 목록을 함께 보고, 상세 화면에서 고객 정보와 증상을 확인합니다. 운영자가 여러 메뉴를 오가며 숫자를 맞추지 않아도 현재 요청이 어느 단계에 있는지 파악할 수 있도록 구성했습니다.");
}

// 07 Scheduling
{
  const s = ppt.slides.add(); base(s, 7);
  title(s, "SCHEDULING", "미배정 요청을 기사 일정으로 전환", false);
  text(s, "관리자는 요청을 선택한 뒤 담당 기사와 방문 시간을 확정합니다.", 66, 164, 800, 34, 19, C.muted, false);
  await image(s, "admin-schedule.png", 66, 224, 770, 410, "cover", 16);
  text(s, "배정 로직", 884, 234, 290, 34, 18, C.cyan2, true);
  const items = [
    ["1", "미배정 접수 선택", "REQUEST 상태와 고객 요청일 확인"],
    ["2", "기사·시간 지정", "기사 목록과 시작·종료시간 입력"],
    ["3", "일정 생성", "AS_SCHEDULE 저장 후 상태를 ASSIGNED로 변경"],
  ];
  items.forEach((d, i) => {
    const y = 300 + i * 96;
    text(s, d[0], 884, y, 36, 34, 24, i === 2 ? C.coral : C.cyan2, true);
    text(s, d[1], 934, y, 260, 30, 18, C.ink, true);
    text(s, d[2], 934, y + 34, 280, 45, 14, C.muted, false);
  });
  setNotes(s, "[5:30~6:35] 일정관리 화면에서는 미배정 요청을 선택하고 담당 기사와 방문 시간을 입력합니다. 저장하면 AS_SCHEDULE에 일정이 생성되고 REQUEST 상태가 ASSIGNED로 변경됩니다. 월간 캘린더에서 기사별 배정 결과를 색상으로 확인할 수 있습니다. 현재 핵심 배정 흐름은 구현됐고, 같은 기사에게 겹치는 시간을 배정하지 않도록 하는 충돌 검증은 다음 보완 과제로 남았습니다.");
}

// 08 Engineer workflow
{
  const s = ppt.slides.add(); base(s, 8, true);
  text(s, "FIELD EXECUTION", 66, 58, 400, 24, 12, C.cyan, true);
  text(s, "기사는 일정 확인부터 결과보고까지 한 흐름으로 처리", 66, 92, 1100, 56, 32, C.white, true);
  await image(s, "engineer-dashboard.png", 66, 180, 548, 402, "cover", 14);
  await image(s, "result-report.png", 666, 180, 548, 402, "cover", 14);
  text(s, "TODAY & MONTH", 66, 602, 180, 22, 11, C.cyan, true);
  text(s, "오늘 일정과 월간 캘린더", 244, 598, 350, 30, 17, C.white, true);
  text(s, "RESULT REPORT", 666, 602, 180, 22, 11, C.coral, true);
  text(s, "처리 내용과 사진 2장 기록", 846, 598, 350, 30, 17, C.white, true);
  setNotes(s, "[6:35~8:05] 기사 화면은 현장 업무 순서에 맞췄습니다. 대시보드에서 오늘 배정과 월간 일정을 확인하고, 방문이 끝나면 결과보고 화면에서 처리 내용과 사진을 최대 두 장까지 등록합니다. 다른 기사의 scheduleNo를 직접 입력해 접근하지 못하도록 소유권을 확인하며, 처리 시각은 브라우저가 아니라 서버가 기록합니다. 신규 결과 등록이 완료되면 해당 접수 상태도 COMPLETED로 변경됩니다.");
}

// 09 Feedback loop
{
  const s = ppt.slides.add(); base(s, 9);
  await image(s, "satisfaction-survey.png", 708, 80, 506, 566, "cover", 16);
  text(s, "CUSTOMER FEEDBACK", 66, 72, 500, 24, 12, C.cyan2, true);
  text(s, "결과보고 뒤\n고객 평가가 운영 데이터가 됩니다", 66, 110, 580, 110, 34, C.ink, true);
  text(s, "차별화 포인트", 66, 256, 220, 28, 15, C.coral, true);
  text(s, "외부 설문으로 끝내지 않고 서비스 내부 DB에 응답을 저장해 기사별 월간 지표로 연결했습니다.", 66, 300, 558, 82, 20, C.ink, false);
  const flow = [
    ["01", "결과 등록"], ["02", "SMS 링크 발송"], ["03", "3항목 평가"], ["04", "월별 평균·전년 비교"],
  ];
  flow.forEach((d, i) => {
    const x = 66 + (i % 2) * 285;
    const y = 440 + Math.floor(i / 2) * 82;
    text(s, d[0], x, y, 40, 28, 13, i === 3 ? C.coral : C.cyan2, true);
    text(s, d[1], x + 50, y - 2, 220, 36, 17, C.muted, false);
  });
  setNotes(s, "[8:05~9:20] 결과보고가 끝나면 Solapi를 통해 고객에게 만족도 설문 링크를 발송합니다. 초기에는 Google Form을 검토했지만 응답을 시스템 통계로 연결하기 어려워 자체 설문으로 변경했습니다. 고객은 세 항목을 평가하고, 응답은 만족도 테이블에 저장됩니다. 기사 대시보드는 월간 평균, 전년 동월 비교와 월별 추이를 보여줍니다. 결과 등록에서 고객 피드백까지 이어지는 흐름이 Schedule의 가장 큰 차별점입니다.");
}

// 10 Team and execution
{
  const s = ppt.slides.add(); base(s, 10, true);
  text(s, "DELIVERY MODEL", 66, 58, 420, 24, 12, C.cyan, true);
  text(s, "화면 단위 오너십으로 6일 안에 통합", 66, 92, 800, 54, 34, C.white, true);
  const timeline = [
    ["09.08", "기획", "역할·화면 범위와 Git 전략"],
    ["09.09", "기반", "DB와 공통 화면 뼈대"],
    ["09.10–11", "연결", "일정·결과·SMS·설문"],
    ["09.14", "개선", "오류 수정·통계·디자인 통합"],
    ["09.15", "검증", "더미 데이터·빌드·테스트"],
  ];
  timeline.forEach((d, i) => {
    const x = 66 + i * 230;
    rect(s, x, 205, 178, 3, i === 3 ? C.coral : C.cyan);
    text(s, d[0], x, 224, 185, 30, 14, i === 3 ? C.coral : C.cyan, true);
    text(s, d[1], x, 266, 185, 34, 22, C.white, true);
    text(s, d[2], x, 310, 190, 66, 14, "#9FB2BE", false);
  });
  rect(s, 66, 414, 1148, 1, "#274251");
  text(s, "ROLE OWNERSHIP", 66, 446, 190, 24, 12, C.cyan, true);
  text(s, "공통·로그인·통합", 66, 492, 250, 32, 18, C.white, true);
  text(s, "팀장명 입력", 66, 530, 240, 24, 14, "#91A5B2", false);
  text(s, "관리자 대시보드·기사관리", 350, 492, 260, 32, 18, C.white, true);
  text(s, "최준영", 350, 530, 240, 24, 14, C.coral, true);
  text(s, "관리자 일정관리", 648, 492, 240, 32, 18, C.white, true);
  text(s, "김성준", 648, 530, 240, 24, 14, C.lime, true);
  text(s, "기사·결과·SMS·만족도", 922, 492, 278, 32, 18, C.white, true);
  text(s, "박성인", 922, 530, 240, 24, 14, C.cyan, true);
  text(s, "멘토 지원 내역은 실제 피드백 내용 입력 필요", 66, 624, 600, 24, 13, "#8298A6", false);
  setNotes(s, "[9:20~10:40] 개발은 9월 8일부터 15일까지 6개 작업일 동안 진행했습니다. 팀은 화면 단위로 오너십을 나눴습니다. 팀장은 공통 화면과 로그인, 통합을 맡았고 최준영은 관리자 대시보드와 기사관리, 김성준은 관리자 일정관리, 박성인은 기사 대시보드와 결과보고, SMS와 만족도를 담당했습니다. 각 담당자가 Controller, Service, Mapper, HTML과 JavaScript까지 하나의 기능 흐름을 완결했습니다. 저장소에 없는 팀장명과 실제 멘토 지원 내용은 제출 전에 입력해야 합니다.");
}

// 11 Engineering evidence and feedback
{
  const s = ppt.slides.add(); base(s, 11);
  title(s, "ENGINEERING", "교육과정의 기술을 실제 서비스 흐름으로 구현", false);
  text(s, "Java 17 · Spring Boot 4.0.8 · Thymeleaf · MyBatis · MariaDB · FullCalendar · Solapi", 66, 165, 1130, 34, 18, C.muted, false);
  text(s, "구현 근거", 66, 234, 220, 30, 16, C.cyan2, true);
  metric(s, "6", "연결된 DB 테이블", 66, 278, C.cyan2);
  metric(s, "4", "자동 테스트 통과", 300, 278, C.coral);
  metric(s, "2", "결과 사진 최대 첨부", 534, 278, C.lime);
  rect(s, 66, 414, 675, 1, C.line);
  text(s, "피드백을 반영한 개선", 66, 446, 300, 30, 16, C.cyan2, true);
  text(s, "기사번호 혼동", 66, 494, 190, 26, 17, C.ink, true);
  text(s, "MEMBER 번호와 ENGINEER 번호를 분리 조회", 272, 494, 440, 32, 16, C.muted, false);
  text(s, "외부 설문 한계", 66, 548, 190, 26, 17, C.ink, true);
  text(s, "자체 설문으로 전환해 DB 통계까지 연결", 272, 548, 440, 32, 16, C.muted, false);
  rect(s, 798, 234, 416, 350, C.ink, 16);
  text(s, "운영 전 보완", 832, 268, 320, 30, 16, C.coral, true);
  text(s, "비밀번호 해시 저장", 832, 322, 320, 28, 20, C.white, true);
  text(s, "SMS 인증정보 환경변수화", 832, 374, 330, 28, 20, C.white, true);
  text(s, "배정 트랜잭션·충돌 검증", 832, 426, 330, 28, 20, C.white, true);
  text(s, "상태값 Enum·운영 로그", 832, 478, 330, 28, 20, C.white, true);
  text(s, "현재 단계: 기능 검증 완료, 운영 보안 강화 필요", 832, 536, 340, 30, 13, "#93A8B5", false);
  setNotes(s, "[10:40~12:05] 기술 구성은 Java 17, Spring Boot 4.0.8, Thymeleaf, MyBatis와 MariaDB입니다. FullCalendar로 일정을 표시하고 Solapi SMS와 Cloudflare Tunnel을 연동했습니다. 교육에서 배운 MVC 구조, 데이터베이스 조인, 파일 업로드와 비동기 처리를 하나의 서비스 흐름에 적용했습니다. 개발 중 MEMBER 번호와 ENGINEER 번호를 혼동한 오류는 기사번호를 별도로 조회하도록 수정했고, 외부 설문은 자체 설문으로 바꿨습니다. 현재 빌드와 자동 테스트는 통과했지만 비밀번호 해시, 비밀정보 외부화와 배정 충돌 검증은 운영 전에 보완해야 합니다.");
}

// 12 Business value and roadmap
{
  const s = ppt.slides.add(); base(s, 12, true);
  text(s, "BUSINESS VALUE", 66, 58, 420, 24, 12, C.cyan, true);
  text(s, "소규모 AS센터가 바로 이해할 수 있는 운영 기반", 66, 92, 950, 56, 34, C.white, true);
  const values = [
    ["운영 가시성", "관리자와 기사가 같은 배정 정보를 확인해 전달 누락 가능성을 줄입니다."],
    ["처리 이력", "접수부터 결과와 평가까지 한 고객 건의 기록이 이어집니다."],
    ["고객 피드백", "결과 등록 뒤 설문을 발송해 별도 입력 없이 만족도를 축적합니다."],
  ];
  values.forEach((d, i) => {
    const y = 220 + i * 110;
    text(s, String(i + 1).padStart(2, "0"), 66, y, 50, 30, 15, i === 2 ? C.coral : C.cyan, true);
    text(s, d[0], 138, y - 4, 230, 34, 23, C.white, true);
    text(s, d[1], 386, y - 2, 520, 58, 16, "#AFC0CA", false);
  });
  rect(s, 960, 204, 254, 332, C.white, 16);
  text(s, "NEXT", 992, 238, 160, 24, 12, C.coral, true);
  text(s, "1", 992, 282, 42, 40, 28, C.ink, true);
  text(s, "일정 충돌 검증", 1042, 286, 140, 30, 16, C.ink, true);
  text(s, "2", 992, 354, 42, 40, 28, C.ink, true);
  text(s, "보안·배포 강화", 1042, 358, 150, 30, 16, C.ink, true);
  text(s, "3", 992, 426, 42, 40, 28, C.ink, true);
  text(s, "모바일·알림 개선", 1042, 430, 160, 30, 16, C.ink, true);
  text(s, "효과는 실제 운영 수치가 아닌 기대 효과로 제시", 66, 622, 600, 24, 13, "#8197A5", false);
  setNotes(s, "[12:05~13:25] 활용 대상은 소규모 AS센터입니다. 관리자는 접수와 배정 현황을 한눈에 보고, 기사는 자신에게 배정된 일정과 처리 이력에 집중할 수 있습니다. 고객 평가까지 같은 DB에 쌓이기 때문에 기사별 서비스 품질을 월간 단위로 확인할 기반도 생깁니다. 다음 단계는 일정 충돌 검증, 보안과 배포 환경 강화, 모바일 화면과 알림 개선입니다. 아직 실제 사업장에서 측정한 운영 수치는 없으므로 이 슬라이드의 내용은 검증된 성과가 아니라 기대 효과입니다.");
}

// 13 Self assessment and close
{
  const s = ppt.slides.add(); base(s, 13);
  text(s, "SELF ASSESSMENT", 66, 58, 420, 24, 12, C.cyan2, true);
  text(s, "핵심 업무 흐름은 완성, 운영 수준까지는 한 단계 남았습니다", 66, 92, 1120, 60, 34, C.ink, true);
  text(s, "8.0", 66, 202, 210, 96, 66, C.coral, true);
  text(s, "/ 10", 222, 250, 90, 34, 22, C.muted, true);
  text(s, "기능 완성도", 66, 302, 220, 30, 17, C.ink, true);
  rect(s, 66, 352, 520, 10, "#E7ECEF", 5);
  rect(s, 66, 352, 416, 10, C.coral, 5);
  text(s, "평가 근거", 650, 202, 220, 28, 15, C.cyan2, true);
  text(s, "고객 접수부터 만족도까지 실제 화면으로 연결", 650, 248, 520, 34, 19, C.ink, true);
  text(s, "역할별 접근 제어와 기사 일정 소유권 확인", 650, 298, 520, 34, 19, C.ink, true);
  text(s, "현재 빌드와 자동 테스트 4개 통과", 650, 348, 520, 34, 19, C.ink, true);
  rect(s, 66, 430, 1148, 1, C.line);
  text(s, "배운 점", 66, 466, 160, 28, 15, C.cyan2, true);
  text(s, "테이블별 PK를 명확히 구분해야 서비스 로직이 안정됩니다", 66, 508, 540, 38, 18, C.ink, true);
  text(s, "처리시각과 권한 검증은 화면보다 서버가 책임져야 합니다", 66, 558, 540, 38, 18, C.ink, true);
  text(s, "Q&A", 900, 490, 250, 56, 40, C.coral, true);
  text(s, "실제 화면과 코드 흐름을 기준으로 답변드리겠습니다", 780, 554, 400, 52, 16, C.muted, false);
  text(s, "팀명 · 발표자 · 멘토 정보 입력 필요", 66, 646, 500, 22, 12, "#95A5AF", false);
  setNotes(s, "[13:25~15:00] 자체 완성도는 10점 만점에 8점으로 평가했습니다. 고객 접수에서 관리자 배정, 기사 처리와 만족도까지 핵심 흐름이 실제 화면에서 동작하고, 현재 빌드와 자동 테스트도 통과했습니다. 반면 비밀번호 해시 저장, SMS 인증정보 환경변수화, 일정 배정 충돌 검증은 운영 전에 반드시 보완해야 합니다. 프로젝트를 통해 테이블마다 다른 기본키를 정확히 구분하는 중요성, 처리 시각과 권한은 서버가 책임져야 한다는 점을 배웠습니다. 이상으로 발표를 마치겠습니다. 질문 주시면 실제 화면과 코드 흐름을 기준으로 답변드리겠습니다.");
}

const candidatePath = path.join(stagingDir, "candidate.pptx");
await (await PresentationFile.exportPptx(ppt)).save(candidatePath);

const { finalizePresentation } = await import(pathToFileURL(path.join(skillDir, "container_tools", "artifact_tool_utils.mjs")).href);
const result = await finalizePresentation({
  explicitTotalSlideCount: 13,
  requiredNativeTableOwnerSlides: [],
  requiredNativeChartOwnerSlides: [],
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
  fontPolicy: { basis: "design", families: [FONT] },
  verifyArtifactToolImport: true,
  receiptPath: path.join(stagingDir, "Schedule_business_15min_v2.validation.json"),
});

console.log(JSON.stringify({ finalPath, result }, null, 2));

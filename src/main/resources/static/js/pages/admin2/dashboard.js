/* 관리자 대시보드 JS - axios 기반 */

//페이지가 열리면 통계와 최근 목록을 함께 불러옴
document.addEventListener("DOMContentLoaded", function() {
  loadDashboardStats();
  loadRecentAs(1);
});

//상단 상태 카드 4개 채우기
function loadDashboardStats() {
  axios.get("/dashboard-api/stats")
    .then(function(response) {
      const data = response.data;

      document.getElementById("receiptCount").innerText  = data.receiptCount  || 0;
      document.getElementById("assignedCount").innerText = data.assignedCount || 0;
      document.getElementById("progressCount").innerText = data.progressCount || 0;
      document.getElementById("completeCount").innerText = data.completeCount || 0;
    })
    .catch(function(error) {
      console.error(error);
      alert("대시보드 데이터를 불러오지 못했습니다.");
    });
}

//최근 AS 목록 + 페이지네이션 조회
function loadRecentAs(page) {
  axios.get("/dashboard-api/recent", {
    params: { page: page }
  })
    .then(function(response) {
      const data = response.data;

      renderRecentTable(data.list);
      renderPagination(data.currentPage, data.totalPages);
    })
    .catch(function(error) {
      console.error(error);
      alert("AS 접수 목록을 불러오지 못했습니다.");
    });
}

//테이블 그리기
function renderRecentTable(list) {
  const tbody = document.getElementById("recentTableBody");
  tbody.innerHTML = "";

  //데이터 없음 처리
  if(!list || list.length === 0) {
    const emptyRow = document.createElement("tr");
    emptyRow.innerHTML = '<td colspan="5" class="empty-row">접수된 AS가 없습니다.</td>';
    tbody.appendChild(emptyRow);
    return;
  }

  //각 행 그리기
  list.forEach(function(item) {
    const tr = document.createElement("tr");

    //행 클릭 시 상세 조회 (AS요청 고유번호 기준)
    tr.onclick = function() {
      openDetailModal(item.requestNo);
    };

    const tdDate = document.createElement("td");
    tdDate.textContent = safe(item.requestDate);

    const tdName = document.createElement("td");
    tdName.textContent = safe(item.customerName);

    const tdSymptom = document.createElement("td");
    tdSymptom.textContent = safe(item.symptom);

    const tdRegion = document.createElement("td");
    tdRegion.textContent = safe(item.region);
  
    const tdStatus = document.createElement("td");
    tdStatus.innerHTML = statusBadge(item.status);

    tr.appendChild(tdDate);
    tr.appendChild(tdName);
    tr.appendChild(tdSymptom);
    tr.appendChild(tdRegion);
    tr.appendChild(tdStatus);

    tbody.appendChild(tr);
  });
}

//페이지네이션 그리기 (총 페이지가 1개 이하면 표시하지 않음)
function renderPagination(currentPage, totalPages) {
  const box = document.getElementById("pagination");
  box.innerHTML = "";

  //5건 이하로 1페이지뿐이면 페이지네이션 숨김
  if(totalPages <= 1) return;

  //처음 버튼
  const firstBtn = document.createElement("button");
  firstBtn.innerText = "처음";
  firstBtn.disabled = (currentPage === 1);
  firstBtn.onclick = function() { loadRecentAs(1); };
  box.appendChild(firstBtn);

  //페이지 번호 버튼 (1 ~ totalPages 전체)
  for(let i = 1; i <= totalPages; i++) {
    const pageBtn = document.createElement("button");
    pageBtn.innerText = i;
    if(i === currentPage) {
      pageBtn.classList.add("active");
    }
    //IIFE로 i 캡처
    pageBtn.onclick = (function(p) {
      return function() { loadRecentAs(p); };
    })(i);
    box.appendChild(pageBtn);
  }

  //마지막 버튼
  const lastBtn = document.createElement("button");
  lastBtn.innerText = "마지막";
  lastBtn.disabled = (currentPage === totalPages);
  lastBtn.onclick = function() { loadRecentAs(totalPages); };
  box.appendChild(lastBtn);
}

//상태값을 한글 배지 HTML로 변환
function statusBadge(status) {
  let label = "-";
  let cls = "";

  if(status === "RECEIVED") {
    label = "접수중";
    cls = "receipt";
  } else if(status === "ASSIGNED") {
    label = "배정완료";
    cls = "assigned";
  } else if(status === "IN_PROGRESS") {
    label = "진행중";
    cls = "progress";
  } else if(status === "COMPLETED") {
    label = "완료";
    cls = "complete";
  } else if(status === "CANCELED") {
    label = "취소";
    cls = "canceled";
  }

  return '<span class="badge ' + cls + '">' + label + '</span>';
}

//null/undefined 방어 (빈 문자열로 치환)
function safe(v) {
  if(v === null || v === undefined) return "";
  return v;
}

//상세 모달 열기 (AS 요청 고유번호로 조회)
function openDetailModal(requestNo) {
  axios.get("/dashboard-api/detail/" + requestNo)
    .then(function(response) {
      const data = response.data;

    //데이터가 없으면(존재하지 않는 requestNo) 명확한 안내 후 종료
      if(!data) {
        alert("해당 AS 접수 정보를 찾을 수 없습니다.");
        return;
      }

      document.getElementById("dRequestNo").innerText    = safe(data.requestNo);
      document.getElementById("dCustomerName").innerText = safe(data.customerName);
      document.getElementById("dCustomerTel").innerText  = safe(data.customerTel);
      document.getElementById("dCustomerAddr").innerText = safe(data.customerAddr);
      document.getElementById("dProductType").innerText  = safe(data.productType);
      document.getElementById("dSymptom").innerText      = safe(data.symptom);
      document.getElementById("dRequestDate").innerText  = safe(data.requestDate);
      document.getElementById("dWishDate").innerText     = safe(data.wishDate);

      //상태는 배지로 표시
      document.getElementById("dStatus").innerHTML = statusBadge(data.status);

      //모달 표시
      document.getElementById("detailModal").classList.add("show");
    })
    .catch(function(error) {
      console.error(error);
      alert("상세정보를 불러오지 못했습니다.");
    });
}

//모달 닫기 (X 버튼, 닫기 버튼 공용)
function closeModal() {
  document.getElementById("detailModal").classList.remove("show");
}

//바깥 오버레이 클릭 시 닫기 (내부 클릭은 event.stopPropagation()으로 차단)
function closeModalOnOverlay(event) {
  if(event.target.id === "detailModal") {
    closeModal();
  }
}

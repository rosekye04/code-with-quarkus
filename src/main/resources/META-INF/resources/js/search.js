// document.getElementById('searchForm').addEventListener('submit', function(e) {
// e.preventDefault(); // 폼기본동작차단(새로고침)
// const query = document.getElementById('searchInput').value.trim();
// if (!query) return;
// window.open('https://www.google.com/search?q=' + encodeURIComponent(query), '_blank');
// });

// ── 챔피언 데이터 (modalId 추가됨) ──────────────────────────────
const CHAMPIONS = [
    { name: '아트록스', engName: 'Aatrox', role: '전사', lane: '탑', img: '../image/Aatrox.jpg', difficulty: '상', modalId: 'modalAatrox' },
    { name: '사일러스', engName: 'Sylas', role: '마법사', lane: '정글/미드', img: 'https://ddragon.leagueoflegends.com/cdn/15.24.1/img/champion/Sylas.png', difficulty: '중', modalId: 'modalSylas' },
    { name: '애니비아', engName: 'Anivia', role: '마법사', lane: '미드', img: 'https://ddragon.leagueoflegends.com/cdn/15.24.1/img/champion/Anivia.png', difficulty: '상', modalId: 'modalAnivia' },
    { name: '브라이어', engName: 'Briar', role: '전사', lane: '정글', img: 'https://ddragon.leagueoflegends.com/cdn/15.24.1/img/champion/Briar.png', difficulty: '중', modalId: 'modalBriar' },
    { name: '잭스', engName: 'Jax', role: '전사', lane: '탑', img: 'https://ddragon.leagueoflegends.com/cdn/15.24.1/img/champion/Jax.png', difficulty: '하', modalId: 'modalJax' },
    { name: '징크스', engName: 'Jinx', role: '원거리딜러', lane: '원딜', img: 'https://ddragon.leagueoflegends.com/cdn/15.24.1/img/champion/Jinx.png', difficulty: '중', modalId: 'modalJinx' },
];

// ── 뉴스 데이터 ──────────────────────────────────────────────
const NEWS = [
    { title: '새로운 챔피언 출시', desc: '2026 루나 레벨 이벤트! 신규 챔피언과 함께하는 특별한 시즌.', category: '게임업데이트' },
    { title: '패치노트 16.4', desc: '챔피언 밸런스 및 아이템 업데이트 내용을 확인하세요.', category: '패치노트' },
];

// ── 검색 실행 ────────────────────────────────────────────────
function performSearch(query) {
    const q = query.trim().toLowerCase();
    if (!q) return;
    
    document.getElementById('searchKeywordDisplay').textContent = `"${query}"`;
    
    const champResults = CHAMPIONS.filter(c =>
        c.name.includes(q) || c.engName.toLowerCase().includes(q) ||
        c.role.includes(q) || c.lane.includes(q)
    );
    
    const newsResults = NEWS.filter(n =>
        n.title.toLowerCase().includes(q) || n.desc.toLowerCase().includes(q) || n.category.toLowerCase().includes(q)
    );
    
    document.getElementById('champCount').textContent = `(${champResults.length})`;
    document.getElementById('newsCount').textContent = `(${newsResults.length})`;
    
    const champList = document.getElementById('championResultList');
    if (champResults.length === 0) {
        champList.innerHTML = `<div class="no-result"><h4>검색결과 없음</h4><p>"${query}"에 해당하는 챔피언이 없습니다.</p></div>`;
    } else {
        champList.innerHTML = champResults.map(c => `
            <div class="search-result-card d-flex align-items-center p-0 overflow-hidden mb-3">
                <img src="${c.img}" alt="${c.name}" style="width: 80px; height: 80px;">
                <div class="p-3 flex-grow-1">
                    <div style="font-weight:700; font-size:1rem; color:#111;">${c.name} <span style="color:#888; font-size:0.85rem;">(${c.engName})</span></div>
                    <div style="color:#555; font-size:0.9rem; margin-top:4px;">역할: ${c.role} | 라인: ${c.lane} | 난이도: ${c.difficulty}</div>
                </div>
                <button type="button" class="btn btn-outline-primary btn-sm me-3" 
                        data-bs-toggle="modal" 
                        data-bs-target="#${c.modalId}">상세 보기</button>
            </div>
        `).join('');
    }
    
    // 뉴스 리스트 생략 (기존과 동일)
    // ...
    
    // 화면 전환 로직
    document.querySelector('.hero').classList.add('d-none');
    document.querySelectorAll('section:not(#searchResults)').forEach(s => s.classList.add('d-none'));
    document.getElementById('searchResults').classList.remove('d-none');
    document.getElementById('searchResults').style.display = 'block';
}

// ── 폼 이벤트 ────────────────────────────────────────────────
document.getElementById('searchForm').addEventListener('submit', function(e) {
    e.preventDefault();
    const query = document.getElementById('searchInput').value;
    performSearch(query);
});
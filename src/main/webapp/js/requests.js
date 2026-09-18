let categories = [];
let services = [];

async function loadCategories() {
    const res = await fetchJson('GET', '/api/categories');
    if (res.ok) {
        categories = res.data;
        const catSelect = document.getElementById('categoryId');
        if (catSelect) {
            catSelect.innerHTML = '<option value="">Select Category</option>';
            categories.forEach(c => {
                catSelect.innerHTML += `<option value="${c.categoryId}">${c.name}</option>`;
            });
            catSelect.addEventListener('change', () => loadServicesByCategory(catSelect.value));
        }
    }
}

async function loadServicesByCategory(categoryId) {
    if (!categoryId) return;
    const res = await fetchJson('GET', `/api/services?categoryId=${categoryId}`);
    if (res.ok) {
        services = res.data;
        const svcSelect = document.getElementById('serviceId');
        if (svcSelect) {
            svcSelect.innerHTML = '<option value="">Select Service</option>';
            services.forEach(s => {
                svcSelect.innerHTML += `<option value="${s.serviceId}">${s.name}</option>`;
            });
        }
    }
}

function renderRequestCard(req) {
    return `
        <div class="card" style="cursor:pointer" onclick="window.location='/request-detail.html?id=${req.requestId}'">
            <div class="flex-between mb-1">
                <span class="font-semibold">${escHtml(req.serviceName)}</span>
                ${statusBadge(req.status)}
            </div>
            <div class="text-muted text-sm mb-1">${escHtml(req.categoryName)} &bull; ${escHtml(req.locality)}</div>
            <div class="text-sm mb-1">${escHtml(req.problemDescription.substring(0, 100))}${req.problemDescription.length > 100 ? '…' : ''}</div>
            <div class="flex-between mt-2 text-sm text-muted">
                <span>📅 ${req.preferredDate} at ${req.preferredTime ? req.preferredTime.substring(0,5) : ''}</span>
                <span>👥 ${req.currentMemberCount}/${req.maxGroupSize} members</span>
                ${req.budgetExpectation ? `<span>💰 ₹${req.budgetExpectation}</span>` : ''}
            </div>
        </div>
    `;
}

async function loadRequests(containerId, params) {
    const container = document.getElementById(containerId);
    if (!container) return;
    container.innerHTML = '<div class="loading">Loading requests…</div>';

    let url = '/api/requests';
    if (params) url += '?' + new URLSearchParams(params).toString();

    const res = await fetchJson('GET', url);
    if (!res.ok) {
        container.innerHTML = '<div class="alert alert-error">Failed to load requests.</div>';
        return;
    }
    if (!res.data || res.data.length === 0) {
        container.innerHTML = '<div class="empty-state"><strong>No requests found</strong><p>Try adjusting your search filters.</p></div>';
        return;
    }
    container.innerHTML = '<div class="card-grid">' + res.data.map(renderRequestCard).join('') + '</div>';
}

async function loadMatchingRequests(serviceId, locality, date, containerId) {
    const container = document.getElementById(containerId);
    if (!container) return;
    container.innerHTML = '<div class="loading">Finding compatible groups…</div>';
    const res = await fetchJson('GET', `/api/requests/match?serviceId=${serviceId}&locality=${encodeURIComponent(locality)}&preferredDate=${date}`);
    if (!res.ok || !res.data || res.data.length === 0) {
        container.innerHTML = '<div class="alert alert-info">No compatible groups found for your selection. Your request will start a new group.</div>';
        return;
    }
    container.innerHTML = `
        <div class="alert alert-success">Found ${res.data.length} compatible group(s) you can join:</div>
        <div class="card-grid">${res.data.map(renderRequestCard).join('')}</div>
    `;
}

function escHtml(str) {
    if (!str) return '';
    return str.replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;');
}

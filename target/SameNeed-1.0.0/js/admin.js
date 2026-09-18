async function loadAdminDashboard() {
    const res = await fetchJson('GET', '/api/admin/dashboard');
    if (!res.ok) return;
    const d = res.data;
    document.getElementById('stat-requests').textContent = d.totalRequests ?? 0;
    document.getElementById('stat-bookings').textContent = d.totalBookings ?? 0;
    document.getElementById('stat-customers').textContent = d.totalCustomers ?? 0;
    document.getElementById('stat-providers').textContent = d.totalProviders ?? 0;
    document.getElementById('stat-reports').textContent = d.openReports ?? 0;
    document.getElementById('stat-verified').textContent = d.verifiedProviders ?? 0;
}

async function loadUserTable(containerId, role) {
    const container = document.getElementById(containerId);
    if (!container) return;
    const res = await fetchJson('GET', `/api/admin/${role === 'CUSTOMER' ? 'users' : 'providers'}`);
    if (!res.ok || !res.data) { container.innerHTML = '<div class="alert alert-error">Failed to load.</div>'; return; }

    const rows = res.data.map(u => `
        <tr>
            <td>${u.userId}</td>
            <td>${escAdm(u.displayName)}</td>
            <td>${escAdm(u.email)}</td>
            <td>${statusBadge(u.active ? 'ACTIVE' : 'SUSPENDED')}</td>
            ${role === 'SERVICE_PROVIDER' ? `<td>${statusBadge(u.verified ? 'VERIFIED' : 'UNVERIFIED')}</td>` : ''}
            <td>
                <button class="btn btn-ghost btn-sm" onclick="toggleUser(${u.userId})">${u.active ? 'Suspend' : 'Activate'}</button>
                ${role === 'SERVICE_PROVIDER' && !u.verified ? `<button class="btn btn-success btn-sm" onclick="verifyProvider(${u.userId})">Verify</button>` : ''}
            </td>
        </tr>
    `).join('');

    container.innerHTML = `
        <table>
            <thead>
                <tr>
                    <th>ID</th><th>Name</th><th>Email</th><th>Status</th>
                    ${role === 'SERVICE_PROVIDER' ? '<th>Verified</th>' : ''}
                    <th>Actions</th>
                </tr>
            </thead>
            <tbody>${rows}</tbody>
        </table>
    `;
}

async function toggleUser(userId) {
    const res = await fetchJson('POST', `/api/admin/users/${userId}/toggle-active`);
    if (res.ok) location.reload();
    else alert(res.data?.error || 'Failed to update user');
}

async function verifyProvider(providerId) {
    const res = await fetchJson('POST', `/api/admin/providers/${providerId}/verify`);
    if (res.ok) location.reload();
    else alert(res.data?.error || 'Failed to verify provider');
}

async function loadReportsTable(containerId) {
    const container = document.getElementById(containerId);
    if (!container) return;
    const res = await fetchJson('GET', '/api/admin/reports');
    if (!res.ok || !res.data) { container.innerHTML = '<div class="alert alert-error">Failed to load reports.</div>'; return; }

    const rows = res.data.map(r => `
        <tr>
            <td>${r.reportId}</td>
            <td>${escAdm(r.reporterName)}</td>
            <td>${r.targetType} #${r.targetId}</td>
            <td>${escAdm(r.reason.substring(0,60))}${r.reason.length > 60 ? '…' : ''}</td>
            <td>${statusBadge(r.status)}</td>
            <td>
                <button class="btn btn-ghost btn-sm" onclick="resolveReport(${r.reportId}, 'RESOLVED')">Resolve</button>
                <button class="btn btn-ghost btn-sm" onclick="resolveReport(${r.reportId}, 'DISMISSED')">Dismiss</button>
            </td>
        </tr>
    `).join('');

    container.innerHTML = `
        <table>
            <thead><tr><th>ID</th><th>Reporter</th><th>Target</th><th>Reason</th><th>Status</th><th>Actions</th></tr></thead>
            <tbody>${rows}</tbody>
        </table>
    `;
}

async function resolveReport(reportId, resolution) {
    const res = await fetchJson('POST', `/api/admin/reports/${reportId}/resolve`, { resolution });
    if (res.ok) location.reload();
    else alert(res.data?.error || 'Failed to update report');
}

async function loadMetaExplorer(containerId) {
    const container = document.getElementById(containerId);
    if (!container) return;
    const entityParam = new URLSearchParams(window.location.search).get('entity');
    if (!entityParam) {
        const res = await fetchJson('GET', '/api/admin/meta');
        container.innerHTML = '<div class="card"><p class="text-muted mb-2">Select an entity to explore its fields using Java Reflection:</p>'
            + res.data.map(e => `<a href="?entity=${e}" class="btn btn-outline btn-sm" style="margin:0.25rem">${e}</a>`).join('')
            + '</div>';
        return;
    }
    const res = await fetchJson('GET', `/api/admin/meta?entity=${entityParam}`);
    if (!res.ok) { container.innerHTML = '<div class="alert alert-error">Failed to load entity info.</div>'; return; }
    const d = res.data;
    const rows = d.fields.map(f => `
        <tr><td>${escAdm(f.name)}</td><td>${escAdm(f.type)}</td><td>${escAdm(f.modifier)}</td></tr>
    `).join('');
    container.innerHTML = `
        <div class="card mb-2">
            <p class="text-sm text-muted">Class: <strong>${escAdm(d.className)}</strong> — Extends: <strong>${escAdm(d.superClass)}</strong></p>
        </div>
        <div class="card">
            <table>
                <thead><tr><th>Field Name</th><th>Type</th><th>Modifier</th></tr></thead>
                <tbody>${rows}</tbody>
            </table>
        </div>
        <div class="mt-2"><a href="?" class="btn btn-ghost btn-sm">← Back to list</a></div>
    `;
}

function escAdm(str) {
    if (!str) return '';
    return String(str).replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;');
}

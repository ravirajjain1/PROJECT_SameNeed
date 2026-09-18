const API_BASE = '';

async function fetchJson(method, url, body) {
    const options = {
        method: method,
        headers: { 'Content-Type': 'application/json' },
        credentials: 'same-origin'
    };
    if (body !== undefined && body !== null) {
        options.body = JSON.stringify(body);
    }
    const res = await fetch(API_BASE + url, options);
    const text = await res.text();
    if (!text) return { ok: res.ok, status: res.status, data: null };
    try {
        const data = JSON.parse(text);
        return { ok: res.ok, status: res.status, data };
    } catch (e) {
        return { ok: res.ok, status: res.status, data: { error: text } };
    }
}

function getSession() {
    const data = sessionStorage.getItem('sn_session');
    if (!data) return null;
    try { return JSON.parse(data); } catch(e) { return null; }
}

function setSession(data) {
    sessionStorage.setItem('sn_session', JSON.stringify(data));
}

function clearSession() {
    sessionStorage.removeItem('sn_session');
}

function requireAuth(allowedRoles) {
    const session = getSession();
    if (!session) {
        window.location.href = '/login.html';
        return null;
    }
    if (allowedRoles && allowedRoles.length > 0 && !allowedRoles.includes(session.role)) {
        window.location.href = '/dashboard.html';
        return null;
    }
    return session;
}

function buildNavbar(containerId, activePath) {
    const session = getSession();
    if (!session) return;
    const el = document.getElementById(containerId);
    if (!el) return;

    let navLinks = '';
    if (session.role === 'CUSTOMER') {
        navLinks = `
            <li><a href="/dashboard.html">Dashboard</a></li>
            <li><a href="/requests.html">Browse Requests</a></li>
            <li><a href="/create-request.html">New Request</a></li>
            <li><a href="/bookings.html">Bookings</a></li>
        `;
    } else if (session.role === 'SERVICE_PROVIDER') {
        navLinks = `
            <li><a href="/provider-dashboard.html">Dashboard</a></li>
            <li><a href="/provider-requests.html">Group Requests</a></li>
            <li><a href="/bookings.html">Bookings</a></li>
        `;
    } else if (session.role === 'ADMIN') {
        navLinks = `
            <li><a href="/admin/index.html">Dashboard</a></li>
            <li><a href="/admin/users.html">Users</a></li>
            <li><a href="/admin/providers.html">Providers</a></li>
            <li><a href="/admin/categories.html">Categories</a></li>
            <li><a href="/admin/reports.html">Reports</a></li>
        `;
    }

    el.innerHTML = `
        <a href="/" class="navbar-brand">Same<span>Need</span></a>
        <ul class="navbar-nav">${navLinks}</ul>
        <div class="navbar-right">
            <a href="/notifications.html" class="btn btn-ghost btn-sm" id="notif-btn">🔔 <span id="notif-count"></span></a>
            <a href="/profile.html" class="btn btn-ghost btn-sm">${session.displayName}</a>
            <button class="btn btn-ghost btn-sm" onclick="logout()">Logout</button>
        </div>
    `;
    loadNotifCount();
}

async function loadNotifCount() {
    const res = await fetchJson('GET', '/api/notifications');
    if (res.ok && res.data) {
        const unread = res.data.filter(n => !n.read).length;
        const el = document.getElementById('notif-count');
        if (el && unread > 0) el.textContent = unread;
    }
}

async function logout() {
    await fetchJson('POST', '/api/auth/logout');
    clearSession();
    window.location.href = '/login.html';
}

function showAlert(containerId, message, type) {
    const el = document.getElementById(containerId);
    if (!el) return;
    el.className = 'alert alert-' + (type || 'info');
    el.textContent = message;
    el.style.display = 'block';
}

function hideAlert(containerId) {
    const el = document.getElementById(containerId);
    if (el) el.style.display = 'none';
}

function statusBadge(status) {
    if (!status) return '';
    const cls = 'badge-' + status.toLowerCase().replace(/_/g, '_');
    return `<span class="badge ${cls}">${status.replace(/_/g, ' ')}</span>`;
}

function formatDate(dateStr) {
    if (!dateStr) return '';
    const d = new Date(dateStr);
    return d.toLocaleDateString('en-IN', { day:'numeric', month:'short', year:'numeric' });
}

function formatDateTime(dtStr) {
    if (!dtStr) return '';
    const d = new Date(dtStr);
    return d.toLocaleString('en-IN', { day:'numeric', month:'short', year:'numeric', hour:'2-digit', minute:'2-digit' });
}

function getParam(name) {
    return new URLSearchParams(window.location.search).get(name);
}

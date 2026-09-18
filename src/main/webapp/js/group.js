let ws = null;
let myAlias = null;

function initChat(requestId, alias) {
    myAlias = alias;
    const proto = location.protocol === 'https:' ? 'wss' : 'ws';
    const url = `${proto}://${location.host}${location.pathname.split('/').slice(0,-1).join('/')}/ws/chat/${requestId}`;
    ws = new WebSocket(url);

    ws.onopen = () => {
        const statusEl = document.getElementById('chat-status');
        if (statusEl) statusEl.textContent = 'Connected';
    };

    ws.onmessage = (event) => {
        const msg = JSON.parse(event.data);
        appendMessage(msg);
    };

    ws.onclose = () => {
        const statusEl = document.getElementById('chat-status');
        if (statusEl) statusEl.textContent = 'Disconnected';
    };

    ws.onerror = () => {
        const statusEl = document.getElementById('chat-status');
        if (statusEl) statusEl.textContent = 'Error';
    };

    const form = document.getElementById('chat-form');
    if (form) {
        form.addEventListener('submit', (e) => {
            e.preventDefault();
            sendMessage();
        });
    }
}

function sendMessage() {
    const input = document.getElementById('chat-input');
    if (!input || !ws || ws.readyState !== WebSocket.OPEN) return;
    const content = input.value.trim();
    if (!content) return;
    ws.send(JSON.stringify({ content }));
    input.value = '';
}

function appendMessage(msg) {
    const container = document.getElementById('chat-messages');
    if (!container) return;
    const isMine = msg.senderAlias === myAlias;
    const div = document.createElement('div');
    div.className = 'chat-msg ' + (isMine ? 'mine' : 'other');
    div.innerHTML = `
        <div class="chat-bubble">${escapeHtml(msg.content)}</div>
        <div class="chat-meta">${escapeHtml(msg.senderAlias)} &bull; ${formatMsgTime(msg.sentAt)}</div>
    `;
    container.appendChild(div);
    container.scrollTop = container.scrollHeight;
}

function formatMsgTime(ts) {
    if (!ts) return '';
    try {
        const d = new Date(ts);
        return d.toLocaleTimeString('en-IN', { hour: '2-digit', minute: '2-digit' });
    } catch(e) { return ts; }
}

async function loadChatHistory(requestId) {
    const res = await fetchJson('GET', `/api/chat?requestId=${requestId}`);
    if (res.ok && res.data) {
        res.data.forEach(msg => appendMessage(msg));
    }
}

function renderMembers(members, containerId) {
    const el = document.getElementById(containerId);
    if (!el) return;
    el.innerHTML = '';
    members.forEach(m => {
        const chip = document.createElement('span');
        chip.className = 'member-chip' + (m.anonymousAlias === 'Group Creator' ? ' creator' : '');
        chip.textContent = m.anonymousAlias;
        el.appendChild(chip);
    });
}

function escapeHtml(str) {
    if (!str) return '';
    return str.replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;');
}

async function loadOffersForRequest(requestId, containerId, session) {
    const container = document.getElementById(containerId);
    if (!container) return;
    container.innerHTML = '<div class="loading">Loading offers…</div>';

    const res = await fetchJson('GET', `/api/offers?requestId=${requestId}`);
    if (!res.ok || !res.data || res.data.length === 0) {
        container.innerHTML = '<div class="empty-state"><strong>No offers yet</strong><p>Set the request status to PROVIDER_CONTACTED to invite providers.</p></div>';
        return;
    }

    container.innerHTML = '';
    for (const offer of res.data) {
        const negRes = await fetchJson('GET', `/api/offers/${offer.offerId}/negotiations`);
        const negotiations = negRes.ok ? negRes.data : [];

        const respRes = await fetchJson('GET', `/api/offers/${offer.offerId}/responses`);
        const responses = respRes.ok ? respRes.data : [];

        const card = document.createElement('div');
        card.className = 'card mb-2';
        card.innerHTML = `
            <div class="flex-between mb-1">
                <span class="font-semibold">₹${offer.amountPerMember} per member</span>
                ${statusBadge(offer.status)}
            </div>
            <div class="text-sm text-muted mb-1">
                Provider: ${escOff(offer.providerName)} &bull;
                Required: ${offer.requiredMembers} member(s) &bull;
                Valid until: ${formatDateTime(offer.validUntil)}
            </div>
            <div class="negotiation-timeline mb-2">
                ${negotiations.map(n => `
                    <div class="neg-item ${n.senderRole === 'COORDINATOR' ? 'coordinator' : ''}">
                        <div class="neg-label">${n.senderRole === 'PROVIDER' ? 'Provider' : 'Coordinator'}</div>
                        <div class="neg-amount">₹${n.amount}</div>
                        ${n.message ? `<div class="neg-message">${escOff(n.message)}</div>` : ''}
                    </div>
                `).join('')}
            </div>
            ${renderOfferActions(offer, responses, session)}
        `;
        container.appendChild(card);
    }
}

function renderOfferActions(offer, responses, session) {
    if (offer.status === 'ACCEPTED' || offer.status === 'REJECTED' || offer.status === 'EXPIRED') {
        if (offer.status === 'ACCEPTED' && responses.length > 0) {
            return `<div class="text-sm"><strong>Member responses:</strong> ${responses.map(r =>
                `${escOff(r.memberAlias)}: ${statusBadge(r.status)}`).join(' ')}</div>`;
        }
        return '';
    }

    let html = '';
    if (session.role === 'CUSTOMER' && offer.status === 'PENDING') {
        html += `
            <div class="flex gap-sm mt-1">
                <button class="btn btn-success btn-sm" onclick="acceptOffer(${offer.offerId})">Accept Offer</button>
                <button class="btn btn-ghost btn-sm" onclick="showCounterForm(${offer.offerId})">Counter Offer</button>
                <button class="btn btn-danger btn-sm" onclick="rejectOffer(${offer.offerId})">Reject</button>
            </div>
            <div id="counter-form-${offer.offerId}" style="display:none;margin-top:0.75rem">
                <div class="form-row">
                    <div class="form-group">
                        <input class="form-control" id="counter-amount-${offer.offerId}" type="number" placeholder="Your offer amount (₹)">
                    </div>
                    <div class="form-group">
                        <input class="form-control" id="counter-msg-${offer.offerId}" type="text" placeholder="Message (optional)">
                    </div>
                </div>
                <button class="btn btn-primary btn-sm" onclick="submitCounter(${offer.offerId}, false)">Send Counter</button>
            </div>
        `;
    } else if (session.role === 'SERVICE_PROVIDER' && offer.status === 'COUNTERED') {
        html += `
            <div class="flex gap-sm mt-1">
                <button class="btn btn-ghost btn-sm" onclick="showCounterForm(${offer.offerId})">Counter Back</button>
            </div>
            <div id="counter-form-${offer.offerId}" style="display:none;margin-top:0.75rem">
                <div class="form-row">
                    <div class="form-group">
                        <input class="form-control" id="counter-amount-${offer.offerId}" type="number" placeholder="Your revised amount (₹)">
                    </div>
                    <div class="form-group">
                        <input class="form-control" id="counter-msg-${offer.offerId}" type="text" placeholder="Message (optional)">
                    </div>
                </div>
                <button class="btn btn-primary btn-sm" onclick="submitCounter(${offer.offerId}, true)">Send Counter</button>
            </div>
        `;
    }
    return html;
}

function showCounterForm(offerId) {
    const form = document.getElementById('counter-form-' + offerId);
    if (form) form.style.display = form.style.display === 'none' ? 'block' : 'none';
}

async function acceptOffer(offerId) {
    const res = await fetchJson('POST', `/api/offers/${offerId}/accept`);
    if (res.ok) { showAlert('offer-msg', 'Offer accepted! Members can now respond.', 'success'); location.reload(); }
    else showAlert('offer-msg', res.data?.error || 'Failed to accept offer', 'error');
}

async function rejectOffer(offerId) {
    if (!confirm('Reject this offer?')) return;
    const res = await fetchJson('POST', `/api/offers/${offerId}/reject`);
    if (res.ok) { showAlert('offer-msg', 'Offer rejected.', 'info'); location.reload(); }
    else showAlert('offer-msg', res.data?.error || 'Failed to reject offer', 'error');
}

async function submitCounter(offerId, isProvider) {
    const amount = document.getElementById('counter-amount-' + offerId)?.value;
    const message = document.getElementById('counter-msg-' + offerId)?.value;
    if (!amount) { showAlert('offer-msg', 'Please enter a counter amount', 'error'); return; }
    const res = await fetchJson('POST', `/api/offers/${offerId}/counter`, { amount, message });
    if (res.ok) { showAlert('offer-msg', 'Counter offer sent.', 'success'); location.reload(); }
    else showAlert('offer-msg', res.data?.error || 'Failed to send counter offer', 'error');
}

function escOff(str) {
    if (!str) return '';
    return str.replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;');
}

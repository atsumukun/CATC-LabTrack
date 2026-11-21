(() => {
    'use strict';

    const body = document.body;
    const csrfHeader = body.dataset.csrfHeader;
    const csrfToken = body.dataset.csrfToken;
    const reserveUrl = body.dataset.reserveUrl || '/api/reservations';
    const MAX_QTY = Number(body.dataset.maxQty) || 3;

    const $ = (selector, root = document) => root.querySelector(selector);
    const $$ = (selector, root = document) => Array.from(root.querySelectorAll(selector));

    /* ---------- Live search over the rows already on the page ---------- */
    const search = $('#search');
    const rows = $$('#equipment-table tbody tr[data-id]');
    const noResults = $('#no-results');
    const resultCount = $('#result-count');

    function applySearch() {
        const q = search.value.trim().toLowerCase();
        let shown = 0;
        rows.forEach((row) => {
            const match = !q || [row.dataset.code, row.dataset.name, row.dataset.location].some(value => value.toLowerCase().includes(q));
            row.hidden = !match;
            if (match) shown++;
        });
        if (noResults) noResults.hidden = shown !== 0 || rows.length === 0;
        if (resultCount) resultCount.textContent = `Showing ${shown} of ${rows.length}`;
    }
    if (search) {
        search.addEventListener('input', applySearch);
        applySearch();
    }

    /* ---------- Reservation dialog ---------- */
    const dialog = $('#reserve-dialog');
    const successDialog = $('#success-dialog');
    if (!dialog || !successDialog) return;

    const form = $('#reserve-form');
    const sections = $$('[data-step]', form);
    const stepLabels = $$('.steps li', form);
    const backBtn = $('#back-btn');
    const nextBtn = $('#next-btn');
    const errorBox = $('#form-error');
    const qtyValue = $('#qty-value');
    const qtyMinus = $('#qty-minus');
    const qtyPlus = $('#qty-plus');
    const qtyHint = $('#qty-hint');
    const reviewList = $('#review-list');

    const state = { step: 1, qty: 1, max: 1, item: null, busy: false, stale: false };
    const NEXT_LABEL = { 1: 'Continue', 2: 'Review', 3: 'Confirm reservation' };

    function todayISO() {
        const d = new Date();
        const pad = (n) => String(n).padStart(2, '0');
        return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`;
    }

    function prettyDate(iso) {
        const d = new Date(`${iso}T00:00:00`);
        return Number.isNaN(d.getTime())
            ? iso
            : d.toLocaleDateString(undefined, { year: 'numeric', month: 'long', day: 'numeric' });
    }

    function showError(message) {
        errorBox.textContent = message;
        errorBox.hidden = false;
    }

    function hideError() {
        errorBox.hidden = true;
        errorBox.textContent = '';
    }

    function renderQuantity() {
        qtyValue.textContent = String(state.qty);
        qtyMinus.disabled = state.qty <= 1;
        qtyPlus.disabled = state.qty >= state.max;
        const free = state.item.available;
        qtyHint.textContent = free < MAX_QTY
            ? `Only ${free} ${free === 1 ? 'unit is' : 'units are'} free right now.`
            : `${free} of ${state.item.total} units free. Maximum of ${MAX_QTY} per request.`;
    }

    function showStep(step) {
        state.step = step;
        sections.forEach((s) => { s.hidden = Number(s.dataset.step) !== step; });
        stepLabels.forEach((li, i) => {
            li.classList.toggle('done', i + 1 < step);
            li.classList.toggle('current', i + 1 === step);
            if (i + 1 === step) li.setAttribute('aria-current', 'step');
            else li.removeAttribute('aria-current');
        });
        backBtn.hidden = step === 1;
        nextBtn.textContent = NEXT_LABEL[step];
        hideError();
    }

    function renderReview() {
        const v = Object.fromEntries(new FormData(form).entries());
        const entries = [
            ['Equipment', `${state.item.name} (${state.item.code})`],
            ['Location', state.item.location],
            ['Quantity', String(state.qty)],
            ['Name', v.borrowerName.trim()],
            ['Year level', v.yearLevel],
            ['Course', v.course.trim()],
            ['Subject to use', v.subject.trim()],
            ['Date of use', prettyDate(v.useDate)],
        ];
        const nodes = [];
        entries.forEach(([label, value]) => {
            const dt = document.createElement('dt');
            dt.textContent = label;
            const dd = document.createElement('dd');
            dd.textContent = value; // textContent keeps typed text from being treated as HTML
            nodes.push(dt, dd);
        });
        reviewList.replaceChildren(...nodes);
    }

    function openFor(row) {
        const available = Number(row.dataset.available);
        const img = $('img', row);
        state.item = {
            id: Number(row.dataset.id),
            code: row.dataset.code,
            name: row.dataset.name,
            location: row.dataset.location,
            available,
            total: Number(row.dataset.total),
        };
        state.max = Math.min(available, MAX_QTY);
        state.qty = 1;
        state.stale = false;

        form.reset();
        const date = form.elements.useDate;
        date.min = todayISO();
        date.value = todayISO();

        $('#dlg-title').textContent = state.item.name;
        $('#dlg-code').textContent = state.item.code;
        $('#dlg-location').textContent = state.item.location;
        const preview = $('#dlg-image');
        preview.hidden = !img;
        if (img) preview.src = img.src; else preview.removeAttribute('src');

        renderQuantity();
        showStep(1);
        dialog.showModal();
    }

    function setBusy(busy) {
        state.busy = busy;
        nextBtn.disabled = busy;
        backBtn.disabled = busy;
        nextBtn.textContent = busy ? 'Reserving…' : NEXT_LABEL[state.step];
    }

    async function submitReservation() {
        const v = Object.fromEntries(new FormData(form).entries());
        const payload = {
            equipmentId: state.item.id,
            quantity: state.qty,
            borrowerName: v.borrowerName.trim(),
            yearLevel: v.yearLevel,
            course: v.course.trim(),
            subject: v.subject.trim(),
            useDate: v.useDate,
        };

        hideError();
        setBusy(true);
        try {
            const response = await fetch(reserveUrl, {
                method: 'POST',
                credentials: 'same-origin',
                headers: {
                    'Content-Type': 'application/json',
                    Accept: 'application/json',
                    [csrfHeader]: csrfToken,
                },
                body: JSON.stringify(payload),
            });

            if (response.ok) {
                const saved = await response.json();
                showSuccess(saved);
                return;
            }

            const data = await response.json().catch(() => ({}));
            if (response.status === 401 || response.status === 403) {
                showError('Your session may have expired. Please refresh the page and sign in again.');
            } else {
                showError(data.message || 'Something went wrong. Please try again.');
            }
            if (response.status === 409 || response.status === 404) state.stale = true;
            // A validation problem sends the person back to the form to fix it.
            if (response.status === 400 && state.step === 3) {
                showStep(2);
                showError(data.message || 'Please check your details.');
            }
        } catch (err) {
            showError('Could not reach the server. Check your connection and try again.');
        } finally {
            setBusy(false);
        }
    }

    function showSuccess(saved) {
        $('#success-ref').textContent = saved.reference;
        $('#success-summary').textContent =
            `${saved.quantity} × ${saved.equipmentName} for ${prettyDate(saved.useDate)}.`;
        state.stale = false;
        dialog.close();
        successDialog.showModal();
    }

    /* ---------- Wiring ---------- */
    $('#equipment-table').addEventListener('click', (event) => {
        const button = event.target.closest('.reserve');
        if (!button || button.disabled) return;
        openFor(button.closest('tr'));
    });

    qtyMinus.addEventListener('click', () => {
        if (state.qty > 1) { state.qty--; renderQuantity(); }
    });
    qtyPlus.addEventListener('click', () => {
        if (state.qty < state.max) { state.qty++; renderQuantity(); }
    });

    dialog.addEventListener('cancel', event => { if (state.busy) event.preventDefault(); });
    backBtn.addEventListener('click', () => showStep(Math.max(1, state.step - 1)));

    form.addEventListener('submit', (event) => {
        event.preventDefault();
        if (state.busy) return;
        if (state.step === 1) { showStep(2); return; }
        if (state.step === 2) {
            if (!form.reportValidity()) return;
            renderReview();
            showStep(3);
            return;
        }
        submitReservation();
    });

    $$('[data-close]').forEach((button) => {
        button.addEventListener('click', () => { if (!state.busy) button.closest('dialog').close(); });
    });

    [dialog, successDialog].forEach((d) => {
        d.addEventListener('click', (event) => {
            if (event.target === d && !state.busy) d.close(); // click on the dimmed backdrop
        });
    });

    // After a successful booking, or a stock conflict, reload so the list shows the real numbers.
    dialog.addEventListener('close', () => { if (state.stale) window.location.reload(); });
    successDialog.addEventListener('close', () => window.location.reload());
})();
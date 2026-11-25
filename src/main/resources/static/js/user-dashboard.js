(() => {
    'use strict';

    const body = document.body;
    const csrfHeader = body.dataset.csrfHeader;
    const csrfToken = body.dataset.csrfToken;
    const reserveUrl = body.dataset.reserveUrl || '/api/reservations';
    const MAX_QTY = Number(body.dataset.maxQty) || 3;
    const RETURN_DAYS = Number(body.dataset.returnDays) || 3;

    const $ = (selector, root = document) => root.querySelector(selector);
    const $$ = (selector, root = document) => Array.from(root.querySelectorAll(selector));

    /* ---------- Date helpers (all date-only, no time zones involved) ---------- */
    const pad = (n) => String(n).padStart(2, '0');
    const toISO = (d) => `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`;
    const parseISO = (iso) => new Date(`${iso}T00:00:00`);
    const todayISO = () => toISO(new Date());
    function addDays(iso, days) {
        const d = parseISO(iso);
        d.setDate(d.getDate() + days);
        return toISO(d);
    }
    function pretty(iso, withWeekday = false) {
        const d = parseISO(iso);
        if (Number.isNaN(d.getTime())) return iso;
        return d.toLocaleDateString(undefined, {
            weekday: withWeekday ? 'long' : undefined, year: 'numeric', month: 'long', day: 'numeric',
        });
    }

    /* ---------- Live search over the rows already on the page ---------- */
    const search = $('#search');
    const rows = $$('#equipment-table tbody tr[data-id]');
    const noResults = $('#no-results');
    const resultCount = $('#result-count');

    function applySearch() {
        const q = search.value.trim().toLowerCase();
        let shown = 0;
        rows.forEach((row) => {
            const match = !q || row.textContent.toLowerCase().includes(q);
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
    const returnHint = $('#return-hint span');
    const agree = $('#agree');

    // The student's own details, rendered by the server from their profile.
    const profile = {
        name: dialog.dataset.profileName,
        id: dialog.dataset.profileId,
        program: dialog.dataset.profileProgram,
        year: dialog.dataset.profileYear,
    };

    const state = { step: 1, qty: 1, max: 1, item: null, busy: false, stale: false };
    const NEXT_LABEL = { 1: 'Continue', 2: 'Review', 3: 'Confirm reservation' };

    function setThumb(img, src, letter) {
        img.classList.remove('broken');
        img.parentElement.dataset.letter = letter;
        img.onerror = () => img.classList.add('broken');
        if (src) img.src = src;
        else { img.removeAttribute('src'); img.classList.add('broken'); }
    }

    function showError(message) {
        errorBox.textContent = message;
        errorBox.hidden = false;
    }

    function hideError() {
        errorBox.hidden = true;
        errorBox.textContent = '';
    }

    // The button stays off while sending, and on the last step until the box is ticked.
    function syncNext() {
        nextBtn.disabled = state.busy || (state.step === 3 && !agree.checked);
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

    function updateReturnHint() {
        const value = form.elements.useDate.value;
        returnHint.textContent = value
            ? `Return by ${pretty(addDays(value, RETURN_DAYS), true)} (${RETURN_DAYS} days after you borrow).`
            : 'Pick a borrow date to see your return date.';
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
        syncNext();
    }

    function renderReview() {
        const v = Object.fromEntries(new FormData(form).entries());
        const borrow = v.useDate;
        const due = addDays(borrow, RETURN_DAYS);

        setThumb($('#c-img'), state.item.image, state.item.name.charAt(0).toUpperCase());
        $('#c-name').textContent = state.item.name;
        $('#c-desc').textContent = state.item.description;
        $('#c-code').textContent = state.item.code;
        $('#c-loc').textContent = state.item.location;
        $('#c-qty').textContent = `× ${state.qty}`;
        $('#c-borrow').textContent = pretty(borrow, true);
        $('#c-return').textContent = pretty(due, true);
        $('#c-return-note').textContent = `${RETURN_DAYS} days after borrowing`;

        const entries = [
            ['Borrower', profile.name],
            ['Student ID', profile.id],
            ['Program', profile.program],
            ['Year level', profile.year],
            ['Instructor', v.instructorName.trim()],
            ['Subject to use', v.subject.trim()],
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
            description: row.dataset.description || '',
            location: row.dataset.location,
            image: img ? img.getAttribute('src') : '',
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
        updateReturnHint();

        $('#dlg-title').textContent = state.item.name;
        $('#dlg-code').textContent = state.item.code;
        $('#dlg-location').textContent = state.item.location;
        setThumb($('#dlg-image'), state.item.image, state.item.name.charAt(0).toUpperCase());

        renderQuantity();
        showStep(1);
        dialog.showModal();
    }

    function setBusy(busy) {
        state.busy = busy;
        backBtn.disabled = busy;
        nextBtn.textContent = busy ? 'Reserving…' : NEXT_LABEL[state.step];
        syncNext();
    }

    async function submitReservation() {
        const v = Object.fromEntries(new FormData(form).entries());
        const payload = {
            equipmentId: state.item.id,
            quantity: state.qty,
            instructorName: v.instructorName.trim(),
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
                showSuccess(await response.json());
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
        $('#success-summary').textContent = `${saved.quantity} × ${saved.equipmentName}`;
        $('#success-borrow').textContent = pretty(saved.useDate);
        $('#success-return').textContent = pretty(saved.returnDate, true);
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

    form.elements.useDate.addEventListener('input', updateReturnHint);
    agree.addEventListener('change', syncNext);
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
        if (!agree.checked) return;
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

    // After a booking, or a stock conflict, reload so the list shows the real numbers.
    dialog.addEventListener('close', () => { if (state.stale) window.location.reload(); });
    successDialog.addEventListener('close', () => window.location.reload());
})();
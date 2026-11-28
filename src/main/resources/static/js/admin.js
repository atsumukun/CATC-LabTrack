(() => {
    'use strict';
    const $ = (sel, root = document) => root.querySelector(sel);
    const csrf = { header: document.body.dataset.csrfHeader, token: document.body.dataset.csrfToken };

    const ICON_WARN = '<svg class="i" viewBox="0 0 24 24"><path d="M12 3l10 18H2z"/><path d="M12 10v5M12 18h.01"/></svg>';
    const ICON_SAVE = '<svg class="i" viewBox="0 0 24 24"><path d="M20 6L9 17l-5-5"/></svg>';

    /* ---------- helpers ---------- */
    async function send(url, method, body) {
        let res;
        try {
            res = await fetch(url, {
                method,
                headers: { 'Content-Type': 'application/json', [csrf.header]: csrf.token },
                body: body === undefined ? undefined : JSON.stringify(body)
            });
        } catch {
            throw new Error('Could not reach LabTrack. Check the server and try again.');
        }
        if (res.ok) return;
        const err = await res.json().catch(() => ({}));
        const message = err.message || `Request failed (${res.status}). Refresh and try again.`;
        const fail = new Error(message);
        fail.duplicate = res.status === 409 && /already registered/i.test(message);
        throw fail;
    }

    let toastTimer;
    function toast(message, isError) {
        const el = $('#toast');
        el.textContent = message;
        el.classList.toggle('error', !!isError);
        el.classList.add('show');
        clearTimeout(toastTimer);
        toastTimer = setTimeout(() => el.classList.remove('show'), 4000);
    }

    const busy = (btn, on) => { btn.disabled = on; btn.classList.toggle('is-busy', on); };

    // Warning pop-up (e.g. "already registered")
    function notice(title, text) {
        $('#notice-title').textContent = title;
        $('#notice-text').textContent = text;
        $('#notice-dialog').showModal();
    }

    // Close a dialog on backdrop click or any [data-close] button
    document.querySelectorAll('dialog').forEach(d => {
        d.addEventListener('click', e => { if (e.target === d) d.close(); });
        d.querySelectorAll('[data-close]').forEach(b => b.addEventListener('click', () => d.close()));
    });

    // Picture tile: image if it loads, otherwise the first letter
    function setPreview(thumb, name, url) {
        const img = $('img', thumb);
        thumb.dataset.letter = (name || '?').trim().charAt(0).toUpperCase() || '?';
        img.classList.remove('broken');
        img.onerror = () => img.classList.add('broken');
        if (url && url.trim()) img.src = url.trim();
        else { img.removeAttribute('src'); img.classList.add('broken'); }
    }

    /* ---------- Confirm pop-up (delete / cancel / save) ---------- */
    const dlg = $('#confirm-dialog'), yes = $('#confirm-yes');
    let pending = null, pendingError = null;

    function ask({ title, text, target, label, tone = 'danger' }, action, onError) {
        const info = tone === 'info';
        $('#confirm-title').textContent = title;
        $('#confirm-text').textContent = text;
        $('#confirm-target').textContent = target;
        $('#confirm-icon').className = 'warn-icon' + (info ? ' info' : '');
        $('#confirm-icon').innerHTML = info ? ICON_SAVE : ICON_WARN;
        $('#confirm-cancel').textContent = info ? 'Go back' : 'Keep it';
        yes.className = 'btn ' + (info ? 'btn-primary' : 'btn-danger');
        yes.textContent = label;
        yes.disabled = false;
        $('#confirm-error').hidden = true;
        pending = action;
        pendingError = onError || null;
        dlg.showModal();
    }

    yes.addEventListener('click', async () => {
        if (!pending) return;
        busy(yes, true);
        try {
            await pending();
            window.location.reload();
        } catch (err) {
            busy(yes, false);
            if (pendingError) pendingError(err);
            else {
                $('#confirm-error').textContent = err.message;
                $('#confirm-error').hidden = false;
            }
        }
    });

    /* ---------- Register a new item ---------- */
    const createForm = $('#create-form');
    createForm.addEventListener('submit', async e => {
        e.preventDefault();
        const out = $('output', createForm), btn = $('button[type=submit]', createForm);
        out.textContent = '';
        busy(btn, true);
        try {
            const d = Object.fromEntries(new FormData(createForm));
            d.totalUnits = Number(d.totalUnits) || 1;
            await send('/api/equipment', 'POST', d);
            window.location.reload();
        } catch (err) {
            if (err.duplicate) notice('Already registered', err.message);
            else out.textContent = err.message;
            busy(btn, false);
        }
    });

    /* ---------- Edit pop-up ---------- */
    const FIELDS = {
        assetCode: 'Asset code', name: 'Equipment name', description: 'Description',
        imageUrl: 'Picture link', location: 'Lab location', totalUnits: 'Units in stock', status: 'Status'
    };
    const editDlg = $('#edit-dialog'), editForm = $('#edit-form');
    const editErr = $('#edit-error'), editThumb = $('#edit-thumb');
    const el = name => editForm.elements[name];
    let editId = null, snapshot = {};

    const refreshPreview = () => setPreview(editThumb, el('name').value, el('imageUrl').value);
    el('name').addEventListener('input', refreshPreview);
    el('imageUrl').addEventListener('input', refreshPreview);

    document.querySelectorAll('.js-edit').forEach(btn => btn.addEventListener('click', () => {
        const d = btn.closest('tr').dataset;
        editId = d.id;
        snapshot = {
            assetCode: d.code || '', name: d.name || '', description: d.description || '',
            imageUrl: d.image || '', location: d.location || '',
            totalUnits: String(d.total || 1), status: d.status
        };
        Object.keys(FIELDS).forEach(k => { el(k).value = snapshot[k]; });
        $('#edit-title').textContent = d.name;
        editErr.hidden = true;
        refreshPreview();
        editDlg.showModal();
    }));

    editForm.addEventListener('submit', e => {
        e.preventDefault();
        editErr.hidden = true;
        const d = Object.fromEntries(new FormData(editForm));
        const changed = Object.keys(FIELDS).filter(k => String(d[k] ?? '').trim() !== String(snapshot[k]).trim());

        // Nothing changed: no need to bother the admin
        if (!changed.length) {
            editDlg.close();
            toast('No changes to save.');
            return;
        }

        const body = { ...d, totalUnits: Number(d.totalUnits) || 1 };
        ask({
            tone: 'info',
            title: 'Save these changes?',
            text: 'You are about to update: ' + changed.map(k => FIELDS[k]).join(', ') + '.',
            target: snapshot.name,
            label: 'Yes, save changes'
        }, () => send(`/api/equipment/${editId}`, 'PUT', body), err => {
            dlg.close();
            if (err.duplicate) notice('Already registered', err.message);
            else { editErr.textContent = err.message; editErr.hidden = false; }
        });
    });

    /* ---------- Delete equipment ---------- */
    document.querySelectorAll('.js-delete').forEach(btn => btn.addEventListener('click', () => {
        const d = btn.closest('tr').dataset;
        ask({
            title: 'Delete this equipment?',
            text: 'This will permanently remove the item from the laboratory inventory. This cannot be undone.',
            target: `${d.name} (${d.code})`,
            label: 'Yes, delete'
        }, () => send(`/api/equipment/${d.id}`, 'DELETE'));
    }));

    /* ---------- Reservation actions ---------- */
    document.querySelectorAll('.js-reservation').forEach(btn => btn.addEventListener('click', async () => {
        const { url, act, ref, equipment } = btn.dataset;
        if (act === 'CANCELLED') {
            ask({
                title: 'Cancel this reservation?',
                text: 'The reserved units will be released back to the inventory.',
                target: `${ref} · ${equipment}`,
                label: 'Yes, cancel it'
            }, () => send(url, 'PUT'));
            return;
        }
        busy(btn, true);
        try {
            await send(url, 'PUT');
            window.location.reload();
        } catch (err) {
            toast(err.message, true);
            busy(btn, false);
        }
    }));
})();
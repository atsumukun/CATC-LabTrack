(() => {
    'use strict';
    const $ = (sel, root = document) => root.querySelector(sel);
    const csrf = { header: document.body.dataset.csrfHeader, token: document.body.dataset.csrfToken };

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
        throw new Error(err.message || `Request failed (${res.status}). Refresh and try again.`);
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

    // Close a dialog when the dark backdrop is clicked, or any [data-close] is pressed
    document.querySelectorAll('dialog').forEach(d => {
        d.addEventListener('click', e => { if (e.target === d) d.close(); });
        d.querySelectorAll('[data-close]').forEach(b => b.addEventListener('click', () => d.close()));
    });

    // Picture tile: image if it loads, otherwise the first letter (same as the user page)
    function setPreview(thumb, name, url) {
        const img = $('img', thumb);
        thumb.dataset.letter = (name || '?').trim().charAt(0).toUpperCase() || '?';
        img.classList.remove('broken');
        img.onerror = () => img.classList.add('broken');
        if (url && url.trim()) img.src = url.trim();
        else { img.removeAttribute('src'); img.classList.add('broken'); }
    }

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
            out.textContent = err.message;
            busy(btn, false);
        }
    });

    /* ---------- Edit pop-up ---------- */
    const editDlg = $('#edit-dialog'), editForm = $('#edit-form');
    const editErr = $('#edit-error'), editThumb = $('#edit-thumb');
    let editId = null;

    const refreshPreview = () => setPreview(editThumb, editForm.elements.name.value, editForm.elements.imageUrl.value);
    editForm.elements.name.addEventListener('input', refreshPreview);
    editForm.elements.imageUrl.addEventListener('input', refreshPreview);

    document.querySelectorAll('.js-edit').forEach(btn => btn.addEventListener('click', () => {
        const d = btn.closest('tr').dataset;
        editId = d.id;
        editForm.elements.assetCode.value = d.code || '';
        editForm.elements.name.value = d.name || '';
        editForm.elements.description.value = d.description || '';
        editForm.elements.imageUrl.value = d.image || '';
        editForm.elements.location.value = d.location || '';
        editForm.elements.totalUnits.value = d.total || 1;
        editForm.elements.status.value = d.status;
        $('#edit-title').textContent = d.name;
        editErr.hidden = true;
        refreshPreview();
        editDlg.showModal();
    }));

    editForm.addEventListener('submit', async e => {
        e.preventDefault();
        const btn = $('#edit-save');
        editErr.hidden = true;
        busy(btn, true);
        try {
            const d = Object.fromEntries(new FormData(editForm));
            d.totalUnits = Number(d.totalUnits) || 1;
            await send(`/api/equipment/${editId}`, 'PUT', d);
            window.location.reload();
        } catch (err) {
            editErr.textContent = err.message;
            editErr.hidden = false;
            busy(btn, false);
        }
    });

    /* ---------- Confirm pop-up (delete equipment / cancel reservation) ---------- */
    const dlg = $('#confirm-dialog'), yes = $('#confirm-yes');
    let pending = null;

    function ask({ title, text, target, label }, action) {
        $('#confirm-title').textContent = title;
        $('#confirm-text').textContent = text;
        $('#confirm-target').textContent = target;
        yes.textContent = label;
        $('#confirm-error').hidden = true;
        pending = action;
        dlg.showModal();
    }

    yes.addEventListener('click', async () => {
        if (!pending) return;
        busy(yes, true);
        try {
            await pending();
            window.location.reload();
        } catch (err) {
            $('#confirm-error').textContent = err.message;
            $('#confirm-error').hidden = false;
            busy(yes, false);
        }
    });

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
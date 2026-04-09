/* ============================================================
   ProjectPulse — Main JavaScript
   ============================================================ */

document.addEventListener('DOMContentLoaded', function () {

  // ── Auto-dismiss alerts after 4s ──────────────────────────
  document.querySelectorAll('.alert[data-auto-dismiss]').forEach(el => {
    setTimeout(() => {
      el.style.transition = 'opacity .5s';
      el.style.opacity = '0';
      setTimeout(() => el.remove(), 500);
    }, 4000);
  });

  // ── Confirm dialogs on dangerous forms ────────────────────
  document.querySelectorAll('[data-confirm]').forEach(el => {
    el.addEventListener('click', function (e) {
      const msg = this.getAttribute('data-confirm');
      if (!confirm(msg)) e.preventDefault();
    });
  });

  // ── Mobile sidebar toggle ──────────────────────────────────
  const sidebarToggle = document.getElementById('sidebarToggle');
  const sidebar = document.querySelector('.sidebar');
  if (sidebarToggle && sidebar) {
    sidebarToggle.addEventListener('click', () => sidebar.classList.toggle('open'));
    // close on outside click
    document.addEventListener('click', e => {
      if (sidebar.classList.contains('open') &&
          !sidebar.contains(e.target) &&
          e.target !== sidebarToggle) {
        sidebar.classList.remove('open');
      }
    });
  }

  // ── Active sidebar link ────────────────────────────────────
  const path = window.location.pathname;
  document.querySelectorAll('.sidebar-nav a').forEach(link => {
    if (link.getAttribute('href') && path.startsWith(link.getAttribute('href')) &&
        link.getAttribute('href') !== '/') {
      link.classList.add('active');
    } else if (link.getAttribute('href') === '/' && path === '/') {
      link.classList.add('active');
    }
  });

  // ── Tooltips (Bootstrap-style fallback) ───────────────────
  document.querySelectorAll('[data-tooltip]').forEach(el => {
    el.title = el.getAttribute('data-tooltip');
  });

  // ── Character counters ────────────────────────────────────
  document.querySelectorAll('[data-maxlength]').forEach(el => {
    const max = parseInt(el.getAttribute('data-maxlength'));
    const counter = document.createElement('small');
    counter.className = 'text-muted fs-12';
    el.parentNode.appendChild(counter);
    const update = () => {
      const rem = max - el.value.length;
      counter.textContent = `${el.value.length}/${max}`;
      counter.style.color = rem < 20 ? 'var(--danger)' : 'var(--text-muted)';
    };
    el.addEventListener('input', update);
    update();
  });

  // ── File input preview ────────────────────────────────────
  const fileInput = document.getElementById('fileInput');
  const fileLabel = document.getElementById('fileLabel');
  if (fileInput && fileLabel) {
    fileInput.addEventListener('change', function () {
      if (this.files.length > 0) {
        const f = this.files[0];
        const size = (f.size / 1024 / 1024).toFixed(2);
        fileLabel.innerHTML = `<i class="bi bi-file-earmark-check"></i> ${f.name} <span class="text-muted">(${size} MB)</span>`;
        fileLabel.style.borderColor = 'var(--success)';
      }
    });
  }

  // ── Progress bar animation ─────────────────────────────────
  document.querySelectorAll('.progress-bar-fill[data-width]').forEach(el => {
    const w = el.getAttribute('data-width');
    setTimeout(() => { el.style.width = w + '%'; }, 200);
  });

});

/* ── Utility: format bytes ─────────────────────────────────── */
function formatBytes(bytes) {
  if (bytes < 1024) return bytes + ' B';
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB';
  return (bytes / 1024 / 1024).toFixed(2) + ' MB';
}

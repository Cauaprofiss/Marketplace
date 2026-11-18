(() => {
  const tabs = [...document.querySelectorAll('.schedule-tab')];
  const cards = [...document.querySelectorAll('.appointment-card')];
  const empty = document.getElementById('scheduleEmpty');
  const matches = (card, tab) => tab.dataset.statuses.split(',').includes(card.dataset.status)
    && card.dataset.role === (tab.dataset.role === 'all' ? 'buyer' : tab.dataset.role);
  tabs.forEach(tab => {
    tab.querySelector('.schedule-count').textContent = cards.filter(card => matches(card, tab)).length;
  });
  const applyFilter = tab => {
    tabs.forEach(item => {
      item.classList.toggle('active', item === tab);
      item.setAttribute('aria-pressed', String(item === tab));
    });
    cards.forEach(card => { card.hidden = !matches(card, tab); });
    empty.classList.toggle('visible', !cards.some(card => !card.hidden));
  };
  tabs.forEach(tab => tab.addEventListener('click', () => applyFilter(tab)));
  if (tabs[0]) applyFilter(tabs[0]);
  const next = cards.find(card => card.dataset.role === 'buyer' && ['pendente', 'confirmado'].includes(card.dataset.status));
  if (next) {
    const badge = document.querySelector('.schedule-heading-badge');
    badge.lastChild.textContent = next.dataset.status === 'confirmado' ? ' Próxima retirada confirmada' : ' Próxima retirada agendada';
  }

  const modal = document.getElementById('cancelModal');
  const form = document.getElementById('cancelForm');
  const description = document.getElementById('cancelDesc');
  let trigger;
  const closeModal = () => {
    modal.classList.remove('open');
    modal.setAttribute('aria-hidden', 'true');
    document.body.style.overflow = '';
    trigger?.focus();
  };
  document.querySelectorAll('.open-cancel-btn').forEach(button => button.addEventListener('click', () => {
    trigger = button;
    form.action = '/agendamentos/' + button.dataset.id + '/cancelar';
    description.textContent = button.dataset.person + ' será informado(a). Você poderá combinar um novo horário pelo chat.';
    modal.classList.add('open');
    modal.setAttribute('aria-hidden', 'false');
    document.body.style.overflow = 'hidden';
    modal.querySelector('[data-cancel-keep]').focus();
  }));
  modal.querySelectorAll('[data-cancel-close], [data-cancel-keep]').forEach(button => button.addEventListener('click', closeModal));
  modal.addEventListener('mousedown', event => { if (event.target === modal) closeModal(); });
  document.addEventListener('keydown', event => {
    if (!modal.classList.contains('open')) return;
    if (event.key === 'Escape') closeModal();
    if (event.key === 'Tab') {
      const focusable = [...modal.querySelectorAll('button, [href], input, select, textarea, [tabindex="0"]')];
      const first = focusable[0], last = focusable[focusable.length - 1];
      if (event.shiftKey && document.activeElement === first) { event.preventDefault(); last.focus(); }
      else if (!event.shiftKey && document.activeElement === last) { event.preventDefault(); first.focus(); }
    }
  });
  document.querySelectorAll('.open-report-btn').forEach(button => button.addEventListener('click', () => {
    const motivo = window.prompt('Conte brevemente o que aconteceu (opcional):');
    if (motivo === null) return;
    const reportForm = document.createElement('form');
    reportForm.method = 'post';
    reportForm.action = '/agendamentos/' + button.dataset.id + '/reportar-problema';
    const input = document.createElement('input');
    input.type = 'hidden'; input.name = 'motivo'; input.value = motivo;
    reportForm.appendChild(input); document.body.appendChild(reportForm); reportForm.submit();
  }));
})();

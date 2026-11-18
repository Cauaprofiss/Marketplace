(() => {
  const optionsButtons = document.querySelectorAll('[data-options-toggle]');
  const statusModal = document.getElementById('statusModal');
  const deleteModal = document.getElementById('deleteModal');
  const statusForm = document.getElementById('statusForm');
  const deleteForm = document.getElementById('deleteForm');
  const statusValue = document.getElementById('statusValue');
  const statusConfirm = statusForm?.querySelector('.modal-confirm');

  const closeMenus = except => {
    document.querySelectorAll('.options-menu.show').forEach(menu => {
      if (menu !== except) {
        menu.classList.remove('show');
        menu.previousElementSibling?.setAttribute('aria-expanded', 'false');
      }
    });
  };

  optionsButtons.forEach(button => {
    button.addEventListener('click', event => {
      event.stopPropagation();
      const menu = button.nextElementSibling;
      const open = !menu.classList.contains('show');
      closeMenus(menu);
      menu.classList.toggle('show', open);
      button.setAttribute('aria-expanded', String(open));
    });
  });

  document.addEventListener('click', event => {
    if (!event.target.closest('.options-wrapper')) closeMenus();
  });

  const setModal = (modal, open) => {
    if (!modal) return;
    modal.classList.toggle('show', open);
    modal.setAttribute('aria-hidden', String(!open));
  };

  document.querySelectorAll('[data-status-open]').forEach(button => {
    button.addEventListener('click', () => {
      const currentStatus = button.dataset.status;
      const canPause = currentStatus === 'disponivel';
      const canReactivate = currentStatus === 'indisponivel';
      const nextStatus = canPause ? 'indisponivel' : (canReactivate ? 'disponivel' : '');
      const statusLabels = {
        disponivel: 'ativo',
        indisponivel: 'pausado',
        em_revisao: 'em revisão',
        rascunho: 'rascunho',
        reprovado: 'reprovado'
      };
      statusForm.action = `/anuncios/${button.dataset.id}/status`;
      document.getElementById('statusProductName').textContent = button.dataset.nome;
      document.getElementById('statusProductInfo').textContent = `R$ ${button.dataset.valor} • ${statusLabels[currentStatus] || currentStatus}`;
      statusValue.value = nextStatus;
      document.querySelectorAll('.status-option').forEach(option => {
        const enabled = (option.dataset.status === 'indisponivel' && canPause) ||
          (option.dataset.status === 'disponivel' && canReactivate);
        option.disabled = !enabled;
        option.classList.toggle('selected', enabled);
      });
      if (statusConfirm) statusConfirm.disabled = nextStatus === '';
      closeMenus();
      setModal(statusModal, true);
    });
  });

  document.querySelectorAll('.status-option').forEach(option => {
    option.addEventListener('click', () => {
      if (option.disabled) return;
      document.querySelectorAll('.status-option').forEach(item => item.classList.remove('selected'));
      option.classList.add('selected');
      statusValue.value = option.dataset.status;
    });
  });

  document.querySelectorAll('[data-status-close]').forEach(button => button.addEventListener('click', () => setModal(statusModal, false)));
  document.querySelectorAll('[data-delete-open]').forEach(button => button.addEventListener('click', () => {
    closeMenus();
    deleteForm.action = `/anuncios/${button.dataset.id}/deletar`;
    setModal(deleteModal, true);
  }));
  document.querySelectorAll('[data-delete-close]').forEach(button => button.addEventListener('click', () => setModal(deleteModal, false)));
  [statusModal, deleteModal].forEach(modal => modal?.addEventListener('mousedown', event => {
    if (event.target === modal) setModal(modal, false);
  }));

  document.addEventListener('keydown', event => {
    if (event.key !== 'Escape') return;
    closeMenus();
    setModal(statusModal, false);
    setModal(deleteModal, false);
  });
})();

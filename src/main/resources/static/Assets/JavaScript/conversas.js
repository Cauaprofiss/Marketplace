(() => {
  const search = document.getElementById('conversationSearch');
  const items = [...document.querySelectorAll('.conversation-item')];
  const filterButtons = [...document.querySelectorAll('[data-conversation-filter]')];
  const empty = document.querySelector('[data-filter-empty]');
  const unreadCount = document.querySelector('[data-unread-count]');
  let activeFilter = 'all';

  const normalize = value => (value || '')
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .toLocaleLowerCase('pt-BR')
    .trim();

  const refreshList = () => {
    const term = normalize(search?.value);
    let visible = 0;
    items.forEach(item => {
      const matchesText = normalize(item.textContent).includes(term);
      const matchesFilter = activeFilter === 'all'
        || (activeFilter === 'unread' && item.dataset.unread === 'true')
        || (activeFilter === 'product' && item.dataset.product === 'true');
      item.hidden = !(matchesText && matchesFilter);
      if (!item.hidden) visible += 1;
    });
    if (empty) empty.hidden = visible > 0 || items.length === 0;
  };

  if (unreadCount) {
    const total = items.filter(item => item.dataset.unread === 'true').length;
    unreadCount.textContent = String(total);
    unreadCount.hidden = total === 0;
  }
  search?.addEventListener('input', refreshList);
  filterButtons.forEach(button => button.addEventListener('click', () => {
    activeFilter = button.dataset.conversationFilter;
    filterButtons.forEach(item => item.classList.toggle('active', item === button));
    refreshList();
  }));

  const messages = document.getElementById('messagesContainer');
  if (messages) messages.scrollTop = messages.scrollHeight;

  const menuToggle = document.querySelector('[data-chat-menu-toggle]');
  const menu = document.querySelector('[data-chat-menu]');
  const closeMenu = () => {
    if (!menu || !menuToggle) return;
    menu.hidden = true;
    menuToggle.setAttribute('aria-expanded', 'false');
  };
  menuToggle?.addEventListener('click', event => {
    event.stopPropagation();
    const opening = menu.hidden;
    menu.hidden = !opening;
    menuToggle.setAttribute('aria-expanded', String(opening));
  });
  document.addEventListener('click', event => {
    if (menu && !menu.hidden && !menu.contains(event.target)) closeMenu();
  });

  const deleteModal = document.querySelector('[data-delete-conversation-modal]');
  const deleteOpen = document.querySelector('[data-delete-conversation-open]');
  const closeDeleteModal = () => {
    if (!deleteModal) return;
    deleteModal.classList.remove('open');
    deleteModal.setAttribute('aria-hidden', 'true');
    deleteOpen?.focus();
  };
  deleteOpen?.addEventListener('click', () => {
    closeMenu();
    deleteModal?.classList.add('open');
    deleteModal?.setAttribute('aria-hidden', 'false');
    deleteModal?.querySelector('[data-delete-conversation-cancel]')?.focus();
  });
  deleteModal?.querySelectorAll('[data-delete-conversation-close], [data-delete-conversation-cancel]')
    .forEach(button => button.addEventListener('click', closeDeleteModal));
  deleteModal?.addEventListener('click', event => {
    if (event.target === deleteModal) closeDeleteModal();
  });
  document.addEventListener('keydown', event => {
    if (event.key !== 'Escape') return;
    if (deleteModal?.classList.contains('open')) closeDeleteModal();
    else closeMenu();
  });
})();

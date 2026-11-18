(() => {
  const notificationButton = document.querySelector('[data-notifications]');
  const notificationPopover = document.querySelector('.notification-popover');
  const profileButton = document.querySelector('[data-profile-toggle]');
  const profilePopover = document.querySelector('.profile-popover');
  const logoutModal = document.querySelector('[data-logout-modal]');
  const searchForm = document.querySelector('.header-search');
  if (!notificationButton || !notificationPopover || !profileButton || !profilePopover || !logoutModal || !searchForm) return;

  let notificationsRead = false;
  const markNotificationsRead = () => {
    notificationButton.querySelector('.notification-badge')?.remove();
    notificationPopover.querySelectorAll('.notification-entry.unread').forEach(entry => {
      entry.classList.remove('unread');
      entry.classList.add('read');
    });
    if (notificationsRead) return;
    notificationsRead = true;
    fetch('/notificacoes/ler-todas', { method: 'POST', credentials: 'same-origin' })
      .catch(() => { notificationsRead = false; });
  };
  const setNotificationsOpen = open => {
    notificationPopover.classList.toggle('open', open);
    notificationButton.setAttribute('aria-expanded', String(open));
    notificationPopover.setAttribute('aria-hidden', String(!open));
    if (open) markNotificationsRead();
  };
  const setProfileOpen = open => {
    profilePopover.classList.toggle('open', open);
    profileButton.setAttribute('aria-expanded', String(open));
    profilePopover.setAttribute('aria-hidden', String(!open));
  };
  const setLogoutOpen = open => {
    logoutModal.classList.toggle('open', open);
    logoutModal.setAttribute('aria-hidden', String(!open));
    if (open) setProfileOpen(false);
  };

  notificationButton.addEventListener('click', event => { event.stopPropagation(); setProfileOpen(false); setNotificationsOpen(!notificationPopover.classList.contains('open')); });
  profileButton.addEventListener('click', event => { event.preventDefault(); event.stopPropagation(); setNotificationsOpen(false); setProfileOpen(!profilePopover.classList.contains('open')); });
  notificationPopover.addEventListener('click', event => event.stopPropagation());
  profilePopover.addEventListener('click', event => event.stopPropagation());
  document.querySelector('[data-notifications-close]')?.addEventListener('click', () => setNotificationsOpen(false));
  document.querySelector('[data-profile-close]')?.addEventListener('click', () => setProfileOpen(false));

  const updateNotificationEmptyState = () => {
    const hasEntries = Boolean(notificationPopover.querySelector('[data-notification-entry]'));
    notificationPopover.querySelector('[data-notification-list]')?.classList.toggle('is-hidden', !hasEntries);
    notificationPopover.querySelector('[data-notification-empty]')?.classList.toggle('is-hidden', hasEntries);
  };

  notificationPopover.querySelectorAll('[data-notification-remove]').forEach(button => {
    button.addEventListener('click', async event => {
      event.preventDefault();
      event.stopPropagation();
      if (button.disabled) return;
      button.disabled = true;
      try {
        const response = await fetch(button.dataset.deleteUrl, { method: 'DELETE', credentials: 'same-origin' });
        if (!response.ok) throw new Error('Não foi possível limpar a notificação.');
        button.closest('[data-notification-entry]')?.remove();
        updateNotificationEmptyState();
      } catch (error) {
        button.disabled = false;
      }
    });
  });

  notificationPopover.querySelector('[data-notifications-clear]')?.addEventListener('click', async event => {
    const button = event.currentTarget;
    event.preventDefault();
    event.stopPropagation();
    if (button.disabled) return;
    button.disabled = true;
    try {
      const response = await fetch(button.dataset.clearUrl, { method: 'DELETE', credentials: 'same-origin' });
      if (!response.ok) throw new Error('Não foi possível limpar as notificações.');
      notificationPopover.querySelectorAll('[data-notification-entry]').forEach(entry => entry.remove());
      updateNotificationEmptyState();
    } catch (error) {
      button.disabled = false;
    }
  });
  document.querySelectorAll('[data-logout-open]').forEach(button => {
    button.addEventListener('click', event => {
      event.preventDefault();
      setLogoutOpen(true);
    });
  });
  document.querySelector('[data-logout-close]')?.addEventListener('click', () => setLogoutOpen(false));
  document.querySelector('[data-logout-cancel]')?.addEventListener('click', () => setLogoutOpen(false));
  logoutModal.addEventListener('mousedown', event => { if (event.target === logoutModal) setLogoutOpen(false); });

  const searchInput = searchForm.querySelector('input');
  const searchToggle = searchForm.querySelector('[data-search-toggle]');
  const setSearchExpanded = expanded => {
    searchForm.classList.toggle('expanded', expanded);
    searchToggle.setAttribute('aria-expanded', String(expanded));
    searchToggle.setAttribute('aria-label', expanded ? 'Pesquisar' : 'Expandir busca');
    if (expanded) setTimeout(() => searchInput.focus(), 220);
  };
  searchToggle.addEventListener('click', event => {
    event.preventDefault(); event.stopPropagation();
    if (!searchForm.classList.contains('expanded')) return setSearchExpanded(true);
    if (searchInput.value.trim()) searchForm.requestSubmit(); else searchInput.focus();
  });
  document.addEventListener('click', event => {
    if (!searchForm.contains(event.target) && !searchInput.value.trim()) setSearchExpanded(false);
    setNotificationsOpen(false); setProfileOpen(false);
  });
  window.addEventListener('scroll', () => { setNotificationsOpen(false); setProfileOpen(false); }, { passive: true });
  document.addEventListener('keydown', event => { if (event.key === 'Escape') { setSearchExpanded(false); setNotificationsOpen(false); setProfileOpen(false); setLogoutOpen(false); } });
})();

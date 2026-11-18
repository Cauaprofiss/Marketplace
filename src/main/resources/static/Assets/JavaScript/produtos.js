(() => {
  const cards = [...document.querySelectorAll('.product-card')];
  const grid = document.querySelector('.products-grid');
  const emptyState = document.querySelector('[data-empty-state]');
  const searchInput = document.getElementById('searchInput');
  const filterSearch = document.querySelector('[data-filter-search]');
  const minPrice = document.querySelector('[data-min-price]');
  const maxPrice = document.querySelector('[data-max-price]');
  const availableOnly = document.querySelector('[data-available-only]');
  const sortSelect = document.querySelector('[data-sort]');
  const resultCount = document.querySelector('[data-result-count]');
  const resultLabel = document.querySelector('[data-result-label]');
  const filterCount = document.querySelector('[data-filter-count]');
  const sortLabel = document.querySelector('[data-sort-label]');
  const filters = document.querySelector('[data-filters]');
  let selectedCategory = 'todos';

  const normalize = value => (value || '').toString().trim().toLocaleLowerCase('pt-BR').normalize('NFD').replace(/[\u0300-\u036f]/g, '');
  const number = value => Number.parseFloat(String(value || '0').replace(',', '.')) || 0;
  const labels = { relevance: 'Mais relevantes', priceAsc: 'Menor preço', priceDesc: 'Maior preço', recent: 'Mais recentes' };

  function updateCount(count) {
    if (resultCount) resultCount.textContent = count;
    if (resultLabel) resultLabel.textContent = count === 1 ? 'resultado' : 'resultados';
    if (filterCount) filterCount.textContent = count;
  }

  function applyFilters() {
    const query = normalize(filterSearch?.value || searchInput?.value);
    const minimum = number(minPrice?.value);
    const maximum = maxPrice?.value === '' ? Number.POSITIVE_INFINITY : number(maxPrice?.value);
    const visible = cards.filter(card => {
      const haystack = normalize(`${card.dataset.name} ${card.dataset.description} ${card.dataset.category}`);
      const categoryMatch = selectedCategory === 'todos' || normalize(card.dataset.category) === normalize(selectedCategory);
      const price = number(card.dataset.price);
      const status = normalize(card.dataset.status);
      const stock = number(card.dataset.stock);
      const availabilityMatch = !availableOnly?.checked || (status === 'disponivel' && stock > 0);
      const show = (!query || haystack.includes(query)) && categoryMatch && price >= minimum && price <= maximum && availabilityMatch;
      card.hidden = !show;
      return show;
    });

    const sorter = sortSelect?.value || 'relevance';
    if (sorter !== 'relevance' && grid) {
      visible.sort((a, b) => {
        if (sorter === 'priceAsc') return number(a.dataset.price) - number(b.dataset.price);
        if (sorter === 'priceDesc') return number(b.dataset.price) - number(a.dataset.price);
        if (sorter === 'recent') return String(b.dataset.date).localeCompare(String(a.dataset.date));
        return 0;
      }).forEach(card => grid.appendChild(card));
    }

    updateCount(visible.length);
    if (sortLabel) sortLabel.textContent = labels[sorter];
    if (emptyState && cards.length) emptyState.classList.toggle('show', visible.length === 0);
  }

  document.querySelectorAll('[data-category]').forEach(button => {
    button.addEventListener('click', () => {
      document.querySelectorAll('[data-category]').forEach(item => item.classList.remove('active'));
      button.classList.add('active');
      selectedCategory = button.dataset.category;
      applyFilters();
    });
  });

  document.querySelector('[data-apply-filters]')?.addEventListener('click', () => {
    applyFilters();
    if (window.matchMedia('(max-width: 980px)').matches) {
      filters?.classList.remove('open');
      document.querySelector('[data-filter-toggle]')?.setAttribute('aria-expanded', 'false');
    }
  });

  document.querySelector('[data-clear-filters]')?.addEventListener('click', () => {
    selectedCategory = 'todos';
    document.querySelectorAll('[data-category]').forEach((item, index) => item.classList.toggle('active', index === 0));
    if (filterSearch) filterSearch.value = '';
    if (searchInput) searchInput.value = '';
    if (minPrice) minPrice.value = '0';
    if (maxPrice) maxPrice.value = maxPrice.defaultValue;
    if (availableOnly) availableOnly.checked = true;
    if (sortSelect) sortSelect.value = 'relevance';
    applyFilters();
  });

  filterSearch?.addEventListener('input', applyFilters);
  searchInput?.addEventListener('input', () => {
    if (filterSearch) filterSearch.value = searchInput.value;
    applyFilters();
  });
  sortSelect?.addEventListener('change', applyFilters);

  document.querySelectorAll('.favorite-button').forEach(button => {
    button.addEventListener('click', async event => {
      event.preventDefault();
      event.stopPropagation();
      const productId = button.dataset.productId;
      if (!productId || button.disabled) return;
      button.disabled = true;
      try {
        const response = await fetch(`/favoritos/${productId}/alternar`, { method: 'POST' });
        if (response.status === 401) { window.location.href = '/login'; return; }
        if (!response.ok) throw new Error('Não foi possível atualizar os favoritos.');
        const result = await response.json();
        button.classList.toggle('active', result.favorito);
        button.setAttribute('aria-label', result.favorito ? 'Remover dos favoritos' : 'Adicionar aos favoritos');
        button.title = result.favorito ? 'Remover dos favoritos' : 'Adicionar aos favoritos';
        CampusFeedback.success(result.favorito ? 'Produto adicionado aos favoritos.' : 'Produto removido dos favoritos.');
      } catch {
        CampusFeedback.error('Não foi possível atualizar os favoritos. Tente novamente.');
      } finally {
        button.disabled = false;
      }
    });
  });

  const filterToggle = document.querySelector('[data-filter-toggle]');
  filterToggle?.addEventListener('click', () => {
    const open = filters?.classList.toggle('open') || false;
    filterToggle.setAttribute('aria-expanded', String(open));
  });
  document.addEventListener('keydown', event => {
    if (event.key === 'Escape') {
      filters?.classList.remove('open');
      filterToggle?.setAttribute('aria-expanded', 'false');
    }
  });

  document.querySelectorAll('.header-search nav a').forEach(link => {
    link.classList.toggle('active', link.getAttribute('href')?.includes('/produtos'));
  });

  applyFilters();
})();
